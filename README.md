# osAPI — ERP Marmoraria São Carlos (Backend)

API REST de um sistema ERP completo, desenvolvida em **Java 17 + Spring Boot**, cobrindo o
fluxo de ponta a ponta de uma marmoraria: clientes, fornecedores, produtos e estoque,
projetos personalizados (peças e recortes), orçamentos, vendas, ordens de serviço,
financeiro (contas a pagar/receber com parcelamento) e relatórios em PDF.

> Projeto originalmente acadêmico (TCC), **refatorado para nível profissional** com foco em
> Clean Code, SOLID, segurança e performance. Cada decisão de refatoração está documentada
> em [REFACTORING.md](REFACTORING.md) — diagnóstico, justificativa e critério de aceitação
> por fase.

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem / runtime | Java 17 |
| Framework | Spring Boot 2.6 (Web, Data JPA, Security, Validation, Mail, Actuator) |
| Persistência | PostgreSQL 16 · Hibernate · H2 (testes) |
| Segurança | JWT (auth0 java-jwt) · RBAC por rota e método HTTP · BCrypt |
| Documentação | springdoc-openapi (Swagger UI) |
| Relatórios | JasperReports |
| Build | Maven |

## Como rodar

Pré-requisitos: JDK 17, Docker e Maven (ou o wrapper da sua IDE).

```bash
# 1. Sobe o PostgreSQL local (banco "marmoraria", postgres/postgres)
docker compose up -d

# 2. Roda a aplicação (o perfil "dev" é assumido por padrão, com defaults locais)
mvn spring-boot:run

# 3. Confirma que está no ar
curl http://localhost:8080/actuator/health
```

- **Swagger UI:** http://localhost:8080/swagger-ui.html — use `POST /auth/login` e o botão
  **Authorize** (bearer JWT) para testar endpoints autenticados.
- **Testes:** `mvn test` — a suíte é auto-contida (H2 em memória), não precisa de Docker.

### Configuração

Toda configuração variável vem de **variáveis de ambiente** (12-factor). O contrato completo
está em [.env.example](.env.example). Em desenvolvimento nada é obrigatório (há defaults
locais funcionais); em produção (`SPRING_PROFILES_ACTIVE=prod`) a aplicação **falha no boot**
se faltar `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` ou `CORS_ALLOWED_ORIGINS` —
melhor quebrar no deploy do que subir com default inseguro.

## Arquitetura

```
controle/      Controllers REST (orquestram requisição/resposta, sem regra de negócio)
services/      Casos de uso e regras de negócio (ex.: VendaService, FaturamentoService)
mapper/        Conversão entidade ⇄ DTO (componentes Spring dedicados)
repository/    Spring Data JPA (queries com fetch join onde o caso de uso exige)
domain/        Entidades JPA com comportamento (ex.: Venda.calculaTotal())
dtos/          Contratos da API
security/      Filtro JWT, RBAC, CORS e política de sessão (STATELESS)
controle/exceptions/  Handler único de erros (contrato 404 / 409 / 400)
```

Princípios aplicados (com exemplos reais no [REFACTORING.md](REFACTORING.md)):

- **SRP** — serviços com um motivo para mudar: a regra de parcelamento vive no
  `FaturamentoService`, não no fluxo de venda.
- **DIP** — injeção por construtor com campos `final` em 100% dos componentes.
- **Open/Closed** — exceções de negócio estendem `RegraDeNegocioException` e já nascem
  mapeadas para HTTP 409 pelo handler único.
- **Fail Fast** — configuração ausente derruba o boot; token inválido lança exceção tipada
  com a causa real (nunca valor mágico).

## Decisões de refatoração e aprendizados

O projeto passou por uma refatoração faseada (fases 0–7, da menor para a maior superfície
de risco), cada fase fechando com build e testes verdes. Destaques:

| Decisão | Por quê |
|---|---|
| Teste de fumaça antes de qualquer mudança | `@SpringBootTest` + H2 detecta bean ausente, ciclo de DI, propriedade faltando e mapeamento JPA inválido — a rede de segurança mais barata que existe |
| Injeção por construtor (142 pontos) | Dependências explícitas na assinatura; ciclos de DI quebram no boot em vez de virar surpresa em produção |
| Exceções tipadas + handler único | Controle de fluxo por `getMessage().contains(...)` quebra silenciosamente; tipo é contrato, mensagem é apresentação |
| Mappers dedicados (à mão, sem MapStruct) | A maioria das conversões precisa de consultas a repositório ou cópias "leves" para controlar serialização — cenário em que o gerador atrapalha |
| Fetch joins honestos | `findByIdWithDetails` sem `JOIN FETCH` gerava N+1 mascarado por 40 linhas de "força inicialização"; a cura é a query certa, não o hack |
| CORS num único lugar | 20 `@CrossOrigin("*")` espalhados tornavam a política global decorativa; origem autorizada agora vem de configuração por ambiente |

A lista completa — incluindo o que **não** foi feito de propósito (consolidação
`VendaUnificadaService`, migração de DTOs restantes) e por quê — está no
[REFACTORING.md](REFACTORING.md) §status de cada fase.

## Roadmap

1. **Migração Spring Boot 2.6 → 3.x** (prioridade: framework EOL) — `javax.*` → `jakarta.*`,
   Hibernate 6, Security 6.
2. **Flyway** — versionamento de schema no lugar de `ddl-auto=update`.
3. **Generalização multi-segmento** — extrair o core ERP (pessoas, vendas, financeiro) dos
   módulos específicos de marmoraria, abrindo caminho para outros domínios (ex.: logística).
4. **Conformidade fiscal (NF-e)** como módulo isolado.
5. **Testes de integração** dos fluxos de negócio (venda completa, OS, faturamento).

## Frontend

A interface web (Angular 16 + PrimeNG) vive em
[front-end-marmoraria-sao-carlos](https://github.com/pimenteljoao44/front-end-marmoraria-sao-carlos).
