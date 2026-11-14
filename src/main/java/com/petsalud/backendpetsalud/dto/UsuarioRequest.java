package com.petsalud.backendpetsalud.dto;

import org.springframework.lang.NonNull;

public record UsuarioRequest(
        @NonNull String email,
        @NonNull String password,
        @NonNull String rol,
        @NonNull String nombre,
        @NonNull String apellido,
        String telefono,
        @NonNull String direccion) {
}
