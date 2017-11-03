package bridgePlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import bridgeBid.DonneBid;
import bridgePlay.DonnePlay.CardPlay;

/**
 * Bridge : simulation du jeu de la carte
 */

public class Simulator {

	/**
	 * 	simulation tree
	 */
	public class TreeNodeSimulation {
			// �tat de la donne simul�e au d�but du pli
		public DonnePlay donnePlay;
		 	// meilleurs cartes jouables et r�sultats
		public HashMap<CardPlay,TreeNodeSimulation> links;
	}

	// constantes

	private static boolean trace = true;

	// jeu

	private DonneBid donneBid;
	private DonnePlay donnePlay;
	private Jeu jeu, jeuDeclarant;
	
	//	simulation
	
	private boolean modeProbleme;
	 	// simulation de la prochaine carte jou�e
	private TreeNodeSimulation simulRoot = null;
	private TreeNodeSimulation nextSimulRoot = null;
	private int profondeur;
	private double tolerance = 0.5; 

	/**
	 * Constructeur � partir des ench�res
	 * 
	 * @param donne
	 * @param profondeur
	 */
	public Simulator(DonneBid donne, int profondeur) {
		this.donneBid = donne;
		this.donnePlay = new DonnePlay(donne);
		this.profondeur = profondeur;
		if ( profondeur > 0 )
			this.modeProbleme = true;
	}

	/**
	 * Constructeur � partir des jeux
	 * 
	 * @param donne
	 * @param profondeur
	 */
	public Simulator(DonnePlay donne, int profondeur) {
		this.donnePlay = donne;
		this.profondeur = profondeur;
		if ( profondeur > 0 )
			this.modeProbleme = true;
	}

	/**
	 * Enregistre une carte jou�e
	 * 
	 * @param carte
	 */
	public void putCarte(String carte) {
		int hauteurJoue = Jeu.hauteurToInt(carte.charAt(0));
		int couleurJoue = Jeu.couleurToInt(carte.charAt(1));
		donnePlay.joueCarte(couleurJoue, hauteurJoue);
	}

	/**
	 * @return derni�re carte � jouer
	 */
	public String getCarteSeule() {
		return donnePlay.getCarteSeule();
	}

	/**
	 * @return la carte � jouer
	 */
	public String getCarte() {

		String carteAJouer = null;

		if (modeProbleme) {
			// recherche du meilleur jeu en mode probl�me � 4 jeux
			CardPlay cp = null;
			if ( simulRoot != null && simulRoot.donnePlay.tour == donnePlay.tour ) {
					// si m�me pli alors prendre la carte simul�e
				cp = nextSimulRoot.donnePlay.plis[nextSimulRoot.donnePlay.tour - 1][donnePlay.joueur];
			} else if (simulRoot == null || profondeur == 1 ) {
					// construction de l'arbre
				simulRoot = simulation(donnePlay, profondeur);
				Set<CardPlay> keys = simulRoot.links.keySet();
				CardPlay[] cardToPlay = (CardPlay[]) keys.toArray();
				cp = cardToPlay[0];				
				nextSimulRoot = simulRoot.links.get(cp);
			} else {
					// prendre comme racine la derni�re carte jou�e
				simulRoot = nextSimulRoot;
					// relancer la simulation sur un niveau
					// todo
			}
			
			carteAJouer = cp.cardToPlay();
			
		} else {
			// recherche de la meilleure carte suivant les r�gles
			carteAJouer = getMeilleureCarte();
		}

		trace("Jeu automatique : " + carteAJouer);
		return carteAJouer;
	}

	/**
	 * Choisir la meilleure carte suivant les r�gles
	 * 
	 * @return carte
	 */
	private String getMeilleureCarte() {
		// jeu courant
		jeu = donnePlay.jeu[donnePlay.joueur];

		// reconstitution du jeu du d�clarant
		// on triche en regardant le jeu
		jeuDeclarant = donnePlay.jeu[donnePlay.declarant];

		if (donnePlay.tour == 0 && donnePlay.nbCartesPli == 0) { // entame
			if (donnePlay.atout == Jeu.SansAtout)
				return entameSA();
			else
				return entameCouleur();
		} else if (donnePlay.nbCartesPli == 0) { // nouveau pli
			if (donnePlay.joueur % 2 == donnePlay.declarant % 2) {
				if (donnePlay.atout == Jeu.SansAtout)
					return declarantSA();
				else
					return declarantCouleur();
			} else {
				if (donnePlay.atout == Jeu.SansAtout)
					return flancSA();
				else
					return flancCouleur();
			}
		} else if (donnePlay.nbCartesPli == 1) { // jeu en second
			if (donnePlay.joueur % 2 == donnePlay.declarant % 2) {
				if (donnePlay.atout == Jeu.SansAtout)
					return declarantSA2();
				else
					return declarantCouleur2();
			} else {
				if (donnePlay.atout == Jeu.SansAtout)
					return flancSA2();
				else
					return flancCouleur2();
			}
		} else if (donnePlay.nbCartesPli == 2) { // jeu en troisi�me
			if (donnePlay.joueur % 2 == donnePlay.declarant % 2) {
				if (donnePlay.atout == Jeu.SansAtout)
					return declarantSA3();
				else
					return declarantCouleur3();
			} else {
				if (donnePlay.atout == Jeu.SansAtout)
					return flancSA3();
				else
					return flancCouleur3();
			}
		} else if (donnePlay.nbCartesPli == 3) { // jeu en quatri�me
			if (donnePlay.joueur % 2 == donnePlay.declarant % 2) {
				if (donnePlay.atout == Jeu.SansAtout)
					return declarantSA4();
				else
					return declarantCouleur4();
			} else {
				if (donnePlay.atout == Jeu.SansAtout)
					return flancSA4();
				else
					return flancCouleur4();
			}
		}
		return null;
	}

