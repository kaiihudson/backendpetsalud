package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.VentaDTO;

import java.util.List;
import java.util.Optional;

public interface VentaService {
    VentaDTO realizarVenta(Long usuarioId, List<Long> productosIds);
    Optional<VentaDTO> obtenerVentaPorId(Long id);
    List<VentaDTO> listarVentasPorUsuario(Long usuarioId);
    List<VentaDTO> listarTodasLasVentas();
}
