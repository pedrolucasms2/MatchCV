package org.example;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class MainApp extends Application {

    private File arquivoSelecionado;
    private Button analisarBtn;

    // --- MUDANÇA: Substituímos a TextArea por uma TableView ---
    private TableView<Avaliacao> tabelaResultados;
    private final ObservableList<Avaliacao> dadosTabela = FXCollections.observableArrayList();

    private final AnaliseService analiseService = new AnaliseService();
    // --- NOVO: Criamos uma instância do Gson para ser usada ---
    private final Gson gson = new Gson();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MatchCV - Analisador de Currículos");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // --- Painel de Controle (Esquerda) ---
        Button selecionarBtn = new Button("1. Selecionar Currículo...");
        analisarBtn = new Button("2. Analisar");
        analisarBtn.setDisable(true);

        VBox controleBox = new VBox(10, selecionarBtn, analisarBtn);
        root.setLeft(controleBox);
        BorderPane.setMargin(controleBox, new Insets(0, 10, 0, 0));

        // --- Painel de Resultados (Centro) com a TableView ---
        Label logLabel = new Label("Resultados da Análise:");

        // Inicializa a tabela
        tabelaResultados = new TableView<>();
        tabelaResultados.setItems(dadosTabela); // Conecta a tabela aos nossos dados

        // Cria a coluna "Competência"
        TableColumn<Avaliacao, String> colunaCompetencia = new TableColumn<>("Competência");
        colunaCompetencia.setCellValueFactory(new PropertyValueFactory<>("competencia"));
        colunaCompetencia.setPrefWidth(150);

        // Cria a coluna "Nível Estimado"
        TableColumn<Avaliacao, String> colunaNivel = new TableColumn<>("Nível Estimado");
        colunaNivel.setCellValueFactory(new PropertyValueFactory<>("nivel_estimado"));
        colunaNivel.setPrefWidth(120);

        // Cria a coluna "Justificativa"
        TableColumn<Avaliacao, String> colunaJustificativa = new TableColumn<>("Justificativa");
        colunaJustificativa.setCellValueFactory(new PropertyValueFactory<>("justificativa"));
        colunaJustificativa.setPrefWidth(350);

        // Adiciona as colunas à tabela
        tabelaResultados.getColumns().add(colunaCompetencia);
        tabelaResultados.getColumns().add(colunaNivel);
        tabelaResultados.getColumns().add(colunaJustificativa);

        VBox resultadoBox = new VBox(5, logLabel, tabelaResultados);
        root.setCenter(resultadoBox);

        // --- Ações dos Botões ---

        selecionarBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecione um currículo (.pdf)");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf")
            );

            File arquivo = fileChooser.showOpenDialog(primaryStage);
            if (arquivo != null) {
                this.arquivoSelecionado = arquivo;
                dadosTabela.clear(); // Limpa resultados antigos
                tabelaResultados.setPlaceholder(new Label("Arquivo '" + arquivo.getName() + "' selecionado. Clique em 'Analisar'."));
                analisarBtn.setDisable(false);
            }
        });

        analisarBtn.setOnAction(e -> {
            if (this.arquivoSelecionado != null) {
                dadosTabela.clear(); // Limpa a tabela
                tabelaResultados.setPlaceholder(new Label("Analisando... Por favor, aguarde."));
                analisarBtn.setDisable(true);

                new Thread(() -> {
                    try {
                        // 1. Extrai o texto (sem mudanças aqui)
                        String textoDoPDF = analiseService.extrairTextoDePDF(this.arquivoSelecionado);

                        // 2. Chama a IA (sem mudanças aqui)
                        String respostaJson = analiseService.analisarTextoComIA(textoDoPDF);

                        // --- MUDANÇA: Parse do JSON usando Gson ---
                        // Converte a string JSON em nossos objetos Java
                        ResultadoAnalise resultado = gson.fromJson(respostaJson, ResultadoAnalise.class);
                        List<Avaliacao> novasAvaliacoes = resultado.getAvaliacoes();

                        // 3. Atualiza a interface com os novos dados
                        Platform.runLater(() -> {
                            dadosTabela.setAll(novasAvaliacoes); // Adiciona todos os novos itens à tabela
                            tabelaResultados.setPlaceholder(new Label("Nenhum resultado encontrado."));
                            analisarBtn.setDisable(false);
                        });

                    } catch (Exception ex) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro na Análise");
                            alert.setHeaderText("Não foi possível completar a análise.");
                            alert.setContentText("Ocorreu um erro: " + ex.getMessage());
                            alert.showAndWait();

                            tabelaResultados.setPlaceholder(new Label("Ocorreu um erro durante a análise."));
                            analisarBtn.setDisable(false);
                        });
                        ex.printStackTrace();
                    }
                }).start();
            }
        });

        Scene scene = new Scene(root, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}