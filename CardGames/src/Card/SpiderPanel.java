package Card;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import winApp.ContexteGlobal;

/**
 * 		Présentation du jeu Spider
 *
 */
public class SpiderPanel extends CardGamePanel implements ActionListener, ItemListener  {

	private static final long serialVersionUID = 1L;
	
		// paramétrage du jeu
	
	/**
	 * nombre de couleurs
	 */
	private int nbColors = 4;
	
	/**
	 * dégagement des couleurs reconstituées sur les tas
	 */
	private boolean bReconst = true;
	private JCheckBox bReconstCheck = new JCheckBox("mettre couleurs entières sur tas");
	
	/**
	 * pouvoir mettre le Roi sur l'As
	 */
	private boolean bRoiSurAs = false;
	private JCheckBox bRoiSurAsCheck = new JCheckBox("mettre le Roi sur l'As");
	
	/**
	 * constructeur
	 */
	public SpiderPanel() {
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
		
		new CardGameHelp(ContexteGlobal.frame, ContexteGlobal.getResourceString("titleBegin"), "spider", true);

		// définition du board

		dimBoard = 19;
		board = new Rectangle[dimBoard];

		// talon case 10

		board[10] = new Rectangle(10, 10, widthCard, heightCard);

		// cases de réception des suites complètes de 11 à 18

		for (int i = 11; i < 19; i++) {
			board[i] = new Rectangle(10 + (i - 9) * (widthCard + CardSpace), 10, widthCard, heightCard);
		}

		// 10 cases de jeu de 0 à 9

		for (int i = 0; i < 10; i++) {
			board[i] = new Rectangle(10 + i * (widthCard + CardSpace), 10 + heightCard + CardSpace, widthCard, 600);
		}

		// cartes de dessus visibles

		lookBoard = new int[dimBoard];
		for (int i = 0; i < 10; i++)
			lookBoard[i] = 1;

		// initialisation du panel

		super.init();

		// création de l'objet spider

		jeu = new Spider(nbColors, bReconst, bRoiSurAs);
		jeu.init();
	}

	/**
	 * paramétrage du jeu
	 */
	public JPanel getPanelParm() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());

		// choix du nombre de couleurs
		
		JLabel nbColorLabel = new JLabel("Nombre de couleurs : ");
		Integer[] nbColorChoose = {1,2,4};
		JComboBox<Integer> nbColor = new JComboBox<Integer>(nbColorChoose);
		nbColorLabel.setLabelFor(nbColor);
		nbColor.setSelectedItem(nbColors);
		nbColor.setActionCommand("nbColor");
		nbColor.addActionListener(this);
		
		bReconstCheck.setSelected(bReconst);
		bReconstCheck.addItemListener(this);
		bReconstCheck.setActionCommand("Reconst");
		
		bRoiSurAsCheck.setSelected(bRoiSurAs);
		bRoiSurAsCheck.addItemListener(this);
		bRoiSurAsCheck.setActionCommand("RoiSurAs");
		
		panel.add(nbColorLabel);
		panel.add(nbColor);
		panel.add(bReconstCheck);
		panel.add(bRoiSurAsCheck);

		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		switch(e.getActionCommand())  {
		case "nbColor":
			nbColors = (int) ((JComboBox<Integer>) e.getSource()).getSelectedItem();
			break;
		}
	}
	
	public void itemStateChanged(ItemEvent e) {
		JCheckBox source = (JCheckBox) e.getItemSelectable();
	    if (source == bReconstCheck) {
	    	bReconst = bReconstCheck.isSelected();		    		
	    } else if (source == bRoiSurAsCheck) {
	    	bRoiSurAs = bRoiSurAsCheck.isSelected();
	    }		    		
	}

}