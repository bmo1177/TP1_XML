# Rapport TP1 : Introduction à XML et Parseurs DOM/SAX

**Master 2 - Génie Logiciel**  
**Date :** 22 Octobre 2025  
**Auteur :** bmo_1177

---

## 1. Introduction

Ce rapport présente le travail réalisé dans le cadre du TP1 sur le langage XML et ses parseurs. L'objectif était de créer une structure XML représentant des livres, puis de lire et analyser ces données en utilisant deux approches différentes : DOM (Document Object Model) et SAX (Simple API for XML).

---

## 2. Structure XML Réalisée

### 2.1 Architecture Générale

La structure XML développée respecte les contraintes suivantes :

```
bibliotheque (racine)
├── livre (x2)
    ├── titre
    ├── auteurs
    │   └── auteur (x1 ou plus)
    │       ├── nom
    │       └── prenom
    └── section (x3)
        ├── titre
        └── chapitre (x2 ou plus)
            ├── titre
            └── paragraphe (x2 ou plus)
```

### 2.2 Justification des Choix

**Élément racine `<bibliotheque>` :**
- Permet de regrouper plusieurs livres dans un même fichier
- Facilite l'extension future (ajout de métadonnées, catalogues, etc.)

**Utilisation d'éléments plutôt qu'attributs :**
- Les titres, noms, prénoms sont des éléments car ils peuvent être longs
- Meilleure lisibilité et maintenabilité
- Facilite les transformations XSLT futures

**Structure hiérarchique :**
- Reflète la structure logique d'un livre
- Permet une navigation intuitive (livre → section → chapitre → paragraphe)

### 2.3 Validation

Le document XML a été validé avec succès en utilisant :
- ✅ VS Code avec l'extension XML Tools
- ✅ Parseur DOM Java (aucune exception levée)
- ✅ Parseur SAX Java (parsing sans erreur)

Le document est **bien-formé** (balises correctement fermées, imbrication correcte) mais n'utilise pas de DTD formelle dans cette version.

---

## 3. Comparaison DOM vs SAX

### 3.1 Approche DOM (Document Object Model)

#### Principe de Fonctionnement
DOM charge l'intégralité du document XML en mémoire sous forme d'arbre. Chaque élément, attribut et texte devient un nœud de cet arbre.

#### Avantages
- ✅ **Accès aléatoire** : Possibilité de naviguer librement dans l'arbre
- ✅ **Modifications faciles** : Ajout, suppression, modification de nœuds
- ✅ **Navigation bidirectionnelle** : Parent → enfant et enfant → parent
- ✅ **API intuitive** : Méthodes comme `getElementsByTagName()`, `getChildNodes()`

#### Inconvénients
- ❌ **Consommation mémoire élevée** : Tout le document est en RAM
- ❌ **Temps de chargement** : Doit parser l'intégralité avant traitement
- ❌ **Inadapté aux gros fichiers** : Risque de `OutOfMemoryError`

#### Cas d'Usage Recommandés
- Fichiers XML de petite/moyenne taille (< 10 MB)
- Nécessité de modifier le document
- Accès multiple à différentes parties du document
- Configuration d'applications (web.xml, pom.xml, etc.)

### 3.2 Approche SAX (Simple API for XML)

#### Principe de Fonctionnement
SAX lit le document séquentiellement et déclenche des événements (`startElement`, `characters`, `endElement`) sans charger tout le document en mémoire.

#### Avantages
- ✅ **Faible consommation mémoire** : Lecture en flux
- ✅ **Rapidité** : Commence le traitement immédiatement
- ✅ **Adapté aux gros fichiers** : Peut traiter des fichiers de plusieurs GB
- ✅ **Streaming** : Idéal pour le traitement séquentiel

#### Inconvénients
- ❌ **Pas d'accès aléatoire** : Lecture unidirectionnelle
- ❌ **Pas de modification** : Le document ne peut pas être modifié
- ❌ **Complexité du code** : Gestion manuelle de l'état avec des variables
- ❌ **Pas de retour en arrière** : Une fois un élément lu, on ne peut pas y revenir

#### Cas d'Usage Recommandés
- Fichiers XML volumineux (> 100 MB)
- Traitement séquentiel simple (comptage, filtrage, extraction)
- Conversion de format (XML → CSV, JSON, etc.)
- Flux de données en temps réel (RSS, logs, etc.)

### 3.3 Tableau Comparatif

| Critère | DOM | SAX |
|---------|-----|-----|
| Consommation mémoire | Élevée (tout en RAM) | Faible (flux) |
| Vitesse de démarrage | Lente | Rapide |
| Accès aux données | Aléatoire | Séquentiel |
| Modification | Oui | Non |
| Complexité du code | Simple | Moyenne |
| Taille fichiers supportés | Petite/moyenne | Illimitée |

### 3.4 Recommandation pour ce TP

Pour notre fichier `livres.xml` (~15 KB), **DOM est plus approprié** car :
- La taille est négligeable en mémoire
- Le code est plus lisible et maintenable
- Permet des traitements plus complexes si nécessaire

