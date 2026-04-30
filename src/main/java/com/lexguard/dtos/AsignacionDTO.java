package com.lexguard.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignacionDTO {
    @NotNull(message = "El ID del abogado es obligatorio")
    private Long abogadoId;
}
