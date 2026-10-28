package application;

import java.io.File;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class Main extends Application {

    private Graph graph;

    private Pane mapPane;
    private ComboBox<City> sourceComboBox;
    private ComboBox<City> targetComboBox;
    private TextArea pathArea;
    private TextField distanceField;
    private Label statusLabel;
    private Label sourceValueLabel;
    private Label targetValueLabel;
    private CheckBox showNamesCheckBox;
    private City selectedSource = null;
    private City selectedTarget = null;

    private final ArrayList<Line> pathLines = new ArrayList<>();
    private final ArrayList<Circle> cityCircles = new ArrayList<>();
    private final ArrayList<Label> cityLabels = new ArrayList<>();

    
    private static final double MAP_WIDTH = 560;
    private static final double MAP_HEIGHT = 730;
    private static final double MIN_LAT = 31.22;
    private static final double MAX_LAT = 31.59;
    private static final double MIN_LON = 34.16;
    private static final double MAX_LON = 34.62;

    @Override
    public void start(Stage primaryStage) {
        graph = MapFileReader.readGraph("data/gazamap.txt");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #eaf1ff, #f8fafc);");

        mapPane = new Pane();
        mapPane.setPrefSize(MAP_WIDTH, MAP_HEIGHT);
        mapPane.setMinSize(MAP_WIDTH, MAP_HEIGHT);
        mapPane.setMaxSize(MAP_WIDTH, MAP_HEIGHT);
        mapPane.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #1e3a8a;" +
                "-fx-border-width: 2;" +
                "-fx-background-radius: 12;" +
                "-fx-border-radius: 12;"
        );

        loadMapImage();
        calculateCityPositions();
        drawCities();

        VBox leftSide = createMapSection();
        VBox rightSide = createControlPanel();

        root.setCenter(leftSide);
        root.setRight(rightSide);

        Scene scene = new Scene(root, 980, 790);

        primaryStage.setTitle("Dijkstra Shortest Path - Gaza Map");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private VBox createMapSection() {
        Label title = new Label("Gaza Strip Shortest Path Map");
        title.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #172554;"
        );

        Label subtitle = new Label("Cities are loaded from the input file using real latitude and longitude.");
        subtitle.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #475569;"
        );

        ScrollPane scrollPane = new ScrollPane(mapPane);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setPannable(true);
        scrollPane.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;"
        );

        VBox mapCard = new VBox(10);
        mapCard.setAlignment(Pos.TOP_CENTER);
        mapCard.setPadding(new Insets(14));
        mapCard.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-border-radius: 16;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.16), 12, 0, 0, 4);"
        );
        mapCard.getChildren().addAll(title, subtitle, scrollPane);

        VBox wrapper = new VBox(mapCard);
        wrapper.setPadding(new Insets(16));
        wrapper.setAlignment(Pos.TOP_CENTER);

        return wrapper;
    }

    private VBox createControlPanel() {
        Label titleLabel = new Label("Route Control");
        titleLabel.setStyle(
                "-fx-font-size: 22px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #172554;"
        );

        Label infoLabel = new Label("Choose source and target using the mouse or the drop-down lists.");
        infoLabel.setWrapText(true);
        infoLabel.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-text-fill: #475569;"
        );

        Separator separator1 = new Separator();

        Label sourceLabel = createSmallTitle("Source City");
        sourceComboBox = new ComboBox<>();
        sourceComboBox.setItems(FXCollections.observableArrayList(graph.getCities()));
        sourceComboBox.setPrefWidth(250);
        sourceComboBox.setPromptText("Select source");

        Label targetLabel = createSmallTitle("Target City");
        targetComboBox = new ComboBox<>();
        targetComboBox.setItems(FXCollections.observableArrayList(graph.getCities()));
        targetComboBox.setPrefWidth(250);
        targetComboBox.setPromptText("Select target");

        sourceComboBox.setOnAction(e -> {
            selectedSource = sourceComboBox.getValue();
            updateSelectedLabels();
            refreshCityColors();
        });

        targetComboBox.setOnAction(e -> {
            selectedTarget = targetComboBox.getValue();
            updateSelectedLabels();
            refreshCityColors();
        });

        sourceValueLabel = new Label("Source: Not selected");
        sourceValueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #166534; -fx-font-weight: bold;");

        targetValueLabel = new Label("Target: Not selected");
        targetValueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1d4ed8; -fx-font-weight: bold;");

        Button runButton = new Button("Run Dijkstra");
        runButton.setPrefWidth(130);
        runButton.setPrefHeight(36);
        runButton.setStyle(
                "-fx-background-color: #2563eb;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;"
        );
        runButton.setOnAction(e -> runDijkstra());

        Button clearButton = new Button("Clear");
        clearButton.setPrefWidth(90);
        clearButton.setPrefHeight(36);
        clearButton.setStyle(
                "-fx-background-color: #e2e8f0;" +
                "-fx-text-fill: #0f172a;" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;"
        );
        clearButton.setOnAction(e -> clearResult());

        HBox buttons = new HBox(12, runButton, clearButton);
        buttons.setAlignment(Pos.CENTER);

        showNamesCheckBox = new CheckBox("Show city names");
        showNamesCheckBox.setSelected(true);
        showNamesCheckBox.setStyle("-fx-font-size: 12px; -fx-text-fill: #334155;");
        showNamesCheckBox.setOnAction(e -> {
            boolean visible = showNamesCheckBox.isSelected();
            for (Label label : cityLabels) {
                label.setVisible(visible);
            }
        });

        Separator separator2 = new Separator();

        Label pathLabel = createSmallTitle("Shortest Path");
        pathArea = new TextArea();
        pathArea.setPrefSize(250, 230);
        pathArea.setEditable(false);
        pathArea.setWrapText(true);
        pathArea.setStyle(
                "-fx-font-size: 12px;" +
                "-fx-control-inner-background: #f8fafc;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-border-radius: 8;"
        );

        Label distanceLabel = createSmallTitle("Total Distance");
        distanceField = new TextField();
        distanceField.setEditable(false);
        distanceField.setPrefWidth(250);
        distanceField.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #172554;" +
                "-fx-control-inner-background: #f8fafc;"
        );

        Label legendTitle = createSmallTitle("Legend");
        Label legend = new Label("● Red: City    ● Green: Source    ● Blue: Target    ━ Blue Line: Shortest Path");
        legend.setWrapText(true);
        legend.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");

        statusLabel = new Label("Click a city: first click selects Source, second click selects Target.");
        statusLabel.setWrapText(true);
        statusLabel.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: #475569;" +
                "-fx-background-color: #f1f5f9;" +
                "-fx-padding: 8px;" +
                "-fx-background-radius: 8;"
        );

        VBox panel = new VBox(11);
        panel.setPadding(new Insets(18));
        panel.setPrefWidth(310);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #cbd5e1;" +
                "-fx-border-width: 0 0 0 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, -3, 0);"
        );

        VBox.setVgrow(pathArea, Priority.ALWAYS);

        panel.getChildren().addAll(
                titleLabel,
                infoLabel,
                separator1,
                sourceLabel,
                sourceComboBox,
                targetLabel,
                targetComboBox,
                sourceValueLabel,
                targetValueLabel,
                buttons,
                showNamesCheckBox,
                separator2,
                pathLabel,
                pathArea,
                distanceLabel,
                distanceField,
                legendTitle,
                legend,
                statusLabel
        );

        return panel;
    }

    private Label createSmallTitle(String text) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 13px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #1e293b;"
        );
        return label;
    }

    private void loadMapImage() {
        try {
            File imageFile = null;

            File jpg = new File("images/gaza_map.jpg");
            File jpeg = new File("images/gaza_map.jpeg");
            File png = new File("images/gaza_map.png");

            if (jpg.exists()) {
                imageFile = jpg;
            } else if (jpeg.exists()) {
                imageFile = jpeg;
            } else if (png.exists()) {
                imageFile = png;
            }

            if (imageFile != null) {
                Image image = new Image(imageFile.toURI().toString());
                ImageView imageView = new ImageView(image);

                imageView.setFitWidth(MAP_WIDTH);
                imageView.setFitHeight(MAP_HEIGHT);
                imageView.setPreserveRatio(false);

                mapPane.getChildren().add(imageView);
            } else {
                mapPane.setStyle("-fx-background-color: #e8f4ff; -fx-border-color: #1e3a8a;");
                System.out.println("Map image not found in images folder.");
            }

        } catch (Exception e) {
            mapPane.setStyle("-fx-background-color: #e8f4ff; -fx-border-color: #1e3a8a;");
            System.out.println("Error loading map image: " + e.getMessage());
        }
    }

    private void calculateCityPositions() {
        for (City city : graph.getCities()) {
            double x = longitudeToX(city.getLongitude());
            double y = latitudeToY(city.getLatitude());

            x = Math.max(10, Math.min(MAP_WIDTH - 10, x));
            y = Math.max(10, Math.min(MAP_HEIGHT - 10, y));

            city.setX(x);
            city.setY(y);
        }
    }

    private double longitudeToX(double longitude) {
        return ((longitude - MIN_LON) / (MAX_LON - MIN_LON)) * MAP_WIDTH;
    }

    private double latitudeToY(double latitude) {
        return ((MAX_LAT - latitude) / (MAX_LAT - MIN_LAT)) * MAP_HEIGHT;
    }

    private void drawCities() {
        for (City city : graph.getCities()) {
            Circle circle = new Circle(city.getX(), city.getY(), 4.5);
            circle.setFill(Color.RED);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(1.5);
            circle.setUserData(city);

            Label label = new Label(city.getName().replace("_", " "));
            label.setStyle(
                    "-fx-font-size: 7px;" +
                    "-fx-font-weight: bold;" +
                    "-fx-text-fill: #111827;" +
                    "-fx-background-color: rgba(255,255,255,0.78);" +
                    "-fx-padding: 1px 2px 1px 2px;" +
                    "-fx-background-radius: 3;"
            );

            double[] offset = getLabelOffset(city.getName());
            label.setLayoutX(city.getX() + offset[0]);
            label.setLayoutY(city.getY() + offset[1]);

            Tooltip tooltip = new Tooltip(city.getName().replace("_", " "));
            Tooltip.install(circle, tooltip);
            Tooltip.install(label, tooltip);

            circle.setOnMouseClicked(e -> handleCityClick(city));
            label.setOnMouseClicked(e -> handleCityClick(city));

            cityCircles.add(circle);
            cityLabels.add(label);

            mapPane.getChildren().addAll(circle, label);
        }
    }

    private double[] getLabelOffset(String cityName) {
        switch (cityName) {
            case "Beit_Lahia":
                return new double[]{7, -12};
            case "Erez_Crossing":
                return new double[]{7, -12};
            case "Jabalia":
                return new double[]{7, -10};
            case "Beit_Hanoun":
                return new double[]{7, -10};
            case "Gaza_City":
                return new double[]{7, -10};
            case "Shati_Camp":
                return new double[]{7, -10};

            case "Al_Mughraqa":
                return new double[]{7, -10};
            case "Nuseirat_Camp":
                return new double[]{7, -10};
            case "Al_Nuseirat":
                return new double[]{7, 4};
            case "Az_Zawayda":
                return new double[]{7, 4};
            case "Deir_Al_Balah_Camp":
                return new double[]{7, -10};
            case "Deir_Al_Balah":
                return new double[]{7, 4};
            case "Al_Bureij":
                return new double[]{7, -10};
            case "Al_Maghazi":
                return new double[]{7, 4};
            case "Wadi_As_Salqa":
                return new double[]{7, -10};

            case "Al_Qarara":
                return new double[]{7, -10};
            case "Al_Amal":
                return new double[]{7, -10};
            case "Bani_Suheila":
                return new double[]{7, 4};
            case "Khan_Younis":
                return new double[]{7, 4};
            case "Khan_Younis_Camp":
                return new double[]{7, -12};

            case "Khirbat_Al_Adas":
                return new double[]{7, -10};
            case "Rafah_Camp":
                return new double[]{7, 4};
            case "Rafah":
                return new double[]{7, -10};

            default:
                return new double[]{7, -10};
        }
    }

    private void handleCityClick(City city) {
        if (selectedSource == null) {
            selectedSource = city;
            sourceComboBox.setValue(city);
            statusLabel.setText("Source selected: " + cleanName(city));
        } else if (selectedTarget == null && city != selectedSource) {
            selectedTarget = city;
            targetComboBox.setValue(city);
            statusLabel.setText("Target selected: " + cleanName(city));
        } else {
            selectedSource = city;
            selectedTarget = null;
            sourceComboBox.setValue(city);
            targetComboBox.setValue(null);
            clearPathLines();
            pathArea.clear();
            distanceField.clear();
            statusLabel.setText("New source selected: " + cleanName(city));
        }

        updateSelectedLabels();
        refreshCityColors();
    }

    private void updateSelectedLabels() {
        if (selectedSource == null) {
            sourceValueLabel.setText("Source: Not selected");
        } else {
            sourceValueLabel.setText("Source: " + cleanName(selectedSource));
        }

        if (selectedTarget == null) {
            targetValueLabel.setText("Target: Not selected");
        } else {
            targetValueLabel.setText("Target: " + cleanName(selectedTarget));
        }
    }

    private void refreshCityColors() {
        for (Circle circle : cityCircles) {
            City city = (City) circle.getUserData();

            if (city == selectedSource) {
                circle.setFill(Color.LIMEGREEN);
                circle.setRadius(6);
            } else if (city == selectedTarget) {
                circle.setFill(Color.DODGERBLUE);
                circle.setRadius(6);
            } else {
                circle.setFill(Color.RED);
                circle.setRadius(4.5);
            }
        }
    }

    private void runDijkstra() {
        selectedSource = sourceComboBox.getValue();
        selectedTarget = targetComboBox.getValue();

        if (selectedSource == null || selectedTarget == null) {
            showAlert("Please choose source and target cities.");
            return;
        }

        if (selectedSource == selectedTarget) {
            showAlert("Source and target cannot be the same city.");
            return;
        }

        clearPathLines();

        Dijkstra.Result result = Dijkstra.findShortestPath(graph, selectedSource, selectedTarget);

        if (result.getPath().isEmpty() || result.getDistance() == Double.MAX_VALUE) {
            pathArea.setText("No path found.");
            distanceField.setText("");
            statusLabel.setText("No path found between selected cities.");
            return;
        }

        displayPath(result.getPath());
        distanceField.setText(String.format("%.2f km", result.getDistance()));
        drawPath(result.getPath());
        refreshCityColors();
        updateSelectedLabels();

        statusLabel.setText("Shortest path calculated successfully.");
    }

    private void displayPath(ArrayList<City> path) {
        StringBuilder sb = new StringBuilder();

        if (path == null || path.isEmpty()) {
            pathArea.setText("No path found.");
            return;
        }

        for (int i = 0; i < path.size(); i++) {
            sb.append(i + 1).append(". ").append(cleanName(path.get(i)));

            if (i != path.size() - 1) {
                sb.append("\n↓\n");
            }
        }

        pathArea.setText(sb.toString());
    }

    private void drawPath(ArrayList<City> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            City from = path.get(i);
            City to = path.get(i + 1);

            Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
            line.setStroke(Color.web("#2563eb"));
            line.setStrokeWidth(4.5);
            line.setOpacity(0.88);

            pathLines.add(line);
            mapPane.getChildren().add(line);
            line.toFront();
        }

        ArrayList<Node> nodesCopy = new ArrayList<>(mapPane.getChildren());

        for (Node node : nodesCopy) {
            if (node instanceof Circle || node instanceof Label) {
                node.toFront();
            }
        }
    }

    private void clearResult() {
        selectedSource = null;
        selectedTarget = null;

        sourceComboBox.setValue(null);
        targetComboBox.setValue(null);

        pathArea.clear();
        distanceField.clear();

        clearPathLines();
        refreshCityColors();
        updateSelectedLabels();

        statusLabel.setText("Click a city: first click selects Source, second click selects Target.");
    }

    private void clearPathLines() {
        mapPane.getChildren().removeAll(pathLines);
        pathLines.clear();
    }

    private String cleanName(City city) {
        return city.getName().replace("_", " ");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}