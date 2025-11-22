package com.upc.tp1inventory.ServiceImpl;

import com.upc.tp1inventory.DTO.SupplierDTO;
import com.upc.tp1inventory.Entity.Supplier;
import com.upc.tp1inventory.Exception.ResourceNotFoundException;
import com.upc.tp1inventory.Repository.SupplierRepository;
import com.upc.tp1inventory.Service.SupplierService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierServiceImpl(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public List<SupplierDTO> getAll() {
        return supplierRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierDTO getById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + id));
        return toDTO(supplier);
    }

    @Override
    public SupplierDTO create(SupplierDTO dto) {
        Supplier supplier = new Supplier();
        apply(dto, supplier);
        Supplier saved = supplierRepository.save(supplier);
        return toDTO(saved);
    }

    @Override
    public SupplierDTO update(UUID id, SupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado: " + id));
        apply(dto, supplier);
        Supplier saved = supplierRepository.save(supplier);
        return toDTO(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proveedor no encontrado: " + id);
        }
        supplierRepository.deleteById(id);
    }

    private SupplierDTO toDTO(Supplier s) {
        SupplierDTO dto = new SupplierDTO();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setContactPerson(s.getContactPerson());
        dto.setPhone(s.getPhone());
        dto.setEmail(s.getEmail());
        dto.setAddress(s.getAddress());
        dto.setCity(s.getCity());
        dto.setRegion(s.getRegion());
        dto.setIsActive(s.getIsActive());
        return dto;
    }

    private void apply(SupplierDTO dto, Supplier s) {
        s.setName(dto.getName());
        s.setContactPerson(dto.getContactPerson());
        s.setPhone(dto.getPhone());
        s.setEmail(dto.getEmail());
        s.setAddress(dto.getAddress());
        s.setCity(dto.getCity());
        s.setRegion(dto.getRegion());
        s.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : Boolean.TRUE);
    }
}
