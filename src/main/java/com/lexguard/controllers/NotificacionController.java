package com.lexguard.controllers;

import com.lexguard.entities.Notificacion;
import com.lexguard.services.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<Notificacion>> obtenerMisNotificaciones(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(notificacionService.obtenerNotificacionesPorEmail(email));
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(Authentication authentication) {
        String email = authentication.getName();
        Map<String, Long> result = new HashMap<>();
        result.put("count", notificacionService.contarNoLeidasPorEmail(email));
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/leer")
    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }

    @PutMapping("/leer-todas")
    public ResponseEntity<Map<String, String>> marcarTodasComoLeidas(Authentication authentication) {
        String email = authentication.getName();
        notificacionService.marcarTodasComoLeidas(email);
        Map<String, String> result = new HashMap<>();
        result.put("mensaje", "Todas las notificaciones marcadas como leídas");
        return ResponseEntity.ok(result);
    }
}
