package Card;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.Method;

import winApp.ContexteGlobal;

/**
 * Affichage du jeu en cours
 *
 */
public class CardGamePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 *    version
	 */
	private static final String VERSION = "1.0";

		// 		présentation des cartes	
	/**
	 * largeur d'écran
	 */
	protected static int widthScreen = new Integer(ContexteGlobal.getResourceString("screenHorizontalSize"));
	/**
	 * hauteur d'écran
	 */
	protected static int heigthScreen = new Integer(ContexteGlobal.getResourceString("screenVerticalSize"));
	/**
	 * couleur de fond
	 */
	protected  Color backgroundColor = Color.gray;	
	/**
	 * espacement entre deux tas de cartes
	 */
	protected static int CardSpace = 15;	
	/**
	 * espacement dans l'empilage vertical
	 */
	protected static int CardVerticalSpace = 15;
	/**
	 * espacement dans l'empilage horizontal
	 */
	protected static int CardHorizontalSpace = 10;
	/**
	 * largeur des cartes
	 */
	protected static int widthCard = 97;
	/**
	 * hauteur des cartes
	 */
	protected static int heightCard = 143;
	/**
	 * dimension des cartes
	 */
	protected static Dimension CardSize = new Dimension(widthCard, heightCard);
	/**
	 * numéro du dos des cartes
	 */
	protected static int back = 52;
	/**
	 * jeu représenté par ce panel
	 */
	protected  CardGame jeu;

			// animateur
	/**
	 * jeu en cours d'animation
	 */
	protected  boolean bJeuEnCours = false;
	/**
	 * animateur
	 */
	protected  CardAnim anim = null;

	 		// distribution animée
	/**
	 * animateur de distribution
	 */
	protected CardAnim animDistrib = null;
	/**
	 * distribution animée
	 */
	protected boolean distributionAnim = false;
	/**
	 * dimension du board
	 */
	protected int dimBoard = 13;
	/**
	 * représentation des tas sur le board
	 */
	protected  Rectangle[] board;
	/**
	 * positionnement des cartes
	 * cartes empilées
	 * 1=vertical, 2=horizontal, 6=horiz droite-gauche	
	 */
	protected  int[] lookBoard;
	/**
	 * composant carte sur le board
	 */
	protected cardCanvas[] boardCanvas;
	/**
	 * implémentation du drag & drop
	 */
	protected MouseListener myListener;
	/**
	 * drag & drop en cours
	 */
	protected boolean dragging = false;
	/**
	 * dnd : ensemble des cartes sélectionnées
	 */
	protected int[] dragCard = new int[13];
	/**
	 * dnd : numéro de carte sélectionnée
	 */
	protected int noCard = 0;
	/**
	 * dnd : nombre de cartes sélectionnées	
	 */
	protected int nbDragCard = 0;
	/**
	 * point de départ
	 */
	protected Point p;
	/**
	 * déplacement
	 */
	protected Point dp;
	/**
	 * tas de départ
	 */
	protected int fromBoard;

	/**
	 * contructeur
	 */
	public CardGamePanel() {
		add(new JLabel(ContexteGlobal.getResourceString("titleCardGame")));
	}

	/**
	 * initialisation
	 */
	protected void init() {
		setBackground(backgroundColor);
		CardImages.makeCardsGif();
		initBoard();
		myListener = new MouseListener();
		addMouseListener(myListener);
		addMouseMotionListener(myListener);
	}

	/**
	 * abandon du jeu et réinitialisation
	 */
	public void reInit() {
		int st = JOptionPane.showConfirmDialog(this, ContexteGlobal.getResourceString("mess5"),
				ContexteGlobal.getResourceString("mess9"), JOptionPane.YES_NO_OPTION);
		if (st == JOptionPane.YES_OPTION) {
			if ( animDistrib != null ) {
				animDistrib.halt();
			}
			jeu.init();
			repaint();
		}
		ContexteGlobal.frame.setMessage(jeu.messageFixe);
	}

	/**
	 *  initialisation de la présentation du jeu
	 */
	private void initBoard() {
		removeAll();
		setLayout(null);
		boardCanvas = new cardCanvas[dimBoard];
		for (int i = 0; i < dimBoard; i++) {
			boardCanvas[i] = new cardCanvas(i);
			add(boardCanvas[i]);
		}
	}

	/**
	 * affichage du jeu
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if (dragging) {
			for (int i = 0; i < nbDragCard; i++)
				g.drawImage(CardImages.cims[dragCard[i]],
						p.x + dp.x, p.y + dp.y + i * CardVerticalSpace,
						widthCard, heightCard,
						this);
		}
	}

	/**
	 * animation de la distribution
	 */
	public void distribution() {
		Method methodAnim = null;
		try {
			Class jeuClass = jeu.getClass();
			methodAnim = jeuClass.getDeclaredMethod("distribAuto");
		} catch (Exception e) {
			e.printStackTrace();
		}
		animDistrib = new CardAnim(jeu,
				CardAnim.DISTRIBUTION,
				methodAnim,
				ContexteGlobal.frame.getGlassPane().getSize(),
				new Point(ContexteGlobal.frame.panel.getLocation().x,
						  ContexteGlobal.frame.panel.getLocation().y + ContexteGlobal.frame.menuBar.getHeight()));
		ContexteGlobal.frame.setGlassPane(animDistrib);
		animDistrib.go();
	}

	/**
	 * animation
	 */
	public void animation() {
		Method methodAnim = null;
		try {
			Class jeuClass = jeu.getClass();
			methodAnim = jeuClass.getDeclaredMethod("jeuAuto");
		} catch (Exception e) {
			e.printStackTrace();
		}
		anim = new CardAnim(jeu,
				CardAnim.JEU_TAS,
				methodAnim,
				ContexteGlobal.frame.getGlassPane().getSize(),
				new Point(ContexteGlobal.frame.panel.getLocation().x,
						  ContexteGlobal.frame.panel.getLocation().y + ContexteGlobal.frame.menuBar.getHeight()));
		ContexteGlobal.frame.setGlassPane(anim);
		anim.go();
	}

	/**
	 * Composant carte sur le board
	 *
	 */
	protected class cardCanvas extends JComponent {
		private static final long serialVersionUID = 1L;

		int noBoard;

		public cardCanvas(int i) {
			setLocation(new Point(board[i].x, board[i].y));
			setSize(board[i].width + 2, board[i].height + 2);
			noBoard = i;
		}

		public void paint(Graphics g) {
			
			 // cartes visibles
			
			if (jeu.b.nbCartesVisibles[noBoard] > 0) {
				
				// positionnement vertical
				
				if (lookBoard[noBoard] == 1) {
					int y = 0;
					if (jeu.b.nbCartesCaches[noBoard] > 0) {
						g.drawImage(CardImages.cims[back],
								0, y,
								widthCard, heightCard,
								this);
						y += CardVerticalSpace;
					}
					for (int i = 0; i < jeu.b.nbCartesVisibles[noBoard]; i++) {
						g.drawImage(CardImages.cims[jeu.b.boardVisible[noBoard][i]],
								0, y,
								widthCard, heightCard,
								this);
						y += CardVerticalSpace;
					}
					
					 // positionnement horizontal
					
				} else if (lookBoard[noBoard] == 2) {
					int y = 0;
					if (jeu.b.nbCartesCaches[noBoard] > 0) {
						g.drawImage(CardImages.cims[back],
								y, 0,
								widthCard, heightCard,
								this);
						y += CardHorizontalSpace;
					}
					for (int i = 0; i < jeu.b.nbCartesVisibles[noBoard]; i++) {
						g.drawImage(CardImages.cims[jeu.b.boardVisible[noBoard][i]],
								y, 0,
								widthCard, heightCard,
								this);
						y += CardHorizontalSpace;
					}
					
					 // positionnement horizontal de droite à gauche
					
				} else if (lookBoard[noBoard] == 6) {
					int y = board[noBoard].width - CardSize.width;
					if (jeu.b.nbCartesCaches[noBoard] > 0) {
						g.drawImage(CardImages.cims[back],
								y, 0,
								widthCard, heightCard,
								this);
						y -= CardHorizontalSpace / 2;
					}
					for (int i = 0; i < jeu.b.nbCartesVisibles[noBoard]; i++) {
						g.drawImage(CardImages.cims[jeu.b.boardVisible[noBoard][i]],
								y, 0,
								widthCard, heightCard,
								this);
						y -= CardHorizontalSpace;
					}
					
					 // cartes empilées
					
				} else {
					g.drawImage(CardImages.cims[jeu.b.boardVisible[noBoard][jeu.b.nbCartesVisibles[noBoard] - 1]],
							0, 0,
							widthCard, heightCard,
							this);
				}
				
				 // cartes retournées face non visible
				
			} else if (jeu.b.nbCartesCaches[noBoard] > 0) {
				g.drawImage(CardImages.cims[back],
						0, 0,
						widthCard, heightCard,
						this);
				
				 // case vide
				
			} else {
				if (lookBoard[noBoard] == 6)
					g.drawRect(board[noBoard].width - widthCard, 0, widthCard, heightCard);
				else
					g.drawRect(0, 0, widthCard, heightCard);
			}
		}
	}

	/**
	 * gestion de la souris
	 *
	 */
	protected class MouseListener extends MouseInputAdapter {
		public void mouseDragged(MouseEvent e) {
			Point pn = new Point(e.getX(), e.getY());

			if (!dragging) {
				int noBoard = getBoard(pn);
				
				// recherche de la carte sélectionnée dans la colonne
				
				if (noBoard >= 0 && jeu.b.nbCartesVisibles[noBoard] > 0) {
					if (lookBoard[noBoard] == 1) {
						
						// calcul n° carte sur le board base 0
						
						int selectCard = (e.getY() - board[noBoard].y
								- CardVerticalSpace * (jeu.b.nbCartesCaches[noBoard] > 0 ? 1 : 0))
								/ CardVerticalSpace;
						
						// calcul n° carte visible base 0
						
						noCard = Math.min(selectCard, jeu.b.nbCartesVisibles[noBoard] - 1);
						noCard = Math.max(noCard, 0);
					} else
						noCard = jeu.b.nbCartesVisibles[noBoard] - 1;

					nbDragCard = 0;
					for (int i = noCard; i < jeu.b.nbCartesVisibles[noBoard]; i++)
						dragCard[nbDragCard++] = jeu.b.boardVisible[noBoard][i];
					jeu.b.nbCartesVisibles[noBoard] -= nbDragCard;
					dragging = true;
					fromBoard = noBoard;
					if (lookBoard[noBoard] == 6) {
						p = new Point(board[noBoard].x + board[noBoard].width - CardSize.width, board[noBoard].y);
						p.x -= ((jeu.b.nbCartesCaches[noBoard] > 0) ? noCard + 1 : noCard) * CardVerticalSpace;
					} else {
						p = new Point(board[noBoard].x, board[noBoard].y);
						if (lookBoard[noBoard] == 1)
							p.y += ((jeu.b.nbCartesCaches[noBoard] > 0) ? noCard + 1 : noCard) * CardVerticalSpace;
						else if (lookBoard[noBoard] == 2)
							p.x += ((jeu.b.nbCartesCaches[noBoard] > 0) ? noCard + 1 : noCard) * CardVerticalSpace;
					}
					dp = new Point(p.x - pn.x, p.y - pn.y);
				}
			}

			if (dragging) {
				p = pn;
				repaint();
			}
		}

		public void mouseReleased(MouseEvent e) {
			Point pn = new Point(e.getX(), e.getY());
			if (dragging) {
				dragging = false;
				jeu.b.nbCartesVisibles[fromBoard] += nbDragCard;

				int noBoard = getBoard(pn);
				if (noBoard >= 0 && noBoard != fromBoard) {
					jeu.clearClick();
					int status = jeu.click(fromBoard, noCard);
					if (status == 0)
						status = jeu.click(noBoard, noCard);

					if (status < 0) {
						/*
						 * JOptionPane.showMessageDialog(getParent().getParent(),
						 * CardIHM.resources.getString("mess1"), CardIHM.resources.getString("mess8"),
						 * JOptionPane.ERROR_MESSAGE);
						 */
						getToolkit().beep();
						ContexteGlobal.frame
								.setMessage(jeu.messageFixe + ": " + ContexteGlobal.getResourceString("mess1"));
					} else {
						ContexteGlobal.frame.setMessage(jeu.messageFixe);
						if (jeu.gagne()) {
							JOptionPane.showMessageDialog(getParent().getParent(),
									ContexteGlobal.getResourceString("mess4"),
									ContexteGlobal.getResourceString("mess6"),
									JOptionPane.INFORMATION_MESSAGE);
							reInit();
						}
					}
				}
				if (noBoard < 0)
					getToolkit().beep();
				repaint();
			}
		}

		public void mouseClicked(MouseEvent e) {
			Point pn = new Point(e.getX(), e.getY());
			int noBoard = getBoard(pn);
			if (noBoard >= 0) {
				int status = jeu.click(noBoard, noCard);
				if (status < 0) {
					/*
					 * JOptionPane.showMessageDialog(getParent().getParent(),
					 * CardIHM.resources.getString(j.Message), CardIHM.resources.getString("mess8"),
					 * JOptionPane.ERROR_MESSAGE);
					 */
					getToolkit().beep();
					ContexteGlobal.frame
							.setMessage(jeu.messageFixe + ": " + ContexteGlobal.getResourceString(jeu.Message));
				} else if (status > 0) {
					ContexteGlobal.frame.setMessage(jeu.messageFixe);
					JOptionPane.showMessageDialog(getParent().getParent(),
							ContexteGlobal.getResourceString("mess4"),
							ContexteGlobal.getResourceString("mess6"), JOptionPane.INFORMATION_MESSAGE);
					reInit();
				} else {
					ContexteGlobal.frame.setMessage(jeu.messageFixe);
					repaint();
				}
			}

		}

		/**
		 * Recherche du tas pointé par la souris
		 * @param p 
		 * @return numéro du tas
		 */
		private int getBoard(Point p) {
			for (int i = 0; i < dimBoard; i++) {
				if (board[i].contains(p))
					return i;
			}
			return -1;
		}
	}

	/**
	 * Paramétrage du jeu
	 * @return
	 */
	public JPanel getPanelParm() {
		JPanel panel = new JPanel();
		panel.add(new JLabel("Ce jeu n'est pas paramétrable"));
		return panel;
	}

}
