package lifepharmacy.models;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Facture implements Serializable {
    private String numeroFacture;
    private LocalDate dateFacture;
    private Client client; // Client associé à la facture
    private List<LigneFacture> lignesFacture; // Liste des produits et quantités vendues
    private double total;
    private boolean isProforma; // True si c'est une facture proforma, false sinon

    public Facture(String numeroFacture, LocalDate dateFacture, Client client, boolean isProforma) {
        this.numeroFacture = numeroFacture;
        this.dateFacture = dateFacture;
        this.client = client;
        this.lignesFacture = new ArrayList<>();
        this.total = 0.0;
        this.isProforma = isProforma;
    }

    public Facture(String numeroFacture, LocalDate dateFacturation, Client client, List<LigneFacture> lignesFacture, double montantTotal, boolean isProforma) {
        this.numeroFacture = numeroFacture;
        this.dateFacture = dateFacturation;
        this.client = client;
        this.lignesFacture = lignesFacture;
        this.total = montantTotal;
        this.isProforma = isProforma;
    }



    // Getters
    public String getNumeroFacture() { return numeroFacture; }
    public LocalDate getDateFacture() { return dateFacture; }
    public Client getClient() { return client; }
    public List<LigneFacture> getLignesFacture() { return lignesFacture; }
    public double getTotal() { return total; }
    public boolean isProforma() { return isProforma; }

    // Setters
    public void setNumeroFacture(String numeroFacture) { this.numeroFacture = numeroFacture; }
    public void setDateFacture(LocalDate dateFacture) { this.dateFacture = dateFacture; }
    public void setClient(Client client) { this.client = client; }
    public void setLignesFacture(List<LigneFacture> lignesFacture) { this.lignesFacture = lignesFacture; }
    public void setTotal(double total) { this.total = total; }
    public void setProforma(boolean proforma) { isProforma = proforma; }

    // Méthode pour ajouter une ligne de facture
    public void addLigneFacture(LigneFacture ligne) {
        this.lignesFacture.add(ligne);
        this.total += ligne.getTotalLigne();
    }

    // Classe interne pour les lignes de facture (produit + quantité + prix à l'achat)
    public static class LigneFacture {
        public String getIdProduit() {
            return idProduit;
        }

        public void setIdProduit(String idProduit) {
            this.idProduit = idProduit;
        }

        private String idProduit; // Identifiant unique de la ligne de facture
        private Produit produit;
        private int quantite;
        private double prixUnitaireVendu; // Prix du produit au moment de la vente

        public LigneFacture(Produit produit, int quantite) {
            this.produit = produit;
            this.quantite = quantite;
            this.prixUnitaireVendu = produit.getPrixUnitaire(); // Capture le prix actuel du produit
        }

        // Getters
        public Produit getProduit() { return produit; }
        public int getQuantite() { return quantite; }
        public double getPrixUnitaireVendu() { return prixUnitaireVendu; }

        // Setters
        public void setProduit(Produit produit) { this.produit = produit; }
        public void setQuantite(int quantite) { this.quantite = quantite; }
        public void setPrixUnitaireVendu(double prixUnitaireVendu) { this.prixUnitaireVendu = prixUnitaireVendu; }

        public double getTotalLigne() {
            return quantite * prixUnitaireVendu;
        }
    }
}