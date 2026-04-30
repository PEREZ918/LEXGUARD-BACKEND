package com.lexguard.repositories;

import com.lexguard.entities.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.lexguard.entities.enums.ConsultaEstado;
import com.lexguard.entities.enums.AceptacionEstado;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    
    @EntityGraph(attributePaths = {"usuario", "abogado"})
    Page<Consulta> findByUsuarioId(Long usuarioId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"usuario", "abogado"})
    Page<Consulta> findByUsuarioEmail(String email, Pageable pageable);
    
    long countByEstado(ConsultaEstado estado);
    
    @EntityGraph(attributePaths = {"usuario", "abogado"})
    Page<Consulta> findByEstadoAceptacion(AceptacionEstado estadoAceptacion, Pageable pageable);
    
    @EntityGraph(attributePaths = {"usuario", "abogado"})
    Page<Consulta> findByAbogadoId(Long abogadoId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"usuario", "abogado"})
    Page<Consulta> findByEstadoAceptacionAndTipo(AceptacionEstado estadoAceptacion, com.lexguard.entities.enums.ConsultaTipo tipo, Pageable pageable);
    
    @Override
    @EntityGraph(attributePaths = {"usuario", "abogado"})
    Page<Consulta> findAll(Pageable pageable);
    
    
    @EntityGraph(attributePaths = {"usuario", "abogado"})
    List<Consulta> findByUsuarioEmail(String email);
}
