// src/main/java/module-info.java (ou src/module-info.java)
module lifepharmacy.lifepharmacy { // Le nom de votre module
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson; // Pour GSON
    requires jbcrypt; // Pour BCrypt
    requires java.desktop; // Souvent nécessaire pour les fonctionnalités d'impression/fichier
    requires itextpdf; // <--- AJOUTEZ CETTE LIGNE

    // Ouvre le package des modèles à GSON pour que la réflexion fonctionne
    opens lifepharmacy.models to com.google.gson;

    // Expose les packages pour JavaFX
    opens lifepharmacy.lifepharmacy to javafx.fxml;
    exports lifepharmacy.lifepharmacy;
    exports lifepharmacy.ui;
    exports lifepharmacy.services;
    exports lifepharmacy.models;

    // Permet à TableView d'accéder aux propriétés des classes de modèle (via PropertyValueFactory)
    //opens lifepharmacy.models to javafx.base; // Cette ligne DOIT être unique
}
