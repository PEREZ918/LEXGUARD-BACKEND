package com.lexguard.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RespuestaDTO {
    @NotBlank(message = "La respuesta no puede estar vacía")
    @Size(max = 5000, message = "La respuesta es muy larga, debe tener máximo 5000 caracteres")
    private String contenido;
}
