package org.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.util.List;

public class ResultadoCandidato implements Comparable<ResultadoCandidato> {
    private final StringProperty nomeArquivo;
    private final IntegerProperty pontuacao;
    private final File arquivo;
    private List<Avaliacao> avaliacoesDetalhadas;

    public ResultadoCandidato(File arquivo) {
        this.arquivo = arquivo;
        this.nomeArquivo = new SimpleStringProperty(arquivo.getName());
        this.pontuacao = new SimpleIntegerProperty(0);
    }

    public String getNomeArquivo() {
        return nomeArquivo.get();
    }

    public StringProperty nomeArquivoProperty() {
        return nomeArquivo;
    }

    public int getPontuacao() {
        return pontuacao.get();
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao.set(pontuacao);
    }

    public IntegerProperty pontuacaoProperty() {
        return pontuacao;
    }

    public File getArquivo() {
        return arquivo;
    }

    public List<Avaliacao> getAvaliacoesDetalhadas() {
        return avaliacoesDetalhadas;
    }

    public void setAvaliacoesDetalhadas(List<Avaliacao> avaliacoesDetalhadas) {
        this.avaliacoesDetalhadas = avaliacoesDetalhadas;
    }

    @Override
    public int compareTo(ResultadoCandidato other) {
        return Integer.compare(other.getPontuacao(), this.getPontuacao());
    }
}