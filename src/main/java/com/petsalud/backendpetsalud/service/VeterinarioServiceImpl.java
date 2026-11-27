package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.VeterinarioDto;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Veterinario;
import com.petsalud.backendpetsalud.repository.VeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Objects;

@Service
public class VeterinarioServiceImpl implements VeterinarioService{

    @Autowired
    private VeterinarioRepository veterinarioRepository;

    private VeterinarioDto translateDto(Veterinario veterinario) {
        return new VeterinarioDto(
                veterinario.getId(),
                veterinario.getNombre(),
                veterinario.getApellido(),
                veterinario.getRol().toString(),
                veterinario.getInicioTurno().toString(),
                veterinario.getFinTurno().toString()
        );
    }

    @Override
    public VeterinarioDto getVeterinarioById(Long id) {
        return translateDto(veterinarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Veterinario no encontrado")));
    }

    @Override
    public boolean validateVeterinario(String username, Long id){
        Veterinario vet = veterinarioRepository.findByEmail(username).orElseThrow(() -> new EntityNotFoundException("Veterinario no encontrado"));
        return (Objects.equals(vet.getId(), id));
    }

    @Override
    public VeterinarioDto setTurno(Long id, LocalTime inicio, LocalTime fim, String username) {
        if(!validateVeterinario(username, id)){
            throw new IllegalArgumentException("Veterinario no valido");
        }
        if (inicio.equals(fim) || inicio.isAfter(fim)) {
            throw new IllegalArgumentException("Turno no valido");
        }
        Veterinario vet = veterinarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Veterinario no encontrado"));
        if(vet.getRol() != Rol.VETERINARIO) {
            throw new IllegalArgumentException("Usuario no es veterinario");
        }
        vet.setInicioTurno(inicio);
        vet.setFinTurno(fim);
        return translateDto(veterinarioRepository.save(vet));
    }
}
