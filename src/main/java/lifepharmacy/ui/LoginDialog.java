package lifepharmacy.ui;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lifepharmacy.dao.UtilisateurDAO;
import lifepharmacy.models.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class LoginDialog extends Stage {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button cancelButton;

    private boolean authenticated = false;
    private Utilisateur authenticatedUser; // Pour stocker l'utilisateur authentifié
    private UtilisateurDAO utilisateurDAO;

    public LoginDialog() {
        this.utilisateurDAO = new UtilisateurDAO();

        setTitle("Authentification");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        // Labels and Fields
        Label usernameLabel = new Label("Nom d'utilisateur:");
        usernameField = new TextField();
        usernameField.setPromptText("Entrez votre nom d'utilisateur");

        Label passwordLabel = new Label("Mot de passe:");
        passwordField = new PasswordField();
        passwordField.setPromptText("Entrez votre mot de passe");

        // Buttons
        loginButton = new Button("Se connecter");
        loginButton.setDefaultButton(true);
        loginButton.setOnAction(e -> handleLogin());

        cancelButton = new Button("Annuler");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> {
            authenticated = false;
            authenticatedUser = null; // S'assurer que l'utilisateur n'est pas défini si l'authentification échoue
            close();
        });

        // Add to GridPane
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 0, 2);
        grid.add(cancelButton, 1, 2);

        Scene scene = new Scene(grid);
        setScene(scene);

        // Cette initialisation d'utilisateur par défaut n'est plus strictement nécessaire ici,
        // car UtilisateurDAO s'en occupe déjà si le fichier n'existe pas.
        // C'est juste pour s'assurer qu'un admin existe pour les premiers tests.
        // if (utilisateurDAO.getAll().isEmpty()) {
        //     String hashedPassword = BCrypt.hashpw("admin", BCrypt.gensalt());
        //     utilisateurDAO.save(new Utilisateur("USR001", "admin", hashedPassword, "ADMIN"));
        //     System.out.println("Utilisateur par défaut 'admin' créé avec mot de passe haché.");
        // }
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Optional<Utilisateur> userOpt = utilisateurDAO.getByUsername(username);

        if (userOpt.isPresent()) {
            Utilisateur user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                authenticated = true;
                authenticatedUser = user; // Stocker l'utilisateur authentifié
                System.out.println("Login successful for user: " + username);
                close();
            } else {
                showAlert("Erreur d'authentification", "Mot de passe incorrect.");
            }
        } else {
            showAlert("Erreur d'authentification", "Nom d'utilisateur introuvable.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    // Nouvelle méthode pour récupérer l'utilisateur authentifié
    public Utilisateur getAuthenticatedUser() {
        return authenticatedUser;
    }
}