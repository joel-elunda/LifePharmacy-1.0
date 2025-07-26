package lifepharmacy.ui;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lifepharmacy.models.Produit;
import lifepharmacy.services.StockService;

import java.time.LocalDate;
import java.util.UUID;

public class ProductForm extends Stage {

    private StockService stockService;
    private ObservableList<Produit> productList;
    private TableView<Produit> productTable;

    // Champs du formulaire
    private TextField idField;
    private TextField nomField;
    private TextArea descriptionArea;
    private TextField prixUnitaireField;
    private TextField quantiteEnStockField;
    private DatePicker datePeremptionPicker;

    // Boutons
    private Button addButton;
    private Button saveButton; // Pour modifier
    private Button deleteButton;
    private Button clearButton; // Pour vider les champs
    private Button closeButton;

    public ProductForm(StockService stockService) {
        this.stockService = stockService;
        this.productList = FXCollections.observableArrayList(stockService.getAllProduits());

        setTitle("Gestion des Produits");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);

        // --- Formulaire de saisie ---
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10));

        Label idLabel = new Label("ID Produit:");
        idField = new TextField();
        idField.setDisable(true); // L'ID sera généré automatiquement ou géré par la sélection du tableau

        Label nomLabel = new Label("Nom:");
        nomField = new TextField();

        Label descriptionLabel = new Label("Description:");
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(3);

        Label prixUnitaireLabel = new Label("Prix Unitaire:");
        prixUnitaireField = new TextField();
        prixUnitaireField.setTextFormatter(new TextFormatter<>(change -> { // Accepter seulement les nombres et décimales
            if (change.getControlNewText().matches("\\d*\\.?\\d*")) {
                return change;
            }
            return null;
        }));

        Label quantiteEnStockLabel = new Label("Quantité en Stock:");
        quantiteEnStockField = new TextField();
        quantiteEnStockField.setTextFormatter(new TextFormatter<>(change -> { // Accepter seulement les nombres entiers
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        Label datePeremptionLabel = new Label("Date de Péremption:");
        datePeremptionPicker = new DatePicker(LocalDate.now());

        formGrid.add(idLabel, 0, 0);
        formGrid.add(idField, 1, 0);
        formGrid.add(nomLabel, 0, 1);
        formGrid.add(nomField, 1, 1);
        formGrid.add(descriptionLabel, 0, 2);
        formGrid.add(descriptionArea, 1, 2);
        formGrid.add(prixUnitaireLabel, 0, 3);
        formGrid.add(prixUnitaireField, 1, 3);
        formGrid.add(quantiteEnStockLabel, 0, 4);
        formGrid.add(quantiteEnStockField, 1, 4);
        formGrid.add(datePeremptionLabel, 0, 5);
        formGrid.add(datePeremptionPicker, 1, 5);

        // --- Boutons d'action ---
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        addButton = new Button("Ajouter");
        saveButton = new Button("Enregistrer (Modifier)");
        deleteButton = new Button("Supprimer");
        clearButton = new Button("Effacer");
        closeButton = new Button("Fermer");

        buttonBox.getChildren().addAll(addButton, saveButton, deleteButton, clearButton, closeButton);

        // --- Tableau d'affichage des produits ---
        productTable = new TableView<>();
        TableColumn<Produit, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Produit, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Produit, Double> prixCol = new TableColumn<>("Prix");
        prixCol.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));

        TableColumn<Produit, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("quantiteEnStock"));

        TableColumn<Produit, LocalDate> peremptionCol = new TableColumn<>("Péremption");
        peremptionCol.setCellValueFactory(new PropertyValueFactory<>("datePeremption"));

        productTable.getColumns().addAll(idCol, nomCol, prixCol, stockCol, peremptionCol);
        productTable.setItems(productList);

        // Listener pour la sélection du tableau
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayProductDetails(newSelection);
            } else {
                clearFields();
            }
        });

        root.getChildren().addAll(formGrid, buttonBox, new Separator(), productTable);

        Scene scene = new Scene(root, 700, 600); // Taille du formulaire modal
        setScene(scene);

        // --- Actions des boutons ---
        addButton.setOnAction(e -> handleAddProduct());
        saveButton.setOnAction(e -> handleSaveProduct());
        deleteButton.setOnAction(e -> handleDeleteProduct());
        clearButton.setOnAction(e -> clearFields());
        closeButton.setOnAction(e -> close());

        refreshTable(); // Charger les données au démarrage
    }

    private void displayProductDetails(Produit produit) {
        idField.setText(produit.getId());
        nomField.setText(produit.getNom());
        descriptionArea.setText(produit.getDescription());
        prixUnitaireField.setText(String.valueOf(produit.getPrixUnitaire()));
        quantiteEnStockField.setText(String.valueOf(produit.getQuantiteEnStock()));
        datePeremptionPicker.setValue(produit.getDatePeremption());
    }

    private void clearFields() {
        idField.clear();
        nomField.clear();
        descriptionArea.clear();
        prixUnitaireField.clear();
        quantiteEnStockField.clear();
        datePeremptionPicker.setValue(LocalDate.now());
        productTable.getSelectionModel().clearSelection();
    }

    private void handleAddProduct() {
        if (nomField.getText().isEmpty() || prixUnitaireField.getText().isEmpty() || quantiteEnStockField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        try {
            String id = UUID.randomUUID().toString(); // Générer un ID unique
            String nom = nomField.getText();
            String description = descriptionArea.getText();
            double prixUnitaire = Double.parseDouble(prixUnitaireField.getText());
            int quantiteEnStock = Integer.parseInt(quantiteEnStockField.getText());
            LocalDate datePeremption = datePeremptionPicker.getValue();

            Produit newProduct = new Produit(id, nom, description, prixUnitaire, quantiteEnStock, datePeremption);
            stockService.addProduit(newProduct);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des nombres valides pour le prix et la quantité.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du produit: " + e.getMessage());
        }
    }

    private void handleSaveProduct() {
        if (idField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un produit à modifier dans le tableau.");
            return;
        }
        if (nomField.getText().isEmpty() || prixUnitaireField.getText().isEmpty() || quantiteEnStockField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs obligatoires.");
            return;
        }
        try {
            String id = idField.getText();
            String nom = nomField.getText();
            String description = descriptionArea.getText();
            double prixUnitaire = Double.parseDouble(prixUnitaireField.getText());
            int quantiteEnStock = Integer.parseInt(quantiteEnStockField.getText());
            LocalDate datePeremption = datePeremptionPicker.getValue();

            Produit updatedProduct = new Produit(id, nom, description, prixUnitaire, quantiteEnStock, datePeremption);
            stockService.updateProduit(updatedProduct);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", "Veuillez entrer des nombres valides pour le prix et la quantité.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du produit: " + e.getMessage());
        }
    }

    private void handleDeleteProduct() {
        Produit selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un produit à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce produit ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    stockService.deleteProduit(selectedProduct.getId());
                    refreshTable();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit supprimé avec succès.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du produit: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        productList.setAll(stockService.getAllProduits());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}