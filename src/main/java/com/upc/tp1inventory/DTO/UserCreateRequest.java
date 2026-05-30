package com.upc.tp1inventory.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {
    private String username;
    private String email;
    private String fullName;
    private String role;      // "ADMIN", "MANAGER" o "EMPLOYEE"
    private String phone;
    private String password;
}
