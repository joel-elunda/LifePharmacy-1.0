package lifepharmacy.ui;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lifepharmacy.services.FacturationService;

import java.time.LocalDate;

public class RecettesForm extends Stage {

    private FacturationService facturationService;

    private DatePicker debutDatePicker;
    private DatePicker finDatePicker;
    private Label beneficesLabel;

    public RecettesForm(FacturationService facturationService) {
        this.facturationService = facturationService;

        setTitle("Consultation des Bénéfices");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        Label debutLabel = new Label("Date de début:");
        debutDatePicker = new DatePicker(LocalDate.now().minusMonths(1)); // Par défaut, 1 mois en arrière

        Label finLabel = new Label("Date de fin:");
        finDatePicker = new DatePicker(LocalDate.now()); // Par défaut, aujourd'hui

        beneficesLabel = new Label("Bénéfices calculés: 0.00 CDF");
        beneficesLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: green;");


        formGrid.add(debutLabel, 0, 0);
        formGrid.add(debutDatePicker, 1, 0);
        formGrid.add(finLabel, 0, 1);
        formGrid.add(finDatePicker, 1, 1);

        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        Button calculerButton = new Button("Calculer les Bénéfices");
        Button closeButton = new Button("Fermer");

        buttonBox.getChildren().addAll(calculerButton, closeButton);

        root.getChildren().addAll(formGrid, beneficesLabel, buttonBox);

        Scene scene = new Scene(root, 400, 250);
        setScene(scene);

        // Actions des boutons
        calculerButton.setOnAction(e -> handleCalculerBenefices());
        closeButton.setOnAction(e -> close());

        // Calcul initial
        handleCalculerBenefices();
    }

    private void handleCalculerBenefices() {
        LocalDate debut = debutDatePicker.getValue();
        LocalDate fin = finDatePicker.getValue();

        if (debut == null || fin == null) {
            showAlert(Alert.AlertType.WARNING, "Dates manquantes", "Veuillez sélectionner les dates de début et de fin.");
            return;
        }
        if (fin.isBefore(debut)) {
            showAlert(Alert.AlertType.WARNING, "Erreur de dates", "La date de fin ne peut pas être antérieure à la date de début.");
            return;
        }

        double benefices = facturationService.calculerBeneficesPeriode(debut, fin);
        beneficesLabel.setText(String.format("Bénéfices calculés: %.2f CDF", benefices));
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}