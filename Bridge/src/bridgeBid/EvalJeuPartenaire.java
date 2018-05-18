package bridgeBid;

/**
 * Résultat de l'évaluation d'un jeu en fonction des enchères
 */

class EvalJeuPartenaire {
	public int PH, PHM; // points d'honneurs
	public int PHL, PHLM; // points d'honneurs et de longueur
	public int PDH, PDHM; // points de distribution et d'honneurs
	public int LJ, LJM; // levées de jeux
	public int nbCartesMin[]; // nombre de cartes min par couleur
	public int nbCartesMax[]; // nombre de cartes max par couleur
	public char atout; // couleur d'atout: TKCPS

	/**
	 * évaluation du jeu du partenaire
	 */
	public EvalJeuPartenaire() {
		PH = 0;
		PHM = 40;
		PHL = 0;
		PHLM = 40;
		PDH = 0;
		PDHM = 40;
		LJ = 0;
		LJM = 13;
		nbCartesMin = new int[4];
		nbCartesMax = new int[4];
		for (int i = 0; i < 4; i++) {
			nbCartesMin[i] = 0;
			nbCartesMax[i] = 13;
		}
	}

	/**
	 * édition de contrôle
	 */
	public void print() {
		System.out.println("Jeu type :");
		System.out.println("Atout :" + atout);
		System.out.println("PH : " + PH + "-" + PHM);
		System.out.println("PHL: " + PHL + "-" + PHLM);
		System.out.println("PDH: " + PDH + "-" + PDHM);
		System.out.println("LJ : " + LJ + "-" + LJM);
		System.out.println("nb cartes T: " + nbCartesMin[0] + "-" + nbCartesMax[0]);
		System.out.println("nb cartes K: " + nbCartesMin[1] + "-" + nbCartesMax[1]);
		System.out.println("nb cartes C: " + nbCartesMin[2] + "-" + nbCartesMax[2]);
		System.out.println("nb cartes P: " + nbCartesMin[3] + "-" + nbCartesMax[3]);
	}

}