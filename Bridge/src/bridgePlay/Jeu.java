package bridgePlay;

import java.util.ArrayList;

/**
 * Jeu de cartes de bridge
 */

public class Jeu implements Cloneable {

	// constantes du jeu de bridge
	
	public static String joueurs = "NESO";
	public static String[] nomJoueurs = {"Nord", "Est", "Sud", "Ouest"};
	public static String couleurs = "TKCPS";
	public static String hauteurs = "23456789XVDRA";

	/**
	 * nombre de couleurs
	 */
	public static int nbCouleur = 4;
	/**
	 * nombre de cartes par couleur
	 */
	public static int nbCartesCouleur = 13;
	/**
	 * nombre de joueurs
	 */
	public static int nbJoueur = 4;
	/**
	 * nombre de plis
	 */
	public static int nbPli = 13;
	/**
	 * valeur couleur représentant SA
	 */
	public static int SansAtout = 4;
	/**
	 * valeur des cartes
	 */
	public static int As = 12;
	public static int Roi = 11;
	public static int Dame = 10;
	public static int Valet = 9;
	public static int Dix = 8;
	public static int Neuf = 7;
	public static int Huit = 6;
	public static int Sept = 5;
	public static int Six = 4;
	public static int Cinq = 3;
	public static int Quatre = 2;
	public static int Trois = 1;
	public static int Deux = 0;

	// données du jeu

	/**
	 * représentation du jeu
	 */
	private String[] cartes;
	/**
	 * atout TKCP sous forme 0 à 3
	 */
	private int atout;

	/**
	 * constructeur
	 */

	public Jeu(String[] cartes, int atout) {
		this.atout = atout;
		this.cartes = cartes.clone();
	}

	/**
	 * clonage
	 */

	public Jeu clone() {
		Jeu c = null;
		try {
			c = (Jeu) super.clone();
			c.cartes = cartes.clone();
		} catch (Exception e) {
			return null;
		}

		return c;
	}

	/**
	 * @return jeu sous forme String[couleur]
	 */
	public String[] getJeu() {
		return cartes;
	}

	/**
	 * @return nombre de cartes du jeu
	 */
	public int nbCartes() {
		return cartes[0].length() + cartes[1].length() + cartes[2].length() + cartes[3].length();
	}

	/**
	 * @param couleur
	 * @return nombre de cartes de cette couleur
	 */
	public int nbCartes(int couleur) {
		return cartes[couleur].length();
	}

	/**
	 * @return true si le jeu est vide
	 */
	public boolean isEmpty() {
		return (nbCartes() == 0);
	}

	/**
	 * @param couleur
	 * @return la liste des cartes d'une couleur
	 */
	public ArrayList<Integer> getCartes(int couleur) {
		ArrayList<Integer> lc = new ArrayList<Integer>();
		for (int i = 0; i < cartes[couleur].length(); i++)
			lc.add(Jeu.hauteurToInt(cartes[couleur].charAt(i)));
		return lc;
	}

	/**
	 * @param couleur
	 * @param n
	 * @return nième carte de cette couleur en commençant par 1,
	 * 			-1 si n'existe pas
	 */
	public int carte(int couleur, int n) {
		if (n > 0 && n <= nbCartes(couleur))
			return Jeu.hauteurToInt(cartes[couleur].charAt(n - 1));
		else
			return -1;
	}

	/**
	 * @param couleur
	 * @param no
	 * @return n ième carte sous forme caractère de cette couleur en commençant
	 *         par 1, null si n'existe pas
	 */

	public String carteToString(int couleur, int n) {
		if (n > 0 && n <= nbCartes(couleur))
			return "" + cartes[couleur].charAt(n - 1) + Jeu.couleurToChar(couleur);
		else
			return null;
	}

