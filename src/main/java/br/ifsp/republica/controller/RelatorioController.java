package br.ifsp.republica.controller;

import static br.ifsp.republica.controller.UsuarioAutenticado.id;

import br.ifsp.republica.dto.Contratos.Evento;
import br.ifsp.republica.service.RelatorioService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/relatorios/categorias")
    public Map<String, BigDecimal> categorias(@AuthenticationPrincipal Jwt jwt, @RequestParam String mes) {
        return relatorioService.categorias(id(jwt), mes);
    }

    @GetMapping("/relatorios/mensal")
    public Map<String, BigDecimal> mensal(@AuthenticationPrincipal Jwt jwt) {
        return relatorioService.mensal(id(jwt));
    }

    @GetMapping("/relatorios/extrato")
    public List<Evento> extrato(@AuthenticationPrincipal Jwt jwt) {
        return relatorioService.extrato(id(jwt));
    }
}
