# Guia de estudo para a apresentação

Os dois integrantes precisam entender todas as partes. Alternem as respostas durante o ensaio.

## Demonstração de 5 a 8 minutos

1. Iniciar com Docker ou com o perfil `demo` e abrir o Swagger.
2. Entrar como Ana, consultar os moradores e criar uma despesa de R$ 100 dividida com Bia.
3. Consultar os saldos: Ana recebe R$ 50 e Bia paga R$ 50.
4. Entrar como Bia, registrar o pagamento e mostrar que o saldo ainda não mudou.
5. Entrar como Ana, confirmar o recebimento e mostrar os saldos zerados.
6. Criar um modelo recorrente, gerar um mês duas vezes e mostrar que não duplica.
7. Fechar o mês e tentar editar uma despesa dele: a regra impede a alteração.
8. Mostrar os testes, a cobertura JaCoCo e os diagramas.

Use os IDs retornados pela API. Se repetir a demonstração, escolha um mês ainda aberto e uma data de pagamento que não esteja no futuro.

## O que faz e por que foi feito assim?

| Parte | Explicação |
|---|---|
| Controller | Recebe a requisição HTTP, valida o DTO e chama o serviço. Não calcula saldos. |
| DTO | Define exatamente os dados da API. O DTO de usuário não tem senha nem hash. |
| Serviço | Orquestra consultas, regras, autorização e gravações. |
| Repositório | O Spring Data implementa consultas a partir da interface e dos nomes dos métodos. |
| Entidade | Representa um registro persistido. `@Id` identifica e `@GeneratedValue` gera o ID. |
| JWT | É assinado pelo servidor, identifica o morador no campo `sub` e expira em duas horas. |
| Spring Security | Valida assinatura e validade do token antes de permitir rotas protegidas. |
| BCrypt | Guarda um hash da senha. No login, verifica a senha contra o hash, sem descriptografar. |
| Administrador | É o ID registrado na república. O serviço compara esse ID com o usuário autenticado. |
| `@Transactional` | Despesa e parcelas são gravadas juntas. Uma exceção desfaz a operação. |
| Bloqueio por república | Serializa alterações financeiras para impedir uma saída concorrente com uma nova dívida. |
| `BigDecimal` | Evita os erros de representação de números decimais de `double` para dinheiro. |
| `@RestControllerAdvice` | Centraliza a transformação das exceções em respostas HTTP consistentes. |
| JaCoCo | Mede quais linhas e decisões foram executadas pelos testes; não prova sozinho que as regras estão corretas. |
| Docker | Compila em uma etapa e executa o JAR com Java 21 em outra. Compose configura porta, perfil e volume. |

## Como funciona o saldo?

Ana pagou R$ 100 e sua parcela é R$ 50: `100 - 50 = +50`.
Bia não pagou a despesa e sua parcela é R$ 50: `0 - 50 = -50`.
Depois que Ana confirma o pagamento de Bia, acrescentamos R$ 50 ao saldo de Bia e retiramos R$ 50 do de Ana. Os dois zeram.

O sistema calcula o saldo a partir do histórico; não existe uma coluna de saldo para digitar ou atualizar manualmente.

## Como alterar um comportamento?

- **Token durar uma hora:** mudar `7200` em `AuthService.login`, tanto na expiração quanto no DTO de resposta.
- **Senha ter pelo menos dez caracteres:** alterar `@Size(min=8)` no DTO `Cadastro` e ajustar os testes.
- **Adicionar categoria obrigatória controlada:** trocar o texto livre por uma enumeração no contrato e no modelo, adaptar os modelos recorrentes e testar valores inválidos.
- **Mudar o destino dos centavos:** alterar o final de `DivisaoService.dividir` e atualizar os testes que verificam o resíduo e a conservação do total.
- **Adicionar filtro de mês ao extrato:** receber o parâmetro no controller, filtrar no serviço e testar meses com e sem eventos.

## Percurso para estudar o código

Comecem por `Contratos`, `AuthController` e `RepublicaController`. Sigam uma chamada até o serviço e o repositório. Depois estudem `DivisaoService`, os métodos de saldo, pagamento e fechamento de `RepublicaService`, e `SecurityConfig`. Por último, leiam os testes para entender os casos permitidos e os proibidos.

Cada integrante deve conseguir explicar uma requisição inteira, da entrada HTTP até o banco e de volta ao DTO de resposta.
