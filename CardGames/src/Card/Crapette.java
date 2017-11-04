package Card;

import winApp.ContexteGlobal;

/**
 * 		Fonctionnement du jeu crapette
 */
public class Crapette extends CardGame {

	/**
	 * num�ro joueur 0 ou 1
	 */
	int tourJoueur;

	/* Structure de la table de d�cision du jeu
		 dim 1 = case de d�part de 8 � 21
		 dim 2 = case d'arriv�e de 0 � 21
	   contenu :
		 0 = mouvement interdit
		 1 = couleurs altern�es ou vide
		 2 = m�me couleur en descendant � partir de l'As
		 3 = m�me couleur en montant � partir de l'As
		 4 = m�me couleur dans les 2 sens
		 5 = libre
		 +8 = r�serv� au premier joueur
		 +16 = r�serv� au second joueur
	   organisation des cases :
		 1 � 4 tas montant
		 5 � 7 tas descendant
		 8 � 11 tas de manoeuvre avec couleurs altern�es
		 12 crapette joueur 1
		 13 talon joueur 1
		 14 �cart joueur 1
		 15 � 18 tas de manoeuvre avec couleurs altern�es
		 19 crapette joueur 2
		 20 talon joueur 2
		 21 �cart joueur 2
	*/

	/**
	 * table de d�cision du jeu
	 */
	int[][] tbJeu =	{
			// depuis tas de manoeuvre avec couleurs altern�es 8 � 11
			{ 3, 3, 3, 3, 2, 2, 2, 2, 0, 1, 1, 1, 16 + 4, 0, 16 + 4, 1, 1, 1, 1, 8 + 4, 0, 8 + 4 },
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 0, 1, 1, 16 + 4, 0, 16 + 4, 1, 1, 1, 1, 8 + 4, 0, 8 + 4 },
		  	{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 0, 1, 16 + 4, 0, 16 + 4, 1, 1, 1, 1, 8 + 4, 0, 8 + 4 },
		  	{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 0, 16 + 4, 0, 16 + 4, 1, 1, 1, 1, 8 + 4, 0, 8 + 4 },
		  	//  depuis crapette joueur 1
		  	{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 0, 1, 1, 1, 1, 8 + 4, 0, 8 + 4 },
		  	//  depuis talon joueur 1
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 0, 0, 8 + 5, 1, 1, 1, 1, 8 + 4, 0, 8 + 4 },
			//  depuis �cart joueur 1
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			//  depuis tas de manoeuvre avec couleurs altern�es 15 � 18
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 16 + 4, 0, 16 + 4, 0, 1, 1, 1, 8 + 4, 0, 8 + 4 },
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 16 + 4, 0, 16 + 4, 1, 0, 1, 1, 8 + 4, 0, 8 + 4 },
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 16 + 4, 0, 16 + 4, 1, 1, 0, 1, 8 + 4, 0, 8 + 4 },
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 16 + 4, 0, 16 + 4, 1, 1, 1, 0, 8 + 4, 0, 8 + 4 },
		  	//  depuis crapette joueur 2
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 16 + 4, 0, 16 + 4, 1, 1, 1, 1, 0, 0, 0 },
		  	//  depuis talon joueur 2
			{ 3, 3, 3, 3, 2, 2, 2, 2, 1, 1, 1, 1, 16 + 4, 0, 16 + 4, 1, 1, 1, 1, 0, 0, 16 + 5 },
			//  depuis �cart joueur 2
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			//   depuis ?
			{ 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }
		};

	/**
	 * 		Cr�ation du jeu de crapette
	 * 
	 *	 2 jeux de 52 cartes
	 *	 2 joueurs
	 *	 11 cases par joueur : 4 tas, 4 reconstitutions couleurs, crapette, talon, �cart
	 */
	public Crapette() {
		// 2 jeux de 52 cartes, 22 tas + le tas des jeux � distribuer
		super("crapette", 2, 52, 23, 22);
	}

	/**
	 *  distribution des cartes : 52 cartes par joueur
	 *  pour chaque joueur:
	 * 		4 cartes visibles en vertical,
	 *		13 cartes visible empil�es dans la crapette,
	 *		le reste du jeu au talon
	 */
	protected void distribuerCartes() {
		CardGamePanel cp = (CardGamePanel) ContexteGlobal.frame.panel;
		if ( cp.distributionAnim )  {	
				// initialisation de la distribution anim�e
			joueurDistrib = 1;		// joueur 1 ou 2
			autoCarteDistrib = 1;  	// phase de distribution 1=visible, 2=crapette, 3=talon, 0=fin
			cMax = 4;  				// nombre de cartes � distribuer pour cette phase
			autoCase = 8; 			// case � remplir
			retourne = true;  		// carte visible
			cp.distribution();
		} else {
			distribuerCartesInit();
		}
		
			// d�termination du premier joueur � jouer
			// c'est celui qui a la plus forte carte au somment de sa crapette
		
		if (carteRang2(b.boardVisible[19][0]) > carteRang2(b.boardVisible[12][0])) {
			tourJoueur = 1;
			messageFixe = "Second joueur";
			bStartJeuAuto = true;
		} else {
			tourJoueur = 0;
			messageFixe = "Premier joueur";
		}
		
	}

	/**
	 * distribution non anim�e
	 */
	protected void distribuerCartesInit() {
		for (int joueur = 1; joueur <= 2; joueur++) {
			int i, j;

			// d�termination du jeu � distrubuer
			if (b.nbCartesVisibles[8] == 1)
				i = 15;
			else
				i = 8;
			// case
			for (j = 0; j < 4; j++, i++) {
				b.boardVisible[i][0] = b.boardCache[tas][--b.nbCartesCaches[tas]];
				b.nbCartesVisibles[i]++;
			}
			// crapette
			for (j = 0; j < 13; j++) {
				b.boardVisible[i][j] = b.boardCache[tas][--b.nbCartesCaches[tas]];
				b.nbCartesVisibles[i]++;
			}
			// tas
			i++;
			for (j = 0; j < nbCartes - 17 ; j++) {
				b.boardCache[i][j] = b.boardCache[tas][--b.nbCartesCaches[tas]];
				b.nbCartesCaches[i]++;
			}
		}
	}

	/**
	 * distribution automatique
	 * @return CardMove ou null si aucun mouvement possible
	 */
	public CardMove distribAuto() {
		CardMove m = null;
		switch (autoCarteDistrib) {
		  case 0 :  // fin
			hist.clear();
			hist.historise(b);			  
			break;
		  case 1 :	// 4 cartes retourn�es
			m = new CardMove(tas, autoCase, 0, retourne);
			cMax--;
			autoCase++;
			if ( cMax == 0 ) {
				autoCarteDistrib++;
				cMax = 13;
			}
			break;
			
		  case 2 :  // 13 cartes retourn�es dans la crapette
			m = new CardMove(tas, autoCase, 0, retourne);
			cMax--;
			if ( cMax == 0 ) {
				autoCarteDistrib++;
				autoCase++;
				cMax = 52 - 4 - 13;
				retourne = false;
			}
			break;
			
		  case 3 :  // reste des cartes cach�es dans le talon
			m = new CardMove(tas, autoCase, 0, retourne);
			cMax--;
			if ( cMax == 0 ) {
				if ( joueurDistrib == 2 ) {
					autoCarteDistrib = 0;  // fin				
				} else {
					joueurDistrib = 2;		// joueur 1 ou 2
					autoCarteDistrib = 1;  	// phase de distribution 1=visible, 2=crapette, 3=talon, 0=fin
					cMax = 4;  				// nombre de cartes � distribuer pour cette phase
					autoCase = 15; 			// case � remplir
					retourne = true;  		// carte visible
				}
			}
			break;
		}
		return m;
	}
	
	/**
	 * r�ussite gagn�e quand la crapette, le talon et l'�cart du joueur sont vides
	 * @return true si gagn�e
	 */
	public boolean gagne() {
		for (int i = 12 + tourJoueur * 7, j = 0; j < 3; i++, j++) {
			if (b.nbCartesCaches[i] + b.nbCartesVisibles[i] > 0)
				return false;
		}
		return true;
	}

	/**
	 * dynamique du jeu : click sur une carte d'une case du board
	 * @return -1 en cas d'erreur avec le message dans Message
	 * @return 1 en cas de gain
	 * @return 0 si le click est accept�
	 */
	public int click(int noBoard, int nbDragCard) {
		Message = "";

		// premier click

		if (noClick()) {
			if (noBoard <= 7 || noBoard == 14 || noBoard == 21 || tourJoueur == 0 && (noBoard == 19 || noBoard == 20)
					|| tourJoueur == 1 && (noBoard == 12 || noBoard == 13)) {
				// interdiction de prendre des cartes sur ces tas
				Message = "mess2";
				return -1;
			} else if (retourneTalon(noBoard)) {
				return 0;
			}
			prevClick = noBoard;
			return 0;
		}

		// second click

		if (!moveCard(prevClick, noBoard)) {
			Message = "mess1";
			clearClick();
			return -1;
		}

		// changement de joueur

		if (tourJoueur == 0 && noBoard == 14 && prevClick == 13) {
			tourJoueur = 1;
			messageFixe = "Second joueur";
			bStartJeuAuto = true;
		} else if (tourJoueur == 1 && noBoard == 21 && prevClick == 20) {
			tourJoueur = 0;
			messageFixe = "Premier joueur";
		}

		clearClick();

		// test partie finie

		if (gagne()) {
			Message = "mess4";
			return 1;
		}

		return 0;
	}

	/**
	 * jeu automatique
	 * @return CardMove ou null si aucun mouvement possible
	 */
	public CardMove jeuAuto() {
		CardMove m = new CardMove();
		int s, r;

		// arr�t jeu auto sur premier joueur

		if (tourJoueur == 0) {
			return null;
		}

		// recherche mouvement manoeuvre,crapette,talon vers tas
		// les As sont plac�s sur les tas montants en premier (� am�liorer)

		for (s = 8; s < 21; s++) // source
		{
			for (r = 0; r < 8; r++) // tas
			{
				if (testAutoMoveCarte(m, s, r))
					return m;
			}
		}

		// recherche mouvement manoeuvre, crapette et talon vers crapette et
		// talon adverse

		for (s = 8; s < 21; s++) // source
		{
			if (tourJoueur == 0 && testAutoMoveCarte(m, s, 19))
				return m;
			if (tourJoueur == 0 && testAutoMoveCarte(m, s, 21))
				return m;
			if (tourJoueur == 1 && testAutoMoveCarte(m, s, 12))
				return m;
			if (tourJoueur == 1 && testAutoMoveCarte(m, s, 14))
				return m;
		}

		// recherche mouvement manoeuvre vers manoeuvre pour lib�rer des zones
		// de manoeuvre
		// la zone r�ceptrice devant contenir plus de cartes que la zone
		// �metrice

		for (s = 8; s < 19; s++) // source manoeuvre
		{
			if (s == 12)
				s = 15;
			for (r = 8; r < 19; r++) // manoeuvre
			{
				if (r == 12)
					r = 15;
				if (b.nbCartesVisibles[r] > b.nbCartesVisibles[s] && testAutoMoveCarte(m, s, r))
					return m;
			}
		}

		// recherche mouvement crapette vers manoeuvre

		s = tourJoueur == 0 ? 12 : 19;
		for (r = 8; r < 19; r++) // manoeuvre
		{
			if (r == 12)
				r = 15;
			if (testAutoMoveCarte(m, s, r))
				return m;
		}

		// recherche mouvement talon vers manoeuvre

		s = tourJoueur == 0 ? 13 : 20;
		for (r = 8; r < 19; r++) // manoeuvre
		{
			if (r == 12)
				r = 15;
			if (testAutoMoveCarte(m, s, r))
				return m;
		}

		// recherche mouvement manoeuvre vers manoeuvre dans le but de
		// d�couvrir des cartes � mettre sur les tas, crapette et talon adverse
		// � �tudier
		/*
		 * for( s=8; s<19; s++ ) // source manoeuvre { if ( s == 12 ) s = 15; for ( r=8;
		 * r<19; r++ ) // manoeuvre { if ( r == 12 ) r = 15; if (
		 * testAutoMoveCarte(m,s,r) ) return m; } }
		 */

		// retourner une carte sur le talon
		s = tourJoueur == 0 ? 13 : 20;
		if (testRetourneTalon(s)) {
			m.fromBoard = s;
			m.noCard = 0;
			m.toBoard = s;
			return m;
		}

		// �carter la carte sur le talon pour changer de joueur

		m.fromBoard = s;
		m.noCard = 0;
		m.toBoard = s + 1;
		return m;
	}


	/**
	 * teste si des cartes de s peuvent �tre d�plac�es vers r
	 * et si oui construit le mouvement
	 * @param m
	 * @param s
	 * @param r
	 * @return faux si mouvement impossible
	 */
	private boolean testAutoMoveCarte(CardMove m, int s, int r) {
		int cs = testMoveCard(s, r);
		if (cs >= 0) {
			m.fromBoard = s;
			m.noCard = cs;
			m.toBoard = r;
			if (!boucle(m))
				return true;
		}
		return false;
	}

	/**
	 * Teste le mouvement des cartes
	 * @param fromBoard
	 * @param toBoard
	 * @return l'index de la carte � d�placer
	 */
	private int testMoveCard(int fromBoard, int toBoard) {
		if (fromBoard < 8 || fromBoard == 14 || fromBoard == 21
				|| tourJoueur == 0 && (fromBoard == 19 || fromBoard == 20)
				|| tourJoueur == 1 && (fromBoard == 12 || fromBoard == 13))
			return -1;

		int cd = tbJeu[fromBoard - 8][toBoard];

		if (cd == 0)
			return -1;
		if (cd > 16) {
			if (tourJoueur == 0)
				return -1;
			else
				cd -= 16;
		}
		if (cd > 8) {
			if (tourJoueur == 1)
				return -1;
			else
				cd -= 8;
		}

		int carteObj = -1;
		if (b.nbCartesVisibles[toBoard] > 0) // case r�ceptrice non vide
		{
			carteObj = b.boardVisible[toBoard][b.nbCartesVisibles[toBoard] - 1];
		}
		if (b.nbCartesVisibles[fromBoard] == 0) // case �metrice vide
			return -1;
		int cs = b.nbCartesVisibles[fromBoard] - 1;
		int carteSrc = b.boardVisible[fromBoard][cs];

		// libre

		if (cd == 5) {
			return cs;
		}

		// couleurs altern�es

		else if (cd == 1 && (carteObj < 0
				|| carteNoire(carteObj) != carteNoire(carteSrc) && carteRang(carteObj) == carteRang(carteSrc) + 1)) {
			return cs;
		}
		// mise en place des As

		else if ((cd == 2 || cd == 3) && b.nbCartesVisibles[toBoard] == 0 && carteRang(carteSrc) == 0
				&& carteCouleur(carteSrc) == toBoard % 4) {
			return cs;
		}
		// m�me couleur en descendant

		else if ((cd == 2 || cd == 4) && b.nbCartesVisibles[toBoard] > 0
				&& carteCouleur(carteObj) == carteCouleur(carteSrc)
				&& carteRang2(carteObj) == carteRang2(carteSrc) + 1) {
			return cs;
		}
		// m�me couleur en montant

		else if ((cd == 3 || cd == 4) && b.nbCartesVisibles[toBoard] > 0
				&& carteCouleur(carteObj) == carteCouleur(carteSrc) && carteRang(carteObj) == carteRang(carteSrc) - 1) {
			return cs;
		}
		return -1;
	}

	/**
	 * Mouvement cartes apr�s test
	 * @param fromBoard
	 * @param toBoard
	 * @return faux si mouvement impossible
	 */
	private boolean moveCard(int fromBoard, int toBoard) {
		int cs = testMoveCard(fromBoard, toBoard);
		if (cs >= 0) {
			for (int i = cs; i < b.nbCartesVisibles[fromBoard]; i++) {
				b.boardVisible[toBoard][b.nbCartesVisibles[toBoard]++] = b.boardVisible[fromBoard][i];
			}
			b.nbCartesVisibles[fromBoard] = cs;
			hist.historise(b);
			return true;
		}
		return false;
	}

	/**
	 * Actions sur le talon: retourner 1 carte ou le talon
	 * @param i
	 * @return faux si impossible
	 */
	private boolean retourneTalon(int i) {
		if (testRetourneTalon(i)) {
			if (DecouvrirCarte(i) == false) {
				while (b.nbCartesVisibles[i + 1] > 0) {
					b.boardCache[i][b.nbCartesCaches[i]++] = b.boardVisible[i + 1][--b.nbCartesVisibles[i + 1]];
				}
			}
			hist.historise(b);
			return true;
		}
		return false;
	}

	/**
	 * Test si action possible sur le talon
	 * @param i
	 * @return faux si impossible
	 */
	private boolean testRetourneTalon(int i) {
		int s = tourJoueur == 0 ? 13 : 20;
		if (i == s && (b.nbCartesCaches[s] + b.nbCartesVisibles[s] == 0 || b.nbCartesVisibles[s] == 0)) {
			return true;
		}
		return false;
	}

}