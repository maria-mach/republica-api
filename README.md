# API de Despesas da República — Tema 10

Projeto da disciplina de APIs e Microsserviços. É uma API REST simples, sem interface gráfica, com Java 21, Spring Boot, Spring Security/JWT, JPA e banco HSQLDB em arquivo. A documentação Swagger permite usar a API pelo navegador. Cada usuário pertence a no máximo uma república.

## Rodar com Docker

Requer Docker com suporte a Compose. Dentro desta pasta:

```sh
docker compose up --build
```

Abra http://localhost:8080/swagger-ui/index.html. O Docker compila o projeto, executa os testes e inicia a aplicação com os usuários de demonstração. Os dados ficam no volume `dados`. Para encerrar, use `docker compose down`; os dados são preservados.

A chave JWT de demonstração está no Compose. Para trocar, defina `JWT_SECRET` com pelo menos 32 bytes no ambiente ou em um arquivo `.env`. Não use a chave nem as contas de demonstração em um sistema público.

## Rodar sem Docker

Requer JDK 21. O Maven Wrapper baixa o Maven e as dependências na primeira execução.

Windows (PowerShell):

```powershell
./mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=demo"
```

Linux/macOS:

```sh
sh mvnw spring-boot:run -Dspring-boot.run.profiles=demo
```

Se o Java não for encontrado no Windows, ajuste `JAVA_HOME` para a pasta do JDK 21. A API usa a porta 8080 e cria o banco em `data/`. Para iniciar sem os usuários de demonstração, omita a opção de perfil `demo`.

## Usuários de teste

Criados automaticamente pelo perfil `demo` no primeiro início:

| Nome | E-mail | Senha | Função inicial |
|---|---|---|---|
| Ana | ana@teste.com | senha12345 | Administradora |
| Bia | bia@teste.com | senha12345 | Moradora |
| Caio | caio@teste.com | senha12345 | Morador |

Todos pertencem à República IFSP e começam sem despesas. As senhas são armazenadas com BCrypt. Se Ana já existir, a inicialização não altera os dados existentes. Os IDs não são fixos: consulte o retorno do login e `GET /republicas/minha/moradores`.

No Swagger:

1. Execute `POST /auth/login` com um e-mail acima e a senha `senha12345`.
2. Copie o campo `token` da resposta.
3. Clique em **Authorize**, cole apenas o token e confirme.
4. Execute as demais rotas. Para trocar de usuário, troque o token em **Authorize**.

O token dura duas horas. Fora do Docker, se `JWT_SECRET` não estiver definida, a chave é temporária: reiniciar a aplicação invalida os tokens anteriores.

## Exemplo para demonstrar

Consulte os IDs dos moradores e substitua os números abaixo. Autenticada como Ana, crie uma despesa:

```json
{
  "descricao": "Compra do mercado",
  "valor": 100.00,
  "data": "2026-10-25",
  "categoria": "Alimentação",
  "pagadorId": 1,
  "divisao": "IGUAL",
  "participantes": [{"moradorId": 1}, {"moradorId": 2}]
}
```

Ana fica com R$ 50 a receber e Bia com R$ 50 a pagar. Como Bia, registre em `POST /pagamentos`:

```json
{"recebedorId": 1, "valor": 50.00, "data": "2026-10-25"}
```

O saldo permanece igual até Ana executar `POST /pagamentos/{id}/confirmar`. Depois ambos ficam com saldo zero. Ana pode fechar o mês em `POST /fechamentos/2026-10`; editar ou excluir a despesa passa a retornar erro.

Para testar divisão por percentual, use `"divisao": "PERCENTUAL"` e participantes com `parte` somando 100. Para divisão por valor, use `"divisao": "VALOR"` e `parte` somando o valor da despesa.

Para recorrência, envie a `POST /recorrencias` um objeto `{"dia": 10, "despesa": ...}`, substituindo `...` pelo objeto completo de uma despesa. A administradora gera os modelos com `POST /recorrencias/gerar/2026-11`. Repetir a geração não duplica as despesas daquele modelo/mês.

