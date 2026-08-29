package com.tuempresa.papeletas.servicio;

import com.tuempresa.papeletas.entidad.Papeleta;
import com.tuempresa.papeletas.entidad.PapeletaItem;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoService {

    private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

    private final JavaMailSender mailSender;
    private final DestinatarioService destinatarioService;
    private final boolean habilitada;

    public CorreoService(JavaMailSender mailSender,
                          DestinatarioService destinatarioService,
                          @Value("${papeletas.notificacion.habilitada}") boolean habilitada) {
        this.mailSender = mailSender;
        this.destinatarioService = destinatarioService;
        this.habilitada = habilitada;
    }

    public void notificarRegistro(Papeleta papeleta) {
        List<String> destinatarios = destinatarioService.listarEmails();
        if (!habilitada || destinatarios.isEmpty()) {
            log.info("Notificacion de correo omitida (deshabilitada o sin destinatarios) para papeleta {}", papeleta.getNumero());
            return;
        }

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatarios.toArray(new String[0]));
        mensaje.setSubject("Nueva papeleta de salida registrada: N° " + papeleta.getNumero());
        mensaje.setText(construirCuerpo(papeleta));

        try {
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.error("No se pudo enviar la notificacion de correo para la papeleta {}", papeleta.getNumero(), e);
        }
    }

    private String construirCuerpo(Papeleta papeleta) {
        StringBuilder sb = new StringBuilder();
        sb.append("Se registro una nueva papeleta de salida de bienes.\n\n");
        sb.append("Numero: ").append(papeleta.getNumero()).append('\n');
        sb.append("Tipo de salida: ").append(papeleta.getTipoSalida()).append('\n');
        sb.append("Fecha de salida: ").append(papeleta.getFechaSalida()).append('\n');
        sb.append("Area que autoriza: ").append(papeleta.getAreaQueAutoriza()).append('\n');
        sb.append("Autorizado por: ").append(papeleta.getAutorizadoPor()).append('\n');
        sb.append("Destino del bien: ").append(papeleta.getDestinoBien()).append('\n');
        sb.append("Responsable/solicitante: ").append(papeleta.getResponsableSolicitante()).append('\n');
        sb.append("Traslado: ").append(papeleta.getTrasladoTipo()).append('\n');
        sb.append("\nBienes:\n");
        for (PapeletaItem item : papeleta.getItems()) {
            sb.append(" - ").append(item.getCantidad()).append(' ').append(item.getUnidadMedida())
                    .append(' ').append(item.getDescripcion());
            if (item.getSerie() != null && !item.getSerie().isBlank()) {
                sb.append(" (serie ").append(item.getSerie()).append(')');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
