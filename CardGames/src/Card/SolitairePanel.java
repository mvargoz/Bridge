package Card;

import java.awt.*;

import winApp.ContexteGlobal;

/**
 * 		Présentation du jeu Solitaire
 *
 */
public class SolitairePanel extends CardGamePanel {

	private static final long serialVersionUID = 1L;
	
	/**
	 * constructeur
	 */
	public SolitairePanel() {
		bJeuEnCours = true;
		ContexteGlobal.frame.getAction("undo").setEnabled(true);
		ContexteGlobal.frame.getAction("new").setEnabled(true);
		ContexteGlobal.frame.getAction("auto").setEnabled(true);
		ContexteGlobal.frame.getAction("stop").setEnabled(true);		
		distributionAnim = true;
	}

	/**
	 * initialisation, positionnement des tas
	 */
	protected void init() {
		
		repaint();  // évite que la dialog box efface le panel
		
		new CardGameHelp(ContexteGlobal.frame, ContexteGlobal.getResourceString("titleBegin"), "solitaire", true);
		
		// définition du board

		dimBoard = 13;
		board = new Rectangle[dimBoard];
		
		// 7 cases de jeu de 0 à 6

		for (int i = 0; i < 7; i++) {
			board[i] = new Rectangle(10 + i * (widthCard + CardSpace), heightCard + CardSpace + 10, widthCard, 400);
		}

		// 4 cases de réception des couleurs en partant de l'As de 7 à 10

		for (int i = 7; i < 11; i++) {
			board[i] = new Rectangle( 10 + 2 * (widthCard + CardSpace) + 100 + (i - 7) * (widthCard + CardSpace), 10, widthCard, heightCard);
		}

		// talon case 11 pour les cartes retournées et 12 pour les cartes découvertes

		board[11] = new Rectangle(10, 10, widthCard, heightCard);
		board[12] = new Rectangle(10 + widthCard + CardSpace, 10, widthCard, heightCard);

		// cartes de dessus visibles

		lookBoard = new int[dimBoard];
		for (int i = 0; i < 7; i++)
			lookBoard[i] = 1;

		// initialisation du panel

		super.init();

		// création de l'objet implémentant le fonctionnement du jeu

		jeu = new Solitaire();
		jeu.init();
	}
}