	/**
 	 * <pre>
	 * Choix de l'entame � sans atout
	 * reste � traiter:
	 *  - contre couleur artificelles
	 *  - contre pour entame anormale
	 *  - chelem
 	 * </pre>
	 * @return carte
	 */
	public String entameSA() {
		int[] r;

		// couleur du partenaire
		int ip = donneBid.couleurPartenaire(donnePlay.joueur);
		if (ip >= 0 && jeu.nbCartes(ip) > 1) {
			trace("Entame dans couleur du partenaire");
			return jeu.pairImpair(ip);
		}

		// Couleur 5�me : R ou V d'une s�quence d'honneur
		// ou 4�me avec honneur ou 2�me sans honneur
		r = jeu.rechCoul(5, 13, null, true);
		if (r != null) {
			trace("Entame couleur 5�me");
			if (jeu.looklike(r[0], "ARD") || jeu.looklike(r[0], "ARV") || jeu.looklike(r[0], "AVX")
					|| jeu.looklike(r[0], "RVX") || jeu.looklike(r[0], "DVX"))
				return jeu.niemeCarte(r[0], 2);
			else if (jeu.looklike(r[0], "RDV") || jeu.looklike(r[0], "RDX") || jeu.looklike(r[0], "VX9"))
				return jeu.niemeCarte(r[0], 1);
			else if (jeu.looklike(r[0], "ADV"))
				return jeu.niemeCarte(r[0], 3);
			else if (jeu.valeur(r[0]) >= 1)
				return jeu.niemeCarte(r[0], 4);
			else
				return jeu.niemeCarte(r[0], 2);
		}

		// Couleur 4�me ou 3�me avec honneurs : A, D ou V d'une s�quence
		// d'honneur
		// ou 4�me ou 2�me avec 3 cartes
		r = jeu.rechCoul(3, 4, null, true);
		if (r != null) {
			trace("Entame couleur 4�me ou 3�me avec honneur");
			for (int i = 0; i < 4 && r[i] >= 0; i++) {
				if (jeu.looklike(r[0], "ARD") || jeu.looklike(r[0], "ARV") || jeu.looklike(r[0], "DVX"))
					return jeu.niemeCarte(r[0], 1);
				else if (jeu.looklike(r[0], "ADV") || jeu.looklike(r[0], "RDV") || jeu.looklike(r[0], "RDX")
						|| jeu.looklike(r[0], "VX9"))
					return jeu.niemeCarte(r[0], 2);
				else if (jeu.looklike(r[0], "AVX") || jeu.looklike(r[0], "RVX"))
					return jeu.niemeCarte(r[0], 3);
				else if (!jeu.looklike(r[0], "AD") && jeu.valeur(r[0]) > 2) {
					if (jeu.nbCartes(r[0]) == 4)
						return jeu.niemeCarte(r[0], 4);
					else
						return jeu.niemeCarte(r[0], 2);
				}
			}
		}

		// couleur sans honneur: la plus longue
		int[] c = { 0, 1, 2, 3 };
		c = jeu.triCouleurLg(c);
		trace("Entame couleur longue sans honneur");
		return jeu.niemeCarte(c[0], 2);

	}

	/**
 	 * <pre>
	 * Choix de l'entame � la couleur
	 * reste � traiter:
	 *  - entame contre un plan de jeu pr�visible
	 *  - couleur verte
	 *  - contre couleur artificelles
	 *  - contre pour entame anormale
	 *  - chelem
	 * </pre>
	 * @return carte
	 */
	public String entameCouleur() {
		int[] r;
		// Entame avec plan de jeu pr�vu
		// si coupe : atout
		// - gros bicolore
		// - peu de points H
		// si affranchissement : couleur verte la plus forte
		// - ench�re 1M 2x
		// couleur verte ( � revoir )
		// int[] cVertes =
		// jeu.triCouleurForte(jeu.couleursToInt(donne.CouleursNonAnnoncees()));
		// if ( cVertes[0] >= 0 )
		// return jeu.pairImpair(cVertes[0]);

		// Entame sans plan de jeu pr�visible

		// ARD : entame du Roi
		r = jeu.rechCoul(3, 13, "ARD", true);
		if (r != null) {
			trace("Entame R avec ARD");
			return jeu.niemeCarte(r[0], 2);
		}

		// AR : entame de l'As sauf avec 2 cartes
		r = jeu.rechCoul(2, 13, "AR", true);
		if (r != null) {
			trace("Entame A avec AR sauf si 2 cartes");
			if (jeu.nbCartes(r[0]) == 2)
				return jeu.niemeCarte(r[0], 2);
			else
				return jeu.niemeCarte(r[0], 1);
		}

		// singleton (sauf atout)
		// � contr�ler: pas de lev�es naturelle � l'atout
		// � contr�ler: partenaire assez fort pour avoir un As
		r = jeu.rechCoul(1);
		if (r != null) {
			trace("Entame singleton");
			for (int i = 0; i < 4; i++) {
				if (r[i] >= 0 && r[i] != donnePlay.atout)
					return jeu.niemeCarte(r[i], 1);
			}
		}

		// couleur du partenaire
		int ip = donneBid.couleurPartenaire(donnePlay.joueur);
		if (ip >= 0 && jeu.nbCartes(ip) > 0) {
			trace("Entame dans couleur du partenaire");
			return jeu.pairImpair(ip);
		}

		// s�quence d'honneurs RDV ou DVX
		r = jeu.rechCoul(3, 13, "RDV");
		if (r != null) {
			trace("Entame s�quence d'honneur RDV");
			for (int i = 0; i < 4; i++) {
				if (r[i] >= 0)
					return jeu.niemeCarte(r[i], 1);
			}
		}
		r = jeu.rechCoul(3, 13, "DVX");
		if (r != null) {
			trace("Entame s�quence d'honneur DVX");
			for (int i = 0; i < 4; i++) {
				if (r[i] >= 0)
					return jeu.niemeCarte(r[i], 1);
			}
		}

		// s�quence d'honneurs RD ou DV
		r = jeu.rechCoul(2, 13, "RD");
		if (r != null) {
			trace("Entame s�quence d'honneur RD");
			for (int i = 0; i < 4; i++) {
				if (r[i] >= 0) {
					if (jeu.nbCartes(r[i]) == 2)
						return jeu.niemeCarte(r[i], 2);
					else
						return jeu.niemeCarte(r[i], 1);
				}
			}
		}
		r = jeu.rechCoul(2, 13, "DV");
		if (r != null) {
			trace("Entame s�quence d'honneur DV");
			for (int i = 0; i < 4; i++) {
				if (r[i] >= 0) {
					if (jeu.nbCartes(r[i]) == 2)
						return jeu.niemeCarte(r[i], 2);
					else
						return jeu.niemeCarte(r[i], 1);
				}
			}
		}

		// doubleton sans honneur >= valet (sauf atout)
		r = jeu.rechCoul(2, 2, "V-");
		if (r != null) {
			trace("Entame doubleton");
			for (int i = 0; i < 4; i++) {
				if (r[i] >= 0 && r[i] != donnePlay.atout)
					return jeu.pairImpair(r[i]);
			}
		}

		// 3 petits atouts
		if (jeu.nbCartes(donnePlay.atout) == 3 && jeu.looklike(donnePlay.atout, "X-")) {
			trace("Entame 3 petits atouts");
			return jeu.niemeCarte(donnePlay.atout, 2);
		}

		// couleur sans honneur: la plus longue au moins 3 cartes
		int[] coulLong = { 0, 1, 2, 3 };
		coulLong = jeu.triCouleurLg(coulLong);
		for (int i = 0; i < 4; i++) {
			trace("Entame couleur sans honneur");
			if (jeu.nbCartes(coulLong[i]) >= 3 && jeu.looklike(coulLong[i], "V-"))
				return jeu.pairImpair(coulLong[i]);
		}

		// couleur avec honneur: la plus longue,la plus faible
		for (int i = 0; i < 4; i++) {
			trace("Entame couleur avec honneur sauf sous As");
			if (!jeu.looklike(coulLong[i], "A")) // ne pas entamer sous un As
				return jeu.pairImpair(coulLong[i]);
		}

		// doubleton par l'As (sauf atout)
		r = jeu.rechCoul(2, 2, "A");
		if (r != null) {
			trace("Entame doubleton par l'As");
			for (int i = 0; i < 4; i++) {
				if (r[i] >= 0 && r[i] != donnePlay.atout)
					return jeu.niemeCarte(r[i], 1);
			}
		}
		// en d�sespoir de cause l'As de la couleur la plus longue
		trace("Entame de l'As de la couleur la plus longue");
		return jeu.niemeCarte(coulLong[0], 1);
	}

