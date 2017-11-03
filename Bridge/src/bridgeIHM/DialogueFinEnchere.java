package bridgeIHM;

import java.awt.*;
import javax.swing.*;

import winApp.ContexteGlobal;

import java.awt.event.*;

/**
 * Dialogue à la fin des enchères
 */

public class DialogueFinEnchere extends JDialog {
	private static final long serialVersionUID = 1L;
	
	//		panel

	JPanel panel1 = new JPanel();
	BorderLayout borderLayout1 = new BorderLayout();
	JLabel jLabelContrat = new JLabel();
	JPanel jPanel1 = new JPanel();
	JButton jButtonQuit = new JButton();
	JButton jButtonEnch = new JButton();
	JButton jButtonJouer = new JButton();
	FlowLayout verticalFlowLayout1 = new FlowLayout();
	
	/**
	 * <pre>
	 * Actions:
	 * 0 = abandonner et passer à la donne suivante
	 * 1 = recommencer les enchères
	 * 2 = jouer les cartes
	 * 3 = imposer un contrat (non implémenté)
	 * </pre>
	 */
	int action = 0;

	//		data
	
	private static String title = ContexteGlobal.getResourceString("titleFinEnchere");
	private static boolean modal = true;

	/**
	 * Constructeur
	 */
	public DialogueFinEnchere() {
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
		jLabelContrat.setText("Contrat joué");
		jButtonQuit.setText("Abandonner la donne");
		jButtonQuit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonQuit_actionPerformed(e);
			}
		});
		jButtonEnch.setText("Recommencer les enchères");
		jButtonEnch.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonEnch_actionPerformed(e);
			}
		});
		jButtonJouer.setText("Jouer la donne");
		jButtonJouer.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jButtonJouer_actionPerformed(e);
			}
		});
		jPanel1.setLayout(verticalFlowLayout1);
		panel1.setBackground(Color.orange);
		getContentPane().add(panel1);
		panel1.add(jLabelContrat, BorderLayout.CENTER);
		panel1.add(jPanel1, BorderLayout.SOUTH);
		jPanel1.add(jButtonJouer, null);
		jPanel1.add(jButtonEnch, null);
		jPanel1.add(jButtonQuit, null);
	}

	void jButtonJouer_actionPerformed(ActionEvent e) {
		action = 2;
		dispose();
	}

	void jButtonQuit_actionPerformed(ActionEvent e) {
		action = 0;
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