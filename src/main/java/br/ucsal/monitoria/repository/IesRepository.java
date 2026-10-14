package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.Ies;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IesRepository extends JpaRepository<Ies, Long> {

    List<Ies> findAllByOrderByNomeAsc();

    boolean existsBySiglaIgnoreCase(String sigla);

    boolean existsBySiglaIgnoreCaseAndIdNot(String sigla, Long id);
}
