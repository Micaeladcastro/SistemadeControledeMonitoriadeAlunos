package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.Docente;
import br.ucsal.monitoria.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocenteRepository extends JpaRepository<Docente, Long> {

    List<Docente> findAllByOrderByNomeAsc();

    List<Docente> findByStatusOrderByNomeAsc(Status status);

    Optional<Docente> findByUsuarioLogin(String login);

    boolean existsByMatriculaIgnoreCase(String matricula);

    boolean existsByCentroAcademicoId(Long centroAcademicoId);
}
