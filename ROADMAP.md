# Roadmap — do sistema atual a um ERP completo

> Plano de evolução do ERP Marmoraria São Carlos: o que falta para operação
> real com conformidade fiscal (NF-e/NFS-e), financeiro de verdade e os
> diferenciais de um ERP moderno. Fases ordenadas por dependência técnica —
> cada uma habilita a seguinte. Tamanhos: 🟢 pequeno · 🟡 médio · 🔴 grande.

---

## 0. O que o sistema JÁ cobre (inventário honesto)

| Domínio | Estado |
|---|---|
| Cadastros (clientes PF/PJ, fornecedores, funcionários, produtos/grupos) | ✅ Completo para o fluxo atual |
| Estoque (movimentações, reservas por venda, baixa por OS) | ✅ Funcional |
| Compras → contas a pagar parceladas | ✅ Funcional |
| Projetos personalizados (peças, recortes, medidas, orçamento) | ✅ Diferencial do produto |
| Vendas (produto e projeto) → contas a receber parceladas | ✅ Funcional |
| Ordens de serviço (geração por projeto, agendamento, execução) | ✅ Funcional |
| Relatórios PDF (Jasper) e dashboard | ✅ Básico |
| Segurança (JWT + RBAC por rota, STATELESS, CORS restrito) | ✅ Pós-refatoração |
| Qualidade (testes, Swagger, health check, config 12-factor) | ✅ Pós-refatoração |
| Gateway de pagamento | ⚠️ **Simulado** — não movimenta dinheiro real |
| Emissão fiscal | ❌ Inexistente |
| Cobrança real (boleto/PIX), conciliação bancária | ❌ Inexistente |
| Auditoria (quem alterou o quê) | ❌ Inexistente |

---

## Fase A — Fundação técnica *(pré-requisito de tudo)* 🔴

Construir módulo fiscal sobre framework sem patches de segurança seria
negligência. Esta fase não adiciona feature visível — ela torna as próximas
possíveis e seguras.

1. **Migração Spring Boot 2.6 → 3.x** (`javax.*`→`jakarta.*`, Hibernate 6,
   Security 6). A branch `main` já tem a fundação; o trabalho é reconciliar
   as features. 🔴
2. **Flyway** — schema versionado em migrações; fim do `ddl-auto=update`.
   Obrigatório antes de o banco conter dados fiscais (auditável). 🟡
3. **Testes de integração dos fluxos críticos** (venda completa, OS,
   faturamento) — a rede que protege as fases B–D. 🟡
4. **CI/CD** — GitHub Actions: build + testes nos dois repos a cada push;
   deploy com Docker Compose (API + Postgres + frontend atrás de proxy com
   HTTPS) e **backup automatizado do Postgres** (sem backup não há ERP). 🟡
5. **Upgrade Angular 16 → versão suportada** via `ng update`. 🟡
6. Autenticação madura: refresh token, expiração curta de access token,
   cookie HttpOnly (já mapeado), rate limiting no login. 🟡

**IA acelera:** migração Boot 2→3 e Angular são casos ideais de transformação
mecânica assistida (mesmo padrão dos scripts desta refatoração); geração de
testes de integração a partir dos fluxos existentes.

---

## Fase B — Cadastros e dados fiscais *(habilita a NF-e)* 🟡

A nota fiscal é tão boa quanto os dados que a alimentam.

1. **Empresa emitente**: CNPJ, IE, IM, regime tributário (Simples Nacional é
   o provável para marmoraria — **validar com o contador**), endereço fiscal,
   certificado digital **A1** (arquivo .pfx — o formato automatizável).
2. **Produtos**: NCM (obrigatório), unidade tributável, origem da mercadoria,
   CEST quando aplicável.
3. **Clientes**: CPF/CNPJ validado, IE/isento, endereço fiscal completo com
   código IBGE do município.
4. **Operações**: CFOP por tipo de operação (venda dentro/fora do estado),
   CSOSN (Simples) por produto/operação.
5. **Naturezas de operação** configuráveis (venda, remessa, devolução).

**IA acelera:** sugestão de NCM por descrição do produto (classificação
assistida — sempre com revisão humana/contador, NCM errado = multa).

---

## Fase C — Emissão fiscal (NF-e / NFS-e) 🔴 — o coração do pedido

### Decisão de arquitetura: API fiscal terceirizada, não implementação própria

Implementar comunicação SEFAZ na mão (assinatura XML, schemas, contingência,
SEFAZ de cada UF) é projeto de **meses** e manutenção perpétua. Provedores
como **Focus NFe, NFe.io, eNotas, PlugNotas** expõem isso como API REST com
sandbox — e **absorvem as mudanças regulatórias**, o que em 2026 não é
detalhe: a **reforma tributária está em transição** (fase de testes de
CBS/IBS com destaque em nota a partir de 2026, layout da NF-e atualizado).
Acompanhar isso sozinho é inviável para um time pequeno.