	/**
	 * @param couleur
	 * @return carte en pair-impair
	 */
	public String pairImpair(int couleur) {
		int n = nbCartes(couleur);
		int c;
		if (n % 2 == 1) // impair = la plus petite
			c = n - 1;
		else if (n == 2) // 2 cartes la plus forte < D
		{
			if (carte(couleur, 1) >= Dame)
				c = 2;
			else
				c = 1;
		} else
			// pair > 2, à partir de la seconde meilleure <= 9
			for (c = 2; c <= n && carte(couleur, c) > Neuf; c++)
				;

		return carteToString(couleur, c);
	}

	/**
	 * @param couleur
	 * @return appel préférentiel
	 */
	public String appelPref(int couleur) {
		int[] c = { -1, -1, -1, -1 };
		for (int cl = 0, i = 0; cl < nbCouleur; cl++) {
			if (cl != atout || cl != couleur)
				c[i++] = cl;
		}
		c = triCouleurForte(c);
		if (c[0] > c[1])
			return grosseCarte(couleur);
		else
			return niemeCarte(couleur, 99);
	}

	/**
	 * @param couleur
	 * @param n
	 * @return joue la nième carte de la couleur ou la plus petite
	 */
	public String niemeCarte(int couleur, int n) {
		int nc = nbCartes(couleur);
		if (n > nc)
			n = nc;
		return carteToString(couleur, n - 1);
	}

	/**
	 * @param couleur
	 * @return la plus grosse carte de la couleur <= 9 de préférence
	 */
	public String grosseCarte(int couleur) {
		int c;
		for (c = 1; c <= nbCartes(couleur) && carte(couleur, 0) > '9'; c++)
			;
		return carteToString(couleur, c);
	}

	/**
	 * @param couleur
	 * @return la plus basse carte équivalente des plus fortes de la couleur
	 */
	public String minMaxCarte(int couleur) {
		int c;
		for (c = 1; c < nbCartes(couleur) && carte(couleur, c) == carte(couleur, c + 1) + 1; c++)
			;
		return carteToString(couleur, c);
	}

	/**
	 * @param couleur
	 * @param hauteurRef
	 * @return la plus basse carte juste supérieure à une carte donnée, null si
	 *         impossible
	 */
	public String minMaxCarte(int couleur, int hauteurRef) {
		int c;
		for (c = nbCartes(couleur); c > 0 && carte(couleur, c) < hauteurRef; c--)
			;
		if (carte(couleur, c) > hauteurRef)
			return carteToString(couleur, c);
		else
			return null;
	}

	/**
	 * tri des couleurs par longueur décroissante et par force croissante
	 * 
	 * @param c
	 * @return tableau trié
	 */
	public int[] triCouleurLg(int[] c) {
		for (int i = 0; i < 3 && c[i + 1] >= 0; i++)
			for (int j = i; j < 4 && c[j] >= 0; j++) {
				if (nbCartes(c[j]) > nbCartes(c[i])
						|| nbCartes(c[j]) == nbCartes(c[i]) && valeur(c[j]) < valeur(c[j])) {
					int k = c[i];
					c[i] = c[j];
					c[j] = k;
				}
			}
		return c;
	}

	/**
	 * tri des couleurs par force décroissante et par longueur croissante
	 * 
	 * @param c
	 * @return tableau trié
	 */
	public int[] triCouleurForte(int[] c) {
		for (int i = 0; i < 3 && c[i + 1] >= 0; i++)
			for (int j = i; j < 4 && c[j] >= 0; j++) {
				if (valeur(c[j]) > valeur(c[i]) || valeur(c[j]) == valeur(c[i]) && nbCartes(c[j]) < nbCartes(c[i])) {
					int k = c[i];
					c[i] = c[j];
					c[j] = k;
				}
			}
		return c;
	}

	// valeur des cartes d'une couleur
	// As=4, R=3, D=2, V=1, et pour départager: X=1/4, 9=1/8, 8=1/12, 7=1/16

	/**
	 * Valeur: As=4, R=3, D=2, V=1, et pour départager: X=1/4, 9=1/8, 8=1/12,
	 * 7=1/16
	 * 
	 * @param couleur
	 * @return valeur des cartes de la couleur
	 */
	public double valeur(int couleur) {
		double val = 0;
		for (int i = 1; i <= nbCartes(couleur); i++) {
			int v = carte(couleur, i) - 8;
			if (v > 0)
				val += v;
			else
				val += 1 / ((1 - v) * 4);
		}
		return val;
	}

