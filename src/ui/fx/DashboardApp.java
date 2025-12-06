package ui.fx;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import dto.EstadoPopulacao;
import dto.EstadoResumo;
import dto.FiltroResolvido;
import dto.MunicipioResumo;
import dto.PontoSerieTemporal;
import dto.RegiaoResumo;
import exceptions.ExceptionFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import service.DashboardService;
import service.ImportService;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DashboardApp extends Application {
    private Stage primaryStage;
    private final DashboardService dashboardService = new DashboardService();
    private final ImportService importService = new ImportService();
    private ExecutorService executor;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.executor = Executors.newFixedThreadPool(3, new DaemonFactory());
        stage.setTitle("Dashboards Populacionais");
        stage.setScene(criarHomeScene());
        stage.show();
    }

    @Override
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception ignored) {
        }
    }

    private Scene criarHomeScene() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Dashboards Populacionais");
        titulo.setFont(Font.font(22));

        Label subtitulo = new Label("Selecione uma opção para começar");
        subtitulo.setFont(Font.font(14));

        Button btnDashboards = new Button("Abrir dashboards");
        btnDashboards.setMaxWidth(Double.MAX_VALUE);
        btnDashboards.setOnAction(e -> primaryStage.setScene(criarDashboardScene()));

        Button btnImport = new Button("Carregar novos dados (CSV)");
        btnImport.setMaxWidth(Double.MAX_VALUE);
        btnImport.setOnAction(e -> abrirDialogoImportacao());

        root.getChildren().addAll(titulo, subtitulo, btnDashboards, btnImport);
        VBox.setVgrow(btnDashboards, Priority.ALWAYS);
        VBox.setVgrow(btnImport, Priority.ALWAYS);

        return new Scene(root, 460, 280);
    }

    private Scene criarDashboardScene() {
        DashboardView view = new DashboardView(dashboardService, executor, this::abrirDialogoImportacao, this::voltarHome);
        return new Scene(view, 1024, 640);
    }

    private void voltarHome() {
        primaryStage.setScene(criarHomeScene());
    }

    private void abrirDialogoImportacao() {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Importar CSV");

        Label info = new Label("Selecione um arquivo CSV para importar. O processamento acontece em segundo plano.");
        info.setWrapText(true);

        Label status = new Label();
        status.setWrapText(true);
        status.setMaxWidth(Double.MAX_VALUE);
        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        Button btnSelecionar = new Button("Selecionar arquivo...");
        btnSelecionar.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Selecionar CSV populacional");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV", "*.csv"));
            File arquivo = chooser.showOpenDialog(dialog);
            if (arquivo != null) {
                executarImportacao(arquivo, status, progress);
            }
        });

        VBox layout = new VBox(12, info, btnSelecionar, new Separator(), status, progress);
        layout.setFillWidth(true);
        layout.setPadding(new Insets(20));

        dialog.setScene(new Scene(layout, 480, 220));
        dialog.show();
    }

    private void executarImportacao(File arquivo, Label status, ProgressIndicator progress) {
        progress.setVisible(true);
        status.setText("Importando " + arquivo.getName() + "...");
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                try {
                    var resultado = importService.importarCsvParaBanco(arquivo.getAbsolutePath());
                    Platform.runLater(() -> status.setText(String.format("Importação concluída: %d estados, %d municípios, %d registros populacionais.",
                            resultado.estadosInseridos(), resultado.municipiosInseridos(), resultado.populacoesInseridas())));
                } catch (Exception ex) {
                    Platform.runLater(() -> status.setText("Erro ao importar: " + ExceptionFormatter.detalhar(ex)));
                } finally {
                    Platform.runLater(() -> progress.setVisible(false));
                }
                return null;
            }
        };
        executor.submit(task);
    }

    private static class DaemonFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "dashboard-worker-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

    private static class DashboardView extends BorderPane {
        private final DashboardService dashboardService;
        private final ExecutorService executor;
        private final Runnable importarCsvAction;
        private final Runnable voltarHomeAction;

        private final ComboBox<Integer> comboAno = new ComboBox<>();
        private final ComboBox<RegiaoResumo> comboRegiao = new ComboBox<>();
        private final ComboBox<EstadoResumo> comboEstado = new ComboBox<>();
        private final ComboBox<MunicipioResumo> comboMunicipio = new ComboBox<>();
        private final Button btnAplicar = new Button("Aplicar filtro");
        private final Button btnLimpar = new Button("Limpar");
        private final Label lblMensagem = new Label();
        private final Label lblStatusErro = new Label();
        private final ProgressIndicator loadingOverlay = new ProgressIndicator();
        private Integer anoPadrao;

        private final BarChart<String, Number> barChart;
        private final LineChart<Number, Number> lineChart;

        DashboardView(DashboardService dashboardService, ExecutorService executor, Runnable importarCsvAction, Runnable voltarHomeAction) {
            this.dashboardService = dashboardService;
            this.executor = executor;
            this.importarCsvAction = importarCsvAction;
            this.voltarHomeAction = voltarHomeAction;

            setPadding(new Insets(12));
            setTop(criarTopo());

            CategoryAxis estadoAxis = new CategoryAxis();
            estadoAxis.setLabel("Estado");
            NumberAxis populacaoAxis = new NumberAxis();
            populacaoAxis.setLabel("População total (habitantes)");
            barChart = new BarChart<>(estadoAxis, populacaoAxis);
            barChart.setTitle("População total por estado (último ano disponível)");
            barChart.setLegendVisible(false);

            NumberAxis anoAxis = new NumberAxis();
            anoAxis.setLabel("Ano");
            anoAxis.setForceZeroInRange(false);
            NumberAxis habitantesAxis = new NumberAxis();
            habitantesAxis.setLabel("Habitantes");
            habitantesAxis.setForceZeroInRange(false);
            lineChart = new LineChart<>(anoAxis, habitantesAxis);
            lineChart.setTitle("Evolução populacional do município");
            lineChart.setLegendVisible(true);
            lineChart.setCreateSymbols(true);

            HBox charts = new HBox(12, barChart, lineChart);
            charts.setAlignment(Pos.CENTER);
            HBox.setHgrow(barChart, Priority.ALWAYS);
            HBox.setHgrow(lineChart, Priority.ALWAYS);
            barChart.setAnimated(false);
            lineChart.setAnimated(false);

            StackPane center = new StackPane(charts, loadingOverlay);
            loadingOverlay.setMaxSize(120, 120);
            loadingOverlay.setVisible(false);

            setCenter(center);
            setBottom(criarRodape());

            configurarEventos();
            carregarFiltrosIniciais();
        }

        private BorderPane criarTopo() {
            Button btnVoltar = new Button("Voltar");
            btnVoltar.setOnAction(e -> voltarHomeAction.run());

            Button btnImportar = new Button("Importar CSV");
            btnImportar.setOnAction(e -> importarCsvAction.run());

            HBox botoes = new HBox(8, btnVoltar, btnImportar);
            botoes.setAlignment(Pos.CENTER_LEFT);

            Label titulo = new Label("Dashboards");
            titulo.setFont(Font.font(20));

            BorderPane top = new BorderPane();
            top.setPadding(new Insets(8));
            top.setLeft(botoes);
            top.setCenter(titulo);
            BorderPane.setAlignment(titulo, Pos.CENTER);
            return top;
        }

        private VBox criarRodape() {
            comboAno.setPromptText("Ano obrigatório");
            comboAno.setDisable(true);
            comboRegiao.setPromptText("Região (opcional)");
            comboRegiao.setDisable(true);
            comboEstado.setPromptText("Estado (opcional)");
            comboMunicipio.setPromptText("Município (opcional)");
            comboMunicipio.setDisable(true);

            btnAplicar.setDisable(true);

            btnLimpar.setOnAction(e -> {
                comboAno.getSelectionModel().select(anoPadrao);
                comboRegiao.getSelectionModel().clearSelection();
                comboEstado.getSelectionModel().clearSelection();
                comboMunicipio.getSelectionModel().clearSelection();
                comboMunicipio.setDisable(true);
                carregarEstados(null, true);
            });

            btnAplicar.setOnAction(e -> {
                Integer estadoId = Optional.ofNullable(comboEstado.getValue()).map(EstadoResumo::id).orElse(null);
                Integer municipioId = Optional.ofNullable(comboMunicipio.getValue()).map(MunicipioResumo::id).orElse(null);
                carregarDashboard(estadoId, municipioId);
            });

            comboEstado.setOnAction(e -> {
                EstadoResumo selecionado = comboEstado.getSelectionModel().getSelectedItem();
                carregarMunicipios(selecionado == null ? null : selecionado.id());
            });

            comboMunicipio.setOnAction(e -> btnAplicar.setDisable(false));
            comboAno.setOnAction(e -> btnAplicar.setDisable(false));
            comboRegiao.setOnAction(e -> {
                Integer regiaoId = obterRegiaoSelecionada();
                comboEstado.getSelectionModel().clearSelection();
                comboMunicipio.getItems().clear();
                comboMunicipio.setDisable(true);
                btnAplicar.setDisable(true);
                carregarEstados(regiaoId, false);
            });

            GridPane filtros = new GridPane();
            filtros.setHgap(10);
            filtros.setVgap(10);
            filtros.setPadding(new Insets(12));

            filtros.add(new Label("Ano:"), 0, 0);
            filtros.add(comboAno, 1, 0);
            filtros.add(new Label("Região:"), 0, 1);
            filtros.add(comboRegiao, 1, 1);
            filtros.add(new Label("Estado:"), 0, 2);
            filtros.add(comboEstado, 1, 2);
            filtros.add(new Label("Município:"), 0, 3);
            filtros.add(comboMunicipio, 1, 3);

            HBox botoes = new HBox(8, btnAplicar, btnLimpar);
            botoes.setAlignment(Pos.CENTER_RIGHT);
            filtros.add(botoes, 1, 4);

            lblMensagem.setPadding(new Insets(0, 0, 6, 12));
            lblStatusErro.setPadding(new Insets(0, 0, 12, 12));
            lblStatusErro.getStyleClass().add("text-error");

            VBox rodape = new VBox(lblMensagem, lblStatusErro, filtros);
            return rodape;
        }

        private void configurarEventos() {
            Tooltip.install(barChart, new Tooltip("Clique em aplicar filtro para atualizar"));
            lblStatusErro.setStyle("-fx-text-fill: #c62828;");
        }

        private void carregarFiltrosIniciais() {
            setLoading(true);
            Task<List<Integer>> anosTask = new Task<>() {
                @Override
                protected List<Integer> call() {
                    return dashboardService.listarAnosDisponiveis();
                }
            };
            anosTask.setOnSucceeded(e -> {
                List<Integer> anos = anosTask.getValue();
                comboAno.setItems(FXCollections.observableArrayList(anos));
                if (!anos.isEmpty()) {
                    anoPadrao = anos.get(anos.size() - 1);
                    comboAno.getSelectionModel().select(anoPadrao);
                }
                comboAno.setDisable(false);
                carregarRegioes();
            });
            anosTask.setOnFailed(e -> {
                lblStatusErro.setText("Falha ao carregar anos: " + ExceptionFormatter.detalhar(anosTask.getException()));
                setLoading(false);
            });
            executor.submit(anosTask);
        }

        private void carregarRegioes() {
            Task<List<RegiaoResumo>> regiaoTask = new Task<>() {
                @Override
                protected List<RegiaoResumo> call() {
                    return dashboardService.listarRegioes();
                }
            };
            regiaoTask.setOnSucceeded(e -> {
                comboRegiao.setItems(FXCollections.observableArrayList(regiaoTask.getValue()));
                comboRegiao.getSelectionModel().clearSelection();
                comboRegiao.setDisable(false);
                carregarEstados(null, true);
            });
            regiaoTask.setOnFailed(e -> {
                lblStatusErro.setText("Falha ao carregar regiões: " + ExceptionFormatter.detalhar(regiaoTask.getException()));
                setLoading(false);
            });
            executor.submit(regiaoTask);
        }

        private void carregarEstados(Integer regiaoId, boolean atualizarDashboard) {
            setLoading(true);
            btnAplicar.setDisable(true);
            comboMunicipio.getItems().clear();
            comboMunicipio.setDisable(true);
            Task<List<EstadoResumo>> task = new Task<>() {
                @Override
                protected List<EstadoResumo> call() {
                    return dashboardService.listarEstadosPorRegiao(regiaoId);
                }
            };
            task.setOnSucceeded(e -> {
                comboEstado.setItems(FXCollections.observableArrayList(task.getValue()));
                comboEstado.getSelectionModel().clearSelection();
                btnAplicar.setDisable(false);
                if (atualizarDashboard) {
                    carregarDashboard(null, null);
                } else {
                    setLoading(false);
                }
            });
            task.setOnFailed(e -> {
                lblStatusErro.setText("Falha ao carregar estados: " + ExceptionFormatter.detalhar(task.getException()));
                setLoading(false);
            });
            executor.submit(task);
        }

        private void carregarMunicipios(Integer estadoId) {
            comboMunicipio.getItems().clear();
            if (estadoId == null) {
                comboMunicipio.setDisable(true);
                btnAplicar.setDisable(false);
                comboMunicipio.getSelectionModel().clearSelection();
                return;
            }

            comboMunicipio.setDisable(true);
            btnAplicar.setDisable(true);

            Task<List<MunicipioResumo>> task = new Task<>() {
                @Override
                protected List<MunicipioResumo> call() {
                    return dashboardService.listarMunicipios(estadoId);
                }
            };
            task.setOnSucceeded(e -> {
                comboMunicipio.setItems(FXCollections.observableArrayList(task.getValue()));
                comboMunicipio.setDisable(false);
                btnAplicar.setDisable(false);
            });
            task.setOnFailed(e -> lblStatusErro.setText("Falha ao carregar municípios: " + ExceptionFormatter.detalhar(task.getException())));
            executor.submit(task);
        }

        private void carregarDashboard(Integer estadoId, Integer municipioId) {
            setLoading(true);
            lblStatusErro.setText("");
            Integer anoSelecionado = obterAnoSelecionado();
            Integer regiaoId = obterRegiaoSelecionada();

            CompletableFuture<FiltroResolvido> filtroFuture = CompletableFuture.supplyAsync(
                    () -> dashboardService.resolverFiltro(estadoId, municipioId, anoSelecionado), executor);

            CompletableFuture<List<EstadoPopulacao>> barraFuture = filtroFuture.thenApplyAsync(filtro -> {
                logThread("Carregando barras", filtro, anoSelecionado, regiaoId);
                return dashboardService.carregarPopulacaoEstados(filtro, anoSelecionado, regiaoId);
            }, executor);

            CompletableFuture<List<LineChartSeries>> linhaFuture = filtroFuture.thenApplyAsync(filtro -> {
                logThread("Carregando linhas", filtro, anoSelecionado, regiaoId);
                return carregarSeriesLinha(filtro);
            }, executor);

            barraFuture.whenCompleteAsync((dados, ex) -> Platform.runLater(() -> {
                if (ex == null) {
                    atualizarBarChart(dados);
                } else {
                    lblStatusErro.setText("Erro no gráfico de barras: " + ExceptionFormatter.detalhar(unwrap(ex)));
                }
            }), executor);

            linhaFuture.whenCompleteAsync((dados, ex) -> Platform.runLater(() -> {
                if (ex == null) {
                    atualizarLineChart(dados);
                } else {
                    lblStatusErro.setText("Erro no gráfico de linha: " + ExceptionFormatter.detalhar(unwrap(ex)));
                }
            }), executor);

            filtroFuture.whenCompleteAsync((filtro, ex) -> Platform.runLater(() -> {
                if (ex == null) {
                    lblMensagem.setText(dashboardService.mensagemParaFiltro(filtro));
                } else {
                    lblStatusErro.setText("Erro ao resolver filtros: " + ExceptionFormatter.detalhar(unwrap(ex)));
                }
            }), executor);

            CompletableFuture.allOf(barraFuture, linhaFuture)
                    .whenCompleteAsync((v, ex) -> Platform.runLater(() -> setLoading(false)), executor);
        }

        private void atualizarBarChart(List<EstadoPopulacao> dados) {
            barChart.setTitle(tituloGraficoBarras());
            barChart.getData().clear();
            int index = 0;
            for (EstadoPopulacao d : dados) {
                XYChart.Series<String, Number> serie = new XYChart.Series<>();
                String categoria = (d.sigla() != null && !d.sigla().isBlank()) ? d.sigla() : d.estado();
                serie.setName(d.estado());
                serie.getData().add(new XYChart.Data<>(categoria, d.habitantes()));
                barChart.getData().add(serie);
                Color color = pickColor(index++);
                aplicarCorBarra(serie, color);
            }
        }

        private void atualizarLineChart(List<LineChartSeries> dados) {
            lineChart.getData().clear();
            int index = 0;
            for (LineChartSeries dto : dados) {
                if (dto.pontos().isEmpty()) {
                    continue;
                }
                XYChart.Series<Number, Number> serie = new XYChart.Series<>();
                serie.setName(dto.nome());
                for (PontoSerieTemporal p : dto.pontos()) {
                    serie.getData().add(new XYChart.Data<>(p.ano(), p.habitantes()));
                }
                lineChart.getData().add(serie);
                aplicarCorLinha(serie, pickColor(index++));
            }
        }

        private void setLoading(boolean loading) {
            loadingOverlay.setVisible(loading);
        }

        private Integer obterAnoSelecionado() {
            Integer selecionado = comboAno.getValue();
            if (selecionado != null) {
                return selecionado;
            }
            return anoPadrao;
        }

        private Integer obterRegiaoSelecionada() {
            return Optional.ofNullable(comboRegiao.getValue()).map(RegiaoResumo::id).orElse(null);
        }

        private List<LineChartSeries> carregarSeriesLinha(FiltroResolvido filtro) {
            if (filtro.municipio() != null) {
                List<PontoSerieTemporal> pontos = dashboardService.carregarSerieTemporal(filtro);
                return List.of(new LineChartSeries(filtro.municipio().nome(), pontos));
            }

            List<EstadoResumo> estados = dashboardService.listarEstadosPorRegiao(obterRegiaoSelecionada());
            return estados.stream().map(estado -> {
                List<MunicipioResumo> municipios = dashboardService.listarMunicipios(estado.id());
                return municipios.stream().map(municipio -> {
                    FiltroResolvido filtroMunicipio = new FiltroResolvido(estado, municipio);
                    List<PontoSerieTemporal> serie = dashboardService.carregarSerieTemporal(filtroMunicipio);
                    return new LineChartSeries(municipio.nome() + " (" + estado.sigla() + ")", serie);
                }).toList();
            }).flatMap(List::stream).toList();
        }

        private String tituloGraficoBarras() {
            StringBuilder titulo = new StringBuilder("População total por estado");
            Integer ano = obterAnoSelecionado();
            if (ano != null) {
                titulo.append(" — ").append(ano);
            }
            RegiaoResumo regiao = comboRegiao.getValue();
            if (regiao != null) {
                titulo.append(" | ").append(regiao.nome());
            }
            return titulo.toString();
        }

        private void logThread(String acao, FiltroResolvido filtro, Integer ano, Integer regiaoId) {
            System.out.println("[" + Thread.currentThread().getName() + "] " + acao +
                " | ano=" + (ano == null ? "*" : ano) +
                " | regiao=" + (regiaoId == null ? "*" : regiaoId) +
                " | estado=" + (filtro.estado() == null ? "*" : filtro.estado().sigla()) +
                " | municipio=" + (filtro.municipio() == null ? "*" : filtro.municipio().nome()));
        }

        private Color pickColor(int index) {
            Color[] palette = new Color[]{
                    Color.web("#E76F51"),
                    Color.web("#2A9D8F"),
                    Color.web("#264653"),
                    Color.web("#F4A261"),
                    Color.web("#8AB17D"),
                    Color.web("#588157"),
                    Color.web("#BC4749")
            };
            return palette[index % palette.length];
        }

        private void aplicarCorBarra(XYChart.Series<String, Number> serie, Color color) {
            serie.nodeProperty().addListener((obs, oldNode, newNode) -> pintarBarras(newNode, color));
            pintarBarras(serie.getNode(), color);
        }

        private void aplicarCorLinha(XYChart.Series<Number, Number> serie, Color color) {
            serie.nodeProperty().addListener((obs, oldNode, newNode) -> pintarLinha(newNode, color));
            pintarLinha(serie.getNode(), color);
            serie.getData().forEach(data -> {
                data.nodeProperty().addListener((o, oldNode, newNode) -> pintarSimbolo(newNode, color));
                pintarSimbolo(data.getNode(), color);
            });
        }

        private void pintarBarras(Node node, Color color) {
            if (node == null) return;
            node.lookupAll(".chart-bar").forEach(bar -> bar.setStyle("-fx-bar-fill: " + toRgb(color) + ";"));
        }

        private void pintarLinha(Node node, Color color) {
            if (node == null) return;
            node.setStyle("-fx-stroke: " + toRgb(color) + ";");
            node.lookupAll(".chart-series-line").forEach(line -> line.setStyle("-fx-stroke: " + toRgb(color) + ";"));
        }

        private void pintarSimbolo(Node node, Color color) {
            if (node == null) return;
            node.setStyle("-fx-background-color: " + toRgb(color) + ";");
        }

        private String toRgb(Color color) {
            int r = (int) Math.round(color.getRed() * 255);
            int g = (int) Math.round(color.getGreen() * 255);
            int b = (int) Math.round(color.getBlue() * 255);
            return String.format("rgb(%d,%d,%d)", r, g, b);
        }

        private record LineChartSeries(String nome, List<PontoSerieTemporal> pontos) {
        }

        private Throwable unwrap(Throwable ex) {
            if (ex instanceof CompletionException ce && ce.getCause() != null) {
                return ce.getCause();
            }
            return ex;
        }
    }
}
