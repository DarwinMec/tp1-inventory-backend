package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.InventoryTransactionDTO;
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
import com.upc.tp1inventory.Service.InventoryTransactionService;
import com.upc.tp1inventory.Service.SaleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final DishRepository dishRepository;
    private final InventoryService inventoryService;
    private final InventoryTransactionService inventoryTransactionService;
    private final DishIngredientRepository dishIngredientRepository;

    public SaleServiceImpl(SaleRepository saleRepository,
                           SaleItemRepository saleItemRepository,
                           DishRepository dishRepository,
                           InventoryService inventoryService,
                           InventoryTransactionService inventoryTransactionService,
                           DishIngredientRepository dishIngredientRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.dishRepository = dishRepository;
        this.inventoryService = inventoryService;
        this.inventoryTransactionService = inventoryTransactionService;
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
    public Page<SaleDTO> getAllPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("saleDate").descending()
                        .and(Sort.by("saleTime").descending())
        );

        return saleRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @Override
    public SaleDTO getById(UUID id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + id));
        return toDTO(sale);
    }

    @Override
    @Transactional
    public SaleDTO create(SaleDTO dto, String username) {

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un ítem");
        }

        Map<UUID, Dish> dishesById = new HashMap<>();
        Map<UUID, ProductConsumption> consumptionByProduct = new HashMap<>();

        /*
         * 1. Validar ítems de venta y calcular consumo acumulado por insumo.
         * Esto evita errores cuando varios platillos consumen el mismo producto.
         */
        for (SaleItemDTO itemDTO : dto.getItems()) {

            Dish dish = dishRepository.findById(itemDTO.getDishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + itemDTO.getDishId()));

            dishesById.put(dish.getId(), dish);

            Integer quantity = itemDTO.getQuantity();

            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Cantidad inválida para plato: " + dish.getName());
            }

            List<DishIngredient> ingredients = dishIngredientRepository.findByDish(dish);

            if (ingredients == null || ingredients.isEmpty()) {
                throw new IllegalArgumentException(
                        "El plato '" + dish.getName() + "' no tiene ingredientes registrados en su receta"
                );
            }

            for (DishIngredient ing : ingredients) {

                if (ing.getQuantityNeeded() == null || ing.getQuantityNeeded().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException(
                            "La cantidad requerida del producto '" + ing.getProduct().getName() +
                                    "' en el plato '" + dish.getName() + "' no es válida"
                    );
                }

                BigDecimal requiredAmount = ing.getQuantityNeeded()
                        .multiply(BigDecimal.valueOf(quantity));

                UUID productId = ing.getProduct().getId();
                String productName = ing.getProduct().getName();

                ProductConsumption current = consumptionByProduct.get(productId);

                if (current == null) {
                    current = new ProductConsumption(productName, BigDecimal.ZERO);
                }

                current.setQuantity(current.getQuantity().add(requiredAmount));
                consumptionByProduct.put(productId, current);
            }
        }

        /*
         * 2. Validar stock acumulado antes de registrar la venta.
         * Si falta stock en un ingrediente, no se guarda nada.
         */
        for (Map.Entry<UUID, ProductConsumption> entry : consumptionByProduct.entrySet()) {

            UUID productId = entry.getKey();
            ProductConsumption consumption = entry.getValue();

            boolean hasStock = inventoryService.hasSufficientStock(
                    productId,
                    consumption.getQuantity().doubleValue()
            );

            if (!hasStock) {
                throw new IllegalArgumentException(
                        "Stock insuficiente del producto '" + consumption.getProductName() +
                                "'. Requerido total: " + consumption.getQuantity()
                );
            }
        }

        /*
         * 3. Crear cabecera de venta.
         */
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
        List<SaleItem> savedItems = new ArrayList<>();

        /*
         * 4. Registrar detalle de venta.
         */
        for (SaleItemDTO itemDTO : dto.getItems()) {

            Dish dish = dishesById.get(itemDTO.getDishId());

            if (dish == null) {
                dish = dishRepository.findById(itemDTO.getDishId())
                        .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + itemDTO.getDishId()));
            }

            int quantity = itemDTO.getQuantity();

            BigDecimal unitPrice = itemDTO.getUnitPrice() != null
                    ? itemDTO.getUnitPrice()
                    : dish.getPrice();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

            SaleItem item = new SaleItem();
            item.setSale(savedSale);
            item.setDish(dish);
            item.setQuantity(quantity);
            item.setUnitPrice(unitPrice);
            item.setTotalAmount(lineTotal);

            SaleItem savedItem = saleItemRepository.save(item);
            savedItems.add(savedItem);

            total = total.add(lineTotal);
        }

        savedSale.setTotalAmount(total);
        savedSale.setItems(savedItems);
        saleRepository.save(savedSale);

        /*
         * 5. Registrar salida automática de inventario por cada insumo consumido.
         * Aquí se descuenta stock y se crea inventory_transaction tipo outbound.
         */
        for (Map.Entry<UUID, ProductConsumption> entry : consumptionByProduct.entrySet()) {

            UUID productId = entry.getKey();
            ProductConsumption consumption = entry.getValue();

            InventoryTransactionDTO tx = new InventoryTransactionDTO();
            tx.setProductId(productId);
            tx.setTransactionType("outbound");
            tx.setQuantity(consumption.getQuantity());
            tx.setReferenceNumber("SALE-" + savedSale.getId());
            tx.setNotes("Salida automática por venta de platillos. Venta ID: " + savedSale.getId());
            tx.setTransactionDate(LocalDateTime.of(savedSale.getSaleDate(), savedSale.getSaleTime()));

            inventoryTransactionService.create(tx, username);
        }

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

    private static class ProductConsumption {

        private String productName;
        private BigDecimal quantity;

        public ProductConsumption(String productName, BigDecimal quantity) {
            this.productName = productName;
            this.quantity = quantity;
        }

        public String getProductName() {
            return productName;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }
    }
}