package bridgeIHM;

import bridgeBid.Interpreter;
import bridgeBid.DonneBid;
import bridgePlay.Jeu;
import bridgePlay.Simulator;
import winApp.ContexteGlobal;

import java.io.*;
import java.util.*;

import bridge.*;

/**
 * Contrôle du jeu de bridge
 */

/**
 * @author Michel
 *
 */
public class BridgeGame {

			// constantes

	/**
	 * nombre de cartes par jeu
	 */
	public static final int nbCartes = 52;
	/**
	 * dimension du board: 4 joueurs x 4 couleurs +
	 *   carte fournie par chaque joueur pour le pli en cours
	 */
	public static final int dimBoard = 20;
	/**
	 * nombre de joueur
	 */
	public static final int nbJoueur = 4;
	/**
	 * nombre de couleur
	 */
	public static final int nbCouleur = 4;
	/**
	 * board représentant la table
	 */
	public static final int table = 16;
	/**
	 * séquence des vulnérabilités
	 */
	public static final String vulnerabilites[] = { "P", "NS", "EO", "T", "NS", "EW", "T" };
	/**
	 * séquence des donneurs
	 */
	public static final String donneurs[] = { "N", "O", "S", "E" };

			// description du jeu

	/**
	 * Message
	 */
	public String Message;
	/**
	 * message fixe en bas d'écran
	 */
	public String messageFixe = "";
	/**
	 * 
	 */
	public CardBoard cardBoard;
	/**
	 * 
	 */
	public CardBoard cardBoardInit;
	/**
	 * donne
	 */
	public DonneBid donne;
	/**
	 * système enchères NS
	 */
	public String systemNS = null;
	/**
	 * système enchères EO
	 */
	public String systemEO = null;
	/**
	 * indice donneur dans tableau
	 */
	public int idonneur = -1;
	/**
	 * donneur
	 */
	public String donneur = null;
	/**
	 * indice vulnérabilité dans tableau
	 */
	public int ivulnerabilite = -1;
	/**
	 * vulnérabilité
	 */
	public String vulnerabilite = null;
	/**
	 * Type Tournoi
	 */
	public String TypeTournoi = null;
	/**
	 * contrat
	 */
	public String contrat = null;

			// controle du jeu

	/**
	 * stade du jeu ; 0 = enchères, 1 = jeu, 2 = fin
	 */
	public int stadeJeu;
	/**
	 * nombre de plis réalisés en NS
	 */
	public int plisNS;
	/**
	 * nombre de plis réalisés en EO
	 */
	public int plisEO;
	/**
	 * numéro du joueur humain
	 */
	private int joueurHumain;
	/**
	 * mort éventuel joué par un humain
	 */
	private int joueurHumain2;
	/**
	 * contrôle des encheres
	 */
	private Interpreter SysEnchere;
	/**
	 * nombre de passes
	 */
	private int nbPasse;

			// controle du jeu de la carte

	/**
	 * joueur ayant la main
	 */
	public int joueurAyantLaMain;
	/**
	 * 
	 */
	private int[] jeuxCartes;
	/**
	 * historique des coups
	 */
	private CardBoardHisto hist = new CardBoardHisto();
	/**
	 * joueur jouant le contrat
	 */
	private int joueurContrat;
	/**
	 * mort
	 */
	private int mort;
	/**
	 * tour
	 */
	private int tour;
	/**
	 * couleur du tour
	 */
	private int couleurTour;
	/**
	 * nombre de cartes dans le pli
	 */
	private int nbCartesPli;
	/**
	 * atout
	 */
	private int atout;
	/**
	 * nombre de plis du contrat
	 */
	private int nbPliContrat;
	/**
	 * simulateur du jeu de la carte
	 */
	private Simulator bjc;
	/**
	 * représentation des jeux
	 */
	private String jeu[][] = new String[nbJoueur][nbCouleur];

	/**
	 * Constructeur
	 */
	public BridgeGame() {
		String file = BridgePanel.baseDir + "/" + winApp.ContexteGlobal.getResourceString("encheres");
		systemNS = ContexteGlobal.getResourceString("sytemEnchere");
		systemEO = ContexteGlobal.getResourceString("sytemEnchere");
		TypeTournoi = ContexteGlobal.getResourceString("tournoi");
		SysEnchere = new Interpreter(file, true);
		jeuxCartes = new int[nbCartes];
		cardBoard = new CardBoard(dimBoard, 13);
	}

