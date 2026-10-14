package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.StatusDto;
import br.ucsal.monitoria.entity.HistoricoStatus;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.repository.HistoricoStatusRepository;
import br.ucsal.monitoria.security.UsuarioLogado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Historico de ativacao/inativacao (com motivo) de qualquer cadastro. */
@Service
@RequiredArgsConstructor
public class HistoricoService {

    public static final String IES = "IES";
    public static final String CENTRO_ACADEMICO = "CENTRO_ACADEMICO";
    public static final String CURSO = "CURSO";
    public static final String DOCENTE = "DOCENTE";
    public static final String DISCENTE = "DISCENTE";
    public static final String DISCIPLINA = "DISCIPLINA";
    public static final String MONITORIA = "MONITORIA";
    public static final String RELATORIO_MONITORIA = "RELATORIO_MONITORIA";

    private final HistoricoStatusRepository repository;

    public void registrar(String entidade, Long id, Status anterior, Status novo, String motivo) {
        HistoricoStatus h = new HistoricoStatus();
        h.setEntidade(entidade);
        h.setEntidadeId(id);
        h.setStatusAnterior(anterior);
        h.setStatusNovo(novo);
        h.setMotivo(motivo);
        h.setUsuarioLogin(UsuarioLogado.loginAtualOuNull());
        repository.save(h);
    }

    public boolean possuiHistorico(String entidade, Long id) {
        return repository.existsByEntidadeAndEntidadeId(entidade, id);
    }

    public List<StatusDto.HistoricoResponse> listar(String entidade, Long id) {
        return repository.findByEntidadeAndEntidadeIdOrderByDataHoraDesc(entidade.toUpperCase(), id).stream()
                .map(StatusDto.HistoricoResponse::from).toList();
    }
}
