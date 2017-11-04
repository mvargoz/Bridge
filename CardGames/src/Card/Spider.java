package Card;

import winApp.ContexteGlobal;

/**
 * 		Fonctionnement du jeu spider
 */
public class Spider extends CardGame {
	
			// paramétrage du jeu
	
	/**
	 * nombre de couleurs
	 */
	private int nbColors = 4;	
	/**
	 * nombre de cartes au talon
	 */
	private int nbCartesTalon = 50;	
	/**
	 * dégagement des couleurs reconstituées sur les tas
	 */
	private boolean bReconst = true;	
	/**
	 * pouvoir mettre le Roi sur l'As
	 */	 
	private boolean bRoiSurAs = false;	
	/**
	 *  nombre de couleurs reconstituées
	 */
	private int nbCouleursReconst = 0;

	/**
	 *  création du jeu:
	 *		deux jeux de 52 cartes
	 *		19 tas de cartes
	 * 		la case 10 contient le talon
	 *		les cases 0 à 9 contiennent les tas
	 *		les cases 11 à 18 contiennent les jeux reconstitués
	 *@param nbColors
	 *@param bReconst
	 *@param bRoiSurAs
	 */
	public Spider(int nbColors, boolean bReconst, boolean bRoiSurAs) {
		
		// 2 jeux si 4 couleurs, 4 si 2 et 8 si 1
		// 19 cases : 10 tas, 8 couleurs reconstituées, 1 talon 
		// la case 10 contient le talon de 50 cartes

		super("spider", 8/nbColors, 13*nbColors, 19, 10);
		
		this.nbColors = nbColors;
		this.bReconst = bReconst;
		this.bRoiSurAs = bRoiSurAs;
	}

	/**
	 * Distribution des cartes
	 * distribuer 54 cartes dans les 10 tas de 0 à 9
	 * la dernière carte distribuée sur le tas est visible
	 * 50 cartes restent au talon
	 */
	protected void distribuerCartes() {
		CardGamePanel cp = (CardGamePanel) ContexteGlobal.frame.panel;
		if ( cp.distributionAnim )  {	
				// initialisation de la distribution animée
			cMax = nbJeux * 13 * nbColors - nbCartesTalon;
			autoCase = 0;
			autoCarteDistrib = 0;
			retourne = false;
			cp.distribution();
		} else {
			distribuerCartesInit();
		}

		// les cases 11 à 18 recevront les couleurs complètes de l'As au Roi

		nbCouleursReconst = 0;
	}

	/**
	 * 	distribution non animée
	 */
	protected void distribuerCartesInit() {
		int cMax = nbJeux * 13 * nbColors - nbCartesTalon;

		// distribuer les cartes dans les 10 tas de 0 à 9

		for (int c = 0, i = 0; c < cMax; i++) {
			for (int j = 0; j < 10 && c < cMax; j++, c++) {
				b.boardCache[j][i] = b.boardCache[tas][--b.nbCartesCaches[tas]];
				b.nbCartesCaches[j]++;
			}
		}

		// découvrir les cartes du dessus des tas

		for (int i = 0; i < 10; i++)
			DecouvrirCarte(i);
	}

	/**
	 * Distribution animée
	 * @return CardMove
	 */
	public CardMove distribAuto() {
		if (autoCarteDistrib == cMax) {
			hist.clear();
			hist.historise(b);
			return null;
		}

		if (autoCarteDistrib == cMax - 10)
			retourne = true;

		CardMove m = new CardMove(tas, autoCase, 0, retourne);

		autoCarteDistrib++;
		if (autoCase == 9)
			autoCase = 0;
		else
			autoCase++;

		return m;
	}

	/**
	 * Réussite gagnée quand les 8 couleurs ont été reconstituées
	 * @return true si gagnée
	 */
	public boolean gagne() {
		if (nbCouleursReconst < nbJeux * nbColors) {
			return false;
		}
		return true;
	}

