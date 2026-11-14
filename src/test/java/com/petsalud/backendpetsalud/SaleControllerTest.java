package com.petsalud.backendpetsalud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petsalud.backendpetsalud.controller.VentaController;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.security.JwtAuthFilter;
import com.petsalud.backendpetsalud.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VentaController.class)
public class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VentaService ventaService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void makeSale_returnOKSale() throws Exception{
        Long usuarioId= 1L;
        List<Long> productosId = List.of(1L, 2L);

        Venta ventaEsperada = Venta.builder()
                .id(1L)
                .total(new BigDecimal("300.00"))
                .build();

        when(ventaService.realizarVenta(usuarioId, productosId))
                .thenReturn(ventaEsperada);

        System.out.println(
                objectMapper.writeValueAsString(ventaEsperada)
        );

        mockMvc.perform(post("/api/ventas")
                .param("usuarioId", usuarioId.toString())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productosId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.total").value("300.00"))
                .andDo(print());

        verify(ventaService, times(1))
                .realizarVenta(usuarioId, productosId);
    }
}
