package Card;

import winApp.ContexteGlobal;

/**
 * 		Fonctionnement du jeu solitaire
 */
public class Solitaire extends CardGame {

	/**
	 * 		Création du jeu
	 * 
	 *	 un jeu de 52 cartes
	 *	 13 cases : 7 tas, 4 reconstitutions couleurs, 2 pour le talon
	 *	 talon : cases 11 et 12
	 */
	public Solitaire() {
		super("solitaire", 1, 52, 13, 11);
	}

	/**
	 *  distribution des cartes dans les 7 cases de 0 à 6
	 * une carte visible dans la première, carte retournée dans les suivantes
	 * au second tour on recommence en commençant par la seconde case
	 * etc. pour les tours suivants
	 */
	protected void distribuerCartes() {
		CardGamePanel cp = (CardGamePanel) ContexteGlobal.frame.panel;
			// initialisation de la distribution animée
		if ( cp.distributionAnim )  {
			cMax = 7+6+5+4+3+2+1; 	// nombre de cartes à distribuer
			autoCarteDistrib = 0;	// nombre de cartes distribuées
			autoCase = 0;			// première case à remplir
			autoCaseDeb = 0;		// case début de remplissage évolue d'un à chaque tour
			retourne = true;		// carte visible
			cp.distribution();
		} else {
			distribuerCartesInit();
		}
		// le talon dans la case 11 contient les cartes restantes
	}

	/**
	 *  distribution non animée
	 */
	protected void distribuerCartesInit() {

		// distribuer les cartes dans les 7 tas de 0 à 6

		for (int i = 0; i < 7; i++)
			// carte
			for (int j = i; j < 7; j++)	{
				// case
				b.boardCache[j][i] = b.boardCache[tas][--b.nbCartesCaches[tas]];
				b.nbCartesCaches[j]++;
			}

		// découvrir les cartes du dessus des tas

		for (int i = 0; i < 7; i++) {
			DecouvrirCarte(i);
		}
	}

	/**
	 * jeu automatique
	 * @return CardMove ou null si aucun mouvement possible
	 */
	public CardMove distribAuto() {
		// test de fin
		if (autoCarteDistrib == cMax) {
			hist.clear();
			hist.historise(b);
			return null;
		}
		
		// mouvement
		CardMove m = new CardMove(tas, autoCase, 0, retourne);

		// préparation du mouvement suivant
		autoCarteDistrib++;
		if (autoCase == 6)  {
			autoCaseDeb++;
			autoCase = autoCaseDeb;
			retourne = true;
		}  else  {
			autoCase++;
			retourne = false;
		}
		return m;
	}

	// réussite gagnée quand il n'y a plus de carte au talon et dans les tas

	/**
	 * réussite gagnée quand il n'y a plus de carte au talon et dans les tas
	 * @return true si partie gagnée
	 */
	public boolean gagne() {
		for (int i = 0; i < b.length(); i++) {
			if ((i < 7 || i > 10) && b.nbCartesCaches[i] + b.nbCartesVisibles[i] > 0)
				return false;
		}
		return true;
	}

	/**
	 * dynamique du jeu : click sur une case du board
	 * @return -1 en cas d'erreur avec le message dans Message
	 * @return 1 en cas de gain
	 * @return 0 si le click est accepté
	 */
	public int click(int noBoard, int noCard) {
		// System.out.print("Position no "+Integer.toString(noBoard));
		// System.out.println(",caches="+Integer.toString(nbCartesCaches[noBoard])+",visibles="+Integer.toString(nbCartesVisibles[noBoard]));

		Message = "";
		
		// Talon caché -> on retourne 3 cartes
		// s'il est vide on retourne le talon exposé
		
		if (noBoard == 11)	{
			retourneTalon();
			clearClick();
		} else if (noBoard == 12)	{
			 // Talon exposé
			if (noClick() == false) {
				clearClick();
				Message = "mess1";
				return -1;
			}
			prevClick = 12;
		} else if (noBoard >= 0 && noBoard <= 6) {
			// sélection colonne		
			if (noClick()) {
				if (b.nbCartesVisibles[noBoard] > 0) { 
					// colonne source
					prevClick = noBoard;
				} else {
					// colonne source vide
					Message = "mess2";
					return -1;
				}
			} else if (prevClick == noBoard) {
				clearClick(); // annulation
			} else {
				// mouvement colonne-colonne ou Talon-colonne
				if (moveCard(prevClick, noBoard) < 0) {
					Message = "mess1";
					clearClick();
					return -1;
				}
				clearClick();
			}
		} else if (noBoard >= 7 && noBoard <= 10)  {
			// tas de cartes reconstitués par couleur
			if (noClick()) {
				Message = "mess3";
				return -1;
			}
			if (moveCard(prevClick, noBoard) < 0) {
				Message = "mess1";
				clearClick();
				return -1;
			}
			clearClick();
		}
		if (gagne()) {
			Message = "mess4";
			return 1;
		}
		return 0;
	}

