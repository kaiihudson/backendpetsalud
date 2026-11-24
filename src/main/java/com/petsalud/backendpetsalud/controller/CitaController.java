package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.CitaDto;
import com.petsalud.backendpetsalud.entity.Cita;
import com.petsalud.backendpetsalud.service.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaService citaService;

    private CitaDto translateToDTO(Cita cita) {
        return new CitaDto(
                cita.getId(),
                cita.getFechaHora().toString(),
                cita.getEstado().name(),
                cita.getMotivo(),
                cita.getMascota().getNombre(),
                cita.getVeterinario().getNombre()
        );
    }

    @PostMapping
    public ResponseEntity<CitaDto> agendarCita(
            @RequestParam Long mascotaId,
            @RequestParam Long veterinarioId,
            @RequestParam String fechaHora, // Formato ISO 8601
            @RequestParam String motivo) {
        LocalDateTime fechaHoraParsed = LocalDateTime.parse(fechaHora);
        try {
            Cita nuevaCita = citaService.agendarCita(mascotaId, veterinarioId, fechaHoraParsed, motivo);
            return ResponseEntity.ok(translateToDTO(nuevaCita));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CitaDto> obtenerCitaPorId(@PathVariable Long id) {
        return citaService.obtenerCitaPorId(id)
                .map(this::translateToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO')")
    @PutMapping("/{id}/estado")
    public ResponseEntity<CitaDto> actualizarEstadoCita(@PathVariable Long id, @RequestParam String estado) {
        try {
            Cita citaActualizada = citaService.actualizarEstadoCita(id, estado);
            return ResponseEntity.ok(translateToDTO(citaActualizada));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarCita(@PathVariable Long id) {
        citaService.cancelarCita(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<Cita>> listarCitasPorMascota(@PathVariable Long mascotaId) {
        List<Cita> citas = citaService.listarCitasPorMascota(mascotaId);
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<Cita>> listarCitasPorVeterinario(@PathVariable Long veterinarioId) {
        List<Cita> citas = citaService.listarCitasPorVeterinario(veterinarioId);
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/entre-fechas")
    public ResponseEntity<List<Cita>> listarCitasEntreFechas(
            @RequestParam String inicio,
            @RequestParam String fin) {
        LocalDateTime inicioParsed = LocalDateTime.parse(inicio);
        LocalDateTime finParsed = LocalDateTime.parse(fin);
        List<Cita> citas = citaService.listarCitasEntreFechas(inicioParsed, finParsed);
        return ResponseEntity.ok(citas);
    }
}
