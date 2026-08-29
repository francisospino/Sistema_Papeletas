package com.tuempresa.papeletas.dto;

import com.tuempresa.papeletas.entidad.TipoSalida;
import com.tuempresa.papeletas.entidad.TipoTraslado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PapeletaFormulario {

    public static final int FILAS_ITEMS = 5;

    @NotNull(message = "Selecciona el tipo de salida")
    private TipoSalida tipoSalida;

    private String otrosDescripcion;

    @NotNull(message = "La fecha de salida es obligatoria")
    private LocalDate fechaSalida;

    @NotBlank(message = "Indica el area que autoriza")
    private String areaQueAutoriza;

    @NotBlank(message = "Indica quien autoriza")
    private String autorizadoPor;

    @NotBlank(message = "Indica el destino del bien")
    private String destinoBien;

    @NotBlank(message = "Indica el responsable o solicitante")
    private String responsableSolicitante;

    @NotNull(message = "Selecciona si el traslado es temporal o definitivo")
    private TipoTraslado trasladoTipo;

    private LocalDate fechaRetornoEstimada;

    private String observaciones;

    private List<ItemFormulario> items = nuevaListaItems();

    public static List<ItemFormulario> nuevaListaItems() {
        List<ItemFormulario> lista = new ArrayList<>();
        for (int i = 0; i < FILAS_ITEMS; i++) {
            lista.add(new ItemFormulario());
        }
        return lista;
    }

    public TipoSalida getTipoSalida() {
        return tipoSalida;
    }

    public void setTipoSalida(TipoSalida tipoSalida) {
        this.tipoSalida = tipoSalida;
    }

    public String getOtrosDescripcion() {
        return otrosDescripcion;
    }

    public void setOtrosDescripcion(String otrosDescripcion) {
        this.otrosDescripcion = otrosDescripcion;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getAreaQueAutoriza() {
        return areaQueAutoriza;
    }

    public void setAreaQueAutoriza(String areaQueAutoriza) {
        this.areaQueAutoriza = areaQueAutoriza;
    }

    public String getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(String autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public String getDestinoBien() {
        return destinoBien;
    }

    public void setDestinoBien(String destinoBien) {
        this.destinoBien = destinoBien;
    }

    public String getResponsableSolicitante() {
        return responsableSolicitante;
    }

    public void setResponsableSolicitante(String responsableSolicitante) {
        this.responsableSolicitante = responsableSolicitante;
    }

    public TipoTraslado getTrasladoTipo() {
        return trasladoTipo;
    }

    public void setTrasladoTipo(TipoTraslado trasladoTipo) {
        this.trasladoTipo = trasladoTipo;
    }

    public LocalDate getFechaRetornoEstimada() {
        return fechaRetornoEstimada;
    }

    public void setFechaRetornoEstimada(LocalDate fechaRetornoEstimada) {
        this.fechaRetornoEstimada = fechaRetornoEstimada;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<ItemFormulario> getItems() {
        return items;
    }

    public void setItems(List<ItemFormulario> items) {
        this.items = items;
    }
}