	/**
	 * mouvement cartes
	 * @param fromBoard
	 * @param toBoard
	 * @return -1 si mouvement impossible
	 */
	private int moveCard(int fromBoard, int toBoard) {
		
			// mouvement colonne-colonne ou Talon-colonne

		if ((fromBoard >= 0 && fromBoard <= 6 || fromBoard == 12) && (toBoard >= 0 && toBoard <= 6)) {
			int carteInd;
			if (b.nbCartesVisibles[toBoard] > 0)  {
				// colonne réceptrice non vide
				int carteObj = b.boardVisible[toBoard][b.nbCartesVisibles[toBoard] - 1];
				for (carteInd = b.nbCartesVisibles[fromBoard] - 1; carteInd >= 0; carteInd--) {
					int carteSrc = b.boardVisible[fromBoard][carteInd];
					if (carteNoire(carteObj) != carteNoire(carteSrc)
							&& carteRang(carteObj) == carteRang(carteSrc) + 1) {
						for (int i = carteInd; i < b.nbCartesVisibles[fromBoard]; i++)
							b.boardVisible[toBoard][b.nbCartesVisibles[toBoard]++] = b.boardVisible[fromBoard][i];
						b.nbCartesVisibles[fromBoard] = carteInd;
						break;
					}
					if (fromBoard == 12) // pas d'itération sur le talon
						return -1;
				}
				if (carteInd < 0)
					return -1;
			} else {
				// colonne réceptrice vide			
				for (carteInd = b.nbCartesVisibles[fromBoard] - 1; carteInd >= 0; carteInd--) {
					int carteSrc = b.boardVisible[fromBoard][carteInd];
					if (carteRang(carteSrc) == 12) // recherche Roi dans source
					{
						for (int i = carteInd; i < b.nbCartesVisibles[fromBoard]; i++) {
							b.boardVisible[toBoard][b.nbCartesVisibles[toBoard]++] = b.boardVisible[fromBoard][i];
						}
						b.nbCartesVisibles[fromBoard] = carteInd;
						break;
					}
					if (fromBoard == 12) // pas d'itération sur le talon
						break;
				}
				if (carteInd < 0)
					return -1;
			}
		}

		// mouvement colonne-tas ou Talon-tas

		else if ((fromBoard >= 0 && fromBoard <= 6 || fromBoard == 12) && toBoard >= 7 && toBoard <= 10) {
			int carteCol = b.boardVisible[fromBoard][b.nbCartesVisibles[fromBoard] - 1];
			int carteTas = -1;
			if (b.nbCartesVisibles[toBoard] > 0)
				carteTas = b.boardVisible[toBoard][b.nbCartesVisibles[toBoard] - 1];
			if ((carteTas < 0 && carteRang(carteCol) == 0)
					|| (carteTas >= 0 && carteCouleur(carteCol) == carteCouleur(carteTas)
							&& carteRang(carteCol) == carteRang(carteTas) + 1)) {
				b.boardVisible[toBoard][b.nbCartesVisibles[toBoard]++] = b.boardVisible[fromBoard][--b.nbCartesVisibles[fromBoard]];
			} else
				return -1;
		} else
			return -1;

		if (fromBoard <= 6 && b.nbCartesVisibles[fromBoard] == 0)
			DecouvrirCarte(fromBoard);

		hist.historise(b);
		return 0;
	}

	/**
	 * actions sur le talon: retourner 3 cartes ou le talon
	 */
	public void retourneTalon() {
		if (b.nbCartesCaches[11] > 0) {
			for (int i = 0; i < 3; i++) {
				if (b.nbCartesCaches[11] == 0)
					break;
				b.boardVisible[12][b.nbCartesVisibles[12]++] = b.boardCache[11][--b.nbCartesCaches[11]];
			}
		} else {
			while (b.nbCartesVisibles[12] > 0) {
				b.boardCache[11][b.nbCartesCaches[11]++] = b.boardVisible[12][--b.nbCartesVisibles[12]];
			}
		}
		hist.historise(b);
	}

