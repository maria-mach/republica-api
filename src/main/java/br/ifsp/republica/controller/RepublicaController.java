package br.ifsp.republica.controller;

import static br.ifsp.republica.controller.UsuarioAutenticado.id;

import br.ifsp.republica.dto.Contratos.Administracao;
import br.ifsp.republica.dto.Contratos.Convite;
import br.ifsp.republica.dto.Contratos.NovaRepublica;
import br.ifsp.republica.dto.Contratos.Usuario;
import br.ifsp.republica.dto.Respostas.RepublicaDto;
import br.ifsp.republica.service.MembroService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RepublicaController {

    private final MembroService membroService;

    public RepublicaController(MembroService membroService) {
        this.membroService = membroService;
    }

    @PostMapping("/republicas")
    public ResponseEntity<RepublicaDto> criar(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NovaRepublica novaRepublica) {
        RepublicaDto republica = RepublicaDto.from(membroService.criarRepublica(id(jwt), novaRepublica));
        return ResponseEntity.created(URI.create("/republicas/minha")).body(republica);
    }

    @PostMapping("/republicas/entrar")
    public RepublicaDto entrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Convite convite) {
        return RepublicaDto.from(membroService.entrarNaRepublica(id(jwt), convite));
    }

    @GetMapping("/republicas/minha")
    public RepublicaDto minha(@AuthenticationPrincipal Jwt jwt) {
        return RepublicaDto.from(membroService.buscarRepublica(id(jwt)));
    }

    @GetMapping("/republicas/minha/moradores")
    public List<Usuario> moradores(@AuthenticationPrincipal Jwt jwt) {
        return membroService.listarMembros(id(jwt));
    }

    @PutMapping("/republicas/minha/administrador")
    public RepublicaDto transferir(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Administracao administracao) {
        return RepublicaDto.from(membroService.transferirAdministracao(id(jwt), administracao.moradorId()));
    }

    @DeleteMapping("/republicas/minha/moradores/{moradorId}")
    public ResponseEntity<Void> remover(@AuthenticationPrincipal Jwt jwt, @PathVariable Long moradorId) {
        membroService.sair(id(jwt), moradorId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/republicas/minha/sair")
    public ResponseEntity<Void> sair(@AuthenticationPrincipal Jwt jwt) {
        membroService.sair(id(jwt), id(jwt));
        return ResponseEntity.noContent().build();
    }
}