	/**
	 * Comparaison avec un pattern
	 * 
	 * @param couleur
	 * @param pattern
	 *            V- : pas d'honneurs >= valet
	 * @param pattern
	 *            X- : pas d'honneurs >= 10
	 * @param pattern
	 *            ARDVX98765432, x= carte du 2 au 9, H = honneur
	 * @return true or false
	 */
	public boolean looklike(int couleur, String pattern) {
		if (pattern == null || pattern.length() == 0)
			return true;
		if (nbCartes(couleur) == 0)
			return false;
		if (pattern.equals("V-")) {
			if (carte(couleur, 1) < Valet)
				return true;
			else
				return false;
		}
		if (pattern.equals("X-")) {
			if (carte(couleur, 1) < Dix)
				return true;
			else
				return false;
		}
		for (int i = 0; i < pattern.length(); i++) {
			int rangCarte = Jeu.hauteurToInt(pattern.charAt(i));
			if (i >= nbCartes(couleur) || (rangCarte > -1 && rangCarte != carte(couleur, i + 1)))
				return false;
			if (rangCarte > -1 && rangCarte != carte(couleur, i + 1))
				return false;
			if (pattern.charAt(i) == 'H' && carte(couleur, i + 1) < Dix)
				return false;
			if (pattern.charAt(i) == 'x' && carte(couleur, i + 1) > Neuf)
				return false;
		}
		return true;
	}

	/**
	 * @param couleur
	 * @param carte
	 * @return nombre de cartes supérieures à cette carte
	 */
	public int nbCartesSup(int couleur, char carte) {
		return nbCartesSup(couleur, Jeu.hauteurToInt(carte));
	}

	/**
	 * @param couleur
	 * @param carte
	 * @return nombre de cartes supérieures à cette carte
	 */
	public int nbCartesSup(int couleur, int rangCarte) {
		int nb;
		for (nb = 1; nb <= nbCartes(couleur) && rangCarte < carte(couleur, nb); nb++)
			;
		return nb;
	}

	// existence d'une carte donnée dans une couleur

	/**
	 * Teste l'existence d'une carte dans une couleur
	 * 
	 * @param couleur
	 * @param carte
	 * @return true ou false
	 */
	public boolean cartePresente(int couleur, char carte) {
		int i;
		int rangCarte = Jeu.hauteurToInt(carte);
		for (i = 1; i <= nbCartes(couleur) && rangCarte < carte(couleur, i); i++)
			;
		if (i < nbCartes(couleur) && rangCarte == carte(couleur, i))
			return true;
		else
			return false;
	}

	// classées par ordre de longueur et de force décroissantes
	// invTri inverse cet ordre

	/**
	 * Recherche de l'ensemble des couleurs classées par ordre de longueur et de
	 * force décroissantes répondant aux critères suivants:
	 * 
	 * @param lgMin
	 *            <= longueur
	 * @return tableau couleur sous forme 0 à 3
	 */
	public int[] rechCoul(int lgMin) {
		return rechCoul(lgMin, lgMin, null, false);
	}

	/**
	 * Recherche de l'ensemble des couleurs classées par ordre de longueur et de
	 * force décroissantes répondant aux critères suivants:
	 * 
	 * @param lgMin
	 *            <= longueur
	 * @param lgMax
	 *            >= longueur
	 * @return tableau couleur sous forme 0 à 3
	 */
	public int[] rechCoul(int lgMin, int lgMax) {
		return rechCoul(lgMin, lgMax, null, false);
	}

