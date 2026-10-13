package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.CentroAcademico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CentroAcademicoRepository extends JpaRepository<CentroAcademico, Long> {

    List<CentroAcademico> findAllByOrderByNomeAsc();

    List<CentroAcademico> findByIesIdOrderByNomeAsc(Long iesId);

    boolean existsByIesId(Long iesId);

    boolean existsByIesIdAndSiglaIgnoreCase(Long iesId, String sigla);

    boolean existsByIesIdAndSiglaIgnoreCaseAndIdNot(Long iesId, String sigla, Long id);
}
