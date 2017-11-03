package bridgeBid;

import bridgePlay.Jeu;

/*
 *			Gestion des variables couleur
 */

class Variable {

	static int nbVar = 10; 	// nombre de variables
	// 0 = ?
	// 1 = M	majeure
	// 2 = m	mineure
	// 3 = M'	autre majeure
	// 4 = m'	autre mineure
	// 5 = x, 6 = y, z = 7	couleur quelconque dans l'ordre croissant
	// 8 = n	couleur quelconque non annoncée
	// 9 = s	couleur soutenue

	static int nbSav = 10; // nombre de sauvegardes empilées
	
	private String var[];	//	stockage des variables
	private int indSav;		//  index sauvegarde
	private int varCour;	//  variable courante
	private EvalJeu jeu;
	private DonneBid Donne;

	/**
	 * constructeur
	 * @param pDonne
	 */
	public Variable(DonneBid pDonne) {
		var = new String[nbSav * nbVar];
		initSave();
		varCour = 0;
		Donne = pDonne;
		jeu = Donne.getJeu();
		init();
	}

	/**
	 *  initialisation des variables
	 */
	public void init() {
		for (int i = 0; i < nbVar; i++)
			var[i] = new String("");
	}
	
	/**
	 *  initialisation de la sauvegarde des variables
	 */
	public void initSave()  {
		indSav = 0;		
	}
	
	/**
	 *  sauvegarde des variables
	 */
	public void saveVar() {
		indSav += nbVar;
		if (indSav >= nbSav * nbVar) {
			System.out.println("Débordement pile variables");
			System.exit(1);
		}
		for (int i = 0; i < nbVar; i++)
			var[indSav + i] = new String(var[i]);
	}

	/**
	 *  Restauration des variables
	 */
	public void restVar() {
		if (indSav == 0) {
			System.out.println("Pile variables vide");
			System.exit(1);
		}
		for (int i = 0; i < nbVar; i++)
			var[i] = var[indSav + i];
		indSav -= nbVar;
	}
	
	/**
	 *  destruction de la dernière sauvegarde
	 */

	public void norestVar() {
		if (indSav > 0) {
			indSav -= nbVar;
		}
	}

	/**
	 *  dump des variables
	 */
	public void dump() {
		for (int i = 0; i < nbVar; i++)
			System.out.println("Variable:" + i + "=" + var[i]);
	}

	/**
	 * évaluation d'une variable couleur
	 * @param i
	 * @return contenu variable i
	 */
	public String getVar(int i) {
		if (i <= 0 || i >= nbVar) {
			System.out.println("Erreur getVar variable inconnue:" + i);
			dump();
			System.exit(1);
		}
		varCour = 0;
		if (var[i].length() == 0) {
			// calcul de la variable couleur
			switch (i) {
			case (1): // M si non affecté la plus longue
				// 55 la plus chère, 44 la moins chère
				int lp = jeu.nbCartes('P');
				int lc = jeu.nbCartes('C');
				if (lc > lp || lc == lp && lc < 5) {
					var[1] = "CP";
				} else {
					var[1] = "PC";
				}
				varCour = 1;
				break;
			case (2): // m si non affecté la plus longue
				// 55 la plus chère, 44 la moins chère
				int lk = jeu.nbCartes('K');
				int lt = jeu.nbCartes('T');
				if (lt > lk || lt == lk && lt < 5) {
					var[2] = "TK";
				} else {
					var[2] = "KT";
				}
				varCour = 2;
				break;
			case (5): // x détermination en fct de y
				if (var[6].length() == 0)
					break;
				else if (var[6].equals("K")) {
					var[5] = "T";
				} else if (var[6].equals("C")) {
					var[5] = sortDec("TK");
					varCour = 5;
				} else if (var[6].equals("P")) {
					var[5] = sortDec("TKC");
					varCour = 5;
				}
				break;
			case (6): // y détermination en fct de x et/ou z
				if (var[5].length() == 1 && var[7].length() == 1) {
					if (var[5].equals("T") && var[7].equals("C")) {
						var[6] = "K";
					} else if (var[5].equals("T") && var[7].equals("P")) {
						var[6] = sortDec("KC");
						varCour = 6;
					} else if (var[5].equals("K") && var[7].equals("P")) {
						var[6] = "C";
					}
				} else if (var[5].length() == 1) {
					if (var[5].equals("T")) {
						var[6] = sortDec("KCP");
						varCour = 6;
					} else if (var[5].equals("K")) {
						var[6] = sortDec("CP");
						varCour = 6;
					} else if (var[5].equals("C")) {
						var[6] = "P";
					}
				} else if (var[7].length() == 1) {
					if (var[7].equals("K")) {
						var[6] = "T";
					} else if (var[7].equals("C")) {
						var[6] = sortDec("TK");
						varCour = 6;
					} else if (var[7].equals("P")) {
						var[6] = sortDec("TKC");
						varCour = 6;
					}
				}
				break;
			case (7): // z détermination en fct de y
				if (var[6].length() > 0) {
					if (var[6].equals("T")) {
						var[7] = sortDec("KCP");
						varCour = 7;
					} else if (var[6].equals("K")) {
						var[7] = sortDec("CP");
						varCour = 7;
					} else if (var[6].equals("C")) {
						var[7] = "P";
					}
					break;
				}
				break;
			case (8): // n recherche couleur non annoncée
				var[8] = sortDec(Donne.CouleursNonAnnoncees());
				varCour = 8;
				break;
			case (9): // s recherche couleur soutenue
				var[9] = sortDec(Donne.CouleursSoutenues());
				varCour = 9;
				break;
			}
		}
		return var[i];
	}

