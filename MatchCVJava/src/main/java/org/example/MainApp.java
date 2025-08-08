package org.example;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class MainApp extends Application {
    private Button analisarBtn;
    private Button limparFilaBtn;
    private Button selecionarArquivosBtn;
    private Button selecionarPastaBtn;
    private ProgressBar progressBar;
    private Label progressoLabel;

    private TableView<Requisito> tabelaRequisitos;
    private final ObservableList<Requisito> dadosRequisitos = FXCollections.observableArrayList();
    private TextField inputCompetencia;
    private ComboBox<String> comboImportancia;
    private Button btnAddReq;

    private TableView<ResultadoCandidato> tabelaRanking;
    private final ObservableList<ResultadoCandidato> dadosRanking = FXCollections.observableArrayList();

    private TableView<Avaliacao> tabelaDetalhes;
    private final ObservableList<Avaliacao> dadosDetalhes = FXCollections.observableArrayList();

    private final List<File> arquivosParaAnalisar = new ArrayList<>();
    private final AnaliseService analiseService = new AnaliseService();
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("MatchCV - Analisador de Currículos v1.2");

        try (InputStream iconStream = getClass().getResourceAsStream("/appIcon.icns")) {
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(15));

        Text tituloApp = new Text("MatchCV");
        tituloApp.setId("titulo-app");
        tituloApp.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        BorderPane.setAlignment(tituloApp, Pos.CENTER);
        mainLayout.setTop(tituloApp);
        BorderPane.setMargin(tituloApp, new Insets(0, 0, 20, 0));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        mainLayout.setCenter(grid);

        VBox painelRequisitos = criarPainelRequisitos();
        grid.add(painelRequisitos, 0, 0);

        VBox painelRanking = criarPainelRanking();
        grid.add(painelRanking, 1, 0);

        VBox painelDetalhes = criarPainelDetalhes();
        grid.add(painelDetalhes, 2, 0);

        HBox statusBar = criarBarraDeStatus();
        mainLayout.setBottom(statusBar);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(25);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2, col3);

        Scene scene = new Scene(mainLayout, 1400, 700);

        String cssFile = "/styles.css";
        URL cssUrl = getClass().getResource(cssFile);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox criarPainelRequisitos() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TitledPane titledPane = new TitledPane("Definir Requisitos e Selecionar Arquivos", content);
        titledPane.setCollapsible(false);

        VBox painelRequisitosDefinicao = new VBox(5);
        Label titulo = new Label("Requisitos da Vaga:");
        titulo.getStyleClass().add("subtitulo-painel");

        tabelaRequisitos = new TableView<>(dadosRequisitos);
        TableColumn<Requisito, String> colReqComp = new TableColumn<>("Competência");
        colReqComp.setCellValueFactory(new PropertyValueFactory<>("competencia"));
        TableColumn<Requisito, String> colReqImp = new TableColumn<>("Importância");
        colReqImp.setCellValueFactory(new PropertyValueFactory<>("importancia"));
        tabelaRequisitos.getColumns().addAll(colReqComp, colReqImp);
        tabelaRequisitos.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        colReqComp.prefWidthProperty().bind(tabelaRequisitos.widthProperty().multiply(0.70));
        colReqImp.prefWidthProperty().bind(tabelaRequisitos.widthProperty().multiply(0.30));

        inputCompetencia = new TextField();
        inputCompetencia.setPromptText("Nome da competência");
        comboImportancia = new ComboBox<>(FXCollections.observableArrayList("Essencial", "Desejável"));
        comboImportancia.setValue("Essencial");

        btnAddReq = new Button("Adicionar");
        btnAddReq.getStyleClass().add("btn-com-icone");
        btnAddReq.setGraphic(criarIcone("/icons/add-icon.png"));
        btnAddReq.setOnAction(e -> adicionarRequisito());
        btnAddReq.setDisable(true);

        inputCompetencia.textProperty().addListener((observable, oldValue, newValue) -> {
            btnAddReq.setDisable(newValue.trim().isEmpty());
        });

        HBox addBox = new HBox(5, inputCompetencia, comboImportancia, btnAddReq);
        addBox.setAlignment(Pos.CENTER_LEFT);

        painelRequisitosDefinicao.getChildren().addAll(titulo, tabelaRequisitos, addBox);

        VBox painelSelecaoArquivos = new VBox(5);
        Label selecionarLabel = new Label("Adicionar Currículos à Fila:");
        selecionarLabel.getStyleClass().add("subtitulo-painel");

        selecionarArquivosBtn = new Button("Selecionar Arquivos...");
        selecionarArquivosBtn.getStyleClass().add("btn-com-icone");
        selecionarArquivosBtn.setGraphic(criarIcone("/icons/file-icon.png"));
        selecionarArquivosBtn.setOnAction(e -> selecionarArquivos());

        selecionarPastaBtn = new Button("Selecionar Pasta...");
        selecionarPastaBtn.getStyleClass().add("btn-com-icone");
        selecionarPastaBtn.setGraphic(criarIcone("/icons/folder-icon.png"));
        selecionarPastaBtn.setOnAction(e -> selecionarPasta());

        limparFilaBtn = new Button("Limpar Fila");
        limparFilaBtn.getStyleClass().add("btn-com-icone");
        limparFilaBtn.setGraphic(criarIcone("/icons/clear-icon.png"));
        limparFilaBtn.setDisable(true);
        limparFilaBtn.setOnAction(e -> limparFila());

        HBox botoesSelecao = new HBox(10, selecionarArquivosBtn, selecionarPastaBtn, limparFilaBtn);

        analisarBtn = new Button("3. Analisar Currículos");
        analisarBtn.setId("btn-analisar");
        analisarBtn.setDisable(true);
        analisarBtn.setOnAction(e -> iniciarAnalise());

        painelSelecaoArquivos.getChildren().addAll(selecionarLabel, botoesSelecao, new Separator(), analisarBtn);

        content.getChildren().addAll(painelRequisitosDefinicao, new Separator(), painelSelecaoArquivos);

        return new VBox(titledPane);
    }

    private VBox criarPainelRanking() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TitledPane titledPane = new TitledPane("Ranking de Candidatos", content);
        titledPane.setCollapsible(false);

        tabelaRanking = new TableView<>(dadosRanking);
        TableColumn<ResultadoCandidato, String> colRankNome = new TableColumn<>("Arquivo");
        colRankNome.setCellValueFactory(new PropertyValueFactory<>("nomeArquivo"));
        TableColumn<ResultadoCandidato, Integer> colRankPontos = new TableColumn<>("Pontuação");
        colRankPontos.setCellValueFactory(new PropertyValueFactory<>("pontuacao"));

        colRankPontos.setSortType(TableColumn.SortType.DESCENDING);
        tabelaRanking.getColumns().addAll(colRankNome, colRankPontos);
        tabelaRanking.getSortOrder().add(colRankPontos);
        tabelaRanking.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colRankNome.prefWidthProperty().bind(tabelaRanking.widthProperty().multiply(0.70));
        colRankPontos.prefWidthProperty().bind(tabelaRanking.widthProperty().multiply(0.25));

        tabelaRanking.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null && newSelection.getAvaliacoesDetalhadas() != null) {
                        dadosDetalhes.setAll(newSelection.getAvaliacoesDetalhadas());
                    } else {
                        dadosDetalhes.clear();
                    }
                }
        );

        content.getChildren().add(tabelaRanking);

        return new VBox(titledPane);
    }

    private VBox criarPainelDetalhes() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        TitledPane titledPane = new TitledPane("Detalhes da Análise do Candidato Selecionado", content);
        titledPane.setCollapsible(false);

        tabelaDetalhes = new TableView<>(dadosDetalhes);
        TableColumn<Avaliacao, String> colDetComp = new TableColumn<>("Competência");
        colDetComp.setCellValueFactory(new PropertyValueFactory<>("competencia"));
        TableColumn<Avaliacao, String> colDetNivel = new TableColumn<>("Nível Estimado");
        colDetNivel.setCellValueFactory(new PropertyValueFactory<>("nivel_estimado"));
        TableColumn<Avaliacao, String> colDetJust = new TableColumn<>("Justificativa");
        colDetJust.setCellValueFactory(new PropertyValueFactory<>("justificativa"));

        tabelaDetalhes.getColumns().addAll(colDetComp, colDetNivel, colDetJust);
        tabelaDetalhes.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colDetComp.prefWidthProperty().bind(tabelaDetalhes.widthProperty().multiply(0.20));
        colDetNivel.prefWidthProperty().bind(tabelaDetalhes.widthProperty().multiply(0.20));
        colDetJust.prefWidthProperty().bind(tabelaDetalhes.widthProperty().multiply(0.55));

        content.getChildren().add(tabelaDetalhes);

        return new VBox(titledPane);
    }

    private HBox criarBarraDeStatus() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5, 15, 5, 15));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("status-bar");

        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(progressBar, Priority.ALWAYS);

        progressoLabel = new Label("Adicione requisitos e selecione arquivos/pastas.");
        progressoLabel.getStyleClass().add("status-label");

        statusBar.getChildren().addAll(progressoLabel, progressBar);
        return statusBar;
    }

    private ImageView criarIcone(String path) {
        try (InputStream iconStream = getClass().getResourceAsStream(path)) {
            if (iconStream != null) {
                Image image = new Image(iconStream);
                ImageView imageView = new ImageView(image);
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                return imageView;
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar o ícone: " + path);
        }
        return null;
    }

    private void adicionarRequisito() {
        String competencia = inputCompetencia.getText().trim();
        String importancia = comboImportancia.getValue();
        dadosRequisitos.add(new Requisito(competencia, importancia));
        inputCompetencia.clear();
        verificarSePodeAnalisar();
    }

    private void selecionarArquivos() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione um ou mais currículos (.pdf)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documentos PDF", "*.pdf"));
        List<File> arquivosSelecionados = fileChooser.showOpenMultipleDialog(null);
        if (arquivosSelecionados != null && !arquivosSelecionados.isEmpty()) {
            for (File arquivo : arquivosSelecionados) {
                if (!this.arquivosParaAnalisar.contains(arquivo)) {
                    this.arquivosParaAnalisar.add(arquivo);
                }
            }
            atualizarStatusFila();
        }
    }

    private void selecionarPasta() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecione a pasta com os currículos (.pdf)");
        File pasta = directoryChooser.showDialog(null);
        if (pasta != null) {
            File[] arquivosDaPasta = pasta.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (arquivosDaPasta != null) {
                for (File arquivo : arquivosDaPasta) {
                    if (!this.arquivosParaAnalisar.contains(arquivo)) {
                        this.arquivosParaAnalisar.add(arquivo);
                    }
                }
            }
            atualizarStatusFila();
        }
    }

    private void limparFila() {
        this.arquivosParaAnalisar.clear();
        atualizarStatusFila();
    }

    private void atualizarStatusFila() {
        int total = this.arquivosParaAnalisar.size();
        if (total == 0) {
            progressoLabel.setText("Fila de análise vazia.");
            limparFilaBtn.setDisable(true);
        } else if (total == 1) {
            progressoLabel.setText("1 currículo na fila de análise.");
            limparFilaBtn.setDisable(false);
        } else {
            progressoLabel.setText(total + " currículos na fila de análise.");
            limparFilaBtn.setDisable(false);
        }
        verificarSePodeAnalisar();
    }

    private void verificarSePodeAnalisar() {
        boolean podeAnalisar = !arquivosParaAnalisar.isEmpty() && !dadosRequisitos.isEmpty();
        analisarBtn.setDisable(!podeAnalisar);
    }

    private void iniciarAnalise() {
        if (arquivosParaAnalisar == null || arquivosParaAnalisar.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Por favor, adicione arquivos à fila de análise.").show();
            return;
        }
        if(dadosRequisitos.isEmpty()){
            new Alert(Alert.AlertType.WARNING, "Por favor, adicione pelo menos um requisito para a vaga.").show();
            return;
        }

        analisarBtn.setDisable(true);
        limparFilaBtn.setDisable(true);
        selecionarArquivosBtn.setDisable(true);
        selecionarPastaBtn.setDisable(true);
        dadosRanking.clear();
        dadosDetalhes.clear();

        AtomicInteger arquivosProcessados = new AtomicInteger(0);
        int totalArquivos = arquivosParaAnalisar.size();
        progressBar.setProgress(0);

        new Thread(() -> {
            for (File arquivo : arquivosParaAnalisar) {
                Platform.runLater(() -> progressoLabel.setText("Analisando: " + arquivo.getName()));
                try {
                    String texto = analiseService.extrairTextoDePDF(arquivo);
                    List<String> listaRequisitos = dadosRequisitos.stream()
                            .map(Requisito::getCompetencia)
                            .collect(Collectors.toList());

                    String json = analiseService.analisarTextoComIA(texto, listaRequisitos);
                    System.out.println("JSON recebido do backend: " + json);
                    ResultadoAnalise resultadoAnalise = gson.fromJson(json, ResultadoAnalise.class);

                    ResultadoCandidato resultadoCandidato = new ResultadoCandidato(arquivo);
                    resultadoCandidato.setAvaliacoesDetalhadas(resultadoAnalise.getAvaliacoes());
                    int pontuacao = calcularPontuacao(resultadoCandidato.getAvaliacoesDetalhadas());
                    resultadoCandidato.setPontuacao(pontuacao);

                    Platform.runLater(() -> dadosRanking.add(resultadoCandidato));

                } catch (Exception e) {
                    System.err.println("Erro ao analisar o arquivo \"" + arquivo.getName() + "\": " + e.getMessage());
                } finally {
                    int processados = arquivosProcessados.incrementAndGet();
                    Platform.runLater(() -> progressBar.setProgress((double) processados / totalArquivos));
                }
            }

            Platform.runLater(() -> {
                Collections.sort(dadosRanking);
                progressoLabel.setText("Análise concluída! " + totalArquivos + " currículos processados.");
                analisarBtn.setDisable(false);
                limparFilaBtn.setDisable(false);
                selecionarArquivosBtn.setDisable(false);
                selecionarPastaBtn.setDisable(false);
            });

        }).start();
    }

    private int calcularPontuacao(List<Avaliacao> avaliacoes) {
        if (dadosRequisitos.isEmpty()) {
            System.err.println("Erro: Nenhum requisito foi definido.");
            return 0;
        }

        Map<String, String> mapaRequisitos = dadosRequisitos.stream()
                .collect(Collectors.toMap(
                        r -> r.getCompetencia().trim().toLowerCase(),
                        Requisito::getImportancia
                ));

        if (avaliacoes == null || avaliacoes.isEmpty()) {
            System.err.println("Aviso: A lista de avaliações retornada pela IA está vazia.");
            return 0;
        }

        int pontuacaoTotal = 0;

        for (Avaliacao aval : avaliacoes) {
            if (aval == null || aval.getCompetencia() == null) {
                System.err.println("Ignorando uma avaliação nula ou inválida.");
                continue;
            }

            String competenciaDaIA = aval.getCompetencia();
            String competenciaDaIA_lower = competenciaDaIA.trim().toLowerCase();

            if (mapaRequisitos.containsKey(competenciaDaIA_lower)) {
                String importancia = mapaRequisitos.get(competenciaDaIA_lower);
                String nivel = aval.getNivel_estimado();
                int pontosNivel = getPontosPorNivel(nivel);
                int pesoImportancia = getPesoPorImportancia(importancia);
                pontuacaoTotal += pontosNivel * pesoImportancia;
            } else {
                System.err.println("Competência '" + competenciaDaIA_lower + "' não encontrada entre os requisitos definidos.");
            }
        }
        return pontuacaoTotal;
    }

    private int getPontosPorNivel(String nivel) {
        if (nivel == null) return 0;
        switch (nivel.toLowerCase()) {
            case "especialista": return 4;
            case "avançado":
            case "avancado": return 3;
            case "intermediário":
            case "intermediario": return 2;
            case "iniciante": return 1;
            default: return 0;
        }
    }

    private int getPesoPorImportancia(String importancia) {
        if (importancia == null) return 1;
        switch (importancia.toLowerCase()) {
            case "essencial": return 3;
            case "desejável":
            case "desejavel": return 1;
            default: return 1;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}