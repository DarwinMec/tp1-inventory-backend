package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.*;
import com.upc.tp1inventory.Entity.Dish;
import com.upc.tp1inventory.Entity.DishIngredient;
import com.upc.tp1inventory.Entity.Inventory;
import com.upc.tp1inventory.Entity.Product;
import com.upc.tp1inventory.Repository.DishRepository;
import com.upc.tp1inventory.Repository.InventoryRepository;
import com.upc.tp1inventory.Service.MLServiceIntegration;
import com.upc.tp1inventory.Service.WeeklyGlobalSupplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeeklyGlobalSupplyServiceImpl implements WeeklyGlobalSupplyService {

    private final MLServiceIntegration mlServiceIntegration;
    private final DishRepository dishRepository;
    private final InventoryRepository inventoryRepository;

    public WeeklyGlobalSupplyServiceImpl(MLServiceIntegration mlServiceIntegration,
                                         DishRepository dishRepository,
                                         InventoryRepository inventoryRepository) {
        this.mlServiceIntegration = mlServiceIntegration;
        this.dishRepository = dishRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public WeeklyGlobalPredictionResponseDTO getWeeklyGlobalPlan(Integer weeksAhead) {
        if (weeksAhead == null || weeksAhead < 1) {
            weeksAhead = 1;
        }

        // 1) Pedimos predicciones globales al servicio de ML
        MLPredictionRequestDTO request = new MLPredictionRequestDTO();
        request.setDishId(null);             // null = TODOS los platos
        request.setWeeksAhead(weeksAhead);
        request.setSaveToDb(false);          // aquí solo queremos consulta

        MLPredictionResponseDTO mlResponse = mlServiceIntegration.generatePredictions(request);

        if (mlResponse == null || Boolean.FALSE.equals(mlResponse.getSuccess())) {
            throw new IllegalStateException(
                    mlResponse != null && mlResponse.getMessage() != null
                            ? mlResponse.getMessage()
                            : "No se pudieron obtener predicciones desde el servicio ML"
            );
        }

        List<MLServicePredictionDTO> predictions = mlResponse.getPredictions();
        if (predictions == null || predictions.isEmpty()) {
            WeeklyGlobalPredictionResponseDTO empty = new WeeklyGlobalPredictionResponseDTO();
            empty.setWeekStart(null);
            empty.setDishes(List.of());
            empty.setSupplies(List.of());
            return empty;
        }

        // Si weeksAhead > 1 podrías extender la lógica a varias semanas.
        // Por ahora nos concentramos en la primera semana (la más cercana).
        String targetWeek = predictions.get(0).getWeekStart();

        // 2) Convertimos predicciones por plato en DTOs para el frontend
        List<WeeklyGlobalDishPredictionDTO> dishPredictions = new ArrayList<>();

        // Map para agregar insumos por producto
        Map<UUID, WeeklyGlobalSupplyItemDTO> suppliesMap = new LinkedHashMap<>();

        // Cache para no buscar el mismo Dish muchas veces
        Map<UUID, Dish> dishCache = new HashMap<>();

        for (MLServicePredictionDTO pred : predictions) {
            if (pred.getPredictedDemand() == null || pred.getPredictedDemand() <= 0) {
                continue; // ignorar predicciones sin demanda
            }

            UUID dishId;
            try {
                dishId = UUID.fromString(pred.getDishId());
            } catch (Exception e) {
                // Si viene algo raro desde Python, lo saltamos
                continue;
            }

            // Obtener el plato desde BD (usando cache)
            Dish dish = dishCache.computeIfAbsent(dishId, id ->
                    dishRepository.findById(id).orElse(null)
            );

            if (dish == null) {
                continue;
            }

            // 2.a) Armar DTO de predicción por plato
            WeeklyGlobalDishPredictionDTO dishDto = new WeeklyGlobalDishPredictionDTO();
            dishDto.setDishId(dishId);
            dishDto.setDishName(dish.getName());
            dishDto.setWeekStart(pred.getWeekStart());
            dishDto.setPredictedDemand(pred.getPredictedDemand());
            dishDto.setConfidence(pred.getConfidence());

            dishPredictions.add(dishDto);

            // 3) Transformar esa demanda en insumos
            if (dish.getIngredients() == null) {
                continue;
            }

            for (DishIngredient di : dish.getIngredients()) {
                Product product = di.getProduct();
                if (product == null) continue;

                UUID productId = product.getId();
                double quantityPerDish = di.getQuantityNeeded() != null ? di.getQuantityNeeded() : 0.0;
                double predictedDemand = pred.getPredictedDemand() != null ? pred.getPredictedDemand() : 0.0;
                double requiredForThisDish = quantityPerDish * predictedDemand;

                WeeklyGlobalSupplyItemDTO supplyItem = suppliesMap.get(productId);
                if (supplyItem == null) {
                    supplyItem = new WeeklyGlobalSupplyItemDTO();
                    supplyItem.setProductId(productId);
                    supplyItem.setProductName(product.getName());
                    supplyItem.setUnitMeasure(product.getUnitMeasure());
                    supplyItem.setTotalRequired(requiredForThisDish);
                    suppliesMap.put(productId, supplyItem);
                } else {
                    supplyItem.setTotalRequired(
                            (supplyItem.getTotalRequired() != null ? supplyItem.getTotalRequired() : 0.0)
                                    + requiredForThisDish
                    );
                }
            }
        }

        // 4) Cruzar con inventario para calcular stock y cantidad a comprar
        for (WeeklyGlobalSupplyItemDTO item : suppliesMap.values()) {
            Inventory inventory = inventoryRepository
                    .findByProduct_Id(item.getProductId())
                    .orElse(null);

            double currentStock = 0.0;
            double availableStock = 0.0;

            if (inventory != null) {
                if (inventory.getCurrentStock() != null) {
                    currentStock = inventory.getCurrentStock().doubleValue();
                }
                if (inventory.getAvailableStock() != null) {
                    availableStock = inventory.getAvailableStock().doubleValue();
                } else {
                    availableStock = currentStock;
                }
            }

            item.setCurrentStock(currentStock);
            item.setAvailableStock(availableStock);

            double toBuy = (item.getTotalRequired() != null ? item.getTotalRequired() : 0.0) - availableStock;
            if (toBuy < 0) toBuy = 0.0;
            item.setQuantityToBuy(toBuy);
        }

        WeeklyGlobalPredictionResponseDTO response = new WeeklyGlobalPredictionResponseDTO();
        response.setWeekStart(targetWeek);
        response.setDishes(dishPredictions);

        // Mantener orden de inserción
        response.setSupplies(new ArrayList<>(suppliesMap.values()));

        return response;
    }
}
