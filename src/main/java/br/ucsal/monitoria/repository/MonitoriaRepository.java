package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.Monitoria;
import br.ucsal.monitoria.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitoriaRepository extends JpaRepository<Monitoria, Long> {

    List<Monitoria> findAllByOrderByDataInicioDesc();

    List<Monitoria> findByDocenteIdOrderByDataInicioDesc(Long docenteId);

    List<Monitoria> findByDiscenteIdOrderByDataInicioDesc(Long discenteId);

    boolean existsByDisciplinaId(Long disciplinaId);

    boolean existsByDiscenteId(Long discenteId);

    /** Regra: o discente so pode ter uma monitoria ativa por semestre. */
    boolean existsByDiscenteIdAndSemestreAndStatusAndIdNot(Long discenteId, String semestre, Status status, Long id);
}
