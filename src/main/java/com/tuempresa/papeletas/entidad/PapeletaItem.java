package com.tuempresa.papeletas.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "papeleta_item")
public class PapeletaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "papeleta_id", nullable = false)
    private Papeleta papeleta;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    @Column(nullable = false)
    private String descripcion;

    private String marca;

    private String modelo;

    private String serie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoBien estado;

    private String observaciones;

    protected PapeletaItem() {
    }

    private PapeletaItem(Builder builder) {
        this.papeleta = builder.papeleta;
        this.cantidad = builder.cantidad;
        this.unidadMedida = builder.unidadMedida;
        this.descripcion = builder.descripcion;
        this.marca = builder.marca;
        this.modelo = builder.modelo;
        this.serie = builder.serie;
        this.estado = builder.estado;
        this.observaciones = builder.observaciones;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Papeleta getPapeleta() {
        return papeleta;
    }

    public void setPapeleta(Papeleta papeleta) {
        this.papeleta = papeleta;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public String getSerie() {
        return serie;
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

    public static class Builder {
        private Papeleta papeleta;
        private Integer cantidad;
        private String unidadMedida;
        private String descripcion;
        private String marca;
        private String modelo;
        private String serie;
        private EstadoBien estado;
        private String observaciones;

        public Builder papeleta(Papeleta papeleta) {
            this.papeleta = papeleta;
            return this;
        }

        public Builder cantidad(Integer cantidad) {
            this.cantidad = cantidad;
            return this;
        }

        public Builder unidadMedida(String unidadMedida) {
            this.unidadMedida = unidadMedida;
            return this;
        }

        public Builder descripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public Builder marca(String marca) {
            this.marca = marca;
            return this;
        }

        public Builder modelo(String modelo) {
            this.modelo = modelo;
            return this;
        }

        public Builder serie(String serie) {
            this.serie = serie;
            return this;
        }

        public Builder estado(EstadoBien estado) {
            this.estado = estado;
            return this;
        }

        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public PapeletaItem build() {
            return new PapeletaItem(this);
        }
    }
}
