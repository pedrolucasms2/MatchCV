package org.example; // Mude para o seu nome de pacote se for diferente

import java.util.List;

public class ResultadoAnalise {

    // O nome da variável DEVE ser exatamente igual à chave no JSON.
    // O tipo é uma Lista de objetos da classe que acabamos de criar.
    private List<Avaliacao> avaliacoes;

    // Getter
    public List<Avaliacao> getAvaliacoes() {
        return avaliacoes;
    }

    // Setter
    public void setAvaliacoes(List<Avaliacao> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }
}