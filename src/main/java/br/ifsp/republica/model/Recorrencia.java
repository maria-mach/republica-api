package br.ifsp.republica.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class Recorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long republicaId;

    private String descricao;

    private String categoria;

    @Column(precision = 19, scale = 2)
    private BigDecimal valor;

    private Long pagadorId;

    private int dia;

    private String divisao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recorrencia_participantes", joinColumns = @JoinColumn(name = "recorrencia_id"))
    @MapKeyColumn(name = "morador_id")
    @Column(name = "peso", precision = 19, scale = 2)
    private Map<Long, BigDecimal> participantes = new LinkedHashMap<>();

    public Long getId() {
        return id;
    }

    public Long getRepublicaId() {
        return republicaId;
    }

    public void setRepublicaId(Long republicaId) {
        this.republicaId = republicaId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Long getPagadorId() {
        return pagadorId;
    }

    public void setPagadorId(Long pagadorId) {
        this.pagadorId = pagadorId;
    }

    public int getDia() {
        return dia;
    }

    public void setDia(int dia) {
        this.dia = dia;
    }

    public String getDivisao() {
        return divisao;
    }

    public void setDivisao(String divisao) {
        this.divisao = divisao;
    }

    public Map<Long, BigDecimal> getParticipantes() {
        return Collections.unmodifiableMap(participantes);
    }

    public void adicionarParticipante(Long moradorId, BigDecimal parte) {
        participantes.put(moradorId, parte);
    }
}
