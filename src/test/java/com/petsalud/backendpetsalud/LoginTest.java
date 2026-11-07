package com.petsalud.backendpetsalud;

import com.petsalud.backendpetsalud.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.testng.Assert;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertThrows;
import static org.testng.AssertJUnit.assertNotNull;

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

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    void checkLogin_withValidCredentials(){
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("test", "test");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(token);

        Authentication authentication = authenticationManager.authenticate(token);

        assertNotNull(authentication);
        verify(authenticationManager, times(1)).authenticate(token);
    }

    @Test
    void checkLogin_withInvalidCredentials(){
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("test", "badPassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(BadCredentialsException.class);

        assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(token));

        verify(authenticationManager, times(1)).authenticate(token);
    }

}
