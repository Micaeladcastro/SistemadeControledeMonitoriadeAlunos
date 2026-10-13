# Back-End: Sistema de Controle de Monitoria de Alunos

API REST em **Java 21 + Spring Boot 3**, com **Spring Security (usuários no banco) + JWT**, **PostgreSQL** e **Flyway**.

## Como rodar

**Opção 1: só com Docker Desktop (não precisa instalar Java nem PostgreSQL)**

```bash
cd backend
docker compose up --build     # sobe o PostgreSQL + a API em http://localhost:8080
```

Para parar, use `Ctrl+C` e depois `docker compose down`. Para apagar o banco e começar do zero, use `docker compose down -v`.

**Opção 2: rodando o Java na máquina (JDK 21)**

```bash
cd backend
docker compose up -d postgres   # só o banco
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run  (o Maven é baixado sozinho)
```

**Testar no VS Code:** instale a extensão **REST Client**, abra `api.http` e clique em *Send Request* em cada requisição, de cima para baixo.

Na primeira execução, o **Flyway** cria todas as tabelas (`src/main/resources/db/migration`) e carrega os dados de teste. O `DataSeeder` cria os usuários:

| Login   | Senha      | Perfil         |
|---------|------------|----------------|
| `admin` | `admin123` | ADMINISTRADOR  |
| `prof`  | `prof123`  | COLABORADOR (docente D0001, CCET) |
| `aluno` | `aluno123` | USUARIO (discente A0001, Eng. de Software) |

Docentes e discentes cadastrados pela API entram com **login = matrícula** e **senha = matrícula** (ou a `senhaInicial` informada no cadastro).

**Swagger (documentação interativa):** http://localhost:8080/swagger-ui.html. Clique em *Authorize* e informe o token JWT e a `X-Frontend-Key`.

**Teste automático de ponta a ponta** (34 verificações, precisa de `curl` e `jq`, banco recém-criado): `./scripts/teste-api.sh`

### Configuração (variáveis de ambiente)

| Variável | Padrão | Para que serve |
|---|---|---|
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | `jdbc:postgresql://localhost:5432/monitoria` / `monitoria` / `monitoria` | Conexão com o banco |
| `FRONTEND_ORIGIN` | `http://localhost:4200` | Única origem aceita pelo CORS |
| `FRONTEND_KEY` | `monitoria-frontend-dev-key` | Chave que o proxy do front envia (vazia = desligado) |
| `JWT_SECRET` | (valor de dev) | Segredo do token. **Troque em produção** (mínimo de 32 caracteres) |
| `ADMIN_LOGIN` / `ADMIN_PASSWORD` | `admin` / `admin123` | Administrador inicial |
| `SEED_DEMO_USERS` | `true` | Cria os usuários `prof` e `aluno` |

## Restrições arquiteturais: como foram atendidas

| Restrição | Implementação |
|---|---|
| 1.2 Java + Spring | Spring Boot 3.3 (Web, Data JPA, Security, Validation) |
| 1.3 Spring Security | `SecurityConfig` + JWT (`JwtService`, `JwtAuthenticationFilter`) + `@PreAuthorize` por perfil |
| 1.3.1 Security com banco | `Usuario implements UserDetails`, carregado pelo `UserDetailsService` via JPA; senhas com BCrypt |
| 1.3.2 Só o front acessa o back | 3 camadas: **(a)** CORS libera apenas `FRONTEND_ORIGIN`; **(b)** `FrontendOnlyFilter` barra com 403 qualquer `/api/**` sem o header `X-Frontend-Key`, que é injetado pelo **proxy** do front, e não pelo navegador; **(c)** em produção, o back não expõe porta e só o container do front o enxerga |
| 1.3.3 / 1.4 PostgreSQL só via back | Apenas o back conhece as credenciais; não exponha a porta 5432 em produção |

## Integração com o front-end (Angular)

1. Copie `docs/proxy.conf.json` para a raiz do projeto Angular e rode `ng serve --proxy-config proxy.conf.json`. O proxy injeta a `X-Frontend-Key` e evita problemas de CORS.
2. No Angular, chame sempre caminhos relativos (`/api/...`).
3. Login: `POST /api/auth/login` com `{"login":"admin","senha":"admin123"}`. Guarde o `token` e envie `Authorization: Bearer <token>` com um `HttpInterceptor`.
4. Use `GET /api/auth/me` para saber os perfis (`ADMINISTRADOR`, `COLABORADOR`, `USUARIO`) e montar o menu. Um usuário pode ter mais de um perfil.
5. PDFs: faça a requisição com `responseType: 'blob'` e abra com `URL.createObjectURL(blob)`.

**Erros** vêm sempre no formato `{status, erro, mensagem, campos?}`:

| Código | Significado |
|---|---|
| `400` | Validação (detalhe por campo em `campos`) |
| `401` | Não logado |
| `403` | Sem permissão |
| `404` | Não encontrado |
| `409` | Violação de restrição do banco |
| `422` | Regra de negócio. Mostre `mensagem` ao usuário |

## Endpoints

Todas as rotas começam com `/api`. "Inativar" e "ativar" recebem `{"motivo": "..."}` (obrigatório) e gravam o histórico.

