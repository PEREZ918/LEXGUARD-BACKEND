package com.lexguard.controllers;

import com.lexguard.dto.LoginDTO;
import com.lexguard.dto.RegistroDTO;
import com.lexguard.entities.Usuario;
import com.lexguard.entities.UserRole;
import com.lexguard.security.JwtUtil;
import com.lexguard.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@Valid @RequestBody RegistroDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(dto.getNombreCompleto());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());
        
        UserRole rol = dto.getRol() != null ? UserRole.valueOf(dto.getRol()) : UserRole.CLIENTE;
        usuario.setRol(rol);
        
        
        if (rol == UserRole.ABOGADO) {
            if (dto.getEspecialidad() == null || dto.getEspecialidad().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Los abogados deben especificar su área de especialidad");
                return ResponseEntity.badRequest().body(error);
            }
            usuario.setEspecialidad(dto.getEspecialidad());
        }

        Usuario nuevoUsuario = usuarioService.registrarUsuario(usuario);
        String token = jwtUtil.generateToken(nuevoUsuario.getEmail(), nuevoUsuario.getRol().toString());

        Map<String, Object> response = new HashMap<>();
        response.put("id", nuevoUsuario.getId());
        response.put("mensaje", "Usuario registrado exitosamente");
        response.put("token", token);
        response.put("email", nuevoUsuario.getEmail());
        response.put("rol", nuevoUsuario.getRol());
        response.put("nombreCompleto", nuevoUsuario.getNombreCompleto());
     

        if (nuevoUsuario.getEspecialidad() != null) {
            response.put("especialidad", nuevoUsuario.getEspecialidad());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO dto) {
        Usuario usuario = usuarioService.login(dto.getEmail(), dto.getPassword());
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().toString());

        Map<String, Object> response = new HashMap<>();
        response.put("id", usuario.getId());
        response.put("mensaje", "Login exitoso");
        response.put("token", token);
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol());
        response.put("nombreCompleto", usuario.getNombreCompleto());
        if (usuario.getEspecialidad() != null) {
            response.put("especialidad", usuario.getEspecialidad());
        }

        return ResponseEntity.ok(response);
    }
}
