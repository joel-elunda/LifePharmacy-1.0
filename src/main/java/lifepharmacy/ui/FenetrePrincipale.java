package lifepharmacy.ui;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lifepharmacy.models.Utilisateur;
import lifepharmacy.services.FacturationService;
import lifepharmacy.services.StockService;

public class FenetrePrincipale {

    private Stage primaryStage;
    private BorderPane root;
    private StockService stockService;
    private FacturationService facturationService;
    private Utilisateur utilisateurConnecte; // Pour stocker l'utilisateur connecté

    // Boutons de la barre de menu
    private Button btnGestionProduits;
    private Button btnGestionClients;
    private Button btnGestionFournisseurs;
    private Button btnReapprovisionnement;
    private Button btnGestionUtilisateurs;
    private Button btnConsulterBenefices;
    private Button btnConsulterFacturesPayees;
    private Button btnConsulterFacturesProforma;
    private Button btnNouvelleFacture;
    private Button btnVerrouiller;

    public FenetrePrincipale(Stage primaryStage, Utilisateur utilisateurConnecte) { // Accepte l'utilisateur connecté
        this.primaryStage = primaryStage;
        this.utilisateurConnecte = utilisateurConnecte; // Initialise l'utilisateur
        this.stockService = new StockService();
        this.facturationService = new FacturationService();
        initializeUI();
        toggleControls(true); // Tout est désactivé au démarrage
        btnNouvelleFacture.setDisable(false); // S'assurer que celui-ci est actif pour déverrouiller
        applyRolePermissions(); // Appliquer les permissions au démarrage
    }

