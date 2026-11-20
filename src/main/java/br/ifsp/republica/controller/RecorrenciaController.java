package br.ifsp.republica.controller;

import static br.ifsp.republica.controller.UsuarioAutenticado.id;

import br.ifsp.republica.dto.Contratos.NovaRecorrencia;
import br.ifsp.republica.dto.Respostas.DespesaDto;
import br.ifsp.republica.dto.Respostas.RecorrenciaDto;
import br.ifsp.republica.service.RecorrenciaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecorrenciaController {

    private final RecorrenciaService recorrenciaService;

    public RecorrenciaController(RecorrenciaService recorrenciaService) {
        this.recorrenciaService = recorrenciaService;
    }

    @PostMapping("/recorrencias")
    public ResponseEntity<RecorrenciaDto> criarModelo(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NovaRecorrencia novaRecorrencia) {
        RecorrenciaDto modelo = RecorrenciaDto.from(recorrenciaService.criarModelo(id(jwt), novaRecorrencia));
        return ResponseEntity.created(URI.create("/recorrencias")).body(modelo);
    }

    @GetMapping("/recorrencias")
    public List<RecorrenciaDto> listarModelos(@AuthenticationPrincipal Jwt jwt) {
        return recorrenciaService.listarModelos(id(jwt)).stream()
            .map(RecorrenciaDto::from)
            .toList();
    }

    @DeleteMapping("/recorrencias/{modeloId}")
    public ResponseEntity<Void> excluirModelo(@AuthenticationPrincipal Jwt jwt, @PathVariable Long modeloId) {
        recorrenciaService.excluirModelo(id(jwt), modeloId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recorrencias/gerar/{mes}")
    public List<DespesaDto> gerar(@AuthenticationPrincipal Jwt jwt, @PathVariable String mes) {
        return recorrenciaService.gerar(id(jwt), mes).stream()
            .map(DespesaDto::from)
            .toList();
    }
}
