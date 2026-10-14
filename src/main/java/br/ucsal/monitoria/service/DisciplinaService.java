package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.DisciplinaDto;
import br.ucsal.monitoria.entity.Curso;
import br.ucsal.monitoria.entity.Disciplina;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.AcessoNegadoException;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.DisciplinaRepository;
import br.ucsal.monitoria.repository.MonitoriaRepository;
import br.ucsal.monitoria.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Disciplinas que o docente (colaborador) leciona. Cada docente so mexe nas suas. */
@Service
@RequiredArgsConstructor
public class DisciplinaService {

    private final DisciplinaRepository repository;
    private final MonitoriaRepository monitoriaRepository;
    private final CursoService cursoService;
    private final HistoricoService historicoService;
    private final UsuarioLogado usuarioLogado;

    /** Administrador ve todas; colaborador ve apenas as que leciona. */
    @Transactional(readOnly = true)
    public List<DisciplinaDto.Response> listar() {
        var lista = usuarioLogado.isAdmin() ? repository.findAllByOrderByDescricaoAsc()
                : repository.findByDocenteIdOrderByDescricaoAsc(usuarioLogado.docente().getId());
        return lista.stream().map(DisciplinaDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public DisciplinaDto.Response buscar(Long id) {
        return DisciplinaDto.Response.from(buscarComPermissao(id));
    }

    @Transactional
    public DisciplinaDto.Response criar(DisciplinaDto.Request req) {
        Curso curso = cursoService.buscarAtivo(req.cursoId());
        if (repository.existsByCursoIdAndSiglaIgnoreCase(curso.getId(), req.sigla())) {
            throw new RegraNegocioException("Ja existe uma disciplina com a sigla " + req.sigla() + " neste curso");
        }
        Disciplina d = new Disciplina();
        d.setDocente(usuarioLogado.docente()); // preenchido automaticamente
        preencher(d, req, curso);
        return DisciplinaDto.Response.from(repository.save(d));
    }

    @Transactional
    public DisciplinaDto.Response atualizar(Long id, DisciplinaDto.Request req) {
        Disciplina d = buscarComPermissao(id);
        Curso curso = d.getCurso().getId().equals(req.cursoId()) ? d.getCurso() : cursoService.buscarAtivo(req.cursoId());
        if (repository.existsByCursoIdAndSiglaIgnoreCaseAndIdNot(curso.getId(), req.sigla(), id)) {
            throw new RegraNegocioException("Ja existe uma disciplina com a sigla " + req.sigla() + " neste curso");
        }
        preencher(d, req, curso);
        return DisciplinaDto.Response.from(d);
    }

    @Transactional
    public DisciplinaDto.Response alterarStatus(Long id, Status novo, String motivo) {
        Disciplina d = buscarComPermissao(id);
        if (d.getStatus() == novo) {
            throw new RegraNegocioException("A disciplina ja esta " + novo);
        }
        historicoService.registrar(HistoricoService.DISCIPLINA, id, d.getStatus(), novo, motivo);
        d.setStatus(novo);
        return DisciplinaDto.Response.from(d);
    }

    @Transactional
    public void excluir(Long id) {
        Disciplina d = buscarComPermissao(id);
        if (monitoriaRepository.existsByDisciplinaId(id) || historicoService.possuiHistorico(HistoricoService.DISCIPLINA, id)) {
            throw new RegraNegocioException("A disciplina possui historico vinculado e nao pode ser excluida. Inative-a.");
        }
        repository.delete(d);
    }

    public Disciplina buscarComPermissao(Long id) {
        Disciplina d = repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Disciplina", id));
        if (!usuarioLogado.isAdmin() && !d.getDocente().getId().equals(usuarioLogado.docente().getId())) {
            throw new AcessoNegadoException("Esta disciplina pertence a outro docente");
        }
        return d;
    }

    private void preencher(Disciplina d, DisciplinaDto.Request req, Curso curso) {
        d.setSigla(req.sigla().trim());
        d.setDescricao(req.descricao().trim());
        d.setCargaHoraria(req.cargaHoraria());
        d.setCurso(curso);
    }
}