	/**
	 * Recherche de l'ensemble des couleurs classées par ordre de longueur et de
	 * force décroissantes répondant aux critères suivants:
	 * 
	 * @param lgMin
	 *            <= longueur
	 * @param lgMax
	 *            >= longueur
	 * @param pattern
	 *            V- : pas d'honneurs >= valet
	 * @param pattern
	 *            X- : pas d'honneurs >= 10
	 * @param pattern
	 *            ARDVX98765432, x= carte du 2 au 9, H = honneur
	 * @return tableau couleur sous forme 0 à 3
	 */
	public int[] rechCoul(int lgMin, int lgMax, String pattern) {
		return rechCoul(lgMin, lgMax, pattern, false);
	}

	/**
	 * Recherche de l'ensemble des couleurs classées par ordre de longueur et de
	 * force décroissantes répondant aux critères suivants:
	 * 
	 * @param lgMin
	 *            <= longueur
	 * @param lgMax
	 *            >= longueur
	 * @param pattern
	 *            V- : pas d'honneurs >= valet
	 * @param pattern
	 *            X- : pas d'honneurs >= 10
	 * @param pattern
	 *            ARDVX98765432, x= carte du 2 au 9, H = honneur
	 * @param invTri
	 *            inverse l'ordre de tru
	 * @return tableau couleur sous forme 0 à 3
	 */
	public int[] rechCoul(int lgMin, int lgMax, String pattern, boolean invTri) {
		int[] res = new int[nbCouleur];
		for (int i = 0; i < nbCouleur; i++)
			res[i] = -1;
		int nb = 0;

		// sélection

		for (int i = 0; i < nbCouleur; i++) {
			if (nbCartes(i) >= lgMin && nbCartes(i) <= lgMax && looklike(i, pattern))
				res[nb++] = i;
		}
		if (nb == 0)
			return null;

		// tri

		for (int i = 0; i < nb - 1; i++)
			for (int j = i; j < nb; j++) {
				if (!invTri
						&& (nbCartes(res[j]) > nbCartes(res[i])
								|| nbCartes(res[j]) == nbCartes(res[i]) && valeur(res[j]) > valeur(res[i]))
						|| invTri && (valeur(res[j]) > valeur(res[i])
								|| valeur(res[j]) == valeur(res[i]) && nbCartes(res[j]) > nbCartes(res[i]))) {
					int k = res[i];
					res[i] = res[j];
					res[j] = k;
				}
			}

		return res;
	}

	/**
	 * Ote une carte au jeu
	 * 
	 * @param couleur
	 * @param carte
	 */
	public void oteCarte(int couleur, char carte) {
		cartes[couleur] = oteCarte(cartes[couleur], carte);
	}

	/**
	 * Ote une carte au jeu
	 * 
	 * @param couleur
	 * @param rangCarte
	 */
	public void oteCarte(int couleur, int rangCarte) {
		oteCarte(couleur, hauteurToChar(rangCarte));
	}

	/**
	 * Ajoute une carte au jeu
	 * 
	 * @param couleur
	 * @param carte
	 */
	public void ajouteCarte(int couleur, char carte) {
		cartes[couleur] = ajouteCarte(cartes[couleur], carte);
	}

	/**
	 * Ajoute une carte au jeu
	 * 
	 * @param couleur
	 * @param rangCarte
	 */
	public void ajouteCarte(int couleur, int rangCarte) {
		ajouteCarte(couleur, hauteurToChar(rangCarte));
	}

	/************************************
	 * 
	 *		 STATIC FUNCTIONS
	 *
	 ************************************/

	/**
	 * @return nombre de cartes du jeu
	 */
	public static int nbCartes(String[] cartes) {
		return cartes[0].length() + cartes[1].length() + cartes[2].length() + cartes[3].length();
	}

	/**
	 * @param couleur
	 * @return nombre de cartes de cette couleur
	 */
	public static int nbCartes(String[] cartes, int couleur) {
		return cartes[couleur].length();
	}

	/**
	 * @return true si le jeu est vide
	 */
	public static boolean isEmpty(String[] cartes) {
		return (nbCartes(cartes) == 0);
	}
	
