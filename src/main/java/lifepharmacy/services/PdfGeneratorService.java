package lifepharmacy.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lifepharmacy.models.Facture;
import lifepharmacy.models.Facture.LigneFacture;
import lifepharmacy.models.Produit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Optional; // Pour récupérer le produit

public class PdfGeneratorService {

    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.BLUE);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font BOLD_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

    private StockService stockService; // Pour récupérer les détails du produit

    public PdfGeneratorService(StockService stockService) {
        this.stockService = stockService;
    }

    public void generateFacturePdf(Facture facture, String filePath) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50); // Marge: gauche, droite, haut, bas

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // --- En-tête de la facture ---
            addHeader(document, facture);

            // --- Détails du client ---
            addClientDetails(document, facture);

            // --- Détails des produits/lignes de facture ---
            addLignesFactureTable(document, facture);

            // --- Totaux et remarques ---
            addTotalsAndRemarks(document, facture);

        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
    }

    private void addHeader(Document document, Facture facture) throws DocumentException {
        Paragraph title = new Paragraph("FACTURE " + (facture.isProforma() ? "PROFORMA" : "COMMERCIALE"), TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE); // Ligne vide

        Paragraph pharmacyInfo = new Paragraph("PHARMACIE DE L'AVENIR", SUBTITLE_FONT);
        pharmacyInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(pharmacyInfo);

        Paragraph addressInfo = new Paragraph("Adresse: 123, Rue de la Paix, Lubumbashi\n" +
                "Téléphone: +243 999 123 456\n" +
                "Email: info@pharmacie-avenir.com", SMALL_FONT);
        addressInfo.setAlignment(Element.ALIGN_CENTER);
        document.add(addressInfo);
        document.add(Chunk.NEWLINE);

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 1});
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Cellule gauche
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph("Numéro Facture: " + facture.getNumeroFacture(), BOLD_FONT));
        leftCell.addElement(new Paragraph("Date: " + facture.getDateFacture().format(dateFormatter), BOLD_FONT));
        headerTable.addCell(leftCell);

        // Cellule droite (vide ou informations supplémentaires si besoin)
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.addElement(new Paragraph("", BOLD_FONT)); // Espace
        headerTable.addCell(rightCell);

        document.add(headerTable);
        document.add(new Chunk(new LineSeparator())); // Ligne de séparation
        document.add(Chunk.NEWLINE);
    }

    private void addClientDetails(Document document, Facture facture) throws DocumentException {
        Paragraph clientTitle = new Paragraph("Détails du Client:", BOLD_FONT);
        document.add(clientTitle);

        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(50); // Occupe 50% de la largeur
        clientTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        clientTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        clientTable.setSpacingAfter(10f); // Espace après la table

        clientTable.addCell(new Phrase("Nom:", BOLD_FONT));
        clientTable.addCell(new Phrase(facture.getClient().getNom(), NORMAL_FONT));
        clientTable.addCell(new Phrase("Téléphone:", BOLD_FONT));
        clientTable.addCell(new Phrase(facture.getClient().getTelephone(), NORMAL_FONT));
        clientTable.addCell(new Phrase("Adresse:", BOLD_FONT));
        clientTable.addCell(new Phrase(facture.getClient().getAdresse(), NORMAL_FONT));

        document.add(clientTable);
        document.add(new Chunk(new LineSeparator())); // Ligne de séparation
        document.add(Chunk.NEWLINE);
    }

    private void addLignesFactureTable(Document document, Facture facture) throws DocumentException {
        Paragraph itemsTitle = new Paragraph("Détails de la Facture:", BOLD_FONT);
        document.add(itemsTitle);
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6); // Num, Désignation, Qté, Prix Unitaire, Total HT, Remise/TVA (simplifié)
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        table.setWidths(new float[]{0.5f, 3f, 1f, 1.5f, 1.5f, 1.5f});

        // En-têtes du tableau
        addTableHeader(table, "N°", "Désignation", "Quantité", "Prix Unit. (CDF)", "Total HT (CDF)", "TVA (simplifiée)");

        // Lignes de la facture
        int itemNumber = 1;
        for (LigneFacture ligne : facture.getLignesFacture()) {
            Optional<Produit> produitOptional = stockService.getProduitById(ligne.getIdProduit());
            String nomProduit = produitOptional.map(Produit::getNom).orElse("Produit Inconnu");
            double prixUnitaire = ligne.getPrixUnitaireVendu(); // Utilise le prix unitaire enregistré dans la ligne de facture
            double totalLigneHT = ligne.getQuantite() * prixUnitaire;
            // Simplification de la TVA pour l'exemple (à adapter selon votre logique)
            double tvaPourcent = 0.16; // Exemple: 16% de TVA
            double tvaLigne = totalLigneHT * tvaPourcent;

            table.addCell(new Phrase(String.valueOf(itemNumber++), NORMAL_FONT));
            table.addCell(new Phrase(nomProduit, NORMAL_FONT));
            table.addCell(new Phrase(String.valueOf(ligne.getQuantite()), NORMAL_FONT));
            table.addCell(new Phrase(String.format("%,.2f", prixUnitaire), NORMAL_FONT));
            table.addCell(new Phrase(String.format("%,.2f", totalLigneHT), NORMAL_FONT));
            table.addCell(new Phrase(String.format("%,.2f", tvaLigne), NORMAL_FONT)); // TVA simplifiée
        }

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, BOLD_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTotalsAndRemarks(Document document, Facture facture) throws DocumentException {
        // Table pour les totaux (alignée à droite)
        PdfPTable totalsTable = new PdfPTable(2);
        totalsTable.setWidthPercentage(50); // Occupe 50% de la largeur
        totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalsTable.setSpacingBefore(20f);
        totalsTable.setSpacingAfter(20f);
        totalsTable.getDefaultCell().setBorder(Rectangle.NO_BORDER); // Enlève les bordures par défaut

        double montantTotalHT = 0;
        double montantTotalTVA = 0;
        double tvaPourcent = 0.16; // Exemple: 16% de TVA
        for (LigneFacture ligne : facture.getLignesFacture()) {
            double prixUnitaire = ligne.getPrixUnitaireVendu();
            double totalLigneHT = ligne.getQuantite() * prixUnitaire;
            montantTotalHT += totalLigneHT;
            montantTotalTVA += (totalLigneHT * tvaPourcent);
        }

        double montantTotalTTC = montantTotalHT + montantTotalTVA;


        // Ligne Montant HT
        PdfPCell labelCellHT = new PdfPCell(new Phrase("Montant Total HT:", BOLD_FONT));
        labelCellHT.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCellHT.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(labelCellHT);
        PdfPCell valueCellHT = new PdfPCell(new Phrase(String.format("%,.2f CDF", montantTotalHT), NORMAL_FONT));
        valueCellHT.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCellHT.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(valueCellHT);

        // Ligne TVA
        PdfPCell labelCellTVA = new PdfPCell(new Phrase("TVA (" + (int)(tvaPourcent*100) + "%):", BOLD_FONT));
        labelCellTVA.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCellTVA.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(labelCellTVA);
        PdfPCell valueCellTVA = new PdfPCell(new Phrase(String.format("%,.2f CDF", montantTotalTVA), NORMAL_FONT));
        valueCellTVA.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCellTVA.setBorder(Rectangle.NO_BORDER);
        totalsTable.addCell(valueCellTVA);


        // Ligne Montant Total TTC (avec bordure)
        PdfPCell labelCellTTC = new PdfPCell(new Phrase("MONTANT TOTAL TTC:", BOLD_FONT));
        labelCellTTC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCellTTC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        labelCellTTC.setPadding(5);
        totalsTable.addCell(labelCellTTC);
        PdfPCell valueCellTTC = new PdfPCell(new Phrase(String.format("%,.2f CDF", montantTotalTTC), BOLD_FONT));
        valueCellTTC.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCellTTC.setBackgroundColor(BaseColor.LIGHT_GRAY);
        valueCellTTC.setPadding(5);
        totalsTable.addCell(valueCellTTC);


        document.add(totalsTable);

        // Remarques ou pied de page
        document.add(new Chunk(new LineSeparator()));
        document.add(Chunk.NEWLINE);
        Paragraph remarks = new Paragraph("Merci pour votre confiance !", SMALL_FONT);
        remarks.setAlignment(Element.ALIGN_CENTER);
        document.add(remarks);
    }
}