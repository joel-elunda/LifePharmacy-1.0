package lifepharmacy.lifepharmacy;

import javafx.application.Application;
import javafx.stage.Stage;
import lifepharmacy.models.Utilisateur;
import lifepharmacy.ui.FenetrePrincipale;
import lifepharmacy.ui.LoginDialog;

import java.io.File;

public class MainApp extends Application {
    private static final String DATA_DIRECTORY = "data";
    private static Utilisateur utilisateurConnecte; // Pour stocker l'utilisateur connecté

    public static void main(String[] args) {
        // Crée le dossier 'data' si n'existe pas
        createDataDirectory();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Au démarrage, afficher la boîte de dialogue d'authentification
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.showAndWait(); // Attend que la boîte de dialogue soit fermée

        // Si l'authentification est réussie, démarrer l'application principale
        if (loginDialog.isAuthenticated()) {
            utilisateurConnecte = loginDialog.getAuthenticatedUser(); // Récupère l'utilisateur
            System.out.println("Authentification réussie ! Utilisateur: " + utilisateurConnecte.getUsername() + ", Rôle: " + utilisateurConnecte.getRole());
            FenetrePrincipale fenetrePrincipale = new FenetrePrincipale(primaryStage, utilisateurConnecte); // Passe l'utilisateur à la fenêtre principale
            fenetrePrincipale.show();
        } else {
            System.out.println("Authentification échouée ou annulée. Fermeture de l'application.");
            primaryStage.close(); // Ferme la stage principale si l'authentification échoue
        }
    }

    private static void createDataDirectory() {
        File dataDir = new File(DATA_DIRECTORY);
        if (!dataDir.exists()) {
            if (dataDir.mkdirs()) {
                System.out.println("Dossier '" + DATA_DIRECTORY + "' créé avec succès.");
            } else {
                System.err.println("Échec de la création du dossier '" + DATA_DIRECTORY + "'.");
                System.exit(1);
            }
        }
    }

    // Getter pour l'utilisateur connecté (peut être utile pour d'autres parties de l'app si nécessaire)
    public static Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }
}