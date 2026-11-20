package br.ifsp.republica.controller;

import static br.ifsp.republica.controller.UsuarioAutenticado.id;

import br.ifsp.republica.dto.Contratos.Saldo;
import br.ifsp.republica.dto.Contratos.Transferencia;
import br.ifsp.republica.service.SaldoService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaldoController {

    private final SaldoService saldoService;

    public SaldoController(SaldoService saldoService) {
        this.saldoService = saldoService;
    }

    @GetMapping("/saldos/me")
    public Saldo saldo(@AuthenticationPrincipal Jwt jwt) {
        return saldoService.saldo(id(jwt));
    }

    @GetMapping("/saldos/simplificar")
    public List<Transferencia> simplificar(@AuthenticationPrincipal Jwt jwt) {
        return saldoService.simplificar(id(jwt));
    }
}