	/**
	 * jeu automatique
	 * @return CardMove ou null jeu bloqué
	 */
	public CardMove jeuAuto() {
		int s, cs, crts, r, crtr;

		// recherche mouvement colonne vers tas

		for (s = 0; s < 7; s++) {
			// colonne source
			if (b.nbCartesVisibles[s] > 0) {
				cs = b.nbCartesVisibles[s] - 1;
				crts = b.boardVisible[s][cs];
				for (r = 7; r < 11; r++) // tas
				{
					crtr = -1;
					if (b.nbCartesVisibles[r] > 0)
						crtr = b.boardVisible[r][b.nbCartesVisibles[r] - 1];
					if ((crtr < 0 && carteRang(crts) == 0) || (crtr >= 0 && carteCouleur(crts) == carteCouleur(crtr)
							&& carteRang(crts) == carteRang(crtr) + 1)) {
						CardMove m = new CardMove(s, r, cs, false);
						if (!boucle(m))
							return m;
					}
				}
			}
		}
		
		// recherche mouvement talon vers tas

		s = 12;
		if (b.nbCartesVisibles[s] > 0) {
			cs = b.nbCartesVisibles[s] - 1;
			crts = b.boardVisible[s][cs];
			for (r = 7; r < 11; r++) // tas
			{
				crtr = -1;
				if (b.nbCartesVisibles[r] > 0)
					crtr = b.boardVisible[r][b.nbCartesVisibles[r] - 1];
				if ((crtr < 0 && carteRang(crts) == 0) || (crtr >= 0 && carteCouleur(crts) == carteCouleur(crtr)
						&& carteRang(crts) == carteRang(crtr) + 1)) {
					CardMove m = new CardMove(s, r, cs, false);
					if (!boucle(m))
						return m;
				}
			}
		}

		// recherche mouvement entre colonnes

		for (s = 0; s < 7; s++) // colonne source
		{
			for (cs = 0; cs < b.nbCartesVisibles[s]; cs++) // carte
			{
				crts = b.boardVisible[s][cs];
				if (carteRang(crts) == 0)
					break; // dégager les As en priorité vers les tas
				if (carteRang(crts) == 12 && b.nbCartesCaches[s] == 0)
					// ne pas déplacer un Roi qui est déja sur une colonne vide
					continue;
				for (r = 0; r < 7; r++) // colonne réceptrice
				{
					if (r != s && b.nbCartesVisibles[r] > 0) {
						crtr = b.boardVisible[r][b.nbCartesVisibles[r] - 1];
						if (carteNoire(crts) != carteNoire(crtr) && carteRang(crtr) == carteRang(crts) + 1) {
							CardMove m = new CardMove(s, r, cs, false);
							if (!boucle(m))
								return m;
						}
					} else if (b.nbCartesVisibles[r] == 0) {
						if (carteRang(crts) == 12) {
							// mettre un Roi sur une colonne vide
							CardMove m = new CardMove(s, r, cs, false);
							if (!boucle(m))
								return m;
						}
					}
				}
			}
		}

		// recherche mouvement talon vers colonne

		s = 12;
		if (b.nbCartesVisibles[s] > 0)	{
			// carte
			cs = b.nbCartesVisibles[s] - 1;
			crts = b.boardVisible[s][cs];
			for (r = 0; r < 7; r++)	{
				// colonne réceptrice
				if (r != s && b.nbCartesVisibles[r] > 0) {
					crtr = b.boardVisible[r][b.nbCartesVisibles[r] - 1];
					if (carteNoire(crts) != carteNoire(crtr) && carteRang(crtr) == carteRang(crts) + 1) {
						CardMove m = new CardMove(s, r, cs, false);
						if (!boucle(m))
							return m;
					}
				} else if (b.nbCartesVisibles[r] == 0) {
					if (carteRang(crts) == 12) {
						CardMove m = new CardMove(s, r, cs, false);
						if (!boucle(m))
							return m;
					}
				}
			}
		}

		// talon

		if (b.nbCartesCaches[11] > 0 || b.nbCartesVisibles[12] > 0) {
			CardMove m = new CardMove(11, 11, 0, false);
			if (!boucle(m))
				return m;
		}

		return null;
	}

}