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
import lifepharmacy.dao.UtilisateurDAO;
import lifepharmacy.models.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.UUID;

public class GestionUtilisateursForm extends Stage {

    private UtilisateurDAO utilisateurDAO;
    private ObservableList<Utilisateur> utilisateurList;
    private TableView<Utilisateur> utilisateurTable;

    // Champs du formulaire
    private TextField idField;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<String> roleComboBox;

    // Boutons
    private Button addButton;
    private Button saveButton; // Pour modifier
    private Button deleteButton;
    private Button clearButton;
    private Button closeButton;

    public GestionUtilisateursForm() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.utilisateurList = FXCollections.observableArrayList(utilisateurDAO.getAll());

        setTitle("Gestion des Utilisateurs");
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

        Label idLabel = new Label("ID Utilisateur:");
        idField = new TextField();
        idField.setDisable(true); // L'ID sera généré automatiquement ou géré par la sélection du tableau

        Label usernameLabel = new Label("Nom d'utilisateur:");
        usernameField = new TextField();

        Label passwordLabel = new Label("Mot de passe:");
        passwordField = new PasswordField();

        Label roleLabel = new Label("Rôle:");
        roleComboBox = new ComboBox<>();
        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "VENTE", "STOCK")); // Définir les rôles disponibles
        roleComboBox.getSelectionModel().selectFirst();


        formGrid.add(idLabel, 0, 0);
        formGrid.add(idField, 1, 0);
        formGrid.add(usernameLabel, 0, 1);
        formGrid.add(usernameField, 1, 1);
        formGrid.add(passwordLabel, 0, 2);
        formGrid.add(passwordField, 1, 2);
        formGrid.add(roleLabel, 0, 3);
        formGrid.add(roleComboBox, 1, 3);


        // --- Boutons d'action ---
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        addButton = new Button("Ajouter");
        saveButton = new Button("Enregistrer (Modifier)");
        deleteButton = new Button("Supprimer");
        clearButton = new Button("Effacer");
        closeButton = new Button("Fermer");

        buttonBox.getChildren().addAll(addButton, saveButton, deleteButton, clearButton, closeButton);

        // --- Tableau d'affichage des utilisateurs ---
        utilisateurTable = new TableView<>();
        TableColumn<Utilisateur, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Utilisateur, String> usernameCol = new TableColumn<>("Nom d'utilisateur");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Utilisateur, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        utilisateurTable.getColumns().addAll(idCol, usernameCol, roleCol);
        utilisateurTable.setItems(utilisateurList);

        // Listener pour la sélection du tableau
        utilisateurTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayUserDetails(newSelection);
            } else {
                clearFields();
            }
        });

        root.getChildren().addAll(formGrid, buttonBox, new Separator(), utilisateurTable);

        Scene scene = new Scene(root, 700, 500);
        setScene(scene);

        // --- Actions des boutons ---
        addButton.setOnAction(e -> handleAddUser());
        saveButton.setOnAction(e -> handleSaveUser());
        deleteButton.setOnAction(e -> handleDeleteUser());
        clearButton.setOnAction(e -> clearFields());
        closeButton.setOnAction(e -> close());

        refreshTable(); // Charger les données au démarrage
    }

    private void displayUserDetails(Utilisateur utilisateur) {
        idField.setText(utilisateur.getId());
        usernameField.setText(utilisateur.getUsername());
        passwordField.setText(""); // Ne pas afficher le mot de passe haché
        roleComboBox.getSelectionModel().select(utilisateur.getRole());
    }

    private void clearFields() {
        idField.clear();
        usernameField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().selectFirst();
        utilisateurTable.getSelectionModel().clearSelection();
    }

    private void handleAddUser() {
        if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty() || roleComboBox.getSelectionModel().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs.");
            return;
        }
        if (utilisateurDAO.getByUsername(usernameField.getText()).isPresent()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Un utilisateur avec ce nom existe déjà.");
            return;
        }
        try {
            String id = UUID.randomUUID().toString();
            String username = usernameField.getText();
            String passwordHash = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()); // Hacher le mot de passe
            String role = roleComboBox.getSelectionModel().getSelectedItem();

            Utilisateur newUser = new Utilisateur(id, username, passwordHash, role);
            utilisateurDAO.save(newUser);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur ajouté avec succès.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
        }
    }

    private void handleSaveUser() {
        if (idField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un utilisateur à modifier dans le tableau.");
            return;
        }
        if (usernameField.getText().isEmpty() || roleComboBox.getSelectionModel().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir le nom d'utilisateur et le rôle.");
            return;
        }
        try {
            String id = idField.getText();
            String username = usernameField.getText();
            String role = roleComboBox.getSelectionModel().getSelectedItem();

            Optional<Utilisateur> existingUserOpt = utilisateurDAO.getById(id);
            if (existingUserOpt.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur introuvable pour la mise à jour.");
                return;
            }
            Utilisateur existingUser = existingUserOpt.get();

            // Ne hacher le mot de passe que s'il a été modifié
            String passwordHash = existingUser.getPasswordHash();
            if (!passwordField.getText().isEmpty()) {
                passwordHash = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
            }

            Utilisateur updatedUser = new Utilisateur(id, username, passwordHash, role);
            utilisateurDAO.update(updatedUser);
            refreshTable();
            clearFields();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur mis à jour avec succès.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }

    private void handleDeleteUser() {
        Utilisateur selectedUser = utilisateurTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection", "Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cet utilisateur ?", ButtonType.YES, ButtonType.NO);
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    utilisateurDAO.delete(selectedUser.getId());
                    refreshTable();
                    clearFields();
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
                }
            }
        });
    }

    private void refreshTable() {
        utilisateurList.setAll(utilisateurDAO.getAll());
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}