package com.tuempresa.papeletas.repositorio;

import com.tuempresa.papeletas.entidad.EstadoPapeleta;
import com.tuempresa.papeletas.entidad.Papeleta;
import com.tuempresa.papeletas.entidad.TipoTraslado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PapeletaRepository extends JpaRepository<Papeleta, Long> {

    Optional<Papeleta> findTopByOrderByNumeroDesc();

    List<Papeleta> findByEstadoOrderByFechaRegistroDesc(EstadoPapeleta estado);

    List<Papeleta> findAllByOrderByFechaRegistroDesc();

    long countByEstado(EstadoPapeleta estado);

    long countByEstadoAndTrasladoTipo(EstadoPapeleta estado, TipoTraslado trasladoTipo);

    @Query("select p from Papeleta p where "
            + "(:responsable is null or lower(p.responsableSolicitante) like lower(concat('%', :responsable, '%'))) "
            + "and (:destino is null or lower(p.destinoBien) like lower(concat('%', :destino, '%'))) "
            + "and (:numero is null or lower(p.numero) like lower(concat('%', :numero, '%'))) "
            + "and (:estado is null or p.estado = :estado) "
            + "order by p.fechaRegistro desc")
    List<Papeleta> buscar(@Param("numero") String numero,
                           @Param("responsable") String responsable,
                           @Param("destino") String destino,
                           @Param("estado") EstadoPapeleta estado);

    @Query("select p.destinoBien, count(p) from Papeleta p group by p.destinoBien order by count(p) desc")
    List<Object[]> contarPorDestino();

    @Query("select p.areaQueAutoriza, count(p) from Papeleta p group by p.areaQueAutoriza order by count(p) desc")
    List<Object[]> contarPorArea();
}
