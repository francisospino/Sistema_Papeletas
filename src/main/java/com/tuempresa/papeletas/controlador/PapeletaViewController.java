package com.tuempresa.papeletas.controlador;

import com.tuempresa.papeletas.dto.PapeletaFormulario;
import com.tuempresa.papeletas.entidad.EstadoBien;
import com.tuempresa.papeletas.entidad.EstadoPapeleta;
import com.tuempresa.papeletas.entidad.Papeleta;
import com.tuempresa.papeletas.entidad.TipoSalida;
import com.tuempresa.papeletas.entidad.TipoTraslado;
import com.tuempresa.papeletas.servicio.PapeletaService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/papeletas")
public class PapeletaViewController {

    private static final DateTimeFormatter FORMATO_ARCHIVO = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final PapeletaService papeletaService;

    public PapeletaViewController(PapeletaService papeletaService) {
        this.papeletaService = papeletaService;
    }

    @GetMapping
    public String listado(@RequestParam(required = false) String numero,
                           @RequestParam(required = false) String responsable,
                           @RequestParam(required = false) String destino,
                           @RequestParam(required = false) String estado,
                           Model model) {
        // La tabla se pinta y autorefresca desde /api/papeletas, asi que aqui solo se
        // devuelven los valores necesarios para repintar los filtros.
        model.addAttribute("numero", numero);
        model.addAttribute("responsable", responsable);
        model.addAttribute("destino", destino);
        model.addAttribute("estado", estado);
        model.addAttribute("estados", EstadoPapeleta.values());
        return "index";
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam(required = false) String numero,
                                            @RequestParam(required = false) String responsable,
                                            @RequestParam(required = false) String destino,
                                            @RequestParam(required = false) String estado) {
        byte[] archivo;
        try {
            archivo = papeletaService.exportarExcel(numero, responsable, destino, EstadoPapeleta.desde(estado));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo generar el archivo Excel", e);
        }

        String nombreArchivo = "papeletas-" + LocalDateTime.now().format(FORMATO_ARCHIVO) + ".xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nombreArchivo).build().toString())
                .body(archivo);
    }

    @GetMapping("/nueva")
    public String formularioNuevo(Model model) {
        if (!model.containsAttribute("formulario")) {
            model.addAttribute("formulario", new PapeletaFormulario());
        }
        agregarListasApoyo(model);
        return "registrar";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("formulario") PapeletaFormulario formulario,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            agregarListasApoyo(model);
            return "registrar";
        }

        try {
            Papeleta guardada = papeletaService.registrar(formulario);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Papeleta N° " + guardada.getNumero() + " registrada correctamente.");
            return "redirect:/papeletas";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            agregarListasApoyo(model);
            return "registrar";
        }
    }

    @PostMapping("/{id}/retorno")
    public String registrarRetorno(@PathVariable Long id,
                                    @RequestParam(required = false) String fechaRetorno,
                                    RedirectAttributes redirectAttributes) {
        LocalDate fecha;
        try {
            fecha = tieneTexto(fechaRetorno) ? LocalDate.parse(fechaRetorno) : LocalDate.now();
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "La fecha de retorno no tiene un formato valido");
            return "redirect:/papeletas";
        }

        try {
            Papeleta actualizada = papeletaService.registrarRetorno(id, fecha);
            redirectAttributes.addFlashAttribute("mensaje",
                    "Retorno registrado para la papeleta N° " + actualizada.getNumero() + ".");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/papeletas";
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("porDestino", papeletaService.reportePorDestino());
        model.addAttribute("porArea", papeletaService.reportePorArea());
        model.addAttribute("pendientesRetorno", papeletaService.listarPendientesRetorno());
        return "reportes";
    }

    private void agregarListasApoyo(Model model) {
        model.addAttribute("tiposSalida", TipoSalida.values());
        model.addAttribute("tiposTraslado", TipoTraslado.values());
        model.addAttribute("estadosBien", EstadoBien.values());
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
