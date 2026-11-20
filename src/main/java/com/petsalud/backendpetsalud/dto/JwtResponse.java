package com.petsalud.backendpetsalud.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type;
    private String username;

    public JwtResponse(String token, String type, String username) {
        this.token = token;
        this.type = type;
        this.username = username;
    }
}