**Desenho:** porta/adaptador — interface `EmissorFiscal` no domínio,
implementação `FocusNfeEmissor` (ou outro) isolada em módulo de
infraestrutura. Trocar de provedor = nova implementação, zero mudança no
domínio. (Mesmo padrão do `FaturamentoService`: a regra não conhece o
transporte.)

### Escopo

1. **NF-e (modelo 55)** para venda de produtos: emissão a partir da venda
   efetivada, DANFE em PDF, e-mail ao cliente, **cancelamento** e **carta de
   correção**, tratamento de rejeições da SEFAZ (status machine na entidade
   `NotaFiscal`).
2. **NFS-e** para o serviço (instalação/mão de obra da OS) — padrão nacional
   ou prefeitura local; **a divisão produto×serviço da marmoraria deve ser
   definida com o contador** (impacta ISS vs ICMS).
3. **Entrada de NF de fornecedor**: importação do XML da compra → confere
   itens → dá entrada no estoque automaticamente.
4. Ambiente de **homologação primeiro**, sempre; só depois produção.

**IA acelera:** leitura do XML/DANFE de fornecedores para pré-preencher a
entrada de compra; explicação em linguagem natural das rejeições da SEFAZ
(códigos crípticos → ação concreta).

### Conformidade adjacente

- **LGPD**: política de retenção, minimização de dados pessoais, controle de
  acesso já existente (RBAC) documentado, processo de exclusão/anonimização
  de cliente sem quebrar histórico fiscal (que tem retenção legal própria).
- Guarda dos XMLs por 5 anos (storage durável + backup).

---

## Fase D — Financeiro de verdade 🔴

1. **Cobrança real**: boleto e **PIX** via API de um banco/fintech (Asaas,
   Efí, Cora, Inter) — gerar cobrança junto da parcela, **webhook de
   liquidação** dá baixa automática na conta a receber. Substitui o gateway
   simulado atual (a abstração `PaymentProvider` já existe — vira adaptador
   real).
2. **Conciliação bancária**: importação OFX (ou Open Finance depois);
   conciliação sugerida automaticamente, confirmada por humano.
3. **Fluxo de caixa projetado** (a pagar + a receber por vencimento) e
   **DRE simplificado**; margem real por projeto (orçado × realizado).
4. Centro de custos básico.

**IA acelera:** matching de conciliação (extrato × lançamentos) com sugestão
ranqueada; categorização automática de despesas; alerta de anomalia
("este fornecedor cobrou 3× a média").

---

## Fase E — Operação madura 🟡

1. **Auditoria**: Hibernate Envers (quem alterou o quê e quando) nas
   entidades sensíveis — pré-requisito de confiança num ERP multiusuário.
2. **Notificações**: e-mail/WhatsApp (API oficial) para orçamento enviado,
   parcela vencendo, OS agendada.
3. **Anexos**: fotos de medição/projeto/comprovantes em storage S3-compatível.
4. **Agenda** de instalação (visão calendário das OS).
5. Soft delete + lixeira; busca global; exportação CSV/Excel dos relatórios.

---

## Fase F — Diferenciais AI-Native 🟡 *(o que poucos ERPs pequenos têm)*

1. **Assistente de orçamento**: descrição em linguagem natural → rascunho de
   projeto com materiais sugeridos e preço (usa o `OrcamentoProjetoService`
   e o histórico como contexto).
2. **Consulta conversacional**: "quanto vendi em maio? quais clientes estão
   inadimplentes?" — agente com tool use sobre a API existente (o Swagger
   da Fase 7 é literalmente o contrato que o agente consome).
3. **Previsão**: demanda de materiais e risco de inadimplência por histórico.
4. **Entrada fiscal inteligente** (da Fase C): XML do fornecedor → estoque,
   sem digitação.

---

## Ordem recomendada e dependências

```
A (fundação) ──► B (dados fiscais) ──► C (NF-e/NFS-e)
     │                                      │
     └────────► D (financeiro real) ◄───────┘  (boleto/PIX independe da NF-e,
     │                                          mas conciliação conversa com C)
     └────────► E (operação)  ──►  F (AI-native, incremental desde já)
```

**Os 3 movimentos de maior valor por esforço, nesta ordem:**
1. Fase A itens 1–2 (Boot 3 + Flyway) — destrava tudo.
2. Fase C com provedor terceirizado — é o que transforma "sistema de gestão"
   em "ERP que emite nota".
3. Fase D item 1 (PIX/boleto com baixa automática) — é o que o usuário final
   sente no bolso todo dia.

**Fora de escopo consciente (por ora):** multi-empresa/filial, folha de
pagamento, SPED completo, BI pesado — são ERPs enterprise; aqui seriam
over-engineering antes de existir demanda real.

**Premissa de todas as fases fiscais:** decisões tributárias (regime, CFOP,
CSOSN, divisão produto×serviço) são definidas **com um contador** — o
sistema implementa, não legisla.
