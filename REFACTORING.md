# Plano de refatoração — osAPI

Este documento registra a rodada de refatoração do backend do TCC (projeto
desenvolvido originalmente em modo "go-horse", sem testes, sem padrão
consistente de injeção de dependência e sem documentação de API) em
`portfolio-refactor`. Cada fase é um commit isolado, validado com
`mvn compile` + `mvn test` (build verde) antes da próxima — nunca duas fases
no mesmo commit, para que o histórico git seja, em si, a documentação do
raciocínio.

## Por que esta ordem

Infraestrutura de boot e rede de testes vêm primeiro porque toda fase
seguinte precisa de um jeito confiável de provar "não quebrei nada" — sem
isso, qualquer refatoração é feita no escuro. Mudanças de baixo risco
(logging, injeção de dependência) vêm antes de mudanças de alto risco
(dividir serviços, mudar queries) porque cada fase precisa do terreno
anterior validado para isolar a causa de uma eventual regressão.

## Fase 0 — Boot reprodutível + rede de segurança de testes

**Commits:** `8667096` (build/config), `9a952f6` (test)

Antes: só existia `application-dev.properties`, nada ativava o perfil
`dev` por default, não havia `application.properties` base nem
`application-prod.properties`, `.gitignore` ignorava *qualquer*
`application.properties` (inclusive um não-sensível), e a suíte de testes
tinha 13 falhas — incluindo um `OsMarmorariaApplicationTests` vazio que
não testava nada e escondia que o contexto Spring não subia.

Depois: perfis `dev`/`test`/`prod` explícitos e documentados,
`.env.example` como contrato de configuração, `docker-compose.yml`
alinhado ao datasource real (PostgreSQL), `.gitignore` corrigido para
ignorar apenas segredos (`.env`), `ApplicationContextSmokeTest` novo que
sobe o contexto Spring completo contra H2, e as 13 falhas pré-existentes
resolvidas ou colocadas em quarentena documentada (`PaymentIntegrationTest`
testava URLs que não existem na base atual).

**Bugs reais encontrados no processo** (não apenas infraestrutura):
- `Usuario`: construtor de 4 argumentos atribuía `senha` duas vezes e
  nunca atribuía `email` — causava falha de validação em qualquer fluxo
  que usasse esse construtor.
- `UsuarioService.fromDTO`: NPE ao atualizar um usuário sem funcionário
  associado (`obj.getFuncionario().getId()` sem null-check).
- `TestConfig.instanciaDB()`: `@Bean` declarado retornando `void` —
  inválido no Spring, travava o contexto no perfil `test`.

## Fase 1 — Logging estruturado + injeção por construtor

**Commits:** `cd3a3af` (log), `9cb84d6` (DI)

`System.out.println`/`printStackTrace` substituídos por SLF4J (`@Slf4j`)
em 4 controllers. Injeção por campo (`@Autowired` em 142 pontos, 48
classes) substituída por injeção por construtor (`private final` +
`@RequiredArgsConstructor`), via script (`tools/migrate_constructor_injection.py`,
porte em Python de um script PowerShell equivalente).

5 casos exigiram correção manual (o script cobre o padrão comum, não
casos irregulares):
- `AuthController` tinha injeção por campo **e** por construtor parcial
  simultaneamente (duplicidade) — consolidado em um único
  `@RequiredArgsConstructor`.
- `SecurityFilter`, `AuthorizationService`, `CidadeService`,
  `EstadoService`: campos *package-private* (sem `private`) — o script só
  torna `final` campos com modificador de acesso explícito; sem a correção
  manual, a dependência deixaria de ser injetada silenciosamente.

## Fase 2 — Exceções de domínio tipadas

**Commit:** `1d27088`

Nova hierarquia `RegraDeNegocioException` → HTTP 409 (conflito de estado),
substituindo `RuntimeException` genérica nos dois pontos de
`VendaService` que geravam idempotentemente ordem de serviço/conta a
receber duplicada. `TransicaoStatusInvalidaException` — que existia no
código mas nunca era lançada nem tinha handler (daria 500 se algum dia
fosse usada) — passou a estender a hierarquia, ficando coberta.

