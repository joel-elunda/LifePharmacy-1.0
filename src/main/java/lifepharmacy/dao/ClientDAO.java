package lifepharmacy.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lifepharmacy.models.Client;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO {
    private static final String FILE_PATH = "data/clients.json";
    private Gson gson;
    private Path path;

    public ClientDAO() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                // Ajoutez ici le LocalDateAdapter si vos clients ont des dates
                // .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .create();
        path = Paths.get(FILE_PATH);
        ensureFileExists(); // C'est ici qu'elle est appelée
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

    public List<Client> getAll() {
        List<Client> clients = new ArrayList<>();
        try (FileReader reader = new FileReader(FILE_PATH)) {
            Type typeListClient = new TypeToken<ArrayList<Client>>() {}.getType();
            clients = gson.fromJson(reader, typeListClient);
            if (clients == null) {
                clients = new ArrayList<>();
            }
        } catch (IOException e) {
            System.err.println("Le fichier " + FILE_PATH + " n'existe pas encore ou est vide. Création d'une nouvelle liste.");
        }
        return clients;
    }

    public Optional<Client> getById(String id) {
        return getAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public void save(Client client) {
        List<Client> clients = getAll();
        clients.add(client);
        saveAll(clients);
    }

    public void update(Client updatedClient) {
        List<Client> clients = getAll();
        boolean found = false;
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId().equals(updatedClient.getId())) {
                clients.set(i, updatedClient);
                found = true;
                break;
            }
        }
        if (found) {
            saveAll(clients);
        } else {
            System.err.println("Client non trouvé pour la mise à jour: " + updatedClient.getId());
        }
    }

    public void delete(String id) {
        List<Client> clients = getAll();
        clients.removeIf(c -> c.getId().equals(id));
        saveAll(clients);
    }

    private void saveAll(List<Client> clients) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(clients, writer);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier JSON pour les clients: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
