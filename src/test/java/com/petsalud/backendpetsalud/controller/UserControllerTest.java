package com.petsalud.backendpetsalud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.dto.UsuarioDTO;
import com.petsalud.backendpetsalud.dto.UsuarioRequest;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.security.JwtUtils;
import com.petsalud.backendpetsalud.security.SecurityConfig;
import com.petsalud.backendpetsalud.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    public void createUser_atNormalMethod_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "somePassword",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());
        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("CREATED"));
        verify(usuarioService, times(1)).registrarUsuario(any());
    }
    @Test
    public void createUser_withInvalidPassword_atNormalMethod_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().is4xxClientError());
        verify(usuarioService, never()).registrarUsuario(any());

    }
    @Test
    public void createUser_withInvalidEmail_atNormalMethod_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "",
                "somePassword",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().is4xxClientError());
        verify(usuarioService, never()).registrarUsuario(any());

    }
    @Test
    public void createUser_withInvalidAdminRole_atNormalMethod_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "somePassword",
                "ADMIN",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().is4xxClientError());
        verify(usuarioService, never()).registrarUsuario(any());

    }
    @Test
    public void createUser_withInvalidVeterinarioRole_atNormalMethod_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "somePassword",
                "VETERINARIO",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/api/usuarios/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().is4xxClientError());
        verify(usuarioService, never()).registrarUsuario(any());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createUser_atAdminMethod_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "somePassword",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());
        mockMvc.perform(post("/api/usuarios/registro/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("CREATED"));
        verify(usuarioService, times(1)).registrarUsuario(any());
    }
    @Test
    @WithMockUser(roles = "CLIENTE")
    public void createUser_atAdminMethod_withClienteRole_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "somePassword",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());
        mockMvc.perform(post("/api/usuarios/registro/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().isForbidden());
        verify(usuarioService, never()).registrarUsuario(any());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    public void createUser_withInvalidPassword_atAdminMethod_returns400() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/api/usuarios/registro/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().is4xxClientError());
        verify(usuarioService, never()).registrarUsuario(any());

    }
    @Test
    @WithMockUser(roles = "ADMIN")
    public void createUser_withInvalidEmail_atAdminMethod_returnsNewUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "",
                "somePassword",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        when(usuarioService.registrarUsuario(any())).thenReturn(new Usuario());

        mockMvc.perform(post("/api/usuarios/registro/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().is4xxClientError());
        verify(usuarioService, never()).registrarUsuario(any());

    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUser_returnsUser() throws Exception {
        UsuarioDTO usuarioDTO = new UsuarioDTO(
                1L,
                "someEmail",
                "someName",
                "someLastName",
                "123456789",
                "someAddress",
                new ArrayList<>()
        );
        when(usuarioService.obtenerUsuarioPorId(any())).thenReturn(Optional.of(usuarioDTO));

        mockMvc.perform(get("/api/usuarios/detalle/{id}", 1L)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(usuarioDTO)));

        verify(usuarioService, times(1)).obtenerUsuarioPorId(any());
    }
    @Test
    public void getUser_withNoSecurity_returns403() throws Exception {
        UsuarioDTO usuarioDTO = new UsuarioDTO(
                1L,
                "someEmail",
                "someName",
                "someLastName",
                "123456789",
                "someAddress",
                new ArrayList<>()
        );
        when(usuarioService.obtenerUsuarioPorId(any())).thenReturn(Optional.of(usuarioDTO));

        mockMvc.perform(get("/api/usuarios/detalle/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).obtenerUsuarioPorId(any());
    }
    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUser_returnsNoUser() throws Exception {

        when(usuarioService.obtenerUsuarioPorId(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/detalle/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).obtenerUsuarioPorId(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void putUser_returnsUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "somePassword",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        Usuario usuario = new Usuario().builder()
                .id(1L)
                .email(usuarioRequest.email())
                .password(usuarioRequest.password())
                .nombre(usuarioRequest.nombre())
                .apellido(usuarioRequest.apellido())
                .telefono(usuarioRequest.telefono())
                .direccion(usuarioRequest.direccion())
                .rol(Rol.CLIENTE)
                .mascotas(new ArrayList<>())
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .build();
        UsuarioDTO usuarioDTO = new UsuarioDTO(
                1L,
                "someEmail",
                "someName",
                "someLastName",
                "123456789",
                "someAddress",
                new ArrayList<>()
        );
        when(usuarioService.actualizarUsuario(1L, usuario)).thenReturn(usuarioDTO);

        mockMvc.perform(put("/api/usuarios/detalle/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuarioRequest)))
                .andExpect(status().isOk());

    }
    @Test
    public void putUser_withInvalidRole_returnsUser() throws Exception {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                "someEmail",
                "somePassword",
                "CLIENTE",
                "someName",
                "someLastName",
                "123456789",
                "someAddress"
        );
        Usuario usuario = new Usuario().builder()
                .id(1L)
                .email(usuarioRequest.email())
                .password(usuarioRequest.password())
                .nombre(usuarioRequest.nombre())
                .apellido(usuarioRequest.apellido())
                .telefono(usuarioRequest.telefono())
                .direccion(usuarioRequest.direccion())
                .rol(Rol.CLIENTE)
                .mascotas(new ArrayList<>())
                .ventas(new ArrayList<>())
                .recompensas(new ArrayList<>())
                .notificaciones(new ArrayList<>())
                .build();

        mockMvc.perform(put("/api/usuarios/detalle/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).actualizarUsuario(1L, usuario);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteUser_returnsNoUser() throws Exception {

        mockMvc.perform(delete("/api/usuarios/detalle/{id}", 1L)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminarUsuario(1L);
    }
    @Test
    public void deleteUser_noExecution() throws Exception {
        mockMvc.perform(delete("/api/usuarios/detalle/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).eliminarUsuario(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUsers_returnsUsers() throws Exception {
        UsuarioDTO usuarioDTO = new UsuarioDTO(
                1L,
                "someEmail",
                "someName",
                "someLastName",
                "123456789",
                "someAddress",
                new ArrayList<>()
        );
        when(usuarioService.listarUsuarios()).thenReturn(Arrays.asList(usuarioDTO));

        mockMvc.perform(get("/api/usuarios")
                .with(csrf())
        ).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void getUsers_withInvalidRole_returns403() throws Exception {
        when(usuarioService.listarUsuarios()).thenReturn(Arrays.asList());
        mockMvc.perform(get("/api/usuarios")
                .with(csrf())
        ).andExpect(status().isForbidden());
    }
}
