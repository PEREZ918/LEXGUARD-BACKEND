package com.lexguard.controllers;

import com.lexguard.entities.Mensaje;
import com.lexguard.services.MensajeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mensajes")
public class MensajeController {

    @Autowired
    private MensajeService mensajeService;

    @PostMapping("/consulta/{consultaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> enviarMensaje(
            @PathVariable Long consultaId,
            @RequestBody Map<String, String> payload,
            Authentication authentication) {
        try {
            String contenido = payload.get("contenido");
            String tipoEmisor = payload.get("tipoEmisor"); 
            String email = authentication.getName();

            if (contenido == null || contenido.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "El contenido del mensaje no puede estar vacío");
                return ResponseEntity.badRequest().body(error);
            }

            Mensaje mensaje = mensajeService.crearMensaje(consultaId, email, contenido, tipoEmisor);
            return ResponseEntity.ok(mensaje);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al enviar el mensaje: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/consulta/{consultaId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Mensaje>> obtenerMensajes(@PathVariable Long consultaId) {
        List<Mensaje> mensajes = mensajeService.obtenerMensajesConsultaOrdenado(consultaId);
        return ResponseEntity.ok(mensajes);
    }
}
