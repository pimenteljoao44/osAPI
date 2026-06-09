# Plano de Refatoração — Backend (osAPI)

> ERP Marmoraria São Carlos · Java 17 · Spring Boot 2.6.4 · Branch: `refactor-backend`
> Objetivo: elevar um projeto didático a nível de portfólio profissional ("freelance-ready"),
> aplicando Clean Code, SOLID e boas práticas de segurança/performance — **com cada decisão
> explicada e justificada**, servindo também como material de estudo.

---

## 1. Diagnóstico (o que foi encontrado e por que importa)

A análise cobriu os 196 arquivos Java do projeto. Os achados estão ordenados por impacto.

### 1.1 O projeto não sobe a partir de um clone limpo 🔴 (impacto máximo)

| Evidência | Problema |
|-----------|----------|
| Só existe `application-dev.properties` | Não há configuração base (`application.properties`) nem perfil de produção; nada ativa o perfil `dev` automaticamente. |
| `TokenService` exige `api.security.token.secret` | A propriedade **não é definida em lugar nenhum do repositório** → a aplicação falha no boot. |
| `docker-compose.yml` sobe **MySQL** (root/root) | O datasource aponta para **PostgreSQL** em `localhost:5432`. A infraestrutura local contradiz a configuração. |
| `spring.jpa.hibernate.ddl-auto=update` | O schema é "evoluído" pelo Hibernate em runtime — sem versionamento, sem auditoria, arriscado fora de ambiente didático. |

**Por que é o item nº 1:** para um cliente de freelance (ou recrutador), o teste decisivo é
`git clone` → rodar. Hoje isso é impossível sem conhecimento tribal. *Reprodutibilidade é
pré-requisito de profissionalismo* — vem antes de qualquer elegância de código.

### 1.2 A suíte de testes existe, mas não roda num clone limpo 🔴

Há 5 classes de teste (~22 casos), porém a suíte estava **vermelha por configuração**:
o perfil `test` não definia `api.security.token.secret` nem `spring.mail.*` — as 8
integrações de pagamento falhavam com *placeholder não resolvido* antes de executar
qualquer asserção; `UsuarioServiceTest.deleteWithSucess` quebrava com NPE por mock
ausente (sintoma direto da injeção por campo: o teste não é avisado pelo compilador
de que o serviço ganhou uma dependência nova); e `OsMarmorariaApplicationTests`
chamava `main()` de verdade — subia a aplicação inteira contra o PostgreSQL local,
passando ou falhando conforme a máquina. Testes que dependem do ambiente não são
rede de proteção: são loteria. A Fase 0 conserta a configuração, substitui o boot
real por um **teste de fumaça** com H2 e deixa a suíte 100% verde e auto-contida.

### 1.3 Smells transversais de Clean Code 🟠

| Smell | Quantidade | Princípio violado |
|-------|-----------:|-------------------|
| `@Autowired` em campo | 142 ocorrências em 48 arquivos | DIP/testabilidade — dependências ocultas, impossível instanciar a classe sem reflexão |
| `System.out` / `printStackTrace` | 25 ocorrências em 8 arquivos | Observabilidade — logs sem nível, sem timestamp, sem contexto, invisíveis em produção |
| Controle de fluxo por mensagem de erro (`e.getMessage().contains("já foram geradas")`) | 2 pontos críticos em `VendaService` | Fail Fast / tipagem — quebra silenciosamente se a mensagem mudar |
| Dois `@ControllerAdvice` concorrentes (`GlobalExceptionHandler` + `ResourceExceptionHandler`) | — | Least Surprise — qual handler responde depende de regras de precedência obscuras |

### 1.4 Serviços com responsabilidades demais (SRP) 🟠

| Classe | Linhas | Responsabilidades misturadas |
|--------|-------:|------------------------------|
| `VendaService` | 510 | CRUD de venda **+** faturamento (parcelas/contas a receber) **+** geração de OS **+** montagem de DTO |
| `ProjetoService` | 508 | CRUD **+** cálculo de orçamento **+** máquina de estados de status **+** montagem de DTO |
| `VendaUnificadaService` | 417 | sobrepõe `VendaService`, duplicando faturamento com regras próprias |
| `OrdemServicoService` | 363 | geração de OS **+** agendamento **+** mapeamento |

"Uma classe deve ter um único motivo para mudar." Hoje, mudar a regra de parcelamento exige
mexer na mesma classe que cria vendas — e cada mudança arrisca as demais responsabilidades.

