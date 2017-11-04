package Card;

import java.awt.*;


import winApp.ContexteGlobal;

/**
 * 		Présentation du jeu Crapette
 *
 */
public class CrapettePanel extends CardGamePanel {

	private static final long serialVersionUID = 1L;
	
	/**
	 * constructeur
	 */
	public CrapettePanel() {
		bJeuEnCours = true;
		ContexteGlobal.frame.getAction("undo").setEnabled(true);
		ContexteGlobal.frame.getAction("new").setEnabled(true);		
		distributionAnim = true;
	}

	/**
	 * initialisation, positionnement des tas
	 */
	protected void init() {
		
		repaint();  // évite que la dialog box efface le panel
		
		new CardGameHelp(ContexteGlobal.frame, ContexteGlobal.getResourceString("titleBegin"), "crapette", true);
		
		// définition du board

		dimBoard = 23;
		board = new Rectangle[dimBoard];
		int hini = 10;
		int vini = 10;
		int hint = 10;
		int vint = 5;
		int mid = widthScreen / 2;
		int hsp = CardGamePanel.widthCard + hint;
		int vsp = CardGamePanel.heightCard + vint;
		int hsize = mid - hsp - hini - hint;
		board[0] = new Rectangle(mid - hsp, vini, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[1] = new Rectangle(mid - hsp, vini + vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[2] = new Rectangle(mid - hsp, vini + 2 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[3] = new Rectangle(mid - hsp, vini + 3 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[4] = new Rectangle(mid + hint, vini, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[5] = new Rectangle(mid + hint, vini + vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[6] = new Rectangle(mid + hint, vini + 2 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[7] = new Rectangle(mid + hint, vini + 3 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);

		board[8] = new Rectangle(hini, vini, hsize, CardGamePanel.heightCard);
		board[9] = new Rectangle(hini, vini + vsp, hsize, CardGamePanel.heightCard);
		board[10] = new Rectangle(hini, vini + 2 * vsp, hsize, CardGamePanel.heightCard);
		board[11] = new Rectangle(hini, vini + 3 * vsp, hsize, CardGamePanel.heightCard);

		board[12] = new Rectangle(mid - 2 * hsp, vini + 4 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[13] = new Rectangle(mid - 3 * hsp, vini + 4 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[14] = new Rectangle(mid - 4 * hsp, vini + 4 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);

		board[15] = new Rectangle(mid + hsp + hint, vini, hsize, CardGamePanel.heightCard);
		board[16] = new Rectangle(mid + hsp + hint, vini + vsp, hsize, CardGamePanel.heightCard);
		board[17] = new Rectangle(mid + hsp + hint, vini + 2 * vsp, hsize, CardGamePanel.heightCard);
		board[18] = new Rectangle(mid + hsp + hint, vini + 3 * vsp, hsize, CardGamePanel.heightCard);

		board[19] = new Rectangle(mid + hsp + hint, vini + 4 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[20] = new Rectangle(mid + 2 * hsp + hint, vini + 4 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);
		board[21] = new Rectangle(mid + 3 * hsp + hint, vini + 4 * vsp, CardGamePanel.widthCard, CardGamePanel.heightCard);

		board[22] = new Rectangle(10, 10, CardGamePanel.widthCard, CardGamePanel.heightCard);

		lookBoard = new int[dimBoard];
		lookBoard[8] = 6;
		lookBoard[9] = 6;
		lookBoard[10] = 6;
		lookBoard[11] = 6;
		lookBoard[15] = 2;
		lookBoard[16] = 2;
		lookBoard[17] = 2;
		lookBoard[18] = 2;

		super.init();
		jeu = new Crapette();
		jeu.init();
		winApp.ContexteGlobal.frame.setMessage(jeu.messageFixe);
		new CardSupervisor(jeu);
	}
}
