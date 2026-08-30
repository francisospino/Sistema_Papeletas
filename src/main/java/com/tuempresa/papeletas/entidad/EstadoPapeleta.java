package com.tuempresa.papeletas.entidad;

public enum EstadoPapeleta {
    ACTIVA,
    RETORNADA;

    /**
     * Convierte el valor que llega por querystring. Devuelve null si viene vacio o si
     * no corresponde a ningun estado, para que un filtro invalido en la URL
     * (por ejemplo ?estado=CUALQUIERCOSA) se ignore en vez de romper la pagina.
     */
    public static EstadoPapeleta desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return EstadoPapeleta.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
