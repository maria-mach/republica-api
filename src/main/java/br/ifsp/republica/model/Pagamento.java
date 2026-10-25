package br.ifsp.republica.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Entity
public class Pagamento {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long republicaId;
 public Long pagadorId;
 public Long recebedorId;
 @Column(precision=19,scale=2) public BigDecimal valor;
 public LocalDate data;
 public boolean confirmado;
}
