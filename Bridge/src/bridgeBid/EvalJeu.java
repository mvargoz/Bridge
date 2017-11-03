package bridgeBid;

/*
 * 		Evaluation d'un jeu de bridge pour les enchères
 */

public class EvalJeu {
	public int PH; // points d'honneurs
	public int PDH; // points de distribution et d'honneurs
	public int PHL; // points de longueur et d'honneurs
	public int PJ; // points de chelem
	public int LJ; // levées de jeux
	public int P; // perdantes
	public int S[]; // points de soutien dans chaque couleur
	public int Hc[]; // points d'honneurs dans chaque couleur
	public String distribution; // nombre de cartes du + au -
	public String jeu[];

	private String typeDistrib1; // type de distribution
	private String typeDistrib2; // type de distribution secondaire
	private int PD, PS, PL[];

	private static String carteSymbole = "ARDVX98765432";

	public EvalJeu(String T, String K, String C, String P) throws Exception {
		int nbCartes = T.length() + K.length() + C.length() + P.length();
		if (nbCartes != 13) {
			throw new Exception("Il n'y pas 13 cartes mais " + nbCartes);
		}
		controleJeu(T);
		controleJeu(K);
		controleJeu(C);
		controleJeu(P);
		jeu = new String[4];
		jeu[0] = new String(T);
		jeu[1] = new String(K);
		jeu[2] = new String(C);
		jeu[3] = new String(P);
		CalculJeu();
		CalculDistribution();
	}

	public static void controleJeu(String j) throws Exception {
		int noCarteCourante = -1;
		for (int i = 0; i < j.length(); i++) {
			int noCarte = carteSymbole.indexOf(j.charAt(i));
			if (noCarte < 0) {
				throw new Exception("Symbole carte non reconnu : " + j.charAt(i));
			}
			if (noCarte <= noCarteCourante) {
				throw new Exception("Cartes non classées : " + j);
			}
			noCarteCourante = noCarte;
		}
	}

	public static int noCouleur(char couleur) {
		if (couleur == 'T')
			return 0;
		else if (couleur == 'K')
			return 1;
		else if (couleur == 'C')
			return 2;
		else if (couleur == 'P')
			return 3;
		return 0;
	}

	public static char couleurToChar(int j) {
		if (j >= 0 && j < 4) {
			String s = "TKCP";
			return s.charAt(j);
		}
		return ' ';
	}

	public boolean typeDistrib(String distrib) {
		if (typeDistrib1.equals(distrib) || typeDistrib2.equals(distrib))
			return true;
		return false;
	}

	public int nbCartes(char couleur) {
		return jeu[noCouleur(couleur)].length();
	}

	public int Controles(char couleur) {
		return testControles(jeu[noCouleur(couleur)]);
	}

	public int ControlesSA(char couleur) {
		return testControlesSA(jeu[noCouleur(couleur)]);
	}

	public boolean Cartes(char couleur, String liste) {
		return testCartes(jeu[noCouleur(couleur)], liste);
	}

	public int nbHonneurs(char couleur) {
		return countH(jeu[noCouleur(couleur)]);
	}

	public int nbGrosHonneurs(char couleur) {
		return countGH(jeu[noCouleur(couleur)]);
	}

	public int nbCartesType(char carte) {
		int c = 0;
		for (int i = 0; i < 4; i++)
			for (int j = 0; j < jeu[i].length(); j++) {
				if (jeu[i].charAt(j) == carte)
					c++;
			}
		return c;
	}

	public int Soutien(char couleur) {
		return S[noCouleur(couleur)];
	}

	public int Honneur(char couleur) {
		return Hc[noCouleur(couleur)];
	}

	public void print() {
		System.out.println("----- Jeu -----");
		for (int i = 0; i < 4; i++)
			System.out.println(couleurToChar(i) + ":" + jeu[i]);
		System.out.println("PH : " + PH);
		System.out.println("PDH: " + PDH);
		System.out.println("PHL: " + PHL);
		System.out.println("PJ : " + PJ);
		System.out.println("LJ : " + LJ);
		System.out.println("P  : " + P);
		System.out.print("Ps: " + S[0]);
		System.out.print(" " + S[1]);
		System.out.print(" " + S[2]);
		System.out.println(" " + S[3]);
		System.out.print("Hc: " + Hc[0]);
		System.out.print(" " + Hc[1]);
		System.out.print(" " + Hc[2]);
		System.out.println(" " + Hc[3]);
		System.out.println("Distribution: " + distribution);
		System.out.println("---------------");
	}

