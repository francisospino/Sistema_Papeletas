package com.tuempresa.papeletas.controlador;

import com.tuempresa.papeletas.dto.PapeletaResumen;
import com.tuempresa.papeletas.dto.ResumenConteo;
import com.tuempresa.papeletas.entidad.EstadoPapeleta;
import com.tuempresa.papeletas.servicio.PapeletaService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/papeletas")
public class PapeletaApiController {

    private final PapeletaService papeletaService;

    public PapeletaApiController(PapeletaService papeletaService) {
        this.papeletaService = papeletaService;
    }

    @GetMapping
    public List<PapeletaResumen> listar(@RequestParam(required = false) String numero,
                                         @RequestParam(required = false) String responsable,
                                         @RequestParam(required = false) String destino,
                                         @RequestParam(required = false) String estado) {
        EstadoPapeleta estadoFiltro = EstadoPapeleta.desde(estado);
        boolean hayFiltro = tieneTexto(numero) || tieneTexto(responsable) || tieneTexto(destino) || estadoFiltro != null;

        return hayFiltro
                ? papeletaService.resumenBuscar(numero, responsable, destino, estadoFiltro)
                : papeletaService.resumenTodas();
    }

    @GetMapping("/resumen")
    public ResumenConteo resumen() {
        return papeletaService.obtenerResumenConteo();
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
