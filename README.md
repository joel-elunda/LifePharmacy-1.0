# 💊 Système de Facturation et de Gestion Pharmaceutique

## Table des Matières
1.  [Introduction](#1-introduction)
2.  [Fonctionnalités](#2-fonctionnalités)
3.  [Technologies Utilisées](#3-technologies-utilisées)
4.  [Prérequis](#4-prérequis)
5.  [Installation et Lancement](#5-installation-et-lancement)
6.  [Structure du Projet](#6-structure-du-projet)
7.  [Utilisation](#7-utilisation)
8.  [Rôles des Utilisateurs](#8-rôles-des-utilisateurs)
9.  [Persistance des Données](#9-persistance-des-données)
10. [Améliorations Futures Possibles](#10-améliorations-futures-possibles)
11. [Contribution](#11-contribution)
12. [Licence](#12-licence)

---

## 1. Introduction

Ce projet est une application de bureau robuste développée en JavaFX, conçue pour la gestion et la facturation dans le contexte d'une pharmacie. Il offre une interface utilisateur intuitive pour la gestion des produits, des clients, des fournisseurs, la création de factures (payées et proforma), la gestion des utilisateurs avec différents rôles, et le suivi des bénéfices.

L'application vise à simplifier les opérations quotidiennes d'une pharmacie en offrant un système centralisé pour la gestion des stocks, des ventes et des données clients.

---

## 2. Fonctionnalités

* **Authentification des Utilisateurs** : Système de connexion sécurisé avec différents rôles (Admin, Vente, Stock).
* **Gestion des Produits** : Ajouter, modifier, supprimer et consulter les produits en stock, y compris la gestion des dates de péremption et des quantités.
* **Gestion des Clients** : Ajouter, modifier, supprimer et consulter les informations des clients.
* **Gestion des Fournisseurs** : Ajouter, modifier, supprimer et consulter les informations des fournisseurs.
* **Facturation** :
    * Création de nouvelles factures.
    * Distinction entre factures payées et factures proforma.
    * Gestion des lignes de facture (produit, quantité, prix, total).
    * Mise à jour automatique du stock après une vente.
* **Réapprovisionnement** : Fonctionnalité pour gérer l'ajout de stock de produits existants.
* **Gestion des Utilisateurs** : (Accessible par les administrateurs) Ajouter, modifier, supprimer des utilisateurs et gérer leurs rôles.
* **Consultation des Bénéfices** : Afficher les bénéfices générés par les ventes.
* **Consultation des Factures** : Lister et consulter les factures existantes (payées et proforma).
* **Persistance des Données** : Toutes les données sont sauvegardées dans des fichiers JSON.
* **Interface Utilisateur Moderne** : Design soigné grâce à JavaFX et CSS.

---

## 3. Technologies Utilisées

* **Java 17+** : Langage de programmation principal.
* **JavaFX** : Pour l'interface utilisateur graphique (GUI).
* **Gson (Google Gson)** : Bibliothèque Java pour la sérialisation et désérialisation d'objets Java en JSON et vice-versa.
* **jBCrypt** : Pour le hachage sécurisé des mots de passe.

---

## 4. Prérequis

Avant de lancer l'application, assurez-vous d'avoir les éléments suivants installés :

* **Java Development Kit (JDK)** : Version 17 ou supérieure.
* **IntelliJ IDEA** (ou un autre IDE Java comme Eclipse, VS Code avec extensions Java) : Recommandé pour le développement et la gestion du projet.
* **Maven** ou **Gradle** : (Si le projet est configuré avec un système de build) Pour la gestion des dépendances et la compilation.

---

## 5. Installation et Lancement

Suivez ces étapes pour installer et lancer l'application :

1.  **Cloner le dépôt (si applicable) ou télécharger le code source** :
    ```bash
    git clone [URL_DU_DEPOT]
    cd nom-du-projet
    ```

2.  **Ouvrir le projet dans IntelliJ IDEA** :
    * Lancez IntelliJ IDEA.
    * Sélectionnez `Open` et naviguez vers le dossier racine du projet.
    * IntelliJ devrait automatiquement détecter qu'il s'agit d'un projet Maven/Gradle et importer les dépendances.

3.  **Vérifier les Dépendances (pom.xml / build.gradle)** :
    Assurez-vous que les dépendances `javafx`, `gson` et `jbcrypt` sont correctement listées dans votre fichier `pom.xml` (pour Maven) ou `build.gradle` (pour Gradle).

    **Exemple pour Maven (`pom.xml`) :**
    ```xml
    <dependencies>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-controls</artifactId>
            <version>VotreVersionJavaFX</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-fxml</artifactId>
            <version>VotreVersionJavaFX</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>
    </dependencies>
    ```

4.  **Configurer `module-info.java`** :
    Le système de modules Java (JPMS) nécessite des configurations spécifiques. Assurez-vous que votre fichier `src/main/java/module-info.java` contient les directives suivantes :

    ```java
    module lifepharmacy.lifepharmacy { // Adaptez le nom de votre module
        requires javafx.controls;
        requires javafx.fxml;
        requires com.google.gson;
        requires jbcrypt;

        opens com.pharmacie.app to javafx.fxml, com.google.gson;
        exports com.pharmacie.app;
        exports com.pharmacie.app.ui;
        exports com.pharmacie.app.service;
        exports com.pharmacie.app.model;

        // Important pour GSON et JavaFX TableView pour accéder aux propriétés des modèles
        opens com.pharmacie.app.model to com.google.gson, javafx.base;
        opens com.pharmacie.app.dao to com.google.gson; // Si GSON interagit avec des champs privés des DAOs
    }
    ```
    **Note** : Assurez-vous d'avoir une seule déclaration pour chaque `opens` ou `exports`.

5.  **Enregistrer le `LocalDateAdapter` pour GSON** :
    Puisque l'application utilise `java.time.LocalDate`, un `TypeAdapter` personnalisé est nécessaire pour la sérialisation/désérialisation avec GSON.
    * Vérifiez que le fichier `src/main/java/com/pharmacie/app/util/LocalDateAdapter.java` existe et contient le code fourni.
    * Assurez-vous que dans chaque classe DAO (`ProduitDAO`, `FactureDAO`, etc.) qui gère des objets avec `LocalDate`, la configuration de Gson inclut l'adaptateur :
        ```java
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        ```

6.  **Nettoyer et Reconstruire le Projet** :
    Dans IntelliJ IDEA, allez dans `Build` -> `Rebuild Project`. Si des problèmes persistent, essayez `File` -> `Invalidate Caches / Restart...` puis `Build` -> `Rebuild Project`.

7.  **Exécuter l'Application** :
    Lancez la classe `MainApp` (ou votre classe principale qui contient la méthode `main`).

---

## 6. Structure du Projet

Le projet suit une architecture modulaire et une séparation des préoccupations :

* `src/main/java/com/pharmacie/app/`
    * `dao/` : Classes Data Access Object pour l'interaction avec les fichiers JSON (persistance).
    * `model/` : Classes de modèle représentant les entités métier (Produit, Client, Facture, Utilisateur, etc.).
    * `service/` : Classes de service contenant la logique métier (gestion du stock, facturation, authentification).
    * `ui/` : Classes de l'interface utilisateur et contrôleurs JavaFX.
    * `util/` : Classes utilitaires (comme `LocalDateAdapter`).
    * `MainApp.java` : Point d'entrée de l'application.
* `src/main/resources/`
    * `styles/` : Fichiers CSS pour la personnalisation de l'interface.
    * `data/` : Dossier où les fichiers JSON des données sont stockés (`produits.json`, `utilisateurs.json`, etc.). Ce dossier est créé automatiquement si inexistant.

---

## 7. Utilisation

Lors du premier lancement, l'application crée un utilisateur administrateur par défaut si aucun utilisateur n'est présent.

* **Identifiant Admin par défaut** : `admin`
* **Mot de passe Admin par défaut** : `admin`

Après la connexion, l'interface principale permet d'accéder aux différentes fonctionnalités via le menu latéral, avec des permissions basées sur le rôle de l'utilisateur connecté.

---

## 8. Rôles des Utilisateurs

L'application prend en charge trois rôles principaux avec des permissions distinctes :

* **ADMIN** : Accès complet à toutes les fonctionnalités (gestion des produits, clients, fournisseurs, utilisateurs, facturation, réapprovisionnement, consultation des bénéfices).
* **VENTE** : Accès aux fonctionnalités liées aux ventes (création de factures, gestion des clients, consultation des factures payées/proforma et bénéfices).
* **STOCK** : Accès aux fonctionnalités liées au stock (gestion des produits, des fournisseurs, et réapprovisionnement).

---

## 9. Persistance des Données

Toutes les données de l'application (produits, clients, fournisseurs, factures, utilisateurs) sont stockées localement dans des fichiers JSON situés dans le répertoire `data/` à la racine du projet.

---

## 10. Améliorations Futures Possibles

* **Rapports PDF** : Génération de factures ou de rapports de stock au format PDF.
* **Notifications de Stock** : Alertes pour les produits en rupture de stock ou périmés.
* **Gestion des Retours/Remboursements** : Ajout d'une fonctionnalité pour les retours de produits.
* **Base de Données Relationnelle** : Migration de la persistance des données de JSON vers une base de données (ex: SQLite, MySQL) pour une meilleure scalabilité et intégrité des données.
* **Historique des Transactions** : Enregistrement détaillé de toutes les opérations.
* **Internationalisation (i18n)** : Prise en charge de plusieurs langues.

---

## 11. Contribution

Les contributions sont les bienvenues ! Si vous souhaitez améliorer cette application, n'hésitez pas à :
1.  Forker ce dépôt.
2.  Créer une branche pour votre fonctionnalité (`git checkout -b feature/AmazingFeature`).
3.  Commiter vos changements (`git commit -m 'Add some AmazingFeature'`).
4.  Pousser vers la branche (`git push origin feature/AmazingFeature`).
5.  Ouvrir une Pull Request.

---

## 12. Licence

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.
