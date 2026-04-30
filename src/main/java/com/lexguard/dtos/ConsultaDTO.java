package com.lexguard.dtos;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConsultaDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private String tipo;
    private String estado;
    private String estadoAceptacion;
    private String respuesta;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRespuesta;
    private Long respondidoPorId;
    private UsuarioSimpleDTO usuario;
    private UsuarioSimpleDTO abogado;
}
