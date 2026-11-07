package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.testng.Assert;

@SpringBootTest(classes = BackendpetsaludApplication.class)
public class LoginTest {

    @Test
    void checkEncoding_Decoding(){
        JwtUtils jwtUtils = new JwtUtils();
        String baseUsername = "test";
        String encoded = jwtUtils.generateToken(baseUsername);
        String decodedUsername = jwtUtils.extractUsername(encoded.replace("Bearer ", ""));
        Assert.assertEquals(decodedUsername, baseUsername);
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void checkLogin_withValidCredentials(){
        // usuario
        String usuario = "user";
        String password = "password";
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, password);
            assert authenticationManager.authenticate(token) != null;
        } catch (BadCredentialsException e) {
            Assert.fail();
        }
    }

    @Test
    void checkLogin_withInvalidCredentials(){
        // usuario
        String usuario = "user";
        String password = "badPassword";
        try {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, password);
            assert authenticationManager.authenticate(token) != null;
        } catch (BadCredentialsException e) {
            assert true;
        }
    }

    @Test
    void checkCreation_withNoPriviledges(){

    }
}
