package com.lexguard.repositories;

import com.lexguard.entities.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByConsultaId(Long consultaId);
    List<Mensaje> findByConsultaIdOrderByFechaCreacionAsc(Long consultaId);
}
