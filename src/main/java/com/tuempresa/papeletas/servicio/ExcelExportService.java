package com.tuempresa.papeletas.servicio;

import com.tuempresa.papeletas.dto.BienesFormato;
import com.tuempresa.papeletas.entidad.Papeleta;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelExportService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String[] ENCABEZADOS = {
            "N° papeleta", "Tipo de salida", "Fecha de salida", "Área que autoriza",
            "Autorizado por", "Destino del bien", "Responsable/solicitante", "Traslado",
            "Estado", "Fecha de retorno", "Bienes"
    };

    public byte[] generarListado(List<Papeleta> papeletas) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = workbook.createSheet("Papeletas");
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloTexto = crearEstiloTextoConAjuste(workbook);

            escribirEncabezado(hoja, estiloEncabezado);

            int numeroFila = 1;
            for (Papeleta papeleta : papeletas) {
                Row fila = hoja.createRow(numeroFila++);
                escribirCelda(fila, 0, papeleta.getNumero(), estiloTexto);
                escribirCelda(fila, 1, papeleta.getTipoSalida().toString(), estiloTexto);
                escribirCelda(fila, 2, formatearFecha(papeleta.getFechaSalida()), estiloTexto);
                escribirCelda(fila, 3, papeleta.getAreaQueAutoriza(), estiloTexto);
                escribirCelda(fila, 4, papeleta.getAutorizadoPor(), estiloTexto);
                escribirCelda(fila, 5, papeleta.getDestinoBien(), estiloTexto);
                escribirCelda(fila, 6, papeleta.getResponsableSolicitante(), estiloTexto);
                escribirCelda(fila, 7, papeleta.getTrasladoTipo().toString(), estiloTexto);
                escribirCelda(fila, 8, papeleta.getEstado().toString(), estiloTexto);
                escribirCelda(fila, 9, formatearFecha(papeleta.getFechaRetorno()), estiloTexto);
                escribirCelda(fila, 10, BienesFormato.resumen(papeleta.getItems()), estiloTexto);
            }

            for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
                hoja.autoSizeColumn(columna);
            }

            workbook.write(salida);
            return salida.toByteArray();
        }
    }

    private void escribirEncabezado(Sheet hoja, CellStyle estilo) {
        Row filaEncabezado = hoja.createRow(0);
        for (int columna = 0; columna < ENCABEZADOS.length; columna++) {
            Cell celda = filaEncabezado.createCell(columna);
            celda.setCellValue(ENCABEZADOS[columna]);
            celda.setCellStyle(estilo);
        }
    }

    private void escribirCelda(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor == null ? "" : valor);
        celda.setCellStyle(estilo);
    }

    private String formatearFecha(java.time.LocalDate fecha) {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    private CellStyle crearEstiloEncabezado(XSSFWorkbook workbook) {
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFont(fuente);
        return estilo;
    }

    private CellStyle crearEstiloTextoConAjuste(XSSFWorkbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setWrapText(true);
        return estilo;
    }
}
