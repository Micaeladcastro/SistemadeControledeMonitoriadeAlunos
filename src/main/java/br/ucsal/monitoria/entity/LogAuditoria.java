package br.ucsal.monitoria.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_auditoria")
@Getter
@Setter
@NoArgsConstructor
public class LogAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usuarioLogin;
    private String metodo;
    private String uri;
    private String acao;
    private Integer statusHttp;
    private String ip;
    private LocalDateTime dataHora = LocalDateTime.now();
}
