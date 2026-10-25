package br.ifsp.republica.controller;

import br.ifsp.republica.dto.Contratos.*;
import br.ifsp.republica.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
public class AuthController {
    private final AuthService auth;
    private final RepublicaService service;
    public AuthController(AuthService auth,RepublicaService service) { this.auth=auth;this.service=service; }
    @PostMapping("/auth/cadastro") public ResponseEntity<Usuario> cadastrar(@Valid @RequestBody Cadastro dto) {
        return ResponseEntity.created(URI.create("/moradores/me")).body(auth.cadastrar(dto));
    }
    @PostMapping("/auth/login") public Token login(@Valid @RequestBody Login dto) { return auth.login(dto); }
    @GetMapping("/moradores/me") public Usuario eu(@AuthenticationPrincipal Jwt jwt) {
        return AuthService.usuario(service.morador(Long.valueOf(jwt.getSubject())));
    }
}
