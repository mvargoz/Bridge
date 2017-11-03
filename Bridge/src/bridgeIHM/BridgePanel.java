package bridgeIHM;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

import javax.swing.border.*;

import bridgeBid.DonneBid;
import bridgePlay.Jeu;
import winApp.*;

/**
 * 		Table de bridge
 */

public class BridgePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	// fonts

	private static Font defaultFont = new Font("Dialog", Font.PLAIN, 12);
	private static Font boldFont = new Font("Dialog", Font.BOLD, 12);
	private static Font bigFont = new Font("Dialog", Font.PLAIN, 18);
	private static Font bigBoldFont = new Font("Dialog", Font.BOLD, 18);
	private static LineBorder encadre = new LineBorder(Color.black, 1);

	// constantes

	private static int dimBoard;
	// private static int back = 52;
	private static int CardVerticalSpace = 15;
	private static int CardHorizontalSpace = 15;

	// jeu

	public BridgeGame bridgeGame;
	public boolean bAttente = true; // bloque le superviseur
	public boolean bEnchere = false; // phase enchères
	public boolean bJeuCarte = false; // phase jeu de la carte

	// description de l'IHM

	public Rectangle[] board;
	public boolean[] visibleBoard;
	public boolean allVisible = false;
	public Color backgroundColor = Color.gray;
	public JLabel lbDonneur, lbContrat, lbPlisNS, lbPlisEO;
	public JLabel lbNord, lbEst, lbSud, lbOuest;
	public JPanel pNord, pEst, pSud, pOuest;
	public JLabel[][] lbEnch;

	private Dimension CardSize;
	private cardCanvas[] boardCanvas;
	private int hPli;
	private int vPli;
	private JPanel infoPanel, enchPanel;
	private BoiteEnchere bidding;
	private DialogueFinEnchere dlgEndBidding = new DialogueFinEnchere();
	private DialogueFinJeu dlgEndGame = new DialogueFinJeu();
	private DialogueSaisieProbleme dlgDonne = new DialogueSaisieProbleme();
	private JFileChooser jFileChooserBridge;
	private GenericFileFilter filter = new GenericFileFilter();

	/**
	 * Constructeur:  mise en place de l'environnement
	 */
	public BridgePanel() {
		dimBoard = BridgeGame.dimBoard;
		CardSize = CardImages.CardSize;
		board = new Rectangle[dimBoard];

		// visibilité des jeux

		visibleBoard = new boolean[dimBoard];

		// setBackground(backgroundColor);
		CardImages.Init(this);

		// initialisation du choix de fichier

		filter.addExtension(ContexteGlobal.getResourceString("saveExt"));
		filter.setDescription(ContexteGlobal.getResourceString("saveMess"));
		jFileChooserBridge = new JFileChooser(ContexteGlobal.getResourceString("saveDirDonne"));
		jFileChooserBridge.setFileFilter(filter);

		// lancement

		winApp.ContexteGlobal.frame.getAction("new").setEnabled(true);
		winApp.ContexteGlobal.frame.getAction("open").setEnabled(true);
		winApp.ContexteGlobal.frame.getAction("save").setEnabled(true);
		winApp.ContexteGlobal.frame.getAction("debug").setEnabled(true);
		winApp.ContexteGlobal.frame.getAction("endgame").setEnabled(true);
		
		initPanelJeu();
		
	}

	/**
	 * Initialisation du jeu
	 */
	public void initPanelJeu()	{

		initBoard();
		dlgEndBidding.setLocationRelativeTo(ContexteGlobal.frame);
		dlgEndGame.setLocationRelativeTo(ContexteGlobal.frame);

		// gestion de la souris

		addMouseListener(new MouseListener());

		// gestion du resize

		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				sizeBoard();
			}
		});

		// superviseur pour simulation

		new CardSupervisor(this);

		// jeu

		bridgeGame = new BridgeGame();
		bridgeGame.init();