	private int countH(String j) {
		int c = 0;
		for (int i = 0; i < j.length(); i++) {
			if (j.charAt(i) == 'A' || j.charAt(i) == 'R' || j.charAt(i) == 'D' || j.charAt(i) == 'V'
					|| j.charAt(i) == 'X')
				c++;
		}
		return c;
	}

	private int countGH(String j) {
		int c = 0;
		for (int i = 0; i < j.length(); i++) {
			if (j.charAt(i) == 'A' || j.charAt(i) == 'R' || j.charAt(i) == 'D')
				c++;
		}
		return c;
	}

	private int testControles(String j) {
		if (j.length() == 0)
			return 1;
		else if (j.length() == 1) {
			if (j.charAt(0) == 'A')
				return 1;
			else
				return 2;
		} else if (j.length() >= 2) {
			if (j.charAt(0) == 'A')
				return 1;
			else if (j.charAt(0) == 'R')
				return 2;
			else if (j.length() >= 3 && j.charAt(0) == 'D' && j.charAt(1) == 'V')
				return 3;
		}
		return 14;
	}

	// nombre de demi-contrôles pour SA

	private int testControlesSA(String j) {
		int ctr = 0;
		if ( j.startsWith("ARD") || j.startsWith("ADV") ||  j.startsWith("ARV"))
			ctr = 6;
		else if ( j.startsWith("ADV") ||  j.startsWith("ARV") )
			ctr = 5;
		else if ( j.startsWith("AR") ||  j.startsWith("RDV"))
			ctr = 4;
		else if ( j.startsWith("AD") ||  j.startsWith("AVX") ||
				  j.length() >= 3 && j.startsWith("RV") ||
				  j.length() >= 3 && j.startsWith("RD")  )
			ctr = 3;	// un arrêt et demi: AD, AVX, RVx, RDx
		else if ( j.startsWith("A") ||
				  j.length() >= 2 && j.startsWith("R") ||
			  	  j.length() >= 3 && j.startsWith("D") )
			ctr = 2;	// arrêt: A ou Rx ou Dxx
		else if ( j.length() >= 3 && j.startsWith("V") ||
				  j.length() >= 2 && j.startsWith("D") )
			ctr = 1;	// demi-arrêt: Vxx ou Dx
		return ctr;
	}

	private boolean testCartes(String j, String liste) {
		int il;
		if (j.length() < liste.length())
			return false;
		for (il = 0; il < liste.length(); il++) {
			if (liste.indexOf(liste.charAt(il)) < 0)
				return false;
		}
		return true;
	}

