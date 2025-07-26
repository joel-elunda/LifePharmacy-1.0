package lifepharmacy.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lifepharmacy.models.Produit;
import lifepharmacy.services.StockService;

public class ReapprovisionnementForm extends Stage {

    private StockService stockService;

    private ComboBox<Produit> produitComboBox;
    private TextField quantiteAAjouterField;
    private Label currentStockLabel; // Pour afficher le stock actuel

    private Button reapprovisionnerButton;
    private Button annulerButton;

    public ReapprovisionnementForm(StockService stockService) {
        this.stockService = stockService;

        setTitle("Réapprovisionnement du Stock");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        // Champ de sélection du produit
        Label produitLabel = new Label("Sélectionner un produit:");
        produitComboBox = new ComboBox<>();
        produitComboBox.setPromptText("Sélectionner un produit");
        // Charger tous les produits
        produitComboBox.setItems(FXCollections.observableArrayList(stockService.getAllProduits()));
        produitComboBox.setConverter(new StringConverter<Produit>() {
            @Override
            public String toString(Produit produit) {
                return (produit != null) ? produit.getNom() : "";
            }

            @Override
            public Produit fromString(String string) {
                return null;
            }
        });
        // Listener pour mettre à jour l'affichage du stock actuel
        produitComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentStockLabel.setText("Stock actuel: " + newVal.getQuantiteEnStock());
            } else {
                currentStockLabel.setText("Stock actuel: N/A");
            }
        });

        // Affichage du stock actuel
        currentStockLabel = new Label("Stock actuel: N/A");
        currentStockLabel.setStyle("-fx-font-weight: bold;");

        // Champ pour la quantité à ajouter
        Label quantiteLabel = new Label("Quantité à ajouter:");
        quantiteAAjouterField = new TextField();
        quantiteAAjouterField.setPromptText("Entrez la quantité");
        quantiteAAjouterField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        formGrid.add(produitLabel, 0, 0);
        formGrid.add(produitComboBox, 1, 0);
        formGrid.add(currentStockLabel, 2, 0); // Positionner à côté du ComboBox
        formGrid.add(quantiteLabel, 0, 1);
        formGrid.add(quantiteAAjouterField, 1, 1);

        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        reapprovisionnerButton = new Button("Réapprovisionner");
        annulerButton = new Button("Annuler");

        buttonBox.getChildren().addAll(reapprovisionnerButton, annulerButton);

        root.getChildren().addAll(formGrid, buttonBox);

        Scene scene = new Scene(root, 450, 250);
        setScene(scene);

        // Actions des boutons
        reapprovisionnerButton.setOnAction(e -> handleReapprovisionnement());
        annulerButton.setOnAction(e -> close());
    }

    private void handleReapprovisionnement() {
        Produit selectedProduit = produitComboBox.getSelectionModel().getSelectedItem();
        if (selectedProduit == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un produit.");
            return;
        }

        int quantiteAjoutee;
        try {
            quantiteAjoutee = Integer.parseInt(quantiteAAjouterField.getText());
            if (quantiteAjoutee <= 0) {
                showAlert(Alert.AlertType.WARNING, "Attention", "La quantité à ajouter doit être supérieure à zéro.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer une quantité valide.");
            return;
        }

        try {
            stockService.reapprovisionnerProduit(selectedProduit.getId(), quantiteAjoutee);
            showAlert(Alert.AlertType.INFORMATION, "Succès", selectedProduit.getNom() + " a été réapprovisionné de " + quantiteAjoutee + " unités. Nouveau stock: " + stockService.getProduitById(selectedProduit.getId()).get().getQuantiteEnStock());
            clearFields();
            // Mettre à jour la liste des produits dans la ComboBox pour refléter le nouveau stock
            produitComboBox.setItems(FXCollections.observableArrayList(stockService.getAllProduits()));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du réapprovisionnement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        produitComboBox.getSelectionModel().clearSelection();
        quantiteAAjouterField.clear();
        currentStockLabel.setText("Stock actuel: N/A");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}