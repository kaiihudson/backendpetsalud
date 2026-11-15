package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.MascotaDTO;
import com.petsalud.backendpetsalud.dto.UsuarioDTO;
import com.petsalud.backendpetsalud.entity.Mascota;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UsuarioDTO translateUserDTO(Usuario usuario) {
        List<Mascota> mascotas = usuario.getMascotas();
        List<MascotaDTO> mascotaDTOS = mascotas
                .stream()
                .map(mascota -> new MascotaDTO(
                        mascota.getId(),
                        mascota.getNombre(),
                        mascota.getEspecie(),
                        mascota.getRaza(),
                        mascota.getSexo()
                ))
                .toList();
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                mascotaDTOS
        );
    }

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        // Verificar si el email ya está registrado
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        Optional<Usuario> user =  usuarioRepository.findById(id);
        if (user.isPresent()) {
            Usuario usuario = user.get();
            return Optional.of(translateUserDTO(usuario));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        Optional<Usuario> user =  usuarioRepository.findByEmail(email);
        if (user.isPresent()) {
            Usuario usuario = user.get();
            return Optional.of(translateUserDTO(usuario));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public UsuarioDTO actualizarUsuario(Long id, Usuario usuarioActualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(usuarioActualizado.getNombre());
            usuario.setApellido(usuarioActualizado.getApellido());
            usuario.setTelefono(usuarioActualizado.getTelefono());
            usuario.setDireccion(usuarioActualizado.getDireccion());
            // No actualizar email ni password aquí por seguridad
            Usuario modifiedUser = usuarioRepository.save(usuario);
            return translateUserDTO(modifiedUser);
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public void eliminarUsuario(Long id) {
        usuarioRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::translateUserDTO)
                .toList();
    }
}
