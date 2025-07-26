package lifepharmacy.ui;

import com.itextpdf.text.DocumentException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter; // NOUVEL IMPORT
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lifepharmacy.dao.ClientDAO;
import lifepharmacy.models.Client;
import lifepharmacy.models.Facture;
import lifepharmacy.models.Produit;
import lifepharmacy.services.FacturationService;
import lifepharmacy.services.PdfGeneratorService;
import lifepharmacy.services.StockService;


import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class FacturationForm extends VBox {

    private StockService stockService;
    private FacturationService facturationService;
    private ClientDAO clientDAO; // NOUVEAU DAO

    // Champs de la facture
    private Label numeroFactureLabel;
    private ComboBox<Client> clientComboBox;
    private DatePicker dateFacturePicker;

    // Ajout de produits
    private ComboBox<Produit> produitComboBox;
    private TextField quantiteField;
    private Button ajouterProduitButton;
    private Button supprimerLigneButton; // NOUVEAU BOUTON

    // Tableau des lignes de facture

    private TableView<Facture.LigneFacture> lignesFactureTable;
    private ObservableList<Facture.LigneFacture> lignesFactureList;

    // Totaux
    private Label totalLabel;
    private double currentTotal = 0.0;

    // Boutons d'action
    private CheckBox proformaCheckBox;
    private Button enregistrerFactureButton;
    private Button imprimerFactureButton;
    private Button annulerFactureButton;

    private PdfGeneratorService pdfGeneratorService;
    private TableView<Facture.LigneFacture> lignesFactureTableView;

    private CheckBox autoPrintCheckBox; // <-- Nouveau champ pour le toggle d'impression automatique


    public FacturationForm(StockService stockService, FacturationService facturationService) {
        this.stockService = stockService;
        this.facturationService = facturationService;
        this.clientDAO = new ClientDAO(); // Initialisation du ClientDAO
        this.lignesFactureList = FXCollections.observableArrayList();

        setPadding(new Insets(20));
        setSpacing(20);
        setAlignment(Pos.TOP_CENTER);

        // --- Détails de la facture ---
        GridPane factureDetailsGrid = new GridPane();
        factureDetailsGrid.setHgap(10);
        factureDetailsGrid.setVgap(10);

        numeroFactureLabel = new Label("N° Facture: "); // Sera mis à jour dynamiquement
        clientComboBox = new ComboBox<>();
        // Charger les clients depuis ClientDAO et définir un StringConverter
        clientComboBox.setItems(FXCollections.observableArrayList(clientDAO.getAll()));
        clientComboBox.setPromptText("Sélectionner un client");
        clientComboBox.setConverter(new StringConverter<Client>() {

            public String toString(Client client) {
                return (client != null) ? client.getNom() + " " + client.getPrenom() : "";
            }


            public Client fromString(String string) {
                // Non utilisé pour une ComboBox en mode sélection
                return null;
            }
        });

        dateFacturePicker = new DatePicker(java.time.LocalDate.now());
        dateFacturePicker.setDisable(true); // La date est celle du jour et n'est pas modifiable

        factureDetailsGrid.add(new Label("Numéro Facture:"), 0, 0);
        factureDetailsGrid.add(numeroFactureLabel, 1, 0);
        factureDetailsGrid.add(new Label("Client:"), 0, 1);
        factureDetailsGrid.add(clientComboBox, 1, 1);
        factureDetailsGrid.add(new Label("Date:"), 0, 2);
        factureDetailsGrid.add(dateFacturePicker, 1, 2);

        // --- Ajout de produits à la facture ---
        HBox addProductBox = new HBox(10);
        addProductBox.setAlignment(Pos.CENTER_LEFT);
        produitComboBox = new ComboBox<>();
        produitComboBox.setPromptText("Sélectionner un produit");
        produitComboBox.setItems(FXCollections.observableArrayList(stockService.getAllProduits())); // Charger les produits
        // Ajouter un converter pour afficher le nom du produit
        produitComboBox.setConverter(new StringConverter<Produit>() {

            public String toString(Produit produit) {
                return (produit != null) ? produit.getNom() + " (Stock: " + produit.getQuantiteEnStock() + ")" : "";
            }

            public Produit fromString(String string) {
                return null;
            }
        });

        quantiteField = new TextField();
        quantiteField.setPromptText("Quantité");
        quantiteField.setPrefWidth(80);
        quantiteField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));


        ajouterProduitButton = new Button("Ajouter Produit");
        supprimerLigneButton = new Button("Supprimer Ligne"); // Nouveau bouton
        addProductBox.getChildren().addAll(produitComboBox, quantiteField, ajouterProduitButton, supprimerLigneButton);

        // --- Tableau des lignes de facture ---
        lignesFactureTable = new TableView<>();
        lignesFactureTable.setItems(lignesFactureList);
        lignesFactureTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Facture.LigneFacture, String> produitNomCol = new TableColumn<>("Produit");
        produitNomCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProduit().getNom()));

        TableColumn<Facture.LigneFacture, Integer> quantiteCol = new TableColumn<>("Quantité");
        quantiteCol.setCellValueFactory(new PropertyValueFactory<>("quantite"));

        TableColumn<Facture.LigneFacture, Double> prixUnitaireCol = new TableColumn<>("Prix Unitaire");
        prixUnitaireCol.setCellValueFactory(new PropertyValueFactory<>("prixUnitaireVendu"));

        TableColumn<Facture.LigneFacture, Double> totalLigneCol = new TableColumn<>("Total Ligne");
        totalLigneCol.setCellValueFactory(new PropertyValueFactory<>("totalLigne"));

        lignesFactureTable.getColumns().addAll(produitNomCol, quantiteCol, prixUnitaireCol, totalLigneCol);
        lignesFactureTable.setPlaceholder(new Label("Aucun produit ajouté à la facture."));


        // --- Total et boutons d'action ---
        totalLabel = new Label("Total: 0.00 CDF");
        totalLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox actionButtonsBox = new HBox(10);
        actionButtonsBox.setAlignment(Pos.CENTER_RIGHT);
        proformaCheckBox = new CheckBox("Facture Proforma");
        enregistrerFactureButton = new Button("Enregistrer Facture");
        imprimerFactureButton = new Button("Imprimer Facture");
        annulerFactureButton = new Button("Annuler");
        actionButtonsBox.getChildren().addAll(proformaCheckBox, enregistrerFactureButton, imprimerFactureButton, annulerFactureButton);


        // --- Ajout des éléments à la VBox principale ---
        getChildren().addAll(
                factureDetailsGrid,
                new Separator(),
                new Label("Ajouter un Produit:"),
                addProductBox,
                lignesFactureTable,
                new Separator(),
                totalLabel,
                actionButtonsBox
        );

        this.pdfGeneratorService = new PdfGeneratorService(new StockService()); // Assurez-vous que StockService est accessible ou instancié correctement

        // --- Initialisation du CheckBox ---
        autoPrintCheckBox = new CheckBox("Imprimer facture automatique");
        autoPrintCheckBox.setSelected(true); // Par défaut, l'impression automatique est activée
        autoPrintCheckBox.getStyleClass().add("check-box"); // Appliquez un style si vous en avez un

        // --- Configuration du bouton "Imprimer Facture" (ou votre bouton de création/sauvegarde) ---
        // Si vous avez un bouton "Créer Facture" ou "Sauvegarder Facture" qui déclenche la logique
        // de handleImprimerFacture() après avoir créé la facture, c'est là qu'il faut l'intégrer.
        Button createAndPrintButton = new Button("Créer & Imprimer Facture");
        createAndPrintButton.getStyleClass().add("button");
        createAndPrintButton.setOnAction(e -> handleImprimerFacture()); // Liaison à la méthode

        // --- Actions des boutons et événements ---
        ajouterProduitButton.setOnAction(e -> handleAddProductToFacture());
        supprimerLigneButton.setOnAction(e -> handleRemoveProductFromFacture()); // Action pour le nouveau bouton
        enregistrerFactureButton.setOnAction(e -> handleEnregistrerFacture());
        imprimerFactureButton.setOnAction(e -> handleImprimerFacture());
        annulerFactureButton.setOnAction(e -> handleAnnulerFacture());

        // Listener sur la liste des lignes de facture pour mettre à jour le total
        lignesFactureList.addListener((javafx.collections.ListChangeListener.Change<? extends Facture.LigneFacture> change) -> {
            updateTotal();
        });

        // Générer le premier numéro de facture
        numeroFactureLabel.setText("N° Facture: " + facturationService.genererNouveauNumeroFacture());
    }



    private void handleImprimerFacture() {
        // --- 1. RÉCUPÉRATION DES DONNÉES DE LA FACTURE DEPUIS LE FORMULAIRE ---
        // Cette section doit toujours récupérer les vraies données de votre formulaire.
        // Exemple (à adapter à votre implémentation réelle) :
        Client selectedClient = /* Récupérez le client depuis votre ComboBox/champ */ null;
        if (clientComboBox != null) { // Supposons que clientComboBox est le champ de sélection de client
            selectedClient = clientComboBox.getSelectionModel().getSelectedItem();
        }

        ObservableList<Facture.LigneFacture> currentLignesFacture = null;
        if (lignesFactureTableView != null) { // Supposons que lignesFactureTableView est votre TableView pour les lignes de facture
            currentLignesFacture = lignesFactureTableView.getItems();
        }

        String numeroFacture = "FAC-" + System.currentTimeMillis(); // Utilisez votre logique de numéro de facture
        // Si vous n'avez pas encore récupéré le client ou les lignes de facture, affichez une alerte
        if (selectedClient == null || currentLignesFacture == null || currentLignesFacture.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Données Manquantes", "Veuillez sélectionner un client et ajouter des produits à la facture.");
            return;
        }

        // --- CONSTRUIRE L'OBJET FACTURE ---
        // Créez l'objet Facture à partir des données collectées du formulaire

        Facture factureAImprimer = new Facture(
                        numeroFacture,
                        LocalDate.now(),
                        selectedClient,
                        new ArrayList<>(currentLignesFacture),
                currentLignesFacture.stream().mapToDouble(lf -> lf.getQuantite() * lf.getPrixUnitaireVendu()).sum(), // À adapter si vous gérez des proformas ici
                false
                );

        // --- 2. LOGIQUE DE GÉNÉRATION ET D'OUVERTURE DU PDF ---
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder la facture PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Facture_" + factureAImprimer.getNumeroFacture() + ".pdf");

        File file = fileChooser.showSaveDialog(this.getScene().getWindow());

        if (file != null) {
            String filePath = file.getAbsolutePath();
            try {
                pdfGeneratorService.generateFacturePdf(factureAImprimer, filePath);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Facture PDF générée avec succès :\n" + filePath);

                // --- NOUVEAU : Vérifier l'état du toggle avant d'ouvrir automatiquement ---
                if (autoPrintCheckBox.isSelected()) { // Vérifie si le CheckBox est coché
                    if (Desktop.isDesktopSupported()) {
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException ex) {
                            showAlert(Alert.AlertType.WARNING, "Erreur d'ouverture", "Impossible d'ouvrir le fichier PDF. Veuillez l'ouvrir manuellement.");
                            ex.printStackTrace();
                        }
                    } else {
                        showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité non supportée", "L'ouverture automatique de fichiers n'est pas supportée sur ce système.");
                    }
                }
                // --- FIN NOUVEAU ---

            } catch (DocumentException | IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'impression", "Impossible de générer le PDF :\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Gère l'événement de clic sur le bouton "Imprimer Facture" de la fenêtre "Nouvelle Facture".
     * Cette méthode génère un PDF de la facture actuelle et l'ouvre.
     *
     * **ATTENTION :** Vous devez adapter la section `// --- RÉCUPÉRATION DE LA FACTURE À IMPRIMER ---`
     * pour qu'elle récupère la vraie facture que l'utilisateur est en train de créer ou de visualiser.
     */
    private void handleImprimerNouvelleFacture() {
        //TODO: A vérifier
        Facture facture = null;
        if(facture == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une ligne de facture à imprimer.");
            return;
        }
        // --- 2. LOGIQUE DE GÉNÉRATION ET D'OUVERTURE DU PDF ---
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder la facture PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Facture_" + facture.getNumeroFacture() + ".pdf");

        // Utilise la fenêtre du formulaire comme parent pour le dialogue de sauvegarde
        File file = fileChooser.showSaveDialog(this.getScene().getWindow());

        if (file != null) {
            String filePath = file.getAbsolutePath();
            try {
                // Génère le PDF en utilisant le service créé
                pdfGeneratorService.generateFacturePdf(facture, filePath);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Facture PDF générée avec succès :\n" + filePath);

                // Tente d'ouvrir le fichier PDF avec l'application par défaut
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException ex) {
                        showAlert(Alert.AlertType.WARNING, "Erreur d'ouverture", "Impossible d'ouvrir le fichier PDF. Veuillez l'ouvrir manuellement.");
                        ex.printStackTrace();
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité non supportée", "L'ouverture automatique de fichiers n'est pas supportée sur ce système.");
                }

            } catch (DocumentException | IOException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'impression", "Impossible de générer le PDF :\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }


    private void handleAddProductToFacture() {
        Produit selectedProduct = produitComboBox.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un produit.");
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(quantiteField.getText());
            if (quantite <= 0) {
                showAlert(Alert.AlertType.WARNING, "Attention", "La quantité doit être supérieure à zéro.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer une quantité valide.");
            return;
        }

        if (selectedProduct.getQuantiteEnStock() < quantite) {
            showAlert(Alert.AlertType.WARNING, "Stock Insuffisant", "Il n'y a pas assez de " + selectedProduct.getNom() + " en stock. Quantité disponible: " + selectedProduct.getQuantiteEnStock());
            return;
        }
        if (selectedProduct.getDatePeremption() != null && selectedProduct.getDatePeremption().isBefore(java.time.LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Produit Périmé", "Le produit sélectionné est périmé: " + selectedProduct.getNom());
            return;
        }

        // Ajouter la ligne de facture (vérifier si le produit est déjà dans la liste pour fusionner)
        Optional<Facture.LigneFacture> existingLigne = lignesFactureList.stream()
                .filter(ligne -> ligne.getProduit().getId().equals(selectedProduct.getId()))
                .findFirst();

        if (existingLigne.isPresent()) {
            Facture.LigneFacture ligne = existingLigne.get();
            // Vérifier si la nouvelle quantité ne dépasse pas le stock
            if (selectedProduct.getQuantiteEnStock() < (ligne.getQuantite() + quantite)) {
                showAlert(Alert.AlertType.WARNING, "Stock Insuffisant", "L'ajout de cette quantité dépasse le stock disponible pour " + selectedProduct.getNom() + ". Total en stock: " + selectedProduct.getQuantiteEnStock());
                return;
            }
            ligne.setQuantite(ligne.getQuantite() + quantite);
            lignesFactureTable.refresh(); // Rafraîchir le tableau pour afficher la nouvelle quantité
        } else {
            Facture.LigneFacture nouvelleLigne = new Facture.LigneFacture(selectedProduct, quantite);
            lignesFactureList.add(nouvelleLigne);
        }

        quantiteField.clear();
        produitComboBox.getSelectionModel().clearSelection();
        updateTotal(); // S'assurer que le total est mis à jour
    }

    // NOUVELLE MÉTHODE
    private void handleRemoveProductFromFacture() {
        Facture.LigneFacture selectedLigne = lignesFactureTable.getSelectionModel().getSelectedItem();
        if (selectedLigne == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une ligne à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cette ligne de facture ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                lignesFactureList.remove(selectedLigne);
                updateTotal();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Ligne supprimée.");
            }
        });
    }

    private void updateTotal() {
        currentTotal = lignesFactureList.stream()
                .mapToDouble(Facture.LigneFacture::getTotalLigne)
                .sum();
        totalLabel.setText(String.format("Total: %.2f CDF", currentTotal));
    }

    private void handleEnregistrerFacture() {
        if (clientComboBox.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un client.");
            return;
        }
        if (lignesFactureList.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez ajouter des produits à la facture.");
            return;
        }

        try {
            Client selectedClient = clientComboBox.getSelectionModel().getSelectedItem();
            boolean isProforma = proformaCheckBox.isSelected();

            Facture nouvelleFacture = facturationService.creerFacture(selectedClient, lignesFactureList, isProforma);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Facture " + nouvelleFacture.getNumeroFacture() + " enregistrée avec succès !");

            // Réinitialiser le formulaire
            clearFactureForm();
            // Rafraîchir la liste des produits dans le service de stock pour refléter les changements de quantité
            produitComboBox.setItems(FXCollections.observableArrayList(stockService.getAllProduits()));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'enregistrement", e.getMessage());
            e.printStackTrace();
        }
    }



    private void handleAnnulerFacture() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir annuler la facture en cours ?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Annuler");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        result.filter(response -> response == ButtonType.YES).ifPresent(response -> clearFactureForm());
    }


    private void clearFactureForm() {
        numeroFactureLabel.setText("N° Facture: " + facturationService.genererNouveauNumeroFacture());
        clientComboBox.getSelectionModel().clearSelection();
        dateFacturePicker.setValue(java.time.LocalDate.now());
        produitComboBox.getSelectionModel().clearSelection();
        quantiteField.clear();
        lignesFactureList.clear(); // Vide le tableau
        proformaCheckBox.setSelected(false);
        updateTotal(); // Réinitialise le total à 0
    }

    public VBox getView() {
        return this;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}