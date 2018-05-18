package bridgeBid;

import bridgePlay.Jeu;

/**
 * Gestion des ench�res de bridge
 */

public class DonneBid {
	private String Donneur; // N E S O
	private int noDonneur;
	private String Vulnerabilite; // NS EO P T
	private String SystemNS;
	private String SystemEO;

	private String TypeTournoi;
	private EvalJeu Jeux[];
	private String Encheres[];
	private int nbEnch;
	private int nbEnchSave;

	private int Tour;
	private int Joueur;

	/**
	 * constructeur
	 * @param pDonneur
	 * @param pVuln
	 * @param pSysNS
	 * @param pSysEO
	 * @param pTypeTournoi
	 */
	public DonneBid(String pDonneur, String pVuln, String pSysNS, String pSysEO, String pTypeTournoi) {
		Donneur = pDonneur;
		Vulnerabilite = pVuln;
		SystemNS = pSysNS;
		SystemEO = pSysEO;
		TypeTournoi = pTypeTournoi;
		Jeux = new EvalJeu[4];
		initEnchere();
	}

	/**
	 * initialisation des ench�res
	 */
	public void initEnchere() {
		Encheres = new String[50];
		nbEnch = 0;
		nbEnchSave = 0;
		Tour = 1;
		noDonneur = Joueur = joueurToInt(Donneur);
	}

	/**
	 * @param joueur NESO
	 * @return num�ro du joueur
	 */
	public static int joueurToInt(String joueur) {
		if (joueur.equalsIgnoreCase("N"))
			return 0;
		else if (joueur.equalsIgnoreCase("E"))
			return 1;
		else if (joueur.equalsIgnoreCase("S"))
			return 2;
		else if (joueur.equalsIgnoreCase("O") || joueur.equalsIgnoreCase("W"))
			return 3;
		return -1;
	}

	/**
	 * @param joueur
	 * @return joueur NESO
	 */
	public static char joueurToChar(int joueur) {
		if (joueur >= 0 && joueur < 4) {
			String s = "NESO";
			return s.charAt(joueur);
		}
		return ' ';
	}

	/**
	 * passage au joueur suivant
	 */
	public void JoueurSuivant() {
		Joueur++;
		if (Joueur > 3) {
			Tour++;
			Joueur = 0;
		}
	}

	/**
	 * @return joueur courant
	 */
	public int getJoueur() {
		return Joueur;
	}

	/**
	 * syst�me d'ench�re
	 * @return
	 */
	public String getSystem() {
		if (Joueur == 0 || Joueur == 2)
			return SystemNS;
		else
			return SystemEO;
	}

	/**
	 * test syst�me d'ench�re
	 * @param s
	 * @return
	 */
	public boolean testSystem(String s) {
		int i = 0;
		if (Joueur == 0 || Joueur == 2)
			i = SystemNS.indexOf(s);
		else
			i = SystemEO.indexOf(s);
		return i >= 0;
	}

	/**
	 * @return vuln�rabilit�
	 */
	public String getVulnerabilite() {
		if (Vulnerabilite.equals("P"))
			return ("NN");
		else if (Vulnerabilite.equals("T"))
			return ("VV");
		else if (Vulnerabilite.equals("NS") && (Joueur == 0 || Joueur == 2)
				|| Vulnerabilite.equals("EO") && (Joueur == 1 || Joueur == 3))
			return ("VN");
		else
			return ("NV");
	}

	/**
	 * donneur
	 * @return num�ro donneur
	 */
	public String getDonneur() {
		return Donneur;
	}

	/**
	 * type joueur
	 * @return type joueur: 1=d�clarant, 3=r�pondant, 2&4=flanc
	 */
	public int getTypeJoueur()  {
		int indJoueur = 0;
		for (int i=0; i < getnbEnch(); i++)  {
			if ( getEnchere(i).equals("-") )  {
				if ( indJoueur != 0 )  {
					indJoueur++;				
				}			
			} else {
				if ( indJoueur == 0 )  {
					indJoueur = 1;
				} else {
					indJoueur++;									
				}
			}
			if ( indJoueur > 4 )
				indJoueur = 1;
		}
		indJoueur++;
		if ( indJoueur > 4 )
			indJoueur = 1;
		return indJoueur;
	}
	
