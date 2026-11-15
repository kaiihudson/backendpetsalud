package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.MascotaDTO;
import com.petsalud.backendpetsalud.dto.ProductoDTO;
import com.petsalud.backendpetsalud.dto.UsuarioDTO;
import com.petsalud.backendpetsalud.dto.VentaDTO;
import com.petsalud.backendpetsalud.entity.Producto;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.entity.Venta;
import com.petsalud.backendpetsalud.repository.ProductoRepository;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    public VentaDTO realizarVenta(Long usuarioId, List<Long> productosIds) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<Producto> productos = productoRepository.findAllById(productosIds);

        if (productos.size() != productosIds.size()) {
            throw new IllegalArgumentException("Algunos productos no fueron encontrados");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Producto producto : productos) {
            if (producto.getStock() <= 0) {
                throw new IllegalArgumentException("El producto " + producto.getNombre() + " no tiene stock disponible");
            }
            // Restar stock
            producto.setStock(producto.getStock() - 1);
            productoRepository.save(producto);

            total = total.add(producto.getPrecio());
        }

        Venta venta = Venta.builder()
                .usuario(usuario)
                .productos(productos)
                .total(total)
                .fecha(LocalDateTime.now())
                .build();

        Venta ventaRealizada = ventaRepository.save(venta);
        return new VentaDTO(
                ventaRealizada.getId(),
                ventaRealizada.getFecha(),
                ventaRealizada.getTotal(),
                new UsuarioDTO(
                        ventaRealizada.getUsuario().getId(),
                        ventaRealizada.getUsuario().getEmail(),
                        ventaRealizada.getUsuario().getNombre(),
                        ventaRealizada.getUsuario().getApellido(),
                        ventaRealizada.getUsuario().getTelefono(),
                        ventaRealizada.getUsuario().getDireccion(),
                        ventaRealizada.getUsuario().getMascotas()
                                .stream()
                                .map(mascota -> new MascotaDTO(
                                                mascota.getId(),
                                                mascota.getNombre(),
                                                mascota.getEspecie(),
                                                mascota.getRaza(),
                                                mascota.getSexo()
                                        )
                                ).toList()),
                ventaRealizada.getProductos()
                        .stream()
                        .map(producto -> new ProductoDTO(
                                producto.getId(),
                                producto.getNombre(),
                                producto.getDescripcion(),
                                producto.getPrecio(),
                                producto.getStock(),
                                producto.getCategoria()
                        ))
                        .toList()
        );
    }

    @Override
    public Optional<VentaDTO> obtenerVentaPorId(Long id) {
        Optional<Venta> venta = ventaRepository.findById(id);
        if (venta.isPresent()) {
            Venta ventaFinal = venta.get();
            return Optional.of(new VentaDTO(
                    ventaFinal.getId(),
                    ventaFinal.getFecha(),
                    ventaFinal.getTotal(),
                    new UsuarioDTO(
                            ventaFinal.getUsuario().getId(),
                            ventaFinal.getUsuario().getEmail(),
                            ventaFinal.getUsuario().getNombre(),
                            ventaFinal.getUsuario().getApellido(),
                            ventaFinal.getUsuario().getTelefono(),
                            ventaFinal.getUsuario().getDireccion(),
                            ventaFinal.getUsuario().getMascotas()
                                    .stream()
                                    .map(mascota -> new MascotaDTO(
                                                    mascota.getId(),
                                                    mascota.getNombre(),
                                                    mascota.getEspecie(),
                                                    mascota.getRaza(),
                                                    mascota.getSexo()
                                            )
                                    ).toList()),
                    ventaFinal.getProductos()
                            .stream()
                            .map(producto -> new ProductoDTO(
                                    producto.getId(),
                                    producto.getNombre(),
                                    producto.getDescripcion(),
                                    producto.getPrecio(),
                                    producto.getStock(),
                                    producto.getCategoria()
                            ))
                            .toList()
            ));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<VentaDTO> listarVentasPorUsuario(Long usuarioId) {
        List<Venta> ventas = ventaRepository.findByUsuarioId(usuarioId);
        return ventas
                .stream()
                .map(venta -> new VentaDTO(
                        venta.getId(),
                        venta.getFecha(),
                        venta.getTotal(),
                        new UsuarioDTO(
                                venta.getUsuario().getId(),
                                venta.getUsuario().getEmail(),
                                venta.getUsuario().getNombre(),
                                venta.getUsuario().getApellido(),
                                venta.getUsuario().getTelefono(),
                                venta.getUsuario().getDireccion(),
                                venta.getUsuario().getMascotas()
                                        .stream()
                                        .map(mascota -> new MascotaDTO(
                                                        mascota.getId(),
                                                        mascota.getNombre(),
                                                        mascota.getEspecie(),
                                                        mascota.getRaza(),
                                                        mascota.getSexo()
                                                )
                                        ).toList()),
                        venta.getProductos()
                                .stream()
                                .map(producto -> new ProductoDTO(
                                        producto.getId(),
                                        producto.getNombre(),
                                        producto.getDescripcion(),
                                        producto.getPrecio(),
                                        producto.getStock(),
                                        producto.getCategoria()
                                ))
                                .toList()
                ))
                .toList();
    }

    @Override
    public List<VentaDTO> listarTodasLasVentas() {
        return ventaRepository.findAll()
                .stream()
                .map(venta -> new VentaDTO(
                        venta.getId(),
                        venta.getFecha(),
                        venta.getTotal(),
                        new UsuarioDTO(
                                venta.getUsuario().getId(),
                                venta.getUsuario().getEmail(),
                                venta.getUsuario().getNombre(),
                                venta.getUsuario().getApellido(),
                                venta.getUsuario().getTelefono(),
                                venta.getUsuario().getDireccion(),
                                venta.getUsuario().getMascotas()
                                        .stream()
                                        .map(mascota -> new MascotaDTO(
                                                        mascota.getId(),
                                                        mascota.getNombre(),
                                                        mascota.getEspecie(),
                                                        mascota.getRaza(),
                                                        mascota.getSexo()
                                                )
                                        ).toList()),
                        venta.getProductos()
                                .stream()
                                .map(producto -> new ProductoDTO(
                                        producto.getId(),
                                        producto.getNombre(),
                                        producto.getDescripcion(),
                                        producto.getPrecio(),
                                        producto.getStock(),
                                        producto.getCategoria()
                                ))
                                .toList()
                ))
                .toList();
    }
}

