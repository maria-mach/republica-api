package br.ifsp.republica.controller;

import static br.ifsp.republica.controller.UsuarioAutenticado.id;

import br.ifsp.republica.dto.Contratos.NovaDespesa;
import br.ifsp.republica.dto.Respostas.DespesaDto;
import br.ifsp.republica.model.Despesa;
import br.ifsp.republica.service.DespesaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DespesaController {

    private final DespesaService despesaService;

    public DespesaController(DespesaService despesaService) {
        this.despesaService = despesaService;
    }

    @PostMapping("/despesas")
    public ResponseEntity<DespesaDto> lancar(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NovaDespesa novaDespesa) {
        Despesa despesa = despesaService.lancar(id(jwt), novaDespesa);
        return ResponseEntity.created(URI.create("/despesas/" + despesa.getId())).body(DespesaDto.from(despesa));
    }

    @GetMapping("/despesas")
    public List<DespesaDto> listar(
            @AuthenticationPrincipal Jwt jwt, @RequestParam(required = false) String mes) {
        return despesaService.listarDespesas(id(jwt), mes).stream()
            .map(DespesaDto::from)
            .toList();
    }

    @GetMapping("/despesas/{despesaId}")
    public DespesaDto buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long despesaId) {
        return DespesaDto.from(despesaService.buscarDespesa(id(jwt), despesaId));
    }

    @PutMapping("/despesas/{despesaId}")
    public DespesaDto editar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long despesaId,
            @Valid @RequestBody NovaDespesa novaDespesa) {
        return DespesaDto.from(despesaService.editar(id(jwt), despesaId, novaDespesa));
    }

    @DeleteMapping("/despesas/{despesaId}")
    public ResponseEntity<Void> excluir(@AuthenticationPrincipal Jwt jwt, @PathVariable Long despesaId) {
        despesaService.excluir(id(jwt), despesaId);
        return ResponseEntity.noContent().build();
    }
}