	/**
	 * Initialisation d'une nouvelle donne
	 * @return true si ok
	 */
	public boolean init() {
		if (!SysEnchere.ok() )
			return false;
		cardBoard.init();
		battreCartes();
		distribuerCartes();

		ivulnerabilite = (ivulnerabilite + 1) % 8;
		vulnerabilite = vulnerabilites[ivulnerabilite];
		idonneur = (idonneur + 1) % 4;
		donneur = donneurs[idonneur];

		for (int i = 0; i < nbJoueur * nbCouleur; i++) {
			jeu[i / 4][i % 4] = "";
			for (int j = 0; j < cardBoard.nbCartes[i]; j++)
				jeu[i / 4][i % 4] += Jeu.hauteurToChar(cardBoard.board[i][j]);
		}

		go();
		return true;
	}

	/**
	 * Lancement du jeu
	 */
	public void go() {
		donne = new DonneBid(donneur, vulnerabilite, systemNS, systemEO, TypeTournoi);

		for (int i = 0; i < nbJoueur; i++) {
			try {
				donne.putJeu(i, jeu[i][0], jeu[i][1], jeu[i][2], jeu[i][3]);
			} catch (Exception ex) {
				System.out.println("Erreur donne : " + ex);
			}
		}

		hist.clear();
		hist.historise(cardBoard);
		cardBoardInit = (CardBoard) cardBoard.clone();

		initEnchere();

	}

	/**
	 * Chargement d'une donne
	 * @param file
	 */
	public void load(File file) {
		String line;
		StringTokenizer STline;
		BufferedReader in;

		try {
			in = new BufferedReader(new FileReader(file));
			String problemType = in.readLine();
			line = in.readLine();
			STline = new StringTokenizer(line, " ");
			donneur = STline.nextToken();
			vulnerabilite = STline.nextToken();

			donne = new DonneBid(donneur, vulnerabilite, systemNS, systemEO, TypeTournoi);
			line = in.readLine();
			for (int i = 0; i < Jeu.nbJoueur; i++) {  // joueurs N E S O
				for (int j = Jeu.nbCouleur-1; j >=0; j--) {  // couleurs P C K T
					jeu[i][j] = line;
					line = in.readLine();
				}
				line = in.readLine();
			}
		} catch (IOException ioe) {
			System.out.println("Erreur lecture fichier donne : " + ioe);
		}

		// initialisation du board

		cardBoard.init();
		for (int i = 0; i < nbJoueur * nbCouleur; i++) {
			String jj = jeu[i / 4][i % 4];
			for (int j = 0; j < jj.length(); j++) {
				cardBoard.board[i][j] = Jeu.hauteurToInt(jj.charAt(j)) + (i % 4) * 13;
				cardBoard.nbCartes[i]++;
			}
		}

		go();
	}

	/**
	 * Sauvegarde de la donne courante
	 * @param file
	 */
	public void save(File file) {
		String line;
		BufferedWriter out;

		try {
			out = new BufferedWriter(new FileWriter(file));
			line = "Donne";
			out.write(line, 0, line.length());
			out.newLine();
			line = donneur + " " + vulnerabilite;
			out.write(line, 0, line.length());
			out.newLine();
			for (int i = 0; i < Jeu.nbJoueur; i++) {  // joueurs N E S O
				for (int j = Jeu.nbCouleur-1; j >=0; j--)  { // couleurs P C K T
					line = jeu[i][j];
					out.write(line, 0, line.length());
					out.newLine();
				}
				out.newLine();
			}
			out.close();
		} catch (IOException ioe) {
			System.out.println("Erreur écriture fichier donne : " + ioe);
		}
	}

	/**
	 * Restauration du jeu au début
	 */
	public void restoreJeu() {
		cardBoard = (CardBoard) cardBoardInit.clone();
	}

	/**
	 * Initialisation des enchères
	 */
	public void initEnchere() {
		donne.initEnchere();
		stadeJeu = 0;
		messageFixe = "Enchères";
		Message = "mess1";
		nbPasse = -1;
		joueurHumain = 2;
		joueurHumain2 = -1;
		plisNS = 0;
		plisEO = 0;
	}