	private void CalculJeu() {
		int i, j;
		Hc = new int[4];
		for (i = 0; i < 4; i++) {
			Hc[i] = 0;
			for (j = 0; j < jeu[i].length(); j++) {
				if (jeu[i].charAt(j) == 'A') {
					Hc[i] += 4;
				} else if (jeu[i].charAt(j) == 'R') {
					Hc[i] += 3;
				} else if (jeu[i].charAt(j) == 'D') {
					Hc[i] += 2;
				} else if (jeu[i].charAt(j) == 'V') {
					Hc[i] += 1;
				}
			}
		}
		PH = Hc[0] + Hc[1] + Hc[2] + Hc[3];

		PS = 0;
		PD = 0;
		for (i = 0; i < 4; i++) {
			if (jeu[i].length() == 0) {
				PD += 3;
				PS += 5;
			} else if (jeu[i].length() == 1) {
				if (jeu[i].charAt(0) == 'R' || jeu[i].charAt(0) == 'D') {
					PS += 1;
				} else if (jeu[i].charAt(0) == 'V') {
					PD += 1;
					PS += 2;
				} else {
					PD += 2;
					PS += 3;
				}
			} else if (jeu[i].length() == 2) {
				PD += 1;
				PS += 1;
			}
		}

		PL = new int[4];
		for (i = 0; i < 4; i++) {
			PL[i] = 0;
			if (jeu[i].length() >= 5 && (jeu[i].substring(0, 2).equals("AR") || jeu[i].substring(0, 2).equals("AD")
					|| jeu[i].substring(0, 2).equals("RD")))
				PL[i] += jeu[i].length() - 4;
			else if (jeu[i].length() >= 6 && (jeu[i].substring(0, 2).equals("AV") || jeu[i].substring(0, 2).equals("RV")
					|| jeu[i].substring(0, 3).equals("DVX")))
				PL[i] += jeu[i].length() - 5;
		}

		PHL = PH + PL[0] + PL[1] + PL[2] + PL[3];
		PDH = PHL + PD;

		PJ = PDH; // à voir plus tard

		S = new int[4];
		S[0] = 0;
		if (jeu[0].length() >= 4) {
			S[0] = PH + PS + jeu[0].length() - 5 + PL[1] + PL[2] + PL[3];
			if (Hc[0] >= 2)
				S[0] += 1;
		}
		if (jeu[1].length() >= 4) {
			S[1] = PH + PS + jeu[1].length() - 5 + PL[0] + PL[2] + PL[3];
			if (Hc[1] >= 2)
				S[1] += 1;
		}
		if (jeu[2].length() >= 3) {
			S[2] = PH + PS + jeu[2].length() - 3 + PL[0] + PL[1] + PL[3];
			if (Hc[2] >= 2)
				S[2] += 1;
		}
		if (jeu[3].length() >= 3) {
			S[3] = PH + PS + jeu[3].length() - 3 + PL[0] + PL[1] + PL[2];
			if (Hc[3] >= 2)
				S[3] += 1;
		}

		double LJtotal = 0;
		for (i = 0; i < 4; i++) {
			double LJc = 0;
			if (jeu[i].length() >= 4 && jeu[i].substring(0, 4).equals("ARDV"))
				LJc = 4;
			else if (jeu[i].length() >= 3 && jeu[i].substring(0, 3).equals("ARD"))
				LJc = 3;
			else if (jeu[i].length() >= 3 && (jeu[i].substring(0, 3).equals("ARV")
					|| jeu[i].substring(0, 3).equals("ADV") || jeu[i].substring(0, 3).equals("RDV")))
				LJc = 2.5;
			else if (jeu[i].length() >= 2 && jeu[i].substring(0, 2).equals("AR"))
				LJc = 2;
			else if (jeu[i].length() >= 2
					&& (jeu[i].substring(0, 2).equals("AD") || jeu[i].substring(0, 2).equals("RD")))
				LJc = 1.5;
			else if (jeu[i].length() >= 2
					&& (jeu[i].substring(0, 2).equals("AV") || jeu[i].substring(0, 2).equals("RV")))
				LJc = 1.25;
			else if (jeu[i].length() >= 1 && jeu[i].substring(0, 1).equals("A"))
				LJc = 1;
			else if (jeu[i].length() >= 2 && jeu[i].substring(0, 1).equals("R"))
				LJc = 0.5;

			if (LJc > 1 && jeu[i].length() >= 5)
				LJc += jeu[i].length() - 3;
			LJc = Math.min(LJc, jeu[i].length());
			LJtotal += LJc;
		}

		LJ = (int) Math.floor(LJtotal);
		P = 13 - (int) Math.round(LJtotal);
	}

	private void CalculDistribution() {
		int i, j, k;
		int lg[] = new int[4];
		for (i = 0; i < 4; i++)
			lg[i] = jeu[i].length();
		for (i = 0; i < 3; i++) {
			for (j = i + 1; j < 4; j++) {
				if (lg[j] > lg[i]) {
					k = lg[i];
					lg[i] = lg[j];
					lg[j] = k;
				}
			}
		}
		distribution = String.valueOf(lg[0]) + String.valueOf(lg[1]) + String.valueOf(lg[2]) + String.valueOf(lg[3]);
		typeDistrib2 = "";
		if (distribution.equals("4333")) {
			typeDistrib1 = "pl";
			typeDistrib2 = "reg";
		} else if (distribution.equals("4432"))
			typeDistrib1 = "reg";
		else if (distribution.equals("5332")) {
			typeDistrib1 = "reg";
			typeDistrib2 = "uni";
		} else if (distribution.equals("5422")) {
			typeDistrib1 = "sreg";
			typeDistrib2 = "bic";
		} else if (distribution.equals("6322")) {
			typeDistrib1 = "sreg";
			typeDistrib2 = "uni";
		} else if (distribution.equals("4441") || distribution.equals("5440"))
			typeDistrib1 = "tri";
		else if (distribution.substring(0, 2).equals("55") || distribution.substring(0, 2).equals("66")
				|| distribution.substring(0, 2).equals("65") || distribution.substring(0, 2).equals("75")
				|| distribution.substring(0, 2).equals("76"))
			typeDistrib1 = "bic";
		else
			typeDistrib1 = "uni";

	}

}
