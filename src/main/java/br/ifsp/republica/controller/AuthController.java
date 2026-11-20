package br.ifsp.republica.controller;

import static br.ifsp.republica.controller.UsuarioAutenticado.id;

import br.ifsp.republica.dto.Contratos.Cadastro;
import br.ifsp.republica.dto.Contratos.Login;
import br.ifsp.republica.dto.Contratos.Token;
import br.ifsp.republica.dto.Contratos.Usuario;
import br.ifsp.republica.model.Morador;
import br.ifsp.republica.service.AuthService;
import br.ifsp.republica.service.MembroService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService authService;
    private final MembroService membroService;

    public AuthController(AuthService authService, MembroService membroService) {
        this.authService = authService;
        this.membroService = membroService;
    }

    @PostMapping("/auth/cadastro")
    public ResponseEntity<Usuario> cadastrar(@Valid @RequestBody Cadastro cadastro) {
        return ResponseEntity.created(URI.create("/moradores/me")).body(authService.cadastrar(cadastro));
    }

    @PostMapping("/auth/login")
    public Token login(@Valid @RequestBody Login credenciais) {
        return authService.login(credenciais);
    }

    @GetMapping("/moradores/me")
    public Usuario eu(@AuthenticationPrincipal Jwt jwt) {
        Morador morador = membroService.buscarMorador(id(jwt));
        return Usuario.from(morador);
    }
}
