package com.upc.tp1inventory.Repository;

import com.upc.tp1inventory.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.username) = LOWER(:identifier)
               OR LOWER(u.email) = LOWER(:identifier)
            """)
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}
