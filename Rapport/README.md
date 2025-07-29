# Rapport de Projet de Fin d'Études - ONCF

Ce dossier contient le rapport LaTeX pour le projet de fin d'études réalisé à l'ONCF (Office National des Chemins de Fer).

## Structure du rapport

Le rapport suit les spécifications standards pour un PFE et respecte les normes de présentation (marges 2.5cm/2cm, police Times New Roman 12pt, interligne 1.5, etc.).

### Fichiers principaux

- `main.tex` - Fichier principal du rapport
- `page_garde.tex` - Page de garde
- `dedicaces.tex` - Page de dédicaces
- `remerciements.tex` - Remerciements
- `acronymes.tex` - Liste des acronymes et abréviations
- `resume.tex` - Résumé du projet
- `introduction_generale.tex` - Introduction générale
- `chapitre1.tex` - Chapitre 1 : Présentation de l'entreprise ONCF
- `conclusion_generale.tex` - Conclusion générale
- `bibliographie.tex` - Références bibliographiques
- `annexes.tex` - Annexes

## Prérequis

Pour compiler le rapport, vous devez avoir installé :

- Une distribution LaTeX complète (TeX Live, MiKTeX, ou MacTeX)
- Les packages LaTeX suivants :
  - `babel` (support français)
  - `times` (police Times New Roman)
  - `geometry` (configuration des marges)
  - `setspace` (interligne)
  - `fancyhdr` (en-têtes et pieds de page)
  - `graphicx` (inclusion d'images)
  - `hyperref` (liens hypertexte)
  - `tocloft` (personnalisation de la table des matières)
  - `titlesec` (personnalisation des titres)

## Compilation

### Méthode 1 : Ligne de commande

```bash
# Compilation complète (recommandée)
pdflatex main.tex
pdflatex main.tex
pdflatex main.tex

# Ou avec latexmk (si disponible)
latexmk -pdf main.tex
```

### Méthode 2 : IDE LaTeX

Vous pouvez utiliser des IDEs comme :
- **TeXstudio** (recommandé)
- **TeXworks**
- **Overleaf** (en ligne)
- **VS Code** avec l'extension LaTeX Workshop

## Personnalisation

### Informations à modifier

1. **Page de garde** (`page_garde.tex`) :
   - Nom de l'université/école
   - Titre du projet
   - Nom de l'étudiant
   - Nom de l'encadrant
   - Nom du maître de stage

2. **Remerciements** (`remerciements.tex`) :
   - Noms des personnes à remercier
   - Nom de l'établissement

3. **Autres sections** :
   - Ajoutez vos propres chapitres en créant de nouveaux fichiers `.tex`
   - Modifiez le contenu selon votre projet spécifique

### Ajout de chapitres

Pour ajouter un nouveau chapitre :

1. Créez un fichier `chapitre2.tex`, `chapitre3.tex`, etc.
2. Ajoutez `\input{chapitre2}` dans `main.tex`
3. Supprimez le commentaire devant la ligne correspondante

### Ajout d'images

1. Placez vos images dans un dossier `images/`
2. Utilisez `\includegraphics{images/mon_image.png}` pour les inclure
3. Assurez-vous que le logo ONCF est accessible (chemin actuel : `../src/main/resources/static/images/Logo-oncf.png`)

## Spécifications techniques

Le rapport respecte les spécifications suivantes :

- **Format** : A4 (210 × 297 mm)
- **Marges** : 
  - Gauche : 2.5 cm (avec reliure 0.5 cm)
  - Droite : 2 cm
  - Haut/Bas : 2 cm
- **Police** : Times New Roman 12 points
- **Interligne** : 1.5
- **Justification** : Texte justifié
- **Numérotation** : 
  - Pages préliminaires : chiffres romains (i, ii, iii...)
  - Corps du rapport : chiffres arabes (1, 2, 3...)

## Conseils

1. **Compilation multiple** : Compilez 2-3 fois pour que la table des matières, les références croisées et la numérotation soient correctes.

2. **Gestion des erreurs** : Si vous rencontrez des erreurs de compilation, vérifiez :
   - Les caractères spéciaux (utilisez `\&` pour &, `\%` pour %, etc.)
   - Les chemins d'images
   - Les packages manquants

3. **Sauvegarde** : Pensez à sauvegarder régulièrement vos fichiers et utilisez un système de contrôle de version (Git).

4. **Révision** : Relisez attentivement le contenu et vérifiez la cohérence du formatage.

## Support

Ce template suit les standards académiques français pour les rapports de PFE. Pour toute question sur LaTeX, consultez :

- [Documentation LaTeX officielle](https://www.latex-project.org/help/documentation/)
- [Wikibooks LaTeX](https://en.wikibooks.org/wiki/LaTeX)
- [Stack Overflow (tag latex)](https://stackoverflow.com/questions/tagged/latex)

## Licence

Ce template est libre d'utilisation pour les projets académiques.