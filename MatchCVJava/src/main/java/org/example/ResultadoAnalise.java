package org.example;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ResultadoAnalise {

    @Expose
    @SerializedName("avaliacoes")
    public List<Avaliacao> avaliacoes;

    public ResultadoAnalise() {
    }

    public List<Avaliacao> getAvaliacoes() {
        return avaliacoes;
    }

    public void setAvaliacoes(List<Avaliacao> avaliacoes) {
        this.avaliacoes = avaliacoes;
    }
}