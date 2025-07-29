# Makefile pour la compilation du rapport LaTeX
# Projet ONCF - Rapport de PFE

# Variables
MAIN = main
TEX_FILES = $(wildcard *.tex)
PDF_OUTPUT = $(MAIN).pdf

# Compilateur LaTeX
LATEX = pdflatex
LATEX_FLAGS = -interaction=nonstopmode -halt-on-error

# Commande par défaut
all: $(PDF_OUTPUT)

# Règle principale de compilation
$(PDF_OUTPUT): $(TEX_FILES)
	@echo "Compilation du rapport en cours..."
	$(LATEX) $(LATEX_FLAGS) $(MAIN).tex
	@echo "Deuxième passe pour les références..."
	$(LATEX) $(LATEX_FLAGS) $(MAIN).tex
	@echo "Troisième passe pour finaliser..."
	$(LATEX) $(LATEX_FLAGS) $(MAIN).tex
	@echo "Compilation terminée avec succès!"

# Compilation rapide (une seule passe)
quick: $(MAIN).tex
	@echo "Compilation rapide..."
	$(LATEX) $(LATEX_FLAGS) $(MAIN).tex

# Nettoyage des fichiers temporaires
clean:
	@echo "Nettoyage des fichiers temporaires..."
	rm -f *.aux *.log *.toc *.lof *.lot *.out *.nav *.snm *.vrb *.fls *.fdb_latexmk *.synctex.gz

# Nettoyage complet (y compris le PDF)
distclean: clean
	@echo "Nettoyage complet..."
	rm -f $(PDF_OUTPUT)

# Ouverture du PDF (selon l'OS)
view: $(PDF_OUTPUT)
ifeq ($(OS),Windows_NT)
	start $(PDF_OUTPUT)
else
	UNAME_S := $(shell uname -s)
	ifeq ($(UNAME_S),Linux)
		xdg-open $(PDF_OUTPUT)
	endif
	ifeq ($(UNAME_S),Darwin)
		open $(PDF_OUTPUT)
	endif
endif

# Vérification des packages LaTeX requis
check-packages:
	@echo "Vérification des packages LaTeX..."
	@echo "Cette commande nécessite kpsewhich (inclus dans les distributions LaTeX)"
	@packages="babel times geometry setspace fancyhdr graphicx hyperref tocloft titlesec"; \
	for pkg in $$packages; do \
		if kpsewhich $$pkg.sty > /dev/null 2>&1; then \
			echo "✓ $$pkg : OK"; \
		else \
			echo "✗ $$pkg : MANQUANT"; \
		fi; \
	done

# Installation automatique des packages (pour TeX Live)
install-packages:
	@echo "Installation des packages LaTeX via tlmgr..."
	tlmgr install babel babel-french times geometry setspace fancyhdr graphicx hyperref tocloft titlesec

# Aide
help:
	@echo "Makefile pour le rapport LaTeX - Projet ONCF"
	@echo ""
	@echo "Commandes disponibles:"
	@echo "  all              - Compilation complète du rapport (3 passes)"
	@echo "  quick            - Compilation rapide (1 passe)"
	@echo "  clean            - Supprime les fichiers temporaires"
	@echo "  distclean        - Supprime tous les fichiers générés"
	@echo "  view             - Ouvre le PDF généré"
	@echo "  check-packages   - Vérifie la présence des packages LaTeX"
	@echo "  install-packages - Installe les packages manquants (TeX Live)"
	@echo "  help             - Affiche cette aide"
	@echo ""
	@echo "Exemples d'usage:"
	@echo "  make             # Compilation complète"
	@echo "  make quick       # Compilation rapide pour tests"
	@echo "  make clean       # Nettoyage avant archivage"
	@echo "  make view        # Compilation et ouverture du PDF"

# Règle pour éviter les conflits avec des fichiers du même nom
.PHONY: all quick clean distclean view check-packages install-packages help

# Compilation et ouverture automatique du PDF
build-and-view: all view

# Surveillance des changements (nécessite inotify-tools sur Linux)
watch:
	@echo "Surveillance des modifications (Ctrl+C pour arrêter)..."
	@while inotifywait -e modify *.tex 2>/dev/null; do \
		echo "Fichier modifié, recompilation..."; \
		make quick; \
	done