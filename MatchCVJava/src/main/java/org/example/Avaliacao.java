package org.example;

import com.google.gson.annotations.SerializedName;

public class Avaliacao {

    @SerializedName(value = "competencia", alternate = "competência")
    private String competencia;
    private String nivel_estimado;
    private String justificativa;

    public Avaliacao() {
    }

    public String getCompetencia() {
        return competencia;
    }

    public String getNivel_estimado() {
        return nivel_estimado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public void setNivel_estimado(String nivel_estimado) {
        this.nivel_estimado = nivel_estimado;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    @Override
    public String toString() {
        return "Avaliacao{" +
                "competencia='" + competencia + '\'' +
                ", nivel_estimado='" + nivel_estimado + '\'' +
                ", justificativa='" + justificativa + '\'' +
                '}';
    }
}