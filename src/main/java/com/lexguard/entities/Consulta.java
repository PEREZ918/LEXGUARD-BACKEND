package com.lexguard.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank(message = "El título es obligatorio")
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "La descripción de la consulta es obligatoria")
    private String descripcion;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.lexguard.entities.enums.ConsultaTipo tipo = com.lexguard.entities.enums.ConsultaTipo.CIVIL; 

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.lexguard.entities.enums.ConsultaEstado estado = com.lexguard.entities.enums.ConsultaEstado.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String respuesta;

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abogado_id", nullable = true)
    @JsonIgnoreProperties({"password", "consultas"})
    private Usuario abogado;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.lexguard.entities.enums.AceptacionEstado estadoAceptacion = com.lexguard.entities.enums.AceptacionEstado.PENDIENTE_ACEPTACION;

    @Column(name = "respondido_por_id")
    private Long respondidoPorId;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"password", "consultasRelacionadas"})
    private Usuario usuario;
}
