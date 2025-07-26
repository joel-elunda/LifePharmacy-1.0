package lifepharmacy.services;



import lifepharmacy.dao.FactureDAO;
import lifepharmacy.dao.ProduitDAO;
import lifepharmacy.models.Client;
import lifepharmacy.models.Facture;
import lifepharmacy.models.Produit;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FacturationService {
    private FactureDAO factureDAO;
    private StockService stockService; // Pour gérer le stock lors de la vente

    public FacturationService() {
        this.factureDAO = new FactureDAO();
        this.stockService = new StockService(); // Initialiser le StockService
    }

    public List<Facture> getAllFactures() {
        return factureDAO.getAll();
    }

    public Optional<Facture> getFactureByNumero(String numero) {
        return factureDAO.getByNumeroFacture(numero);
    }

    public Facture creerFacture(Client client, List<Facture.LigneFacture> lignesFacture, boolean isProforma) {
        if (client == null) {
            throw new IllegalArgumentException("Le client ne peut pas être nul.");
        }
        if (lignesFacture == null || lignesFacture.isEmpty()) {
            throw new IllegalArgumentException("La facture doit contenir au moins une ligne.");
        }

        double montantTotal = 0.0;
        for (Facture.LigneFacture ligne : lignesFacture) {
            Produit produit = ligne.getProduit();
            int quantite = ligne.getQuantite();
            if (stockService.getProduitById(produit.getId()).isEmpty()) {
                throw new IllegalArgumentException("Produit " + produit.getNom() + " non trouvé dans le stock.");
            }
            if (!isProforma) { // Seul le stock réel est impacté pour une facture non proforma
                stockService.retirerQuantiteProduit(produit.getId(), quantite); // Réduire le stock
            }
            montantTotal += ligne.getTotalLigne();
        }

        String numeroFacture = genererNouveauNumeroFacture();
        Facture nouvelleFacture = new Facture(numeroFacture, LocalDate.now(), client, lignesFacture, montantTotal, isProforma);
        factureDAO.save(nouvelleFacture);
        return nouvelleFacture;
    }

    public String genererNouveauNumeroFacture() {
        // Logique simple pour générer un numéro de facture unique (ex: FAC-YYYYMMDD-001)
        long count = factureDAO.getAll().size() + 1;
        return "FAC-" + LocalDate.now().toString().replace("-", "") + "-" + String.format("%03d", count);
    }

    // NOUVELLES MÉTHODES
    public List<Facture> getFacturesPayees() {
        return factureDAO.getAll().stream()
                .filter(f -> !f.isProforma())
                .collect(Collectors.toList());
    }

    public List<Facture> getFacturesProforma() {
        return factureDAO.getAll().stream()
                .filter(Facture::isProforma)
                .collect(Collectors.toList());
    }

    public double calculerBeneficesPeriode(LocalDate debut, LocalDate fin) {
        return factureDAO.getAll().stream()
                .filter(f -> !f.isProforma() && !f.getDateFacture().isBefore(debut) && !f.getDateFacture().isAfter(fin))
                .mapToDouble(facture -> {
                    double beneficeFacture = 0.0;
                    for (Facture.LigneFacture ligne : facture.getLignesFacture()) {
                        Produit produit = ligne.getProduit();
                        double coutAchat = produit.getPrixAchat() * ligne.getQuantite();
                        double prixVente = ligne.getPrixUnitaireVendu() * ligne.getQuantite();
                        beneficeFacture += (prixVente - coutAchat);
                    }
                    return beneficeFacture;
                })
                .sum();
    }
}