	/**
	 * @return type tournoi
	 */
	public String getTypeTournoi() {
		return TypeTournoi;
	}

	/**
	 * @param i
	 * @return ench�re i
	 */
	public String getEnchere(int i) {
		if (i < nbEnch)
			return Encheres[i];
		return "";
	}

	/**
	 * @return nombre d'ench�res
	 */
	public int getnbEnch() {
		return nbEnch;
	}

	/**
	 * retour � l'ench�re pr�c�dente du partenaire avec sauvegarde
	 * @return ench�re pr�c�dente du partenaire
	 */
	public String prevEnch() {
		if (nbEnch >= 2) {
			if (nbEnchSave == 0)
				nbEnchSave = nbEnch;
			nbEnch -= 2;
			return Encheres[nbEnch];
		}
		return "";
	}

	/**
	 * remet ench�re sauvegard�e
	 */
	public void resetEnch() {
		if (nbEnchSave > 0)
			nbEnch = nbEnchSave;
		nbEnchSave = 0;
	}

	/**
	 * met une ench�re
	 * @param enchere
	 */
	public void putEnchere(String enchere) {
		Encheres[nbEnch++] = enchere;
	}

	/**
	 * @return jeux
	 */
	public EvalJeu getJeu() {
		return Jeux[Joueur];
	}

	/**
	 * @param i
	 * @return
	 */
	public EvalJeu getJeu(int i) {
		if (i >= 0 || i <= 3)
			return Jeux[i];
		return null;
	}

	/**
	 * constructeur de jeu
	 * @param i joueur
	 * @param T
	 * @param K
	 * @param C
	 * @param P
	 * @throws Exception
	 */
	public void putJeu(int i, String T, String K, String C, String P) throws Exception {
		if (i >= 0 || i <= 3)
			Jeux[i] = new EvalJeu(T, K, C, P);
	}

	/**
	 * test de validit� d'une ench�re
	 * @param enchere
	 * @return
	 */
	public boolean testEnchere(String enchere) {
		int lastNoEnch = lastNoEnchere();
		if (lastNoEnch < 0) {
			if (enchere.equals("X") || enchere.equals("XX"))
				return false;
			else
				return true;
		}
		if (enchere.equals("-")) {
			if (nbEnch - lastNoEnch > 3)
				return false;
			else
				return true;
		}
		if (enchere.equals("X")) {
			if (Encheres[lastNoEnch].equals("X") || Encheres[lastNoEnch].equals("XX") || (nbEnch - lastNoEnch) % 2 == 0)
				return false;
			else
				return true;
		}
		if (enchere.equals("XX")) {
			if (!Encheres[lastNoEnch].equals("X") || (nbEnch - lastNoEnch) % 2 == 0)
				return false;
			else
				return true;
		}
		String lastEnchere = lastAnnonce();
		int lastNiveau = Character.digit(lastEnchere.charAt(0), 10) - 1;
		int lastCouleur = Jeu.couleurToInt(lastEnchere.charAt(1));
		int niveau = Character.digit(enchere.charAt(0), 10) - 1;
		int couleur = Jeu.couleurToInt(enchere.charAt(1));
		if (niveau > lastNiveau || niveau == lastNiveau && couleur > lastCouleur)
			return true;
		else
			return false;
	}

	/**
	 * n� de la derni�re ench�re
	 * @return dernier n� ench�re
	 */
	public int lastNoEnchere() {
		int i;
		for (i = nbEnch - 1; i >= 0 && Encheres[i].equals("-"); i--)
			/* null body */;
		if (i >= 0)
			return i;
		else
			return -1;
	}

	/**
	 * retourne la derni�re ench�re
	 * @return derni�re ench�re
	 */
	public String lastAnnonce() {
		return lastAnnonce(nbEnch);
	}

	/**
	 * @param i
	 * @return derni�re ench�re
	 */
	public String lastAnnonce(int i) {
		i--;
		while (i >= 0 && !enchereCouleur(i))
			i--;
		if (i >= 0)
			return Encheres[i];
		else
			return null;
	}

