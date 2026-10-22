# 🎨 Frontend — padrões visuais e de componentes

> **Status neste template:** o template não tem frontend. Este documento e os arquivos em `styles/` são a **referência** para o primeiro app Angular que nascer daqui (Angular com standalone components e signals).
> **Fonte de regra:** as regras de UI que valem para qualquer projeto estão resumidas em `docs/architecture/REGRAS-DO-SISTEMA.md` §8.

---

## 0. Princípios

- **Mobile-first.** Projete do menor breakpoint para cima. Usuário final opera no celular.
- **Standalone components + signals.** Sem NgModules. Estado local em `signal<T>()`; RxJS para fluxo assíncrono.
- **Uma fonte de verdade por valor visual.** Fonte, cor de status e espaçamento saem de variável CSS ou da escala. Nenhum componente declara hex solto ou nome de fonte.
- **Verificar no navegador.** Declarar CSS corrigido só lendo SCSS não basta: confira o *computed style* do elemento real. Causa comum: especificidade do tema.

Arquivos de referência: [`styles/global-styles.scss`](styles/global-styles.scss) e [`styles/list-cards.scss`](styles/list-cards.scss).

---

## 1. Tema e tokens

| Token | Papel |
|---|---|
| `--app-bg`, `--app-surface`, `--app-surface-subtle` | Fundo da página, superfície de card, fundo de item de lista |
| `--app-text`, `--app-muted` | Texto principal e secundário |
| `--app-primary` | Cor da marca (topbar, botão primário, links, hover de card) |
| `--app-border`, `--app-divider` | Bordas e separadores |
| `--app-success/danger/warning/info` + `*-light` | Status. Única fonte de verde/vermelho/laranja/azul do app |
| `--app-font`, `--app-font-mono` | Fonte única e fonte monoespaçada (IDs, documentos) |

**Regras**

- Trocar a fonte = editar `--app-font` e o link do Google Fonts. Nenhum outro arquivo contém o nome da fonte.
- Trocar a cor da marca: gere o tema Material pelo schematic (`ng generate @angular/material:m3-theme`) e **não** edite o arquivo gerado à mão.
- Armadilha conhecida: sem `mat.color-variants-backwards-compatibility(...)` depois de `mat.all-component-themes(...)`, `color="warn"` e `color="accent"` por instância ficam sem cor. Se um componente perder a cor após trocar o tema, esse mixin é o primeiro a conferir.
- Exceção deliberada, quando existir, precisa de registro com data no próprio documento (ex.: logo com fonte própria).

---

## 2. Tipografia

- **Prebuilt do Angular Material colocam `Roboto` em dezenas de tokens `--mat-*-font` / `--mdc-*-font`.** Reaponte-os para `var(--app-font)` em `:root` (lista em `styles/global-styles.scss`).
- **Armadilha de especificidade:** o tema prebuilt define `.mat-typography h1..h6 { font: ... Roboto }` com especificidade maior que `.mat-typography` sozinho. Todo título saía em Roboto. Correção: regra explícita `.mat-typography h1, …, h6 { font-family: var(--app-font); }`. Qualquer override de tipografia do Material precisa igualar ou superar a especificidade do seletor do tema.
- Escala de tamanhos (`$font-size-*` no SCSS): 11 / 12 / 14 / 16 / 18 / 20 / 24 / 28 px. Não criar tamanho novo por tela.
- Títulos de página: `.page__title` (28px, bold, `letter-spacing: -0.02em`).

---

## 3. Layout e responsividade

| Breakpoint | Uso |
|---|---|
| 500px | Ajustes finos de cards e padding (telas pequenas) |
| 600px | Mobile: colunas, botões full-width |
| 900px | Tablet: troca de navegação (sidebar ↔ barra inferior) |

- **`.page` é uma regra global.** Largura máxima 1200px, centralizada, padding 16px (24px a partir de 600px). Nenhuma tela redefine `.page`. Largura diferente entre telas é bug.
- **`.list-head` ≠ `.detail-head`.** `.list-head` é só para listagens (título + botão "Novo"). `.detail-head` é só para detalhe de entidade. Em mobile, ambos empilham e os botões ficam full-width.
- **`.row-actions`** é a única classe de linha de botões: horizontal no desktop, coluna full-width abaixo de 600px. Não tratar botão por botão.
- **Alvos de toque ≥ 44px** em botões de ação e ícones.
- **Sem `mat-table` em listas de cadastro.** Use cards (seção 4). Tabela só em telas de operação interna, com wrapper `overflow-x: auto`.
- **Navegação inferior (mobile):** itens de largura fixa, nunca `flex: 1` (com 8 itens, cada novo item espreme os outros até ficar ilegível). Scroll horizontal com scrollbar oculta.

---

## 4. Card pattern — 3 linhas (listagens)

Toda listagem de entidade usa a mesma estrutura, definida uma vez em `styles/list-cards.scss`. Nova entidade **estende** essas classes (`@extend`), não cria CSS próprio.

