package com.petsalud.backendpetsalud.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.dto.ProductoDTO;
import com.petsalud.backendpetsalud.dto.UsuarioDTO;
import com.petsalud.backendpetsalud.dto.VentaDTO;

import com.petsalud.backendpetsalud.security.JwtUtils;
import com.petsalud.backendpetsalud.security.SecurityConfig;
import com.petsalud.backendpetsalud.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentaController.class)
@Import(SecurityConfig.class)
public class SaleControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaService ventaService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsService userDetailsService;


    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void makeSale_returnOKSale() throws Exception{

        UsuarioDTO usuario = new UsuarioDTO(
                1L,
                "someEmail",
                "someName",
                "someLastName",
                "somePhone",
                "someAdress",
                new ArrayList<>()
        );
        ProductoDTO producto1 = new ProductoDTO(
                1L,
                "someName",
                "someDesc",
                BigDecimal.valueOf(100),
                1,
                "SomeCat"
        );
        ProductoDTO producto2 = new ProductoDTO(
                1L,
                "someName",
                "someDesc",
                BigDecimal.valueOf(100),
                1,
                "SomeCat"
        );
        Long usuarioId= 1L;
        List<Long> productosId = List.of(1L, 2L);

        VentaDTO ventaEsperada = new VentaDTO(
                1L,
                LocalDateTime.now(),
                BigDecimal.valueOf(200L),
                usuario,
                List.of(producto1, producto2)
        );
        when(ventaService.realizarVenta(usuarioId, productosId))
                .thenReturn(ventaEsperada);


        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productosId))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(200));

        verify(ventaService, times(1))
                .realizarVenta(usuarioId, productosId);
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    public void makeSale_returnNoSale() throws Exception{

        Long usuarioId= 1L;
        List<Long> productosId = List.of(1L, 2L);

        mockMvc.perform(post("/api/ventas")
                        .with(csrf())
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productosId))
                )
                .andExpect(status().isForbidden());

        verify(ventaService, never()).realizarVenta(usuarioId, productosId);
    }
}
