package br.ifsp.republica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long republicaId;

    private Long pagadorId;

    private Long recebedorId;

    @Column(precision = 19, scale = 2)
    private BigDecimal valor;

    private LocalDate data;

    private boolean confirmado;

    public Long getId() {
        return id;
    }

    public Long getRepublicaId() {
        return republicaId;
    }

    public void setRepublicaId(Long republicaId) {
        this.republicaId = republicaId;
    }

    public Long getPagadorId() {
        return pagadorId;
    }

    public void setPagadorId(Long pagadorId) {
        this.pagadorId = pagadorId;
    }

    public Long getRecebedorId() {
        return recebedorId;
    }

    public void setRecebedorId(Long recebedorId) {
        this.recebedorId = recebedorId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public void confirmar() {
        this.confirmado = true;
    }

    public boolean foiFeitoPor(Long moradorId) {
        return pagadorId.equals(moradorId);
    }

    public boolean foiRecebidoPor(Long moradorId) {
        return recebedorId.equals(moradorId);
    }

    public boolean envolve(Long moradorId) {
        return foiFeitoPor(moradorId) || foiRecebidoPor(moradorId);
    }
}
