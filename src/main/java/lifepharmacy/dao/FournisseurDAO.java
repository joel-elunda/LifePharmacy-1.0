package lifepharmacy.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lifepharmacy.models.Fournisseur;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FournisseurDAO {
    private static final String FILE_PATH = "data/fournisseurs.json";
    private Gson gson;

    public FournisseurDAO() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public List<Fournisseur> getAll() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type typeListFournisseur = new TypeToken<ArrayList<Fournisseur>>() {}.getType();
            fournisseurs = gson.fromJson(reader, typeListFournisseur);
            if (fournisseurs == null) {
                fournisseurs = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Le fichier " + FILE_PATH + " n'existe pas encore ou est vide. Création d'une nouvelle liste.");
        }
        return fournisseurs;
    }

    public Optional<Fournisseur> getById(String id) {
        return getAll().stream()
                .filter(f -> f.getId().equals(id))
                .findFirst();
    }

    public void save(Fournisseur fournisseur) {
        List<Fournisseur> fournisseurs = getAll();
        fournisseurs.add(fournisseur);
        saveAll(fournisseurs);
    }

    public void update(Fournisseur updatedFournisseur) {
        List<Fournisseur> fournisseurs = getAll();
        boolean found = false;
        for (int i = 0; i < fournisseurs.size(); i++) {
            if (fournisseurs.get(i).getId().equals(updatedFournisseur.getId())) {
                fournisseurs.set(i, updatedFournisseur);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(fournisseurs);
        } else {
            System.err.println("Fournisseur non trouvé pour la mise à jour: " + updatedFournisseur.getId());
        }
    }

    public void delete(String id) {
        List<Fournisseur> fournisseurs = getAll();
        fournisseurs.removeIf(f -> f.getId().equals(id));
        saveAll(fournisseurs);
    }

    private void saveAll(List<Fournisseur> fournisseurs) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(fournisseurs, writer);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier JSON pour les fournisseurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
