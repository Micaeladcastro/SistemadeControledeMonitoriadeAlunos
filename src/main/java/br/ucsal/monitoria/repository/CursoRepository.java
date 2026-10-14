package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    List<Curso> findAllByOrderByDescricaoAsc();

    List<Curso> findByCentroAcademicoIdOrderByDescricaoAsc(Long centroAcademicoId);

    boolean existsByCentroAcademicoId(Long centroAcademicoId);

    boolean existsByCentroAcademicoIdAndSiglaIgnoreCase(Long centroAcademicoId, String sigla);

    boolean existsByCentroAcademicoIdAndSiglaIgnoreCaseAndIdNot(Long centroAcademicoId, String sigla, Long id);
}
