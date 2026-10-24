# Mini Banco Digital

[![CI](https://github.com/claudiosilva06/minibanco/actions/workflows/ci.yml/badge.svg)](https://github.com/claudiosilva06/minibanco/actions/workflows/ci.yml)

API REST de um banco digital simplificado, feita com Java e Spring Boot.
Permite criar contas, depositar, sacar, transferir e fazer Pix, consultar o extrato,
com validação de dados, controle de concorrência, limite diário de saídas,
tratamento de erros padronizado, documentação Swagger e testes automatizados.

## Tecnologias

- Java 21
- Spring Boot 4 (Spring Web, Spring Data JPA, Validation)
- H2 Database (banco em memória, padrão para desenvolvimento e testes)
- PostgreSQL (via Docker Compose)
- springdoc-openapi (Swagger UI)
- JUnit 5, AssertJ e MockMvc
- Maven, Docker e GitHub Actions

## Funcionalidades

| Método | Rota | O que faz | Resposta de sucesso |
|---|---|---|---|
| POST | `/contas` | Cria uma conta com saldo zero | 201 Created |
| GET | `/contas/{id}` | Busca uma conta pelo id | 200 OK |
| POST | `/contas/{id}/deposito` | Deposita um valor na conta | 200 OK |
| POST | `/contas/{id}/saque` | Saca um valor da conta | 200 OK |
| GET | `/contas/{id}/extrato` | Extrato paginado, do mais recente para o mais antigo | 200 OK |
| POST | `/transferencias` | Transfere um valor entre duas contas | 204 No Content |
| POST | `/contas/{id}/chaves-pix` | Cadastra uma chave Pix na conta | 201 Created |
| GET | `/contas/{id}/chaves-pix` | Lista as chaves Pix da conta | 200 OK |
| DELETE | `/contas/{id}/chaves-pix/{chave}` | Remove uma chave Pix da conta | 204 No Content |
| GET | `/pix/chaves/{chave}` | Mostra o nome do recebedor antes de pagar | 200 OK |
| POST | `/pix` | Faz um Pix para uma chave e devolve o comprovante | 200 OK |

A documentação interativa, onde dá para testar todas as rotas, fica em
`http://localhost:8080/minibancoclaudioassis` com a API rodando.

### Exemplos de requisição

Criar conta:
```json
POST /contas
{ "titular": "Maria Souza" }
```

Depósito ou saque:
```json
POST /contas/1/deposito
{ "valor": 150.00 }
```

Transferência:
```json
POST /transferencias
{ "origemId": 1, "destinoId": 2, "valor": 50.00 }
```

Extrato (parâmetros opcionais `page`, `size` e `sort`):
```
GET /contas/1/extrato?page=0&size=10
```
```json
{
  "content": [
    { "id": 3, "tipo": "PIX_ENVIADO", "valor": 120.50, "dataHora": "2026-10-24T15:56:46.966151" },
    { "id": 1, "tipo": "DEPOSITO", "valor": 500.00, "dataHora": "2026-10-24T15:56:46.684223" }
  ],
  "page": { "size": 10, "number": 0, "totalElements": 2, "totalPages": 1 }
}
```

Cadastrar chave Pix (tipos: `CPF`, `EMAIL`, `TELEFONE` e `ALEATORIA`):
```json
POST /contas/2/chaves-pix
{ "tipo": "EMAIL", "chave": "joao@email.com" }
```
Para `ALEATORIA`, não envie `chave`: o banco gera um UUID.

Fazer um Pix:
```json
POST /pix
{ "origemId": 1, "chave": "joao@email.com", "valor": 120.50 }
```
```json
{ "chave": "joao@email.com", "recebedor": "João", "valor": 120.50, "dataHora": "2026-10-24T15:56:46.96615" }
```

### Regras de negócio

- Valores precisam ser maiores que zero e ter no máximo 2 casas decimais.
- **Limite diário de saídas:** saques, transferências enviadas e Pix enviados somam
  no máximo R$ 2.000,00 por conta por dia. O valor é configurável em
  `minibanco.limite-diario-saida`.
- **Chaves Pix:** até 5 por conta, e cada chave só pode existir uma vez no banco.
  O CPF é validado pelos dígitos verificadores, o telefone segue o formato `+55DDDNÚMERO`.
  A chave pode ser digitada com ou sem formatação (`529.982.247-25` ou `52998224725`).
- Não é possível transferir nem fazer Pix para a própria conta.

### Erros tratados

Todos os erros retornam um JSON no formato `{ "erro": "mensagem" }`.

| Situação | Código |
|---|---|
| Dados inválidos (titular vazio, valor zero, negativo ou com mais de 2 casas, chave Pix inválida, JSON malformado) | 400 Bad Request |
| Conta ou chave Pix não encontrada | 404 Not Found |
| Chave Pix já cadastrada | 409 Conflict |
| Saldo insuficiente, limite diário excedido ou limite de chaves Pix atingido | 422 Unprocessable Entity |

## Decisões técnicas

- **`BigDecimal` para valores monetários:** `double` gera erros de arredondamento
  (ex.: `0.1 + 0.2` dá `0.30000000000000004`), o que não é aceitável com dinheiro.
- **Transferência com `@Transactional`:** o saque da origem e o depósito no destino
  acontecem juntos. Se qualquer etapa falhar, nada é gravado, e o dinheiro nunca
  "some" nem aparece duplicado.
- **Controle de concorrência com lock pessimista:** antes de alterar o saldo, a conta
  é lida com `SELECT ... FOR UPDATE`. Sem isso, dez saques simultâneos de R$ 10 numa
  conta com R$ 50 eram todos aprovados (existe um teste que prova isso). Nas
  transferências, as duas contas são travadas sempre na ordem do id, o que evita
  deadlock quando A transfere para B e B transfere para A ao mesmo tempo.
- **Limite diário calculado a partir do extrato:** o total já usado no dia é a soma das
  movimentações de saída desde a meia-noite. Como a conta já está travada nesse momento,
  duas operações simultâneas não conseguem furar o limite.
- **Pix reaproveita a transferência:** o Pix localiza a conta pela chave e chama a mesma
  lógica transacional da transferência, mudando só o tipo das movimentações
  (`PIX_ENVIADO`/`PIX_RECEBIDO`).
- **Regras de negócio dentro da entidade `Conta`:** os métodos `depositar` e `sacar`
  validam o valor e o saldo, então nenhuma parte do sistema consegue deixar a conta
  em estado inválido.
- **DTOs de entrada e de saída:** os dados de entrada são `record`s validados com Bean
  Validation, e as respostas também são `record`s, para que as entidades JPA nunca
  sejam expostas diretamente. A consulta de chave Pix, por exemplo, mostra só o nome
  do recebedor, sem saldo nem id da conta.
- **Tratamento global de erros (`@RestControllerAdvice`):** as exceções viram
  respostas HTTP padronizadas em um único lugar.

## Testes

63 testes, rodados a cada push pelo GitHub Actions:

- **Unitários:** `ContaTest` (regras de saldo) e `TipoChavePixTest` (validação de CPF,
  e-mail, telefone e chave aleatória).
- **Integração dos serviços:** `TransferenciaServiceTest`, `ContaServiceTest` (extrato e
  paginação), `PixServiceTest` (cadastro, pagamento, remoção e limites de chaves) e
  `LimiteDiarioTest`.
- **Concorrência (`ConcorrenciaTest`):** saques simultâneos não deixam o saldo negativo, e
  transferências cruzadas simultâneas não travam nem perdem dinheiro.
- **API REST com MockMvc:** `ContaControllerTest`, `PixControllerTest` (códigos HTTP e
  formato das respostas) e `DocumentacaoApiTest` (especificação OpenAPI).

## Como executar

### Com Maven (banco H2 em memória)

Requisito: JDK 21.

```bash
git clone https://github.com/claudiosilva06/minibanco.git
cd minibanco
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080` e o Swagger em `http://localhost:8080/minibancoclaudioassis`.
No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

Para rodar os testes:
```bash
./mvnw test
```

### Com Docker (banco PostgreSQL)

Requisito: Docker com Docker Compose.

```bash
docker compose up --build
```

Isso sobe o PostgreSQL e a API com o perfil `postgres`. Os dados ficam salvos no volume
`dados-postgres` mesmo depois de parar os containers. Para apagar tudo:
`docker compose down -v`.

## Próximos passos

- Autenticação de usuários com Spring Security e JWT
- Migrações de banco com Flyway (hoje o esquema é gerado pelo Hibernate)
- Frontend em React

## Autor

Claudio Augusto Silva de Assis — estudante de Análise e Desenvolvimento de Sistemas na FIAP
[LinkedIn](https://www.linkedin.com/in/claudio-augusto-b7660a433)
