package com.lexguard.services;

import com.lexguard.entities.Notificacion;
import com.lexguard.entities.Usuario;
import com.lexguard.exceptions.ResourceNotFoundException;
import com.lexguard.repositories.NotificacionRepository;
import com.lexguard.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Notificacion crearNotificacion(Usuario destinatario, String mensaje, String tipo) {
        Notificacion notificacion = new Notificacion();
        notificacion.setMensaje(mensaje);
        notificacion.setTipo(tipo);
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacion.setUsuario(destinatario);
        return notificacionRepository.save(notificacion);
    }

    public void notificarATodosLosAbogados(String mensaje) {
        List<Usuario> abogados = usuarioRepository.findByRol(com.lexguard.entities.UserRole.ABOGADO);
        for (Usuario abogado : abogados) {
            crearNotificacion(abogado, mensaje, "INFO");
        }
    }

    public List<Notificacion> obtenerNotificacionesPorEmail(String email) {
        return notificacionRepository.findByUsuarioEmailOrderByFechaCreacionDesc(email);
    }

    public long contarNoLeidasPorEmail(String email) {
        return notificacionRepository.countByUsuarioEmailAndLeidaFalse(email);
    }

    public Notificacion marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + notificacionId));
        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }

    @org.springframework.transaction.annotation.Transactional
    public void marcarTodasComoLeidas(String email) {
        notificacionRepository.marcarTodasComoLeidas(email);
    }
}