	/**
	 * @return n� du joueur jouant le contrat
	 */
	public int joueurContrat() {
		int i = nbEnch - 1;
		while (i >= 0 && !enchereCouleur(i))
			i--;
		if (i >= 0) {
			char couleurContrat = Encheres[i].charAt(1);
			int ench = i;
			i -= 2;
			while (i >= 0) {
				if (enchereCouleur(i) && couleurContrat == Encheres[i].charAt(1))
					ench = i;
				i -= 2;
			}
			return (noDonneur + ench) % 4;
		}
		return -1;
	}

	/**
	 * @return derni�re couleur annonc�es
	 */
	public char lastCouleurAnnonce() {
		return lastCouleurAnnonce(nbEnch);
	}

	/**
	 * @param i
	 * @return derni�re couleur annonc�es
	 */
	public char lastCouleurAnnonce(int i) {
		String ench = lastAnnonce(i);
		if (ench == null)
			return ' ';
		else
			return ench.charAt(1);
	}

	/**
	 * @return derni�re hauteur annonc�es
	 */
	public int lastHauteurAnnonce() {
		return lastHauteurAnnonce(nbEnch);
	}

	/**
	 * @param i
	 * @return derni�re hauteur annonc�es
	 */
	public int lastHauteurAnnonce(int i) {
		String ench = lastAnnonce();
		if (ench == null)
			return 0;
		else
			return Character.digit(ench.charAt(0), 10);
	}


	/**
	 * v�rifie que cette ench�re n'est ni un passe, ni un contre ou un surcontre
	 * @param i
	 * @return true si passe, contre ou surcontre
	 */
	public boolean enchereCouleur(int i) {
		if (i < nbEnch && !Encheres[i].equals("X") && !Encheres[i].equals("XX") && !Encheres[i].equals("-"))
			return true;
		return false;
	}

	/**
	 * @return couleurs annonc�es
	 */
	public String CouleursAnnoncees() {
		String c = "";
		char cl;
		int i;

		for (i = 0; i < nbEnch; i++) {
			if ((cl = CouleurEnchere(i)) != ' ') {
				if (c.indexOf(cl) < 0)
					c = c + cl;
			}
		}
		return c;
	}

	/**
	 * @return couleurs non annonc�es
	 */
	public String CouleursNonAnnoncees() {
		char c[] = { 'T', 'K', 'C', 'P' };
		boolean bca[] = { true, true, true, true };
		String ca = CouleursAnnoncees();
		int i, j;

		for (i = 0; i < ca.length(); i++)
			for (j = 0; j < 4; j++) {
				if (ca.charAt(i) == c[j])
					bca[j] = false;
			}
		String cr = "";
		for (j = 0; j < 4; j++) {
			if (bca[j])
				cr = cr + c[j];
		}

		return cr;
	}

	/**
	 * @return couleurs soutenues
	 */
	public String CouleursSoutenues() {
		int i;
		String ca = ""; // couleurs annonc�es
		String cs = ""; // couleurs soutenues
		char cl;

		for (i = nbEnch - 2; i >= 0; i -= 2) {
			if ((cl = CouleurEnchere(i)) != ' ') {
				ca = ca + cl;
				if (cs.indexOf(cl) >= 0)
					cs = cs + cl;
			}
		}
		return cs;
	}

	/**
	 * @param i n� ench�re
	 * @return couleur ench�re
	 */
	private char CouleurEnchere(int i) {
		if (!Encheres[i].equals("X") && !Encheres[i].equals("XX") && !Encheres[i].equals("-"))
			return Encheres[i].charAt(1);
		else
			return ' ';
	}

	/**
	 * @param joueur
	 * @return couleur annonc�e par le partenaire
	 */
	public int couleurPartenaire(int joueur) {
		int partenaire = (joueur + 2) % 4;
		for (int i = noDonneur, j = 0; j < nbEnch; i++, j++) {
			if (i == partenaire) {
				int c = Jeu.couleurToInt(CouleurEnchere(j));
				if (c >= 0 && c <= 3)
					return c;
			}
		}
		return -1;
	}

	/**
	 * impression
	 */
	public void print() {
		for (int i = 0; i < 4; i++) {
			System.out.println("Joueur: " + joueurToChar(i));
			getJeu(i).print();
			System.out.println("---");
		}
	}
}
