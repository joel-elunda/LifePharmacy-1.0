package lifepharmacy.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lifepharmacy.models.Facture;
import lifepharmacy.utils.LocalDateAdapter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FactureDAO {
    private static final String FILE_PATH = "data/factures.json";
    private Gson gson;

    public FactureDAO() {
        // Enregistrer un adaptateur pour LocalDate
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
    }

    public List<Facture> getAll() {
        List<Facture> factures = new ArrayList<>();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type typeListFacture = new TypeToken<ArrayList<Facture>>() {}.getType();
            factures = gson.fromJson(reader, typeListFacture);
            if (factures == null) {
                factures = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Le fichier " + FILE_PATH + " n'existe pas encore ou est vide. Création d'une nouvelle liste.");
        }
        return factures;
    }

    public Optional<Facture> getByNumero(String numero) {
        return getAll().stream()
                .filter(f -> f.getNumeroFacture().equals(numero))
                .findFirst();
    }

    public Optional<Facture> getByNumeroFacture(String numeroFacture) {
        return getAll().stream()
                .filter(f -> f.getNumeroFacture().equals(numeroFacture))
                .findFirst();
    }

    public void save(Facture facture) {
        List<Facture> factures = getAll();
        factures.add(facture);
        saveAll(factures);
    }

    public void update(Facture updatedFacture) {
        List<Facture> factures = getAll();
        boolean found = false;
        for (int i = 0; i < factures.size(); i++) {
            if (factures.get(i).getNumeroFacture().equals(updatedFacture.getNumeroFacture())) {
                factures.set(i, updatedFacture);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(factures);
        } else {
            System.err.println("Facture non trouvée pour la mise à jour: " + updatedFacture.getNumeroFacture());
        }
    }

    public void delete(String numero) {
        List<Facture> factures = getAll();
        factures.removeIf(f -> f.getNumeroFacture().equals(numero));
        saveAll(factures);
    }

    private void saveAll(List<Facture> factures) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(factures, writer);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier JSON pour les factures: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
