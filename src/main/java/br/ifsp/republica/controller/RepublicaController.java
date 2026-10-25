package br.ifsp.republica.controller;

import br.ifsp.republica.dto.Contratos.*;
import br.ifsp.republica.dto.Respostas.*;
import br.ifsp.republica.service.RepublicaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
public class RepublicaController {
    private final RepublicaService service;
    public RepublicaController(RepublicaService service) { this.service=service; }
    private Long id(Jwt jwt) { return Long.valueOf(jwt.getSubject()); }
    @PostMapping("/republicas") public ResponseEntity<RepublicaDto> criar(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody NovaRepublica dto) {
        return ResponseEntity.created(URI.create("/republicas/minha")).body(RepublicaDto.from(service.criar(id(jwt),dto)));
    }
    @PostMapping("/republicas/entrar") public RepublicaDto entrar(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Convite dto) { return RepublicaDto.from(service.entrar(id(jwt),dto)); }
    @GetMapping("/republicas/minha") public RepublicaDto minha(@AuthenticationPrincipal Jwt jwt) { return RepublicaDto.from(service.republica(id(jwt))); }
    @GetMapping("/republicas/minha/moradores") public List<Usuario> moradores(@AuthenticationPrincipal Jwt jwt) { return service.membros(id(jwt)); }
    @PutMapping("/republicas/minha/administrador") public RepublicaDto transferir(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody Administracao dto) { return RepublicaDto.from(service.transferir(id(jwt),dto.moradorId())); }
    @DeleteMapping("/republicas/minha/moradores/{moradorId}") public ResponseEntity<Void> remover(@AuthenticationPrincipal Jwt jwt,@PathVariable Long moradorId) {
        service.sair(id(jwt),moradorId);return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/republicas/minha/sair") public ResponseEntity<Void> sair(@AuthenticationPrincipal Jwt jwt) {
        service.sair(id(jwt),id(jwt));return ResponseEntity.noContent().build();
    }
    @PostMapping("/despesas") public ResponseEntity<DespesaDto> lancar(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody NovaDespesa dto) {
        var d=service.lancar(id(jwt),dto);return ResponseEntity.created(URI.create("/despesas/"+d.id)).body(DespesaDto.from(d));
    }
    @GetMapping("/despesas") public List<DespesaDto> listar(@AuthenticationPrincipal Jwt jwt,@RequestParam(required=false) String mes) { return service.listar(id(jwt),mes).stream().map(DespesaDto::from).toList(); }
    @GetMapping("/despesas/{despesaId}") public DespesaDto buscar(@AuthenticationPrincipal Jwt jwt,@PathVariable Long despesaId) { return DespesaDto.from(service.buscar(id(jwt),despesaId)); }
    @PutMapping("/despesas/{despesaId}") public DespesaDto editar(@AuthenticationPrincipal Jwt jwt,@PathVariable Long despesaId,@Valid @RequestBody NovaDespesa dto) { return DespesaDto.from(service.editar(id(jwt),despesaId,dto)); }
    @DeleteMapping("/despesas/{despesaId}") public ResponseEntity<Void> excluir(@AuthenticationPrincipal Jwt jwt,@PathVariable Long despesaId) {
        service.excluir(id(jwt),despesaId);return ResponseEntity.noContent().build();
    }
    @PostMapping("/recorrencias") public ResponseEntity<RecorrenciaDto> modelo(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody NovaRecorrencia dto) {
        var r=service.modelo(id(jwt),dto);return ResponseEntity.created(URI.create("/recorrencias")).body(RecorrenciaDto.from(r));
    }
    @GetMapping("/recorrencias") public List<RecorrenciaDto> modelos(@AuthenticationPrincipal Jwt jwt) { return service.modelos(id(jwt)).stream().map(RecorrenciaDto::from).toList(); }
    @DeleteMapping("/recorrencias/{modeloId}") public ResponseEntity<Void> excluirModelo(@AuthenticationPrincipal Jwt jwt,@PathVariable Long modeloId) {
        service.excluirModelo(id(jwt),modeloId);return ResponseEntity.noContent().build();
    }
    @PostMapping("/recorrencias/gerar/{mes}") public List<DespesaDto> gerar(@AuthenticationPrincipal Jwt jwt,@PathVariable String mes) { return service.gerar(id(jwt),mes).stream().map(DespesaDto::from).toList(); }
    @PostMapping("/fechamentos/{mes}") public ResponseEntity<Void> fechar(@AuthenticationPrincipal Jwt jwt,@PathVariable String mes) {
        service.fechar(id(jwt),mes);return ResponseEntity.noContent().build();
    }
    @PostMapping("/pagamentos") public ResponseEntity<PagamentoDto> pagar(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody NovoPagamento dto) {
        var p=service.pagar(id(jwt),dto);return ResponseEntity.created(URI.create("/pagamentos")).body(PagamentoDto.from(p));
    }
    @GetMapping("/pagamentos") public List<PagamentoDto> pagamentos(@AuthenticationPrincipal Jwt jwt) { return service.pagamentos(id(jwt)).stream().map(PagamentoDto::from).toList(); }
    @PostMapping("/pagamentos/{pagamentoId}/confirmar") public PagamentoDto confirmar(@AuthenticationPrincipal Jwt jwt,@PathVariable Long pagamentoId) { return PagamentoDto.from(service.confirmar(id(jwt),pagamentoId)); }
    @GetMapping("/saldos/me") public Saldo saldo(@AuthenticationPrincipal Jwt jwt) { return service.saldo(id(jwt)); }
    @GetMapping("/saldos/simplificar") public List<Transferencia> simplificar(@AuthenticationPrincipal Jwt jwt) { return service.simplificar(id(jwt)); }
    @GetMapping("/relatorios/categorias") public Map<String,BigDecimal> categorias(@AuthenticationPrincipal Jwt jwt,@RequestParam String mes) { return service.categorias(id(jwt),mes); }
    @GetMapping("/relatorios/mensal") public Map<String,BigDecimal> mensal(@AuthenticationPrincipal Jwt jwt) { return service.mensal(id(jwt)); }
    @GetMapping("/relatorios/extrato") public List<Evento> extrato(@AuthenticationPrincipal Jwt jwt) { return service.extrato(id(jwt)); }
}