	/**
	 * Dynamique du jeu : click sur une carte d'une case du board
	 * @return -1 en cas d'erreur avec le message dans Message
	 * @return 1 en cas de gain
	 * @return 0 si le click est accepté
	 */
	public int click(int noBoard, int noCard) {
		// System.out.print("click case "+noBoard + " carte " + noCard);
		// System.out.println(",caches="+Integer.toString(b.nbCartesCaches[noBoard])+",visibles="+Integer.toString(b.nbCartesVisibles[noBoard]));

		Message = "";
		if (noBoard == 10)  {
			// Talon caché -> on distribue une carte par tas
			// rien s'il est vide	
			clearClick();
			if ( distribTalon() )   {
				return 0;
			} else {
				Message = "mess11";
				return -1;
			}

		} else if (noBoard >= 0 && noBoard <= 9)   {
			// sélection colonne		
			if (noClick()) {
				if (b.nbCartesVisibles[noBoard] > 0) {
					prevClick = noBoard; // colonne source
				} else {
					// colonne source vide
					Message = "mess2";
					return -1;
				}
			} else if (prevClick == noBoard) {
				clearClick(); // annulation
			}  else  {
				// mouvement colonne-colonne			
				if (moveCard(prevClick, noCard, noBoard) < 0) {
					Message = "mess1";
					clearClick();
					return -1;
				}
				clearClick();
			}
		}

		// test gain réussite

		if (gagne()) {
			Message = "mess4";
			return 1;
		}
		return 0;
	}

	/**
	 * 		 mouvement cartes
	 * @param fromBoard
	 * @param noCard
	 * @param toBoard
	 * @return -1 si erreur, 0 sinon
	 */
	private int moveCard(int fromBoard, int noCard, int toBoard) {
		int carteCouleur;
		int carteRang;
		int carte;

		// mouvement colonne-colonne

		if ( fromBoard >= 0 && fromBoard <= 9 &&
			 toBoard >= 0 && toBoard <= 9   &&
			 toBoard != fromBoard	)			{

			// vérification que le bloc de cartes à déplacer est
			// une suite descendante de même couleur

			carteCouleur = carteCouleur(b.boardVisible[fromBoard][noCard]);
			carteRang = carteRang(b.boardVisible[fromBoard][noCard]);
			for (int i = noCard + 1; i < b.nbCartesVisibles[fromBoard]; i++) {
				carteRang--;
				if ( carteCouleur(b.boardVisible[fromBoard][i]) != carteCouleur ||
					 carteRang(b.boardVisible[fromBoard][i]) != carteRang)
					return -1;
			}

			// contrôle que le bloc déplacé va à une place vide ou
			// sous une carte de rang juste supérieur
			// ou Roi sur As si autorisé

			if (b.nbCartesVisibles[toBoard] > 0) {
				int carteObj = b.boardVisible[toBoard][b.nbCartesVisibles[toBoard] - 1];
				int carteSrc = b.boardVisible[fromBoard][noCard];
					// Roi sur As si autorisé ou sur carte de rang juste supérieur
				if ( !( bRoiSurAs && carteRang(carteObj) == 0 && carteRang(carteSrc) == 12  ||
						carteRang(carteObj) == carteRang(carteSrc) + 1 ) )
					return -1;
			}

			// déplacement

			for (int i = noCard; i < b.nbCartesVisibles[fromBoard]; i++)
				b.boardVisible[toBoard][b.nbCartesVisibles[toBoard]++] = b.boardVisible[fromBoard][i];
			b.nbCartesVisibles[fromBoard] = noCard;
		} else {
			 // mouvement invalide: même case ou case non 0-9
			return -1;
		}

		// découvrir une nouvelle carte

		if (b.nbCartesVisibles[fromBoard] == 0)
			DecouvrirCarte(fromBoard);

		if (bReconst) {
			// Ranger les suites complètes dans les cases 11 à 18
			// Exploration de la colonne dans laquelle on vient d'ajouter des cartes
			// il faut trouver 13 cartes de la même couleur rangées du Rois à l'As

			for (carte = 0; carte < b.nbCartesVisibles[toBoard] - 12; carte++) {
				carteCouleur = carteCouleur(b.boardVisible[toBoard][carte]);
				carteRang = carteRang(b.boardVisible[toBoard][carte]);
				if (carteRang == 12)  {
					// recherche d'un Roi			
					boolean sequence = true;
					for (int i = 1; i < 13; i++) {
						if (carteCouleur != carteCouleur(b.boardVisible[toBoard][carte + i])
								|| carteRang(b.boardVisible[toBoard][carte + i]) != 12 - i) {
							sequence = false;
							break;
						}
					}
					if (sequence) {
						// stockage de la séquence
						for (int i = 0; i < 13; i++) {
							b.boardVisible[11 + nbCouleursReconst][b.nbCartesVisibles[11
									+ nbCouleursReconst]++] = b.boardVisible[toBoard][carte + i];
							if (b.nbCartesVisibles[toBoard] > carte + i + 13)
								b.boardVisible[toBoard][carte + i] = b.boardVisible[toBoard][carte + i + 13];
						}
						nbCouleursReconst++;
						b.nbCartesVisibles[toBoard] -= 13;
					}

					// découvrir une nouvelle carte

					if (b.nbCartesVisibles[toBoard] == 0)
						DecouvrirCarte(toBoard);
				}
			}
		}

		hist.historise(b);
		return 0;
	}


