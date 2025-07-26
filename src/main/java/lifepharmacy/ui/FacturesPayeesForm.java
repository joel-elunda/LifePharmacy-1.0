package lifepharmacy.ui;

import com.itextpdf.text.DocumentException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lifepharmacy.models.Facture;
import lifepharmacy.services.FacturationService;
import lifepharmacy.services.PdfGeneratorService;
import lifepharmacy.services.StockService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class FacturesPayeesForm extends Stage {

    private FacturationService facturationService;
    private PdfGeneratorService pdfGeneratorService; // Nouvelle instance du service PDF
    private TableView<Facture> facturesTable;
    private ObservableList<Facture> facturesList;

    public FacturesPayeesForm(FacturationService facturationService) {
        this.facturationService = facturationService;
        // Instancier PdfGeneratorService avec un StockService (nécessaire pour récupérer les produits)
        this.pdfGeneratorService = new PdfGeneratorService(new StockService()); // Assurez-vous d'avoir une instance de StockService

        setTitle("Factures Payées");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true); // Peut être redimensionnable pour voir plus de factures

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("form-vbox"); // Appliquer la classe CSS

        // --- TableView des factures ---
        facturesTable = new TableView<>();
        facturesTable.getStyleClass().add("table-view");

        // Colonnes de la table
        TableColumn<Facture, String> numeroCol = new TableColumn<>("N° Facture");
        numeroCol.setCellValueFactory(new PropertyValueFactory<>("numeroFacture"));

        TableColumn<Facture, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateFacturation"));
        dateCol.setCellFactory(column -> new TableCell<Facture, String>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Si l'objet retourné par PropertyValueFactory est déjà une String, utilisez-le directement
                    // Sinon, si c'est un LocalDate, formatez-le
                    if (getTableRow().getItem() != null && getTableRow().getItem().getDateFacture() != null) {
                        setText(getTableRow().getItem().getDateFacture().format(formatter));
                    } else {
                        setText(item); // Fallback si ce n'est pas un LocalDate
                    }
                }
            }
        });


        // --- NOUVELLE DÉCLARATION ET CONFIGURATION ---
        TableColumn<Facture, String> clientCol = new TableColumn<>("Client");
        // Utilisez une expression lambda pour extraire le nom du client
        clientCol.setCellValueFactory(cellData -> {
            Facture facture = cellData.getValue();
            if (facture != null && facture.getClient() != null) {
                return new SimpleStringProperty(facture.getClient().getNom());
            }
            return new SimpleStringProperty(""); // Retourne une chaîne vide si pas de client
        });

        // Le CellFactory n'a plus besoin d'être aussi complexe, le PropertyValueFactory gère déjà l'extraction du nom.
        // Si vous voulez un CellFactory personnalisé, assurez-vous qu'il travaille avec le type String.
        // Si vous voulez juste afficher la chaîne, vous pouvez même supprimer le setCellFactory pour cette colonne.
        // Ou si vous le gardez, il doit être de type <Facture, String>:
        clientCol.setCellFactory(column -> new TableCell<Facture, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item); // 'item' est déjà le nom du client (String)
                }
            }
        });
        // --- FIN NOUVELLE DÉCLARATION ET CONFIGURATION ---


        TableColumn<Facture, Double> totalCol = new TableColumn<>("Montant Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("montantTotal"));

        facturesTable.getColumns().addAll(numeroCol, dateCol, clientCol, totalCol);
        facturesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        facturesList = FXCollections.observableArrayList(facturationService.getFacturesPayees());
        facturesTable.setItems(facturesList);

        // --- Boutons ---
        Button closeButton = new Button("Fermer");
        closeButton.getStyleClass().add("button");
        closeButton.getStyleClass().add("cancel-button");
        closeButton.setOnAction(e -> close());

        Button printButton = new Button("Imprimer la Facture"); // Nouveau bouton
        printButton.getStyleClass().add("button");
        printButton.setOnAction(e -> handlePrintFacture());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(printButton, closeButton); // Ajout du bouton Imprimer

        root.getChildren().addAll(facturesTable, buttonBox);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        setScene(scene);
    }

    private void handlePrintFacture() {
        Facture selectedFacture = facturesTable.getSelectionModel().getSelectedItem();
        if (selectedFacture == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner une facture à imprimer.");
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sauvegarder la facture PDF");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        fileChooser.setInitialFileName("Facture_" + selectedFacture.getNumeroFacture() + ".pdf");

        File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            String filePath = file.getAbsolutePath();
            try {
                pdfGeneratorService.generateFacturePdf(selectedFacture, filePath);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Facture PDF générée avec succès :\n" + filePath);

                // --- NOUVEAU CODE POUR OUVRIR LE PDF ---
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(file); // Ouvre le fichier avec l'application par défaut
                    } catch (IOException e) {
                        showAlert(Alert.AlertType.WARNING, "Erreur d'ouverture", "Impossible d'ouvrir le fichier PDF. Veuillez l'ouvrir manuellement.");
                        e.printStackTrace();
                    }
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité non supportée", "L'ouverture automatique de fichiers n'est pas supportée sur ce système.");
                }
                // --- FIN NOUVEAU CODE ---

            } catch (DocumentException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur d'impression", "Impossible de générer le PDF :\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}