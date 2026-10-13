package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.MonitoriaDto;
import br.ucsal.monitoria.entity.Discente;
import br.ucsal.monitoria.entity.Disciplina;
import br.ucsal.monitoria.entity.Docente;
import br.ucsal.monitoria.entity.Monitoria;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.AcessoNegadoException;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.MonitoriaRepository;
import br.ucsal.monitoria.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonitoriaService {

    /** Regra 8.1: cada encontro de monitoria tem no maximo 50 minutos. */
    public static final int DURACAO_MAXIMA_MINUTOS = 50;

    private final MonitoriaRepository repository;
    private final DiscenteService discenteService;
    private final DisciplinaService disciplinaService;
    private final HistoricoService historicoService;
    private final UsuarioLogado usuarioLogado;

    /**
     * Administrador ve todas; colaborador ve as que orienta; usuario (discente) ve as suas.
     * Quem acumula perfis ve a uniao.
     */
    @Transactional(readOnly = true)
    public List<MonitoriaDto.Response> listar() {
        return visiveis().stream().map(MonitoriaDto.Response::from).toList();
    }

    public List<Monitoria> visiveis() {
        if (usuarioLogado.isAdmin()) {
            return repository.findAllByOrderByDataInicioDesc();
        }
        Map<Long, Monitoria> resultado = new LinkedHashMap<>();
        usuarioLogado.docenteOpcional().ifPresent(d ->
                repository.findByDocenteIdOrderByDataInicioDesc(d.getId()).forEach(m -> resultado.put(m.getId(), m)));
        usuarioLogado.discenteOpcional().ifPresent(d ->
                repository.findByDiscenteIdOrderByDataInicioDesc(d.getId()).forEach(m -> resultado.put(m.getId(), m)));
        return resultado.values().stream().sorted(Comparator.comparing(Monitoria::getDataInicio).reversed()).toList();
    }

    @Transactional(readOnly = true)
    public MonitoriaDto.Response buscar(Long id) {
        return MonitoriaDto.Response.from(buscarParaVisualizar(id));
    }

    @Transactional
    public MonitoriaDto.Response criar(MonitoriaDto.Request req) {
        Docente docente = usuarioLogado.docente(); // registro automatico do docente logado
        Monitoria m = new Monitoria();
        m.setDocente(docente);
        preencherEValidar(m, req);
        return MonitoriaDto.Response.from(repository.save(m));
    }

    @Transactional
    public MonitoriaDto.Response atualizar(Long id, MonitoriaDto.Request req) {
        Monitoria m = buscarParaAlterar(id);
        if (m.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("Monitoria cancelada nao pode ser editada. Reative-a antes.");
        }
        preencherEValidar(m, req);
        return MonitoriaDto.Response.from(m);
    }

    @Transactional
    public MonitoriaDto.Response alterarStatus(Long id, Status novo, String motivo) {
        Monitoria m = buscarParaAlterar(id);
        if (m.getStatus() == novo) {
            throw new RegraNegocioException("A monitoria ja esta " + novo);
        }
        if (novo == Status.ATIVO) {
            validarDiscenteUnicoNoSemestre(m.getDiscente(), m.getSemestre(), m.getId());
        }
        historicoService.registrar(HistoricoService.MONITORIA, id, m.getStatus(), novo, motivo);
        m.setStatus(novo);
        return MonitoriaDto.Response.from(m);
    }

    /** Pode ver: administrador, o docente responsavel e o proprio discente. */
    public Monitoria buscarParaVisualizar(Long id) {
        Monitoria m = repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Monitoria", id));
        boolean docenteResponsavel = usuarioLogado.docenteOpcional()
                .map(d -> d.getId().equals(m.getDocente().getId())).orElse(false);
        boolean discenteDaMonitoria = usuarioLogado.discenteOpcional()
                .map(d -> d.getId().equals(m.getDiscente().getId())).orElse(false);
        if (!usuarioLogado.isAdmin() && !docenteResponsavel && !discenteDaMonitoria) {
            throw new AcessoNegadoException("Voce nao tem acesso a esta monitoria");
        }
        return m;
    }

    /** Pode alterar: administrador e o docente responsavel. */
    private Monitoria buscarParaAlterar(Long id) {
        Monitoria m = repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Monitoria", id));
        if (!usuarioLogado.isAdmin() && !m.getDocente().getId().equals(usuarioLogado.docente().getId())) {
            throw new AcessoNegadoException("Esta monitoria pertence a outro docente");
        }
        return m;
    }

    private void preencherEValidar(Monitoria m, MonitoriaDto.Request req) {
        // Horario: no maximo 50 minutos por dia
        long minutos = Duration.between(req.horaInicio(), req.horaFim()).toMinutes();
        if (minutos <= 0) {
            throw new RegraNegocioException("A hora final deve ser maior que a hora inicial");
        }
        if (minutos > DURACAO_MAXIMA_MINUTOS) {
            throw new RegraNegocioException("A monitoria pode ter no maximo " + DURACAO_MAXIMA_MINUTOS
                    + " minutos por dia (informado: " + minutos + " minutos)");
        }

        // Periodo: dentro de um unico semestre
        if (!req.dataFim().isAfter(req.dataInicio())) {
            throw new RegraNegocioException("A data de encerramento deve ser posterior a data inicial");
        }
        String semestre = semestreDe(req.dataInicio());
        if (!semestre.equals(semestreDe(req.dataFim()))) {
            throw new RegraNegocioException("A monitoria deve iniciar e encerrar no mesmo semestre (" + semestre + ")");
        }

        Discente discente = discenteService.buscarEntidade(req.discenteId());
        if (discente.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("O discente " + discente.getNome() + " esta inativo");
        }

        Disciplina disciplina = disciplinaService.buscarComPermissao(req.disciplinaId());
        if (disciplina.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("A disciplina " + disciplina.getSigla() + " esta inativa");
        }
        if (!disciplina.getDocente().getId().equals(m.getDocente().getId())) {
            throw new RegraNegocioException("A disciplina deve ser lecionada pelo docente responsavel pela monitoria");
        }

        // Regra 8.3: discente unico no semestre
        validarDiscenteUnicoNoSemestre(discente, semestre, m.getId());

        m.setDiscente(discente);
        m.setDisciplina(disciplina);
        m.setSemestre(semestre);
        m.setDiasSemana(new HashSet<>(req.diasSemana()));
        m.setHoraInicio(req.horaInicio());
        m.setHoraFim(req.horaFim());
        m.setDataInicio(req.dataInicio());
        m.setDataFim(req.dataFim());
    }

    private void validarDiscenteUnicoNoSemestre(Discente discente, String semestre, Long monitoriaId) {
        Long idIgnorado = monitoriaId == null ? -1L : monitoriaId;
        if (repository.existsByDiscenteIdAndSemestreAndStatusAndIdNot(discente.getId(), semestre, Status.ATIVO, idIgnorado)) {
            throw new RegraNegocioException("O discente " + discente.getNome()
                    + " ja possui uma monitoria ativa no semestre " + semestre);
        }
    }

    /** Janeiro a junho = AAAA.1; julho a dezembro = AAAA.2 */
    public static String semestreDe(LocalDate data) {
        return data.getYear() + "." + (data.getMonthValue() <= 6 ? 1 : 2);
    }
}
