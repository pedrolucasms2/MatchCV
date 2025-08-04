package org.example; // Mude para o seu nome de pacote se for diferente

public class Avaliacao {

    // Os nomes das variáveis DEVEM ser exatamente iguais aos nomes das chaves no JSON.
    private String competencia;
    private String nivel_estimado;
    private String justificativa;

    // Getters - Métodos para acessar os valores (necessário para o JavaFX)
    public String getCompetencia() {
        return competencia;
    }

    public String getNivel_estimado() {
        return nivel_estimado;
    }

    public String getJustificativa() {
        return justificativa;
    }

    // Setters - Métodos para definir os valores (boa prática, embora o Gson não precise deles)
    public void setCompetencia(String competencia) {
        this.competencia = competencia;
    }

    public void setNivel_estimado(String nivel_estimado) {
        this.nivel_estimado = nivel_estimado;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    // (Opcional) Método toString() para facilitar a depuração
    @Override
    public String toString() {
        return "Avaliacao{" +
                "competencia='" + competencia + '\'' +
                ", nivel_estimado='" + nivel_estimado + '\'' +
                ", justificativa='" + justificativa + '\'' +
                '}';
    }
}