	/**
	 * Ote une carte à un jeu
	 * 
	 * @param jeu
	 * @param couleur
	 * @param carte
	 * @return jeu
	 */
	public static String oteCarte(String jeu, char carte) {
		int j = jeu.indexOf(carte);
		return substring(jeu, 0, j) + substring(jeu, j + 1);
	}
	
	/**
	 * Ajoute une carte à un jeu
	 * 
	 * @param jeu
	 * @param couleur
	 * @param carte
	 * @return jeu
	 */
	public static String ajouteCarte(String jeu, char carte) {
		int j = 0;
		for (; j < jeu.length() && hauteurToInt(carte) < hauteurToInt(jeu.charAt(j)); j++);
		return substring(jeu, 0, j) + carte + substring(jeu, j);
	}
	
	/**
	 * conversion couleur caractère TKCPS en valeur
	 * @param couleurChar
	 * @return 0 à 3
	 */
	public static int couleurToInt(char couleurChar) {
		return couleurs.indexOf(couleurChar);
	}

	/**
	 * conversion couleur valeur en caractère TKCPS
	 * @param couleurInt
	 * @return TKCPS
	 */
	public static char couleurToChar(int couleurInt) {
		return couleurs.charAt(couleurInt);
	}

	/**
	 * conversion hauteur caractère 23456789XVDRA en valeur
	 * @param jauteurChar
	 * @return 0 à 12
	 */
	public static int hauteurToInt(char jauteurChar) {
		return hauteurs.indexOf(jauteurChar);
	}

	/**
	 * conversion hauteur valeur en caractère 23456789XVDRA 
	 * @param hauteurInt
	 * @return 23456789XVDRA
	 */
	public static char hauteurToChar(int hauteurInt) {
		return hauteurs.charAt(carteRang(hauteurInt));
	}

	/**
	 * conversion valeur carte 0 à 53 en caracère hauteur+couleur
	 * @param i
	 * @return hauteur et couleur
	 */
	public static String carteToString(int i) {
		return "" + hauteurToChar(i % 13) + couleurToChar(i / 13);
	}

	/**
	 * conversion carte caracère hauteur+couleur en valeur 0 à 53 
	 * @param s
	 * @return 0 à 53
	 */
	public static int carteToInt(String s) {
		return hauteurToInt(s.charAt(0)) + couleurToInt(s.charAt(1)) * 13;
	}

	/**
	 * couleur d'une valeur carte 0 à 53
	 * @param i
	 * @return code couleur 0 à 3 pour TKCP
	 */
	public static int carteCouleur(int i) {
		return i / 13;
	}

	/**
	 * hauteur d'une valeur carte 0 à 53
	 * @param i
	 * @return rang base 0 pour ordre: 2,3,...,D,R,As
	 */
	public static int carteRang(int i) {
		int rang = i % 13;
		return rang;
	}
	
	/**
	 * substring protégé
	 * @param string
	 * @param debut
	 * @return
	 */
	public static String substring(String string, int debut) {
		return substring(string, debut, string.length());
	}

	/**
	 * substring protégé
	 * @param string
	 * @param debut
	 * @param fin
	 * @return
	 */
	public static String substring(String string, int debut, int fin) {
		if (debut >= fin)
			return "";
		if (debut < 0 && fin > string.length())
			return string;
		else if (debut < 0)
			return string.substring(0, fin);
		else
			return string.substring(debut, fin);
	}

	/**
	 * libellé joueur en fonction du code NESW
	 * @param s
	 * @return Nord, Est, Sud, Ouest
	 */
	public static String libJoueur(String s) {
		return nomJoueurs[joueurs.indexOf(s.charAt(0))];
	}
	
	/**
	 * libellé joueur en fonction du code NESW
	 * @param c
	 * @return
	 */
	public static String libJoueur(char c) {
		return nomJoueurs[joueurs.indexOf(c)];
	}
	
	/**
	 * numéro du joueur en fonction du libellé ou du caractère NESO
	 * @param libJoueur
	 * @return numéro joueur 0 à 3
	 */
	public static int joueur(String libJoueur)  {
		return joueurs.indexOf(libJoueur.charAt(0));
	}

}
