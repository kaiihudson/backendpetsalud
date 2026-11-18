package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.entity.Cita;
import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.CitaRepository;
import com.petsalud.backendpetsalud.repository.MascotaRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
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

    @InjectMocks
    private CitaServiceImpl citaService;

    @Test
    public void agendarCita_withNoOverlaps_success() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= LocalDateTime.now();
        String motivo = "someMotive";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        Mascota mascota = new Mascota().builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        Usuario veterinario = new Usuario().builder()
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
                .build();
        Cita cita = new Cita().builder()
                .id(1L)
                .fechaHora(fecha)
                .estado("Agendada")
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.save(any())).thenReturn(cita);

        Cita citaFromService = citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo);

        assertEquals(cita, citaFromService);
    }
    @Test
    public void agendarCita_mascotaDoesNotExist(){
        Long mascotaId = 1L;
        Long veterinarioId = 1L;
        LocalDateTime fechaHora = LocalDateTime.now();
        String motivo = "someMotive";
        when(mascotaRepository.findById(any())).thenReturn(Optional.empty());

        try {
            citaService.agendarCita(mascotaId, veterinarioId, fechaHora, motivo);
        } catch (IllegalArgumentException e) {
            assertEquals("Mascota no encontrada", e.getMessage());
        }


    }
    @Test
    public void agendarCita_veterinarioDoesNotExist(){
        Long mascotaId = 1L;
        Mascota mascota = new Mascota().builder()
                .id(1L)
                .nombre("someNombre")
                .build();
        Long veterinarioId = 1L;
        LocalDateTime fechaHora = LocalDateTime.now();
        String motivo = "someMotive";
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fechaHora, motivo);
        } catch (IllegalArgumentException e) {
            assertEquals("Veterinario no encontrado", e.getMessage());
        }
    }
    @Test
    public void agendarCita_veterinarioDoesExist_IsInvalid(){
        Long mascotaId = 1L;
        Mascota mascota = new Mascota().builder()
                .id(mascotaId)
                .nombre("someNombre")
                .build();
        Long veterinarioId = 1L;
        Usuario usuario = new Usuario().builder()
                .id(1L)
                .nombre("someNombre")
                .rol(Rol.CLIENTE)
                .build();
        LocalDateTime fechaHora = LocalDateTime.now();
        String motivo = "someMotive";
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(usuario));
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fechaHora, motivo);
        } catch (IllegalArgumentException e) {
            assertEquals("Usuario no es veterinario", e.getMessage());
        }
    }
    @Test
    public void agendarCita_withPartialOverlap_endsWhile_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= LocalDateTime.now();
        String motivo = "someMotive";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        Mascota mascota = new Mascota().builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        Usuario veterinario = new Usuario().builder()
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
                .build();
        Cita citaPrevia = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now().plusMinutes(30))
                .estado("Agendada")
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withFullOverlap_startsWith_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= LocalDateTime.now();
        String motivo = "someMotive";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        Mascota mascota = new Mascota().builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        Usuario veterinario = new Usuario().builder()
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
                .build();
        Cita citaPrevia = new Cita().builder()
                .id(1L)
                .fechaHora(fecha)
                .estado("Agendada")
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withPartialOverlap_startsWhile_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= LocalDateTime.now().plusMinutes(1);
        String motivo = "someMotive";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        Mascota mascota = new Mascota().builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        Usuario veterinario = new Usuario().builder()
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
                .build();
        Cita citaPrevia = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("Agendada")
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withPartialOverlap_startsOnEnd_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= LocalDateTime.now().plusMinutes(45);
        String motivo = "someMotive";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        Mascota mascota = new Mascota().builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        Usuario veterinario = new Usuario().builder()
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
                .build();
        Cita citaPrevia = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("Agendada")
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia));
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }
    @Test
    public void agendarCita_withPartialOverlap_endsOnNext_failure() {
        Long mascotaId  = 1L;
        Long veterinarioId = 2L;
        LocalDateTime fecha= LocalDateTime.now().plusMinutes(50);
        String motivo = "someMotive";
        // TODO: verify mascota controller and usuario handling to prevent infinite recursion
        Mascota mascota = new Mascota().builder()
                .id(1L)
                .nombre("somePetName")
                .especie("somePetSpecies")
                .raza("somePetRace")
                .fechaNacimiento(LocalDate.of(2020, 6, 6))
                .sexo("M")
                .citas(new ArrayList<>())
                .expedientesMedicos(new ArrayList<>())
                .build();
        Usuario veterinario = new Usuario().builder()
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
                .build();
        Cita citaPrevia = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("Agendada")
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        Cita citaPrevia2 = new Cita().builder()
                .id(2L)
                .fechaHora(LocalDateTime.now().plusHours(1))
                .estado("Agendada")
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(veterinario)
                .build();
        when(mascotaRepository.findById(any())).thenReturn(Optional.of(mascota));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(veterinario));
        when(citaRepository.getAllByVeterinarioIdOrderByFechaHoraAsc(2L)).thenReturn(List.of(citaPrevia, citaPrevia2));
        try {
            citaService.agendarCita(mascotaId, veterinarioId, fecha, motivo);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Hora no valida", e.getMessage() );
        }

    }

    @Test
    public void getCita_withValidId(){
        Cita cita = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("someEstado")
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(new Usuario().builder().rol(Rol.VETERINARIO).build())
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

        Optional<Cita> citaFromService = citaService.obtenerCitaPorId(1L);

        if (citaFromService.isPresent()) {
            fail();
        } else {
            assertEquals(Optional.empty(), citaFromService);
        }
    }

    @Test
    public void putCita_withValidId(){
        Cita cita = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("someEstado")
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(new Usuario().builder().rol(Rol.VETERINARIO).build())
                .build();
        long idCita = 1L;
        String nuevoEstado = "newEstado";
        Cita newCita = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado(nuevoEstado)
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(new Usuario().builder().rol(Rol.VETERINARIO).build())
                .build();
        when(citaRepository.findById(any())).thenReturn(Optional.of(cita));
        when(citaRepository.save(any())).thenReturn(newCita);

        try {
            Cita citaFromService = citaService.actualizarEstadoCita(idCita, nuevoEstado);
            assertEquals(nuevoEstado, citaFromService.getEstado());
            verify(citaRepository, times(1)).findById(1L);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    public void putCita_withInvalidId(){
        long idCita = 1L;
        String nuevoEstado = "newEstado";
        when(citaRepository.findById(any())).thenReturn(Optional.empty());

        try {
            citaService.actualizarEstadoCita(idCita, nuevoEstado);
            fail();
        } catch (IllegalArgumentException e){
            assertEquals("Cita no encontrada", e.getMessage() );
        }

    }

    @Test
    public void cancelarCita(){
        long idUsuario = 1L;
        citaService.cancelarCita(idUsuario);
        verify(citaRepository, times(1)).deleteById(idUsuario);
    }

    @Test
    public void getCitas_withValidMascotaId(){
        Cita cita1 = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("someEstado")
                .motivo("someMotive")
                .mascota(new Mascota().builder().id(1L).build())
                .veterinario(new Usuario().builder().rol(Rol.VETERINARIO).build())
                .build();
        Cita cita2 = new Cita().builder()
                .id(2L)
                .fechaHora(LocalDateTime.now().plusDays(31))
                .estado("someEstado")
                .motivo("someMotive")
                .mascota(new Mascota().builder().id(1L).build())
                .veterinario(new Usuario().builder().rol(Rol.VETERINARIO).build())
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
        Cita cita1 = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("someEstado")
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(new Usuario().builder().id(1L).rol(Rol.VETERINARIO).build())
                .build();
        Cita cita2 = new Cita().builder()
                .id(2L)
                .fechaHora(LocalDateTime.now().plusDays(31))
                .estado("someEstado")
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(new Usuario().builder().id(1L).rol(Rol.VETERINARIO).build())
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
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now().plusDays(1);
        Cita cita = new Cita().builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado("someEstado")
                .motivo("someMotive")
                .mascota(new Mascota())
                .veterinario(new Usuario().builder().rol(Rol.VETERINARIO).build())
                .build();
        when(citaRepository.findByFechaHoraBetween(inicio, fin)).thenReturn(List.of(cita));

        List<Cita> citas = citaService.listarCitasEntreFechas(inicio, fin);

        assertEquals(1, citas.size());
    }
}
