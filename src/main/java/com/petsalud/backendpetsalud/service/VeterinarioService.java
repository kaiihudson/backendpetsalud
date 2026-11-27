package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.VeterinarioDto;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

public interface VeterinarioService {
    VeterinarioDto getVeterinarioById(Long id);
    VeterinarioDto setTurno(Long id, LocalTime inicio, LocalTime fim, String username);
    boolean validateVeterinario(String username, Long id);
}
