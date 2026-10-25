# Diagramas do projeto

As imagens SVG podem ser abertas no navegador. As associações do diagrama de classes representam os IDs usados no código, e não relacionamentos JPA com `@ManyToOne`. Parcela é um valor no mapa de `Despesa`, não uma entidade separada.

## Casos de uso

![Casos de uso](casos-de-uso.svg)

Administrador também é morador e pode executar seus casos de uso. Editar/excluir despesa exige ser autor ou administrador. Sair/remover exige saldo zero. Confirmar pagamento exige ser o recebedor. Todos os dados ficam restritos à própria república.

Fonte PlantUML editável:

```plantuml
@startuml
left to right direction
actor Visitante
actor Morador
actor Administrador
Administrador --|> Morador
rectangle "API de Despesas da República" {
  usecase "Cadastrar-se (RF01)" as UC01
  usecase "Entrar com JWT (RF02)" as UC02
  usecase "Criar república (RF03)" as UC03
  usecase "Entrar por convite (RF04)" as UC04
  usecase "Sair com saldo zero (RF05)" as UC05
  usecase "Remover morador / transferir administração (RF05)" as UC05A
  usecase "Lançar e dividir despesa (RF06)" as UC06
  usecase "Cadastrar e gerar recorrências (RF07)" as UC07
  usecase "Editar / excluir despesa (RF08)" as UC08
  usecase "Fechar mês (RF09)" as UC09
  usecase "Consultar saldo (RF10)" as UC10
  usecase "Registrar / confirmar pagamento (RF11)" as UC11
  usecase "Simplificar dívidas (RF12)" as UC12
  usecase "Consultar relatórios (RF13–15)" as UC13
}
Visitante -- UC01
Visitante -- UC02
Morador -- UC03
Morador -- UC04
Morador -- UC05
Morador -- UC06
Morador -- UC08
Morador -- UC10
Morador -- UC11
Morador -- UC12
Morador -- UC13
Administrador -- UC05A
Administrador -- UC07
Administrador -- UC09
@enduml
```

## Classes de domínio

![Classes](classes.svg)

Fonte Mermaid editável (renderizada no GitHub):

```mermaid
classDiagram
class Morador {
  +Long id
  +String nome
  +String email
  +String senhaHash
  +Long republicaId
}
class Republica {
  +Long id
  +String nome
  +String codigoConvite
  +Long administradorId
}
class Despesa {
  +Long id
  +Long republicaId
  +Long autorId
  +Long pagadorId
  +String descricao
  +String categoria
  +BigDecimal valor
  +LocalDate data
  +String divisao
  +Long modeloId
  +String competencia
  +Map~Long,BigDecimal~ parcelas
}
class Recorrencia {
  +Long id
  +Long republicaId
  +String descricao
  +String categoria
  +BigDecimal valor
  +Long pagadorId
  +int dia
  +String divisao
  +Map~Long,BigDecimal~ participantes
}
class Pagamento {
  +Long id
  +Long republicaId
  +Long pagadorId
  +Long recebedorId
  +BigDecimal valor
  +LocalDate data
  +boolean confirmado
}
class Fechamento {
  +Long id
  +Long republicaId
  +String mes
}
Republica "0..1" -- "0..*" Morador : membros
Republica "0..1" --> "1" Morador : administrador
Republica "1" -- "0..*" Despesa : despesas
Republica "1" -- "0..*" Recorrencia : modelos
Republica "1" -- "0..*" Pagamento : pagamentos
Republica "1" -- "0..*" Fechamento : meses fechados
Recorrencia "0..1" --> "0..*" Despesa : gera
Despesa "0..*" --> "1" Morador : autor e pagador
Despesa "0..*" --> "1..*" Morador : parcelas
Pagamento "0..*" --> "1" Morador : pagador
Pagamento "0..*" --> "1" Morador : recebedor
```

Os modelos não possuem métodos de negócio; as regras ficam nos serviços. Um modelo recorrente excluído pode ter seu ID mantido no histórico da despesa gerada.
