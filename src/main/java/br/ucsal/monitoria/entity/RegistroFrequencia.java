package br.ucsal.monitoria.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Uma linha da lista de frequencia: aluno X esteve (ou nao) presente na data Y. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroFrequencia {

    private LocalDate data;
    private String alunoNome;
    private String alunoMatricula;
    private boolean presente;
}
