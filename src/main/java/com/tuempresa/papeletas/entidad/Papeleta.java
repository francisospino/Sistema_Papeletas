package com.tuempresa.papeletas.entidad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "papeleta")
public class Papeleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_salida", nullable = false)
    private TipoSalida tipoSalida;

    @Column(name = "otros_descripcion")
    private String otrosDescripcion;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "area_que_autoriza", nullable = false)
    private String areaQueAutoriza;

    @Column(name = "autorizado_por", nullable = false)
    private String autorizadoPor;

    @Column(name = "destino_bien", nullable = false)
    private String destinoBien;

    @Column(name = "responsable_solicitante", nullable = false)
    private String responsableSolicitante;

    @Enumerated(EnumType.STRING)
    @Column(name = "traslado_tipo", nullable = false)
    private TipoTraslado trasladoTipo;

    @Column(name = "fecha_retorno")
    private LocalDate fechaRetorno;

    @Column(name = "fecha_retorno_estimada")
    private LocalDate fechaRetornoEstimada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPapeleta estado;

    private String observaciones;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @OneToMany(mappedBy = "papeleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PapeletaItem> items = new ArrayList<>();

    protected Papeleta() {
    }

    private Papeleta(Builder builder) {
        this.numero = builder.numero;
        this.tipoSalida = builder.tipoSalida;
        this.otrosDescripcion = builder.otrosDescripcion;
        this.fechaSalida = builder.fechaSalida;
        this.areaQueAutoriza = builder.areaQueAutoriza;
        this.autorizadoPor = builder.autorizadoPor;
        this.destinoBien = builder.destinoBien;
        this.responsableSolicitante = builder.responsableSolicitante;
        this.trasladoTipo = builder.trasladoTipo;
        // Una salida definitiva nunca vuelve; ignora cualquier fecha estimada que
        // se haya enviado para ese caso.
        this.fechaRetornoEstimada = builder.trasladoTipo == TipoTraslado.TEMPORAL ? builder.fechaRetornoEstimada : null;
        this.observaciones = builder.observaciones;
        this.estado = EstadoPapeleta.ACTIVA;
        this.fechaRegistro = LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public void agregarItem(PapeletaItem item) {
        item.setPapeleta(this);
        items.add(item);
    }

    public void registrarRetorno(LocalDate fechaRetorno) {
        this.fechaRetorno = fechaRetorno;
        this.estado = EstadoPapeleta.RETORNADA;
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public TipoSalida getTipoSalida() {
        return tipoSalida;
    }

    public String getOtrosDescripcion() {
        return otrosDescripcion;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public String getAreaQueAutoriza() {
        return areaQueAutoriza;
    }

    public String getAutorizadoPor() {
        return autorizadoPor;
    }

    public String getDestinoBien() {
        return destinoBien;
    }

    public String getResponsableSolicitante() {
        return responsableSolicitante;
    }

    public TipoTraslado getTrasladoTipo() {
        return trasladoTipo;
    }

    public LocalDate getFechaRetorno() {
        return fechaRetorno;
    }

    public LocalDate getFechaRetornoEstimada() {
        return fechaRetornoEstimada;
    }

    public EstadoPapeleta getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public List<PapeletaItem> getItems() {
        return items;
    }

    public static class Builder {
        private String numero;
        private TipoSalida tipoSalida;
        private String otrosDescripcion;
        private LocalDate fechaSalida;
        private String areaQueAutoriza;
        private String autorizadoPor;
        private String destinoBien;
        private String responsableSolicitante;
        private TipoTraslado trasladoTipo;
        private LocalDate fechaRetornoEstimada;
        private String observaciones;

        public Builder numero(String numero) {
            this.numero = numero;
            return this;
        }

        public Builder tipoSalida(TipoSalida tipoSalida) {
            this.tipoSalida = tipoSalida;
            return this;
        }

        public Builder otrosDescripcion(String otrosDescripcion) {
            this.otrosDescripcion = otrosDescripcion;
            return this;
        }

        public Builder fechaSalida(LocalDate fechaSalida) {
            this.fechaSalida = fechaSalida;
            return this;
        }

        public Builder areaQueAutoriza(String areaQueAutoriza) {
            this.areaQueAutoriza = areaQueAutoriza;
            return this;
        }

        public Builder autorizadoPor(String autorizadoPor) {
            this.autorizadoPor = autorizadoPor;
            return this;
        }

        public Builder destinoBien(String destinoBien) {
            this.destinoBien = destinoBien;
            return this;
        }

        public Builder responsableSolicitante(String responsableSolicitante) {
            this.responsableSolicitante = responsableSolicitante;
            return this;
        }

        public Builder trasladoTipo(TipoTraslado trasladoTipo) {
            this.trasladoTipo = trasladoTipo;
            return this;
        }

        public Builder fechaRetornoEstimada(LocalDate fechaRetornoEstimada) {
            this.fechaRetornoEstimada = fechaRetornoEstimada;
            return this;
        }

        public Builder observaciones(String observaciones) {
            this.observaciones = observaciones;
            return this;
        }

        public Papeleta build() {
            return new Papeleta(this);
        }
    }
}
