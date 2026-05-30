package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.MLPredictionRequestDTO;
import com.upc.tp1inventory.DTO.MLPredictionResponseDTO;
import com.upc.tp1inventory.DTO.MLServicePredictionDTO;
import com.upc.tp1inventory.DTO.WeeklyGlobalDishPredictionDTO;
import com.upc.tp1inventory.DTO.WeeklyGlobalPredictionResponseDTO;
import com.upc.tp1inventory.DTO.WeeklyGlobalSupplyItemDTO;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        MLPredictionRequestDTO request = new MLPredictionRequestDTO();
        request.setDishId(null);
        request.setWeeksAhead(weeksAhead);
        request.setSaveToDb(false);
        request.setCreatedBy("ADMIN");

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
            return buildEmptyResponse();
        }

        /*
         * Por ahora el plan semanal trabaja con una sola semana objetivo.
         * Si FastAPI devuelve varias semanas, usamos la primera semana disponible
         * para evitar sumar incorrectamente varias semanas en un solo plan.
         */
        String targetWeek = predictions.get(0).getWeekStart();

        List<MLServicePredictionDTO> targetWeekPredictions = predictions.stream()
                .filter(pred -> pred.getWeekStart() != null)
                .filter(pred -> pred.getWeekStart().equals(targetWeek))
                .toList();

        if (targetWeekPredictions.isEmpty()) {
            return buildEmptyResponse();
        }

        List<WeeklyGlobalDishPredictionDTO> dishPredictions = new ArrayList<>();
        Map<UUID, WeeklyGlobalSupplyItemDTO> suppliesMap = new LinkedHashMap<>();
        Map<UUID, Dish> dishCache = new HashMap<>();

        for (MLServicePredictionDTO pred : targetWeekPredictions) {
            if (pred.getPredictedDemand() == null || pred.getPredictedDemand() <= 0) {
                continue;
            }

            UUID dishId;

            try {
                dishId = UUID.fromString(pred.getDishId());
            } catch (Exception e) {
                continue;
            }

            Dish dish = dishCache.computeIfAbsent(
                    dishId,
                    id -> dishRepository.findById(id).orElse(null)
            );

            if (dish == null) {
                continue;
            }

            WeeklyGlobalDishPredictionDTO dishDto = new WeeklyGlobalDishPredictionDTO();
            dishDto.setDishId(dishId);
            dishDto.setDishName(dish.getName());
            dishDto.setWeekStart(pred.getWeekStart());
            dishDto.setPredictedDemand(pred.getPredictedDemand());
            dishDto.setConfidence(pred.getConfidence());

            dishPredictions.add(dishDto);

            if (dish.getIngredients() == null || dish.getIngredients().isEmpty()) {
                continue;
            }

            BigDecimal predictedDemand = BigDecimal.valueOf(pred.getPredictedDemand());

            for (DishIngredient di : dish.getIngredients()) {
                Product product = di.getProduct();

                if (product == null || product.getId() == null) {
                    continue;
                }

                BigDecimal quantityPerDish = di.getQuantityNeeded() != null
                        ? di.getQuantityNeeded()
                        : BigDecimal.ZERO;

                if (quantityPerDish.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal requiredForThisDish = quantityPerDish.multiply(predictedDemand);

                addSupplyRequirement(
                        suppliesMap,
                        product,
                        requiredForThisDish
                );
            }
        }

        enrichSupplyItemsWithInventory(suppliesMap);

        WeeklyGlobalPredictionResponseDTO response = new WeeklyGlobalPredictionResponseDTO();
        response.setWeekStart(targetWeek);
        response.setDishes(dishPredictions);
        response.setSupplies(new ArrayList<>(suppliesMap.values()));

        return response;
    }

    private void addSupplyRequirement(Map<UUID, WeeklyGlobalSupplyItemDTO> suppliesMap,
                                      Product product,
                                      BigDecimal requiredForThisDish) {

        UUID productId = product.getId();

        WeeklyGlobalSupplyItemDTO supplyItem = suppliesMap.get(productId);

        if (supplyItem == null) {
            supplyItem = new WeeklyGlobalSupplyItemDTO();
            supplyItem.setProductId(productId);
            supplyItem.setProductName(product.getName());
            supplyItem.setUnitMeasure(product.getUnitMeasure());
            supplyItem.setTotalRequired(requiredForThisDish);

            suppliesMap.put(productId, supplyItem);
            return;
        }

        BigDecimal currentRequired = supplyItem.getTotalRequired() != null
                ? supplyItem.getTotalRequired()
                : BigDecimal.ZERO;

        supplyItem.setTotalRequired(currentRequired.add(requiredForThisDish));
    }

    private void enrichSupplyItemsWithInventory(Map<UUID, WeeklyGlobalSupplyItemDTO> suppliesMap) {
        for (WeeklyGlobalSupplyItemDTO item : suppliesMap.values()) {
            Inventory inventory = inventoryRepository
                    .findByProduct_Id(item.getProductId())
                    .orElse(null);

            BigDecimal currentStock = BigDecimal.ZERO;
            BigDecimal availableStock = BigDecimal.ZERO;

            if (inventory != null) {
                currentStock = inventory.getCurrentStock() != null
                        ? inventory.getCurrentStock()
                        : BigDecimal.ZERO;

                availableStock = inventory.getAvailableStock() != null
                        ? inventory.getAvailableStock()
                        : currentStock;
            }

            item.setCurrentStock(currentStock);
            item.setAvailableStock(availableStock);

            BigDecimal totalRequired = item.getTotalRequired() != null
                    ? item.getTotalRequired()
                    : BigDecimal.ZERO;

            BigDecimal quantityToBuy = totalRequired.subtract(availableStock);

            if (quantityToBuy.compareTo(BigDecimal.ZERO) < 0) {
                quantityToBuy = BigDecimal.ZERO;
            }

            item.setQuantityToBuy(quantityToBuy);
        }
    }

    private WeeklyGlobalPredictionResponseDTO buildEmptyResponse() {
        WeeklyGlobalPredictionResponseDTO empty = new WeeklyGlobalPredictionResponseDTO();
        empty.setWeekStart(null);
        empty.setDishes(List.of());
        empty.setSupplies(List.of());
        return empty;
    }
}