Esta base já não tinha o anti-padrão `getMessage().contains(...)` nem
múltiplos `@ControllerAdvice` conflitantes (só havia 1 handler desde o
início) — diferente do histórico de referência de outra rodada deste
mesmo domínio.

## Fase 3 — Mappers dedicados

**Commit:** `55125b5`

`VendaProjetoMapper`, `ProjetoMapper`, `OrdemServicoMapper` extraídos dos
respectivos services, isolando "orquestrar o caso de uso" de "montar a
representação de API" (SRP). Decisão: `@Component` escrito à mão, não
MapStruct — a maioria das conversões depende de consulta a repositório
(itens com fetch join) ou cópia de referência de entidade, caso em que um
mapper gerado não eliminaria a dependência de repositório, só esconderia
código equivalente atrás de uma anotação.

## Fase 4 — SRP em `VendaService`

**Commit:** `479b61a`

`VendaService` tinha duas razões para mudar: o ciclo de vida da venda de
produto de balcão (`VendaController`) e o ciclo de vida da venda de
projeto sob encomenda (`VendaProjetoController`) — os dois fluxos nunca
compartilham chamador, mas viviam na mesma classe de 328 linhas. Os 7
métodos exclusivos de venda de projeto foram extraídos para o novo
`VendaProjetoService`.

## Fase 5 — Performance JPA

**Commit:** `7d70930`

Três problemas distintos de consulta, achados por auditoria própria dos
repositories/mappers produzidos nas Fases 3-4 (não copiados de um
diagnóstico anterior — esta base tinha problemas diferentes):

1. `VendaRepository.findByClienteIdAndVendaTipo` usava JPQL
   `v.cliente.id` — `Cliente` não tem campo `id` (só `cliId`). JPQL não é
   validado em compile-time; o primeiro uso real explodiria em runtime
   com `IllegalArgumentException`. Corrigido para `c.cliId`, com fetch
   join de `cliente`+`pessoa` (evita 2 selects extras por venda listada).
2. `ProjetoMapper.toDto` sempre refazia a query de itens mesmo quando o
   `Projeto` já vinha com `itens` carregado por fetch join — corrigido com
   `Hibernate.isInitialized`.
3. Listagens paginadas de projeto faziam 1 query de itens por projeto da
   página (paginação não permite fetch-joinar uma coleção) — novo
   `ProjetoMapper.toDtoList` resolve em uma única query `IN`.

## Fase 6 — Hardening de segurança

**Commit:** `a564828`

Duas das três frentes de hardening (sessão `STATELESS`, CORS restrito por
`app.cors.allowed-origins` em vez de `@CrossOrigin("*")` espalhado) já
estavam corretas desde a Fase 0. A terceira — `TokenService.validateToken`
devolvendo `""` ao falhar a verificação, escondendo o motivo real do
log — foi corrigida com `TokenInvalidoException`, preservando o
comportamento HTTP (segue sem autenticar, 401 pelo entry point).

O `TODO` de RBAC por role/recurso (hoje: `permitAll()` na maioria dos
endpoints) permanece documentado explicitamente no Javadoc de
`SecurityConfig` — não é mascarado nem fingido como resolvido.

## Fase 7 — API e documentação

Esta fase: `OpenApiConfig` (esquema bearer-JWT para o Swagger UI testar
endpoints autenticados), remoção de dependências mortas do `pom.xml`
(`flyway-core`/`flyway-mysql` sem nenhuma migração versionada no projeto;
`mysql-connector-j` com o datasource sempre PostgreSQL), `README.md`
como porta de entrada do portfólio.

## O que não foi tocado (fora de escopo, por decisão)

- RBAC por role/recurso em `SecurityConfig`.
- Inferência de projeto por `clienteId + status` em
  `VendaProjetoService` (sem FK direta `Venda.projetoId` confiável em
  todos os fluxos).
- `VendaProjetoMapper.podeGerarOS`/`podeGerarContaReceber` calculados
  contra defaults do DTO em vez do estado real da venda.