	/**
	 * distribution du talon
	 * @return true si autorisée
	 */
	public boolean distribTalon() {
		// vérification qu'il n'y pas de colonne vide
		for (int i = 0; i < 10; i++) {
			if (b.nbCartesCaches[i] == 0)  {				
				return false;
			}	
		}
		// distribution
		if (b.nbCartesCaches[tas] > 0) {
			for (int i = 0; i < 10; i++) {
				if (b.nbCartesCaches[tas] == 0)
					break;
				b.boardVisible[i][b.nbCartesVisibles[i]++] = b.boardCache[tas][--b.nbCartesCaches[tas]];
			}
		}
		hist.historise(b);
		return true;
	}

	/**
	 * jeu automatique
	 * @return CardMove ou null si aucun mouvement possible
	 */
	public CardMove jeuAuto() {
		int source; 		// colonne source
		int iCarteSource;	// indice carte source
		int carteSrc;		// carte source
		int objet;			// colonne objet
		int carteObj;		// carte objet sur laquelle on place la carte source
		CardMove m;			// mouvement de cartes
	
		// recherche mouvement entre colonnes

			// colonne source		
		for (source = 0; source < 9; source++)  {
				// sélection d'une carte visible de la colonne source
				// de même couleur et en séquence descendante
			int carteVisible = b.nbCartesVisibles[source];
			if ( carteVisible > 0)  {
				iCarteSource = carteVisible-1;
				carteSrc = b.boardVisible[source][iCarteSource];
				int carteCouleur = carteCouleur(carteSrc);
				int carteRang = carteRang(carteSrc);
				do {
					carteSrc = b.boardVisible[source][iCarteSource];
					if ( carteCouleur(carteSrc) != carteCouleur || carteRang(carteSrc) != carteRang )
						break;
						// recherche colonne réceptrice
					for (objet = 0; objet < 9; objet++) 	{
						if (objet == source) continue;
						if (b.nbCartesVisibles[objet] > 0) {
							carteObj = b.boardVisible[objet][b.nbCartesVisibles[objet] - 1];
							if ( bRoiSurAs && carteRang(carteObj) == 0 && carteRang(carteSrc) == 12  ||
								 carteRang(carteObj) == carteRang(carteSrc) + 1 )	{
								m = new CardMove(source, objet, iCarteSource, false);
								if (!boucle(m))
									return m;
							}
						} else if (b.nbCartesVisibles[objet] == 0 && b.nbCartesCaches[source] > 0) {
							m = new CardMove(source, objet, iCarteSource, false);
							if (!boucle(m))
								return m;
						}
					}
					iCarteSource--;
					carteRang++;
				} while (iCarteSource >= 0);
			}			
		}
		
		// distribution du talon s'il reste des cartes
		if ( !distribTalon() ) {
			return null;			
		}
		// et on continue
		return jeuAuto();
	}

}