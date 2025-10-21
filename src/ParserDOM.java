import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 * Parseur DOM pour lire et afficher le contenu du fichier livres.xml
 * DOM charge tout le document en mémoire sous forme d'arbre
 * Exporte les résultats en TXT et CSV dans le dossier output/
 */
public class ParserDOM {
    
    private static final String OUTPUT_DIR = "output";
    private static final String DATA_FILE = "data/livres.xml";
    
    public static void main(String[] args) {
        PrintWriter txtWriter = null;
        PrintWriter csvWriter = null;
        
        try {
            System.out.println("\n🚀 Démarrage du parseur DOM...\n");
            
            // Créer le dossier output s'il n'existe pas
            creerDossierOutput();
            
            // Étape 1 : Créer une factory pour construire le parseur
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace(true);
            factory.setIgnoringComments(true);
            
            // Étape 2 : Créer le parseur (DocumentBuilder)
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Gestionnaire d'erreurs
            builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
                public void warning(org.xml.sax.SAXParseException e) {
                    System.err.println("⚠️  Avertissement : " + e.getMessage());
                }
                public void error(org.xml.sax.SAXParseException e) {
                    System.err.println("❌ Erreur : " + e.getMessage());
                }
                public void fatalError(org.xml.sax.SAXParseException e) {
                    System.err.println("💥 Erreur fatale : " + e.getMessage());
                }
            });
            
            // Étape 3 : Parser le fichier XML et obtenir l'objet Document
            System.out.println("📖 Lecture du fichier : " + DATA_FILE);
            Document doc = builder.parse(new File(DATA_FILE));
            
            // Normaliser le document (optionnel mais recommandé)
            doc.getDocumentElement().normalize();
            System.out.println("✅ Document XML chargé et normalisé\n");
            
            // Créer les writers pour les fichiers de sortie
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String txtFile = OUTPUT_DIR + "/DOM_output_" + timestamp + ".txt";
            String csvFile = OUTPUT_DIR + "/DOM_export_" + timestamp + ".csv";
            
            txtWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(txtFile), "UTF-8"));
            csvWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(csvFile), "UTF-8"));
            
            // En-tête pour la console et fichier texte
            String header = "═══════════════════════════════════════════════════════════\n" +
                          "       LECTURE DU FICHIER XML AVEC PARSEUR DOM\n" +
                          "═══════════════════════════════════════════════════════════\n";
            afficher(header, txtWriter);
            
            // Étape 4 : Récupérer tous les éléments <livre>
            NodeList livres = doc.getElementsByTagName("livre");
            
            String info = "\n📚 Nombre de livres trouvés : " + livres.getLength() + "\n\n";
            afficher(info, txtWriter);
            
            // En-tête CSV avec BOM UTF-8 pour Excel
            csvWriter.write('\ufeff'); // BOM UTF-8
            csvWriter.println("Numero_Livre,Titre_Livre,Auteurs,Numero_Section,Titre_Section," +
                            "Numero_Chapitre,Titre_Chapitre,Nombre_Paragraphes");
            
            // Étape 5 : Parcourir chaque livre
            for (int i = 0; i < livres.getLength(); i++) {
                traiterLivre((Element) livres.item(i), i + 1, txtWriter, csvWriter);
            }
            
            String footer = "\n═══════════════════════════════════════════════════════════\n" +
                          "          FIN DE LA LECTURE AVEC DOM\n" +
                          "═══════════════════════════════════════════════════════════\n";
            afficher(footer, txtWriter);
            
            // Statistiques globales
            String stats = afficherStatistiques(doc);
            afficher(stats, txtWriter);
            
            // Message de confirmation
            System.out.println("\n✅ FICHIERS GÉNÉRÉS AVEC SUCCÈS :");
            System.out.println("   📄 " + txtFile);
            System.out.println("   📊 " + csvFile);
            System.out.println("\n💡 Ouvrez les fichiers CSV avec Excel ou LibreOffice Calc");
            
        } catch (FileNotFoundException e) {
            System.err.println("\n❌ ERREUR : Fichier non trouvé");
            System.err.println("   Vérifiez que le fichier existe : " + DATA_FILE);
            System.err.println("   Chemin absolu : " + new File(DATA_FILE).getAbsolutePath());
        } catch (org.xml.sax.SAXException e) {
            System.err.println("\n❌ ERREUR DE PARSING XML : " + e.getMessage());
            System.err.println("   Le fichier XML n'est pas bien formé");
        } catch (IOException e) {
            System.err.println("\n❌ ERREUR D'ENTRÉE/SORTIE : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n❌ ERREUR INATTENDUE : " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermer les writers
            if (txtWriter != null) txtWriter.close();
            if (csvWriter != null) csvWriter.close();
        }
    }
    
    /**
     * Traite un livre et l'affiche
     */
    private static void traiterLivre(Element livre, int numeroLivre, 
                                     PrintWriter txtWriter, PrintWriter csvWriter) {
        String separator = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
        String livreHeader = "📖 LIVRE #" + numeroLivre + "\n";
        
        afficher(separator + livreHeader + separator, txtWriter);
        
        // Récupérer le titre du livre
        String titreLivre = getTitreDirectEnfant(livre);
        String titreInfo = "\n📚 Titre : " + titreLivre + "\n";
        afficher(titreInfo, txtWriter);
        
        // Récupérer les auteurs
        StringBuilder auteursStr = new StringBuilder();
        afficher("\n✍️  Auteurs :\n", txtWriter);
        
        NodeList auteursNodes = livre.getElementsByTagName("auteur");
        for (int j = 0; j < auteursNodes.getLength(); j++) {
            Element auteur = (Element) auteursNodes.item(j);
            String nom = getTextContent(auteur, "nom");
            String prenom = getTextContent(auteur, "prenom");
            String auteurInfo = "   • " + prenom + " " + nom + "\n";
            afficher(auteurInfo, txtWriter);
            
            if (j > 0) auteursStr.append("; ");
            auteursStr.append(prenom).append(" ").append(nom);
        }
        
        // Récupérer les sections
        List<Element> sections = getEnfantsParNom(livre, "section");
        String sectionsInfo = "\n📑 Nombre de sections : " + sections.size() + "\n";
        afficher(sectionsInfo, txtWriter);
        
        for (int j = 0; j < sections.size(); j++) {
            traiterSection(sections.get(j), numeroLivre, j + 1, titreLivre, 
                          auteursStr.toString(), txtWriter, csvWriter);
        }
        
        afficher("\n", txtWriter);
    }
    
    /**
     * Traite une section
     */
    private static void traiterSection(Element section, int numeroLivre, int numeroSection,
                                       String titreLivre, String auteurs,
                                       PrintWriter txtWriter, PrintWriter csvWriter) {
        String titreSection = getTitreDirectEnfant(section);
        
        String sectionInfo = "\n  ▶ Section " + numeroSection + " : " + titreSection + "\n";
        afficher(sectionInfo, txtWriter);
        
        // Récupérer les chapitres de cette section
        List<Element> chapitres = getEnfantsParNom(section, "chapitre");
        String chapitresInfo = "    Nombre de chapitres : " + chapitres.size() + "\n";
        afficher(chapitresInfo, txtWriter);
        
        for (int k = 0; k < chapitres.size(); k++) {
            traiterChapitre(chapitres.get(k), numeroLivre, numeroSection, k + 1,
                           titreLivre, auteurs, titreSection, txtWriter, csvWriter);
        }
    }
    
    /**
     * Traite un chapitre
     */
    private static void traiterChapitre(Element chapitre, int numeroLivre, int numeroSection,
                                        int numeroChapitre, String titreLivre, String auteurs,
                                        String titreSection, PrintWriter txtWriter, 
                                        PrintWriter csvWriter) {
        String titreChapitre = getTitreDirectEnfant(chapitre);
        
        String chapitreInfo = "\n    📖 Chapitre " + numeroChapitre + " : " + titreChapitre + "\n";
        afficher(chapitreInfo, txtWriter);
        
        // Récupérer les paragraphes
        NodeList paragraphes = chapitre.getElementsByTagName("paragraphe");
        String paraInfo = "       Nombre de paragraphes : " + paragraphes.getLength() + "\n";
        afficher(paraInfo, txtWriter);
        
        // Afficher un extrait du premier paragraphe
        if (paragraphes.getLength() > 0) {
            String contenuParagraphe = paragraphes.item(0).getTextContent().trim();
            String extrait = contenuParagraphe.length() > 100 
                ? contenuParagraphe.substring(0, 100) + "..." 
                : contenuParagraphe;
            String extraitInfo = "       Premier paragraphe (extrait) :\n" +
                               "       \"" + extrait + "\"\n";
            afficher(extraitInfo, txtWriter);
        }
        
        // Ajouter aux données CSV
        csvWriter.printf("\"%d\",\"%s\",\"%s\",\"%d\",\"%s\",\"%d\",\"%s\",\"%d\"\n",
            numeroLivre,
            echapperCSV(titreLivre),
            echapperCSV(auteurs),
            numeroSection,
            echapperCSV(titreSection),
            numeroChapitre,
            echapperCSV(titreChapitre),
            paragraphes.getLength()
        );
    }
    
    /**
     * Affiche un message sur la console et dans le fichier
     */
    private static void afficher(String message, PrintWriter writer) {
        System.out.print(message);
        if (writer != null) {
            writer.print(message);
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
                System.out.println("📁 Dossier '" + OUTPUT_DIR + "' créé.\n");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur création dossier output : " + e.getMessage());
        }
    }
    
    /**
     * Récupère le titre qui est enfant direct de l'élément
     */
    private static String getTitreDirectEnfant(Element element) {
        NodeList enfants = element.getChildNodes();
        for (int i = 0; i < enfants.getLength(); i++) {
            Node node = enfants.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && 
                node.getNodeName().equals("titre")) {
                return node.getTextContent().trim();
            }
        }
        return "";
    }
    
    /**
     * Récupère le contenu textuel d'un élément enfant
     */
    private static String getTextContent(Element parent, String childName) {
        NodeList nodes = parent.getElementsByTagName(childName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return "";
    }
    
    /**
     * Récupère les éléments enfants directs avec un nom spécifique
     */
    private static List<Element> getEnfantsParNom(Element parent, String nomElement) {
        List<Element> resultat = new ArrayList<>();
        NodeList enfants = parent.getChildNodes();
        
        for (int i = 0; i < enfants.getLength(); i++) {
            Node node = enfants.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && 
                node.getNodeName().equals(nomElement)) {
                resultat.add((Element) node);
            }
        }
        return resultat;
    }
    
    /**
     * Échappe les caractères spéciaux pour CSV
     */
    private static String echapperCSV(String texte) {
        if (texte == null) return "";
        // Remplacer les guillemets doubles par deux guillemets doubles
        return texte.replace("\"", "\"\"");
    }
    
    /**
     * Affiche des statistiques sur le document XML
     */
    private static String afficherStatistiques(Document doc) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\n📊 STATISTIQUES DU DOCUMENT XML\n");
        sb.append("─────────────────────────────────────────────────────────\n");
        
        int nbLivres = doc.getElementsByTagName("livre").getLength();
        int nbAuteurs = doc.getElementsByTagName("auteur").getLength();
        int nbSections = doc.getElementsByTagName("section").getLength();
        int nbChapitres = doc.getElementsByTagName("chapitre").getLength();
        int nbParagraphes = doc.getElementsByTagName("paragraphe").getLength();
        
        sb.append(String.format("%-30s : %d\n", "Total de livres", nbLivres));
        sb.append(String.format("%-30s : %d\n", "Total d'auteurs", nbAuteurs));
        sb.append(String.format("%-30s : %d\n", "Total de sections", nbSections));
        sb.append(String.format("%-30s : %d\n", "Total de chapitres", nbChapitres));
        sb.append(String.format("%-30s : %d\n", "Total de paragraphes", nbParagraphes));
        
        // Calcul des moyennes
        if (nbLivres > 0) {
            double moyenneSections = (double) nbSections / nbLivres;
            double moyenneChapitres = (double) nbChapitres / nbLivres;
            double moyenneParagraphes = (double) nbParagraphes / nbLivres;
            
            sb.append("\n");
            sb.append(String.format("%-30s : %.2f\n", "Moy. sections/livre", moyenneSections));
            sb.append(String.format("%-30s : %.2f\n", "Moy. chapitres/livre", moyenneChapitres));
            sb.append(String.format("%-30s : %.2f\n", "Moy. paragraphes/livre", moyenneParagraphes));
            
            if (nbSections > 0) {
                double moyChapParSection = (double) nbChapitres / nbSections;
                sb.append(String.format("%-30s : %.2f\n", "Moy. chapitres/section", moyChapParSection));
            }
        }
        
        sb.append("─────────────────────────────────────────────────────────\n");
        
        return sb.toString();
    }
}