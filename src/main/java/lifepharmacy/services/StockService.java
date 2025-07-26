package lifepharmacy.services;



import lifepharmacy.dao.ProduitDAO;
import lifepharmacy.models.Produit;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StockService {
    private ProduitDAO produitDAO;

    public StockService() {
        this.produitDAO = new ProduitDAO();
    }

    public List<Produit> getAllProduits() {
        return produitDAO.getAll();
    }

    public Optional<Produit> getProduitById(String id) {
        return produitDAO.getById(id);
    }

    public void addProduit(Produit produit) {
        if (produitDAO.getById(produit.getId()).isPresent()) {
            throw new IllegalArgumentException("Un produit avec cet ID existe déjà.");
        }
        produitDAO.save(produit);
    }

    public void updateProduit(Produit produit) {
        if (produitDAO.getById(produit.getId()).isEmpty()) {
            throw new IllegalArgumentException("Produit non trouvé pour la mise à jour.");
        }
        produitDAO.update(produit);
    }

    public void deleteProduit(String id) {
        if (produitDAO.getById(id).isEmpty()) {
            throw new IllegalArgumentException("Produit non trouvé pour la suppression.");
        }
        produitDAO.delete(id);
    }

    // NOUVELLE MÉTHODE
    public void reapprovisionnerProduit(String productId, int quantiteAjoutee) {
        Optional<Produit> produitOpt = produitDAO.getById(productId);
        if (produitOpt.isPresent()) {
            Produit produit = produitOpt.get();
            int nouveauStock = produit.getQuantiteEnStock() + quantiteAjoutee;
            produit.setQuantiteEnStock(nouveauStock);
            produitDAO.update(produit);
        } else {
            throw new IllegalArgumentException("Produit avec ID " + productId + " non trouvé.");
        }
    }

    public void retirerQuantiteProduit(String productId, int quantiteRetiree) {
        Optional<Produit> produitOpt = produitDAO.getById(productId);
        if (produitOpt.isPresent()) {
            Produit produit = produitOpt.get();
            if (produit.getQuantiteEnStock() < quantiteRetiree) {
                throw new IllegalArgumentException("Quantité insuffisante en stock pour le produit " + produit.getNom());
            }
            int nouveauStock = produit.getQuantiteEnStock() - quantiteRetiree;
            produit.setQuantiteEnStock(nouveauStock);
            produitDAO.update(produit);
        } else {
            throw new IllegalArgumentException("Produit avec ID " + productId + " non trouvé.");
        }
    }

    public List<Produit> getProduitsProchesPeremption(int joursAvant) {
        LocalDate dateLimite = LocalDate.now().plusDays(joursAvant);
        return produitDAO.getAll().stream()
                .filter(p -> p.getDatePeremption() != null && p.getDatePeremption().isBefore(dateLimite))
                .toList();
    }

    public List<Produit> getProduitsEnRuptureDeStock(int seuil) {
        return produitDAO.getAll().stream()
                .filter(p -> p.getQuantiteEnStock() <= seuil)
                .toList();
    }
}