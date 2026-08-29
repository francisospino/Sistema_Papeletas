package com.tuempresa.papeletas.servicio;

import com.tuempresa.papeletas.dto.Conteo;
import com.tuempresa.papeletas.dto.ItemFormulario;
import com.tuempresa.papeletas.dto.PapeletaFormulario;
import com.tuempresa.papeletas.dto.PapeletaResumen;
import com.tuempresa.papeletas.dto.ResumenConteo;
import com.tuempresa.papeletas.entidad.EstadoBien;
import com.tuempresa.papeletas.entidad.EstadoPapeleta;
import com.tuempresa.papeletas.entidad.Papeleta;
import com.tuempresa.papeletas.entidad.PapeletaItem;
import com.tuempresa.papeletas.entidad.TipoTraslado;
import com.tuempresa.papeletas.repositorio.PapeletaRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PapeletaService {

    private static final int DIGITOS_NUMERO = 6;

    private final PapeletaRepository papeletaRepository;
    private final CorreoService correoService;
    private final ExcelExportService excelExportService;

    // Serializa la generacion del numero + guardado: esta app corre como una unica
    // instancia central en la LAN, asi que un lock en memoria basta para evitar
    // que dos registros simultaneos reciban el mismo numero.
    private final Object bloqueoNumeracion = new Object();

    public PapeletaService(PapeletaRepository papeletaRepository, CorreoService correoService,
                            ExcelExportService excelExportService) {
        this.papeletaRepository = papeletaRepository;
        this.correoService = correoService;
        this.excelExportService = excelExportService;
    }

    @Transactional
    public Papeleta registrar(PapeletaFormulario formulario) {
        synchronized (bloqueoNumeracion) {
            Papeleta papeleta = Papeleta.builder()
                    .numero(generarSiguienteNumero())
                    .tipoSalida(formulario.getTipoSalida())
                    .otrosDescripcion(formulario.getOtrosDescripcion())
                    .fechaSalida(formulario.getFechaSalida())
                    .areaQueAutoriza(formulario.getAreaQueAutoriza())
                    .autorizadoPor(formulario.getAutorizadoPor())
                    .destinoBien(formulario.getDestinoBien())
                    .responsableSolicitante(formulario.getResponsableSolicitante())
                    .trasladoTipo(formulario.getTrasladoTipo())
                    .fechaRetornoEstimada(formulario.getFechaRetornoEstimada())
                    .observaciones(formulario.getObservaciones())
                    .build();

            for (ItemFormulario itemForm : formulario.getItems()) {
                if (itemForm.estaVacio()) {
                    continue;
                }
                papeleta.agregarItem(PapeletaItem.builder()
                        .cantidad(itemForm.getCantidad())
                        .unidadMedida(itemForm.getUnidadMedida())
                        .descripcion(itemForm.getDescripcion())
                        .marca(itemForm.getMarca())
                        .modelo(itemForm.getModelo())
                        .serie(itemForm.getSerie())
                        .estado(itemForm.getEstado() != null ? itemForm.getEstado() : EstadoBien.OPERATIVO)
                        .observaciones(itemForm.getObservaciones())
                        .build());
            }

            if (papeleta.getItems().isEmpty()) {
                throw new IllegalArgumentException("La papeleta debe tener al menos un bien registrado");
            }

            Papeleta guardada = papeletaRepository.save(papeleta);
            correoService.notificarRegistro(guardada);
            return guardada;
        }
    }

    private String generarSiguienteNumero() {
        int siguiente = papeletaRepository.findTopByOrderByNumeroDesc()
                .map(p -> Integer.parseInt(p.getNumero()) + 1)
                .orElse(1);
        return String.format("%0" + DIGITOS_NUMERO + "d", siguiente);
    }

    @Transactional
    public Papeleta registrarRetorno(Long id, LocalDate fechaRetorno) {
        Papeleta papeleta = papeletaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Papeleta no encontrada: " + id));
        papeleta.registrarRetorno(fechaRetorno);
        return papeleta;
    }

    @Transactional(readOnly = true)
    public List<Papeleta> listarTodas() {
        return papeletaRepository.findAllByOrderByFechaRegistroDesc();
    }

    @Transactional(readOnly = true)
    public List<Papeleta> listarActivas() {
        return papeletaRepository.findByEstadoOrderByFechaRegistroDesc(EstadoPapeleta.ACTIVA);
    }

    @Transactional(readOnly = true)
    public List<Papeleta> buscar(String numero, String responsable, String destino, EstadoPapeleta estado) {
        return papeletaRepository.buscar(
                blankToNull(numero), blankToNull(responsable), blankToNull(destino), estado);
    }

    @Transactional(readOnly = true)
    public List<PapeletaResumen> resumenTodas() {
        return listarTodas().stream().map(PapeletaResumen::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<PapeletaResumen> resumenBuscar(String numero, String responsable, String destino, EstadoPapeleta estado) {
        return buscar(numero, responsable, destino, estado).stream().map(PapeletaResumen::desde).toList();
    }

    @Transactional(readOnly = true)
    public ResumenConteo obtenerResumenConteo() {
        long total = papeletaRepository.count();
        long activas = papeletaRepository.countByEstado(EstadoPapeleta.ACTIVA);
        long retornadas = papeletaRepository.countByEstado(EstadoPapeleta.RETORNADA);
        long pendientesRetorno = papeletaRepository.countByEstadoAndTrasladoTipo(EstadoPapeleta.ACTIVA, TipoTraslado.TEMPORAL);
        return new ResumenConteo(total, activas, retornadas, pendientesRetorno);
    }

    @Transactional(readOnly = true)
    public byte[] exportarExcel(String numero, String responsable, String destino, EstadoPapeleta estado) throws IOException {
        boolean hayFiltro = blankToNull(numero) != null || blankToNull(responsable) != null
                || blankToNull(destino) != null || estado != null;
        List<Papeleta> papeletas = hayFiltro ? buscar(numero, responsable, destino, estado) : listarTodas();
        return excelExportService.generarListado(papeletas);
    }

    @Transactional(readOnly = true)
    public List<Conteo> reportePorDestino() {
        return papeletaRepository.contarPorDestino().stream()
                .map(fila -> new Conteo((String) fila[0], (Long) fila[1]))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Conteo> reportePorArea() {
        return papeletaRepository.contarPorArea().stream()
                .map(fila -> new Conteo((String) fila[0], (Long) fila[1]))
                .toList();
    }

    private String blankToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
