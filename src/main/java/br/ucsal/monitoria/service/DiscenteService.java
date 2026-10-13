package br.ucsal.monitoria.service;

import br.ucsal.monitoria.dto.DiscenteDto;
import br.ucsal.monitoria.entity.Curso;
import br.ucsal.monitoria.entity.Discente;
import br.ucsal.monitoria.entity.Perfil;
import br.ucsal.monitoria.entity.Status;
import br.ucsal.monitoria.exception.NaoEncontradoException;
import br.ucsal.monitoria.exception.RegraNegocioException;
import br.ucsal.monitoria.repository.DiscenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscenteService {

    private final DiscenteRepository repository;
    private final CursoService cursoService;
    private final UsuarioService usuarioService;
    private final HistoricoService historicoService;

    @Transactional(readOnly = true)
    public List<DiscenteDto.Response> listar() {
        return repository.findAllByOrderByNomeAsc().stream().map(DiscenteDto.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public DiscenteDto.Response buscar(Long id) {
        return DiscenteDto.Response.from(buscarEntidade(id));
    }

    public Discente buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new NaoEncontradoException("Discente", id));
    }

    @Transactional
    public DiscenteDto.Response criar(DiscenteDto.Request req) {
        String matricula = req.matricula().trim();
        if (repository.existsByMatriculaIgnoreCase(matricula)) {
            throw new RegraNegocioException("Ja existe um discente com a matricula " + matricula);
        }
        Discente d = new Discente();
        preencher(d, req, cursoService.buscarAtivo(req.cursoId()));
        d.setMatricula(matricula);
        String senha = req.senhaInicial() != null ? req.senhaInicial() : matricula;
        d.setUsuario(usuarioService.criarOuAcumularPerfil(matricula, d.getNome(), senha, Perfil.USUARIO));
        return DiscenteDto.Response.from(repository.save(d));
    }

    /** A matricula (que tambem e o login) nao muda na edicao. */
    @Transactional
    public DiscenteDto.Response atualizar(Long id, DiscenteDto.Request req) {
        Discente d = buscarEntidade(id);
        Curso curso = d.getCurso().getId().equals(req.cursoId()) ? d.getCurso() : cursoService.buscarAtivo(req.cursoId());
        preencher(d, req, curso);
        d.getUsuario().setNome(d.getNome());
        return DiscenteDto.Response.from(d);
    }

    @Transactional
    public DiscenteDto.Response alterarStatus(Long id, Status novo, String motivo) {
        Discente d = buscarEntidade(id);
        if (d.getStatus() == novo) {
            throw new RegraNegocioException("O discente ja esta " + novo);
        }
        historicoService.registrar(HistoricoService.DISCENTE, id, d.getStatus(), novo, motivo);
        d.setStatus(novo);
        usuarioService.sincronizarAtivo(d.getUsuario(), Perfil.USUARIO, novo == Status.ATIVO);
        return DiscenteDto.Response.from(d);
    }

    private void preencher(Discente d, DiscenteDto.Request req, Curso curso) {
        d.setNome(req.nome().trim());
        d.setEmail(req.email().trim());
        d.setTelefone(req.telefone().trim());
        d.setCurso(curso);
    }
}
