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
import lifepharmacy.dao.FournisseurDAO;
import lifepharmacy.models.Fournisseur;

import java.util.UUID;

public class FournisseurForm extends Stage {

    private FournisseurDAO fournisseurDAO;
    private ObservableList<Fournisseur> fournisseurList;
    private TableView<Fournisseur> fournisseurTable;

    // Champs du formulaire
    private TextField idField;
    private TextField nomField;
    private TextField contactPersonneField;
    private TextField telephoneField;
    private TextField adresseField;
    private TextField emailField;

    // Boutons
    private Button addButton;
    private Button saveButton; // Pour modifier
    private Button deleteButton;
    private Button clearButton;
    private Button closeButton;

    public FournisseurForm() {
        this.fournisseurDAO = new FournisseurDAO();
        this.fournisseurList = FXCollections.observableArrayList(fournisseurDAO.getAll());

        setTitle("Gestion des Fournisseurs");
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

        Label idLabel = new Label("ID Fournisseur:");
        idField = new TextField();
        idField.setDisable(true); // L'ID sera généré automatiquement ou géré par la sélection du tableau

        Label nomLabel = new Label("Nom:");
        nomField = new TextField();

        Label contactPersonneLabel = new Label("Contact:");
        contactPersonneField = new TextField();

        Label telephoneLabel = new Label("Téléphone:");
        telephoneField = new TextField();

        Label adresseLabel = new Label("Adresse:");
        adresseField = new TextField();

        Label emailLabel = new Label("Email:");
        emailField = new TextField();


        formGrid.add(idLabel, 0, 0);
        formGrid.add(idField, 1, 0);
        formGrid.add(nomLabel, 0, 1);
        formGrid.add(nomField, 1, 1);
        formGrid.add(contactPersonneLabel, 0, 2);
        formGrid.add(contactPersonneField, 1, 2);
        formGrid.add(telephoneLabel, 0, 3);
        formGrid.add(telephoneField, 1, 3);
        formGrid.add(adresseLabel, 0, 4);
        formGrid.add(adresseField, 1, 4);
        formGrid.add(emailLabel, 0, 5);
        formGrid.add(emailField, 1, 5);


        // --- Boutons d'action ---
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        addButton = new Button("Ajouter");
        saveButton = new Button("Enregistrer (Modifier)");
        deleteButton = new Button("Supprimer");
        clearButton = new Button("Effacer");
        closeButton = new Button("Fermer");

        buttonBox.getChildren().addAll(addButton, saveButton, deleteButton, clearButton, closeButton);

        // --- Tableau d'affichage des fournisseurs ---
        fournisseurTable = new TableView<>();
        TableColumn<Fournisseur, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Fournisseur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Fournisseur, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contactPersonne"));

        TableColumn<Fournisseur, String> telephoneCol = new TableColumn<>("Téléphone");
        telephoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        TableColumn<Fournisseur, String> adresseCol = new TableColumn<>("Adresse");
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        TableColumn<Fournisseur, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));


        fournisseurTable.getColumns().addAll(idCol, nomCol, contactCol, telephoneCol, adresseCol, emailCol);
        fournisseurTable.setItems(fournisseurList);

        // Listener pour la sélection du tableau
        fournisseurTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayFournisseurDetails(newSelection);
            } else {
                clearFields();
            }
        });

        root.getChildren().addAll(formGrid, buttonBox, new Separator(), fournisseurTable);

        Scene scene = new Scene(root, 750, 600); // Taille du formulaire modal
        setScene(scene);

        // --- Actions des boutons ---
        addButton.setOnAction(e -> handleAddFournisseur());
        saveButton.setOnAction(e -> handleSaveFournisseur());
        deleteButton.setOnAction(e -> handleDeleteFournisseur());
        clearButton.setOnAction(e -> clearFields());
        closeButton.setOnAction(e -> close());

        refreshTable(); // Charger les données au démarrage
    }

    private void displayFournisseurDetails(Fournisseur fournisseur) {
        idField.setText(fournisseur.getId());
        nomField.setText(fournisseur.getNom());
        contactPersonneField.setText(fournisseur.getContactPersonne());
        telephoneField.setText(fournisseur.getTelephone());
        adresseField.setText(fournisseur.getAdresse());
        emailField.setText(fournisseur.getEmail());
    }

    private void clearFields() {
        idField.clear();
        nomField.clear();
        contactPersonneField.clear();
        telephoneField.clear();
        adresseField.clear();
        emailField.clear();
        fournisseurTable.getSelectionModel().clearSelection();
    }

    private void handleAddFournisseur() {
        if (nomField.getText().isEmpty() || contactPersonneField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir le nom et la personne contact.");
            return;
        }
        try {
            String id = UUID.randomUUID().toString(); // Générer un ID unique
            String nom = nomField.getText();
            String contactPersonne = contactPersonneField.getText();
            String telephone = telephoneField.getText();
            String adresse = adresseField.getText();
            String email = emailField.getText();

            Fournisseur newFournisseur = new Fournisseur(id, nom, contactPersonne, telephone, adresse, email);
            fournisseurDAO.save(newFournisseur);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur ajouté avec succès.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du fournisseur: " + e.getMessage());
        }
    }

    private void handleSaveFournisseur() {
        if (idField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un fournisseur à modifier dans le tableau.");
            return;
        }
        if (nomField.getText().isEmpty() || contactPersonneField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir le nom et la personne contact.");
            return;
        }
        try {
            String id = idField.getText();
            String nom = nomField.getText();
            String contactPersonne = contactPersonneField.getText();
            String telephone = telephoneField.getText();
            String adresse = adresseField.getText();
            String email = emailField.getText();

            Fournisseur updatedFournisseur = new Fournisseur(id, nom, contactPersonne, telephone, adresse, email);
            fournisseurDAO.update(updatedFournisseur);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur mis à jour avec succès.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du fournisseur: " + e.getMessage());
        }
    }

    private void handleDeleteFournisseur() {
        Fournisseur selectedFournisseur = fournisseurTable.getSelectionModel().getSelectedItem();
        if (selectedFournisseur == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un fournisseur à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce fournisseur ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    fournisseurDAO.delete(selectedFournisseur.getId());
                    refreshTable();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Fournisseur supprimé avec succès.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du fournisseur: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        fournisseurList.setAll(fournisseurDAO.getAll());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}