	/**
	 * @return carte
	 */
	public String declarantSA() {
		int[] r;
		return null;
	}

	/**
	 * @return carte
	 */
	public String declarantCouleur() {
		int[] r;
		return null;
	}

	/**
	 * @return carte
	 */
	public String declarantSA2() {
		int[] r;
		return null;
	}

	/**
	 * @return carte
	 */
	public String declarantCouleur2() {
		int[] r;
		return null;
	}

	/**
	 * @return carte
	 */
	public String declarantSA3() {
		int[] r;
		return null;
	}

	/**
	 * @return carte
	 */
	public String declarantCouleur3() {
		int[] r;
		return null;
	}

	/**
	 * @return carte
	 */
	public String declarantSA4() {
		int[] r;
		return null;
	}

	/**
	 * @return carte
	 */
	public String declarantCouleur4() {
		int[] r;
		return null;
	}

	/**
	 * Flanc � SA
	 * 
	 * @return carte
	 */
	public String flancSA() {
		String c;
		int[] r;
		int coulEnt = donnePlay.couleurEntame();
		int hautEnt = donnePlay.hauteurEntame();
		int coulfg1 = donnePlay.plis[donnePlay.flancGauche][0].couleur;
		int hautfg1 = donnePlay.plis[donnePlay.flancGauche][0].hauteur;

		// second pli de l'entameur rest� ma�tre
		if (donnePlay.joueur == donnePlay.flancGauche && donnePlay.tour == 1) {
			// apr�s entame de l'As, D ou X et appel
			if ((hautEnt == Jeu.As || hautEnt == Jeu.Dame || hautEnt == Jeu.Dix) && coulfg1 == coulEnt
					&& hautfg1 < Jeu.Six) {
				trace("Second pli apr�s entame A,D,X et appel");
				return jeu.niemeCarte(coulEnt, 1);
			}
			// apr�s entame du R ou V : continuation de la couleur � �tudier
			if ((hautEnt == Jeu.Roi || hautEnt == Jeu.Valet) && coulfg1 == coulEnt) {
				trace("Second pli apr�s entame R,V");
				return jeu.niemeCarte(coulEnt, 1);
			}
		}

		// cartes ma�tresses
		// � moduler ( si chute par ex. )
		for (int i = 0; i < Jeu.nbCouleur; i++) {
			if (jeu.nbCartes(i) > 0 && donnePlay.carteMaitresse(i, jeu.carte(i, 1))) {
				trace("Jouer carte ma�tresse");
				return jeu.niemeCarte(i, 1);
			}
		}

		// flanc droit

		if (donnePlay.joueur == donnePlay.flancDroit) {
			// couleur d'entame si prometteuse
			if (jeu.nbCartes(coulEnt) > 0) {
				if (hautEnt == Jeu.Valet || hautEnt == Jeu.Roi || regleDesOnze() >= 0) {
					trace("Continuer couleur d'entame");
					return jeu.pairImpair(donnePlay.couleurEntame());
				}
			}

			// couleur prometteuse du partenaire

			c = couleurPrometteuse();
			if (c != null)
				return c;

			// Nouvelle couleur longue 5�me
			// Couleur 5�me : t�te de s�quence d'honneur
			// ou 4�me avec honneur ou 2�me sans honneur
			r = jeu.rechCoul(5, 13, null, true);
			if (r != null) {
				trace("Nouvelle couleur longue");
				if (jeu.looklike(r[0], "ARD") || jeu.looklike(r[0], "ARV") || jeu.looklike(r[0], "RDV")
						|| jeu.looklike(r[0], "RDX") || jeu.looklike(r[0], "VX9")) {
					trace("T�te s�quence d'honneurs");
					return jeu.niemeCarte(r[0], 1);
				} else if (jeu.looklike(r[0], "AVX") || jeu.looklike(r[0], "RVX") || jeu.looklike(r[0], "ADV")) {
					trace("T�te s�quence d'honneurs");
					return jeu.niemeCarte(r[0], 2);
				} else if (jeu.valeur(r[0]) >= 1) {
					trace("Quatri�me meilleure");
					return jeu.niemeCarte(r[0], 4);
				} else {
					trace("Seconde meilleure");
					return jeu.niemeCarte(r[0], 2);
				}
			}
			// faible du mort
			return faibleMort();
		} else {
			// flanc gauche
			// couleur d'entame si prometteuse
			if (donnePlay.jeuInitial[donnePlay.joueur].nbCartes(coulEnt) >= 5 && jeu.nbCartes(coulEnt) > 0) {
				trace("Continuer couleur d'entame");
				return jeu.niemeCarte(coulEnt, 1); // � am�liorer
			}

			// couleur prometteuse du partenaire

			c = couleurPrometteuse();
			if (c != null)
				return c;

			// forte du mort
			return forteMort();
		}
	}

