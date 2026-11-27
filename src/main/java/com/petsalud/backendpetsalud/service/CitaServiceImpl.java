package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.*;
import com.petsalud.backendpetsalud.repository.CitaRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    private final MascotaRepository mascotaRepository;
    private final Clock clock;
    private final UsuarioService usuarioService;
    private final VeterinarioRepository veterinarioRepository;

    public CitaServiceImpl(
            CitaRepository citaRepository,
            MascotaRepository mascotaRepository,
            Clock clock,
            UsuarioService usuarioService,
            VeterinarioRepository veterinarioRepository
    ) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.clock = clock;
        this.usuarioService = usuarioService;
        this.veterinarioRepository = veterinarioRepository;
    }

    public boolean checkAvailability(LocalDateTime target, LocalDateTime checker, long duration) {
        LocalDateTime targetEnding = target.plusMinutes(duration);
        LocalDateTime checkEnding = checker.plusMinutes(duration);

        return  !checker.isAfter(targetEnding) && !checkEnding.isBefore(target);
    }

    @Override
    public Cita agendarCita(Long mascotaId, Long veterinarioId, LocalDateTime fechaHora, String motivo, String username) {
        if (fechaHora.isBefore(LocalDateTime.now(clock).minusMinutes(1))) {
            throw new IllegalArgumentException("La cita no puede ocurrir en un tiempo ya pasado");
        }
        if (!usuarioService.checkForPet(username,  mascotaId)) {
            throw new IllegalArgumentException("Mascota no pertenece a usuario");
        }
        // TODO: verificar horario maximo de cita, se asumen 45 minutos, queda configurable
        Mascota mascota = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new EntityNotFoundException("Mascota no encontrada"));

        Veterinario veterinario = veterinarioRepository.findById(veterinarioId).orElseThrow(()-> new EntityNotFoundException("Veterinario no encontrado"));
        long appointmentDuration = 45;
        // tiene horas disponibles en el horario?
        if (fechaHora.toLocalTime().isAfter(veterinario.getInicioTurno())
                && fechaHora.toLocalTime().isBefore(
                        veterinario.getFinTurno().minusMinutes(appointmentDuration))){
            // verificar disponibilidad si hay horas
            List<Cita> citas = citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(veterinario.getId());
            for (Cita current: citas) {
                if (current.getEstado() == AppointmentStatus.CANCELADA){continue;}
                if (checkAvailability(current.getFechaHora(), fechaHora, appointmentDuration)) {
                    throw new IllegalArgumentException("Hora no valida");
                }
            }
        } else {
            throw new IllegalArgumentException("Hora no valida");
        }

        Cita cita = Cita.builder()
                .mascota(mascota)
                .veterinario(veterinario)
                .fechaHora(fechaHora)
                .motivo(motivo)
                .estado(AppointmentStatus.AGENDADA)
                .build();

        return citaRepository.save(cita);
    }

    @Override
    public Optional<Cita> obtenerCitaPorId(Long id) {
        return citaRepository.findById(id);
    }

    @Override
    public Cita actualizarEstadoCita(Long id, String nuevoEstado) {
        return citaRepository.findById(id).map(cita -> {
            try {
                cita.setEstado(AppointmentStatus.valueOf(nuevoEstado.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado no valido");
            }
            return citaRepository.save(cita);
        }).orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));
    }

    @Override
    public void cancelarCita(Long id) {
        Optional<Cita> citaReq = citaRepository.findById(id);
        if (citaReq.isPresent()) {
            citaReq.get().setEstado(AppointmentStatus.CANCELADA);
        } else {
            throw new IllegalArgumentException("Cita no encontrada");
        }
//        citaRepository.deleteById(id);
    }

    @Override
    public List<Cita> listarCitasPorMascota(Long mascotaId) {
        return citaRepository.findByMascotaId(mascotaId);
    }

    @Override
    public List<Cita> listarCitasPorVeterinario(Long veterinarioId) {
        return citaRepository.findByVeterinarioId(veterinarioId);
    }

    @Override
    public List<Cita> listarCitasEntreFechas(LocalDateTime inicio, LocalDateTime fin) {
        return citaRepository.findByFechaHoraBetween(inicio, fin);
    }
}

