package bridgePlay;

import java.util.ArrayList;
import java.util.Collections;

import bridgeBid.DonneBid;

/**
 * Donne de bridge pour le jeu de la carte
 */

public class DonnePlay implements Cloneable {

	/**
	 * représentation d'une carte
	 */

	public static class CardPlay implements Cloneable {
		int couleur; // TKCP
		int hauteur; // ARDVX98765432

		/**
		 * Constructeur
		 * 
		 * @param couleur
		 * @param hauteur
		 */
		public CardPlay(int couleur, int hauteur) {
			this.couleur = couleur;
			this.hauteur = hauteur;
		}

		/**
		 * Clonage
		 */
		public CardPlay clone() throws CloneNotSupportedException {
			return (CardPlay) super.clone();
		}
		
		/**
		 * @return carte sous forme textuelle hauteur couleur
		 */
		public String cardToPlay()  {
			return "" + Jeu.hauteurToChar(hauteur) + Jeu.couleurToChar(couleur);
		}
	}

	// cartes par joueur (NESW) et couleur (TKCP)

	public Jeu[] jeu = new Jeu[Jeu.nbJoueur];
	public Jeu[] jeuInitial = new Jeu[Jeu.nbJoueur];

	// repérage des joueurs

	public int declarant;
	public int mort;
	public int flancDroit;
	public int flancGauche;
	public int joueurAyantLaMain; // joueur qui a la main

	// contrat

	public int hauteurContrat; // nombre de plis à réaliser
	public int atout; // couleur d'atout (0 à 3 pour TKCP, 4 = SA)

	// déroulement du jeu

	public int tour; // numéro du pli courant de 0 à 12
	public CardPlay[][] plis; // plis faits par tour, joueur : valeur + couleur
	public ArrayList<Integer>[] cardPlayed; // cartes déjà jouées par couleur
	public int gainPli[]; // joueur ayant gagné le pli
	public int joueur1Pli[]; // joueur ayant initié le pli
	public int nbPliDeclarant; // nombre de pli fait par le déclarant
	public int nbPliFlanc; // nombre de pli fait par le flanc

	// déroulement du pli

	public int joueur; // joueur courant de 0 à 3
	public int partenaire; // partenaire du joueur courant de 0 à 3
	public int nbCartesPli; // nb de cartes jouées pour ce pli
	public int couleurPli; // couleur demandée
	public int hauteurPli; // hauteur carte maitresse du pli
	public int maitrePli; // joueur maitre du pli en cours
	public boolean pliCoupe; // pli coupé
	public CardPlay[] pliEnCours; // pli en cours
	public CardPlay lastCardPlay; // dernière carte jouée du pli
	
	// valeur des jeux à la fin du pli précédent 0=NS, 1=EO
	
	public double[] gagnante = new double[2] ; // nombre de plis faits + gagnantes potentielles
	public double[] perdante = new double[2] ; // nombre de plis perdus + perdantes potentielles

	/**
	 * Constructeur à partir des enchères
	 * 
	 * @param donne
	 */
	public DonnePlay(DonneBid donne) {
		this.declarant = donne.joueurContrat();
		this.hauteurContrat = donne.lastHauteurAnnonce();
		this.atout = Jeu.couleurToInt(donne.lastCouleurAnnonce());
		for (int i = 0; i < Jeu.nbJoueur; i++) {
			jeu[i] = new Jeu(donne.getJeu(i).jeu, atout);
			jeuInitial[i] = jeu[i].clone();
		}
		this.joueurAyantLaMain = -1;
		initPlay();
	}

