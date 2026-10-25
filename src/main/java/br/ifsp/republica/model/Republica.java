package br.ifsp.republica.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
@Entity
public class Republica {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String nome;
 @Column(nullable=false,unique=true) public String codigoConvite;
 @Column(nullable=false) public Long administradorId;
}
