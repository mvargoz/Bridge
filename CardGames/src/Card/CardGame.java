package Card;

import winApp.ContexteGlobal;

/**
 * jeu sous forme abstraite
 *
 */
public abstract class CardGame {
	
	/**
	 * nom du jeux
	 */
	protected String nom;
	/**
	 * nombre de jeux
	 */
	protected int nbJeux;	
	/**
	 * nombre de cartes par jeu
	 */
	protected int nbCartes;	
	/**
	 * dimension du plateau de jeu
	 */
	protected int dimBoard;
	/**
	 * nombre de cartes par tas
	 */
	protected int nbCartesBoard;
	/**
	 * message au joueur
	 */
	protected String Message;
	/**
	 * message fixe au joueur
	 */
	protected String messageFixe = "";
	/**
	 * repr�sentation interne du d�roulement du jeu
	 */
	protected CardBoard b;
	/**
	 * jeu automaatique
	 */
	protected boolean bStartJeuAuto = false;
	/**
	 * case o� sont mis les jeux au d�part si >= 0
	 */
	protected int tas;
	/**
	 * repr�sentation interne des jeux de cartes
	 */
	protected int[] jeuxCartes;
	/**
	 * clic pr�c�dent
	 */
	protected int prevClick;
	/**
	 * historique des coups pour undo
	 */
	protected CardBoardHisto hist = new CardBoardHisto();

		// donn�es de l'automate d'animation de la distribution
	
	/**
	 * nombre de cartes � distribuer
	 */
	protected int cMax;
	/**
	 * premi�re case � distribuer
	 */
	protected int autoCaseDeb;
	/**
	 * case � distribuer
	 */
	protected int autoCase;
	/**
	 * nombre de cartes distribu�e
	 */
	protected int autoCarteDistrib;
	/**
	 * retourner la carte
	 */
	protected boolean retourne;
	/**
	 * joueur
	 */
	protected int joueurDistrib;

	/**
	 * constructeur
	 * @param nom
	 * @param nbJeux
	 * @param nbCartes
	 * @param dimBoard
	 * @param tas
	 */
	public CardGame(String nom, int nbJeux, int nbCartes, int dimBoard, int tas) {
		this.nom = nom;
		this.nbJeux = nbJeux;
		this.nbCartes = nbCartes;
		this.dimBoard = dimBoard;
		this.tas = tas;
		this.nbCartesBoard = 110;
		jeuxCartes = new int[nbJeux * nbCartes];
		b = new CardBoard(dimBoard, nbCartesBoard);
	}

	/**
	 * initialisation
	 */
	public void init() {
		b.init();
		battreCartes();
		distribuerCartes();
		clearClick();
		CardGamePanel cp = (CardGamePanel) ContexteGlobal.frame.panel;
		if ( !cp.distributionAnim )  {
			hist.clear();
			hist.historise(b);
		}
	}

	/**
	 * brassage d'un jeu de cartes
	 */
	private void battreCartes() {
		int i, j, k, l, c = 0;

		// constitution des jeux

		for (i = 0; i < nbJeux; i++)
			for (j = 0; j < nbCartes; j++)
				jeuxCartes[c++] = j % 52;

		// battre les jeux

		for (i = 0; i < 1000; i++) {
			j = (int) (java.lang.Math.random() * nbJeux * nbCartes);
			k = (int) (java.lang.Math.random() * nbJeux * nbCartes);
			l = jeuxCartes[j];
			jeuxCartes[j] = jeuxCartes[k];
			jeuxCartes[k] = l;
		}

		// mettre les cartes dans le tas face cach�e

		if (tas >= 0) {
			for (i = 0; i < nbJeux * nbCartes; i++) {
				b.boardCache[tas][i] = jeuxCartes[i];
				b.nbCartesCaches[tas]++;
			}
		}
	}

	/**
	 * d�couvrir la premi�re carte d'un tas
	 * @param i = tas
	 * @return true si tas non vide
	 */
	public boolean DecouvrirCarte(int i) {
		if (b.nbCartesCaches[i] > 0) {
			b.boardVisible[i][b.nbCartesVisibles[i]++] = b.boardCache[i][--b.nbCartesCaches[i]];
			return true;
		}
		return false;
	}

	/**
	 * couleur 0 � 4 pour : T, K, C, P
	 * @param carte
	 * @return
	 */
	protected int carteCouleur(int i) {
		return i / 13;
	}

	/**
	 * rang d'une carte 0 � 12 pour As,2,3,...,D,R
	 * @param carte
	 * @return rang
	 */
	protected int carteRang(int i) {
		int rang = i % 13 + 1;
		if (rang == 13)
			rang = 0;
		return rang;
	}

	/**
	 * rang d'une carte 0 � 12 pour 2,3,...,D,R,As
	 * @param carte
	 * @return rang
	 */
	protected int carteRang2(int i) {
		int rang = i % 13;
		return rang;
	}

	/**
	 * teste couleur noire
	 * @param i
	 * @return true si T ou P
	 */
	protected boolean carteNoire(int i) {
		int j = carteCouleur(i);
		if (j == 0 || j == 3)
			return true;
		return false;
	}

	/**
	 * undo
	 */
	public void undo() {
		b = hist.restore();
		if (b == null) {
			System.out.println("undo impossible");
			return;
		}
	}

	/**
	 * Simulation du mouvement pour d�terminer
	 *  s'il provoque une boucle
	 *  (position d�j� rencontr�e dans l'historique)
	 * @param mouvement
	 * @return true si position d�j� rencontr�e dans l'historique
	 */
	protected boolean boucle(CardMove m) {
			//	Simulation du click sur la carte de d�part
		if (click(m.fromBoard, m.noCard) < 0) {
			System.out.println("erreur automate sur " + Integer.toString(m.fromBoard));
			return true;
		}
			//	Simulation du click sur la carte d'arriv�e
		if (m.fromBoard != m.toBoard)
			if (click(m.toBoard, m.noCard) < 0) {
				System.out.println("erreur automate sur " + Integer.toString(m.fromBoard) + " vers "
						+ Integer.toString(m.toBoard));
				return true;
			}
			// m�morisation de la position d'arriv�e pr�vue
		CardBoard bPrevu = (CardBoard) b.clone();
		b = hist.restore();
			// teste si cette position existe d�j� dans l'historique
		if (hist.contains(bPrevu))
			return true;
		return false;
	}

	/**
	 * r�initialise click souris
	 */
	public void clearClick() {
		prevClick = -1;
	}

	/**
	 * teste si clic avant
	 * @return true si pas de click avant
	 */
	public boolean noClick() {
		return (prevClick == -1);
	}

	// m�thodes � impl�menter pour chaque jeu

	/**
	 * distribution
	 */
	protected abstract void distribuerCartes();

	/**
	 * traitement du clic souris
	 * @param noBoard
	 * @param noCard
	 * @return
	 */
	public abstract int click(int noBoard, int noCard);

	/**
	 * jeu automatique
	 * @return
	 */
	public abstract CardMove jeuAuto();

	/**
	 * distribution anim�e
	 * @return
	 */
	public abstract CardMove distribAuto();

	/**
	 * teste si gain
	 * @return true si partie gagn�e
	 */
	public abstract boolean gagne();

}
