package com.petsalud.backendpetsalud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.entity.*;
import com.petsalud.backendpetsalud.security.JwtUtils;
import com.petsalud.backendpetsalud.security.SecurityConfig;
import com.petsalud.backendpetsalud.service.CitaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CitaController.class)
@Import(SecurityConfig.class)
public class AppointmentControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private CitaService citaService;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createAppointment_valid_success() throws Exception {
        Long mascotaId = 1L;
        Long veterinarioId =1L;
        String fechaHora = LocalDateTime.now().toString();
        String motivo = "someMotivo";

        Mascota mascota = new Mascota();
        new Usuario();
        Usuario usuario = Usuario.builder().rol(Rol.VETERINARIO).build();
        new Cita();
        Cita cita =  Cita.builder()
                .id(1L)
                .fechaHora(LocalDateTime.now())
                .estado(AppointmentStatus.AGENDADA)
                .motivo(motivo)
                .mascota(mascota)
                .veterinario(usuario)
                .build();
        when(citaService.agendarCita(any(), any(), any(), any())).thenReturn(cita);

        mockMvc.perform(
                post("/api/citas")
                        .with(csrf())
                        .param("mascotaId", mascotaId.toString())
                        .param( "veterinarioId", veterinarioId.toString())
                        .param("fechaHora", fechaHora)
                        .param("motivo", motivo))
                .andExpect(status().isOk());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    public void createAppointment_invalidTime_hasOverlap_failure() throws Exception {
        Long mascotaId = 1L;
        Long veterinarioId =1L;
        String fechaHora = LocalDateTime.now().toString();
        String motivo = "someMotivo";

        when(citaService.agendarCita(any(), any(), any(), any())).thenThrow(new IllegalArgumentException());

        mockMvc.perform(
                        post("/api/citas")
                                .with(csrf())
                                .param("mascotaId", mascotaId.toString())
                                .param( "veterinarioId", veterinarioId.toString())
                                .param("fechaHora", fechaHora)
                                .param("motivo", motivo))
                .andExpect(status().is4xxClientError());
    }
    @Test
    public void getAppointment_valid() throws Exception {}
    @Test
    public void getAppointment_invalid() throws Exception {}
    @Test
    public void putAppointment_valid() throws Exception {}
    @Test
    public void putAppointment_invalid() throws Exception {}
    @Test
    public void cancelAppointment_valid() throws Exception {}
    @Test
    public void cancelAppointment_invalid() throws Exception {}
    @Test
    public void getAppointmentByPet_valid() throws Exception {}
    @Test
    public void getAppointmentByPet_invalid() throws Exception {}
    @Test
    public void getAppointmentByVet_valid() throws Exception {}
    @Test
    public void getAppointmentByVet_invalid() throws Exception {}
    @Test
    public void getAppointmentByRange_valid() throws Exception {}
    @Test
    public void getAppointmentByRange_wrongTyping() throws Exception {}
    @Test
    public void getAppointmentByRange_wrongRange_equals() throws Exception {}
    @Test
    public void getAppointmentByRange_wrongRange_negative() throws Exception {}
}
