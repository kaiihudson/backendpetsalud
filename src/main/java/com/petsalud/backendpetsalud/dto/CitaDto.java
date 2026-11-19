package com.petsalud.backendpetsalud.dto;

public record CitaDto(
        Long citaId,
        String fechaHora,
        String estado,
        String motivo,
        String nombreMascota,
        String nombreVeterinario) {
}
