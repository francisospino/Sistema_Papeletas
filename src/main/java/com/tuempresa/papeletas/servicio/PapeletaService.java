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
import com.tuempresa.papeletas.entidad.TipoSalida;
import com.tuempresa.papeletas.entidad.TipoTraslado;
import com.tuempresa.papeletas.repositorio.PapeletaRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class PapeletaService {

    private static final int DIGITOS_NUMERO = 6;
    private static final String UNIDAD_POR_DEFECTO = "UND";

    private final PapeletaRepository papeletaRepository;
    private final CorreoService correoService;
    private final ExcelExportService excelExportService;
    private final TransactionTemplate plantillaTransaccion;

    // Serializa la generacion del numero + guardado: esta app corre como una unica
    // instancia central en la LAN, asi que un lock en memoria basta para evitar
    // que dos registros simultaneos reciban el mismo numero.
    //
    // El lock envuelve a la transaccion, no al reves: si estuviera dentro de un metodo
    // @Transactional se soltaria antes del commit y el siguiente hilo leeria el maximo
    // sin ver todavia el registro recien insertado, generando el mismo numero y
    // reventando contra la constraint unica de papeleta.numero.
    private final Object bloqueoNumeracion = new Object();

    public PapeletaService(PapeletaRepository papeletaRepository, CorreoService correoService,
                            ExcelExportService excelExportService,
                            PlatformTransactionManager gestorTransacciones) {
        this.papeletaRepository = papeletaRepository;
        this.correoService = correoService;
        this.excelExportService = excelExportService;
        this.plantillaTransaccion = new TransactionTemplate(gestorTransacciones);
    }

    public Papeleta registrar(PapeletaFormulario formulario) {
        validarFormulario(formulario);

        Papeleta guardada;
        synchronized (bloqueoNumeracion) {
            guardada = plantillaTransaccion.execute(estadoTx -> guardar(formulario));
        }

        // Fuera de la transaccion y del lock: un SMTP lento ya no mantiene abierta la
        // transaccion ni bloquea el registro de las demas papeletas.
        correoService.notificarRegistro(guardada);
        return guardada;
    }

    private Papeleta guardar(PapeletaFormulario formulario) {
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

        for (ItemFormulario itemForm : items(formulario)) {
            if (itemForm.estaVacio()) {
                continue;
            }
            papeleta.agregarItem(PapeletaItem.builder()
                    .cantidad(itemForm.getCantidad())
                    .unidadMedida(unidadOPorDefecto(itemForm.getUnidadMedida()))
                    .descripcion(itemForm.getDescripcion())
                    .marca(itemForm.getMarca())
                    .modelo(itemForm.getModelo())
                    .serie(itemForm.getSerie())
                    .estado(itemForm.getEstado() != null ? itemForm.getEstado() : EstadoBien.OPERATIVO)
                    .observaciones(itemForm.getObservaciones())
                    .build());
        }

        return papeletaRepository.save(papeleta);
    }

    /**
     * Comprueba antes de tocar la base todo lo que Bean Validation no puede expresar:
     * las filas de bienes a medio llenar (que antes llegaban con cantidad o unidad en
     * null a columnas NOT NULL y devolvian un error 500 en vez de un mensaje) y las
     * reglas que dependen del valor de otro campo del formulario.
     */
    private void validarFormulario(PapeletaFormulario formulario) {
        if (formulario.getTipoSalida() == TipoSalida.OTROS && esBlanco(formulario.getOtrosDescripcion())) {
            throw new IllegalArgumentException("Describe el motivo cuando el tipo de salida es OTROS");
        }
        if (formulario.getTrasladoTipo() == TipoTraslado.TEMPORAL
                && formulario.getFechaRetornoEstimada() != null
                && formulario.getFechaSalida() != null
                && formulario.getFechaRetornoEstimada().isBefore(formulario.getFechaSalida())) {
            throw new IllegalArgumentException(
                    "La fecha de retorno estimada no puede ser anterior a la fecha de salida");
        }

        int fila = 0;
        int bienesRegistrados = 0;
        for (ItemFormulario item : items(formulario)) {
            fila++;
            if (item.estaVacio()) {
                continue;
            }
            if (esBlanco(item.getDescripcion())) {
                throw new IllegalArgumentException("Fila " + fila + " de bienes: falta la descripcion del bien");
            }
            if (item.getCantidad() == null || item.getCantidad() < 1) {
                throw new IllegalArgumentException("Fila " + fila + " de bienes: la cantidad debe ser 1 o mas");
            }
            bienesRegistrados++;
        }

        if (bienesRegistrados == 0) {
            throw new IllegalArgumentException("La papeleta debe tener al menos un bien registrado");
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
        if (papeleta.getTrasladoTipo() != TipoTraslado.TEMPORAL) {
            throw new IllegalArgumentException("La papeleta N° " + papeleta.getNumero()
                    + " es una salida definitiva, no corresponde registrar retorno");
        }
        if (papeleta.getEstado() == EstadoPapeleta.RETORNADA) {
            throw new IllegalArgumentException("La papeleta N° " + papeleta.getNumero()
                    + " ya tiene el retorno registrado");
        }
        if (fechaRetorno.isBefore(papeleta.getFechaSalida())) {
            throw new IllegalArgumentException("La fecha de retorno no puede ser anterior a la fecha de salida");
        }
        papeleta.registrarRetorno(fechaRetorno);
        return papeleta;
    }

    @Transactional(readOnly = true)
    public List<Papeleta> listarTodas() {
        return papeletaRepository.findAllByOrderByFechaRegistroDesc();
    }

    @Transactional(readOnly = true)
    public List<Papeleta> listarPendientesRetorno() {
        return papeletaRepository.findByEstadoAndTrasladoTipoOrderByFechaRegistroDesc(
                EstadoPapeleta.ACTIVA, TipoTraslado.TEMPORAL);
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

    private List<ItemFormulario> items(PapeletaFormulario formulario) {
        return formulario.getItems() == null ? List.of() : formulario.getItems();
    }

    private String unidadOPorDefecto(String unidadMedida) {
        return esBlanco(unidadMedida) ? UNIDAD_POR_DEFECTO : unidadMedida.trim();
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    private String blankToNull(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