//		lanceJeu();
	}

	/**
	 * Réinitialisation, changement de jeu
	 */
	public void reInit() {
		if (bridgeGame.vulnerabilite == null) {
			bridgeGame.init();
		} else {
			int st = JOptionPane.showConfirmDialog(this,
					ContexteGlobal.getResourceString("messNewGame"),
					ContexteGlobal.getResourceString("messConfirm"),
					JOptionPane.YES_NO_OPTION);
			if (st == JOptionPane.YES_OPTION) {
				bridgeGame.init();
			} else {
				bridgeGame.restoreJeu();
				bridgeGame.initEnchere();
				afficheInfoJeu();
				repaint();
			}
		}
		lanceJeu();
	}

	/**
	 * Chargement d'un jeu
	 */
	public void open() {
		int returnVal = jFileChooserBridge.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			bridgeGame.load(jFileChooserBridge.getSelectedFile());
		}
		lanceJeu();
	}

	/**
	 * Sauvegarde d'un jeu
	 */
	public void save() {
		int returnVal = jFileChooserBridge.showSaveDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String ext = "." + ContexteGlobal.getResourceString("saveExt");
			File f = jFileChooserBridge.getSelectedFile();
			String nmFile = f.getAbsolutePath();
			if ( !nmFile.endsWith(ext) )
				f = new File(nmFile + ext);
			bridgeGame.save(f);
		}
	}

	/**
	 * undo
	 */
	public void undo() {
		bridgeGame.undo();
		if (bridgeGame.etat == 0)
			afficheEnchere();
		else
			afficheInfoJeu();
	}

	/**
	 * Lancement du jeu
	 */
	private void lanceJeu() {
		visibiliteBoard();
		sizeBoard();
		lbContrat.setText("");
		bEnchere = true;
		bAttente = false;
		afficheEnchere();
		ContexteGlobal.frame.setMessage(bridgeGame.messageFixe);
	}

	/**
	 * Initialisation de l'affichage du jeu
	 */
	private void initBoard() {
		removeAll();
		setLayout(null);

		// joueurs

		pNord = new JPanel();
		lbNord = new JLabel("NORD");
		lbNord.setFont(boldFont);
		pNord.add(lbNord);
		add(pNord);
		pNord.setSize(40, 25);

		pEst = new JPanel();
		lbEst = new JLabel("EST");
		lbEst.setFont(boldFont);
		pEst.add(lbEst);
		add(pEst);
		pEst.setSize(40, 25);

		pSud = new JPanel();
		lbSud = new JLabel("SUD");
		lbSud.setFont(boldFont);
		pSud.add(lbSud);
		add(pSud);
		pSud.setSize(40, 25);

		pOuest = new JPanel();
		lbOuest = new JLabel("OUEST");
		lbOuest.setFont(boldFont);
		pOuest.add(lbOuest);
		add(pOuest);
		pOuest.setSize(40, 25);

		// jeux de cartes

		boardCanvas = new cardCanvas[dimBoard];
		for (int i = 0; i < dimBoard; i++) {
			boardCanvas[i] = new cardCanvas(i);
			add(boardCanvas[i]);
		}

		// informations

		infoPanel = new JPanel(new GridLayout(4, 1, 10, 5));

		lbDonneur = new JLabel(" Prêt à jouer au bridge ");
		infoPanel.add(lbDonneur);
		lbDonneur.setFont(bigBoldFont);

		lbContrat = new JLabel("");
		infoPanel.add(lbContrat);
		lbContrat.setFont(bigBoldFont);

		lbPlisNS = new JLabel("");
		infoPanel.add(lbPlisNS);
		lbPlisNS.setFont(boldFont);

		lbPlisEO = new JLabel("");
		infoPanel.add(lbPlisEO);
		lbPlisEO.setFont(boldFont);

		add(infoPanel);
		infoPanel.setSize(infoPanel.getLayout().minimumLayoutSize(infoPanel));

		// enchères

		enchPanel = new JPanel(new BorderLayout());

		JLabel lb = new JLabel("Enchères");
		lb.setHorizontalAlignment(SwingConstants.CENTER);
		enchPanel.add(lb, "North");
		lb.setFont(boldFont);

		JPanel enchPanel2 = new JPanel(new GridLayout(21, 4, 10, 5));
		lb = new JLabel("N   ");
		enchPanel2.add(lb);
		// lb.setFont(bigBoldFont);

		lb = new JLabel("E   ");
		enchPanel2.add(lb);
		// lb.setFont(bigBoldFont);

		lb = new JLabel("S   ");
		enchPanel2.add(lb);
		// lb.setFont(bigBoldFont);

		lb = new JLabel("O   ");
		enchPanel2.add(lb);
		// lb.setFont(bigBoldFont);

		lbEnch = new JLabel[20][4];
		for (int i = 0; i < 20; i++)
			for (int j = 0; j < 4; j++) {
				lbEnch[i][j] = new JLabel("");
				enchPanel2.add(lbEnch[i][j]);
				// lbEnch[i][j].setFont(bigBoldFont);
			}

		enchPanel.add(enchPanel2, "South");
		add(enchPanel);
		enchPanel.setSize(enchPanel.getLayout().minimumLayoutSize(enchPanel));

		// bidding box

		try {
			Class<?>[] parm = new Class[1];
			parm[0] = Class.forName("java.lang.String");
			bidding = new BoiteEnchere(getClass().getMethod("putEnchere", parm));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		add(bidding);
	}

	/**
	 * Calcul de la taille de l'affichage
	 */
	public void sizeBoard() {
		int wPanel = getSize().width;
		int hPanel = getSize().height;
		if (wPanel == 0 || hPanel == 0)
			return;
		int vsp = CardSize.height / 2 + 5;
		int hsp = CardSize.width + 5;
		int hBoard = CardSize.width + 12 * CardHorizontalSpace;
		int vBoard = CardSize.height + 12 * CardVerticalSpace;
		// positionnements
		int hNord = wPanel / 2 - 2 * hsp;
		int vNord = 40;
		int hSud = wPanel / 2 - hBoard;
		int vSud = hPanel - CardSize.height - 5;
		int vEst = hPanel / 2 - 2 * vsp;
		int vOuest = vEst;
		int hEst = wPanel - hBoard;
		int hOuest = 5;

		for (int i = 0; i < BridgeGame.nbCouleur; i++)
			board[i] = new Rectangle(hNord + (3 - i) * hsp, vNord, CardSize.width, vBoard);
		for (int i = 0; i < BridgeGame.nbCouleur; i++)
			board[i + 4] = new Rectangle(hEst, vEst + (3 - i) * vsp, hBoard, CardSize.height);
		for (int i = 0, tab = hSud; i < BridgeGame.nbCouleur; i++) {
			int hBoardS = CardSize.width + (bridgeGame.cardBoard.nbCartes[i + 8]) * CardHorizontalSpace;
			board[i + 8] = new Rectangle(tab, vSud, hBoardS, CardSize.height);
			tab += hBoardS + 5;
		}
		for (int i = 0; i < BridgeGame.nbCouleur; i++)
			board[i + 12] = new Rectangle(hOuest, vOuest + (3 - i) * vsp, hBoard, CardSize.height);

		hPli = wPanel / 2;
		vPli = hPanel / 2;
		board[16] = new Rectangle(hPli, vPli - CardSize.height / 4, CardSize.width, CardSize.height);
		board[17] = new Rectangle(hPli + CardSize.width / 2, vPli, CardSize.width, CardSize.height);
		board[18] = new Rectangle(hPli, vPli + CardSize.height / 4, CardSize.width, CardSize.height);
		board[19] = new Rectangle(hPli - CardSize.width / 2, vPli, CardSize.width, CardSize.height);

		// modification de taille pour les jeux non visibles

		for (int i = 0; i < BridgeGame.table; i++) {
			if (!visibleBoard[i])
				if (i % 4 == 2)
					board[i] = new Rectangle(board[i].x, board[i].y, 3 * CardSize.width, 3 * CardSize.height);
				else
					board[i] = new Rectangle(0, 0, 0, 0);
		}

		// mise en place des composants graphiques

		for (int i = 0; i < dimBoard; i++) {
			boardCanvas[i].setLocation(new Point(board[i].x, board[i].y));
			boardCanvas[i].setSize(board[i].width + 2, board[i].height + 2);
			boardCanvas[i].setVisible(true);
		}
		pNord.setLocation(wPanel / 2, 5);
		pEst.setLocation(hEst + hsp, vEst - 30);
		pSud.setLocation(wPanel / 2, vSud - vNord);
		pOuest.setLocation(hOuest + hsp, vOuest - 30);
		infoPanel.setLocation(0, 0);
		enchPanel.setLocation(wPanel - 150, 0);
		bidding.setLocation(hEst - bidding.getSize().width - 50, vSud - bidding.getSize().height - 50);

	}

	/**
	 * Visibilité des jeux
	 */
	public void visibiliteBoard() {
		visibleBoard = new boolean[dimBoard];
		for (int i = 0; i < BridgeGame.table; i++)
			visibleBoard[i] = bridgeGame.joueurHumain(i / 4) || allVisible;
		for (int i = BridgeGame.table; i < dimBoard; i++)
			visibleBoard[i] = (bridgeGame.etat == 1);
	}

	/**
	 * Enchère manuelle ou automatique
	 */
	public void enchere() {
		if (bridgeGame.enchereHumaine())
			bidding.open(bridgeGame.donne.lastAnnonce(), bridgeGame.donne.testEnchere("X"), bridgeGame.donne.testEnchere("XX"));
		else
			putEnchere("");
	}

		// traitement de l'enchère joueur humain venant du bidding

	/**
	 * Traitement de l'enchère manuelle venant du bidding
	 * @param enchere
	 */
	public void putEnchere(String enchere) {
		bridgeGame.enchere(enchere);
		afficheEnchere();
		if (bridgeGame.etat == 1) {
			bidding.close();
			lbContrat.setText(bridgeGame.contrat);
			visibiliteBoard();
			afficheInfoJeu();
			dlgEndBidding.setContrat(bridgeGame.contrat);
			dlgEndBidding.setVisible(true);
			if (dlgEndBidding.getAction() == 0) {
				// abandonner la donne			
				bridgeGame.init();
				lanceJeu();
			} else if (dlgEndBidding.getAction() == 1) {
				// recommencer les enchères
				bridgeGame.initEnchere();
				lanceJeu();
			} else {
				// jeu de la carte			
				bEnchere = false;
				bJeuCarte = true;
				bridgeGame.initJeuCarte();
			}
		}
		bAttente = false;
	}

	/**
	 * Affichage des enchères
	 */
	private void afficheEnchere() {
		lbDonneur.setText("Donneur " + Jeu.libJoueur(bridgeGame.donneur));
		// mise en evidence du joueur qui doit enchérir
		pNord.setBorder(null);
		pEst.setBorder(null);
		pSud.setBorder(null);
		pOuest.setBorder(null);
		if (bridgeGame.donne.getJoueur() == 0)
			pNord.setBorder(encadre);
		else if (bridgeGame.donne.getJoueur() == 1)
			pEst.setBorder(encadre);
		else if (bridgeGame.donne.getJoueur() == 2)
			pSud.setBorder(encadre);
		else
			pOuest.setBorder(encadre);
		// affichage vulnérabilité
		if (bridgeGame.vulnerabilite.equals("P") || bridgeGame.vulnerabilite.equals("EO")) {
			lbNord.setForeground(Color.green);
			lbSud.setForeground(Color.green);
		} else {
			lbNord.setForeground(Color.red);
			lbSud.setForeground(Color.red);
		}
		if (bridgeGame.vulnerabilite.equals("P") || bridgeGame.vulnerabilite.equals("NS")) {
			lbEst.setForeground(Color.green);
			lbOuest.setForeground(Color.green);
		} else {
			lbEst.setForeground(Color.red);
			lbOuest.setForeground(Color.red);
		}

		// liste des enchères

		for (int i = 0; i < 20; i++)
			for (int k = 0; k < 4; k++)
				lbEnch[i][k].setText("");
		int n = bridgeGame.donne.getnbEnch();
		for (int i = 0, tour = 0; i < n; tour++) {
			int k = 0;
			if (tour == 0)
				k = DonneBid.joueurToInt(bridgeGame.donneur);
			for (; k < 4 && i < n; k++, i++)
				lbEnch[tour][k].setText(bridgeGame.donne.getEnchere(i));
		}
	}

	/**
	 * Jeu de la carte
	 */
	public void jeuCarte() {
		ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messJeuAuto"));
		int status = bridgeGame.jeuAuto();
		if (status < 0) { 					// carte interdite
			getToolkit().beep();
			ContexteGlobal.frame.setMessage(bridgeGame.messageFixe + ": " + ContexteGlobal.getResourceString(bridgeGame.Message));
			bAttente = true;
		} else if (status > 0) { 			// fin du jeu
			finJeu();
		} else { 							// suite du jeu
			afficheInfoJeu();
			bAttente = bridgeGame.jeuCarteHumain();
			repaint();
		}
	}

	/**
	 * Affichage des infos du jeu de la carte
	 */
	private void afficheInfoJeu() {
			// nombre de plis
		lbPlisNS.setText("Pli en N/S : " + bridgeGame.plisNS);
		lbPlisEO.setText("Pli en E/O : " + bridgeGame.plisEO);
			// mise en evidence du joueur qui doit jouer
		pNord.setBorder(null);
		pEst.setBorder(null);
		pSud.setBorder(null);
		pOuest.setBorder(null);
		if (bridgeGame.joueurAyantLaMain == 0)
			pNord.setBorder(encadre);
		else if (bridgeGame.joueurAyantLaMain == 1)
			pEst.setBorder(encadre);
		else if (bridgeGame.joueurAyantLaMain == 2)
			pSud.setBorder(encadre);
		else
			pOuest.setBorder(encadre);
			// messages
		ContexteGlobal.frame.setMessage(bridgeGame.messageFixe);
	}

	/**
	 * Fin du jeu
	 */
	public void finJeu() {
		bAttente = true;
		bJeuCarte = false;
		ContexteGlobal.frame.setMessage(bridgeGame.messageFixe);
		dlgEndGame.setContrat(bridgeGame.messageFixe);
		dlgEndGame.setVisible(true);
		if (dlgEndGame.getAction() == 0) // donne suivante
		{
			reInit();
		} else if (dlgEndGame.getAction() == 1) // recommencer les enchères
		{
			bridgeGame.restoreJeu();
			bridgeGame.initEnchere();
			afficheInfoJeu();
			lanceJeu();
		} else
		// recommencer le jeu de la carte
		{
			bridgeGame.restoreJeu();
			bridgeGame.initJeuCarte();
			bEnchere = false;
			bJeuCarte = true;
			afficheInfoJeu();
			bAttente = false;
		}

	}

	/**
	 * Gestion des donnes problèmes
	 */
	public void problem() {
		dlgDonne.open();
	}
	
	/**
	 * Jeu d'un joueur affiché sur le board
	 *
	 */
	private class cardCanvas extends JComponent {
		private static final long serialVersionUID = 1L;

		/**
		 * Position du joueur NESW = 0 à 3
		 * 
		 */
		int noBoard;

		/**
		 * Constructeur
		 * @param noBoard
		 */
		public cardCanvas(int noBoard) {
			setVisible(false);
			setRequestFocusEnabled(false);
			this.noBoard = noBoard;
		}

		/**
		 * Affichage du composant graphique
		 */
		public void paint(Graphics g) {
			if (visibleBoard[noBoard]) {
				int x = 0;
				int y = 0;
				for (int i = 0; i < bridgeGame.cardBoard.nbCartes[noBoard]; i++) {
					int card = bridgeGame.cardBoard.board[noBoard][i];
					g.drawImage(CardImages.cims[card], x, y, this);
					if (board[noBoard].height > CardSize.height)
						y += CardVerticalSpace;
					if (board[noBoard].width > CardSize.width)
						x += CardHorizontalSpace;
				}
			} else if (noBoard < BridgeGame.table && noBoard % 4 == 2)
				g.drawImage(CardImages.fans[5], 0, 0, this);
			// else
			// g.drawRect(0,0,CardSize.width,CardSize.height);

		}
	}

	/**
	 * Gestion de la souris
	 *
	 */
	private class MouseListener extends MouseInputAdapter {

		/**
		 * Click d'une carte à jouer
		 *
		 */
		public void mouseClicked(MouseEvent e) {
			if (!bJeuCarte)
				return;
			Point pn = new Point(e.getX(), e.getY());
			int noBoard = getBoard(pn);
			if (noBoard >= 0 && bridgeGame.cardBoard.nbCartes[noBoard] > 0) {
				int selectCard = 0;
				if (board[noBoard].height > CardSize.height)
					selectCard = (e.getY() - board[noBoard].y) / CardVerticalSpace;
				else if (board[noBoard].width > CardSize.width)
					selectCard = (e.getX() - board[noBoard].x) / CardHorizontalSpace;
				int pos = Math.min(selectCard, bridgeGame.cardBoard.nbCartes[noBoard] - 1);
				pos = Math.max(pos, 0);
				int status = bridgeGame.click(noBoard, pos);
				repaint();
				if (status < 0) { // carte interdite
					getToolkit().beep();
					ContexteGlobal.frame
							.setMessage(bridgeGame.messageFixe + ": " + ContexteGlobal.getResourceString(bridgeGame.Message));
				} else if (status > 0) { // fin du jeu
					finJeu();
				} else { // suite du jeu
					afficheInfoJeu();
					bAttente = bridgeGame.jeuCarteHumain();
				}
			} else
				getToolkit().beep();
			repaint();
		}

		/**
		 * Calcule le numéro de joueur NESW à partir de la position de la souris
		 * @param position
		 * @return numéro joueur 0 à 3, -1 si le pointeur n'est pas sur un jeu
		 */
		private int getBoard(Point position) {
			for (int i = 0; i < dimBoard; i++) {
				if (board[i].contains(position))
					return i;
			}
			return -1;
		}
	}

}
