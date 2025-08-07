package org.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Requisito {
    private final StringProperty competencia;
    private final StringProperty importancia;

    public Requisito(String competencia, String importancia) {
        this.competencia = new SimpleStringProperty(competencia);
        this.importancia = new SimpleStringProperty(importancia);
    }

    public String getCompetencia() {
        return competencia.get();
    }
    public String getImportancia() {
        return importancia.get();
    }

    public StringProperty competenciaProperty() {
        return competencia;
    }
    public StringProperty importanciaProperty() {
        return importancia;
    }
}