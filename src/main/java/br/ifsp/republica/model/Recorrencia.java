package br.ifsp.republica.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Entity
public class Recorrencia {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long republicaId;
 public String descricao;
 public String categoria;
 @Column(precision=19,scale=2) public BigDecimal valor;
 public Long pagadorId;
 public int dia;
 public String divisao;
 @ElementCollection(fetch=FetchType.EAGER) @CollectionTable(name="recorrencia_participantes",joinColumns=@JoinColumn(name="recorrencia_id"))
 @MapKeyColumn(name="morador_id") @Column(name="peso",precision=19,scale=2)
 public Map<Long,BigDecimal> participantes=new LinkedHashMap<>();
}
