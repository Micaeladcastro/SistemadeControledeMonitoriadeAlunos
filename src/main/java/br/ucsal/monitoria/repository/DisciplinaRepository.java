package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.Disciplina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DisciplinaRepository extends JpaRepository<Disciplina, Long> {

    List<Disciplina> findAllByOrderByDescricaoAsc();

    List<Disciplina> findByDocenteIdOrderByDescricaoAsc(Long docenteId);

    boolean existsByCursoId(Long cursoId);

    boolean existsByDocenteId(Long docenteId);

    boolean existsByCursoIdAndSiglaIgnoreCase(Long cursoId, String sigla);

    boolean existsByCursoIdAndSiglaIgnoreCaseAndIdNot(Long cursoId, String sigla, Long id);
}
