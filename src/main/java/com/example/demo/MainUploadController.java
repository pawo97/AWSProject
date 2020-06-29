package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainUploadController {

    @GetMapping("/mainPage")
    public String getUploadMainPage() {
        return "main";
    }
}
