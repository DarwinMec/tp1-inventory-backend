package com.upc.tp1inventory.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.*;
import java.util.List;
import java.util.UUID;

public class GeneratePassword {

    private static final String DB_URL = getEnvOrDefault(
            "DB_URL",
            "jdbc:postgresql://localhost:5432/db_TP1test2"
    );

    private static final String DB_USER = getEnvOrDefault(
            "DB_USER",
            "postgres"
    );

    private static final String DB_PASSWORD = getEnvOrDefault(
            "DB_PASSWORD",
            "1234"
    );

    private static final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(12);

    public static void main(String[] args) {
        List<SeedUser> users = List.of(
                new SeedUser(
                        "adminsuper",
                        "adminsuper@gestrest.ai",
                        "Administrador General",
                        "ADMIN",
                        "900000001",
                        "admin1234"
                ),
                new SeedUser(
                        "manageruser",
                        "manager@gestrest.ai",
                        "Gerente del Restaurante",
                        "MANAGER",
                        "900000002",
                        "manager1234"
                ),
                new SeedUser(
                        "employeeuser",
                        "employee@gestrest.ai",
                        "Empleado Operativo",
                        "EMPLOYEE",
                        "900000003",
                        "employee1234"
                )
        );

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Conexión exitosa a PostgreSQL");
            System.out.println("Base de datos: " + DB_URL);
            System.out.println();

            for (SeedUser user : users) {
                upsertUser(connection, user);
            }

            System.out.println();
            System.out.println("Usuarios de prueba listos:");
            System.out.println("ADMIN    -> username: adminsuper   | password: admin1234");
            System.out.println("MANAGER  -> username: manageruser  | password: manager1234");
            System.out.println("EMPLOYEE -> username: employeeuser | password: employee1234");

        } catch (SQLException e) {
            System.err.println("Error conectando o insertando usuarios:");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void upsertUser(Connection connection, SeedUser user) throws SQLException {
        String existingUserId = findExistingUserId(connection, user.username(), user.email());

        String passwordHash = encoder.encode(user.password());

        if (existingUserId == null) {
            insertUser(connection, user, passwordHash);
            System.out.println("Usuario creado: " + user.username() + " [" + user.role() + "]");
        } else {
            updateUser(connection, existingUserId, user, passwordHash);
            System.out.println("Usuario actualizado: " + user.username() + " [" + user.role() + "]");
        }
    }

    private static String findExistingUserId(Connection connection,
                                             String username,
                                             String email) throws SQLException {
        String sql = """
                SELECT id::text
                FROM users
                WHERE username = ? OR email = ?
                LIMIT 1
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("id");
                }
            }
        }

        return null;
    }

    private static void insertUser(Connection connection,
                                   SeedUser user,
                                   String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO users (
                    id,
                    username,
                    email,
                    password_hash,
                    full_name,
                    role,
                    phone,
                    is_active,
                    created_at,
                    updated_at
                )
                VALUES (
                    CAST(? AS uuid),
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    true,
                    NOW(),
                    NOW()
                )
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, user.username());
            statement.setString(3, user.email());
            statement.setString(4, passwordHash);
            statement.setString(5, user.fullName());
            statement.setString(6, user.role());
            statement.setString(7, user.phone());

            statement.executeUpdate();
        }
    }

    private static void updateUser(Connection connection,
                                   String userId,
                                   SeedUser user,
                                   String passwordHash) throws SQLException {
        String sql = """
                UPDATE users
                SET
                    username = ?,
                    email = ?,
                    password_hash = ?,
                    full_name = ?,
                    role = ?,
                    phone = ?,
                    is_active = true,
                    updated_at = NOW()
                WHERE id = CAST(? AS uuid)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.username());
            statement.setString(2, user.email());
            statement.setString(3, passwordHash);
            statement.setString(4, user.fullName());
            statement.setString(5, user.role());
            statement.setString(6, user.phone());
            statement.setString(7, userId);

            statement.executeUpdate();
        }
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    private record SeedUser(
            String username,
            String email,
            String fullName,
            String role,
            String phone,
            String password
    ) {
    }
}