package br.ifsp.republica.controller;

import static br.ifsp.republica.controller.UsuarioAutenticado.id;

import br.ifsp.republica.dto.Contratos.NovoPagamento;
import br.ifsp.republica.dto.Respostas.PagamentoDto;
import br.ifsp.republica.service.PagamentoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping("/pagamentos")
    public ResponseEntity<PagamentoDto> pagar(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NovoPagamento novoPagamento) {
        PagamentoDto pagamento = PagamentoDto.from(pagamentoService.pagar(id(jwt), novoPagamento));
        return ResponseEntity.created(URI.create("/pagamentos")).body(pagamento);
    }

    @GetMapping("/pagamentos")
    public List<PagamentoDto> listar(@AuthenticationPrincipal Jwt jwt) {
        return pagamentoService.listarPagamentos(id(jwt)).stream()
            .map(PagamentoDto::from)
            .toList();
    }

    @PostMapping("/pagamentos/{pagamentoId}/confirmar")
    public PagamentoDto confirmar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long pagamentoId) {
        return PagamentoDto.from(pagamentoService.confirmar(id(jwt), pagamentoId));
    }
}
