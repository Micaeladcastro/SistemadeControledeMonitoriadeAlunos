package br.ucsal.monitoria.config;

import br.ucsal.monitoria.entity.CentroAcademico;
import br.ucsal.monitoria.entity.Curso;
import br.ucsal.monitoria.entity.Discente;
import br.ucsal.monitoria.entity.Docente;
import br.ucsal.monitoria.entity.Perfil;
import br.ucsal.monitoria.entity.Usuario;
import br.ucsal.monitoria.repository.CentroAcademicoRepository;
import br.ucsal.monitoria.repository.CursoRepository;
import br.ucsal.monitoria.repository.DiscenteRepository;
import br.ucsal.monitoria.repository.DocenteRepository;
import br.ucsal.monitoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Cria o administrador inicial (as senhas precisam de BCrypt, por isso nao estao no SQL do Flyway)
 * e, opcionalmente, um docente e um discente de demonstracao.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final DocenteRepository docenteRepository;
    private final DiscenteRepository discenteRepository;
    private final CentroAcademicoRepository centroAcademicoRepository;
    private final CursoRepository cursoRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-login}")
    private String adminLogin;
    @Value("${app.seed.admin-password}")
    private String adminSenha;
    @Value("${app.seed.demo-users}")
    private boolean demoUsers;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!usuarioRepository.existsByLogin(adminLogin)) {
            usuarioRepository.save(new Usuario(adminLogin, passwordEncoder.encode(adminSenha), "Administrador",
                    Set.of(Perfil.ADMINISTRADOR)));
            log.info("Usuario administrador '{}' criado", adminLogin);
        }
        if (demoUsers && !usuarioRepository.existsByLogin("prof")) {
            criarDemo();
        }
    }

    private void criarDemo() {
        CentroAcademico ccet = centroAcademicoRepository.findAll().stream()
                .filter(c -> c.getSigla().equals("CCET")).findFirst().orElse(null);
        Curso es = cursoRepository.findAll().stream()
                .filter(c -> c.getSigla().equals("ES")).findFirst().orElse(null);
        if (ccet == null || es == null) {
            return;
        }
        Docente docente = new Docente();
        docente.setMatricula("D0001");
        docente.setNome("Professor Demonstracao");
        docente.setEmail("prof@ucsal.br");
        docente.setTelefone("(71) 99999-0001");
        docente.setCentroAcademico(ccet);
        docente.setUsuario(usuarioRepository.save(new Usuario("prof", passwordEncoder.encode("prof123"),
                docente.getNome(), Set.of(Perfil.COLABORADOR))));
        docenteRepository.save(docente);

        Discente discente = new Discente();
        discente.setMatricula("A0001");
        discente.setNome("Aluno Demonstracao");
        discente.setEmail("aluno@ucsal.edu.br");
        discente.setTelefone("(71) 99999-0002");
        discente.setCurso(es);
        discente.setUsuario(usuarioRepository.save(new Usuario("aluno", passwordEncoder.encode("aluno123"),
                discente.getNome(), Set.of(Perfil.USUARIO))));
        discenteRepository.save(discente);
        log.info("Usuarios de demonstracao criados: prof/prof123 (COLABORADOR) e aluno/aluno123 (USUARIO)");
    }
}
