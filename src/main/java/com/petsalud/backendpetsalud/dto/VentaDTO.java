package com.petsalud.backendpetsalud.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VentaDTO(
        Long id,
        LocalDateTime fecha,
        BigDecimal total,
        UsuarioDTO usuario,
        List<ProductoDTO> productos
) {
}
