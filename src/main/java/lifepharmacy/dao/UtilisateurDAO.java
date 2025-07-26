package lifepharmacy.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lifepharmacy.models.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UtilisateurDAO  {
    private static final String FILE_PATH = "data/utilisateurs.json";
    private Gson gson;
    private Path path;

    public UtilisateurDAO() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        path = Paths.get(FILE_PATH);

        createDefaultAdminIfNotExists();
    }

    // Dans UtilisateurDAO.java
    private void createDefaultAdminIfNotExists() {
        List<Utilisateur> utilisateurs = getAll();
        if (utilisateurs.isEmpty()) {
            // C'est ici que le problème se produit souvent.
            // Assurez-vous que le hachage est généré CORRECTEMENT.
            String password = "admin"; // Le mot de passe en clair pour le premier utilisateur
            String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
            Utilisateur defaultAdmin = new Utilisateur(UUID.randomUUID().toString(), "admin", hashedPassword, "ADMIN");
            save(defaultAdmin);
            System.out.println("Utilisateur 'admin' par défaut créé avec le mot de passe: '" + password + "' (haché)");
            System.out.println("Hachage généré: " + hashedPassword); // Pour le débogage
        }
    }

    public List<Utilisateur> getAll() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type typeListUtilisateur = new TypeToken<ArrayList<Utilisateur>>() {}.getType();
            utilisateurs = gson.fromJson(reader, typeListUtilisateur);
            if (utilisateurs == null) {
                utilisateurs = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Le fichier " + FILE_PATH + " n'existe pas encore ou est vide. Création d'une nouvelle liste.");
            // Initialiser un utilisateur admin si le fichier n'existe pas
            if (utilisateurs.isEmpty()) {
                System.out.println("Création de l'utilisateur admin par défaut.");
                // Hacher le mot de passe avant de le sauvegarder
                String hashedPassword = BCrypt.hashpw("admin", BCrypt.gensalt());
                utilisateurs.add(new Utilisateur("USR001", "admin", hashedPassword, "ADMIN"));
                saveAll(utilisateurs);
            }
        }
        return utilisateurs;
    }

    public Optional<Utilisateur> getById(String id) {
        return getAll().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public Optional<Utilisateur> getByUsername(String username) {
        return getAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    public void save(Utilisateur utilisateur) {
        List<Utilisateur> utilisateurs = getAll();
        utilisateurs.add(utilisateur);
        saveAll(utilisateurs);
    }

    public void update(Utilisateur updatedUtilisateur) {
        List<Utilisateur> utilisateurs = getAll();
        boolean found = false;
        for (int i = 0; i < utilisateurs.size(); i++) {
            if (utilisateurs.get(i).getId().equals(updatedUtilisateur.getId())) {
                utilisateurs.set(i, updatedUtilisateur);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(utilisateurs);
        } else {
            System.err.println("Utilisateur non trouvé pour la mise à jour: " + updatedUtilisateur.getId());
        }
    }

    public void delete(String id) {
        List<Utilisateur> utilisateurs = getAll();
        utilisateurs.removeIf(u -> u.getId().equals(id));
        saveAll(utilisateurs);
    }

    private void saveAll(List<Utilisateur> utilisateurs) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(utilisateurs, writer);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier JSON pour les utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }
}