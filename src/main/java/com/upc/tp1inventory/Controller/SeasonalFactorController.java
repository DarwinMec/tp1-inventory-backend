package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.SeasonalFactorDTO;
import com.upc.tp1inventory.Service.SeasonalFactorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ml/seasonal-factors")
@CrossOrigin(origins = "*")
public class SeasonalFactorController {

    private final SeasonalFactorService seasonalFactorService;

    public SeasonalFactorController(SeasonalFactorService seasonalFactorService) {
        this.seasonalFactorService = seasonalFactorService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<SeasonalFactorDTO> getAll() {
        return seasonalFactorService.getAll();
    }

    @GetMapping("/by-month")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public List<SeasonalFactorDTO> getByMonth(@RequestParam int month) {
        return seasonalFactorService.getByMonth(month);
    }

    // 👉 Endpoint pensado para tu script de Python
    @PostMapping("/bulk-upsert")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<SeasonalFactorDTO>> bulkUpsert(@RequestBody List<SeasonalFactorDTO> dtos) {
        List<SeasonalFactorDTO> result = seasonalFactorService.bulkUpsert(dtos);
        return ResponseEntity.ok(result);
    }
}