| Recurso | Rotas | Quem pode |
|---|---|---|
| Auth | `POST /auth/login`, `GET /auth/me`, `PUT /auth/senha` | público / logado |
| IES | `GET/POST /ies`, `GET/PUT/DELETE /ies/{id}`, `PATCH /ies/{id}/inativar\|ativar` | leitura: logado; escrita: ADMIN |
| Centros Acadêmicos | `/centros-academicos` (mesmo padrão; filtro `?iesId=`) | leitura: logado; escrita: ADMIN |
| Cursos | `/cursos` (mesmo padrão; filtro `?centroAcademicoId=`) | leitura: logado; escrita: ADMIN |
| Docentes | `/docentes` (sem DELETE; filtro `?status=ATIVO\|INATIVO`) | ADMIN |
| Discentes | `/discentes` (sem DELETE) | ADMIN, COLABORADOR |
| Disciplinas | `/disciplinas` (colaborador vê/edita só as suas) | ADMIN, COLABORADOR |
| Monitorias | `/monitorias` (sem DELETE) | leitura: cada um vê as suas; escrita: COLABORADOR dono / ADMIN |
| Relatório da monitoria | `/relatorios-monitoria`, `GET /relatorios-monitoria/por-monitoria/{id}` | escrita: discente da monitoria; leitura: discente, docente responsável, ADMIN |
| Usuários e perfis | `GET/POST /usuarios`, `PUT /usuarios/{id}/perfis`, `PATCH /usuarios/{id}/ativar\|inativar` | ADMIN |
| Auditoria | `GET /auditoria?usuario=&page=&size=` | ADMIN |
| Histórico de status | `GET /historico/{ENTIDADE}/{id}` | ADMIN, COLABORADOR |
| PDF 5.1 | `GET /relatorios/estrutura-academica` | ADMIN |
| PDF 5.2 | `GET /relatorios/docentes?status=` | ADMIN |
| PDF 9.1 | `GET /relatorios/minhas-monitorias` | COLABORADOR |
| PDF 11.1 | `GET /relatorios/monitorias/{monitoriaId}/final` | discente, docente responsável, ADMIN |

### Exemplo: cadastrar uma monitoria (colaborador)

```json
POST /api/monitorias
{
  "discenteId": 1,
  "disciplinaId": 1,
  "diasSemana": ["MONDAY", "WEDNESDAY"],
  "horaInicio": "14:00",
  "horaFim": "14:50",
  "dataInicio": "2026-08-03",
  "dataFim": "2026-12-10"
}
```

O docente é preenchido automaticamente pelo usuário logado, e o semestre (`2026.2`) é calculado pela data inicial.

## Regras de negócio implementadas

- **Não excluir com histórico:** IES, CA, Curso e Disciplina só são excluídos se não tiverem vínculos nem histórico; caso contrário, retorna 422 pedindo para inativar. Docente, Discente e Monitoria só podem ser inativados.
- **Inativação com motivo** registrada em `historico_status` (quem, quando, de/para, motivo).
- **Login acompanha o cadastro:** docente ou discente inativo perde o login (exceto se acumular outro perfil, como ADMIN).
- **1 reitor por IES e 1 coordenador por CA:** são campos únicos. Pró-reitorias são uma lista com nomeado e data.
- **Vínculos obrigatórios:**
  - todo CA tem 1 IES
  - todo Curso tem 1 CA
  - todo Docente tem 1 CA
  - todo Discente tem 1 Curso
  - toda Disciplina tem 1 Curso
- **Cadastros inativos não recebem vínculos novos:** não é possível vincular um registro novo a IES, CA ou Curso inativo.
- **Monitoria:**
  - no máximo **50 minutos** por dia
  - data final maior que a inicial, dentro do **mesmo semestre**
  - o discente tem **uma única monitoria ativa por semestre**
  - a disciplina precisa ser do próprio docente
  - discente e disciplina precisam estar ativos
- **Relatório da monitoria:** um por monitoria, e somente o discente da monitoria cadastra ou edita.
- **Auditoria:** toda requisição a `/api/**` (inclusive login e acessos negados) é gravada em `log_auditoria`.

## Estrutura do código

```
src/main/java/br/ucsal/monitoria
├── config/      SecurityConfig, CORS, Swagger, DataSeeder
├── security/    JWT, filtro de front-end, UsuarioLogado
├── audit/       interceptor de auditoria
├── entity/      entidades JPA
├── repository/  Spring Data JPA
├── dto/         records de entrada (Request) e saída (Response)
├── service/     regras de negócio
├── report/      geração de PDF (OpenPDF)
├── controller/  endpoints REST
└── exception/   erros padronizados
```

Para criar um cadastro novo, siga o fluxo **Entity → Repository → Dto → Service → Controller** (use `Ies*` como modelo) e crie uma migration `V3__...sql` para a tabela. Nunca edite uma migration que já rodou.

## Pontos a confirmar com o professor

O cenário pede para levantar ambiguidades. Estas são as decisões tomadas:

1. **"Discente único no semestre":** foi implementado como uma monitoria ativa por discente por semestre.
2. **Reitor, pró-reitores e coordenadores:** foram guardados como nomes, e não como usuários do sistema.
3. **Login do discente:** é criado automaticamente quando o colaborador o cadastra (login e senha = matrícula).
4. **"Frequência dos alunos":** é uma lista de (data, aluno, matrícula, presente).
5. **Semestres:** uma monitoria não atravessa semestres (jan–jun = `.1`, jul–dez = `.2`).