	/**
	 * Flanc � la couleur
	 * 
	 * @return carte
	 */
	public String flancCouleur() {
		int coulEnt = donnePlay.couleurEntame();
		int hautEnt = donnePlay.hauteurEntame();
		int coulfg1 = donnePlay.plis[donnePlay.flancGauche][0].couleur;
		int hautfg1 = donnePlay.plis[donnePlay.flancGauche][0].hauteur;

		// second pli de l'entameur rest� ma�tre
		if (donnePlay.joueur == donnePlay.flancGauche && donnePlay.tour == 1) {
			// apr�s entame de l'As et appel
			if (hautEnt == Jeu.As && coulfg1 == coulEnt) {
				if (hautfg1 == Jeu.Dame) // si Dame
				{
					trace("Apr�s entame As appel Dame");
					return jeu.appelPref(coulEnt);
				} else if (hautfg1 > Jeu.Cinq) {
					trace("Apr�s entame As et appel");
					return jeu.niemeCarte(coulEnt, 1);
				}
			}
		}
		// troisi�me pli de l'entameur rest� ma�tre
		if (donnePlay.joueur == donnePlay.flancGauche && donnePlay.tour == 2) {
			// apr�s As et Roi
			if (hautEnt == Jeu.As) {
				trace("Apr�s entame As Roi et appel");
				return jeu.appelPref(coulEnt);
			}
		}

		// retour du partenaire de l'entameur dans couleur d'entame
		if (donnePlay.joueur == donnePlay.flancDroit && donnePlay.tour < 4) {
			if (jeu.nbCartes(coulEnt) > 0) {
				trace("Retour dans couleur d'entame");
				return jeu.appelPref(coulEnt);
			}
		}

		// cartes ma�tresses
		// � moduler ( si chute par ex. )
		for (int i = 0; i < Jeu.nbCouleur; i++) {
			if (jeu.nbCartes(i) > 0 && donnePlay.carteMaitresse(i, jeu.carte(i, 1))) {
				trace("Jouer carte ma�tresse");
				return jeu.niemeCarte(i, 1);
			}
		}

		// flanc droit

		if (donnePlay.joueur == donnePlay.flancDroit) {
			// faible du mort
			return faibleMort();
		} else {
			// forte du mort
			return forteMort();
		}
	}

	/**
	 * @return carte
	 */
	public String flancSA2() {
		if (jeu.nbCartes(donnePlay.couleurPli) > 0) {
			// Honneur sur honneur ou petit
			return honneurSurHonneur();
		}
		// d�fausse
		return defausse();
	}

	/**
	 * @return carte
	 */
	public String flancCouleur2() {
		if (jeu.nbCartes(donnePlay.couleurPli) > 0) {
			// Honneur sur honneur ou petit
			return honneurSurHonneur();
		}

		// coupe
		String c = coupe();
		if (c != null)
			return c;

		// d�fausse
		return defausse();
	}

	/**
	 * @return carte
	 */
	public String flancSA3() {
		if (jeu.nbCartes(donnePlay.couleurPli) > 0) {
			// premier tour: signalisation
			if (donnePlay.tour == 0) {
				int h = donnePlay.hauteurEntame();
				// entame R ou V : d�bloquer ou compte
				if (h == Jeu.Roi || h == Jeu.Valet) {
					if (jeu.carte(donnePlay.couleurPli, 1) >= Jeu.Valet)
						return jeu.niemeCarte(donnePlay.couleurPli, 1);
					else
						return jeu.pairImpair(donnePlay.couleurPli);
				}
				// entame A, D ou X : petit appel
				else if (h == Jeu.As || h == Jeu.Dame || h == Jeu.Dix) {
					if (jeu.nbCartes(donnePlay.couleurPli) >= Jeu.Six)
						return jeu.niemeCarte(donnePlay.couleurPli, 99);
					else
						return jeu.grosseCarte(donnePlay.couleurPli);
				}
			}

			if (jeu.carte(donnePlay.couleurPli, 1) > donnePlay.hauteurPli)
				// monter avec la plus forte en troisi�me
				// � faire: intercaler derri�re le mort
				return jeu.minMaxCarte(donnePlay.couleurPli);
			else
				return jeu.niemeCarte(donnePlay.couleurPli, 99);
		}
		// d�fausse
		return defausse();
	}

	/**
	 * @return carte
	 */
	public String flancCouleur3() {
		if (jeu.nbCartes(donnePlay.couleurPli) > 0) {
			// premier tour: signalisation
			if (donnePlay.tour == 0) {
				int h = donnePlay.hauteurEntame();
				// entame As : appel
				if (h == Jeu.As) {
					trace("Entame de l'As");
					if (donnePlay.jeu[donnePlay.mort].nbCartes(donnePlay.couleurPli) < 2
							|| donnePlay.jeu[donnePlay.mort].looklike(donnePlay.couleurPli, "R")) {
						trace("Appel de pr�f�rence avec courte ou Roi au mort");
						return jeu.appelPref(donnePlay.couleurPli);
					} else if (jeu.looklike(donnePlay.couleurPli, "DV")) {
						trace("Appel avec la dame avec DV");
						return jeu.niemeCarte(donnePlay.couleurPli, 1);
					} else if (jeu.nbCartes(donnePlay.couleurPli) == 2) {
						trace("Appel avec 2 cartes");
						return jeu.niemeCarte(donnePlay.couleurPli, 1);
					} else if (jeu.looklike(donnePlay.couleurPli, "D") || jeu.looklike(donnePlay.couleurPli, "R")) {
						trace("Appel avec la Dame ou le Roi!");
						return jeu.niemeCarte(donnePlay.couleurPli, 1);
					} else {
						trace("Refus");
						return jeu.niemeCarte(donnePlay.couleurPli, 99);
					}
				}
				// entame R : parit�
				else if (h == Jeu.Roi) {
					trace("Entame Roi : parit�");
					return jeu.pairImpair(donnePlay.couleurPli);
				}
			}
			if (!donnePlay.pliCoupe && jeu.carte(donnePlay.couleurPli, 1) > donnePlay.hauteurPli)
				// monter avec la plus forte en troisi�me
				// � faire: intercaler derri�re le mort
				return jeu.minMaxCarte(donnePlay.couleurPli);
			else
				return jeu.niemeCarte(donnePlay.couleurPli, 99);
		}

		// coupe
		String c = coupe();
		if (c != null)
			return c;

		// d�fausse
		return defausse();
	}