Há uma coleção em `postman/Republica.postman_collection.json`, que cadastra usuários próprios e salva tokens, IDs e convite. Execute em ordem e use um `sufixo` novo a cada execução. A demonstração do Swagger acima usa as contas do perfil `demo`.

## Endpoints

Cadastro, login e documentação são públicos. As outras rotas exigem `Authorization: Bearer TOKEN`. A república é obtida pelo usuário autenticado.

| RF | Método | Caminho | Função / corpo |
|---|---|---|---|
| 01 | POST | `/auth/cadastro` | `nome`, `email`, `senha` (mínimo 8 caracteres) |
| 02 | POST | `/auth/login` | `email`, `senha`; devolve JWT |
| — | GET | `/moradores/me` | Dados próprios sem senha |
| 03 | POST | `/republicas` | `nome`; criador vira administrador |
| 04 | POST | `/republicas/entrar` | `codigo` de convite |
| — | GET | `/republicas/minha` | República e código de convite |
| — | GET | `/republicas/minha/moradores` | Moradores da república |
| 05 | DELETE | `/republicas/minha/sair` | Sair com saldo zero |
| 05 | DELETE | `/republicas/minha/moradores/{id}` | Administrador remove morador com saldo zero |
| 05 | PUT | `/republicas/minha/administrador` | `moradorId` do novo administrador |
| 06 | POST | `/despesas` | Lançar despesa e parcelas |
| — | GET | `/despesas?mes=2026-10` | Listar; filtro de mês opcional |
| — | GET | `/despesas/{id}` | Consultar despesa |
| 07 | POST | `/recorrencias` | Administrador cadastra `dia` e objeto `despesa` |
| — | GET | `/recorrencias` | Listar modelos |
| — | DELETE | `/recorrencias/{id}` | Administrador exclui modelo |
| 07 | POST | `/recorrencias/gerar/{mes}` | Gerar os modelos uma vez no mês |
| 08 | PUT | `/despesas/{id}` | Substituir dados; autor ou administrador |
| 08 | DELETE | `/despesas/{id}` | Excluir; autor ou administrador |
| 09 | POST | `/fechamentos/{mes}` | Administrador fecha o mês |
| 10 | GET | `/saldos/me` | Saldo próprio e saldo por outro morador |
| 11 | POST | `/pagamentos` | `recebedorId`, `valor`, `data`; pagador é quem está autenticado |
| — | GET | `/pagamentos` | Listar pagamentos da república |
| 11 | POST | `/pagamentos/{id}/confirmar` | Somente quem recebeu confirma |
| 12 | GET | `/saldos/simplificar` | Sugestões de transferências |
| 13 | GET | `/relatorios/categorias?mes=2026-10` | Gastos por categoria no mês |
| 14 | GET | `/relatorios/extrato` | Despesas, parcelas e pagamentos próprios por data |
| 15 | GET | `/relatorios/mensal` | Gastos totais mês a mês |

O mês usa o formato `AAAA-MM`; datas usam `AAAA-MM-DD`. Criações de recursos retornam 201; exclusões e fechamento, 204; demais operações bem-sucedidas, 200. Erros: 400 (entrada/regra), 401 (autenticação), 403 (permissão), 404 (registro inexistente ou fora da república), 409 (duplicação/conflito). Erros de negócio e validação retornam `timestamp`, `status`, `message`, `path` e `fields`. Falhas de autenticação são tratadas pelo Spring Security.

Swagger: `/swagger-ui/index.html`. Contrato OpenAPI: `/v3/api-docs`.

## Regras principais