SAX serait préférable si nous devions traiter un catalogue de bibliothèque avec des milliers de livres.

---

## 4. Résultats Obtenus

### 4.1 Sortie du Parseur DOM

```
=================================================
     LECTURE DU FICHIER XML AVEC PARSEUR DOM
=================================================

Nombre de livres trouvés : 2

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
LIVRE #1
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📚 Titre : Introduction au Langage XML

✍️  Auteurs :
   - Marie Dupont
   - Jean Martin

📑 Nombre de sections : 3

  ▶ Section 1 : Les Fondamentaux du XML
    Nombre de chapitres : 3
    
    📖 Chapitre 1 : Qu'est-ce que XML ?
       Nombre de paragraphes : 3
       Premier paragraphe (extrait) :
       "XML signifie eXtensible Markup Language..."
```

### 4.2 Sortie du Parseur SAX

```
=================================================
     LECTURE DU FICHIER XML AVEC PARSEUR SAX
=================================================

📄 Début de l'analyse du document XML...

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
LIVRE #1
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

📚 Titre : Introduction au Langage XML

✍️  Auteurs :
   - Marie Dupont
   - Jean Martin
   
  ▶ Section 1 : Les Fondamentaux du XML
    
    📖 Chapitre 1 : Qu'est-ce que XML ?
       Premier paragraphe (extrait) :
       "XML signifie eXtensible Markup Language..."
```

### 4.3 Statistiques

Les deux parseurs ont correctement identifié :
- **2 livres**
- **3 auteurs** au total
- **6 sections**
- **12 chapitres**
- **24 paragraphes**

---

## 5. Difficultés Rencontrées et Solutions

### 5.1 Problème : Récupération des titres avec DOM

**Difficulté :** `getElementsByTagName("titre")` retournait tous les titres du document, y compris ceux des sections et chapitres imbriqués.

**Solution :** 
```java
// Parcourir les résultats et vérifier le parent
for (int t = 0; t < titresChapitre.getLength(); t++) {
    Node titreNode = titresChapitre.item(t);
    if (titreNode.getParentNode().equals(chapitre)) {
        titreChapitre = titreNode.getTextContent();
        break;
    }
}
```

### 5.2 Problème : Gestion de l'état avec SAX

**Difficulté :** Distinguer entre le titre du livre, de la section et du chapitre.

**Solution :** Utilisation de variables booléennes de contexte :
```java
private boolean dansLivre = false;
private boolean dansSection = false;
private boolean dansChapitre = false;
```

### 5.3 Problème : Encodage des caractères

**Difficulté :** Caractères accentués mal affichés dans la console.

**Solution :** 
- Ajout de `encoding="UTF-8"` dans la déclaration XML
- Configuration de l'encodage de la console : `System.out.println()`

### 5.4 Problème : Espaces et retours à la ligne

**Difficulté :** SAX capture aussi les espaces entre les balises comme du texte.

**Solution :**
```java
String texte = contenu.toString().trim();
if (!texte.isEmpty()) {
    // Traiter le contenu
}
```

---

## 6. Extensions Possibles (Bonus)

### 6.1 Filtrage par Auteur

Ajout d'une méthode pour filtrer les livres par auteur :

```java
public static void filtrerParAuteur(Document doc, String nomAuteur) {
    // Implémentation du filtre
}
```

### 6.2 Export vers CSV

Création d'un fichier CSV avec la structure :
```
Titre Livre,Auteur,Nombre Sections,Nombre Chapitres
```

### 6.3 Validation par DTD

Création d'une DTD formelle :

```xml
<!DOCTYPE bibliotheque [
<!ELEMENT bibliotheque (livre+)>
<!ELEMENT livre (titre, auteurs, section+)>
<!ELEMENT auteurs (auteur+)>
<!ELEMENT auteur (nom, prenom)>
...
]>
```

---

## 7. Conclusion

Ce TP a permis de :
- ✅ Comprendre la structure et la syntaxe XML
- ✅ Maîtriser les concepts de documents bien-formés
- ✅ Implémenter deux approches de parsing (DOM et SAX)
- ✅ Analyser les avantages et inconvénients de chaque méthode
- ✅ Manipuler les API Java pour XML

**Apprentissages clés :**
1. XML est un format universel pour l'échange de données structurées
2. Le choix entre DOM et SAX dépend du contexte (taille, traitement)
3. La gestion des erreurs est cruciale lors du parsing
4. La structure hiérarchique XML offre une grande flexibilité

**Perspectives :**
- Exploration de XPath pour des requêtes plus avancées
- Utilisation de XSLT pour les transformations
- Étude des schémas XML (XSD) pour une validation plus riche
- Intégration avec des bases de données XML natives

---

## 8. Références

- Documentation W3C XML : https://www.w3.org/XML/
- Oracle Java XML API : https://docs.oracle.com/javase/tutorial/jaxp/
- Cours XML - Slides du professeur
- DOM Level 3 Specification : https://www.w3.org/TR/DOM-Level-3-Core/
- SAX Project : http://www.saxproject.org/

---

**Fin du rapport**