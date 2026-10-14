#!/usr/bin/env bash
# Teste de ponta a ponta da API. Uso: ./scripts/teste-api.sh   (com o back-end rodando em localhost:8080)
# Requer: curl e jq. Roda sobre um banco recem-criado (dados do Flyway + usuarios de demonstracao).
set -u
API=${API:-http://localhost:8080/api}
KEY=${FRONTEND_KEY:-monitoria-frontend-dev-key}
PASSOU=0; FALHOU=0

req() { # metodo url token [json] -> imprime "status corpo"
  local m=$1 u=$2 t=$3 d=${4:-}
  local args=(-s -o /tmp/resp.json -w "%{http_code}" -X "$m" "$API$u" -H "X-Frontend-Key: $KEY" -H "Content-Type: application/json")
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$d" ] && args+=(-d "$d")
  curl "${args[@]}"
}
espera() { # descricao esperado obtido
  if [ "$2" = "$3" ]; then echo "  OK   $1 ($3)"; PASSOU=$((PASSOU+1));
  else echo "  FALHA $1: esperado $2, obtido $3 -> $(head -c 300 /tmp/resp.json)"; FALHOU=$((FALHOU+1)); fi
}
login() { req POST /auth/login "" "{\"login\":\"$1\",\"senha\":\"$2\"}" >/dev/null; jq -r .token /tmp/resp.json; }

echo "== Seguranca"
s=$(curl -s -o /tmp/resp.json -w "%{http_code}" "$API/ies"); espera "sem X-Frontend-Key e barrado" 403 "$s"
s=$(req GET /ies ""); espera "sem token pede login" 401 "$s"
s=$(req POST /auth/login "" '{"login":"admin","senha":"errada"}'); espera "senha errada" 401 "$s"
ADMIN=$(login admin admin123); PROF=$(login prof prof123); ALUNO=$(login aluno aluno123)
[ "$ADMIN" != "null" ] && echo "  OK   login admin/prof/aluno" || echo "  FALHA login"
s=$(req POST /ies "$PROF" '{}'); espera "colaborador nao cadastra IES" 403 "$s"

echo "== Administrador: IES / CA / Curso"
s=$(req POST /ies "$ADMIN" '{"sigla":"UCB","nome":"Universidade Catolica de Brasilia","email":"contato@ucb.br","telefone":"(61) 3356-9000","reitor":"Reitor UCB","proReitorias":[{"nome":"Pro-Reitoria de Graduacao","nomeado":"Fulana","dataNomeacao":"2025-02-01"}]}')
espera "cadastra IES" 201 "$s"; UCB=$(jq .id /tmp/resp.json)
s=$(req POST /ies "$ADMIN" '{"sigla":"ucb","nome":"x","email":"a@b.com","telefone":"1","reitor":"r"}'); espera "sigla duplicada" 422 "$s"
s=$(req POST /ies "$ADMIN" '{"sigla":"","nome":"x","email":"invalido","telefone":"1","reitor":"r"}'); espera "validacao de campos" 400 "$s"
s=$(req POST /centros-academicos "$ADMIN" "{\"sigla\":\"CTCH\",\"nome\":\"Centro de Teologia\",\"email\":\"ctch@ucb.br\",\"telefone\":\"1\",\"coordenador\":\"Coord\",\"iesId\":$UCB}")
espera "cadastra CA" 201 "$s"; CA=$(jq .id /tmp/resp.json)
s=$(req DELETE /ies/$UCB "$ADMIN"); espera "nao exclui IES com CA vinculado" 422 "$s"
s=$(req POST /cursos "$ADMIN" "{\"sigla\":\"TEO\",\"descricao\":\"Teologia\",\"centroAcademicoId\":$CA,\"turnos\":[\"NOTURNO\"],\"coordenador\":\"Coord TEO\"}")
espera "cadastra curso" 201 "$s"; CURSO=$(jq .id /tmp/resp.json)
s=$(req PATCH /cursos/$CURSO/inativar "$ADMIN" '{}'); espera "inativar exige motivo" 400 "$s"
s=$(req PATCH /cursos/$CURSO/inativar "$ADMIN" '{"motivo":"Curso nao sera mais oferecido"}'); espera "inativa curso" 200 "$s"
s=$(req DELETE /cursos/$CURSO "$ADMIN"); espera "nao exclui curso com historico" 422 "$s"
s=$(req GET /historico/CURSO/$CURSO "$ADMIN"); espera "historico do curso" 200 "$s"

echo "== Administrador: Docentes"
s=$(req POST /docentes "$ADMIN" "{\"matricula\":\"D0002\",\"nome\":\"Maria Docente\",\"email\":\"maria@ucb.br\",\"telefone\":\"1\",\"centroAcademicoId\":$CA,\"senhaInicial\":\"maria123\"}")
espera "cadastra docente" 201 "$s"; DOC2=$(jq .id /tmp/resp.json)
MARIA=$(login D0002 maria123); [ "$MARIA" != "null" ] && echo "  OK   docente faz login com a matricula" || echo "  FALHA login docente"
s=$(req PATCH /docentes/$DOC2/inativar "$ADMIN" '{"motivo":"Afastamento"}'); espera "inativa docente" 200 "$s"
s=$(req POST /auth/login "" '{"login":"D0002","senha":"maria123"}'); espera "docente inativo nao loga" 401 "$s"

echo "== Colaborador: Disciplina / Discente / Monitoria"
ES=$(req GET "/cursos" "$PROF" >/dev/null; jq '.[] | select(.sigla=="ES") | .id' /tmp/resp.json)
s=$(req POST /disciplinas "$PROF" "{\"sigla\":\"POO\",\"descricao\":\"Programacao Orientada a Objetos\",\"cursoId\":$ES,\"cargaHoraria\":60}")
espera "cadastra disciplina" 201 "$s"; DISC=$(jq .id /tmp/resp.json)
s=$(req POST /discentes "$PROF" "{\"matricula\":\"A0002\",\"nome\":\"Joao Discente\",\"email\":\"joao@ucsal.edu.br\",\"telefone\":\"1\",\"cursoId\":$ES}")
espera "cadastra discente" 201 "$s"
ALUNO_ID=$(req GET /auth/me "$ALUNO" >/dev/null; jq .discenteId /tmp/resp.json)
M='"diasSemana":["MONDAY","WEDNESDAY"],"dataInicio":"2026-08-03","dataFim":"2026-12-10"'
s=$(req POST /monitorias "$PROF" "{\"discenteId\":$ALUNO_ID,\"disciplinaId\":$DISC,\"horaInicio\":\"14:00\",\"horaFim\":\"15:00\",$M}"); espera "monitoria > 50 min e recusada" 422 "$s"
s=$(req POST /monitorias "$PROF" "{\"discenteId\":$ALUNO_ID,\"disciplinaId\":$DISC,\"horaInicio\":\"14:00\",\"horaFim\":\"14:50\",\"diasSemana\":[\"MONDAY\"],\"dataInicio\":\"2026-05-01\",\"dataFim\":\"2026-08-10\"}"); espera "monitoria entre semestres e recusada" 422 "$s"
s=$(req POST /monitorias "$PROF" "{\"discenteId\":$ALUNO_ID,\"disciplinaId\":$DISC,\"horaInicio\":\"14:00\",\"horaFim\":\"14:50\",$M}"); espera "cadastra monitoria" 201 "$s"
MON=$(jq .id /tmp/resp.json); echo "       semestre=$(jq -r .semestre /tmp/resp.json) docente=$(jq -r .docenteNome /tmp/resp.json) situacao=$(jq -r .situacao /tmp/resp.json)"
s=$(req POST /monitorias "$PROF" "{\"discenteId\":$ALUNO_ID,\"disciplinaId\":$DISC,\"horaInicio\":\"10:00\",\"horaFim\":\"10:50\",$M}"); espera "discente unico no semestre" 422 "$s"
s=$(req GET /monitorias "$ALUNO"); espera "discente ve as suas monitorias" 200 "$s"; echo "       qtd=$(jq length /tmp/resp.json)"

echo "== Usuario (discente): Relatorio da monitoria"
R="{\"monitoriaId\":$MON,\"tipo\":\"PRESENCIAL\",\"localAtuacao\":\"Laboratorio 3\",\"ocorrencias\":\"Duvidas sobre heranca\",\"frequencia\":[{\"data\":\"2026-08-03\",\"alunoNome\":\"Ana\",\"alunoMatricula\":\"A9\",\"presente\":true},{\"data\":\"2026-08-03\",\"alunoNome\":\"Beto\",\"presente\":false}],\"consideracoesFinais\":\"Boa participacao\"}"
s=$(req POST /relatorios-monitoria "$PROF" "$R"); espera "colaborador nao cadastra relatorio" 403 "$s"
s=$(req POST /relatorios-monitoria "$ALUNO" "$R"); espera "discente cadastra relatorio" 201 "$s"; REL=$(jq .id /tmp/resp.json)
s=$(req PUT /relatorios-monitoria/$REL "$ALUNO" "${R/Laboratorio 3/Sala 101}"); espera "discente edita relatorio" 200 "$s"

echo "== Relatorios PDF"
pdf() { curl -s -o /tmp/r.pdf -w "%{http_code}" "$API$1" -H "X-Frontend-Key: $KEY" -H "Authorization: Bearer $2"; }
s=$(pdf /relatorios/estrutura-academica "$ADMIN"); espera "PDF estrutura academica (admin)" 200 "$s"
s=$(pdf /relatorios/docentes "$ADMIN"); espera "PDF docentes (admin)" 200 "$s"
s=$(pdf /relatorios/minhas-monitorias "$PROF"); espera "PDF monitorias (colaborador)" 200 "$s"
s=$(pdf /relatorios/monitorias/$MON/final "$ALUNO"); espera "PDF relatorio final (discente)" 200 "$s"
s=$(pdf /relatorios/estrutura-academica "$ALUNO"); espera "discente nao emite relatorio do admin" 403 "$s"

echo "== Auditoria"
s=$(req GET "/auditoria?size=5" "$ADMIN"); espera "admin consulta logs" 200 "$s"; echo "       total de acoes registradas=$(jq .totalElements /tmp/resp.json)"
s=$(req GET /auditoria "$PROF"); espera "colaborador nao ve logs" 403 "$s"

echo; echo "Resultado: $PASSOU ok, $FALHOU falha(s)"; [ $FALHOU -eq 0 ]