    private void initializeUI() {
        primaryStage.setTitle("Système de Facturation Pharmaceutique - Connecté en tant que " + utilisateurConnecte.getUsername() + " (" + utilisateurConnecte.getRole() + ")"); // Affiche l'utilisateur et son rôle
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();

        root = new BorderPane();
        Scene scene = new Scene(root, 1200, 800);
        // --- CHARGEMENT DU CSS POUR LA SCÈNE PRINCIPALE ---
        //scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
        primaryStage.setScene(scene);




        // --- Barre de Menu à Gauche ---
        VBox menuBar = new VBox(10);
        menuBar.setPadding(new Insets(15));
        menuBar.setAlignment(Pos.TOP_LEFT);
        menuBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 0 1 0 0;");
        // Appliquer une classe CSS pour la barre de menu
        //menuBar.getStyleClass().add("menu-bar");

        // Initialisation des boutons de menu
        btnNouvelleFacture = createMenuButton("Nouvelle Facture");
        btnGestionProduits = createMenuButton("Gestion Produits");
        btnGestionClients = createMenuButton("Gestion Clients");
        btnGestionFournisseurs = createMenuButton("Gestion Fournisseurs");
        btnReapprovisionnement = createMenuButton("Réapprovisionnement");
        btnGestionUtilisateurs = createMenuButton("Gestion Utilisateurs");
        btnConsulterBenefices = createMenuButton("Consulter Bénéfices");
        btnConsulterFacturesPayees = createMenuButton("Factures Payées");
        btnConsulterFacturesProforma = createMenuButton("Factures Proforma");
        btnVerrouiller = createMenuButton("Verrouiller");

        menuBar.getChildren().addAll(
                btnNouvelleFacture,
                new Separator(),
                btnGestionProduits,
                btnGestionClients,
                btnGestionFournisseurs,
                btnReapprovisionnement,
                new Separator(),
                btnGestionUtilisateurs,
                btnConsulterBenefices,
                btnConsulterFacturesPayees,
                btnConsulterFacturesProforma,
                new Separator(),
                btnVerrouiller
        );
        root.setLeft(menuBar);



        // --- Contenu Central (Zone de travail) ---
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setAlignment(Pos.TOP_CENTER);
        Label welcomeLabel = new Label("Bienvenue dans le Système de Facturation Pharmaceutique");


        //mainContent.getStyleClass().add("main-content-vbox"); // Appliquer une classe CSS

        //welcomeLabel.getStyleClass().add("welcome-label"); // Appliquer une classe CSS

        // Exemple simplifié de création de la scène de bienvenue pour la démonstration
        // Si vous utilisez FXML, vous ne créerez pas ces éléments directement ici.
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");


        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        mainContent.getChildren().add(welcomeLabel);
        root.setCenter(mainContent);


        // --- Actions des boutons ---
        btnNouvelleFacture.setOnAction(e -> handleNouvelleFacture());
        btnGestionProduits.setOnAction(e -> showProductForm());
        btnGestionClients.setOnAction(e -> showClientForm());
        btnGestionFournisseurs.setOnAction(e -> showFournisseurForm());
        btnReapprovisionnement.setOnAction(e -> showReapprovisionnementForm());
        btnGestionUtilisateurs.setOnAction(e -> showGestionUtilisateursForm());
        btnConsulterBenefices.setOnAction(e -> showRecettesForm());
        btnConsulterFacturesPayees.setOnAction(e -> showFacturesPayeesForm());
        btnConsulterFacturesProforma.setOnAction(e -> showFacturesProformaForm());
        btnVerrouiller.setOnAction(e -> toggleControls(true));

        // Gestion de la fermeture de l'application
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Fermeture de l'application.");
            Platform.exit();
            System.exit(0);
        });
    }

    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(200);
        button.setPrefHeight(40);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");
        // Effet au survol
        button.setOnMouseEntered(e -> button.setStyle("-fx-font-size: 16px; -fx-background-color: #e0e0e0; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;"));
        return button;
    }

    private void toggleControls(boolean disable) {
        // Ces contrôles sont désactivés quand l'application est "verrouillée" (hors mode facture)
        // Ils sont réactivés par handleNouvelleFacture()
        btnGestionProduits.setDisable(disable);
        btnGestionClients.setDisable(disable);
        btnGestionFournisseurs.setDisable(disable);
        btnReapprovisionnement.setDisable(disable);
        btnGestionUtilisateurs.setDisable(disable);
        btnConsulterBenefices.setDisable(disable);
        btnConsulterFacturesPayees.setDisable(disable);
        btnConsulterFacturesProforma.setDisable(disable);
        btnNouvelleFacture.setDisable(false); // Toujours actif pour déverrouiller
        btnVerrouiller.setDisable(false); // Toujours actif pour verrouiller

        // Si le formulaire est verrouillé, on affiche le message de bienvenue, sinon le formulaire de facturation
        if (disable) {
            VBox mainContent = new VBox(20);
            mainContent.setPadding(new Insets(20));
            mainContent.setAlignment(Pos.TOP_CENTER);
            Label welcomeLabel = new Label("Bienvenue dans le Système de Facturation Pharmaceutique");
            welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            mainContent.getChildren().add(welcomeLabel);
            root.setCenter(mainContent);
        } else {
            root.setCenter(new FacturationForm(stockService, facturationService).getView());
        }
        applyRolePermissions(); // Réappliquer les permissions après le toggle
    }

    // NOUVELLE MÉTHODE : Applique les permissions basées sur le rôle de l'utilisateur
    private void applyRolePermissions() {
        String role = utilisateurConnecte.getRole();
        System.out.println("Application des permissions pour le rôle: " + role);

        // Par défaut, désactiver tout ce qui n'est pas "Nouvelle Facture" ou "Verrouiller"
        btnGestionProduits.setDisable(true);
        btnGestionClients.setDisable(true);
        btnGestionFournisseurs.setDisable(true);
        btnReapprovisionnement.setDisable(true);
        btnGestionUtilisateurs.setDisable(true);
        btnConsulterBenefices.setDisable(true);
        btnConsulterFacturesPayees.setDisable(true);
        btnConsulterFacturesProforma.setDisable(true);

        switch (role) {
            case "ADMIN":
                // L'admin a accès à tout
                btnGestionProduits.setDisable(false);
                btnGestionClients.setDisable(false);
                btnGestionFournisseurs.setDisable(false);
                btnReapprovisionnement.setDisable(false);
                btnGestionUtilisateurs.setDisable(false);
                btnConsulterBenefices.setDisable(false);
                btnConsulterFacturesPayees.setDisable(false);
                btnConsulterFacturesProforma.setDisable(false);
                btnNouvelleFacture.setDisable(false);
                break;
            case "VENTE":
                // Le rôle VENTE gère les clients et les factures
                btnGestionClients.setDisable(false);
                btnNouvelleFacture.setDisable(false);
                btnConsulterFacturesPayees.setDisable(false);
                btnConsulterFacturesProforma.setDisable(false);
                btnConsulterBenefices.setDisable(false); // Peut consulter les bénéfices pour le suivi
                break;
            case "STOCK":
                // Le rôle STOCK gère les produits, fournisseurs et réapprovisionnement
                btnGestionProduits.setDisable(false);
                btnGestionFournisseurs.setDisable(false);
                btnReapprovisionnement.setDisable(false);
                break;
            default:
                // Pour tout autre rôle non défini, tout reste désactivé sauf login/logout
                break;
        }
        // Ces boutons sont toujours actifs pour tous les rôles
        btnNouvelleFacture.setDisable(false);
        btnVerrouiller.setDisable(false);
    }


    private void handleNouvelleFacture() {
        toggleControls(false); // Déverrouille les contrôles
    }

    private void showProductForm() {
        ProductForm productForm = new ProductForm(stockService);
        productForm.show();
    }

    private void showClientForm() {
        ClientForm clientForm = new ClientForm();
        clientForm.show();
    }

    private void showFournisseurForm() {
        FournisseurForm fournisseurForm = new FournisseurForm();
        fournisseurForm.show();
    }

    private void showReapprovisionnementForm() {
        ReapprovisionnementForm reapproForm = new ReapprovisionnementForm(stockService);
        reapproForm.show();
    }

    private void showGestionUtilisateursForm() {
        GestionUtilisateursForm gestionUtilisateursForm = new GestionUtilisateursForm();
        gestionUtilisateursForm.show();
    }

    private void showRecettesForm() {
        RecettesForm recettesForm = new RecettesForm(facturationService);
        recettesForm.show();
    }

    private void showFacturesPayeesForm() {
        FacturesPayeesForm facturesPayeesForm = new FacturesPayeesForm(facturationService);
        facturesPayeesForm.show();
    }

    private void showFacturesProformaForm() {
        FacturesProformaForm facturesProformaForm = new FacturesProformaForm(facturationService);
        facturesProformaForm.show();
    }

    public void show() {
        primaryStage.show();
    }
}