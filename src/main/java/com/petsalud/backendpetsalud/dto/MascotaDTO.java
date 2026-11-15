package com.petsalud.backendpetsalud.dto;

public record MascotaDTO(
        Long id,
        String nombre,
        String especie,
        String raza,
        String sexo
) {
}