### 1.5 Performance — JPA usado contra si mesmo 🟠

- `ProjetoRepository.findByIdWithDetails` **mente no nome**: a query é
  `SELECT p FROM Projeto p WHERE p.id = :id` — *idêntica* ao `findById`, sem nenhum
  `JOIN FETCH`. Quem confia no nome paga N+1 sem saber.
- `VendaService.efetuarVendaProjeto` tem ~40 linhas de getters chamados só para
  "forçar inicialização" de proxies lazy (linha 503: `venda.getCliente().getCliId(); // força inicialização`).
  É o sintoma clássico de fetch mal resolvido — a cura é a query certa, não o hack.
- Consultas de lista de venda-projeto disparam 1 select de cliente + 1 de pessoa **por linha**
  (associações EAGER carregadas sem fetch join em queries JPQL).

### 1.6 Segurança — bom RBAC, arestas a aparar 🟡

O que **já está bom** (e merece destaque no portfólio): matriz de autorização completa por
recurso × método HTTP com `hasAnyRole("ADMIN","GERENTE","FUNCIONARIO")`, filtro JWT, BCrypt.

O que falta:
- `SessionCreationPolicy.IF_REQUIRED` numa API JWT — deveria ser `STATELESS` (sem sessão,
  sem superfície de session fixation; coerente com o modelo de token).
- CORS `allowedOriginPatterns("*")` **com** `allowCredentials(true)` — combinação perigosa;
  origens devem vir de configuração por ambiente.
- `TokenService.validateToken` devolve `""` quando o token é inválido — engole a causa
  (expirado? assinatura errada?) e força o chamador a comparar string vazia.
- Segredos e credenciais devem vir de variáveis de ambiente (12-factor), nunca do repositório.

### 1.7 Modernização (consciente, não compulsiva) 🟡

- **Spring Boot 2.6.4 está EOL** (sem patches de segurança). A migração para 3.x
  (`javax.*` → `jakarta.*`, Hibernate 6, Security 6) é a recomendação futura nº 1, mas é
  um projeto em si — fica **fora** desta rodada para não misturar refatoração com migração
  (uma mudança de cada vez, sempre com a base verde).
- Sem Swagger/OpenAPI, sem Actuator — entram na fase de documentação.
- `mysql-connector-j` no pom sem uso real (datasource é Postgres) — remover na limpeza.

---

## 2. Princípios norteadores (o "porquê" recorrente)

| Princípio | Tradução prática neste projeto |
|-----------|--------------------------------|
| **S**RP | Serviço = orquestração de UM caso de uso. Faturamento ≠ venda ≠ OS ≠ mapeamento. |
| **O**pen/Closed | Exceções de domínio + handler único: novos erros não exigem `if` em cada catch. |
| **L**iskov | DTOs e entidades não se substituem — fronteiras explícitas via mappers. |
| **I**nterface Segregation | Controllers consomem serviços enxutos, não "deuses" de 500 linhas. |
| **D**ependency Inversion | Injeção por construtor com campos `final` — dependências visíveis e testáveis. |
| DRY | Conversões DTO↔entidade centralizadas; regras de parcela num único lugar. |
| KISS / YAGNI | Nada de abstração especulativa: cada fase resolve dor real catalogada acima. |
| Fail Fast | Exceções tipadas no lugar de strings mágicas; configuração ausente = erro no boot, não em runtime. |

---

## 3. Fases (ordenadas da menor para a maior superfície de risco)

Cada fase = commits pequenos e temáticos, aplicação compilando e testes verdes ao final.
A regra de ouro: **refatoração preserva comportamento**; onde houver risco, um teste de
caracterização entra antes.

### Fase 0 — Reprodutibilidade e rede de segurança *(risco baixo, valor altíssimo)*
1. `application.properties` base com placeholders de ambiente
   (`${JWT_SECRET}`, `${DB_URL}`, …) e `application-dev.properties` reduzido a overrides
   locais documentados. `.env.example` como contrato de configuração.
2. `docker-compose.yml` alinhado ao datasource real (PostgreSQL) — `docker compose up`
   + `mvn spring-boot:run` = aplicação no ar.
3. **Teste de fumaça**: `@SpringBootTest` que sobe o contexto inteiro com H2.
   É o teste mais barato que existe e captura: bean faltando, ciclo de dependência,
   propriedade ausente, erro de mapeamento JPA.
- **Aceitação:** clone limpo roda em ≤ 3 comandos documentados; 1 teste verde no CI local.