	/**
	 * Brassage du jeu de cartes
	 */
	private void battreCartes() {
		int i, j, k, l;

		for (i = 0; i < nbCartes; i++) {
			jeuxCartes[i] = i % 52;
		}
		for (i = 0; i < 1000; i++) {
			j = (int) (java.lang.Math.random() * nbCartes);
			k = (int) (java.lang.Math.random() * nbCartes);
			l = jeuxCartes[j];
			jeuxCartes[j] = jeuxCartes[k];
			jeuxCartes[k] = l;
		}
	}

	/**
	 * Distribution des cartes
	 */
	protected void distribuerCartes() {
		int j, c;

		for (j = 0, c = 0; c < nbCartes; c++) {
			int noBoard = j * nbCouleur + (jeuxCartes[c] / 13);
			cardBoard.board[noBoard][cardBoard.nbCartes[noBoard]++] = jeuxCartes[c];
			j = j == 3 ? 0 : j + 1;
		}

		// tri des cartes

		for (j = 0; j < nbJoueur * nbCouleur; j++) {
			for (int i = 0; i < cardBoard.nbCartes[j] - 1; i++)
				for (int k = i + 1; k < cardBoard.nbCartes[j]; k++) {
					if (cardBoard.board[j][k] > cardBoard.board[j][i]) {
						int temp = cardBoard.board[j][k];
						cardBoard.board[j][k] = cardBoard.board[j][i];
						cardBoard.board[j][i] = temp;
					}
				}

		}
	}

	/**
	 * undo
	 */
	public void undo() {
		cardBoard = hist.restore();
		if (cardBoard == null) {
			System.out.println("undo impossible");
			return;
		}
	}

	/**
	 * Gestion du click de la souris
	 * @param noBoard
	 * @param pos
	 * @return 0 OK, -1 erreur
	 */
	public int click(int noBoard, int pos) {
		if (stadeJeu == 1 && noBoard < nbJoueur * nbCouleur) {
			int joueur = noBoard / nbCouleur;
			int couleurJoue = noBoard % nbCouleur;
			if (joueur != joueurAyantLaMain || nbCartesPli > nbJoueur) {
				Message = "mess2";
				return -1;
			}
			if (couleurTour < 0)
				couleurTour = couleurJoue;
			else if (couleurTour != couleurJoue && nbCartesCouleur(joueur, couleurTour) > 0) {
				Message = "mess3";
				return -1;
			}

			// jeu de la carte

			bjc.putCarte(Jeu.carteToString(cardBoard.board[noBoard][pos]));
			cardBoard.board[table + joueurAyantLaMain][cardBoard.nbCartes[table + joueurAyantLaMain]++] = cardBoard.board[noBoard][pos];
			for (int i = pos + 1; i < cardBoard.nbCartes[noBoard]; i++)
				cardBoard.board[noBoard][i - 1] = cardBoard.board[noBoard][i];
			cardBoard.nbCartes[noBoard]--;
			joueurAyantLaMain = joueurAyantLaMain == 3 ? 0 : joueurAyantLaMain + 1;
			nbCartesPli++;
			if (nbCartesPli > nbJoueur) {
				
					// fin du pli : détermination du gagnant
				
				int couleurRef = Jeu.carteCouleur(cardBoard.board[table + joueurAyantLaMain][0]);
				int hauteurRef = Jeu.carteRang(cardBoard.board[table + joueurAyantLaMain][0]);
				for (int i = 0; i < nbJoueur; i++) {
					int couleur = Jeu.carteCouleur(cardBoard.board[table + i][0]);
					int hauteur = Jeu.carteRang(cardBoard.board[table + i][0]);
					if (couleur == couleurRef && hauteur > hauteurRef) {
						joueurAyantLaMain = i;
						hauteurRef = hauteur;
					} else if (couleurRef != atout && couleur == atout) {
						joueurAyantLaMain = i;
						couleurRef = atout;
						hauteurRef = hauteur;
					}
				}

				if (joueurAyantLaMain % 2 == 0)
					plisNS++;
				else
					plisEO++;
			}
		} else if (stadeJeu == 1 && noBoard >= nbJoueur && nbCartesPli > nbJoueur) {
			// pli suivant

			tour++;
			if (tour > 13) { // fin de la donne
				stadeJeu = 2;
				int resultat = 0;
				if (joueurContrat % 2 == 00)
					resultat = plisNS - 6 - donne.lastHauteurAnnonce();
				else
					resultat = plisEO - 6 - donne.lastHauteurAnnonce();
				if (resultat == 0)
					messageFixe = "Contrat juste fait";
				else if (resultat > 0)
					messageFixe = "Contrat fait avec " + resultat + " plis de mieux";
				else
					messageFixe = "Contrat chuté de " + resultat + " pli(s)";
				return 1;
			}
			couleurTour = -1;
			nbCartesPli = 1;
			for (int i = table; i < table + nbJoueur; i++)
				cardBoard.nbCartes[i] = 0;
		} else
			return -1;

		return 0;
	}

