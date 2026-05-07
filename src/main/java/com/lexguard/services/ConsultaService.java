package com.lexguard.services;

import com.lexguard.entities.Consulta;
import com.lexguard.entities.Usuario;
import com.lexguard.entities.enums.ConsultaEstado;
import com.lexguard.entities.enums.ConsultaTipo;
import com.lexguard.entities.enums.AceptacionEstado;
import com.lexguard.exceptions.ResourceNotFoundException;
import com.lexguard.repositories.ConsultaRepository;
import com.lexguard.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@org.springframework.transaction.annotation.Transactional
public class ConsultaService {

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionService notificacionService;

    public Consulta crearConsulta(Long usuarioId, Consulta consulta) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + usuarioId));

        consulta.setUsuario(usuario);
        consulta.setFechaCreacion(LocalDateTime.now());
        if (consulta.getEstado() == null) {
            consulta.setEstado(ConsultaEstado.PENDIENTE);
        }
        if (consulta.getEstadoAceptacion() == null) {
            consulta.setEstadoAceptacion(AceptacionEstado.PENDIENTE_ACEPTACION);
        }

        if (consulta.getDescripcion() != null) {
            consulta.setDescripcion(HtmlUtils.htmlEscape(consulta.getDescripcion()));
        }

        Consulta guardada = consultaRepository.save(consulta);

        notificacionService.notificarATodosLosAbogados(
                "Nueva consulta legal recibida: \"" + consulta.getTitulo() + "\" por " + usuario.getNombreCompleto());

        return guardada;
    }

    public Consulta crearConsultaPorEmail(String email, Consulta consulta) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con email: " + email));
        return crearConsulta(usuario.getId(), consulta);
    }

    public Page<Consulta> obtenerConsultasPorUsuario(Long usuarioId, Pageable pageable) {
        return consultaRepository.findByUsuarioId(usuarioId, pageable);
    }

    public Page<Consulta> obtenerConsultasPorEmail(String email, Pageable pageable) {
        return consultaRepository.findByUsuarioEmail(email, pageable);
    }

    public Consulta obtenerConsultaPorId(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la consulta con ID: " + id));
    }

    public Page<Consulta> obtenerTodasLasConsultas(Pageable pageable) {
        return consultaRepository.findAll(pageable);
    }

    public Page<Consulta> obtenerConsultasDisponiblesPorAbogado(String emailAbogado, Pageable pageable) {
        Usuario abogado = usuarioRepository.findByEmail(emailAbogado)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No se encontró el abogado con email: " + emailAbogado));

        if (abogado.getEspecialidad() != null && !abogado.getEspecialidad().isEmpty()) {
            try {
                ConsultaTipo tipo = ConsultaTipo.valueOf(abogado.getEspecialidad());
                return consultaRepository.findByEstadoAceptacionAndTipo(AceptacionEstado.PENDIENTE_ACEPTACION, tipo,
                        pageable);
            } catch (IllegalArgumentException e) {

            }
        }

        return consultaRepository.findByEstadoAceptacion(AceptacionEstado.PENDIENTE_ACEPTACION, pageable);
    }

    public Page<Consulta> obtenerConsultasAsignadas(String emailAbogado, Pageable pageable) {
        Usuario abogado = usuarioRepository.findByEmail(emailAbogado)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No se encontró el abogado con email: " + emailAbogado));

        return consultaRepository.findByAbogadoId(abogado.getId(), pageable);
    }

    public Consulta asignarAbogado(Long consultaId, Long abogadoId) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la consulta con ID: " + consultaId));

        Usuario abogado = usuarioRepository.findById(abogadoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el abogado con ID: " + abogadoId));

        consulta.setAbogado(abogado);
        consulta.setEstadoAceptacion(AceptacionEstado.ACEPTADA);
        Consulta guardada = consultaRepository.save(consulta);

        notificacionService.crearNotificacion(
                consulta.getUsuario(),
                "Tu consulta \"" + consulta.getTitulo() + "\" ha sido asignada al abogado "
                        + abogado.getNombreCompleto(),
                "INFO");

        return guardada;
    }

    public Consulta aceptarConsulta(Long consultaId, String emailAbogado) {
        Usuario abogado = usuarioRepository.findByEmail(emailAbogado)
                .orElseThrow(
                        () -> new ResourceNotFoundException("No se encontró el abogado con email: " + emailAbogado));

        return asignarAbogado(consultaId, abogado.getId());
    }

    public Consulta rechazarConsulta(Long consultaId) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la consulta con ID: " + consultaId));

        consulta.setAbogado(null);
        consulta.setEstadoAceptacion(AceptacionEstado.RECHAZADA);
        return consultaRepository.save(consulta);
    }

    public Consulta responderConsulta(Long consultaId, String respuesta, Long respondidoPorId) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la consulta con ID: " + consultaId));

        String respuestaSanitizada = HtmlUtils.htmlEscape(respuesta);

        consulta.setRespuesta(respuestaSanitizada);
        consulta.setEstado(ConsultaEstado.RESUELTA);
        consulta.setRespondidoPorId(respondidoPorId);
        consulta.setFechaRespuesta(LocalDateTime.now());

        Consulta guardada = consultaRepository.save(consulta);

        notificacionService.crearNotificacion(
                consulta.getUsuario(),
                "Tu consulta \"" + consulta.getTitulo() + "\" ha sido respondida.",
                "EXITO");

        return guardada;
    }

    public Map<String, Long> obtenerEstadisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalConsultas", consultaRepository.count());
        stats.put("pendientes", consultaRepository.countByEstado(ConsultaEstado.PENDIENTE));
        stats.put("resueltas", consultaRepository.countByEstado(ConsultaEstado.RESUELTA));
        stats.put("totalUsuarios", usuarioRepository.count());
        return stats;
    }
}
