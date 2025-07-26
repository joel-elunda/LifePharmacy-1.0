package lifepharmacy.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lifepharmacy.models.Produit;
import lifepharmacy.utils.LocalDateAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProduitDAO {
    private static final String FILE_PATH = "data/produits.json"; // Créer un dossier 'data' à la racine du projet
    private Gson gson;
    private Path path;

    public ProduitDAO() {
        // Enregistrez votre LocalDateAdapter ici
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter()) // AJOUTEZ CETTE LIGNE
                .create();
        path = Paths.get(FILE_PATH);
        ensureFileExists();

        //gson = new GsonBuilder().setPrettyPrinting().create();
    }

    private void ensureFileExists() {
        if (!Files.exists(path)) {
            try {
                // Crée les répertoires parents si nécessaire
                Files.createDirectories(path.getParent());
                // Crée le fichier et y écrit une liste vide pour un JSON valide
                Files.write(path, gson.toJson(new ArrayList<>()).getBytes());
            } catch (IOException e) {
                System.err.println("Erreur lors de la création du fichier JSON pour les clients: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public List<Produit> getAll() {
        List<Produit> produits = new ArrayList<>();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type typeListProduit = new TypeToken<ArrayList<Produit>>() {}.getType();
            produits = gson.fromJson(reader, typeListProduit);
            if (produits == null) {
                produits = new ArrayList<>();
            }
        } catch (IOException e) {
            // Le fichier n'existe pas ou est vide, retourner une liste vide
            System.err.println("Le fichier " + FILE_PATH + " n'existe pas encore ou est vide. Création d'une nouvelle liste.");
        }
        return produits;
    }

    public Optional<Produit> getById(String id) {
        return getAll().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    public void save(Produit produit) {
        List<Produit> produits = getAll();
        produits.add(produit);
        saveAll(produits);
    }

    public void update(Produit updatedProduit) {
        List<Produit> produits = getAll();
        boolean found = false;
        for (int i = 0; i < produits.size(); i++) {
            if (produits.get(i).getId().equals(updatedProduit.getId())) {
                produits.set(i, updatedProduit);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(produits);
        } else {
            System.err.println("Produit non trouvé pour la mise à jour: " + updatedProduit.getId());
        }
    }

    public void delete(String id) {
        List<Produit> produits = getAll();
        produits.removeIf(p -> p.getId().equals(id));
        saveAll(produits);
    }

    private void saveAll(List<Produit> produits) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(produits, writer);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier JSON pour les produits: " + e.getMessage());
            e.printStackTrace();
        }
    }
}