	/**
	 * @param joueur
	 * @param couleur
	 * @return nombre de cartes de la couleur sur le board
	 */
	public int nbCartesCouleur(int joueur, int couleur) {
		int nb = 0;
		int noBoard = joueur * nbCouleur + couleur;
		for (int i = 0; i < cardBoard.nbCartes[noBoard]; i++) {
			if (Jeu.carteCouleur(cardBoard.board[noBoard][i]) == couleur)
				nb++;
		}
		return nb;
	}

	/**
	 * Teste si un joueur est humain
	 * @param joueur
	 * @return true ou false
	 */
	public boolean joueurHumain(int joueur) {
		if (joueur == joueurHumain || stadeJeu == 1 && joueur == joueurHumain2)
			return true;
		return false;
	}

	/**
	 * le mort est-il visible ?
	 * @param i joueur
	 * @return true or false
	 */
	public boolean mortVisible(int i)  {
		if ( stadeJeu == 1 && mort == i && ( tour > 1 || nbCartesPli > 1 ) )
			return true;
		return false;
	}

	/**
	 * Teste si c'est au tour du joueur humain d'enchérir
	 * @return true ou false
	 */
	public boolean enchereHumaine() {
		return joueurHumain == donne.getJoueur();
	}

	/**
	 * Traitement d'une enchère
	 * @param enchere
	 */
	public void enchere(String enchere) {
		if (enchere.length() == 0)
			enchere = SysEnchere.getEnchere(donne);
		if (enchere.equals("-"))
			nbPasse++;
		else
			nbPasse = 0;
		donne.putEnchere(enchere);
		donne.JoueurSuivant();
		if (nbPasse == 3)
			stadeJeu = 1;
	}

	/**
	 * Initialisation du jeu de la carte
	 */
	public void initJeuCarte() {
		stadeJeu = 1;
		joueurContrat = donne.joueurContrat();
		joueurHumain = joueurContrat; // pour les tests
		atout = Jeu.couleurToInt(donne.lastCouleurAnnonce());
		nbPliContrat = donne.lastHauteurAnnonce();
		joueurAyantLaMain = (joueurContrat + 1) % nbJoueur;
		mort = (joueurContrat + 2) % nbJoueur;
		if (joueurContrat % 2 == joueurHumain % 2)
			joueurHumain2 = (joueurHumain + 2) % 4;
		tour = 1;
		couleurTour = -1;
		nbCartesPli = 1;
		plisNS = 0;
		plisEO = 0;
		contrat = "Contrat " + donne.lastAnnonce() + " joué par " + DonneBid.joueurToChar(joueurContrat);
		messageFixe = "Jeu de la carte";

		bjc = new Simulator(donne, 0);

	}

	/**
	 * Teste si c'est au tour du joueur humain de jouer
	 * @return true ou false
	 */
	public boolean jeuCarteHumain() {
		return ((joueurAyantLaMain == joueurHumain || joueurAyantLaMain == joueurHumain2)
				&& bjc.getCarteSeule() == null);
	}
	
	/**
	 * Jeu ordinateur
	 * @return 0 OK, -1 erreur, -2 non implémenté
	 */
	public int jeuAuto() {
		String s; // carte à jouer
		s = bjc.getCarteSeule();
		if (s == null && !jeuCarteHumain()) {
			s = bjc.getCarte();
			if (s == null) {
				Message = "mess7";
				return -2; // non implémenté
			}
		}
		if (s != null) {
			System.out.println("jeu auto carte: " + s);
			int couleur = Jeu.couleurToInt(s.charAt(1));
			int carte = Jeu.carteToInt(s);
			int noBoard = joueurAyantLaMain * nbCouleur + couleur;
			int pos = 0;
			for (; cardBoard.board[noBoard][pos] != carte && pos < cardBoard.nbCartes[noBoard]; pos++)
				;
			return click(noBoard, pos);
		}
		return 0;
	}

}