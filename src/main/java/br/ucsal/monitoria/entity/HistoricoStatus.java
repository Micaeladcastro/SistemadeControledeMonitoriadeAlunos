package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Registra cada ativacao/inativacao com o motivo (afastamento, desligamento, cancelamento...). */
@Entity
@Table(name = "historico_status")
@Getter
@Setter
@NoArgsConstructor
public class HistoricoStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entidade;
    private Long entidadeId;

    @Enumerated(EnumType.STRING)
    private Status statusAnterior;

    @Enumerated(EnumType.STRING)
    private Status statusNovo;

    private String motivo;
    private String usuarioLogin;
    private LocalDateTime dataHora = LocalDateTime.now();
}
