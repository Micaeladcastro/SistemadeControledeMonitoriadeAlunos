package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.CentroAcademicoDto;
import br.ucsal.monitoria.entity.CentroAcademico;
import br.ucsal.monitoria.entity.Ies;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.CentroAcademicoRepository;
import br.ucsal.monitoria.repository.CursoRepository;
import br.ucsal.monitoria.repository.DocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CentroAcademicoService {

    private final CentroAcademicoRepository repository;
    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;
    private final IesService iesService;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<CentroAcademicoDto.Response> listar(Long iesId) {
        var lista = iesId == null ? repository.findAllByOrderByNomeAsc() : repository.findByIesIdOrderByNomeAsc(iesId);
        return lista.stream().map(CentroAcademicoDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public CentroAcademicoDto.Response buscar(Long id) {
        return CentroAcademicoDto.Response.from(buscarEntidade(id));
    }

    public CentroAcademico buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Centro Academico", id));
    }

    @Transactional
    public CentroAcademicoDto.Response criar(CentroAcademicoDto.Request req) {
        Ies ies = iesAtiva(req.iesId());
        if (repository.existsByIesIdAndSiglaIgnoreCase(ies.getId(), req.sigla())) {
            throw new RegraNegocioException("Ja existe um CA com a sigla " + req.sigla() + " nesta IES");
        }
        CentroAcademico ca = new CentroAcademico();
        preencher(ca, req, ies);
        return CentroAcademicoDto.Response.from(repository.save(ca));
    }

    @Transactional
    public CentroAcademicoDto.Response atualizar(Long id, CentroAcademicoDto.Request req) {
        CentroAcademico ca = buscarEntidade(id);
        Ies ies = ca.getIes().getId().equals(req.iesId()) ? ca.getIes() : iesAtiva(req.iesId());
        if (repository.existsByIesIdAndSiglaIgnoreCaseAndIdNot(ies.getId(), req.sigla(), id)) {
            throw new RegraNegocioException("Ja existe um CA com a sigla " + req.sigla() + " nesta IES");
        }
        preencher(ca, req, ies);
        return CentroAcademicoDto.Response.from(ca);
    }

    @Transactional
    public CentroAcademicoDto.Response alterarStatus(Long id, Status novo, String motivo) {
        CentroAcademico ca = buscarEntidade(id);
        if (ca.getStatus() == novo) {
            throw new RegraNegocioException("O Centro Academico ja esta " + novo);
        }
        if (novo == Status.ATIVO && ca.getIes().getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("Nao e possivel ativar um CA de uma IES inativa");
        }
        historicoService.registrar(HistoricoService.CENTRO_ACADEMICO, id, ca.getStatus(), novo, motivo);
        ca.setStatus(novo);
        return CentroAcademicoDto.Response.from(ca);
    }

    @Transactional
    public void excluir(Long id) {
        CentroAcademico ca = buscarEntidade(id);
        if (cursoRepository.existsByCentroAcademicoId(id) || docenteRepository.existsByCentroAcademicoId(id)
                || historicoService.possuiHistorico(HistoricoService.CENTRO_ACADEMICO, id)) {
            throw new RegraNegocioException("O Centro Academico possui historico vinculado e nao pode ser excluido. Inative-o.");
        }
        repository.delete(ca);
    }

    private Ies iesAtiva(Long iesId) {
        Ies ies = iesService.buscarEntidade(iesId);
        if (ies.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("A IES " + ies.getSigla() + " esta inativa");
        }
        return ies;
    }

    private void preencher(CentroAcademico ca, CentroAcademicoDto.Request req, Ies ies) {
        ca.setSigla(req.sigla().trim());
        ca.setNome(req.nome().trim());
        ca.setEmail(req.email().trim());
        ca.setTelefone(req.telefone().trim());
        ca.setCoordenador(req.coordenador().trim());
        ca.setIes(ies);
    }
}