> **Status: ✅ CONCLUÍDA.** Perfis base/dev/prod com env vars; `.env.example`;
> docker-compose alinhado ao Postgres; `ApplicationContextSmokeTest` (perfil `h2`);
> suíte corrigida e verde (15 testes; `PaymentIntegrationTest` em quarentena
> documentada — ver §6.6).

### Fase 1 — Fundamentos de Clean Code *(risco baixo)*
1. Injeção por construtor em todos os `@Service`/`@Component`/`@Controller`
   (Lombok `@RequiredArgsConstructor` + campos `final`). 142 pontos.
2. SLF4J (`@Slf4j`) no lugar de `System.out`/`printStackTrace`. 25 pontos.
3. Remoção de código morto e extração de números mágicos para constantes nomeadas.
- **Aceitação:** zero `@Autowired` de campo, zero `System.out` em `src/main`; build verde.
- **Didática:** por que injeção por construtor? (1) a classe declara o que precisa na
  assinatura — honestidade; (2) `final` garante imutabilidade pós-construção; (3) testes
  unitários viram `new Service(mock1, mock2)` sem mágica de reflexão.

> **Status: ✅ CONCLUÍDA.** 142 pontos migrados em 48 classes (script
> `tools/migrate-constructor-injection.ps1` + 10 correções manuais); SLF4J nos 25
> `System.out`/`printStackTrace`; código morto removido; `DIAS_ENTRE_PARCELAS` extraída.
> Contexto Spring sobe inteiro — prova de ausência de ciclos de dependência.

### Fase 2 — Erros como contrato *(risco baixo)*
1. Hierarquia de exceções de negócio (`RegraDeNegocioException` e filhas específicas,
   ex.: `ContasReceberJaGeradasException`).
2. Eliminar `getMessage().contains(...)` — capturar o **tipo**, não a frase.
3. Consolidar os dois `@ControllerAdvice` num único handler com respostas padronizadas
   (404 para não-encontrado, 409 para conflito de regra, 400 para validação).
- **Aceitação:** nenhuma comparação de string de exceção; um único handler; contrato de erro uniforme.

> **Status: ✅ CONCLUÍDA.** Criadas `RegraDeNegocioException` (→ 409) e filhas
> `ContasReceberJaGeradasException` / `ContasPagarJaGeradasException`;
> `DeletionRestrictedException` entrou na hierarquia e perdeu o `@ResponseStatus`
> conflitante; `GlobalExceptionHandler` deletado — `ResourceExceptionHandler` é o
> handler único (404/409/400 com envelope `StandardError`); zero
> `getMessage().contains(...)` no projeto.

### Fase 3 — Camada de mapeamento *(risco médio)*
1. Mappers dedicados (`@Component`, escritos à mão — avaliado MapStruct, mas a maioria das
   conversões precisa de consultas a repositório ou cópias "leves" para controlar
   serialização, onde MapStruct atrapalha mais que ajuda).
2. Serviços param de montar DTOs inline; construtores de DTO param de conter lógica de consulta.
- **Aceitação:** conversões centralizadas e testadas; serviços só orquestram.

> **Status: ✅ CONCLUÍDA (fatia de maior valor).** Pacote `mapper/` criado com
> `VendaProjetoMapper` (cópias "leves" extraídas do `VendaService`),
> `ProjetoMapper` (peças/recortes/itens, antes em `ProjetoService`) e
> `OrdemServicoMapper` (antes em `OrdemServicoService`) — os três serviços que a
> Fase 4 decompõe. **Pendência incremental:** os `convertToDTO` simples de
> `ContaPagarService`/`ContaReceberService`/`ParcelaService` e as conversões em
> construtores de DTO (`VendaDTO`, `CompraDTO`, ...) seguem onde estão — são
> autocontidos e migram quando esses módulos forem tocados.

### Fase 4 — SRP nos serviços grandes *(risco médio/alto)*
1. `VendaService` → extrai `FaturamentoService` (parcelas/contas a receber) e mappers.
2. `ProjetoService` e `OrdemServicoService` → mesmo recorte.
3. Sobreposição `VendaService` × `VendaUnificadaService`: mapear antes, consolidar só com
   teste de integração cobrindo (regras de faturamento divergem — consolidar sem teste
   seria mudança de comportamento às cegas).
- **Aceitação:** nenhum serviço com mais de um motivo claro para mudar; alvo ~250 linhas.