	/**
	 * @return carte
	 */
	public String flancSA4() {
		// prendre si possible
		String s = prendrePliEnDernier();
		if (s != null)
			return s;

		// d�fausse
		return defausse();
	}

	/**
	 * @return carte
	 */
	public String flancCouleur4() {
		// prendre si possible
		String s = prendrePliEnDernier();
		if (s != null)
			return s;

		// d�fausse
		return defausse();
	}

	/**
	 * Coupe
	 * 
	 * @return carte
	 */
	public String coupe() {
		if (jeu.nbCartes(donnePlay.atout) == 0)
			return null;
		if (donnePlay.nbCartesPli == 3) // en quatri�me position
		{
			if (donnePlay.pliCoupe) {
				if (jeu.carte(donnePlay.atout, 1) < donnePlay.hauteurPli)
					return null; // ne peut pas surcouper
				else {
					trace("Surcoupe");
					return jeu.minMaxCarte(donnePlay.atout, donnePlay.hauteurPli);
				}
			}
			if (donnePlay.maitrePli % 2 != donnePlay.joueur % 2) // partenaire
																	// non
																	// maitre
			{
				trace("Coupe");
				return jeu.minMaxCarte(donnePlay.atout);
			}
		} else if (donnePlay.joueur == donnePlay.flancGauche) // avant le mort
																// en 2 ou 3�me
		// position
		{
			boolean mortCoupe = false;
			boolean mortSurcoupe = false;
			if (donnePlay.jeu[donnePlay.mort].nbCartes(donnePlay.couleurPli) == 0
					&& donnePlay.jeu[donnePlay.mort].nbCartes(donnePlay.atout) > 0) {
				mortCoupe = true;
				if (donnePlay.jeu[donnePlay.mort].carte(donnePlay.atout, 1) > jeu.carte(donnePlay.atout, 1))
					mortSurcoupe = true;
			}
			if (mortSurcoupe)
				return null; // pas de coupe si surcoupe au mort
			if (donnePlay.nbCartesPli == 1) // en seconde position avant le mort
			{
				if (mortCoupe) {
					trace("Coupe avant le mort");
					return jeu.minMaxCarte(donnePlay.atout, donnePlay.jeu[donnePlay.mort].carte(donnePlay.atout, 1));
				}
				if (donnePlay.carteMaitresse(donnePlay.couleurPli, donnePlay.hauteurPli)) // carte
																							// maitresse
				{
					trace("Coupe carte ma�tresse");
					return jeu.minMaxCarte(donnePlay.atout);
				}
				if (donnePlay.carteMaitresse(donnePlay.couleurPli,
						donnePlay.jeu[donnePlay.mort].carte(donnePlay.couleurPli, 1))) {
					trace("Coupe car carte ma�tresse au mort");
					return jeu.minMaxCarte(donnePlay.atout);
				}
			} else if (donnePlay.nbCartesPli == 2) // en troisi�me position
													// avant le
			// mort
			{
				if (donnePlay.pliCoupe) {
					if (jeu.carte(donnePlay.atout, 1) < donnePlay.hauteurPli)
						return null; // ne peut pas surcouper
					else {
						trace("surcoupe avant le mort");
						return jeu.minMaxCarte(donnePlay.atout, Math
								.max(donnePlay.jeu[donnePlay.mort].carte(donnePlay.atout, 1), donnePlay.hauteurPli));
					}
				}
				if (mortCoupe) {
					trace("Coupe avant le mort");
					return jeu.minMaxCarte(donnePlay.atout, donnePlay.jeu[donnePlay.mort].carte(donnePlay.atout, 1));
				}
				if (donnePlay.maitrePli % 2 != donnePlay.joueur % 2) // partenaire
																		// non
																		// maitre
				{
					trace("Coupe car partenaire pas maitre");
					return jeu.minMaxCarte(donnePlay.atout);
				}
				if (donnePlay.carteMaitresse(donnePlay.couleurPli,
						donnePlay.jeu[donnePlay.mort].carte(donnePlay.couleurPli, 1))) {
					trace("Coupe car carte ma�tresse au mort");
					return jeu.minMaxCarte(donnePlay.atout);
				}
			}
		} else
		// apr�s le mort en 2 ou 3�me position
		{
			boolean declarantCoupe = false;
			boolean declarantSurcoupe = false;
			if (jeuDeclarant.nbCartes(donnePlay.couleurPli) == 0 && jeuDeclarant.nbCartes(donnePlay.atout) > 0) {
				declarantCoupe = true;
				if (jeuDeclarant.carte(donnePlay.atout, 1) > jeu.carte(donnePlay.atout, 1))
					declarantSurcoupe = true;
			}
			if (declarantSurcoupe)
				return null; // pas de coupe si surcoupe au mort
			if (donnePlay.nbCartesPli == 1) // en seconde position apr�s le mort
			{
				if (declarantCoupe) {
					trace("Coupe avant le d�clarant");
					return jeu.minMaxCarte(donnePlay.atout, jeuDeclarant.carte(donnePlay.atout, 1));
				}
				if (donnePlay.carteMaitresse(donnePlay.couleurPli, donnePlay.hauteurPli)) // carte
																							// maitresse
				{
					trace("Coupe carte ma�tresse");
					return jeu.minMaxCarte(donnePlay.atout);
				}
				if (donnePlay.carteMaitresse(donnePlay.couleurPli, jeuDeclarant.carte(donnePlay.couleurPli, 1))) {
					trace("Coupe car carte ma�tresse chez le d�clarant");
					return jeu.minMaxCarte(donnePlay.atout);
				}
			} else if (donnePlay.nbCartesPli == 2) // en troisi�me position
													// apr�s le
			// mort
			{
				if (donnePlay.pliCoupe) {
					if (jeu.carte(donnePlay.atout, 1) < donnePlay.hauteurPli)
						return null; // ne peut pas surcouper
					else {
						trace("surcoupe avant le mort");
						return jeu.minMaxCarte(donnePlay.atout,
								Math.max(jeuDeclarant.carte(donnePlay.atout, 1), donnePlay.hauteurPli));
					}
				}
				if (declarantCoupe) {
					trace("Coupe avant le mort");
					return jeu.minMaxCarte(donnePlay.atout, jeuDeclarant.carte(donnePlay.atout, 1));
				}
				if (donnePlay.maitrePli % 2 != donnePlay.joueur % 2) // partenaire
																		// non
																		// maitre
				{
					trace("Coupe car partenaire pas maitre");
					return jeu.minMaxCarte(donnePlay.atout);
				}
				if (donnePlay.carteMaitresse(donnePlay.couleurPli, jeuDeclarant.carte(donnePlay.couleurPli, 1))) {
					trace("Coupe car carte ma�tresse chez le d�clarant");
					return jeu.minMaxCarte(donnePlay.atout);
				}
			}
		}

		return null;
	}

