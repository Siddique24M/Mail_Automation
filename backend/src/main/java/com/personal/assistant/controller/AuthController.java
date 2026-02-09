package com.personal.assistant.controller;

import com.personal.assistant.service.GmailService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class AuthController {

    private final GmailService gmailService;

    public AuthController(GmailService gmailService) {
        this.gmailService = gmailService;
    }

    @GetMapping("/login/google")
    public void login(HttpServletResponse response) throws IOException {
        String url = gmailService.getAuthorizationUrl();
        response.sendRedirect(url);
    }

    @GetMapping("/login/oauth2/code/google")
    public void callback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        gmailService.exchangeCode(code);
        response.sendRedirect("http://localhost:5173/dashboard"); // Redirect to Frontend Dashboard
    }
}
