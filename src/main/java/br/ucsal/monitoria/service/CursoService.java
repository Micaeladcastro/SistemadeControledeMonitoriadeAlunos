package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.CursoDto;
import br.ucsal.monitoria.entity.CentroAcademico;
import br.ucsal.monitoria.entity.Curso;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.CursoRepository;
import br.ucsal.monitoria.repository.DiscenteRepository;
import br.ucsal.monitoria.repository.DisciplinaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository repository;
    private final DisciplinaRepository disciplinaRepository;
    private final DiscenteRepository discenteRepository;
    private final CentroAcademicoService centroAcademicoService;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<CursoDto.Response> listar(Long centroAcademicoId) {
        var lista = centroAcademicoId == null ? repository.findAllByOrderByDescricaoAsc()
                : repository.findByCentroAcademicoIdOrderByDescricaoAsc(centroAcademicoId);
        return lista.stream().map(CursoDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public CursoDto.Response buscar(Long id) {
        return CursoDto.Response.from(buscarEntidade(id));
    }

    public Curso buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Curso", id));
    }

    public Curso buscarAtivo(Long id) {
        Curso curso = buscarEntidade(id);
        if (curso.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("O curso " + curso.getSigla() + " esta inativo");
        }
        return curso;
    }

    @Transactional
    public CursoDto.Response criar(CursoDto.Request req) {
        CentroAcademico ca = caAtivo(req.centroAcademicoId());
        if (repository.existsByCentroAcademicoIdAndSiglaIgnoreCase(ca.getId(), req.sigla())) {
            throw new RegraNegocioException("Ja existe um curso com a sigla " + req.sigla() + " neste CA");
        }
        Curso curso = new Curso();
        preencher(curso, req, ca);
        return CursoDto.Response.from(repository.save(curso));
    }

    @Transactional
    public CursoDto.Response atualizar(Long id, CursoDto.Request req) {
        Curso curso = buscarEntidade(id);
        CentroAcademico ca = curso.getCentroAcademico().getId().equals(req.centroAcademicoId())
                ? curso.getCentroAcademico() : caAtivo(req.centroAcademicoId());
        if (repository.existsByCentroAcademicoIdAndSiglaIgnoreCaseAndIdNot(ca.getId(), req.sigla(), id)) {
            throw new RegraNegocioException("Ja existe um curso com a sigla " + req.sigla() + " neste CA");
        }
        preencher(curso, req, ca);
        return CursoDto.Response.from(curso);
    }

    @Transactional
    public CursoDto.Response alterarStatus(Long id, Status novo, String motivo) {
        Curso curso = buscarEntidade(id);
        if (curso.getStatus() == novo) {
            throw new RegraNegocioException("O curso ja esta " + novo);
        }
        if (novo == Status.ATIVO && curso.getCentroAcademico().getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("Nao e possivel ativar um curso de um CA inativo");
        }
        historicoService.registrar(HistoricoService.CURSO, id, curso.getStatus(), novo, motivo);
        curso.setStatus(novo);
        return CursoDto.Response.from(curso);
    }

    @Transactional
    public void excluir(Long id) {
        Curso curso = buscarEntidade(id);
        if (disciplinaRepository.existsByCursoId(id) || discenteRepository.existsByCursoId(id)
                || historicoService.possuiHistorico(HistoricoService.CURSO, id)) {
            throw new RegraNegocioException("O curso possui historico vinculado e nao pode ser excluido. Inative-o.");
        }
        repository.delete(curso);
    }

    private CentroAcademico caAtivo(Long id) {
        CentroAcademico ca = centroAcademicoService.buscarEntidade(id);
        if (ca.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("O Centro Academico " + ca.getSigla() + " esta inativo");
        }
        return ca;
    }

    private void preencher(Curso curso, CursoDto.Request req, CentroAcademico ca) {
        curso.setSigla(req.sigla().trim());
        curso.setDescricao(req.descricao().trim());
        curso.setCoordenador(req.coordenador().trim());
        curso.setCentroAcademico(ca);
        curso.setTurnos(new HashSet<>(req.turnos()));
    }
}
