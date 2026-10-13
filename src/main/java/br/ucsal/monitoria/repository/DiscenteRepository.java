package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.Discente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiscenteRepository extends JpaRepository<Discente, Long> {

    List<Discente> findAllByOrderByNomeAsc();

    Optional<Discente> findByUsuarioLogin(String login);

    boolean existsByMatriculaIgnoreCase(String matricula);

    boolean existsByCursoId(Long cursoId);
}
