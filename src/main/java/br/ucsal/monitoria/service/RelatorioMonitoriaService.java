package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.RelatorioMonitoriaDto;
import br.ucsal.monitoria.entity.Discente;
import br.ucsal.monitoria.entity.Monitoria;
import br.ucsal.monitoria.entity.RelatorioMonitoria;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.AcessoNegadoException;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.RelatorioMonitoriaRepository;
import br.ucsal.monitoria.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Relatorio final da monitoria, preenchido pelo discente (perfil USUARIO). */
@Service
@RequiredArgsConstructor
public class RelatorioMonitoriaService {

    private final RelatorioMonitoriaRepository repository;
    private final MonitoriaService monitoriaService;
    private final HistoricoService historicoService;
    private final UsuarioLogado usuarioLogado;

    @Transactional(readOnly = true)
    public List<RelatorioMonitoriaDto.Response> listar() {
        if (usuarioLogado.isAdmin()) {
            return repository.findAllByOrderByDataCadastroDesc().stream().map(RelatorioMonitoriaDto.Response::from).toList();
        }
        Map<Long, RelatorioMonitoria> resultado = new LinkedHashMap<>();
        usuarioLogado.docenteOpcional().ifPresent(d -> repository.findByMonitoriaDocenteIdOrderByDataCadastroDesc(d.getId())
                .forEach(r -> resultado.put(r.getId(), r)));
        usuarioLogado.discenteOpcional().ifPresent(d -> repository.findByMonitoriaDiscenteIdOrderByDataCadastroDesc(d.getId())
                .forEach(r -> resultado.put(r.getId(), r)));
        return resultado.values().stream().map(RelatorioMonitoriaDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public RelatorioMonitoriaDto.Response buscar(Long id) {
        RelatorioMonitoria r = buscarEntidade(id);
        monitoriaService.buscarParaVisualizar(r.getMonitoria().getId()); // valida acesso
        return RelatorioMonitoriaDto.Response.from(r);
    }

    @Transactional(readOnly = true)
    public RelatorioMonitoriaDto.Response buscarPorMonitoria(Long monitoriaId) {
        monitoriaService.buscarParaVisualizar(monitoriaId); // valida acesso
        return repository.findByMonitoriaId(monitoriaId).map(RelatorioMonitoriaDto.Response::from)
                .orElseThrow(() -> new NaoEncontradoException("Relatorio da monitoria", monitoriaId));
    }

    @Transactional
    public RelatorioMonitoriaDto.Response criar(RelatorioMonitoriaDto.Request req) {
        Discente discente = usuarioLogado.discente();
        Monitoria monitoria = monitoriaService.buscarParaVisualizar(req.monitoriaId());
        if (!monitoria.getDiscente().getId().equals(discente.getId())) {
            throw new AcessoNegadoException("So o discente da monitoria pode cadastrar o relatorio");
        }
        if (monitoria.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("A monitoria esta cancelada");
        }
        if (repository.existsByMonitoriaId(monitoria.getId())) {
            throw new RegraNegocioException("Esta monitoria ja possui relatorio. Edite o relatorio existente.");
        }
        RelatorioMonitoria r = new RelatorioMonitoria();
        r.setMonitoria(monitoria);
        preencher(r, req);
        return RelatorioMonitoriaDto.Response.from(repository.save(r));
    }

    @Transactional
    public RelatorioMonitoriaDto.Response atualizar(Long id, RelatorioMonitoriaDto.Request req) {
        RelatorioMonitoria r = buscarParaAlterar(id);
        if (r.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("Relatorio inativo nao pode ser editado. Reative-o antes.");
        }
        preencher(r, req);
        return RelatorioMonitoriaDto.Response.from(r);
    }

    @Transactional
    public RelatorioMonitoriaDto.Response alterarStatus(Long id, Status novo, String motivo) {
        RelatorioMonitoria r = buscarParaAlterar(id);
        if (r.getStatus() == novo) {
            throw new RegraNegocioException("O relatorio ja esta " + novo);
        }
        historicoService.registrar(HistoricoService.RELATORIO_MONITORIA, id, r.getStatus(), novo, motivo);
        r.setStatus(novo);
        return RelatorioMonitoriaDto.Response.from(r);
    }

    private RelatorioMonitoria buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Relatorio da monitoria", id));
    }

    /** Pode alterar: o proprio discente ou o administrador. */
    private RelatorioMonitoria buscarParaAlterar(Long id) {
        RelatorioMonitoria r = buscarEntidade(id);
        if (!usuarioLogado.isAdmin()
                && !r.getMonitoria().getDiscente().getId().equals(usuarioLogado.discente().getId())) {
            throw new AcessoNegadoException("Este relatorio pertence a outro discente");
        }
        return r;
    }

    private void preencher(RelatorioMonitoria r, RelatorioMonitoriaDto.Request req) {
        r.setTipo(req.tipo());
        r.setLocalAtuacao(req.localAtuacao().trim());
        r.setOcorrencias(req.ocorrencias());
        r.setConsideracoesFinais(req.consideracoesFinais());
        r.getFrequencia().clear();
        if (req.frequencia() != null) {
            req.frequencia().forEach(f -> r.getFrequencia().add(f.toEntity()));
        }
    }
}
