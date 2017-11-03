package bridgeIHM;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import bridge.*;
import bridgePlay.Jeu;

import java.lang.reflect.*;

/**
 * Boite à enchères
 */

public class BoiteEnchere extends JPanel {
	private static final long serialVersionUID = 1L;

	private Method biddingListener;
	private bidButton btAlerte;
	private bidButton btPasse;
	private bidButton btContre;
	private bidButton btSurcontre;
	private bidButton btEnchere[][];
	private JPanel pTete;
	private JPanel pEnchere;
	private ActionListener buttonListener;

	/**
	 * Création de la boite d'enchère
	 * @param method listener
	 */
	public BoiteEnchere(Method method) {
		biddingListener = method;
		buttonListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				bidButton bt = (bidButton) e.getSource();
				String arg[] = new String[1];
				arg[0] = bt.getEnchere();
				try {
					biddingListener.invoke(getParent(), (Object[]) arg);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};

		setLayout(new BorderLayout());
		setBackground(Color.CYAN);
		setBorder(new LineBorder(Color.black, 3));
		pTete = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		btAlerte = new bidButton("Auto", "");
		btAlerte.setBackground(Color.cyan);
		pTete.add(btAlerte);
		btPasse = new bidButton("Passe", "-");
		btPasse.setBackground(Color.green);
		pTete.add(btPasse);
		btContre = new bidButton("X", "X");
		btContre.setBackground(Color.red);
		pTete.add(btContre);
		btSurcontre = new bidButton("XX", "XX");
		btSurcontre.setBackground(Color.blue);
		pTete.add(btSurcontre);
		add(pTete, "North");
		pEnchere = new JPanel(new GridLayout(7, 5, 0, 0));
		btEnchere = new bidButton[7][5];
		for (int niveau = 0; niveau < 7; niveau++) {
			for (int couleur = 4; couleur >= 0; couleur--) {
				String ench = Integer.toString(niveau + 1) + Jeu.couleurToChar(couleur);
				if (couleur < 4)
					btEnchere[niveau][couleur] = new bidButton(Integer.toString(niveau + 1),
							new ImageIcon(CardImages.suits[3 - couleur]), ench);
				else
					btEnchere[niveau][couleur] = new bidButton(ench + "A", ench);
				pEnchere.add(btEnchere[niveau][couleur]);
			}
		}
		add(pEnchere);
		setSize(getLayout().minimumLayoutSize(this));
		setVisible(false);
	}

	/**
	 * Affichage de la boite
	 * @param lastEnchere
	 * @param isX
	 * @param isXX
	 */
	public void open(String lastEnchere, boolean isX, boolean isXX) {
		btContre.setEnabled(isX);
		btSurcontre.setEnabled(isXX);
		if (lastEnchere != null) {
			int lastNiveau = Character.digit(lastEnchere.charAt(0), 10) - 1;
			int lastCouleur = Jeu.couleurToInt(lastEnchere.charAt(1));
			for (int niveau = 0; niveau < 7; niveau++)
				for (int couleur = 0; couleur < 5; couleur++)
					btEnchere[niveau][couleur]
							.setEnabled(niveau > lastNiveau || niveau == lastNiveau && couleur > lastCouleur);
		}
		setVisible(true);
	}

	/**
	 * Fermeture de la boite d'enchère
	 */
	public void close() {
		setVisible(false);
	}

	/**
	 * Bouton d'enchère
	 *
	 */
	class bidButton extends JButton {
		private static final long serialVersionUID = 1L;

		private String enchere;

		/**
		 * Création du bouton
		 * @param titre
		 * @param enchere
		 */
		public bidButton(String titre, String enchere) {
			super(titre);
			this.enchere = enchere;
			addActionListener(buttonListener);
		}

		/**
		 * Création du bouton
		 * @param titre
		 * @param icon
		 * @param enchere
		 */
		public bidButton(String titre, Icon icon, String enchere) {
			super(titre, icon);
			this.enchere = enchere;
			addActionListener(buttonListener);
		}

		/**
		 * Enchère faite
		 * @return enchère
		 */
		public String getEnchere() {
			return enchere;
		}
	}

}
