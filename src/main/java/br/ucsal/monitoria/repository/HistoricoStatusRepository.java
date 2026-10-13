package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.HistoricoStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

    List<HistoricoStatus> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Long entidadeId);

    boolean existsByEntidadeAndEntidadeId(String entidade, Long entidadeId);
}
