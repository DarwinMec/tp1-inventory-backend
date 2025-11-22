package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.Inventory;
import com.upc.tp1inventory.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProduct(Product product);

    Optional<Inventory> findByProduct_Id(UUID productId);
}