- Saldo positivo significa receber; negativo significa pagar. Fórmula: despesas pagas − próprias parcelas + pagamentos enviados confirmados − pagamentos recebidos confirmados.
- Despesa é o gasto; parcelas são as responsabilidades. Pagamentos são acertos e não entram nos relatórios de gastos.
- Dinheiro usa `BigDecimal` com até duas casas decimais. Na divisão igual, o resíduo fica com o pagador. Em percentuais, os valores são truncados e o resíduo também fica com o pagador. Ele pode pagar pelos outros sem participar da divisão; assume apenas o eventual resíduo.
- Somente quem recebeu confirma o pagamento; confirmar novamente não duplica seu efeito. Pendências aparecem no extrato, mas não afetam o saldo.
- A simplificação combina devedores e credores até zerar os saldos, com no máximo N − 1 transferências para N moradores com saldo diferente de zero. A sugestão não registra pagamentos automaticamente.
- O fechamento bloqueia criação, edição e exclusão de despesas no mês. Pagamentos continuam permitidos para quitar contas.
- A geração recorrente não duplica modelo/mês e usa o último dia do mês quando o dia cadastrado não existir. Se um participante saiu, o lote é desfeito; exclua o modelo e cadastre outro.
- Para sair ou ser removido, é necessário saldo zero e ausência de pagamentos pendentes. O administrador transfere a função antes de sair. O histórico é preservado; despesas envolvendo ex-moradores não podem ser editadas/excluídas para recriar dívidas após a saída.
- Alterações de despesas e parcelas são transacionais. Um bloqueio por república evita alterações financeiras simultâneas conflitantes; o banco também impede duplicação de modelo/mês.

## Testes e cobertura JaCoCo

No Windows:

```powershell
./mvnw.cmd clean verify
```

No Linux/macOS:

```sh
sh mvnw clean verify
```

O comando executa os testes unitários e funcionais, gera o JAR em `target/republica-api-1.0.0.jar` e o relatório de cobertura em **`target/site/jacoco/index.html`**. Abra esse HTML no navegador. Relatórios dos testes ficam em `target/surefire-reports/`. Não há porcentagem mínima de cobertura imposta, pois o enunciado exige a configuração do JaCoCo e não define uma meta.

- `DivisaoServiceTest`: regras de divisão, centavos e somas inválidas.
- `RepublicaApiTest`: testes funcionais HTTP com MockMvc, Spring Security e banco H2 em memória, cobrindo autenticação, isolamento, permissões, pagamentos, recorrências, fechamento, relatórios e rollback.
- `DadosDemonstracaoTest`: contas de demonstração, login, vínculo com a república e inicialização sem duplicação.

Os testes não usam o banco persistente da aplicação. O Mockito é carregado como agente de teste pelo Maven, evitando a anexação dinâmica de agentes no Java 21. O agente JaCoCo permanece habilitado para medir a cobertura.

## Organização e diagramas

```text
src/main/java/br/ifsp/republica/
  config/       JWT, Swagger e dados de demonstração
  controller/   recebe HTTP e devolve DTOs
  dto/          contratos de entrada e saída
  service/      regras de negócio e transações
  repository/   consultas JPA
  model/        entidades persistidas
  exception/    exceções e tratamento centralizado
```

O domínio usa seis entidades. As parcelas ficam no mapa `Despesa.parcelas`, persistido na tabela `despesa_parcelas`; por isso não existe uma classe `Parcela` separada. Os relacionamentos guardam IDs e são validados pelo serviço.

- [Diagrama de casos de uso](docs/casos-de-uso.svg)
- [Diagrama de classes](docs/classes.svg)
- [Fontes editáveis dos diagramas](docs/diagramas.md)
- [Guia para apresentação](docs/apresentacao.md)

## Checklist da entrega

RF01–RF15 implementados; JWT/Spring Security; testes unitários e funcionais; JaCoCo; Swagger; Dockerfile e docker-compose; diagramas de casos de uso e classes; README com execução, contas e endpoints.

Entregue esta pasta compactada ou o link do repositório. `target/` e `data/` não precisam entrar no ZIP; testes e relatório são reproduzidos com `clean verify`. Os dois integrantes devem estudar o código e responder alternadamente na apresentação.