	/**
	 * couleur prometteuse du partenaire
	 * 
	 * @return carte
	 */
	public String couleurPrometteuse() {
		boolean couleurJoue[] = { false, false, false, false };
		for (int t = 0; t < donnePlay.tour; t++) {
			int c = donnePlay.plis[donnePlay.flancDroit][t].couleur;
			int h = donnePlay.plis[donnePlay.flancDroit][t].hauteur;
			// honneur ou carte < 6 = prometteuse
			if (t > 0 && !couleurJoue[t] && donnePlay.joueur1Pli[t] == donnePlay.partenaire && jeu.nbCartes(c) > 0
					&& (h < Jeu.Six || h > Jeu.Neuf)) {
				trace("Continuer couleur prometteuse");
				return jeu.pairImpair(c);
			}
			couleurJoue[c] = true;
		}
		return null;
	}

	/**
	 * D�fausse
	 * 
	 * @return carte
	 */
	public String defausse() {
		trace("D�fausser");
		// couleur sans honneur: la plus longue
		int[] c = { 0, 1, 2, 3 };
		c = jeu.triCouleurLg(c);
		for (int i = 0; i < 4; i++) {
			if (c[i] != donnePlay.atout && jeu.looklike(c[i], "V-")) {
				trace("D�fausser dans couleur longue sans honneur");
				return jeu.pairImpair(c[i]);
			}
		}
		for (int i = 0; i < 4; i++) {
			if (c[i] != donnePlay.atout && donnePlay.jeu[donnePlay.mort].nbCartes(c[i]) < 4
					&& jeuDeclarant.nbCartes(c[i]) < 4) {
				trace("D�fausser dans couleur courte du mort ou du d�clarant");
				return jeu.pairImpair(c[i]);
			}
		}
		trace("D�fausse par d�faut");
		return jeu.pairImpair(c[0]);
	}

	/**
	 * Prendre le pli si possible Avec la carte la plus �conomique sinon fournir
	 * petit Couper ou surcouper si possible sinon d�fausser
	 * 
	 * @return carte
	 */
	private String prendrePliEnDernier() {
		String c = null;
		if (jeu.nbCartes(donnePlay.couleurPli) > 0) {
			if (donnePlay.maitrePli % 2 != donnePlay.joueur % 2 && !donnePlay.pliCoupe
					&& jeu.carte(donnePlay.couleurPli, 1) > donnePlay.hauteurPli) {
				trace("Prendre avec la carte la plus �conomique");
				c = jeu.minMaxCarte(donnePlay.couleurPli, donnePlay.hauteurPli);
			} else {
				trace("Fournir la plus faible");
				c = jeu.niemeCarte(donnePlay.couleurPli, 99);
			}
		} else if (donnePlay.atout != Jeu.SansAtout && donnePlay.maitrePli % 2 != donnePlay.joueur % 2
				&& jeu.nbCartes(donnePlay.atout) > 0) {
			if (donnePlay.pliCoupe)
				if (jeu.carte(donnePlay.atout, 1) > donnePlay.hauteurPli) {
					trace("Surcouper avec la plus �conomique");
					c = jeu.minMaxCarte(donnePlay.atout, donnePlay.hauteurPli);
				} else {
					c = defausse();
				}
			else {
				trace("Couper avec la plus �conomique");
				c = jeu.niemeCarte(donnePlay.atout, 99);
			}
		}

		return c;
	}

	/**
	 * Faible du mort
	 * 
	 * @return carte
	 */
	private String faibleMort() {
		int[] c = { 0, 1, 2, 3 };
		c = donnePlay.jeu[donnePlay.mort].triCouleurForte(c);
		for (int i = 3; i >= 0; i--) {
			if (jeu.nbCartes(c[i]) > 0) {
				if (jeu.valeur(c[i]) >= 2) {
					trace("Faible du mort, prometteur");
					return jeu.niemeCarte(c[i], 99);
				} else {
					trace("Faible du mort, non prometteur");
					return jeu.grosseCarte(c[i]);
				}
			}
		}
		return null;
	}

	/**
	 * Forte du mort
	 * 
	 * @return carte
	 */
	private String forteMort() {
		int[] c = { 0, 1, 2, 3 };
		c = donnePlay.jeu[donnePlay.mort].triCouleurForte(c);
		for (int i = 0; i < 4; i++) {
			if (jeu.nbCartes(c[i]) > 0) {
				trace("Forte du mort");
				return jeu.niemeCarte(c[i], 99); // � revoir pour H en
				// for�ante
			}
		}
		return null;
	}

