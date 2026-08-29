package com.tuempresa.papeletas.dto;

import com.tuempresa.papeletas.entidad.PapeletaItem;
import java.util.List;
import java.util.stream.Collectors;

public final class BienesFormato {

    private BienesFormato() {
    }

    public static String resumen(List<PapeletaItem> items) {
        return items.stream()
                .map(BienesFormato::describirItem)
                .collect(Collectors.joining("\n"));
    }

    public static String describirItem(PapeletaItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getCantidad()).append(' ');
        if (tieneTexto(item.getUnidadMedida())) {
            sb.append(item.getUnidadMedida()).append(' ');
        }
        sb.append(item.getDescripcion());

        String marcaModelo = concatenarConEspacio(item.getMarca(), item.getModelo());
        boolean hayMarcaModelo = tieneTexto(marcaModelo);
        boolean haySerie = tieneTexto(item.getSerie());
        if (hayMarcaModelo || haySerie) {
            sb.append(" (");
            if (hayMarcaModelo) {
                sb.append(marcaModelo);
            }
            if (haySerie) {
                if (hayMarcaModelo) {
                    sb.append(", ");
                }
                sb.append("serie ").append(item.getSerie());
            }
            sb.append(')');
        }
        return sb.toString();
    }

    private static String concatenarConEspacio(String a, String b) {
        boolean tieneA = tieneTexto(a);
        boolean tieneB = tieneTexto(b);
        if (tieneA && tieneB) {
            return a + " " + b;
        }
        return tieneA ? a : (tieneB ? b : "");
    }

    private static boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
