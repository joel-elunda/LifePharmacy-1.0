package lifepharmacy.models;


import java.io.Serializable;
import java.time.LocalDate;

public class Produit implements Serializable {
    private String id;
    private String nom;
    private String description;
    private double prixAchat; // Champ pour le prix d'achat
    private double prixVente;

    public double getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(double prixAchat) {
        this.prixAchat = prixAchat;
    }

    public double getPrixVente() {
        return prixVente;
    }

    public void setPrixVente(double prixVente) {
        this.prixVente = prixVente;
    }

    private double prixUnitaire;
    private int quantiteEnStock;
    private LocalDate datePeremption;

    public Produit(String id, String nom, String description, double prixUnitaire, int quantiteEnStock, LocalDate datePeremption) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prixUnitaire = prixUnitaire;
        this.quantiteEnStock = quantiteEnStock;
        this.datePeremption = datePeremption;
    }
    public Produit(String id, String nom, String description, double prixAchat, double prixVente, int quantiteStock, LocalDate datePeremption) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prixAchat = prixAchat;
        this.prixVente = prixVente;
        this.quantiteEnStock = quantiteStock;
        this.datePeremption = datePeremption;
    }

    // Getters
    public String getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public double getPrixUnitaire() { return prixUnitaire; }
    public int getQuantiteEnStock() { return quantiteEnStock; }
    public LocalDate getDatePeremption() { return datePeremption; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNom(String nom) { this.nom = nom; }
    public void setDescription(String description) { this.description = description; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }
    public void setQuantiteEnStock(int quantiteEnStock) { this.quantiteEnStock = quantiteEnStock; }
    public void setDatePeremption(LocalDate datePeremption) { this.datePeremption = datePeremption; }

    @Override
    public String toString() {
        return "Produit{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", prixUnitaire=" + prixUnitaire +
                ", quantiteEnStock=" + quantiteEnStock +
                ", datePeremption=" + datePeremption +
                '}';
    }
}