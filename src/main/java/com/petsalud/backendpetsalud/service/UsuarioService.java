package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.UsuarioDTO;
import com.petsalud.backendpetsalud.entity.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario registrarUsuario(Usuario usuario);
    Optional<UsuarioDTO> obtenerUsuarioPorId(Long id);
    Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email);
    UsuarioDTO actualizarUsuario(Long id, Usuario usuario);
    void eliminarUsuario(Long id);
    List<UsuarioDTO> listarUsuarios();
    boolean checkForPet(String username, Long id);
}
