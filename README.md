# SocialConnect — Gestão de apoio social

API para organizar beneficiários, doações e estoque de produtos de instituições sociais.

## Tecnologias

Java 21, Spring Boot, Spring Data JPA, Bean Validation e Flyway. O desenvolvimento usa H2 em memória; o projeto também inclui o driver PostgreSQL e testes com Testcontainers.

## Executar

Instale o JDK 21. Na pasta do projeto, execute:

```bash
./mvnw spring-boot:run
```

No Windows:

```bat
mvnw.cmd spring-boot:run
```

| Ferramenta | Endereço local |
| --- | --- |
| Swagger | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI | http://localhost:8080/api-docs |
| Console H2 | http://localhost:8080/h2-console |

No console H2, use `jdbc:h2:mem:socialconnectdb`, usuário `sa` e senha vazia. Os dados desse banco são temporários.

## Recursos disponíveis

| Módulo | Rota base | Operações |
| --- | --- | --- |
| Beneficiários | `/api/v1/beneficiarios` | Listar, consultar, cadastrar, atualizar, atualizar parcialmente e excluir |
| Doações | `/api/v1/doacoes` | Listar, consultar, cadastrar, atualizar, atualizar parcialmente e excluir |
| Produtos | `/api/v1/produtos` | Listar, consultar, cadastrar, atualizar e excluir |

As listagens possuem paginação. Beneficiários permitem filtros por nome e CPF; doações, por intervalo de datas e tipo; produtos, por nome e categoria. O cadastro de doadores está modelado na persistência, sem controller próprio.

## Regras principais

- CPF de beneficiário não pode ser duplicado.
- Doações devem estar vinculadas a um doador existente.
- Produtos devem ter nome único, sem diferenciar maiúsculas e minúsculas.
- Estoque negativo é rejeitado; a resposta sinaliza estoque abaixo do mínimo.
- Campos de entrada passam por validações e os erros seguem o formato Problem Details.

## Organização

Cada recurso reúne controllers, DTOs, entidades, repositories e services. Os controllers recebem as requisições; os services executam as regras; os repositories acessam o banco. As pastas `config`, `exception` e `validation` concentram os componentes compartilhados. As migrations ficam em `src/main/resources/db/migration`.

## Testes

Testes unitários:

```bash
./mvnw test -Dtest='*ServiceTest,*ValidatorTest'
```

Suíte completa (requer Docker para os testes de integração):

```bash
./mvnw clean test
```
