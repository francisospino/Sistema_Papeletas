package com.tuempresa.papeletas.servicio;

import com.tuempresa.papeletas.entidad.Destinatario;
import com.tuempresa.papeletas.repositorio.DestinatarioRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DestinatarioService {

    private static final Logger log = LoggerFactory.getLogger(DestinatarioService.class);
    private static final Pattern PATRON_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final DestinatarioRepository destinatarioRepository;
    private final String destinatariosSemilla;

    public DestinatarioService(DestinatarioRepository destinatarioRepository,
                                @Value("${papeletas.notificacion.destinatarios:}") String destinatariosSemilla) {
        this.destinatarioRepository = destinatarioRepository;
        this.destinatariosSemilla = destinatariosSemilla;
    }

    /**
     * Si la tabla esta vacia y se configuro NOTIFICACION_DESTINATARIOS al arrancar,
     * los importa una sola vez para que queden administrables desde la pantalla de
     * Configuracion (compatibilidad con instalaciones previas a esta pantalla).
     */
    @PostConstruct
    @Transactional
    public void sembrarDesdeVariableDeEntorno() {
        if (destinatarioRepository.count() > 0 || destinatariosSemilla == null || destinatariosSemilla.isBlank()) {
            return;
        }
        for (String email : destinatariosSemilla.split(",")) {
            try {
                agregar(email);
            } catch (IllegalArgumentException e) {
                log.warn("No se pudo importar el destinatario inicial '{}': {}", email, e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Destinatario> listar() {
        return destinatarioRepository.findAllByOrderByEmailAsc();
    }

    @Transactional(readOnly = true)
    public List<String> listarEmails() {
        return destinatarioRepository.findAllByOrderByEmailAsc().stream()
                .map(Destinatario::getEmail)
                .toList();
    }

    @Transactional
    public Destinatario agregar(String email) {
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase();
        if (!PATRON_EMAIL.matcher(emailNormalizado).matches()) {
            throw new IllegalArgumentException("El correo '" + email + "' no tiene un formato valido");
        }
        if (destinatarioRepository.findByEmailIgnoreCase(emailNormalizado).isPresent()) {
            throw new IllegalArgumentException("Ese correo ya esta en la lista");
        }
        return destinatarioRepository.save(Destinatario.builder().email(emailNormalizado).build());
    }

    @Transactional
    public void eliminar(Long id) {
        destinatarioRepository.deleteById(id);
    }
}
