# PR — Avaliação A1: Módulo de Produtos

**Branch:** `avaliacao1` → `main`
**Data:** 02/10/2026

## O que foi feito

- **Entidade e persistência:**
  - `Produto` (`idProduto`, `nome`, `categoria`, `estoqueAtual`, `estoqueMinimo`, `unidadeMedida`, `dataCadastro`) e enum `CategoriaProduto` (`ALIMENTO`, `ROUPA`, `HIGIENE`, `OUTROS`).
  - Migration `V4__create_produtos_table.sql`: PK `id_produto` (padrão `id_`), `UNIQUE` no nome e `CHECK (>= 0)` nos dois estoques, como última barreira no banco.
- **DTOs e validações:**
  - `ProdutoRequestDTO` e `ProdutoResponseDTO` como `record`, com `@Schema(example = ...)`.
  - Bean Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Min`) com mensagens i18n no `messages.properties` (`produto.*`).
  - Validação customizada `@EstoqueNaoNegativo` + `EstoqueNaoNegativoValidator`, aplicada em `estoqueAtual`.
- **Service e regras de negócio:**
  - Interface `ProdutoService` (contrato pedido na prova) e implementação `ProdutoServiceImpl`.
  - Estoque negativo → `EstoqueNegativoException` → **422**.
  - Nome duplicado (sem diferenciar maiúsculas/minúsculas; no PUT ignora o próprio produto) → `NomeProdutoDuplicadoException` → **409**.
  - `estoqueBaixo` calculado no mapeamento (`estoqueAtual < estoqueMinimo`). O campo não é gravado no banco.
  - Filtros opcionais `nome` (parcial) e `categoria` com `ProdutoSpecifications`, no mesmo padrão de `DoacaoSpecifications`.
- **Controller e HTTP** (`/api/v1/produtos`): `GET` paginado, `GET /{id_produto}`, `POST` (201 + `Location`), `PUT /{id_produto}`, `DELETE /{id_produto}` (204).
  - Paginação com `page`, `size` e `sort` sanitizado, no mesmo padrão da correção do professor na busca de beneficiários (o Swagger UI envia `["nome,desc"]`).
- **`GlobalExceptionHandler`:**
  - handlers para 409 (`produto-duplicado`) e 422 (`estoque-negativo`) em Problem Details;
  - quando a **única** falha de Bean Validation é `@EstoqueNaoNegativo`, a resposta é 422 em vez de 400.
- **Swagger:** `@Tag`, `@Operation`, `@ApiResponse` (incluindo 409/422 com schema `ProblemDetail`) e `@Parameter` em todos os endpoints.
- **README:** como rodar, como testar, estrutura de pacotes e endpoints principais.

## Como testar

1. `mvn clean compile` → `BUILD SUCCESS`.
2. `mvn spring-boot:run` e abrir `http://localhost:8080/swagger-ui.html` → seção **Produtos**.
3. `POST /api/v1/produtos` com:
   ```json
   { "nome": "Arroz 5kg", "categoria": "ALIMENTO", "estoqueAtual": 3, "estoqueMinimo": 10, "unidadeMedida": "unidade" }
   ```
   → **201**, `Location: /api/v1/produtos/1` e `"estoqueBaixo": true`.
4. Repetir o mesmo POST (ou com `"nome": "arroz 5kg"`) → **409** `"Já existe um produto cadastrado com o nome 'arroz 5kg'."`.
5. POST com `"estoqueAtual": -1` → **422** com `errors[0].field = "estoqueAtual"`.
6. POST com `"nome": ""` e `"estoqueAtual": -1` → **400** (o corpo tem erro de formato além da regra de negócio).
7. `GET /api/v1/produtos?categoria=ALIMENTO&nome=arr` → 200 com o produto. Testar também `sort` = `["nome,desc"]`.
8. `PUT /api/v1/produtos/1` com `"estoqueAtual": 30` → 200 e `"estoqueBaixo": false`.
9. `GET /api/v1/produtos/99` → 404. `DELETE /api/v1/produtos/1` → 204; repetir → 404.
10. Com o Docker rodando: `mvn clean test` → `Tests run: 24, Failures: 0, Errors: 0, Skipped: 0` (suíte existente; o Flyway aplica V1–V4 sem quebrar os testes anteriores).

## Checklist de Qualidade

- [x] Código compila sem erros (`mvn clean compile`)
- [x] Testes passam (`mvn test`)
- [x] Arquitetura em camadas respeitada (Controller → Service → Repository)
- [x] DTOs separados da Entity
- [x] Injeção de dependência via construtor (sem `@Autowired` em atributo)
- [x] Commits atômicos e descritivos (Conventional Commits)

## Uso de IA

- [ ] Não usei IA nesta entrega

## Observações

- **422 x 400 no estoque negativo:** o exemplo de DTO da prova usa `@Min(0)` em `estoqueAtual`, mas isso devolveria 400, e a regra pede 422. Por isso `estoqueAtual` usa `@EstoqueNaoNegativo`, e o handler converte essa violação para 422 quando ela é a única falha. O service também valida a regra, para que ela valha mesmo fora do controller.
- `estoqueMinimo` negativo continua com `@Min(0)` → 400, pois é erro de entrada e não a regra "estoque atual negativo".
- O path variable segue o enunciado (`{id_produto}`), mapeado para `Long idProduto` com `@PathVariable("id_produto")`.
- Foi usado `HttpStatus.UNPROCESSABLE_CONTENT` (nome atual do 422 no Spring 7; `UNPROCESSABLE_ENTITY` está depreciado).
- O bônus de testes automatizados (`ProdutoServiceTest` e `ProdutoControllerIntegrationTest`) não foi incluído nesta entrega.
