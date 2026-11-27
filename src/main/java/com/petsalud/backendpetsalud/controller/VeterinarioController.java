package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.service.VeterinarioService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/veterinario")
public class VeterinarioController {
    @Autowired
    private VeterinarioService veterinarioService;

    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getVeterinario(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(veterinarioService.getVeterinarioById(id));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'VETERINARIO')")
    @PutMapping("/turnos/{id}")
    public ResponseEntity<?> setTurnoOnVeterinario(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody String inicioTurno, @RequestBody String fimTurno){
        String username = userDetails.getUsername();
        //TODO: get rol of veterinario
        try {
            LocalTime inicio =  LocalTime.parse(inicioTurno);
            LocalTime fim = LocalTime.parse(fimTurno);
            return ResponseEntity.ok(veterinarioService.setTurno(id, inicio, fim, username));
        } catch (DateTimeParseException | IllegalArgumentException | EntityNotFoundException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }
}
