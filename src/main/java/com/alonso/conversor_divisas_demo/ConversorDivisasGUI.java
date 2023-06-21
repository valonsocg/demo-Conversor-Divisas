package com.alonso.conversor_divisas_demo;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ConversorDivisasGUI extends Application {

    private static final String API_KEY = "69ebc9bdae4397733782805f";

    private Map<Integer, String> codigoDivisas;
    private ComboBox<String> origenComboBox;
    private ComboBox<String> destinoComboBox;
    private TextField cantidadTextField;
    private Label resultadoLabel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        codigoDivisas = crearMapaDivisas();

        primaryStage.setTitle("Convertidor de Divisas");

        GridPane gridPane = createGridPane();
        addLabelsAndComboBoxes(gridPane);
        addAmountTextField(gridPane);
        addConvertButton(gridPane);
        addResultadoLabel(gridPane);

        Scene scene = new Scene(gridPane, 400, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setAlignment(Pos.CENTER);
        return gridPane;
    }

    private void addLabelsAndComboBoxes(GridPane gridPane) {
        Label origenLabel = new Label("Divisa de origen:");
        origenComboBox = new ComboBox<>();
        origenComboBox.getItems().addAll(codigoDivisas.values());

        Label destinoLabel = new Label("Divisa de destino:");
        destinoComboBox = new ComboBox<>();
        destinoComboBox.getItems().addAll(codigoDivisas.values());

        gridPane.add(origenLabel, 0, 0);
        gridPane.add(origenComboBox, 1, 0);
        gridPane.add(destinoLabel, 0, 1);
        gridPane.add(destinoComboBox, 1, 1);
    }

    private void addAmountTextField(GridPane gridPane) {
        Label cantidadLabel = new Label("Cantidad:");
        cantidadTextField = new TextField();

        gridPane.add(cantidadLabel, 0, 2);
        gridPane.add(cantidadTextField, 1, 2);
    }

    private void addConvertButton(GridPane gridPane) {
        Button convertButton = new Button("Convertir");
        convertButton.setOnAction(event -> {
            String divisaOrigen = origenComboBox.getValue();
            String divisaDestino = destinoComboBox.getValue();
            BigDecimal monto = new BigDecimal(cantidadTextField.getText());

            try {
                BigDecimal resultado = sendHttpGETRequest(divisaOrigen, divisaDestino, monto);
                resultadoLabel.setText(monto + " " + divisaOrigen + " = " + resultado + " " + divisaDestino);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().add(convertButton);
        hbox.setAlignment(Pos.CENTER);
        gridPane.add(hbox, 1, 3);
    }

    private void addResultadoLabel(GridPane gridPane) {
        resultadoLabel = new Label();
        gridPane.add(resultadoLabel, 0, 4, 2, 1);
    }

    private Map<Integer, String> crearMapaDivisas() {
        Map<Integer, String> codigoDivisas = new HashMap<>();

        codigoDivisas.put(1, "USD");
        codigoDivisas.put(2, "EUR");
        codigoDivisas.put(3, "GBP");
        codigoDivisas.put(4, "JPY");
        codigoDivisas.put(5, "KRW");
        codigoDivisas.put(6, "PEN");

        return codigoDivisas;
    }

    private BigDecimal sendHttpGETRequest(String divisaOrigen, String divisaDestino, BigDecimal monto) throws IOException {
        String GET_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/pair/" + divisaOrigen + "/" + divisaDestino;
        URL url = new URL(GET_URL);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("GET");
        int responseCode = httpURLConnection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();

            JSONObject obj = new JSONObject(response.toString());
            BigDecimal tasaDeCambio = obj.getBigDecimal("conversion_rate");
            return monto.multiply(tasaDeCambio).setScale(2, BigDecimal.ROUND_HALF_UP);
        } else {
            System.out.println("GET request failed");
        }

        return BigDecimal.ZERO;
    }
}