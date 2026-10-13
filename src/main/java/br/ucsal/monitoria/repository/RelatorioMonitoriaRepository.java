package br.ucsal.monitoria.repository;

import br.ucsal.monitoria.entity.RelatorioMonitoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RelatorioMonitoriaRepository extends JpaRepository<RelatorioMonitoria, Long> {

    Optional<RelatorioMonitoria> findByMonitoriaId(Long monitoriaId);

    boolean existsByMonitoriaId(Long monitoriaId);

    List<RelatorioMonitoria> findByMonitoriaDocenteIdOrderByDataCadastroDesc(Long docenteId);

    List<RelatorioMonitoria> findByMonitoriaDiscenteIdOrderByDataCadastroDesc(Long discenteId);

    List<RelatorioMonitoria> findAllByOrderByDataCadastroDesc();
}
