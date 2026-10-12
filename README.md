# osMarmoraria API

ERP para marmoraria sob encomenda (osAPI): clientes, fornecedores, produtos,
projetos de marcenaria/marmoraria, ordens de serviço, vendas (balcão e
projeto), compras e financeiro (contas a pagar/receber, parcelas). Backend
do TCC original; esta branch (`portfolio-refactor`) documenta uma rodada de
refatoração do código de produção, preservando o comportamento observável.

## Stack

- **Java 17** · **Spring Boot 3.2.5** (Jakarta EE, Spring Security 6)
- **PostgreSQL** (único SGBD em todos os ambientes — ver `docker-compose.yml`)
- **Spring Data JPA / Hibernate**, **Lombok**, **JWT** (`java-jwt`, Auth0)
- **springdoc-openapi** (Swagger UI) + **Spring Boot Actuator**
- **JasperReports** (relatórios PDF)
- Testes: JUnit 5 + Spring Boot Test, **H2** em memória (perfil `test`)

## Como rodar localmente

Pré-requisitos: JDK 17, Maven (ou o wrapper `./mvnw`), Docker (para o banco).

```bash
# 1. Suba o PostgreSQL (porta 5432, banco "marmoraria", user/senha "postgres")
docker compose up -d

# 2. Configure o segredo do JWT (único obrigatório em dev; os demais têm default)
cp .env.example .env
#   edite .env e preencha JWT_SECRET (ex.: openssl rand -base64 48)

# 3. Suba a aplicação (perfil dev por default)
export $(grep -v '^#' .env | xargs)   # ou exporte as variáveis manualmente
./mvnw spring-boot:run

# 4. Health check
curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

Documentação navegável da API: `http://localhost:8080/swagger-ui.html`
(clique em **Authorize** e cole o token retornado por `POST /auth/login`
para testar endpoints autenticados direto pela UI).

Em **produção**, todas as variáveis de `.env.example` são obrigatórias —
a aplicação falha ao subir (fail-fast) se `JWT_SECRET`, `DB_URL` ou
`CORS_ALLOWED_ORIGINS` não estiverem definidas. Isso é intencional: um
segredo ou uma origem CORS ausente deve impedir o deploy, não cair
silenciosamente em um default inseguro.

### Rodando os testes

```bash
./mvnw test
```

A suíte sobe o contexto Spring completo contra H2 em memória (perfil
`test`/`h2`, ver `src/test/resources/`) — não depende do PostgreSQL local.

## Arquitetura — mapa de pacotes

| Pacote | Responsabilidade |
|---|---|
| `controle` | Controllers REST — tradução HTTP ↔ DTO, sem regra de negócio |
| `services` | Casos de uso e orquestração; uma classe por agregado de domínio |
| `mapper` | Montagem de DTOs a partir de entidades (extraído dos services) |
| `domain` | Entidades JPA |
| `dtos` | Contratos de entrada/saída da API |
| `repository` | Spring Data JPA — consultas, inclusive as com fetch join explícito |
| `security` | `SecurityConfig`, `SecurityFilter` (JWT), `AuthorizationService` |
| `config` | `OpenApiConfig`, `DevConfig`/`TestConfig` (inicialização de banco por perfil) |
| `services.exceptions` / `controle.exceptions` | Hierarquia de exceções de domínio + `ResourceExceptionHandler` único |
| `gateway` | Integração com gateway de pagamento |

## Princípios aplicados nesta refatoração

Cada decisão abaixo tem um commit próprio, validado com `mvn test` (build
verde) antes do próximo — histórico completo em `git log`, detalhamento
fase a fase em `REFACTORING.md`.

1. **Boot reprodutível a partir de um clone limpo** — perfis `dev`/`test`/
   `prod` explícitos, `.env.example` como contrato de configuração,
   `docker-compose.yml` alinhado ao datasource real.
2. **Injeção por construtor** no lugar de `@Autowired` em campo — torna
   toda dependência obrigatória visível desde a compilação (142 pontos,
   48 classes).
3. **Logging estruturado** (SLF4J/`@Slf4j`) no lugar de `System.out`/
   `printStackTrace` — nível, timestamp e logger de origem configuráveis.
4. **Exceções de domínio tipadas** (`RegraDeNegocioException` → HTTP 409)
   no lugar de `RuntimeException` genérica para conflitos de estado.
5. **Mappers dedicados** para a montagem de DTOs, separando "orquestrar o
   caso de uso" de "montar a representação de API" (SRP).
6. **Serviços coesos por responsabilidade** — `VendaService` (venda de
   produto de balcão) e `VendaProjetoService` (venda de projeto sob
   encomenda) separados: nunca compartilharam chamador, mas viviam na
   mesma classe.
7. **Performance JPA deliberada** — fetch joins reais (e não apenas no
   nome do método), batch de N+1 em listagens paginadas, fronteiras
   `@Transactional(readOnly = true)` em leituras.
8. **Hardening de segurança** — sessão `STATELESS`, CORS restrito por
   configuração (nunca `@CrossOrigin("*")`), falha explícita (não string
   vazia) na validação de JWT.
9. **Observabilidade e documentação** — Actuator (`/actuator/health`) e
   OpenAPI/Swagger UI com esquema bearer-JWT, dependências mortas
   (`flyway-mysql`, `mysql-connector-j` — o datasource sempre foi
   PostgreSQL) removidas do `pom.xml`.

## O que **não** foi feito de propósito

Transparência sobre pendências é parte da entrega, não um detalhe a
esconder:

- A maioria dos endpoints em `SecurityConfig` ainda é `permitAll()` — RBAC
  por role/recurso está marcado como `TODO` explícito no Javadoc da
  classe, não mascarado.
- `VendaProjetoService.buscarVendaProjetoPorId` infere o projeto da venda
  por `clienteId + status VENDIDO` (sem FK direta) — funciona enquanto um
  cliente não tiver duas vendas de projeto vendidas simultaneamente;
  documentado no Javadoc da classe.
- `VendaProjetoMapper.podeGerarOS`/`podeGerarContaReceber` são calculados
  contra os defaults do DTO, nunca populados a partir do estado real da
  venda — pendência de regra de negócio, não de refatoração.

## Decisões de refatoração (por quê, não só o quê)

| Decisão | Por quê |
|---|---|
| Injeção por construtor, não por campo | Torna testável sem container e obrigatória em compile-time |
| Mapper como `@Component`, não MapStruct | A maioria das conversões depende de consulta a repositório (fetch de itens) — um mapper gerado não eliminaria essa dependência, só esconderia o código equivalente |
| `VendaService` dividido por fluxo de chamador, não por regra de cálculo | O split certo depende do que a base real tem: aqui não há parcelamento mensal dentro do serviço (como em outras implementações do mesmo domínio) — há dois `Controller`s consumindo subconjuntos disjuntos de métodos |
| `RegraDeNegocioException` → 409, não 400/500 | Um conflito de estado (gerar o mesmo artefato duas vezes) não é um erro de validação de entrada (400) nem uma falha de infraestrutura (500) |

## Roadmap (fora do escopo desta refatoração)

- RBAC por role/recurso em `SecurityConfig` (hoje: `permitAll()` na maioria)
- FK direta entre `Venda` e `Projeto` (hoje: inferência por cliente+status)
- Módulo fiscal (NF-e) e financeiro completo (conciliação bancária)
- Testes de integração por fluxo de negócio (hoje: smoke test de contexto
  + testes unitários de `Usuario`)
