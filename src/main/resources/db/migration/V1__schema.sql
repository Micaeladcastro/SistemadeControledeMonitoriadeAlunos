-- =========================================================
-- Seguranca (Spring Security com banco de dados)
-- =========================================================
CREATE TABLE usuario (
    id              BIGSERIAL PRIMARY KEY,
    login           VARCHAR(60)  NOT NULL UNIQUE,
    senha           VARCHAR(100) NOT NULL,
    nome            VARCHAR(150) NOT NULL,
    ativo           BOOLEAN      NOT NULL DEFAULT TRUE,
    data_cadastro   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Um usuario pode acumular mais de um perfil
CREATE TABLE usuario_perfil (
    usuario_id  BIGINT      NOT NULL REFERENCES usuario(id),
    perfil      VARCHAR(20) NOT NULL,
    PRIMARY KEY (usuario_id, perfil)
);

-- =========================================================
-- Estrutura academica
-- =========================================================
CREATE TABLE ies (
    id              BIGSERIAL PRIMARY KEY,
    sigla           VARCHAR(20)  NOT NULL UNIQUE,
    nome            VARCHAR(200) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    telefone        VARCHAR(30)  NOT NULL,
    reitor          VARCHAR(150) NOT NULL,   -- apenas 1 reitor por IES
    data_cadastro   TIMESTAMP    NOT NULL DEFAULT NOW(),
    status          VARCHAR(10)  NOT NULL
);

CREATE TABLE pro_reitoria (
    id              BIGSERIAL PRIMARY KEY,
    ies_id          BIGINT       NOT NULL REFERENCES ies(id),
    nome            VARCHAR(150) NOT NULL,   -- ex.: Pro-Reitoria de Graduacao
    nomeado         VARCHAR(150) NOT NULL,   -- pro-reitor(a) nomeado(a)
    data_nomeacao   DATE
);

CREATE TABLE centro_academico (
    id              BIGSERIAL PRIMARY KEY,
    sigla           VARCHAR(20)  NOT NULL,
    nome            VARCHAR(200) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    telefone        VARCHAR(30)  NOT NULL,
    coordenador     VARCHAR(150) NOT NULL,   -- apenas 1 coordenador por CA
    ies_id          BIGINT       NOT NULL REFERENCES ies(id),
    data_cadastro   TIMESTAMP    NOT NULL DEFAULT NOW(),
    status          VARCHAR(10)  NOT NULL,
    UNIQUE (ies_id, sigla)
);

CREATE TABLE curso (
    id                  BIGSERIAL PRIMARY KEY,
    sigla               VARCHAR(20)  NOT NULL,
    descricao           VARCHAR(200) NOT NULL,
    coordenador         VARCHAR(150) NOT NULL,
    centro_academico_id BIGINT       NOT NULL REFERENCES centro_academico(id),
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT NOW(),
    status              VARCHAR(10)  NOT NULL,
    UNIQUE (centro_academico_id, sigla)
);

CREATE TABLE curso_turno (
    curso_id    BIGINT      NOT NULL REFERENCES curso(id),
    turno       VARCHAR(20) NOT NULL,
    PRIMARY KEY (curso_id, turno)
);

-- =========================================================
-- Pessoas
-- =========================================================
CREATE TABLE docente (
    id                  BIGSERIAL PRIMARY KEY,
    matricula           VARCHAR(30)  NOT NULL UNIQUE,
    nome                VARCHAR(150) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    telefone            VARCHAR(30)  NOT NULL,
    centro_academico_id BIGINT       NOT NULL REFERENCES centro_academico(id),
    usuario_id          BIGINT       NOT NULL UNIQUE REFERENCES usuario(id),
    data_cadastro       TIMESTAMP    NOT NULL DEFAULT NOW(),
    status              VARCHAR(10)  NOT NULL
);

CREATE TABLE discente (
    id              BIGSERIAL PRIMARY KEY,
    matricula       VARCHAR(30)  NOT NULL UNIQUE,
    nome            VARCHAR(150) NOT NULL,
    email           VARCHAR(150) NOT NULL,
    telefone        VARCHAR(30)  NOT NULL,
    curso_id        BIGINT       NOT NULL REFERENCES curso(id),
    usuario_id      BIGINT       NOT NULL UNIQUE REFERENCES usuario(id),
    data_cadastro   TIMESTAMP    NOT NULL DEFAULT NOW(),
    status          VARCHAR(10)  NOT NULL
);

-- =========================================================
-- Monitoria
-- =========================================================
CREATE TABLE disciplina (
    id              BIGSERIAL PRIMARY KEY,
    sigla           VARCHAR(20)  NOT NULL,
    descricao       VARCHAR(200) NOT NULL,
    carga_horaria   INTEGER      NOT NULL CHECK (carga_horaria > 0),
    curso_id        BIGINT       NOT NULL REFERENCES curso(id),
    docente_id      BIGINT       NOT NULL REFERENCES docente(id),  -- docente que leciona
    data_cadastro   TIMESTAMP    NOT NULL DEFAULT NOW(),
    status          VARCHAR(10)  NOT NULL,
    UNIQUE (curso_id, sigla)
);

CREATE TABLE monitoria (
    id              BIGSERIAL PRIMARY KEY,
    docente_id      BIGINT      NOT NULL REFERENCES docente(id),
    discente_id     BIGINT      NOT NULL REFERENCES discente(id),
    disciplina_id   BIGINT      NOT NULL REFERENCES disciplina(id),
    semestre        VARCHAR(6)  NOT NULL,   -- ex.: 2026.2
    hora_inicio     TIME        NOT NULL,
    hora_fim        TIME        NOT NULL,
    data_inicio     DATE        NOT NULL,
    data_fim        DATE        NOT NULL,
    data_cadastro   TIMESTAMP   NOT NULL DEFAULT NOW(),
    status          VARCHAR(10) NOT NULL,
    CHECK (hora_fim > hora_inicio),
    CHECK (data_fim > data_inicio)
);

CREATE TABLE monitoria_dia_semana (
    monitoria_id    BIGINT      NOT NULL REFERENCES monitoria(id),
    dia_semana      VARCHAR(10) NOT NULL,
    PRIMARY KEY (monitoria_id, dia_semana)
);

CREATE TABLE relatorio_monitoria (
    id                      BIGSERIAL PRIMARY KEY,
    monitoria_id            BIGINT       NOT NULL UNIQUE REFERENCES monitoria(id),
    tipo                    VARCHAR(12)  NOT NULL,   -- PRESENCIAL / REMOTO
    local_atuacao           VARCHAR(200) NOT NULL,
    ocorrencias             TEXT,
    consideracoes_finais    TEXT,
    data_cadastro           TIMESTAMP    NOT NULL DEFAULT NOW(),
    status                  VARCHAR(10)  NOT NULL
);

-- Frequencia dos alunos que participaram da monitoria
CREATE TABLE relatorio_frequencia (
    relatorio_id    BIGINT       NOT NULL REFERENCES relatorio_monitoria(id),
    data            DATE         NOT NULL,
    aluno_nome      VARCHAR(150) NOT NULL,
    aluno_matricula VARCHAR(30),
    presente        BOOLEAN      NOT NULL
);

-- =========================================================
-- Historico (ativacao/inativacao com motivo) e auditoria
-- =========================================================
CREATE TABLE historico_status (
    id              BIGSERIAL PRIMARY KEY,
    entidade        VARCHAR(40)  NOT NULL,
    entidade_id     BIGINT       NOT NULL,
    status_anterior VARCHAR(10),
    status_novo     VARCHAR(10)  NOT NULL,
    motivo          VARCHAR(500) NOT NULL,
    usuario_login   VARCHAR(60),
    data_hora       TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_historico_entidade ON historico_status(entidade, entidade_id);

CREATE TABLE log_auditoria (
    id              BIGSERIAL PRIMARY KEY,
    usuario_login   VARCHAR(60),
    metodo          VARCHAR(10)  NOT NULL,
    uri             VARCHAR(500) NOT NULL,
    acao            VARCHAR(200),
    status_http     INTEGER      NOT NULL,
    ip              VARCHAR(60),
    data_hora       TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_log_data ON log_auditoria(data_hora);
