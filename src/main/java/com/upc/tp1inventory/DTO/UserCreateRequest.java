package com.upc.tp1inventory.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 50, message = "El nombre de usuario no debe superar 50 caracteres")
    private String username;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    @Size(max = 100, message = "El correo electrónico no debe superar 100 caracteres")
    private String email;

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre completo no debe superar 100 caracteres")
    private String fullName;

    @NotBlank(message = "El rol es obligatorio")
    private String role;

    @Size(max = 15, message = "El teléfono no debe superar 15 caracteres")
    private String phone;

    @NotBlank(message = "La contraseña temporal es obligatoria")
    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    private String password;
}
