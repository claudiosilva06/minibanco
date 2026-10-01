# Mini Banco Digital

API REST de um banco digital simplificado, feita com Java e Spring Boot.
Permite criar contas, depositar, sacar e transferir valores entre contas,
com validação de dados, tratamento de erros padronizado e testes automatizados.

## Tecnologias

- Java 21
- Spring Boot 4 (Spring Web, Spring Data JPA, Validation)
- H2 Database (banco em memória)
- JUnit 5
- Maven

## Funcionalidades

| Método | Rota | O que faz | Resposta de sucesso |
|---|---|---|---|
| POST | `/contas` | Cria uma conta com saldo zero | 201 Created |
| GET | `/contas/{id}` | Busca uma conta pelo id | 200 OK |
| POST | `/contas/{id}/deposito` | Deposita um valor na conta | 200 OK |
| POST | `/contas/{id}/saque` | Saca um valor da conta | 200 OK |
| POST | `/transferencias` | Transfere um valor entre duas contas | 204 No Content |

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

### Erros tratados

Todos os erros retornam um JSON no formato `{ "erro": "mensagem" }`.

| Situação | Código |
|---|---|
| Dados inválidos (titular vazio, valor zero ou negativo) | 400 Bad Request |
| Conta não encontrada | 404 Not Found |
| Saldo insuficiente | 422 Unprocessable Entity |

## Decisões técnicas

- **`BigDecimal` para valores monetários:** `double` gera erros de arredondamento
  (ex.: `0.1 + 0.2` dá `0.30000000000000004`), o que não é aceitável com dinheiro.
- **Transferência com `@Transactional`:** o saque da origem e o depósito no destino
  acontecem juntos. Se qualquer etapa falhar, nada é gravado, e o dinheiro nunca
  "some" nem aparece duplicado.
- **Regras de negócio dentro da entidade `Conta`:** os métodos `depositar` e `sacar`
  validam o valor e o saldo, então nenhuma parte do sistema consegue deixar a conta
  em estado inválido.
- **Tratamento global de erros (`@RestControllerAdvice`):** as exceções viram
  respostas HTTP padronizadas em um único lugar.
- **DTOs com `record` e Bean Validation:** os dados de entrada são validados antes
  de chegar à regra de negócio.

## Testes

- **Unitários (`ContaTest`):** saldo inicial, depósito, saque, saque acima do saldo
  e depósito negativo.
- **Integração (`TransferenciaServiceTest`):** transferência com sucesso, garantia de
  que nenhuma conta muda quando o saldo é insuficiente, e conta de destino inexistente.

## Como executar

Requisito: JDK 21.

```bash
git clone https://github.com/claudiosilva06/minibanco.git
cd minibanco
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

Para rodar os testes:
```bash
./mvnw test
```

## Próximos passos

- Extrato com o histórico de movimentações da conta
- Controle de concorrência em transferências simultâneas
- Autenticação de usuários
- Banco PostgreSQL com Docker
- Frontend em React

## Autor

Claudio Augusto Silva de Assis — estudante de Análise e Desenvolvimento de Sistemas na FIAP
[LinkedIn](https://www.linkedin.com/in/claudio-augusto-b7660a433)