### Fase 5 — Performance JPA *(risco médio)*
1. `findByIdWithDetails` honesto: `LEFT JOIN FETCH` de cliente/pessoa/itens/produto.
2. Apagar o bloco "força inicialização" — a query certa torna o hack desnecessário.
3. Fetch joins nas consultas de lista de venda-projeto (fim do select por linha).
4. Fronteiras `@Transactional` revisadas: `readOnly = true` em leituras (dispensa dirty
   checking → menos memória e CPU por requisição), transação obrigatória onde há escrita.
- **Aceitação:** zero force-init; N+1 catalogados eliminados (verificável ligando `show-sql` em dev).

### Fase 6 — Hardening de segurança *(risco médio)*
1. `SessionCreationPolicy.STATELESS`.
2. CORS por configuração (`app.cors.allowed-origins` por ambiente), nunca `*` com credenciais.
3. `validateToken` lança exceção específica (expirado ≠ inválido) → 401 com mensagem útil.
4. Segredos exclusivamente por variável de ambiente; validação de presença no boot (fail fast).
- **Aceitação:** OWASP A05 (Security Misconfiguration) e A07 (Identification and
  Authentication Failures) endereçados; nada sensível no repositório.

### Fase 7 — API e documentação *(risco baixo)*
1. `springdoc-openapi` (Swagger UI) com anotações nos controllers.
2. Actuator (health/info) para observabilidade básica.
3. `README.md` completo: arquitetura, como rodar, decisões de refatoração e aprendizados.
- **Aceitação:** Swagger reflete a API real; README permite subir o projeto do zero.

---

## 4. Riscos e mitigação

| Risco | Mitigação |
|-------|-----------|
| Refatorar sem testes | Fase 0 cria o teste de fumaça **antes** de qualquer mudança de código; testes de caracterização antes das fases 3–5. |
| Mudança de comportamento silenciosa | Commits pequenos (um tema por commit); diffs revisáveis; nada de "aproveitar para arrumar". |
| Sobreposição Venda/VendaUnificada | Mapear chamadas reais do frontend antes de consolidar. |
| Migração Boot 3 misturada à refatoração | Explicitamente **fora de escopo** desta rodada (ver §6). |

## 5. AI-Native Software Engineering aplicado a este projeto

Conceitos da formação aplicados de forma concreta (não decorativa):

1. **Testes de caracterização gerados por IA** — para fluxos sem teste (venda, OS), a IA lê
   o código e propõe testes que *documentam o comportamento atual* antes da refatoração.
   É exatamente o fluxo usado nestas fases.
2. **Observabilidade preparada para análise por IA** — logs estruturados (chave=valor,
   por evento de negócio) viram insumo para um agente diagnosticar erros em produção.
   A Fase 1 (SLF4J com contexto) é o pré-requisito disso.
3. **Documentação viva** — OpenAPI gerado do código (Fase 7) permite que agentes de IA
   consumam a API de forma autônoma — relevante para o objetivo de generalizar o backend
   para outros segmentos (ex.: app de logística consumindo a mesma API).
4. **Revisão assistida** — cada fase fecha com revisão de diff por IA procurando mudanças
   de comportamento não intencionais (prática usada nesta própria rodada).

## 6. Recomendações futuras (fora desta rodada — registradas para não virar escopo fantasma)

1. **Migração Spring Boot 2.6.4 → 3.x** *(prioridade nº 1)* — framework EOL. A branch `main`
   já tem essa fundação (Boot 3.2.5 + Security 6 + Flyway); o trabalho é reconciliar as
   features desta branch com aquela base.
2. **Flyway** — versionar o schema e abandonar `ddl-auto=update`.
3. **Generalização multi-segmento** — extrair o "core ERP" (pessoas, vendas, financeiro)
   de módulos específicos de marmoraria (projetos/peças/recortes), preparando o backend
   para servir outros domínios (ex.: app de transporte: cliente ↔ empresa ↔ transportadora).
4. **Conformidade fiscal (NF-e)** — módulo isolado, conforme extensão descrita no Notion.
5. **Cobertura de testes de integração** dos fluxos de negócio (venda completa, OS, faturamento).
6. **Reconciliar `PaymentIntegrationTest` com a API real** — a suíte de pagamentos foi
   escrita contra URLs que não existem (`/api/compras`, `/api/parcelas`); está em
   quarentena (`@Disabled`) desde a Fase 0 com a causa documentada no próprio arquivo.

## 7. Convenção de commits

`tipo(escopo): descrição` — corpo explica o *porquê* (didático).
Tipos: `feat`, `refactor`, `fix`, `perf`, `test`, `docs`, `chore`, `build`.
