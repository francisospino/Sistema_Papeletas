package com.tuempresa.papeletas.dto;

import com.tuempresa.papeletas.entidad.EstadoPapeleta;
import com.tuempresa.papeletas.entidad.Papeleta;
import com.tuempresa.papeletas.entidad.TipoSalida;
import com.tuempresa.papeletas.entidad.TipoTraslado;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PapeletaResumen(
        Long id,
        String numero,
        TipoSalida tipoSalida,
        LocalDate fechaSalida,
        String areaQueAutoriza,
        String autorizadoPor,
        String destinoBien,
        String responsableSolicitante,
        TipoTraslado trasladoTipo,
        EstadoPapeleta estado,
        LocalDate fechaRetorno,
        LocalDate fechaRetornoEstimada,
        String bienesResumen,
        LocalDateTime fechaRegistro) {

    public static PapeletaResumen desde(Papeleta papeleta) {
        return new PapeletaResumen(
                papeleta.getId(),
                papeleta.getNumero(),
                papeleta.getTipoSalida(),
                papeleta.getFechaSalida(),
                papeleta.getAreaQueAutoriza(),
                papeleta.getAutorizadoPor(),
                papeleta.getDestinoBien(),
                papeleta.getResponsableSolicitante(),
                papeleta.getTrasladoTipo(),
                papeleta.getEstado(),
                papeleta.getFechaRetorno(),
                papeleta.getFechaRetornoEstimada(),
                BienesFormato.resumen(papeleta.getItems()),
                papeleta.getFechaRegistro());
    }
}
