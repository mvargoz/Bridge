package Card;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.*;

import winApp.ContexteGlobal;

/**
 * Animation du jeu de carte
 *
 */
public class CardAnim extends JComponent implements Runnable {
	private static final long serialVersionUID = 1L;
	

	/**
	 * Jeu à animer
	 */
	private CardGame jeu;
	/**
	 * Type d'animation
	 */
	private int animType;
	/**
	 * Animation de la distribution
	 * une seule carte non visible vers un tas
	 * retournement possible
	 */
	public static final int DISTRIBUTION = 1; 
	/**
	 * Animation du transfert entre tas
	 * une ou plusieurs cartes visibles d'un tas à l'autre 
	 */
	public static final int JEU_TAS= 2;
	/**
	 * Méthode pour obtenir le mouvement
	 */
	private Method methodAuto;	
	/**
	 * Mouvement obtenu
	 */
	private CardMove m;
	/**
	 * Nombre d'étapes pour animer un mouvement
	 */
	private static int nbStep = 10;
	/**
	 * Etape courante
	 */
	private int Step = 0;
	/**
	 * Attente pour ralentir le mouvement
	 */
	private static int lgSleep = 20;
	/**
	 * thread d'animation
	 */
	private Thread animator;
	/**
	 * Position du début de l'animation
	 */
	private Point initLocation;
	/**
	 * position tas source
	 */
	private Point pFrom;
	/**
	 * position tas objet
	 */
	private Point pTo;
	/**
	 * tas de cartes à déplacer
	 */
	private int[] dragCard = new int[13];
	/**
	 * nombre de cartes à déplacer
	 */
	private int nbDragCard = 1;
	/**
	 * rendre la carte visible
	 */
	private boolean cardVisible;

	/**
	 * Constructeur
	 * @param jeu
	 * @param animType
	 * @param methodAuto
	 * @param size
	 * @param location
	 */
	public CardAnim(CardGame jeu, int animType, Method methodAuto, Dimension size, Point location) {
		super();
		this.jeu = jeu;
		this.animType = animType;
		this.methodAuto = methodAuto;
		setSize(size);
		this.initLocation = location;
	}

