package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.UsuarioRequest;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private Usuario translateRequest(UsuarioRequest usuario) {
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
        return user;
    }

    @PostMapping("/registro")
    public ResponseEntity<String> registrarUsuario(@RequestBody UsuarioRequest usuario) {
        if (usuario.password().isEmpty() || usuario.email().isEmpty()) {
            throw new IllegalArgumentException("Email/Contraseña no válido");
        }
        if (usuario.rol().equalsIgnoreCase("admin") || usuario.rol().equalsIgnoreCase("veterinario")) {
            throw new IllegalArgumentException("Rol no permitido");
        }
        try {
            usuarioService.registrarUsuario(translateRequest(usuario));
            return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin")
    public ResponseEntity<String> registrarUsuarioAsAdmin(@RequestBody UsuarioRequest usuario) {
        if (usuario.password().isEmpty() || usuario.email().isEmpty()) {
            throw new IllegalArgumentException("Email/Contraseña no válido");
        }
        try {
            usuarioService.registrarUsuario(translateRequest(usuario));
            return ResponseEntity.status(HttpStatus.CREATED).body("CREATED");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return usuarioService.obtenerUsuarioPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuario) {
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','VETERINARIO')")
    @GetMapping
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }
}
