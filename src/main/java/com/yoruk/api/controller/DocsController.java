package com.yoruk.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DocsController {

    @GetMapping("/docs")
    public String docs() {
        return "redirect:/";
    }
}
