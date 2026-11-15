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
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    // create a sale as an admin, returns new sale
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
    // create a sale as a client, returns 403
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
    // get a sale as an admin, returns sale
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void getSale_returnOKSale() throws Exception{

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

        VentaDTO ventaEsperada = new VentaDTO(
                1L,
                LocalDateTime.now(),
                BigDecimal.valueOf(200L),
                usuario,
                List.of(producto1, producto2)
        );
        Long ventaId = 1L;
        when(ventaService.obtenerVentaPorId(ventaEsperada.id()))
                .thenReturn(Optional.of(ventaEsperada));

        mockMvc.perform(get("/api/ventas/{id}", ventaId)
                        .with(csrf())
                        .param("usuarioId", ventaId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value(200));

        verify(ventaService, times(1))
                .obtenerVentaPorId(ventaId);
    }
    // get a sale as a client, returns 403
    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    public void getSale_returnNoSale() throws Exception{

        Long ventaId = 1L;

        mockMvc.perform(get("/api/ventas")
                        .with(csrf())
                        .param("usuarioId", ventaId.toString())
                )
                .andExpect(status().isForbidden());

        verify(ventaService, never()).obtenerVentaPorId(ventaId);
    }
    // create a sale as an admin, returns new sale
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void getSale_forClient_returnUserSales() throws Exception{

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

        VentaDTO ventaEsperada = new VentaDTO(
                1L,
                LocalDateTime.now(),
                BigDecimal.valueOf(200L),
                usuario,
                List.of(producto1, producto2)
        );
        when(ventaService.listarVentasPorUsuario(usuarioId)).thenReturn(List.of(ventaEsperada));

        mockMvc.perform(get("/api/ventas/usuario/{usuarioId}", usuarioId)
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].total").value(200));

        verify(ventaService, times(1)).listarVentasPorUsuario(usuarioId);
    }
    // create a sale as a client, returns 403
    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    public void getSale_forClient_returnNoSales() throws Exception{

        Long usuarioId= 1L;

        mockMvc.perform(get("/api/ventas/usuario/{usuarioId}", usuarioId)
                        .with(csrf())
                )
                .andExpect(status().isForbidden());

        verify(ventaService, never()).listarVentasPorUsuario(usuarioId);
    }
    // create a sale as an admin, returns new sale
    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    public void getSales_returnOKSale() throws Exception{

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

        VentaDTO ventaEsperada = new VentaDTO(
                1L,
                LocalDateTime.now(),
                BigDecimal.valueOf(200L),
                usuario,
                List.of(producto1, producto2)
        );
        when(ventaService.listarTodasLasVentas()).thenReturn(List.of(ventaEsperada));


        mockMvc.perform(get("/api/ventas")
                        .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].total").value(200));

        verify(ventaService, times(1)).listarTodasLasVentas();
    }
    // create a sale as a client, returns 403
    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    public void getSales_returnNoSale() throws Exception{

        Long usuarioId= 1L;

        mockMvc.perform(get("/api/ventas")
                        .with(csrf())
                )
                .andExpect(status().isForbidden());

        verify(ventaService, never()).listarVentasPorUsuario(usuarioId);
    }

}
