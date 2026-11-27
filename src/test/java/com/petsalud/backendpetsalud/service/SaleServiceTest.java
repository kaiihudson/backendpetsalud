package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.VentaDTO;
import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.repository.ProductoRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static java.time.LocalDateTime.now;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SaleServiceTest {

    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VentaRepository ventaRepository;

    private VentaServiceImpl ventaService;

    private static final ZoneId ZONE = ZoneId.of("America/Santiago");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 6, 13, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);

    @BeforeEach
    void setUp(){
        ventaService = new VentaServiceImpl(
                ventaRepository,
                usuarioRepository,
                productoRepository,
                FIXED_CLOCK
        );
    }

    // validate that the user in the sale has
    // minimal info: email, password, name, last name, address
    @Test
    public void createSale_checkUser() {
        //arrange
        new Usuario();
        Usuario user = Usuario
                .builder()
                .id(1L)
                .email("someEmail")
                .password("somePassword")
                .nombre("someName")
                .apellido("someLastName")
                .direccion("someDireccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        new Producto();
        Producto producto = Producto
                .builder()
                .id(1L)
                .nombre("someName")
                .precio(BigDecimal.valueOf(10L))
                .descripcion("some description")
                .stock(1).
                categoria("someCategory").
                build();
        when(productoRepository.findAllById(any())).thenReturn(Collections.singletonList(producto));
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(user));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //act
        VentaDTO newVenta = ventaService.realizarVenta(1L, Collections.singletonList(producto.getId()));

        //assert
        assertNotNull(newVenta.usuario().id());
        assertNotNull(newVenta.usuario().email());
        assertNotNull(newVenta.usuario().nombre());
        assertNotNull(newVenta.usuario().apellido());
        assertNotNull(newVenta.usuario().direccion());
    }
    // create Sale with invalid user
    @Test
    public void createSale_checkInvalidUser(){
        //arrange
        new Usuario();
        new Producto();
        Producto producto = Producto
                .builder()
                .id(1L)
                .nombre("someName")
                .precio(BigDecimal.valueOf(10L))
                .descripcion("some description")
                .stock(1).
                categoria("someCategory").
                build();
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());
        //act
        try {
                ventaService.realizarVenta(1L, Collections.singletonList(producto.getId()));
            //assert
            fail("encontro un usuario");
        } catch (IllegalArgumentException e){
            assertEquals("Usuario no encontrado", e.getMessage());
        }


    }
    // create Sale with invalid product
    @Test
    public void createSale_withInvalidProducts(){
        //Arr
        new Usuario();
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("someUser")
                .build();
        new Producto();
        Producto producto1 = Producto.builder()
                .id(1L)
                .nombre("someProduct")
                .categoria("someCategoria")
                .precio(BigDecimal.valueOf(200L))
                .stock(1).build();
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(usuario));
        when(productoRepository.findAllById(any())).thenReturn(Collections.singletonList(producto1));
        //act
        try {
            ventaService.realizarVenta(1L, Arrays.asList(1L, 2L));
            //ass
            fail("encontro un producto");
        } catch (IllegalArgumentException e) {
            assertEquals("Algunos productos no fueron encontrados", e.getMessage());
        }

    }
    // create Sale with invalid product Stock
    @Test
    public void createSale_withInvalidStock(){
        // arrange
        new Usuario();
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("someUser")
                .build();
        new Producto();
        Producto producto1 = Producto.builder()
                .id(1L)
                .nombre("someProduct")
                .categoria("someCategoria")
                .precio(BigDecimal.valueOf(100L))
                .stock(0).build();
        when(productoRepository.findAllById(any())).thenReturn(Collections.singletonList(producto1));
        when(usuarioRepository.findById(any(Long.class))).thenReturn(Optional.of(usuario));

        try {
            // act
            ventaService.realizarVenta(1L, Collections.singletonList(producto1.getId()));
            // assert
            fail("La venta fue hecha");
        } catch (IllegalArgumentException e) {
            assertEquals("El producto " + producto1.getNombre() + " no tiene stock disponible", e.getMessage(), "El producto no tiene stock disponible");
        }
    }
    // create Sale and validate its total value
    @Test
    public void createSale_andCheckTotal(){
        // arrange
        new Usuario();
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("someUser")
                .mascotas(new ArrayList<>())
                .build();
        new Producto();
        Producto producto1 = Producto.builder()
                .id(1L)
                .nombre("someProduct")
                .categoria("someCategoria")
                .precio(BigDecimal.valueOf(100L))
                .stock(1).build();
        new Producto();
        Producto producto2 = Producto.builder()
                .id(2L)
                .nombre("someProduct")
                .categoria("someCategoria")
                .precio(BigDecimal.valueOf(200L))
                .stock(1).build();
        when(productoRepository.findAllById(any())).thenReturn(Arrays.asList(producto1, producto2));
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(usuario));
        when(ventaRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        // act
        VentaDTO venta = ventaService.realizarVenta(1L, Arrays.asList(producto1.getId(), producto2.getId()));
        // assert
        assertEquals(BigDecimal.valueOf(300L), venta.total(), "El total debe corresponder a la suma de ambos productos");
    }
    // get specific
    @Test
    public void getSale_withSpecificId(){
        new Usuario();
        Usuario usuario1 = Usuario.builder()
                .id(1L)
                .nombre("someUser")
                .mascotas(new ArrayList<>())
                .build();
        new Producto();
        Producto producto1 = Producto.builder()
                .id(1L)
                .nombre("someProduct")
                .categoria("someCategoria")
                .precio(BigDecimal.valueOf(200L))
                .stock(1).build();
        new Venta();
        Venta venta1 = Venta
                .builder()
                .id(1L)
                .fecha(now())
                .total(BigDecimal.valueOf(200L))
                .usuario(usuario1)
                .productos(Collections.singletonList(producto1))
                .build();
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta1));
        Optional<VentaDTO> venta = ventaService.obtenerVentaPorId(1L);
        assertNotNull(venta);
        venta.ifPresent(lambdaVenta -> {
            assertEquals(lambdaVenta.id(), venta1.getId());
            assertEquals(lambdaVenta.total(), venta1.getTotal());
            assertEquals(lambdaVenta.fecha(), venta1.getFecha());
        });
    }
    // get specific sale with invalidId
    @Test
    public void getSale_withSpecificId_noSaleFound(){
        when(ventaRepository.findById(1L)).thenReturn(Optional.empty());
        Optional<VentaDTO> venta = ventaService.obtenerVentaPorId(1L);
        assertNotNull(venta);
    }
    // get sale with valid id
    // expects to return only the sale you asked for
    @Test
    public void getSale_withValidId(){
        //Arr
        new Usuario();
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombre("someUser")
                .mascotas(new ArrayList<>())
                .build();
        new Producto();
        Producto producto1 = Producto.builder()
                .id(1L)
                .nombre("someProduct")
                .categoria("someCategoria")
                .precio(BigDecimal.valueOf(200L))
                .stock(1).build();
        new Venta();
        Venta venta = Venta
                .builder()
                .id(1L)
                .fecha(now())
                .total(BigDecimal.valueOf(200L))
                .usuario(usuario)
                .productos(Collections.singletonList(producto1))
                .build();
        when(ventaRepository.findAll()).thenReturn(Collections.singletonList(venta));
        //Ac
        List<VentaDTO> ventas = ventaService.listarTodasLasVentas();
        //As
        assertEquals(1, ventas.size());
    }
    //get sales of specific user
    @Test
    public void getSales_WithSpecicicUser(){
        //Arr
        new Usuario();
        Usuario usuario1 = Usuario.builder()
                .id(1L)
                .nombre("someUser")
                .mascotas(new ArrayList<>())
                .build();
        new Usuario();
        new Producto();
        Producto producto1 = Producto.builder()
                .id(1L)
                .nombre("someProduct")
                .categoria("someCategoria")
                .precio(BigDecimal.valueOf(200L))
                .stock(1).build();
        new Venta();
        Venta venta1 = Venta
                .builder()
                .id(1L)
                .fecha(now())
                .total(BigDecimal.valueOf(200L))
                .usuario(usuario1)
                .productos(Collections.singletonList(producto1))
                .build();
        new Venta();
        Venta venta2 = Venta
                .builder()
                .id(1L)
                .fecha(now())
                .total(BigDecimal.valueOf(200L))
                .usuario(usuario1)
                .productos(Collections.singletonList(producto1))
                .build();
        ventaRepository.save(venta1);
        ventaRepository.save(venta2);
        when(ventaRepository.findByUsuarioId(any())).thenReturn(List.of(venta1));
        //Ac
        List<VentaDTO> ventas = ventaService.listarVentasPorUsuario(1L);
        //As
        assertEquals(1, ventas.size());
    }
    //get sales try with user and no user is found
    @Test
    public void getSales_withSpecificUser_noSaleFound(){
        when(ventaRepository.findByUsuarioId(any())).thenReturn(List.of());
        List<VentaDTO> ventas = ventaService.listarVentasPorUsuario(1L);
        assertEquals(0, ventas.size());
    }
}
