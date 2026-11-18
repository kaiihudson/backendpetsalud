package com.petsalud.backendpetsalud.service;

import com.petsalud.backendpetsalud.dto.UsuarioDTO;
import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // create a user with minimal info
    @Test
    public void createUser_withMinimalInfo(){
        Usuario user = Usuario
                .builder()
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);

        usuarioService.registrarUsuario(user);

        assertNull(user.getRecompensas());
        assertNull(user.getNotificaciones());
    }
    // try to create a user that already exists
    @Test
    public void createUser_withAnAlreadyExistentUser(){
        Usuario user = Usuario
                .builder()
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        Usuario cloneUser = Usuario
                .builder()
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.findByEmail("testEmail")).thenReturn(Optional.of(user));
        try {
            usuarioService.registrarUsuario(cloneUser);
            fail("no encontro el usuario");
        } catch (IllegalArgumentException e) {
            assertEquals("El email ya está registrado", e.getMessage());
        }
    }
    // get a usuario, given its id
    @Test
    public void getUser_withID(){
        Usuario user = Usuario
                .builder()
                .id(1L)
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(user));
        Optional<UsuarioDTO> responseUser = usuarioService.obtenerUsuarioPorId(1L);
        assertNotNull(responseUser);
        responseUser.ifPresent( lambdaUser -> {
            assertEquals(lambdaUser.id(), user.getId());
            assertEquals(lambdaUser.email(), user.getEmail());
            assertEquals(lambdaUser.nombre(), user.getNombre());
            assertEquals(lambdaUser.apellido(), user.getApellido());
            assertEquals(lambdaUser.direccion(), user.getDireccion());
        });
    }
    // get a usuario, given its id, find no user
    @Test
    public void getUser_withID_findNothing(){
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());
        Optional<UsuarioDTO> responseUser = usuarioService.obtenerUsuarioPorId(1L);
        assertNotNull(responseUser);
    }
    // get a usuario, given its email
    @Test
    public void getUser_withEmail(){
        Usuario user = Usuario
                .builder()
                .id(1L)
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(user));
        Optional<UsuarioDTO> responseUser = usuarioService.obtenerUsuarioPorEmail("testEmail");
        assertNotNull(responseUser);
        responseUser.ifPresent( lambdaUser -> {
            assertEquals(lambdaUser.id(), user.getId());
            assertEquals(lambdaUser.email(), user.getEmail());
            assertEquals(lambdaUser.nombre(), user.getNombre());
            assertEquals(lambdaUser.apellido(), user.getApellido());
            assertEquals(lambdaUser.direccion(), user.getDireccion());
        });
    }
    // get a usuario, given its email, find no user
    @Test
    public void getUser_withEmail_findNothing(){
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        Optional<UsuarioDTO> responseUser = usuarioService.obtenerUsuarioPorEmail("testEmail");
        assertNotNull(responseUser);
    }
    // modify a usuario, with a new usuario
    @Test
    public void modifyUser_withNewUser(){
        Usuario user = Usuario
                .builder()
                .id(1L)
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        Usuario newUser = Usuario
                .builder()
                .id(1L)
                .email("testEmail")
                .password("password")
                .nombre("newName")
                .apellido("newApellido")
                .direccion("newDireccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .telefono("123456789")
                .build();
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(newUser);
        UsuarioDTO responseUser = usuarioService.actualizarUsuario(1L, newUser);
        assertNotNull(responseUser);
        assertEquals(responseUser.id(), newUser.getId());
        assertEquals(responseUser.email(), newUser.getEmail());
        assertEquals(responseUser.nombre(), newUser.getNombre());
        assertEquals(responseUser.apellido(), newUser.getApellido());
        assertEquals(responseUser.direccion(), newUser.getDireccion());
        assertEquals(responseUser.telefono(), newUser.getTelefono());
    }
    // modify a usuario, when usuario is invalid
    @Test
    public void modifyUser_withNewUser_whenOGUserIsInvalid(){

        Usuario newUser = Usuario
                .builder()
                .id(1L)
                .email("testEmail")
                .password("password")
                .nombre("newName")
                .apellido("newApellido")
                .direccion("newDireccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .telefono("123456789")
                .build();
        when(usuarioRepository.findById(any())).thenReturn(Optional.empty());
        try {
            usuarioService.actualizarUsuario(1L, newUser);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Usuario no encontrado", e.getMessage());
        }
    }
    // delete a user with invalid id
    @Test
    public void deleteUser(){
        Long id = 1L;
        usuarioService.eliminarUsuario(id);
        verify(usuarioRepository, times(1)).deleteById(id);
    }
    // list multiple users
    @Test
    public void listUsers(){
        Usuario user1 = Usuario
                .builder()
                .id(1L)
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        Usuario user2 = Usuario
                .builder()
                .id(2L)
                .email("testEmail2")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
                .mascotas(new ArrayList<>())
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        List<UsuarioDTO> usuarios = usuarioService.listarUsuarios();
        assertEquals(2, usuarios.size());
    }
}