	/**
	 * Honneur sur honneur ou petit sauf intercalage
	 * 
	 * @return carte
	 */
	private String honneurSurHonneur() {
		// contr�le si l'on peut monter ou si le pli est coup�
		if (donnePlay.pliCoupe || jeu.nbCartesSup(donnePlay.couleurPli, donnePlay.hauteurPli) == 0) {
			trace("Ne peut pas monter");
			return jeu.niemeCarte(donnePlay.couleurPli, 99);
		}

		// Honneur sur honneur � partir du dix
		String c = null;
		if (jeu.nbCartes(donnePlay.couleurPli) > 1 && donnePlay.lastCardPlay.hauteur >= Jeu.Dix
				&& jeu.nbCartesSup(donnePlay.couleurPli, donnePlay.lastCardPlay.hauteur) > 0) {
			// Prendre si possible
			if (donnePlay.carteMaitresse(donnePlay.couleurPli, jeu.carte(donnePlay.couleurPli, 1))) {
				trace("Prendre");
				return jeu.niemeCarte(donnePlay.couleurPli, 1);
			} else {
				// OK si H second ou si plusieurs honneurs
				if (jeu.nbCartes(donnePlay.couleurPli) == 2 || jeu.nbCartesSup(donnePlay.couleurPli, '9') > 1) {
					trace("H/H avec H second ou plusieurs H");
					c = jeu.minMaxCarte(donnePlay.couleurPli, donnePlay.hauteurPli);
				}
				// avec un seul H au moins 3�me : pas H/H si atout ou couleur
				// longue
				else if (donnePlay.couleurPli != donnePlay.atout || jeu.nbCartes(donnePlay.couleurPli) < 4) {
					// Avant le mort : pas devant H isol� au mort et pas sur le
					// dix
					if (donnePlay.joueur == donnePlay.flancGauche) {
						if (donnePlay.lastCardPlay.hauteur >= Jeu.Valet
								&& donnePlay.jeu[donnePlay.mort].nbCartesSup(donnePlay.couleurPli, '9') > 1) {
							trace("H/H avant mort");
							c = jeu.minMaxCarte(donnePlay.couleurPli, donnePlay.hauteurPli);
						} else
							return jeu.niemeCarte(donnePlay.couleurPli, 99);
					}
					// Apr�s le mort : seulement sur H isol� au mort
					if (donnePlay.joueur == donnePlay.flancDroit) {
						if (donnePlay.jeu[donnePlay.mort].nbCartesSup(donnePlay.couleurPli, '9') == 0) {
							trace("H/H apr�s mort");
							c = jeu.minMaxCarte(donnePlay.couleurPli, donnePlay.hauteurPli);
						} else
							return jeu.niemeCarte(donnePlay.couleurPli, 99);
					}
				}
			}
		}
		// intercalage si 2 honneurs
		if (c == null && jeu.nbCartesSup(donnePlay.couleurPli, '9') > 0) {
			trace("intercaler un H");
			c = jeu.minMaxCarte(donnePlay.couleurPli, Jeu.Neuf);
		}
		// intercalage avec D ou R devant AVXx(x) au mort
		if (c == null && donnePlay.joueur == donnePlay.flancGauche && jeu.looklike(donnePlay.couleurPli, "AVXx")) {
			trace("intercaler un H");
			c = jeu.minMaxCarte(donnePlay.couleurPli, Jeu.Neuf);
		}
		if (c == null)
			c = jeu.niemeCarte(donnePlay.couleurPli, 99);
		return c;
	}

	// calcule le nombre de cartes du d�clarant suivant la r�gle des onze
	// retourne < 0 si pas 4ieme

	/**
	 * Calcule le nombre de cartes du d�clarant sup�rieures � l'entame suivant
	 * la r�gle des onze pour une entame en quatri�me meilleure
	 * 
	 * @return nombre de cartes du d�clarant sup�rieures (<0 si pas quatri�me
	 *         meilleure)
	 */
	private int regleDesOnze() {
		int c = donnePlay.couleurEntame();
		int h = donnePlay.hauteurEntame();
		int nb = 9 - h - donnePlay.jeu[donnePlay.mort].nbCartesSup(c, h) - jeu.nbCartesSup(c, h)
				- ((donnePlay.plis[0][donnePlay.declarant].hauteur > h) ? 1 : 0);

		return nb;
	}

	/**
	 * Simulation de jeu pour trouver la meilleure carte � jouer
	 * 
	 * @param donnePlay
	 *            : donne � analyser (DonnePlay)
	 * @param profondeur
	 *            : nombre de coups � analyser (int)
	 * @return resultatSimulation
	 */

