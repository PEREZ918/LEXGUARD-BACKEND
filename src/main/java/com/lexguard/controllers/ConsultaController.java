package com.lexguard.controllers;

import com.lexguard.dtos.AsignacionDTO;
import com.lexguard.dtos.ConsultaDTO;
import com.lexguard.dtos.RespuestaDTO;
import com.lexguard.dtos.UsuarioSimpleDTO;
import com.lexguard.entities.Consulta;
import com.lexguard.entities.Usuario;
import com.lexguard.services.ConsultaService;
import com.lexguard.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/consultas")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;
    
    @Autowired
    private UsuarioService usuarioService;

    
    private ConsultaDTO mapToDTO(Consulta consulta) {
        ConsultaDTO dto = new ConsultaDTO();
        dto.setId(consulta.getId());
        dto.setTitulo(consulta.getTitulo());
        dto.setDescripcion(consulta.getDescripcion());
        dto.setTipo(consulta.getTipo() != null ? consulta.getTipo().name() : null);
        dto.setEstado(consulta.getEstado() != null ? consulta.getEstado().name() : null);
        dto.setEstadoAceptacion(consulta.getEstadoAceptacion() != null ? consulta.getEstadoAceptacion().name() : null);
        dto.setRespuesta(consulta.getRespuesta());
        dto.setFechaCreacion(consulta.getFechaCreacion());
        dto.setFechaRespuesta(consulta.getFechaRespuesta());
        dto.setRespondidoPorId(consulta.getRespondidoPorId());

        if (consulta.getUsuario() != null) {
            UsuarioSimpleDTO userDto = new UsuarioSimpleDTO();
            userDto.setId(consulta.getUsuario().getId());
            userDto.setNombreCompleto(consulta.getUsuario().getNombreCompleto());
            userDto.setEmail(consulta.getUsuario().getEmail());
            userDto.setRol(consulta.getUsuario().getRol());
            dto.setUsuario(userDto);
        }

        if (consulta.getAbogado() != null) {
            UsuarioSimpleDTO abogadoDto = new UsuarioSimpleDTO();
            abogadoDto.setId(consulta.getAbogado().getId());
            abogadoDto.setNombreCompleto(consulta.getAbogado().getNombreCompleto());
            abogadoDto.setEmail(consulta.getAbogado().getEmail());
            abogadoDto.setRol(consulta.getAbogado().getRol());
            dto.setAbogado(abogadoDto);
        }

        return dto;
    }

    @PostMapping("/crear")
    public ResponseEntity<ConsultaDTO> crearConsulta(
            @Valid @RequestBody Consulta consulta,
            Authentication authentication) {
        try {
            log.info("Creando consulta para usuario: {}", authentication.getName());
            String email = authentication.getName();
            Consulta nuevaConsulta = consultaService.crearConsultaPorEmail(email, consulta);
            
            log.info("Consulta creada exitosamente con ID: {}", nuevaConsulta.getId());
            return ResponseEntity.ok(mapToDTO(nuevaConsulta));
        } catch (Exception e) {
            log.error("Error al crear consulta", e);
            throw e;
        }
    }

    @GetMapping("/mis-consultas")
    public ResponseEntity<Page<ConsultaDTO>> obtenerMisConsultas(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = authentication.getName();
        Page<Consulta> consultas = consultaService.obtenerConsultasPorEmail(
            email, PageRequest.of(page, size, Sort.by("fechaCreacion").descending()));
        return ResponseEntity.ok(consultas.map(this::mapToDTO));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ConsultaDTO> obtenerConsultaPorId(@PathVariable Long id) {
        Consulta consulta = consultaService.obtenerConsultaPorId(id);
        return ResponseEntity.ok(mapToDTO(consulta));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<ConsultaDTO>> obtenerConsultasPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Consulta> consultas = consultaService.obtenerConsultasPorUsuario(
            usuarioId, PageRequest.of(page, size, Sort.by("fechaCreacion").descending()));
        return ResponseEntity.ok(consultas.map(this::mapToDTO));
    }
    
    @GetMapping("/todas")
    @PreAuthorize("hasAnyAuthority('ABOGADO','ADMIN')")
    public ResponseEntity<Page<ConsultaDTO>> obtenerTodasLasConsultas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Consulta> consultas = consultaService.obtenerTodasLasConsultas(
            PageRequest.of(page, size, Sort.by("fechaCreacion").descending()));
        return ResponseEntity.ok(consultas.map(this::mapToDTO));
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyAuthority('ABOGADO','ADMIN')")
    public ResponseEntity<Page<ConsultaDTO>> obtenerConsultasDisponibles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = authentication.getName();
        Page<Consulta> consultas = consultaService.obtenerConsultasDisponiblesPorAbogado(
            email, PageRequest.of(page, size, Sort.by("fechaCreacion").descending()));
        return ResponseEntity.ok(consultas.map(this::mapToDTO));
    }

    @GetMapping("/asignadas")
    @PreAuthorize("hasAnyAuthority('ABOGADO','ADMIN')")
    public ResponseEntity<Page<ConsultaDTO>> obtenerConsultasAsignadas(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = authentication.getName();
        Page<Consulta> consultas = consultaService.obtenerConsultasAsignadas(
            email, PageRequest.of(page, size, Sort.by("fechaCreacion").descending()));
        return ResponseEntity.ok(consultas.map(this::mapToDTO));
    }

    @PostMapping("/{id}/aceptar")
    @PreAuthorize("hasAnyAuthority('ABOGADO','ADMIN')")
    public ResponseEntity<ConsultaDTO> aceptarConsulta(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        Consulta consulta = consultaService.aceptarConsulta(id, email);
        return ResponseEntity.ok(mapToDTO(consulta));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyAuthority('ABOGADO','ADMIN')")
    public ResponseEntity<ConsultaDTO> rechazarConsulta(@PathVariable Long id) {
        Consulta consulta = consultaService.rechazarConsulta(id);
        return ResponseEntity.ok(mapToDTO(consulta));
    }

    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ConsultaDTO> asignarAbogado(
            @PathVariable Long id, 
            @Valid @RequestBody AsignacionDTO asignacionDTO) {
        Consulta consulta = consultaService.asignarAbogado(id, asignacionDTO.getAbogadoId());
        return ResponseEntity.ok(mapToDTO(consulta));
    }
    
    @PutMapping("/{id}/responder")
    @PreAuthorize("hasAnyAuthority('ABOGADO','ADMIN')")
    public ResponseEntity<ConsultaDTO> responderConsulta(
            @PathVariable Long id, 
            @Valid @RequestBody RespuestaDTO respuestaDTO,
            Authentication authentication) {
        
        Usuario usuarioActual = usuarioService.obtenerUsuarioPorEmail(authentication.getName());
        
        Consulta consulta = consultaService.responderConsulta(id, respuestaDTO.getContenido(), usuarioActual.getId());
        return ResponseEntity.ok(mapToDTO(consulta));
    }

    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyAuthority('ABOGADO','ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerEstadisticas() {
        return ResponseEntity.ok(consultaService.obtenerEstadisticas());
    }
}
