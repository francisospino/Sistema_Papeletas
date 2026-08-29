package com.tuempresa.papeletas.repositorio;

import com.tuempresa.papeletas.entidad.Destinatario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinatarioRepository extends JpaRepository<Destinatario, Long> {

    List<Destinatario> findAllByOrderByEmailAsc();

    Optional<Destinatario> findByEmailIgnoreCase(String email);
}
