package com.petsalud.backendpetsalud.dto;

public record UsuarioRequest(
        String email,
        String password,
        String rol,
        String nombre,
        String apellido,
        String telefono,
        String direccion) {
}
