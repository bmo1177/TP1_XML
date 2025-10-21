import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parseur SAX pour lire et afficher le contenu du fichier livres.xml
 * Exporte les résultats en TXT et CSV dans le dossier output/
 */
public class ParserSAX {
    
    private static final String OUTPUT_DIR = "output";
    
    public static void main(String[] args) {
        try {
            // Créer le dossier output s'il n'existe pas
            creerDossierOutput();
            
            // Créer les fichiers de sortie avec timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String txtFile = OUTPUT_DIR + "/SAX_output_" + timestamp + ".txt";
            String csvFile = OUTPUT_DIR + "/SAX_export_" + timestamp + ".csv";
            
            PrintWriter txtWriter = new PrintWriter(new FileWriter(txtFile));
            PrintWriter csvWriter = new PrintWriter(new FileWriter(csvFile));
            
            // Étape 1 : Créer une factory pour construire le parseur SAX
            SAXParserFactory factory = SAXParserFactory.newInstance();
            
            // Étape 2 : Créer le parseur SAX
            SAXParser parser = factory.newSAXParser();
            
            String header = "=================================================\n" +
                          "     LECTURE DU FICHIER XML AVEC PARSEUR SAX\n" +
                          "=================================================\n";
            System.out.print(header);
            txtWriter.print(header);
            
            // Étape 3 : Créer notre handler personnalisé avec les writers
            LivreHandler handler = new LivreHandler(txtWriter, csvWriter);
            
            // Étape 4 : Parser le fichier XML avec notre handler
            parser.parse(new File("data/livres.xml"), handler);
            
            String footer = "\n=================================================\n" +
                          "        FIN DE LA LECTURE AVEC SAX\n" +
                          "=================================================\n";
            System.out.print(footer);
            txtWriter.print(footer);
            
            // Afficher les statistiques collectées
            String stats = handler.getStatistiques();
            System.out.print(stats);
            txtWriter.print(stats);
            
            // Fermer les writers
            txtWriter.close();
            csvWriter.close();
            
            // Message de confirmation
            String success = "\n✅ Fichiers générés avec succès :\n" +
                           "   📄 " + txtFile + "\n" +
                           "   📊 " + csvFile + "\n";
            System.out.println(success);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du parsing SAX : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crée le dossier output s'il n'existe pas
     */
    private static void creerDossierOutput() {
        try {
            Path outputPath = Paths.get(OUTPUT_DIR);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
                System.out.println("📁 Dossier '" + OUTPUT_DIR + "' créé.");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur création dossier output : " + e.getMessage());
        }
    }
}

/**
 * Handler personnalisé qui étend DefaultHandler
 * Redéfinit les méthodes appelées lors des événements SAX
 */
class LivreHandler extends DefaultHandler {
    
    // Writers pour les fichiers de sortie
    private PrintWriter txtWriter;
    private PrintWriter csvWriter;
    
    // Variables pour suivre le contexte actuel
    private String elementActuel = "";
    private StringBuilder contenu = new StringBuilder();
    
    // Variables pour la structure du document
    private int numeroLivre = 0;
    private int numeroSection = 0;
    private int numeroChapitre = 0;
    private int numeroParagraphe = 0;
    
    // Variables pour stocker les informations temporaires
    private String titreLivre = "";
    private String titreSection = "";
    private String titreChapitre = "";
    private String nomAuteur = "";
    private String prenomAuteur = "";
    private StringBuilder auteursLivre = new StringBuilder();
    
    // Indicateurs de contexte
    private boolean dansLivre = false;
    private boolean dansAuteur = false;
    private boolean dansSection = false;
    private boolean dansChapitre = false;
    private boolean dansParagraphe = false;
    private boolean dansTitre = false;
    private boolean premierAuteur = true;
    
    // Statistiques
    private int totalLivres = 0;
    private int totalAuteurs = 0;
    private int totalSections = 0;
    private int totalChapitres = 0;
    private int totalParagraphes = 0;
    
    /**
     * Constructeur avec les writers
     */
    public LivreHandler(PrintWriter txtWriter, PrintWriter csvWriter) {
        this.txtWriter = txtWriter;
        this.csvWriter = csvWriter;
        
        // En-tête CSV
        csvWriter.println("Numero_Livre,Titre_Livre,Auteurs,Numero_Section,Titre_Section,Numero_Chapitre,Titre_Chapitre,Nombre_Paragraphes");
    }
    
    /**
     * Appelée au début du document
     */
    @Override
    public void startDocument() throws SAXException {
        String msg = "\n📄 Début de l'analyse du document XML...\n\n";
        System.out.print(msg);
        txtWriter.print(msg);
    }
    
    /**
     * Appelée à la fin du document
     */
    @Override
    public void endDocument() throws SAXException {
        String msg = "\n✅ Fin de l'analyse du document XML.\n";
        System.out.print(msg);
        txtWriter.print(msg);
    }
    
    /**
     * Appelée au début de chaque élément
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) 
            throws SAXException {
        
        // Réinitialiser le contenu pour le nouvel élément
        contenu.setLength(0);
        elementActuel = qName;
        
        switch (qName) {
            case "livre":
                numeroLivre++;
                totalLivres++;
                dansLivre = true;
                numeroSection = 0;
                auteursLivre.setLength(0);
                premierAuteur = true;
                
                String livreHeader = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                   "LIVRE #" + numeroLivre + "\n" +
                                   "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
                System.out.print(livreHeader);
                txtWriter.print(livreHeader);
                break;
                
            case "auteur":
                dansAuteur = true;
                totalAuteurs++;
                break;
                
            case "section":
                numeroSection++;
                totalSections++;
                dansSection = true;
                numeroChapitre = 0;
                break;
                
            case "chapitre":
                numeroChapitre++;
                totalChapitres++;
                dansChapitre = true;
                numeroParagraphe = 0;
                break;
                
            case "paragraphe":
                numeroParagraphe++;
                totalParagraphes++;
                dansParagraphe = true;
                break;
                
            case "titre":
                dansTitre = true;
                break;
        }
    }
    
    /**
     * Appelée pour lire le contenu textuel d'un élément
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // Accumuler le contenu textuel
        contenu.append(new String(ch, start, length).trim());
    }
    
    /**
     * Appelée à la fin de chaque élément
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        
        String texte = contenu.toString().trim();
        
        switch (qName) {
            case "livre":
                dansLivre = false;
                System.out.println("\n");
                txtWriter.println("\n");
                break;
                
            case "titre":
                if (dansTitre) {
                    if (dansLivre && !dansSection && !dansChapitre) {
                        // Titre du livre
                        titreLivre = texte;
                        String titreInfo = "\n📚 Titre : " + titreLivre + "\n";
                        System.out.print(titreInfo);
                        txtWriter.print(titreInfo);
                    } else if (dansSection && !dansChapitre) {
                        // Titre de section
                        titreSection = texte;
                        String sectionInfo = "\n  ▶ Section " + numeroSection + " : " + titreSection + "\n";
                        System.out.print(sectionInfo);
                        txtWriter.print(sectionInfo);
                    } else if (dansChapitre) {
                        // Titre de chapitre
                        titreChapitre = texte;
                        String chapitreInfo = "\n    📖 Chapitre " + numeroChapitre + " : " + titreChapitre + "\n";
                        System.out.print(chapitreInfo);
                        txtWriter.print(chapitreInfo);
                    }
                    dansTitre = false;
                }
                break;
                
            case "auteurs":
                // Afficher l'en-tête des auteurs une fois
                if (premierAuteur) {
                    String auteurHeader = "\n✍️  Auteurs :\n";
                    System.out.print(auteurHeader);
                    txtWriter.print(auteurHeader);
                    premierAuteur = false;
                }
                break;
                
            case "auteur":
                if (dansAuteur) {
                    String auteurInfo = "   - " + prenomAuteur + " " + nomAuteur + "\n";
                    System.out.print(auteurInfo);
                    txtWriter.print(auteurInfo);
                    
                    // Ajouter à la liste des auteurs pour CSV
                    if (auteursLivre.length() > 0) {
                        auteursLivre.append("; ");
                    }
                    auteursLivre.append(prenomAuteur).append(" ").append(nomAuteur);
                    
                    nomAuteur = "";
                    prenomAuteur = "";
                    dansAuteur = false;
                }
                break;
                
            case "nom":
                if (dansAuteur) {
                    nomAuteur = texte;
                }
                break;
                
            case "prenom":
                if (dansAuteur) {
                    prenomAuteur = texte;
                }
                break;
                
            case "section":
                dansSection = false;
                break;
                
            case "chapitre":
                // Écrire la ligne CSV pour ce chapitre
                csvWriter.printf("\"%d\",\"%s\",\"%s\",\"%d\",\"%s\",\"%d\",\"%s\",\"%d\"\n",
                    numeroLivre,
                    echapperCSV(titreLivre),
                    echapperCSV(auteursLivre.toString()),
                    numeroSection,
                    echapperCSV(titreSection),
                    numeroChapitre,
                    echapperCSV(titreChapitre),
                    numeroParagraphe
                );
                
                String paraInfo = "       Nombre de paragraphes : " + numeroParagraphe + "\n";
                System.out.print(paraInfo);
                txtWriter.print(paraInfo);
                
                dansChapitre = false;
                break;
                
            case "paragraphe":
                if (dansParagraphe && numeroParagraphe == 1) {
                    // Afficher un extrait du premier paragraphe seulement
                    String extrait = texte.length() > 100 
                        ? texte.substring(0, 100) + "..." 
                        : texte;
                    String extraitInfo = "       Premier paragraphe (extrait) :\n" +
                                       "       \"" + extrait + "\"\n";
                    System.out.print(extraitInfo);
                    txtWriter.print(extraitInfo);
                }
                dansParagraphe = false;
                break;
        }
    }
    
    /**
     * Gestion des erreurs
     */
    @Override
    public void error(SAXParseException e) throws SAXException {
        String errMsg = "❌ Erreur : " + e.getMessage() + "\n" +
                       "   Ligne : " + e.getLineNumber() + "\n" +
                       "   Colonne : " + e.getColumnNumber() + "\n";
        System.err.print(errMsg);
    }
    
    /**
     * Gestion des erreurs fatales
     */
    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        String errMsg = "❌ Erreur fatale : " + e.getMessage() + "\n" +
                       "   Ligne : " + e.getLineNumber() + "\n" +
                       "   Colonne : " + e.getColumnNumber() + "\n";
        System.err.print(errMsg);
        throw e;
    }
    
    /**
     * Gestion des avertissements
     */
    @Override
    public void warning(SAXParseException e) throws SAXException {
        String warnMsg = "⚠️  Avertissement : " + e.getMessage() + "\n" +
                        "   Ligne : " + e.getLineNumber() + "\n";
        System.err.print(warnMsg);
    }
    
    /**
     * Échappe les caractères spéciaux pour CSV
     */
    private String echapperCSV(String texte) {
        if (texte == null) return "";
        return texte.replace("\"", "\"\"");
    }
    
    /**
     * Retourne les statistiques collectées
     */
    public String getStatistiques() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n📊 STATISTIQUES DU DOCUMENT XML\n");
        sb.append("─────────────────────────────────────────────────\n");
        sb.append("Total de livres      : ").append(totalLivres).append("\n");
        sb.append("Total d'auteurs      : ").append(totalAuteurs).append("\n");
        sb.append("Total de sections    : ").append(totalSections).append("\n");
        sb.append("Total de chapitres   : ").append(totalChapitres).append("\n");
        sb.append("Total de paragraphes : ").append(totalParagraphes).append("\n");
        
        if (totalLivres > 0) {
            double moyenneSections = (double) totalSections / totalLivres;
            double moyenneChapitres = (double) totalChapitres / totalLivres;
            double moyenneParagraphes = (double) totalParagraphes / totalLivres;
            
            sb.append("\nMoyenne de sections par livre    : ").append(String.format("%.2f", moyenneSections)).append("\n");
            sb.append("Moyenne de chapitres par livre   : ").append(String.format("%.2f", moyenneChapitres)).append("\n");
            sb.append("Moyenne de paragraphes par livre : ").append(String.format("%.2f", moyenneParagraphes)).append("\n");
        }
        
        sb.append("─────────────────────────────────────────────────\n");
        
        return sb.toString();
    }
}