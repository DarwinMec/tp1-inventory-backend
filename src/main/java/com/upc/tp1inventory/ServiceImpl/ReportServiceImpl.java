package com.upc.tp1inventory.ServiceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upc.tp1inventory.DTO.ReportDTO;
import com.upc.tp1inventory.DTO.SalesReportRequestDTO;
import com.upc.tp1inventory.Entity.Report;
import com.upc.tp1inventory.Entity.Sale;
import com.upc.tp1inventory.Entity.SaleItem;
import com.upc.tp1inventory.Entity.User;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.ReportRepository;
import com.upc.tp1inventory.Repository.SaleRepository;
import com.upc.tp1inventory.Repository.UserRepository;
import com.upc.tp1inventory.Service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final String SALES_SUMMARY_TYPE = "SALES_SUMMARY";

    public ReportServiceImpl(ReportRepository reportRepository,
                             SaleRepository saleRepository,
                             UserRepository userRepository,
                             ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.saleRepository = saleRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<ReportDTO> getAll() {
        return reportRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReportDTO generateSalesSummary(SalesReportRequestDTO request, String username) {

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();

        if (start == null || end == null) {
            throw new IllegalArgumentException("Debe especificar fecha de inicio y de fin");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        // 1️⃣ Obtener ventas del rango
        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);

        int totalOrders = sales.size();
        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        int totalItems = 0;

        Map<String, Integer> dishQuantityMap = new HashMap<>();

        for (Sale sale : sales) {
            if (sale.getTotalAmount() != null) {
                totalSalesAmount = totalSalesAmount.add(sale.getTotalAmount());
            }

            if (sale.getItems() != null) {
                for (SaleItem item : sale.getItems()) {
                    int q = item.getQuantity() != null ? item.getQuantity() : 0;
                    totalItems += q;

                    String dishName = item.getDish().getName();
                    dishQuantityMap.merge(dishName, q, Integer::sum);
                }
            }
        }

        // Top 5 platos más vendidos
        List<Map<String, Object>> topDishes = dishQuantityMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("dishName", e.getKey());
                    m.put("totalQuantity", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());

        // 2️⃣ Construir JSON de parámetros/resultado
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("startDate", start);
        payload.put("endDate", end);
        payload.put("totalOrders", totalOrders);
        payload.put("totalSalesAmount", totalSalesAmount);
        payload.put("totalItems", totalItems);
        payload.put("topDishes", topDishes);

        Report report = Report.builder()
                .reportType(SALES_SUMMARY_TYPE)
                .title("Resumen de ventas del " + start + " al " + end)
                .parameters(payload)   // ✅ ahora Map -> jsonb
                .fileFormat("JSON")
                .generatedBy(user)
                .build();

        Report saved = reportRepository.save(report);

        return toDTO(saved);
    }

    private ReportDTO toDTO(Report report) {
        ReportDTO dto = new ReportDTO();
        dto.setId(report.getId());
        dto.setReportType(report.getReportType());
        dto.setTitle(report.getTitle());
        dto.setParametersJson(report.getParameters());
        dto.setFilePath(report.getFilePath());
        dto.setFileFormat(report.getFileFormat());
        dto.setGeneratedAt(report.getGeneratedAt());
        if (report.getGeneratedBy() != null) {
            dto.setGeneratedByUsername(report.getGeneratedBy().getUsername());
        }
        return dto;
    }

}
