package com.lexguard.services;

import com.lexguard.entities.Mensaje;
import com.lexguard.entities.Consulta;
import com.lexguard.entities.Usuario;
import com.lexguard.repositories.MensajeRepository;
import com.lexguard.repositories.ConsultaRepository;
import com.lexguard.repositories.UsuarioRepository;
import com.lexguard.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MensajeService {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Mensaje crearMensaje(Long consultaId, String emailUsuario, String contenido, String tipoEmisor) {
        Consulta consulta = consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la consulta con ID: " + consultaId));

        if (!"ACEPTADA".equals(consulta.getEstadoAceptacion())) {
            throw new IllegalStateException("La consulta debe estar aceptada para enviar mensajes");
        }

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con email: " + emailUsuario));

        Mensaje mensaje = new Mensaje();
        mensaje.setConsulta(consulta);
        mensaje.setUsuario(usuario);
        if (tipoEmisor == null) tipoEmisor = "";
        mensaje.setContenido(contenido);
        mensaje.setTipoEmisor(tipoEmisor);
        mensaje.setFechaCreacion(LocalDateTime.now());

        Mensaje guardado = mensajeRepository.save(mensaje);

        // Enviar DTO limpio por WebSocket para evitar serialización circular de JPA
        Map<String, Object> wsPayload = new HashMap<>();
        wsPayload.put("id", guardado.getId());
        wsPayload.put("contenido", guardado.getContenido());
        wsPayload.put("tipoEmisor", guardado.getTipoEmisor());
        wsPayload.put("usuarioId", guardado.getUsuario().getId());
        wsPayload.put("usuarioNombre", guardado.getUsuario().getNombreCompleto());
        wsPayload.put("fechaCreacion", guardado.getFechaCreacion().toString());
        messagingTemplate.convertAndSend("/topic/consulta/" + consultaId, (Object) wsPayload);

        return guardado;
    }

    public List<Mensaje> obtenerMensajesConsulta(Long consultaId) {
        return mensajeRepository.findByConsultaIdOrderByFechaCreacionAsc(consultaId);
    }

    public List<Mensaje> obtenerMensajesConsultaOrdenado(Long consultaId) {
        return mensajeRepository.findByConsultaIdOrderByFechaCreacionAsc(consultaId);
    }
}
