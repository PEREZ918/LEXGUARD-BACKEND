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
import java.util.List;

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
        mensaje.setContenido(contenido);
        mensaje.setTipoEmisor(tipoEmisor);
        mensaje.setFechaCreacion(LocalDateTime.now());

        Mensaje guardado = mensajeRepository.save(mensaje);
        messagingTemplate.convertAndSend("/topic/consulta/" + consultaId, guardado);

        return guardado;
    }

    public List<Mensaje> obtenerMensajesConsulta(Long consultaId) {
        return mensajeRepository.findByConsultaIdOrderByFechaCreacionAsc(consultaId);
    }

    public List<Mensaje> obtenerMensajesConsultaOrdenado(Long consultaId) {
        return mensajeRepository.findByConsultaIdOrderByFechaCreacionAsc(consultaId);
    }
}