	/**
	 * démarrer l'animation
	 */
	public void go() {
		try {
			m = (CardMove) methodAuto.invoke(jeu);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
/*		if ( animType == DISTRIBUTION ) {
			m = jeu.distribAuto();
		} else if ( animType == JEU_TAS ) {
			m = jeu.jeuAuto();
		}*/
		if ( m  != null ) {
				// Calcul des oordonnées de la carte à déplacer
			CardGamePanel gp = (CardGamePanel) ContexteGlobal.frame.panel;
			pFrom = gp.board[m.fromBoard].getLocation();
			if (gp.lookBoard[m.fromBoard] == 1) {
				if (jeu.b.nbCartesCaches[m.fromBoard] > 0)
					pFrom.y += CardGamePanel.CardVerticalSpace;
				pFrom.y += m.noCard * CardGamePanel.CardVerticalSpace;
			} else if (gp.lookBoard[m.fromBoard] == 2) {
				pFrom.x += m.noCard * CardGamePanel.CardHorizontalSpace;
			} else if (gp.lookBoard[m.fromBoard] == 6) {
				pFrom.x += gp.board[m.fromBoard].width - CardGamePanel.CardSize.width;
				pFrom.x -= m.noCard * CardGamePanel.CardHorizontalSpace;
			}
			pTo = gp.board[m.toBoard].getLocation();
			if (gp.lookBoard[m.toBoard] == 1) {
				if (jeu.b.nbCartesCaches[m.toBoard] > 0)
					pTo.y += CardGamePanel.CardVerticalSpace;
				pTo.y += (jeu.b.nbCartesVisibles[m.toBoard] - 1) * CardGamePanel.CardVerticalSpace;
			} else if (gp.lookBoard[m.toBoard] == 2) {
				pTo.x += (jeu.b.nbCartesVisibles[m.toBoard] - 1) * CardGamePanel.CardHorizontalSpace;
			} else if (gp.lookBoard[m.toBoard] == 6) {
				pTo.x += gp.board[m.toBoard].width - CardGamePanel.CardSize.width;
				pTo.x -= (jeu.b.nbCartesVisibles[m.toBoard] - 1) * CardGamePanel.CardHorizontalSpace;
			}

			// nombre de cartes à déplacer

			if ( animType == DISTRIBUTION ) {
				nbDragCard = 1;
				cardVisible = m.cardVisible;
			} else if ( animType == JEU_TAS ) {
				nbDragCard = jeu.b.nbCartesVisibles[m.fromBoard] - m.noCard;
				for (int i = m.noCard, j = 0; j < nbDragCard; i++, j++)
					dragCard[j] = jeu.b.boardVisible[m.fromBoard][i];
				jeu.b.nbCartesVisibles[m.fromBoard] -= nbDragCard;
			}

			// lance la thread (exécution méthode run)

//			System.out.println("move card " + m.noCard + " from " + m.fromBoard + " to " + m.toBoard);
			animator = new Thread(this);
			animator.start();

		} else {
			ContexteGlobal.frame.setMessage(jeu.messageFixe);
			ContexteGlobal.frame.panel.repaint();
		}
	}

	/**
	 * arrèter l'animation
	 */
	public void halt() {
		animator = null;
		setVisible(false);
		if ( animType == JEU_TAS ) {
			jeu.b.nbCartesVisibles[m.fromBoard] += nbDragCard;
		}
	}

	/**
	 * animation
	 */
	public void run() {
		setVisible(true);
		Step = 0;
		Thread me = Thread.currentThread();
		while (animator == me) {
			try {
				Thread.sleep(lgSleep);
			} catch (InterruptedException e) {
				break;
			}
			synchronized (this) {
				Step++;
				if (Step > nbStep) {
						// mouvement en cours terminé
					animator = null;
					setVisible(false);
					if ( animType == DISTRIBUTION ) {
						int from = --jeu.b.nbCartesCaches[m.fromBoard];
						int to = jeu.b.nbCartesCaches[m.toBoard]++;
						jeu.b.boardCache[m.toBoard][to] = jeu.b.boardCache[m.fromBoard][from];
						if (cardVisible)
							jeu.DecouvrirCarte(m.toBoard);
						ContexteGlobal.frame.panel.repaint();
						go(); 		// mouvement suivant
					} else if ( animType == JEU_TAS ) {
						jeu.b.nbCartesVisibles[m.fromBoard] += nbDragCard;
						int r = jeu.click(m.fromBoard, m.noCard);
						if (m.fromBoard != m.toBoard)
							r = jeu.click(m.toBoard, m.noCard);
						if (r > 0) {
							ContexteGlobal.frame.setMessage(
									jeu.messageFixe + ": " + ContexteGlobal.getResourceString("mess6"));
							ContexteGlobal.frame.panel.repaint();
						} else {
							ContexteGlobal.frame.panel.repaint();
							go(); 	// mouvement suivant
						}
					}
				} else {
					repaint();
				}
			}
		}
	}

	/**
	 * affichage
	 */
	public void paint(Graphics g) {
		if (m.fromBoard != m.toBoard) {
			int px = pFrom.x + (pTo.x - pFrom.x) * Step / nbStep + initLocation.x;
			int py = pFrom.y + (pTo.y - pFrom.y) * Step / nbStep + initLocation.y;
			if ( animType == DISTRIBUTION ) {
				g.drawImage(CardImages.cims[CardGamePanel.back],
						px, py,
						CardGamePanel.widthCard, CardGamePanel.heightCard,
						this);				
			} else if ( animType == JEU_TAS ) {
				for (int i = 0; i < nbDragCard; i++)
					g.drawImage(CardImages.cims[dragCard[i]],
							px, py + i * CardGamePanel.CardVerticalSpace,
							CardGamePanel.widthCard, CardGamePanel.heightCard,
							this);	
			}
		}
	}
	
}
