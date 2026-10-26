package br.ifsp.republica.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Contratos {
    private Contratos() {}
    public record Cadastro(@NotBlank @Size(max=120) String nome, @NotBlank @Email @Size(max=254) String email,
                           @NotBlank @Size(min=8,max=72) String senha) {}
    public record Login(@NotBlank String email, @NotBlank String senha) {}
    public record Usuario(Long id, String nome, String email, Long republicaId) {}
    public record Token(String token, String tipo, long expiraEmSegundos, Usuario usuario) {}
    public record NovaRepublica(@NotBlank @Size(max=120) String nome) {}
    public record Convite(@NotBlank String codigo) {}
    public record Administracao(@NotNull Long moradorId) {}
    public record Participante(@NotNull Long moradorId, @DecimalMin("0.00") @Digits(integer=17,fraction=2) BigDecimal parte) {}
    public record NovaDespesa(@NotBlank @Size(max=250) String descricao,
        @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal valor,
        @NotNull LocalDate data, @NotBlank @Size(max=100) String categoria,
        @NotNull Long pagadorId, @NotBlank @Pattern(regexp="IGUAL|PERCENTUAL|VALOR") String divisao,
        @NotEmpty List<@NotNull @Valid Participante> participantes) {}
    public record NovaRecorrencia(@NotNull @Valid NovaDespesa despesa, @Min(1) @Max(31) int dia) {}
    public record NovoPagamento(@NotNull Long recebedorId,
        @NotNull @DecimalMin("0.01") @Digits(integer=17,fraction=2) BigDecimal valor, @NotNull LocalDate data) {}
    public record Transferencia(Long de, Long para, BigDecimal valor) {}
    public record SaldoOutro(Long moradorId, BigDecimal saldo) {}
    public record Saldo(BigDecimal saldo, List<SaldoOutro> porMorador) {}
    public record Evento(LocalDate data, String tipo, Long referenciaId, BigDecimal valor) {}
}
