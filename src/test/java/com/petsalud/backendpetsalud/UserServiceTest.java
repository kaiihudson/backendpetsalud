package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.entity.Rol;
import com.petsalud.backendpetsalud.entity.Usuario;
import com.petsalud.backendpetsalud.repository.UsuarioRepository;
import com.petsalud.backendpetsalud.service.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
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
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);

        usuarioService.registrarUsuario(user);

        assertNull(user.getMascotas());
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
                .rol(Rol.CLIENTE)
                .build();
        Usuario cloneUser = Usuario
                .builder()
                .email("testEmail")
                .password("password")
                .nombre("name")
                .apellido("apellido")
                .direccion("direccion")
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
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(user));
        Optional<Usuario> responseUser = usuarioService.obtenerUsuarioPorId(1L);
        assertNotNull(responseUser);
        responseUser.ifPresent( lambdaUser -> {
            assertEquals(lambdaUser.getId(), user.getId());
            assertEquals(lambdaUser.getEmail(), user.getEmail());
            assertEquals(lambdaUser.getNombre(), user.getNombre());
            assertEquals(lambdaUser.getApellido(), user.getApellido());
            assertEquals(lambdaUser.getDireccion(), user.getDireccion());
            assertEquals(lambdaUser.getRol(), user.getRol());
        });
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
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.of(user));
        Optional<Usuario> responseUser = usuarioService.obtenerUsuarioPorEmail("testEmail");
        assertNotNull(responseUser);
        responseUser.ifPresent( lambdaUser -> {
            assertEquals(lambdaUser.getId(), user.getId());
            assertEquals(lambdaUser.getEmail(), user.getEmail());
            assertEquals(lambdaUser.getNombre(), user.getNombre());
            assertEquals(lambdaUser.getApellido(), user.getApellido());
            assertEquals(lambdaUser.getDireccion(), user.getDireccion());
            assertEquals(lambdaUser.getRol(), user.getRol());
        });
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
                .rol(Rol.CLIENTE)
                .telefono("123456789")
                .build();
        when(usuarioRepository.findById(any())).thenReturn(Optional.of(user));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(newUser);
        Usuario responseUser = usuarioService.actualizarUsuario(1L, newUser);
        assertNotNull(responseUser);
        assertEquals(responseUser.getId(), newUser.getId());
        assertEquals(responseUser.getEmail(), newUser.getEmail());
        assertEquals(responseUser.getNombre(), newUser.getNombre());
        assertEquals(responseUser.getApellido(), newUser.getApellido());
        assertEquals(responseUser.getDireccion(), newUser.getDireccion());
        assertEquals(responseUser.getRol(), newUser.getRol());
        assertEquals(responseUser.getTelefono(), newUser.getTelefono());
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
                .rol(Rol.CLIENTE)
                .build();
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        assertEquals(2, usuarios.size());
    }
}
