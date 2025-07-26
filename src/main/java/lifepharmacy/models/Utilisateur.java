package lifepharmacy.models;


import java.io.Serializable;

public class Utilisateur implements Serializable {
    private static final long serialVersionUID = 1L; // Recommandé pour Serializable

    private String id;
    private String username;
    private String passwordHash;  // mot de passe, pas le mot de passe en clair
    private String role; // Ex: "ADMIN", "CAISSIER", "MANAGER"

    public Utilisateur(String id, String username, String passwordHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}