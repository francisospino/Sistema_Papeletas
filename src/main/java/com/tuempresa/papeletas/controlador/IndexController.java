package com.tuempresa.papeletas.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String raiz() {
        return "redirect:/papeletas";
    }
}
