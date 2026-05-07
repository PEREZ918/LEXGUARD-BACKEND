package com.lexguard.websocket;

import com.lexguard.entities.Mensaje;
import com.lexguard.services.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private MensajeService mensajeService;

    @MessageMapping("/chat/{consultaId}")
    @SendTo("/topic/consulta/{consultaId}")
    public Map<String, Object> enviarMensajeChat(
            @DestinationVariable Long consultaId,
            Map<String, Object> payload) {
        try {
            String contenido = (String) payload.get("contenido");
            String tipoEmisor = (String) payload.get("tipoEmisor");
            String email = (String) payload.get("email");

            Mensaje mensaje = mensajeService.crearMensaje(consultaId, email, contenido, tipoEmisor);

            Map<String, Object> response = new HashMap<>();
            response.put("id", mensaje.getId());
            response.put("contenido", mensaje.getContenido());
            response.put("tipoEmisor", mensaje.getTipoEmisor());
            response.put("usuarioId", mensaje.getUsuario().getId());
            response.put("usuarioNombre", mensaje.getUsuario().getNombreCompleto());
            response.put("fechaCreacion", mensaje.getFechaCreacion());
            response.put("success", true);

            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return error;
        }
    }
}
