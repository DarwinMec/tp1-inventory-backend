package com.upc.tp1inventory.Controller;

import com.upc.tp1inventory.DTO.ProveedorResumenDto;
import com.upc.tp1inventory.Entity.Supplier;
import com.upc.tp1inventory.Repository.SupplierRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorQueryController {

    private final SupplierRepository supplierRepository;

    public ProveedorQueryController(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    public List<ProveedorResumenDto> getProveedoresActivos() {
        List<Supplier> suppliers = supplierRepository.findAll(); // en tu entidad Supplier ya tienes isActive

        return suppliers.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .map(s -> {
                    ProveedorResumenDto dto = new ProveedorResumenDto();
                    dto.setId(s.getId());
                    dto.setNombre(s.getName());
                    // De momento no tenemos calificación ni insumos, así que van como null
                    dto.setCalificacion(null);
                    dto.setInsumosClave(null);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
