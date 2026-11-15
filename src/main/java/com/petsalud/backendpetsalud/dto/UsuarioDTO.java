package com.petsalud.backendpetsalud.dto;

import java.util.List;

public record UsuarioDTO(
        Long id,
        String email,
        String nombre,
        String apellido,
        String telefono,
        String direccion,
        List<MascotaDTO> mascotas
) {
}
