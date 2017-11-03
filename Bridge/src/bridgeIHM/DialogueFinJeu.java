package bridgeIHM;

import java.awt.*;
import javax.swing.*;

import winApp.ContexteGlobal;

import java.awt.event.*;

/**
 * Dialogue à la fin du jeu
 */

public class DialogueFinJeu extends JDialog {
	private static final long serialVersionUID = 1L;
	
	//		panel

	JPanel panel1 = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JLabel jLabelContrat = new JLabel();
	JPanel jPanel1 = new JPanel();
	JButton jButtonEnch = new JButton();
	FlowLayout verticalFlowLayout1 = new FlowLayout();
	JButton jButtonRejouer = new JButton();
	JButton jButtonSuivant = new JButton();
	
	/**
	 * <pre>
	 * Actions:
	 * 0 = passer à la donne suivante
	 * 1 = recommencer les enchères
	 * 2 = rejouer les cartes
	 * </pre>
	 */
	int action = 0;

	//		data
	
	private static String title = ContexteGlobal.getResourceString("titleFinJeu");
	private static boolean modal = true;
	
	/**
	 * Constructeur
	 */
	public DialogueFinJeu() {
		super(winApp.ContexteGlobal.frame, title, modal);
		try {
			jbInit();
			pack();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	void jbInit() throws Exception {
		panel1.setLayout(borderLayout1);
		jLabelContrat.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelContrat.setHorizontalTextPosition(SwingConstants.CENTER);
		jLabelContrat.setText("Résultat du contrat joué");
		jButtonEnch.setText("Refaire les enchères");
		jButtonEnch.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonEnch_actionPerformed(e);
			}
		});
		jPanel1.setLayout(verticalFlowLayout1);
		jButtonRejouer.setText("Rejouer la donne");
		jButtonRejouer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonRejouer_actionPerformed(e);
			}
		});
		jButtonSuivant.setText("Passer à la donne suivante");
		jButtonSuivant.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonSuivant_actionPerformed(e);
			}
		});
		panel1.setBackground(Color.orange);
		getContentPane().add(panel1);
		panel1.add(jLabelContrat, BorderLayout.CENTER);
		panel1.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(jButtonSuivant, null);
		jPanel1.add(jButtonRejouer, null);
		jPanel1.add(jButtonEnch, null);
	}

	void jButtonSuivant_actionPerformed(ActionEvent e) {
		action = 0;
		dispose();
	}

	void jButtonRejouer_actionPerformed(ActionEvent e) {
		action = 2;
		dispose();
	}

	void jButtonEnch_actionPerformed(ActionEvent e) {
		action = 1;
		dispose();
	}

	public void setContrat(String contrat) {
		jLabelContrat.setText(contrat);
	}

	public int getAction() {
		return action;
	}

}