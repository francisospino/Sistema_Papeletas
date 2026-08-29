package com.tuempresa.papeletas.controlador;

import com.tuempresa.papeletas.servicio.DestinatarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final DestinatarioService destinatarioService;

    public ConfiguracionController(DestinatarioService destinatarioService) {
        this.destinatarioService = destinatarioService;
    }

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("destinatarios", destinatarioService.listar());
        return "configuracion";
    }

    @PostMapping("/destinatarios")
    public String agregar(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            destinatarioService.agregar(email);
            redirectAttributes.addFlashAttribute("mensaje", "Correo agregado correctamente.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/configuracion";
    }

    @PostMapping("/destinatarios/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        destinatarioService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Correo eliminado.");
        return "redirect:/configuracion";
    }
}
