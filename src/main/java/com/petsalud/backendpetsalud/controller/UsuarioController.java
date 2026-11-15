package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.UsuarioDTO;
import com.petsalud.backendpetsalud.dto.UsuarioRequest;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public Usuario translateRequest(UsuarioRequest usuario) {
        Usuario user = new Usuario();
        // translate dto
        user.setEmail(usuario.email());
        String password = new BCryptPasswordEncoder().encode(usuario.password());
        user.setPassword(password);
        user.setNombre(usuario.nombre());
        user.setApellido(usuario.apellido());
        user.setTelefono(usuario.telefono());
        user.setDireccion(usuario.direccion());
        user.setRol(Rol.valueOf(usuario.rol().toUpperCase()));
        user.setMascotas(new ArrayList<>());
        user.setVentas(new ArrayList<>());
        user.setNotificaciones(new ArrayList<>());
        user.setRecompensas(new ArrayList<>());
        return user;
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody UsuarioRequest usuario) {

        try {
            if (usuario.password().isEmpty() || usuario.email().isEmpty()) {
                throw new IllegalArgumentException("Email/Contraseña no válido");
            }
            if (usuario.rol().equalsIgnoreCase("admin") || usuario.rol().equalsIgnoreCase("veterinario")) {
                throw new IllegalArgumentException("Rol no permitido");
            }
                usuarioService.registrarUsuario(translateRequest(usuario));
                return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<String> registrarUsuarioAsAdmin(@RequestBody UsuarioRequest usuario) {
        try {
            if (usuario.password().isEmpty() || usuario.email().isEmpty()) {
            throw new IllegalArgumentException("Email/Contraseña no válido");
        }
            usuarioService.registrarUsuario(translateRequest(usuario));
            return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/detalle/{id}")
    public ResponseEntity<UsuarioDTO> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/detalle/{id}")
    public ResponseEntity<UsuarioDTO> actualizarUsuario(@PathVariable Long id, @RequestBody UsuarioRequest usuario) {
        UsuarioDTO usuarioActualizado = usuarioService.actualizarUsuario(id, translateRequest(usuario));
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/detalle/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('VETERINARIO')")
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
}