| Linha | Conteúdo | Regra |
|---|---|---|
| **1 — Header** | Nome como link (ícone + texto) à esquerda; badges de status à direita | Nunca documento ou código na linha 1 |
| **2 — Meta** | `.code-chip` (documento/ID) à esquerda; valor (`.item-card__amount`) à direita | Nunca inverter os lados. `.code-chip` sempre com fundo |
| **3 — Legenda** (opcional) | Contadores `.legend-item` com ícone, só em entidades com dados agregados | Alinhada à **esquerda**. `[disabled]` quando contador é zero |

**Checklist de nova lista:**
1. `mat-card class="item-card"`, linhas 1 e 2 conforme a tabela.
2. Testar em 375px e em 1200px+.
3. Sem CSS novo na tela: se faltar algo, a classe compartilhada ganha a variante.
4. Skeleton de cards (`app-list-skeleton`) enquanto carrega, em vez de spinner ou tela em branco.
5. Empty state com CTA que reaproveita a mesma ação do botão "Novo".

---

## 5. Formulários e validação

| Regra | Detalhe |
|---|---|
| **Erro de validação inline** | `mat-error` no campo. **Nunca toast** para validação. Toast é só para resultado de ação. |
| **Erro de cadastro (409/422) em dialog** | Em formulário de criação, suprimir o toast global e abrir dialog com a causa. Título varia por caso (ex.: "Documento já cadastrado"). Ao fechar, foque de volta o campo com erro. |
| **Data** | Não usar `<input type="date">`. Usar um componente de campo de data que aceita calendário **e** digitação `dd/mm/aaaa`. Valor interno continua `AAAA-MM-DD`, então validators e payloads não mudam. Converta sem deslocamento de fuso (`new Date('AAAA-MM-DD')` vira o dia anterior no Brasil). |
| **Máscaras e pipes** | Um pipe/diretiva por tipo, usado em todo lugar: moeda, data, documento (CPF/CNPJ), telefone. Máscara também nos **filtros de busca**, não só no formulário de cadastro. |
| **Documento** | Validar checksum no frontend espelhando o backend. Enviar só dígitos. |
| **Valor monetário e percentual** | `type="number" step="0.01" inputmode="decimal"`; `Validators.pattern(/^\d+(\.\d{1,2})?$/)`; percentual com `Validators.max(100)`. Espelha a validação do backend. |
| **Sem spinners** | Remover spinners de `input[type=number]` globalmente (ver `global-styles.scss`). |
| **Campo numérico com valor inicial 0 ou 1** | Selecionar o conteúdo ao focar, para o usuário digitar por cima. |
| **IDs vindos da API** | Podem chegar como `number` mesmo tipados como `string`. Comparar sempre como string (`String(a) === String(b)`; `[compareWith]` no `mat-select`). `2 === "2"` é `false`, e o erro é silencioso. |

---

## 6. Diálogos

- Abertura única: `dialog.open(Component, { data })` → `afterClosed().subscribe(done => done && reload())`. Assim todo call site é igual e fácil de auditar.
- **Largura padrão de formulário:** 480px. Diálogo nunca passa de 96vw em tela pequena.
- **Padding:** 32px horizontal no título e no conteúdo (20px em ≤600px, 16px em ≤500px).
- **Ordem dos botões:** "Cancelar" primeiro, ação primária por último.
- **Ação irreversível ou auditável nunca é um clique direto.** Abre diálogo pedindo o motivo, e o motivo entra no histórico.
- **`confirm()` nativo do navegador é proibido.** Use o diálogo de confirmação do app.

---

## 7. Botões e ações

- Um wrapper de botão do projeto (com loading state e variantes). Não usar o botão do Material direto nas telas, para centralizar loading e cores.
- **Botão de ícone sempre tem tooltip** (`matTooltip` com o mesmo texto do `aria-label`). Ícone sem tooltip não informa nada ao usuário que não conhece o símbolo.
- **Ação proibida pelo estado ou permissão não aparece.** Ex.: "Pagar" some em contrato fechado. Reabrir continua visível quando for permitido. O backend continua bloqueando; o frontend só evita mostrar o que não pode ser usado.
- Regras de estado terminal devem estar em um único lugar (um mapa ou função de guard), e não duplicadas em cada template.

---

## 8. Feedback ao usuário

- **Snackbar:** sucesso 3,5 s; erro 6 s. Variantes `app-snackbar--success/error/warning` com as cores de status.
- **Loading:** skeleton na lista, spinner só em ação pontual.
- **Empty state:** texto explicando o que falta + CTA para a próxima ação.
- **Formatação de data e valor:** sempre pelo pipe padrão. Nunca `ISO` cru na tela.

---

## 9. Como usar os arquivos de referência

1. Copie `styles/global-styles.scss` para `src/styles.scss` do app. Ajuste `--app-*` e `--app-font` à identidade visual.
2. Copie `styles/list-cards.scss` para `src/app/shared/styles/list-cards.scss`.
3. Confirme no navegador (computed style) que os títulos e botões herdam `--app-font` e as cores de tema.
4. Registre no `REGRAS-DO-SISTEMA.md` §8 qualquer desvio desta referência, com data.

---

## Cuidados ao copiar a referência

- Não deixe cor hardcoded em componente: use sempre as variáveis de `styles/global-styles.scss`.
- Diálogos de formulário tendem a repetir boilerplate; um diálogo base abstrato reduz isso.
- Em telas pequenas (abaixo de 500px), medir antes de ajustar cards altos e barras de navegação.
