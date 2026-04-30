package com.lexguard.repositories;

import com.lexguard.entities.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);
    List<Notificacion> findByUsuarioEmailOrderByFechaCreacionDesc(String email);
    long countByUsuarioIdAndLeidaFalse(Long usuarioId);
    long countByUsuarioEmailAndLeidaFalse(String email);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Notificacion n SET n.leida = true WHERE n.usuario.email = :email AND n.leida = false")
    void marcarTodasComoLeidas(@org.springframework.data.repository.query.Param("email") String email);
}