	/**
	 * positionnement de la valeur de la variable courante
	 * @param valeur
	 */
	public void setVar(char valeur) {
		if (varCour == 0) {
			System.out.println("Erreur variable courante non affectée:" + varCour);
			dump();
			System.exit(1);
		}
		setVar(varCour, valeur);
	}

	/**
	 * affectation de variable et controle de cohérence
	 * @param i : numero de variable
	 * @param valeur
	 * @return true ou false
	 */
	public boolean setVar(int i, char valeur) {
		if (i <= 0 || i >= nbVar) {
			System.out.println("Erreur setVar variable inconnue:" + i + "=" + valeur);
			dump();
			System.exit(1);
		}
		if (valeur == 'S')
			return false; // SA n'est pas une couleur
		if (var[i].length() == 1)
			return true; // variable déjà affectée
		int couleur = Jeu.couleurToInt(valeur);
		// calcul des variables couleurs déduites
		switch (i) {
		case (1): // M donne M'
			if (valeur == 'P')
				var[3] = "C";
			else if (valeur == 'C')
				var[3] = "P";
			else
				return false; // valeur n'est une majeure
			break;
		case (2): // m donne m'
			if (valeur == 'K')
				var[4] = "T";
			else if (valeur == 'T')
				var[4] = "K";
			else
				return false; // valeur n'est une mineure
			break;
		case (3): // M'
			return false;
		case (4): // m'
			return false;
		case (5): // controle x en fct de y
			if (var[6].length() > 0 && Jeu.couleurToInt(var[6].charAt(0)) <= couleur)
				return false;
			break;
		case (6): // y controle en fct de x et z
			if (var[5].length() > 0 && Jeu.couleurToInt(var[5].charAt(0)) >= couleur)
				return false;
			if (var[7].length() > 0 && Jeu.couleurToInt(var[7].charAt(0)) <= couleur)
				return false;
			break;
		case (7): // z controle en fct de y
			if (var[6].length() > 0 && Jeu.couleurToInt(var[6].charAt(0)) >= couleur)
				return false;
			break;
		}
		var[i] = String.valueOf(valeur);
		return true;
	}
	
	/**
	 * tri des couleurs dans l'ordre décroissant de longueur
	 * @param listCouleurs
	 * @return
	 */
	public String sortDec(String listCouleurs) {
		if (listCouleurs == null)
			return "";
		int lg = listCouleurs.length();
		if (lg == 0)
			return "";
		char[] s = new char[lg];
		listCouleurs.getChars(0, lg, s, 0);
		for (int i = 0; i < lg - 1; i++) {
			char c = s[i];
			for (int j = i + 1; j < lg; j++) {
				if (jeu.nbCartes(s[j]) > jeu.nbCartes(c)) {
					s[i] = s[j];
					s[j] = c;
					c = s[i];
				}
			}
		}
		String result = new String(s);
		return result;
	}

}
