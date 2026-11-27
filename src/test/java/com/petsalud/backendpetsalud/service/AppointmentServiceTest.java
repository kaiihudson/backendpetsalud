package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.*;
import com.petsalud.backendpetsalud.repository.CitaRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VeterinarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    private CitaServiceImpl citaService;

    private static final ZoneId ZONE = ZoneId.of("America/Santiago");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 6, 13, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
    private static final LocalTime SHIFT_START = LocalTime.of(10,0);
    private static final LocalTime SHIFT_END = LocalTime.of(22,0);

    @BeforeEach
    void setUp(){
        citaService = new CitaServiceImpl(
                citaRepository,
                mascotaRepository,
                FIXED_CLOCK,
                usuarioService,
                veterinarioRepository
        );
    }

    @Test
    public void agendarCita_withNoOverlaps_success() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha = NOW;
        String username = "someEmail";
        String motivo = "someMotive";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(List.of(mascota))
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        new Cita();
        Cita cita = Cita.builder()
                .id(1L)
                .fechaHora(fecha)
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        when(citaRepository.save(any())).thenReturn(cita);

        Cita citaFromService = citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo, username);

        assertEquals(cita, citaFromService);
    }
    @Test
    public void agendarCita_timeIsBeforeNow(){
        Long mascotaId = 1L;
        Long veterinarioId = 1L;
        LocalDateTime fechaHora = NOW.minusHours(1);
        String motivo = "someMotive";
        String username = "someEmail";

        try {
            citaService.agendarCita(mascotaId, veterinarioId, fechaHora, motivo, username);
        } catch (IllegalArgumentException e) {
            assertEquals("La cita no puede ocurrir en un tiempo ya pasado", e.getMessage());
        }


    }
    @Test
    public void agendarCita_mascotaDoesNotExist(){
        Long mascotaId = 1L;
        Long veterinarioId = 1L;
        String motivo = "someMotive";
        String username = "someEmail";
        when(mascotaRepository.findById(any())).thenReturn(Optional.empty());
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, NOW, motivo, username);
        } catch (EntityNotFoundException e) {
            assertEquals("Mascota no encontrada", e.getMessage());
        }


    }
    @Test
    public void agendarCita_veterinarioDoesNotExist(){
        Long mascotaId = 1L;
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("someNombre")
                .build();
        Long veterinarioId = 1L;
        String motivo = "someMotive";
        String username = "someEmail";
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, NOW, motivo, username);
        } catch (EntityNotFoundException e) {
            assertEquals("Veterinario no encontrado", e.getMessage());
        }
    }
    @Test
    public void agendarCita_veterinarioDoesExist_IsInvalid(){
        Long mascotaId = 1L;
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(mascotaId)
                .nombre("someNombre")
                .build();
        Long veterinarioId = 1L;
        new Usuario();
        String motivo = "someMotive";
        String username = "someEmail";
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, NOW, motivo, username);
        } catch (EntityNotFoundException e) {
            assertEquals("Veterinario no encontrado", e.getMessage());
        }
    }
    @Test
    public void agendarCita_withPartialOverlap_endsWhile_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        String motivo = "someMotive";
        String username = "someEmail";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(List.of(mascota))
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        new Cita();
        Cita citaPrevia = Cita.builder()
                .id(1L)
                .fechaHora(NOW.plusMinutes(30))
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, NOW, motivo, username);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withFullOverlap_startsWith_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= NOW;
        String motivo = "someMotive";
        String username = "someEmail";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(List.of(mascota))
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        new Cita();
        Cita citaPrevia = Cita.builder()
                .id(1L)
                .fechaHora(fecha)
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo, username);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withPartialOverlap_startsWhile_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= NOW.plusMinutes(1);
        String motivo = "someMotive";
        String username = "someEmail";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(List.of(mascota))
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        new Cita();
        Cita citaPrevia = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo, username);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withPartialOverlap_startsOnEnd_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= NOW.plusMinutes(45);
        String motivo = "someMotive";
        String username = "someEmail";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(new ArrayList<>())
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        new Cita();
        Cita citaPrevia = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo, username);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withPartialOverlap_endsOnNext_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= NOW.plusMinutes(50);
        String motivo = "someMotive";
        String username = "someEmail";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(List.of(mascota))
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        new Cita();
        Cita citaPrevia = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        new Cita();
        Cita citaPrevia2 = Cita.builder()
                .id(2L)
                .fechaHora(NOW.plusHours(1))
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia, citaPrevia2));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo, username);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withPartialOverlap_overlapIsCANCELADA_success(){
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= NOW.plusMinutes(50);
        String motivo = "someMotive";
        String username = "someEmail";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(List.of(mascota))
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        new Cita();
        Cita citaPrevia = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        new Cita();
        Cita citaPrevia2 = Cita.builder()
                .id(2L)
                .fechaHora(NOW.plusHours(1))
                .estado(AppointmentStatus.CANCELADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia, citaPrevia2));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            Cita newCita = citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo, username);
            //TODO: do this
            System.out.println(newCita);
            assert true;
        } catch (EntityNotFoundException | IllegalArgumentException e){
            fail();
        }
    }
    @Test
    public void agendarCita_usuarioIsNotOwner(){
        when(usuarioService.checkForPet(any(), any())).thenReturn(false);
        try {
            citaService.agendarCita(1L, 2L, NOW, null, null);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Mascota no pertenece a usuario", e.getMessage());
        }
    }
    @Test
    public void agendarCita_DateIsBeforeStart_failure(){
        LocalTime beforeStart = SHIFT_START.minusMinutes(1);
        LocalDateTime appointment = NOW.plusDays(1).withHour(beforeStart.getHour()).withMinute(beforeStart.getMinute());
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(new ArrayList<>())
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        Long mascotaId= 1L;
        Long veterinarioId = 2L;
        String motivo = "someMotivo";
        String username = "someUsername";
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, appointment, motivo, username);
            fail("Se ejecuto con exito");
        } catch (IllegalArgumentException ex) {
            assertEquals("Hora no valida", ex.getMessage());
        }
    }
    @Test
    public void agendarCita_DateIsAfterEnd_failure(){
        LocalTime beforeStart = SHIFT_END.plusMinutes(1);
        LocalDateTime appointment = NOW.plusDays(1).withHour(beforeStart.getHour()).withMinute(beforeStart.getMinute());
        new Mascota();
        Mascota mascota = Mascota.builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        new Veterinario();
        Veterinario veterinario = Veterinario.builder()
                .id(2L)
                .email("someOtherEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .rol(Rol.VETERINARIO)
                .mascotas(new ArrayList<>())
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .inicioTurno(SHIFT_START)
                .finTurno(SHIFT_END)
                .build();
        Long mascotaId= 1L;
        Long veterinarioId = 2L;
        String motivo = "someMotivo";
        String username = "someUsername";
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(usuarioService.checkForPet(any(), any())).thenReturn(true);
        try {
            citaService.agendarCita(mascotaId, veterinarioId, appointment, motivo, username);
            fail("Se ejecuto con exito");
        } catch (IllegalArgumentException ex) {
            assertEquals("Hora no valida", ex.getMessage());
        }
    }

    @Test
    public void getCita_withValidId(){
        new Usuario();
        new Cita();
        Cita cita = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        when(citaRepository.findById(any())).thenReturn(Optional.of(cita));

        Optional<Cita> citaFromService = citaService.obtenerCitaPorId(1L);

        if (citaFromService.isPresent()) {
            assertEquals(cita, citaFromService.get());
        } else {
            fail();
        }
    }
    @Test
    public void getCita_withInvalidId(){
        long idCita = 1L;

        when(citaRepository.findById(any())).thenReturn(Optional.empty());

        Optional<Cita> citaFromService = citaService.obtenerCitaPorId(idCita);

        if (citaFromService.isPresent()) {
            fail();
        } else {
            assertEquals(Optional.empty(), citaFromService);
        }
    }

    @Test
    public void putCita_withValidId(){
        new Usuario();
        new Cita();
        Cita cita = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        long idCita = 1L;
        String nuevoEstado = "completa";
        new Usuario();
        new Cita();
        Cita newCita = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.COMPLETA)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        when(citaRepository.findById(any())).thenReturn(Optional.of(cita));
        when(citaRepository.save(any())).thenReturn(newCita);

        try {
            Cita citaFromService = citaService.actualizarEstadoCita(idCita, nuevoEstado);
            assertEquals(AppointmentStatus.valueOf(nuevoEstado.toUpperCase()), citaFromService.getEstado());
            verify(citaRepository, times(1)).findById(1L);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void putCita_withInvalidId(){
        long idCita = 1L;
        String nuevoEstado = "completa";
        when(citaRepository.findById(any())).thenReturn(Optional.empty());

        try {
            citaService.actualizarEstadoCita(idCita, nuevoEstado);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Cita no encontrada", e.getMessage() );
        }

    }
    @Test
    public void putCita_withInvalidStatus(){
        new Usuario();
        new Cita();
        Cita cita = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        long idCita = 1L;
        when(citaRepository.findById(any())).thenReturn(Optional.of(cita));
        String nuevoEstado = "newEstado";

        try {
            citaService.actualizarEstadoCita(idCita, nuevoEstado);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Estado no valido", e.getMessage() );
        }

    }

    @Test
    public void cancelarCita_successful(){
        long idUsuario = 1L;
        new Usuario();
        new Cita();
        Cita cita = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotivo")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        when(citaRepository.findById(any())).thenReturn(Optional.of(cita));
        try {
            citaService.cancelarCita(idUsuario);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }
    @Test
    public void cancelarCita_failed(){
        long idUsuario = 1L;
        try {
            citaService.cancelarCita(idUsuario);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Cita no encontrada", e.getMessage() );
        }
    }

    @Test
    public void getCitas_withValidMascotaId(){
        new Usuario();
        new Mascota();
        new Cita();
        Cita cita1 = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(Mascota.builder().id(1L).build())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        new Usuario();
        new Mascota();
        new Cita();
        Cita cita2 = Cita.builder()
                .id(2L)
                .fechaHora(NOW.plusDays(31))
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(Mascota.builder().id(1L).build())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        long idMascota = 1L;
        when(citaRepository.findByMascotaId(idMascota)).thenReturn(List.of(cita1, cita2));

        List<Cita> citas = citaService.listarCitasPorMascota(idMascota);

        assertEquals(2, citas.size());
        assertEquals(idMascota, citas.get(0).getMascota().getId());
    }
    @Test
    public void getCitas_withInvalidMascotaId(){
        long idMascota = 1L;
        when(citaRepository.findByMascotaId(idMascota)).thenReturn(List.of());

        List<Cita> citas = citaService.listarCitasPorMascota(idMascota);

        assertEquals(0, citas.size());
    }

    @Test
    public void getCitas_withValidVeterinarioId(){
        new Usuario();
        new Cita();
        Cita cita1 = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().id(1L).rol(Rol.VETERINARIO).build())
                .build();
        new Usuario();
        new Cita();
        Cita cita2 = Cita.builder()
                .id(2L)
                .fechaHora(NOW.plusDays(31))
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().id(1L).rol(Rol.VETERINARIO).build())
                .build();
        long idVeterinario = 1L;
        when(citaRepository.findByVeterinarioId(idVeterinario)).thenReturn(List.of(cita1, cita2));

        List<Cita> citas = citaService.listarCitasPorVeterinario(idVeterinario);

        assertEquals(2, citas.size());
        assertEquals(idVeterinario, citas.get(0).getVeterinario().getId());
    }

    @Test
    public void getCitas_withInvalidVeterinarioId(){
        long idVeterinario = 1L;
        when(citaRepository.findByVeterinarioId(idVeterinario)).thenReturn(List.of());

        List<Cita> citas = citaService.listarCitasPorVeterinario(idVeterinario);

        assertEquals(0, citas.size());
    }

    @Test
    public void getCitas_withDate(){
        LocalDateTime inicio = NOW.minusDays(1);
        LocalDateTime fin = NOW.plusDays(1);
        new Usuario();
        new Cita();
        Cita cita = Cita.builder()
                .id(1L)
                .fechaHora(NOW)
                .estado(AppointmentStatus.AGENDADA)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(Usuario.builder().rol(Rol.VETERINARIO).build())
                .build();
        when(citaRepository.findByFechaHoraBetween(inicio, fin)).thenReturn(List.of(cita));

        List<Cita> citas = citaService.listarCitasEntreFechas(inicio, fin);

        assertEquals(1, citas.size());
    }
}
