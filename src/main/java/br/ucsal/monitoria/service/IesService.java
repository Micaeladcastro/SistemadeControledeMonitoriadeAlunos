package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.IesDto;
import br.ucsal.monitoria.entity.Ies;
import br.ucsal.monitoria.entity.ProReitoria;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.CentroAcademicoRepository;
import br.ucsal.monitoria.repository.IesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IesService {

    private final IesRepository repository;
    private final CentroAcademicoRepository centroAcademicoRepository;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<IesDto.Response> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(IesDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public IesDto.Response buscar(Long id) {
        return IesDto.Response.from(buscarEntidade(id));
    }

    public Ies buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("IES", id));
    }

    @Transactional
    public IesDto.Response criar(IesDto.Request req) {
        if (repository.existsBySiglaIgnoreCase(req.sigla())) {
            throw new RegraNegocioException("Ja existe uma IES com a sigla " + req.sigla());
        }
        Ies ies = new Ies();
        preencher(ies, req);
        return IesDto.Response.from(repository.save(ies));
    }

    @Transactional
    public IesDto.Response atualizar(Long id, IesDto.Request req) {
        Ies ies = buscarEntidade(id);
        if (repository.existsBySiglaIgnoreCaseAndIdNot(req.sigla(), id)) {
            throw new RegraNegocioException("Ja existe uma IES com a sigla " + req.sigla());
        }
        preencher(ies, req);
        return IesDto.Response.from(ies);
    }

    @Transactional
    public IesDto.Response alterarStatus(Long id, Status novo, String motivo) {
        Ies ies = buscarEntidade(id);
        if (ies.getStatus() == novo) {
            throw new RegraNegocioException("A IES ja esta " + novo);
        }
        historicoService.registrar(HistoricoService.IES, id, ies.getStatus(), novo, motivo);
        ies.setStatus(novo);
        return IesDto.Response.from(ies);
    }

    /** So pode excluir se nao houver nenhum historico vinculado (CAs ou alteracoes de status). */
    @Transactional
    public void excluir(Long id) {
        Ies ies = buscarEntidade(id);
        if (centroAcademicoRepository.existsByIesId(id) || historicoService.possuiHistorico(HistoricoService.IES, id)) {
            throw new RegraNegocioException("A IES possui historico vinculado e nao pode ser excluida. Inative-a.");
        }
        repository.delete(ies);
    }

    private void preencher(Ies ies, IesDto.Request req) {
        ies.setSigla(req.sigla().trim());
        ies.setNome(req.nome().trim());
        ies.setEmail(req.email().trim());
        ies.setTelefone(req.telefone().trim());
        ies.setReitor(req.reitor().trim());
        ies.getProReitorias().clear();
        if (req.proReitorias() != null) {
            req.proReitorias().forEach(p -> {
                ProReitoria pr = new ProReitoria();
                pr.setIes(ies);
                pr.setNome(p.nome().trim());
                pr.setNomeado(p.nomeado().trim());
                pr.setDataNomeacao(p.dataNomeacao());
                ies.getProReitorias().add(pr);
            });
        }
    }
}
