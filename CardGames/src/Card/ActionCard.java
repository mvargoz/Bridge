package Card;

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

import winApp.ContexteGlobal;

public class ActionCard {

	/**
	 * lance un nouveau jeu
	 *
	 */
	public static class newAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public newAction() {
			super("new");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			CardGamePanel p = (CardGamePanel) ContexteGlobal.frame.panel;
			p.reInit();
		}
	}

	/**
	 * lance le jeu automatique
	 *
	 */
	public static class autoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public autoAction() {
			super("auto");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			CardGamePanel p = (CardGamePanel) ContexteGlobal.frame.panel;
			p.jeu.cMax = 0; // sert à itérer sur des cartes à distribuer
			p.animation();
		}

	}

	/**
	 * arrête le jeu automatique
	 *
	 */
	public static class stopAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public stopAction() {
			super("stop");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			CardGamePanel p = (CardGamePanel) ContexteGlobal.frame.panel;
			p.anim.halt();
		}
	}

	/**
	 * undo last move
	 *
	 */
	public static class undoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public undoAction() {
			super("undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			CardGamePanel p = (CardGamePanel) ContexteGlobal.frame.panel;
			p.jeu.undo();
			p.repaint();
		}
	}

	/**
	 * affiche la règle du jeu en cours
	 *
	 */
	public static class helpAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public helpAction() {
			super("help");
		}

		public void actionPerformed(ActionEvent e) {
			CardGameHelp help = new CardGameHelp(ContexteGlobal.frame, ContexteGlobal.getResourceString("titleHelp"), null, false);
		}
	}

	/**
	 * change background color
	 *
	 */
	public static class couleurAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public couleurAction() {
			super("couleur");
		}

		public void actionPerformed(ActionEvent e) {
			Color c = JColorChooser.showDialog(ContexteGlobal.frame.panel, ContexteGlobal.getResourceString("titleColor"),
					Color.blue);
			ContexteGlobal.frame.panel.setBackground(c);
			if (ContexteGlobal.frame.panel instanceof CardGamePanel) {
				((CardGamePanel) ContexteGlobal.frame.panel).backgroundColor = c;
			}
		}
	}

	/**
	 * crapette
	 *
	 */
	public static class crapetteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public crapetteAction() {
			super("crapette");
		}

		public void actionPerformed(ActionEvent e) {
			if (((CardGamePanel) ContexteGlobal.frame.panel).bJeuEnCours) {
				int st = JOptionPane.showConfirmDialog(ContexteGlobal.frame.panel,
						ContexteGlobal.getResourceString("mess5"),
						ContexteGlobal.getResourceString("mess9"), JOptionPane.YES_NO_OPTION);
				if (st == JOptionPane.NO_OPTION)
					return;
			}
			ContexteGlobal.frame.getContentPane().remove(ContexteGlobal.frame.panel);
			ContexteGlobal.frame.panel = new CrapettePanel();
			ContexteGlobal.frame.getContentPane().add("Center", ContexteGlobal.frame.panel);
			((CrapettePanel) ContexteGlobal.frame.panel).init();
			ContexteGlobal.frame.panel.revalidate();
		}
	}

	/**
	 * solitaire
	 *
	 */
	public static class solitaireAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public solitaireAction() {
			super("solitaire");
		}

		public void actionPerformed(ActionEvent e) {
			if (((CardGamePanel) ContexteGlobal.frame.panel).bJeuEnCours) {
				int st = JOptionPane.showConfirmDialog(ContexteGlobal.frame.panel,
						ContexteGlobal.getResourceString("mess5"),
						ContexteGlobal.getResourceString("mess9"), JOptionPane.YES_NO_OPTION);
				if (st == JOptionPane.NO_OPTION)
					return;
			}
			ContexteGlobal.frame.getContentPane().remove(ContexteGlobal.frame.panel);
			ContexteGlobal.frame.panel = new SolitairePanel();
			ContexteGlobal.frame.getContentPane().add("Center", ContexteGlobal.frame.panel);
			((SolitairePanel) ContexteGlobal.frame.panel).init();
			ContexteGlobal.frame.panel.revalidate();
		}
	}

	/**
	 * spider
	 *
	 */
	public static class spiderAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public spiderAction() {
			super("spider");
		}

		public void actionPerformed(ActionEvent e) {
			if (((CardGamePanel) ContexteGlobal.frame.panel).bJeuEnCours) {
				int st = JOptionPane.showConfirmDialog(ContexteGlobal.frame.panel,
						ContexteGlobal.getResourceString("mess5"),
						ContexteGlobal.getResourceString("mess9"), JOptionPane.YES_NO_OPTION);
				if (st == JOptionPane.NO_OPTION)
					return;
			}
			ContexteGlobal.frame.getContentPane().remove(ContexteGlobal.frame.panel);
			ContexteGlobal.frame.panel = new SpiderPanel();
			ContexteGlobal.frame.getContentPane().add("Center", ContexteGlobal.frame.panel);
			((SpiderPanel) ContexteGlobal.frame.panel).init();
			ContexteGlobal.frame.panel.revalidate();
		}
	}

}