	public TreeNodeSimulation simulation(DonnePlay donnePlay1, int profondeur) {
			// cr�ation node courant
		int joueurSimul = donnePlay1.joueur;
		TreeNodeSimulation next = new TreeNodeSimulation();
		next.donnePlay = donnePlay1.clone();
		next.links = new HashMap<CardPlay,TreeNodeSimulation>();
		
			// d�but de pli ?
		int couleurCourante = -1;
		if (donnePlay1.nbCartesPli == 1)
			couleurCourante = donnePlay1.couleurPli;
		
			// liste des meilleurs jeux simul�s r�sultats
		ArrayList<DonnePlay> listDonne = new ArrayList<DonnePlay>();
		
			//	cartes � jouer
		ArrayList<CardPlay> nextCardToPlay[] = new ArrayList[Jeu.nbJoueur - donnePlay1.nbCartesPli - 1];
		nextCardToPlay[0] = donnePlay1.cartesJouables(joueurSimul, couleurCourante);
			//  jouer toutes les cartes du joueur
		for (CardPlay carteJ1 : nextCardToPlay[0]) {
			DonnePlay donnePlay2 = donnePlay1.clone(); 
			donnePlay2.joueCarte(carteJ1.couleur, carteJ1.hauteur);
				// cartes �ventuellement jou�es par les adversaires et le partenaire
			for ( int i = 1; i < nextCardToPlay.length; i++ ) {
				nextCardToPlay[i] = donnePlay2.cartesJouables((joueurSimul+i)%4, donnePlay2.couleurPli);
			}
			ArrayList<DonnePlay> listDonnePart = new ArrayList<DonnePlay>();
			if ( nextCardToPlay.length == 3 )  {
				//	jeu du partenaire
				for (CardPlay carteJp : nextCardToPlay[1]) {
					//	jeu du flanc
					ArrayList<DonnePlay> listDonneFlanc = new ArrayList<DonnePlay>();
					for (CardPlay carteJf1 : nextCardToPlay[0]) {
						for (CardPlay carteJf2 : nextCardToPlay[2]) {
							DonnePlay donnePlay3 = donnePlay1.clone(); 
							donnePlay3.joueCarte(carteJf1.couleur, carteJf1.hauteur);
							donnePlay3.joueCarte(carteJp.couleur, carteJp.hauteur);
							donnePlay3.joueCarte(carteJf2.couleur, carteJf2.hauteur);
							// retenir le minimum
							gardeDonneMin(listDonneFlanc, donnePlay3, joueurSimul%2);
						}
					}
					// retenir le maximum					
					gardeDonneMax(listDonnePart, listDonneFlanc.get(0), joueurSimul%2);
				}
				
			} else if ( nextCardToPlay.length == 2 )  {
				//	jeu du partenaire
				for (CardPlay carteJp : nextCardToPlay[1]) {
					//	jeu du flanc
					ArrayList<DonnePlay> listDonneFlanc = new ArrayList<DonnePlay>();
					for (CardPlay carteJf1 : nextCardToPlay[0]) {
						DonnePlay donnePlay3 = donnePlay1.clone(); 
						donnePlay3.joueCarte(carteJf1.couleur, carteJf1.hauteur);
						donnePlay3.joueCarte(carteJp.couleur, carteJp.hauteur);
						// retenir le minimum
						gardeDonneMin(listDonneFlanc, donnePlay3, joueurSimul%2);
					}
					// retenir le maximum					
					gardeDonneMax(listDonnePart, listDonneFlanc.get(0), joueurSimul%2);
				}
				
			} else if ( nextCardToPlay.length == 1 )  {
				//	jeu du flanc
				ArrayList<DonnePlay> listDonneFlanc = new ArrayList<DonnePlay>();
				for (CardPlay carteJf1 : nextCardToPlay[0]) {
					DonnePlay donnePlay3 = donnePlay1.clone(); 
					donnePlay3.joueCarte(carteJf1.couleur, carteJf1.hauteur);
					// retenir le minimum
					gardeDonneMin(listDonneFlanc, donnePlay3, joueurSimul%2);
				}
				// retenir le maximum					
				gardeDonneMax(listDonnePart, listDonneFlanc.get(0), joueurSimul%2);
				
			}
			// retenir le maximum					
			gardeDonneMax(listDonne, listDonnePart.get(0), joueurSimul%2);			
		}
		
		next.donnePlay = listDonne.get(0);
		// si profondeur = 1 alors retourner l'un des meilleurs couple
		if (profondeur == 1)
			return next;

		// sinon it�rer en appelant simulation avec profondeur-1 sur la donne
		//	et retenir les meilleurs r�sultats
		// todo
		
		return next;
	}
	
	/**
	 * Stockage des donnes min
	 * on conserve toutes les valeurs �gales � une tol�rance pr�t
	 * @param listDonne
	 * @param donnePlayNext
	 */
	private void gardeDonneMin(ArrayList<DonnePlay> listDonne, DonnePlay donnePlayNext, int camp)  {
			//	initialisation de la liste
		if ( listDonne.isEmpty() ) {
			listDonne.add(donnePlayNext);
			return;
		}
		double valeurMin = listDonne.get(0).gagnante[camp];
		double valeurInsert = donnePlayNext.gagnante[camp];
		//	la donne � ins�rer est-elle dans la tol�rance?
		if ( valeurInsert > valeurMin + tolerance )
			return;
		//	la donne � ins�rer est-elle dans la premi�re?
		if ( valeurInsert < valeurMin )  {
			valeurMin = valeurInsert;
			//  suppression des donnes hors tol�rance
			for ( int i = listDonne.size() - 1; i >= 0 ; i-- )  {
				if ( listDonne.get(i).gagnante[camp] > valeurMin + tolerance )  {
					listDonne.remove(i);
				} else break;
			}
		}	
			//	insertion de la donne � sa place en ordre croissant		
		for ( int i = 0; i < listDonne.size(); i++ )  {
			if ( valeurInsert < listDonne.get(i).gagnante[camp] )  {
				listDonne.add(i, donnePlayNext);
				return;
			}
		}
		listDonne.add(donnePlayNext);		
	}
	
	/**
	 * Stockage des donnes max
	 * on conserve toutes les valeurs �gales � une tol�rance pr�t
	 * @param listDonne
	 * @param donnePlayNext
	 */
	private void gardeDonneMax(ArrayList<DonnePlay> listDonne, DonnePlay donnePlayNext, int camp)  {
		//	initialisation de la liste
		if ( listDonne.isEmpty() ) {
			listDonne.add(donnePlayNext);
			return;
		}
		double valeurMax = listDonne.get(0).gagnante[camp];
		double valeurInsert = donnePlayNext.gagnante[camp];
		//	la donne � ins�rer est-elle dans la tol�rance?
		if ( valeurInsert < valeurMax - tolerance )
			return;
		//	la donne � ins�rer est-elle dans la premi�re?
		if ( valeurInsert > valeurMax )  {
			valeurMax = valeurInsert;
			//  suppression des donnes hors tol�rance
			for ( int i = listDonne.size() - 1; i >= 0 ; i-- )  {
				if ( listDonne.get(i).gagnante[camp] < valeurMax - tolerance )  {
					listDonne.remove(i);
				} else break;
			}
		}	
			//	insertion de la donne � sa place en ordre d�croissant		
		for ( int i = 0; i < listDonne.size(); i++ )  {
			if ( valeurInsert > listDonne.get(i).gagnante[camp] )  {
				listDonne.add(i, donnePlayNext);
				return;
			}
		}
		listDonne.add(donnePlayNext);		
	}

	/**
	 * Trace
	 * 
	 * @param mess
	 *            : message (String)
	 */

	private void trace(String mess) {
		if (trace) {
			System.out.println("Trace: " + mess);
		}
	}

}
