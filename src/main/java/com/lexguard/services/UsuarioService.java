package com.lexguard.services;

import com.lexguard.entities.Usuario;
import com.lexguard.entities.UserRole;
import com.lexguard.exceptions.EmailAlreadyExistsException;
import com.lexguard.exceptions.InvalidCredentialsException;
import com.lexguard.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario registrarUsuario(Usuario usuario) {
        Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());
        if (existente.isPresent()) {
            throw new EmailAlreadyExistsException("El correo " + usuario.getEmail() + " ya está registrado.");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        if (usuario.getRol() == null) {
            usuario.setRol(UserRole.CLIENTE);
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().getPassword())) {
            return usuario.get();
        }
        throw new InvalidCredentialsException("Credenciales inválidas para el correo proporcionado.");
    }
    
    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).orElseThrow(() -> 
            new com.lexguard.exceptions.ResourceNotFoundException("Usuario no encontrado: " + email));
    }
}
