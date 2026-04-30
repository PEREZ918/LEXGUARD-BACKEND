package com.lexguard.controllers;

import com.lexguard.entities.Usuario;
import com.lexguard.entities.UserRole;
import com.lexguard.repositories.UsuarioRepository;
import com.lexguard.exceptions.ResourceNotFoundException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<Usuario>> listarUsuarios() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> obtenerUsuarioActual(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, Long>> obtenerStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsuarios", usuarioRepository.count());
        stats.put("clientes", (long) usuarioRepository.findByRol(UserRole.CLIENTE).size());
        stats.put("abogados", (long) usuarioRepository.findByRol(UserRole.ABOGADO).size());
        stats.put("admins", (long) usuarioRepository.findByRol(UserRole.ADMIN).size());
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{id}/rol")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> cambiarRol(
        @PathVariable Long id,
        @RequestBody Map<String, String> payload
    ) {
        return usuarioRepository.findById(id).map(usuario -> {
            try {
                
                String rolString = payload.get("rol");
                if (rolString == null || rolString.trim().isEmpty()) {
                    return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("El rol no puede estar vacío"));
                }

                UserRole nuevoRol = UserRole.fromValue(rolString);
                usuario.setRol(nuevoRol);
                usuarioRepository.save(usuario);
                
                return ResponseEntity.ok(usuario);
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        usuarioRepository.delete(usuario);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado exitosamente");
        return ResponseEntity.ok(response);
    }

    
    @Data
    public static class ErrorResponse {
        private String error;
        private long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
