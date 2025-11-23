package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.DashboardStatsDto;
import com.upc.tp1inventory.DTO.DashboardTendenciaDto;
import com.upc.tp1inventory.Entity.Inventory;
import com.upc.tp1inventory.Entity.Product;
import com.upc.tp1inventory.Entity.Sale;
import com.upc.tp1inventory.Repository.InventoryRepository;
import com.upc.tp1inventory.Repository.ProductRepository;
import com.upc.tp1inventory.Repository.SaleRepository;
import com.upc.tp1inventory.Repository.DishRepository;
import com.upc.tp1inventory.Repository.SupplierRepository;
import com.upc.tp1inventory.Service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ProductRepository productRepository;
    private final DishRepository dishRepository;
    private final SupplierRepository supplierRepository;
    private final SaleRepository saleRepository;
    private final InventoryRepository inventoryRepository;

    public DashboardServiceImpl(ProductRepository productRepository,
                                DishRepository dishRepository,
                                SupplierRepository supplierRepository,
                                SaleRepository saleRepository,
                                InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.dishRepository = dishRepository;
        this.supplierRepository = supplierRepository;
        this.saleRepository = saleRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public DashboardStatsDto getStats() {
        DashboardStatsDto dto = new DashboardStatsDto();

        long totalInsumos = productRepository.count();
        long totalPlatillos = dishRepository.count();
        long totalProveedores = supplierRepository.count();

        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        BigDecimal ventasHoy = sumVentasEnFecha(hoy);
        BigDecimal ventasAyer = sumVentasEnFecha(ayer);

        double variacion = 0.0;
        if (ventasAyer != null && ventasAyer.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = ventasHoy.subtract(ventasAyer);
            variacion = diff
                    .divide(ventasAyer, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        double nivelServicio = calcularNivelServicio();
        double rotacion = calcularRotacionInventario();

        dto.setTotalInsumos(totalInsumos);
        dto.setTotalPlatillos(totalPlatillos);
        dto.setTotalProveedores(totalProveedores);
        dto.setVentasHoy(ventasHoy);
        dto.setVariacionVentasPorcentaje(variacion);
        dto.setNivelServicio(nivelServicio);
        dto.setRotacionInventario(rotacion);

        return dto;
    }

    private BigDecimal sumVentasEnFecha(LocalDate fecha) {
        List<Sale> ventas = saleRepository.findBySaleDateBetween(fecha, fecha);
        return ventas.stream()
                .map(Sale::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Nivel de servicio muy simple:
     * 100 - (% de productos en alerta de stock).
     */
    private double calcularNivelServicio() {
        long totalProductos = productRepository.count();
        if (totalProductos == 0) return 100.0;

        List<Inventory> inventarios = inventoryRepository.findAll();
        long productosEnAlerta = inventarios.stream()
                .filter(inv -> {
                    Product p = inv.getProduct();
                    if (p == null) return false;
                    Integer minStock = p.getMinStock();
                    if (minStock == null) return false;
                    if (inv.getCurrentStock() == null) return false;
                    return inv.getCurrentStock()
                            .compareTo(BigDecimal.valueOf(minStock)) <= 0;
                })
                .count();

        double porcentajeEnAlerta =
                (double) productosEnAlerta / (double) totalProductos * 100.0;
        double nivel = 100.0 - porcentajeEnAlerta;
        if (nivel < 0) nivel = 0;
        if (nivel > 100) nivel = 100;
        return nivel;
    }

    /**
     * Rotación de inventario (simplificada):
     * (ventas de los últimos 30 días / total de productos)
     */
    private double calcularRotacionInventario() {
        long totalProductos = productRepository.count();
        if (totalProductos == 0) return 0.0;

        LocalDate hoy = LocalDate.now();
        LocalDate inicio = hoy.minusDays(30);

        List<Sale> ventas = saleRepository.findBySaleDateBetween(inicio, hoy);
        BigDecimal totalVentas = ventas.stream()
                .map(Sale::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalVentas.compareTo(BigDecimal.ZERO) <= 0) return 0.0;

        BigDecimal rotacion = totalVentas.divide(
                BigDecimal.valueOf(totalProductos),
                2,
                RoundingMode.HALF_UP
        );
        return rotacion.doubleValue();
    }

    @Override
    public List<DashboardTendenciaDto> getTendencias(int meses) {
        if (meses <= 0) meses = 6;

        LocalDate hoy = LocalDate.now();
        List<DashboardTendenciaDto> resultado = new ArrayList<>();

        Locale localeEs = new Locale("es", "ES");

        for (int i = meses - 1; i >= 0; i--) {
            LocalDate inicioMes = hoy.minusMonths(i).withDayOfMonth(1);
            LocalDate finMes = inicioMes.plusMonths(1).minusDays(1);

            List<Sale> ventasMes = saleRepository.findBySaleDateBetween(inicioMes, finMes);
            BigDecimal totalMes = ventasMes.stream()
                    .map(Sale::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DashboardTendenciaDto dto = new DashboardTendenciaDto();
            dto.setMes(
                    inicioMes.getMonth().getDisplayName(TextStyle.SHORT, localeEs)
                            .substring(0, 1).toUpperCase() +
                            inicioMes.getMonth().getDisplayName(TextStyle.SHORT, localeEs)
                                    .substring(1)
            );
            dto.setVentas(totalMes);

            // Predicción simple: +5% sobre las ventas reales
            BigDecimal pred = totalMes.multiply(BigDecimal.valueOf(1.05))
                    .setScale(2, RoundingMode.HALF_UP);
            dto.setPrediccion(pred);

            resultado.add(dto);
        }

        return resultado;
    }
}