	/**
	 * Constructeur à partir des jeux
	 * 
	 * @param jeux
	 * @param declarant
	 * @param hauteurContrat
	 * @param atout
	 * @param main  joueur ayant la main pour problème partiel, -1 sinon
	 */
	public DonnePlay(String[][] jeux, int declarant, int hauteurContrat, int atout, int main) {
		jeu = new Jeu[Jeu.nbJoueur];
		for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++)
			jeu[joueur] = new Jeu(jeux[joueur], atout);
		this.declarant = declarant;
		this.hauteurContrat = hauteurContrat;
		this.atout = atout;
		this.joueurAyantLaMain = main;
		initPlay();
	}

	/**
	 * initialisation de la donne
	 */

	private void initPlay() {
		mort = (declarant + 2) % Jeu.nbJoueur;
		flancDroit = (declarant - 1) % Jeu.nbJoueur;
		flancGauche = (declarant + 1) % Jeu.nbJoueur;
		tour = 0;
		nbPliDeclarant = nbPliFlanc = 0;

		plis = new CardPlay[Jeu.nbPli][Jeu.nbJoueur];
		for (int nbPli = 0; nbPli < Jeu.nbPli; nbPli++)
			for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++)
				plis[nbPli][joueur] = new CardPlay(-1,-1);
		cardPlayed = new ArrayList[Jeu.nbCouleur];
		for (int i = 0; i < Jeu.nbCouleur; i++)
			cardPlayed[i] = new ArrayList<Integer>();
		gainPli = new int[Jeu.nbPli];
		joueur1Pli = new int[Jeu.nbPli];
		if ( joueurAyantLaMain < 0 )
			joueurAyantLaMain = flancGauche;	// début de la partie
		joueur1Pli[0] = joueurAyantLaMain;

		evalCamp(0);
		evalCamp(1);
		
		initPli();
	}

	/**
	 * Clonage de la donne
	 */

	public DonnePlay clone() {
		DonnePlay donnePlayCopy = null;
		try {
			donnePlayCopy = (DonnePlay) super.clone();
			for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++)
				donnePlayCopy.jeu[joueur] = jeu[joueur].clone();
			for (int nbPli = 0; nbPli < Jeu.nbPli; nbPli++)
				for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++)
					donnePlayCopy.plis[nbPli][joueur] = plis[nbPli][joueur].clone();
		} catch (Exception e) {
			System.out.println("Clonage DonnePlay impossible");
			e.printStackTrace();
			return null;
		}
		return donnePlayCopy;
	}

	/**
	 * initialisation du jeu d'un pli
	 */

	private void initPli() {
		nbCartesPli = 0;
		hauteurPli = -1;
		maitrePli = -1;
		pliCoupe = false;
		joueur = joueurAyantLaMain;
		partenaire = (joueur + 2) % Jeu.nbJoueur;
		pliEnCours = new CardPlay[4];
		lastCardPlay = new CardPlay(-1,-1);
	}

	/**
	 * Jouer une carte
	 * 
	 * @param int
	 *            couleur jouée
	 * @param int
	 *            hauteur jouée
	 *  
	 * @return true si fin du pli, false sinon
	 */

	public boolean joueCarte(int couleurJoue, int hauteurJoue) {

		//	pli en cours
		
		lastCardPlay = new CardPlay(couleurJoue,hauteurJoue);
		pliEnCours[nbCartesPli] = lastCardPlay;

		// enlève la carte jouée du jeu

		jeu[joueur].oteCarte(couleurJoue, hauteurJoue);

		// la met dans le pli

		plis[tour][joueur] = lastCardPlay;
		cardPlayed[couleurJoue].add(hauteurJoue);
		cardPlayed[couleurJoue].sort(Collections.reverseOrder());

		// gestion du pli

		if (nbCartesPli == 0) {
			couleurPli = couleurJoue;
			hauteurPli = hauteurJoue;
			maitrePli = joueur;
		} else if (!pliCoupe && couleurJoue == couleurPli || pliCoupe && couleurJoue == atout) {
			if (hauteurJoue > hauteurPli) {
				hauteurPli = hauteurJoue;
				maitrePli = joueur;
			}
		} else if (!pliCoupe && couleurJoue == atout) {
			pliCoupe = true;
			hauteurPli = hauteurJoue;
			maitrePli = joueur;
		}

		// joueur suivant

		joueur = (joueur == Jeu.nbJoueur - 1) ? 0 : joueur + 1;
		partenaire = (joueur + 2) % Jeu.nbJoueur;
		nbCartesPli++;

		// fin du pli

		if (nbCartesPli == 4) {
			joueurAyantLaMain = maitrePli;
			gainPli[tour] = maitrePli;
			if (joueurAyantLaMain % 2 == declarant % 2)
				nbPliDeclarant++;
			else
				nbPliFlanc++;
			tour++;
			if (tour < Jeu.nbPli) {
				joueur1Pli[tour] = joueurAyantLaMain;
				initPli();
			}
			evalCamp(0);
			evalCamp(1);
			return true;
		}
		return false;
	}

	/**
	 * Teste s'il ne reste plus qu'une seule carte à jouer
	 * 
	 * @return carte sous forme hauteur couleur,
	 * null s'il reste plus d'une seule carte à jouer
	 */

	public String getCarteSeule() {
		if (nbCartesPli > 0 && jeu[joueur].nbCartes(couleurPli) == 1) {
			int h = jeu[joueur].carte(couleurPli, 1);
			return "" + Jeu.hauteurToChar(h) + Jeu.couleurToChar(couleurPli);
		}
		return null;
	}

	/**
	 * Evaluation de la valeur du jeu du camp du joueur
	 * @param joueur
	 */

	public void evalCamp(int joueur) {
		gagnante[joueur % 2] = (joueur % 2 == declarant % 2) ? nbPliDeclarant : nbPliFlanc;
		perdante[joueur % 2] = (joueur % 2 == declarant % 2) ? nbPliFlanc : nbPliDeclarant;
		int partenaire = (joueur + 2) % Jeu.nbJoueur;
		// pour chaque couleur
		for (int couleur = 0; couleur < Jeu.nbCouleur; couleur++) {
			// évaluation des jeux
			ArrayList<Integer> cardJoueur = jeu[joueur].getCartes(couleur);
			ArrayList<Integer> cardPartenaire = jeu[partenaire].getCartes(couleur);
			ArrayList<Integer> cardCamps = new ArrayList<Integer>(cardJoueur);
			cardCamps.addAll(cardPartenaire);
			cardCamps.sort(Collections.reverseOrder());
			cardPlayed[couleur].sort(Collections.reverseOrder());
			double lg = Math.max(cardJoueur.size(), cardPartenaire.size());

			// élève le niveau des cartes en fonction des cartes déjà jouées

			int j = 0; // indice des cartes jouées
			int cj = 0; // indice cartes joueur
			int cp = 0; // indice cartes partenaire
			int decalage = 0; // nombre de cartes supérieures jouées
			for ( int c = 0; c < cardCamps.size(); )  {
				if (cardPlayed[couleur].size() > 0 && cardPlayed[couleur].get(j) > cardCamps.get(c)) {
					decalage++;
					j++;
				} else {
					if (cj < cardJoueur.size() && cardJoueur.get(cj) == cardCamps.get(c)) {
						cardJoueur.set(cj, cardJoueur.get(cj) + decalage);
						cj++;
					} else {
						cardPartenaire.set(cp, cardPartenaire.get(cp) + decalage);
						cp++;
					}
					cardCamps.set(c, cardCamps.get(c) + decalage);
					c++;
				}
			}

			// évaluation des cartes maitresses

			double LJg = 0; // nombre de gagnantes
			int cmax = Jeu.As; // niveau carte maitresse
			decalage = 0;
			for (int c = 0; c < cardCamps.size() && decalage < 2 ; c++) {
				decalage = cmax - cardCamps.get(c);
				if (decalage == 0) {
					// carte maitresse
					LJg += 1;
					cmax--;
				} else if (decalage == 1) {
					cmax-=2;								
				}
			}
			// limite le nombre de plis d'honneur au nombre de cartes de la couleur la plus longue
			if (LJg >= lg)
				LJg = lg;

			// impasse


			// pli de longueur

			double LJgl = 0; // nombre de gagnantes
			if (cardCamps.size() >= 7) {
				if (lg >= 5)
					LJgl += lg - 4;
				if (cardCamps.size() == 7)
					LJgl += 0.3;
				else if (cardCamps.size() == 8)
					LJgl += 0.7;
				else
					LJgl += 1;
			}
			LJg += LJgl;
			if (LJg >= lg)
				LJg = lg;
			
			gagnante[joueur % 2] += LJg;
			
			// évaluation des perdantes
			
			double LJp = 0; // nombre de perdantes
			
			// évaluation des coupes

			if (atout < 4 ) {
	/*			int nbCardDiff = cardJoueur.size() - cardPartenaire.size();
				if (nbCardDiff > 0 && jeu[partenaire].nbCartes(atout) > 0) {
					LJp -= Math.min(nbCardDiff, jeu[partenaire].nbCartes(atout));
				} else if (nbCardDiff < 0 && jeu[joueur].nbCartes(atout) > 0) {
					LJp -= Math.min(-nbCardDiff, jeu[joueur].nbCartes(atout));
				}
	*/	
			}
			perdante[joueur % 2] += LJp;
		}

	}

	/**
	 * Cartes jouables par un joueur
	 * 
	 * @param joueur
	 * @param couleurDemande
	 *            (-1 pas de couleur fixée, premier joueur pli)
	 * @return ensemble des cartes jouables dans un contexte donné en commençant
	 *         par la plus grosse, ne donne que la plus grosse carte d'une
	 *         séquence
	 */

	public ArrayList<CardPlay> cartesJouables(int joueur, int couleurDemande) {
		ArrayList<CardPlay> card = new ArrayList<CardPlay>();

		// première carte du pli ou pas de carte dans la couleur demandée

		if (couleurDemande == -1 || jeu[joueur].isEmpty()) {
			for (int couleur = 0; couleur < Jeu.nbCouleur; couleur++) {
				int hPrec = -100;
				for (int carte = 1; carte <= jeu[joueur].nbCartes(couleur); carte++) {
					int hauteur = jeu[joueur].carte(couleur, carte);
					if (hauteur != hPrec - 1) {
						CardPlay cc = new CardPlay(couleur,hauteur);
						card.add(cc);
					}
					hPrec = hauteur;
				}
			}

		} else {

			// fournir dans la couleur demandée

			int hPrec = -100;
			for (int carte = jeu[joueur].nbCartes(couleurDemande); carte > 0; carte--) {
				int hauteur = jeu[joueur].carte(couleurDemande, carte);
				if (hauteur != hPrec + 1) {
					CardPlay cc = new CardPlay(couleurDemande,hauteur);
					card.add(cc);
				}
				hPrec = hauteur;
			}
		}

		return card;
	}

	/**
	 * @return couleur carte d'entame
	 */
	public int couleurEntame() {
		return plis[flancGauche][0].couleur;
	}

	/**
	 * @return hauteur carte d'entame
	 */
	public int hauteurEntame() {
		return plis[flancGauche][0].hauteur;
	}

	/**
	 * Test carte maitresse
	 * @param couleur
	 * @param hauteur
	 * @return true si la carte est maitresse
	 */
	public boolean carteMaitresse(int couleur, int hauteur) {
		// toutes les cartes supérieures sont-elles tombées ?
		for (int hj = 0; hj < 12 - hauteur; hj++) {
			if ( hj >= cardPlayed[couleur].size() )
				return false;				
			if ( cardPlayed[couleur].get(hj) != 12 - hj )
				return false;
		}
		return true;
	}

}
