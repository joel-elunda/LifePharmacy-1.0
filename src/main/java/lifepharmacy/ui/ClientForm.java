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
import lifepharmacy.dao.ClientDAO;
import lifepharmacy.models.Client;

import java.util.UUID;

public class ClientForm extends Stage {

    private ClientDAO clientDAO;
    private ObservableList<Client> clientList;
    private TableView<Client> clientTable;

    // Champs du formulaire
    private TextField idField;
    private TextField nomField;
    private TextField prenomField;
    private TextField telephoneField;
    private TextField adresseField;

    // Boutons
    private Button addButton;
    private Button saveButton; // Pour modifier
    private Button deleteButton;
    private Button clearButton;
    private Button closeButton;

    public ClientForm() {
        this.clientDAO = new ClientDAO();
        this.clientList = FXCollections.observableArrayList(clientDAO.getAll());

        setTitle("Gestion des Clients");
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

        Label idLabel = new Label("ID Client:");
        idField = new TextField();
        idField.setDisable(true);

        Label nomLabel = new Label("Nom:");
        nomField = new TextField();

        Label prenomLabel = new Label("Prénom:");
        prenomField = new TextField();

        Label telephoneLabel = new Label("Téléphone:");
        telephoneField = new TextField();

        Label adresseLabel = new Label("Adresse:");
        adresseField = new TextField();


        formGrid.add(idLabel, 0, 0);
        formGrid.add(idField, 1, 0);
        formGrid.add(nomLabel, 0, 1);
        formGrid.add(nomField, 1, 1);
        formGrid.add(prenomLabel, 0, 2);
        formGrid.add(prenomField, 1, 2);
        formGrid.add(telephoneLabel, 0, 3);
        formGrid.add(telephoneField, 1, 3);
        formGrid.add(adresseLabel, 0, 4);
        formGrid.add(adresseField, 1, 4);

        // --- Boutons d'action ---
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        addButton = new Button("Ajouter");
        saveButton = new Button("Enregistrer (Modifier)");
        deleteButton = new Button("Supprimer");
        clearButton = new Button("Effacer");
        closeButton = new Button("Fermer");

        buttonBox.getChildren().addAll(addButton, saveButton, deleteButton, clearButton, closeButton);

        // --- Tableau d'affichage des clients ---
        clientTable = new TableView<>();
        TableColumn<Client, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Client, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        TableColumn<Client, String> prenomCol = new TableColumn<>("Prénom");
        prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        TableColumn<Client, String> telephoneCol = new TableColumn<>("Téléphone");
        telephoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));

        TableColumn<Client, String> adresseCol = new TableColumn<>("Adresse");
        adresseCol.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        clientTable.getColumns().addAll(idCol, nomCol, prenomCol, telephoneCol, adresseCol);
        clientTable.setItems(clientList);

        // Listener pour la sélection du tableau
        clientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayClientDetails(newSelection);
            } else {
                clearFields();
            }
        });

        root.getChildren().addAll(formGrid, buttonBox, new Separator(), clientTable);

        Scene scene = new Scene(root, 700, 600);
        setScene(scene);

        // --- Actions des boutons ---
        addButton.setOnAction(e -> handleAddClient());
        saveButton.setOnAction(e -> handleSaveClient());
        deleteButton.setOnAction(e -> handleDeleteClient());
        clearButton.setOnAction(e -> clearFields());
        closeButton.setOnAction(e -> close());

        refreshTable();
    }

    private void displayClientDetails(Client client) {
        idField.setText(client.getId());
        nomField.setText(client.getNom());
        prenomField.setText(client.getPrenom());
        telephoneField.setText(client.getTelephone());
        adresseField.setText(client.getAdresse());
    }

    private void clearFields() {
        idField.clear();
        nomField.clear();
        prenomField.clear();
        telephoneField.clear();
        adresseField.clear();
        clientTable.getSelectionModel().clearSelection();
    }

    private void handleAddClient() {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir le nom et le prénom.");
            return;
        }
        try {
            String id = UUID.randomUUID().toString();
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String telephone = telephoneField.getText();
            String adresse = adresseField.getText();

            Client newClient = new Client(id, nom, prenom, telephone, adresse);
            clientDAO.save(newClient);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Client ajouté avec succès.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du client: " + e.getMessage());
        }
    }

    private void handleSaveClient() {
        if (idField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un client à modifier dans le tableau.");
            return;
        }
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir le nom et le prénom.");
            return;
        }
        try {
            String id = idField.getText();
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String telephone = telephoneField.getText();
            String adresse = adresseField.getText();

            Client updatedClient = new Client(id, nom, prenom, telephone, adresse);
            clientDAO.update(updatedClient);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Client mis à jour avec succès.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour du client: " + e.getMessage());
        }
    }

    private void handleDeleteClient() {
        Client selectedClient = clientTable.getSelectionModel().getSelectedItem();
        if (selectedClient == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un client à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer ce client ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    clientDAO.delete(selectedClient.getId());
                    refreshTable();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Client supprimé avec succès.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du client: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        clientList.setAll(clientDAO.getAll());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}