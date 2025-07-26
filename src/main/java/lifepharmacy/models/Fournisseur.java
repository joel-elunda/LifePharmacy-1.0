package lifepharmacy.models;

import java.io.Serializable;

public class Fournisseur implements Serializable {
    private String id;
    private String nom;
    private String contactPersonne;
    private String telephone;
    private String adresse;
    private String email;

    public Fournisseur(String id, String nom, String contactPersonne, String telephone, String adresse, String email) {
        this.id = id;
        this.nom = nom;
        this.contactPersonne = contactPersonne;
        this.telephone = telephone;
        this.adresse = adresse;
        this.email = email;
    }

    // Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getContactPersonne() { return contactPersonne; }
    public String getTelephone() { return telephone; }
    public String getAdresse() { return adresse; }
    public String getEmail() { return email; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setContactPersonne(String contactPersonne) { this.contactPersonne = contactPersonne; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "Fournisseur{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", contactPersonne='" + contactPersonne + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
