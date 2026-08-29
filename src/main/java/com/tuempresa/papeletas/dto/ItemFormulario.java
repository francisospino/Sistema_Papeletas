package com.tuempresa.papeletas.dto;

import com.tuempresa.papeletas.entidad.EstadoBien;

public class ItemFormulario {

    private Integer cantidad;
    private String unidadMedida;
    private String descripcion;
    private String marca;
    private String modelo;
    private String serie;
    private EstadoBien estado;
    private String observaciones;

    public boolean estaVacio() {
        return descripcion == null || descripcion.isBlank();
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public EstadoBien getEstado() {
        return estado;
    }

    public void setEstado(EstadoBien estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
