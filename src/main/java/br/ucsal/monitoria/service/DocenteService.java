package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.DocenteDto;
import br.ucsal.monitoria.entity.CentroAcademico;
import br.ucsal.monitoria.entity.Docente;
import br.ucsal.monitoria.entity.Perfil;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.DocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository repository;
    private final CentroAcademicoService centroAcademicoService;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<DocenteDto.Response> listar(Status status) {
        var lista = status == null ? repository.findAllByOrderByNomeAsc() : repository.findByStatusOrderByNomeAsc(status);
        return lista.stream().map(DocenteDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public DocenteDto.Response buscar(Long id) {
        return DocenteDto.Response.from(buscarEntidade(id));
    }

    public Docente buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Docente", id));
    }

    @Transactional
    public DocenteDto.Response criar(DocenteDto.Request req) {
        String matricula = req.matricula().trim();
        if (repository.existsByMatriculaIgnoreCase(matricula)) {
            throw new RegraNegocioException("Ja existe um docente com a matricula " + matricula);
        }
        Docente d = new Docente();
        preencher(d, req, caAtivo(req.centroAcademicoId()));
        d.setMatricula(matricula);
        String senha = req.senhaInicial() != null ? req.senhaInicial() : matricula;
        d.setUsuario(usuarioService.criarOuAcumularPerfil(matricula, d.getNome(), senha, Perfil.COLABORADOR));
        return DocenteDto.Response.from(repository.save(d));
    }

    /** A matricula (que tambem e o login) nao muda na edicao. */
    @Transactional
    public DocenteDto.Response atualizar(Long id, DocenteDto.Request req) {
        Docente d = buscarEntidade(id);
        CentroAcademico ca = d.getCentroAcademico().getId().equals(req.centroAcademicoId())
                ? d.getCentroAcademico() : caAtivo(req.centroAcademicoId());
        preencher(d, req, ca);
        d.getUsuario().setNome(d.getNome());
        return DocenteDto.Response.from(d);
    }

    @Transactional
    public DocenteDto.Response alterarStatus(Long id, Status novo, String motivo) {
        Docente d = buscarEntidade(id);
        if (d.getStatus() == novo) {
            throw new RegraNegocioException("O docente ja esta " + novo);
        }
        historicoService.registrar(HistoricoService.DOCENTE, id, d.getStatus(), novo, motivo);
        d.setStatus(novo);
        usuarioService.sincronizarAtivo(d.getUsuario(), Perfil.COLABORADOR, novo == Status.ATIVO);
        return DocenteDto.Response.from(d);
    }

    private CentroAcademico caAtivo(Long id) {
        CentroAcademico ca = centroAcademicoService.buscarEntidade(id);
        if (ca.getStatus() != Status.ATIVO) {
            throw new RegraNegocioException("O Centro Academico " + ca.getSigla() + " esta inativo");
        }
        return ca;
    }

    private void preencher(Docente d, DocenteDto.Request req, CentroAcademico ca) {
        d.setNome(req.nome().trim());
        d.setEmail(req.email().trim());
        d.setTelefone(req.telefone().trim());
        d.setCentroAcademico(ca);
    }
}
