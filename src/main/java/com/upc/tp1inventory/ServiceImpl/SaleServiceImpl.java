package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.SaleDTO;
import com.upc.tp1inventory.DTO.SaleItemDTO;
import com.upc.tp1inventory.Entity.Dish;
import com.upc.tp1inventory.Entity.DishIngredient;
import com.upc.tp1inventory.Entity.Sale;
import com.upc.tp1inventory.Entity.SaleItem;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.DishIngredientRepository;
import com.upc.tp1inventory.Repository.DishRepository;
import com.upc.tp1inventory.Repository.SaleItemRepository;
import com.upc.tp1inventory.Repository.SaleRepository;
import com.upc.tp1inventory.Service.InventoryService;
import com.upc.tp1inventory.Service.SaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final DishRepository dishRepository;
    private final InventoryService inventoryService;
    private final DishIngredientRepository dishIngredientRepository;


    public SaleServiceImpl(SaleRepository saleRepository,
                           SaleItemRepository saleItemRepository,
                           DishRepository dishRepository,
                           InventoryService inventoryService,
                           DishIngredientRepository dishIngredientRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.dishRepository = dishRepository;
        this.inventoryService = inventoryService;
        this.dishIngredientRepository = dishIngredientRepository;
    }


    @Override
    public List<SaleDTO> getAll() {
        return saleRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SaleDTO getById(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + id));
        return toDTO(sale);
    }

    @Override
    @Transactional
    public SaleDTO create(SaleDTO dto) {

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un ítem");
        }

        // 1️⃣ VALIDAR STOCK DE TODOS LOS INGREDIENTES (PRIMERA PASADA)
        for (SaleItemDTO itemDTO : dto.getItems()) {

            Dish dish = dishRepository.findById(itemDTO.getDishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + itemDTO.getDishId()));

            int quantity = itemDTO.getQuantity();
            if (quantity <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para plato: " + dish.getName());
            }

            // Buscar ingredientes del plato
            List<DishIngredient> ingredients = dishIngredientRepository.findByDish(dish);

            // Validar cada ingrediente
            for (DishIngredient ing : ingredients) {

                double requiredAmount = ing.getQuantityNeeded() * quantity;

                boolean hasStock = inventoryService.hasSufficientStock(
                        ing.getProduct().getId(),
                        requiredAmount
                );

                if (!hasStock) {
                    throw new IllegalArgumentException(
                            "Stock insuficiente del producto '" + ing.getProduct().getName() +
                                    "' para vender " + quantity + " unidades de '" + dish.getName() + "'."
                    );
                }
            }
        }


        // 2️⃣ CREAR LA VENTA (CABECERA)
        Sale sale = new Sale();
        sale.setSaleDate(dto.getSaleDate());
        sale.setSaleTime(dto.getSaleTime());
        sale.setWeather(dto.getWeather());
        sale.setIsHoliday(dto.getIsHoliday());
        sale.setIsWeekend(dto.getIsWeekend());
        sale.setCreatedAt(LocalDateTime.now());

        sale.setTotalAmount(BigDecimal.ZERO);
        Sale savedSale = saleRepository.save(sale);


        BigDecimal total = BigDecimal.ZERO;


        // 3️⃣ REGISTRAR ITEMS + DESCONTAR STOCK (SEGUNDA PASADA)
        for (SaleItemDTO itemDTO : dto.getItems()) {

            Dish dish = dishRepository.findById(itemDTO.getDishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + itemDTO.getDishId()));

            int quantity = itemDTO.getQuantity();
            BigDecimal unitPrice = itemDTO.getUnitPrice() != null ?
                    itemDTO.getUnitPrice() :
                    BigDecimal.valueOf(dish.getPrice());

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            // Guardar item
            SaleItem item = new SaleItem();
            item.setSale(savedSale);
            item.setDish(dish);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setTotalAmount(lineTotal);

            saleItemRepository.save(item);

            total = total.add(lineTotal);

            // Descontar inventario según la receta del plato
            List<DishIngredient> ingredients = dishIngredientRepository.findByDish(dish);

            for (DishIngredient ing : ingredients) {

                double requiredAmount = ing.getQuantityNeeded() * quantity;

                inventoryService.deductStock(
                        ing.getProduct().getId(),
                        requiredAmount
                );
            }
        }


        // 4️⃣ ACTUALIZAR TOTAL FINAL DE LA VENTA
        savedSale.setTotalAmount(total);
        saleRepository.save(savedSale);

        return toDTO(savedSale);
    }


    private SaleDTO toDTO(Sale sale) {
        SaleDTO dto = new SaleDTO();
        dto.setId(sale.getId());
        dto.setSaleDate(sale.getSaleDate());
        dto.setSaleTime(sale.getSaleTime());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setDayOfWeek(sale.getDayOfWeek());
        dto.setMonth(sale.getMonth());
        dto.setYear(sale.getYear());
        dto.setWeather(sale.getWeather());
        dto.setIsHoliday(sale.getIsHoliday());
        dto.setIsWeekend(sale.getIsWeekend());

        if (sale.getItems() != null) {
            dto.setItems(
                    sale.getItems().stream().map(item -> {
                        SaleItemDTO i = new SaleItemDTO();
                        i.setId(item.getId());
                        i.setDishId(item.getDish().getId());
                        i.setDishName(item.getDish().getName());
                        i.setQuantity(item.getQuantity());
                        i.setUnitPrice(item.getUnitPrice());
                        i.setTotalAmount(item.getTotalAmount());
                        return i;
                    }).collect(Collectors.toList())
            );
        }

        return dto;
    }
}
