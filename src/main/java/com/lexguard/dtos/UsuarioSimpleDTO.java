package com.lexguard.dtos;

import com.lexguard.entities.UserRole;
import lombok.Data;

@Data
public class UsuarioSimpleDTO {
    private Long id;
    private String nombreCompleto;
    private String email;
    private UserRole rol;
}
