package bridgeIHM;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import bridge.Bridge;
import bridge.BridgeDonneProblem;
import bridgePlay.Jeu;
import winApp.ContexteGlobal;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;

/**
 * Boite de dialogue pour la création et modification des fichiers de donne de bridge
 *
 */
public class DialogueSaisieProbleme extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private Frame parentFrame;

	//		panel

	private JPanel panel = new JPanel(new BorderLayout(10,20));
	private JPanel spanel = new JPanel(new BorderLayout(10,10));
	private JPanel panelSaisie = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

	//		Identifiant
	private JPanel panelKey = new JPanel(new FlowLayout());
	private JButton dirKeyButton = new JButton(ContexteGlobal.getResourceString("dirKeyButton"));
	private JButton newKeyButton = new JButton(ContexteGlobal.getResourceString("newKeyButton"));
	private JLabel problemKey = new JLabel(ContexteGlobal.getResourceString("problemKey"));
	private Vector<String> listFileKey = new Vector<String>();
	private JComboBox<String> listFile = new JComboBox<String>(listFileKey);
	private JButton deleteButton = new JButton(ContexteGlobal.getResourceString("deleteButton"),
								   new ImageIcon(ContexteGlobal.getResource("deleteImage")));
	private JCheckBox debugCheck = new JCheckBox(ContexteGlobal.getResourceString("debugButton"));
	//		Type
	private JPanel panelType = new JPanel(new GridLayout(3,1,2,5));
	private JLabel problemType = new JLabel(ContexteGlobal.getResourceString("problemType"));
	private JComboBox<String> chooseProblemType = new JComboBox<String>(BridgeDonneProblem.listProblemType);
	
	//		Donneur
	private JLabel problemDonneur = new JLabel(ContexteGlobal.getResourceString("problemDonneur"));
	private JComboBox<String> chooseProblemDonneur = new JComboBox<String>(BridgeDonneProblem.listProblemDonneur);
	
	//		Vulnérabilité
	private JLabel problemVulnerable = new JLabel(ContexteGlobal.getResourceString("problemVulnerable"));
	private JComboBox<String> chooseProblemVulnerable = new JComboBox<String>(BridgeDonneProblem.listProblemVulnerable);

	//		données facultatives et commentaires
	
	private JTextArea dataProblem = new JTextArea(8,30);
	private JScrollPane scrollDataProblem = new JScrollPane(dataProblem);
	
	//		Jeux
	private JPanel panelJeu = new JPanel(new FlowLayout(FlowLayout.CENTER,80,0));
	private ButtonGroup groupJeu = new ButtonGroup();
	private JRadioButton joueurN = new JRadioButton(Jeu.nomJoueurs[0],true);
	private JRadioButton joueurE = new JRadioButton(Jeu.nomJoueurs[1]);
	private JRadioButton joueurS = new JRadioButton(Jeu.nomJoueurs[2]);
	private JRadioButton joueurW = new JRadioButton(Jeu.nomJoueurs[3]);
	private JButton btClear = new JButton(ContexteGlobal.getResourceString("ResetJoueur"));
	private JButton btFill = new JButton(ContexteGlobal.getResourceString("FillJoueur"));

	// 		Cartes
	private JPanel panelSaisieJeu = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private String[][] cartes = new String[Jeu.nbJoueur][Jeu.nbCouleur];
	private JList<String>[] jeuCartes = new JList[Jeu.nbJoueur];
	private JPopupMenu popupMenuJeu = new JPopupMenu();
	
	//		Validation
	private JPanel panelValid = new JPanel(new FlowLayout());
	private JButton testButton = new JButton(ContexteGlobal.getResourceString("testButton"),
			  					  new ImageIcon(ContexteGlobal.getResource("testImage")));
	private JButton validButton = new JButton(ContexteGlobal.getResourceString("validButton"),
								  new ImageIcon(ContexteGlobal.getResource("validImage")));
	private JButton cancelButton = new JButton(ContexteGlobal.getResourceString("cancelButton"),
								   new ImageIcon(ContexteGlobal.getResource("cancelImage")));
	
	// 		Cartes à distribuer
	private JPanel panelCartes = new JPanel(new GridLayout(Jeu.nbCouleur,Jeu.nbCartesCouleur+1));
	private JButton[][] jeuCarteDistrib = new JButton[Jeu.nbCouleur][Jeu.nbCartesCouleur];

	//		Résultat du test
	private DialogueTestProbleme dialogResult = null;
	
	//		data
	
	private static String title = ContexteGlobal.getResourceString("titleDonneProblem");
	private static String cartesDistrib = ContexteGlobal.getResourceString("cartesDistrib");
	private static boolean modal = false;
	private static Dimension dimComboBoxKey = new Dimension(250,25);
	private static Dimension dimComboBoxData = new Dimension(150,25);
		
	/**
	 * 		Création de la boite de dialogue
	 */
	public DialogueSaisieProbleme() {
		super(ContexteGlobal.frame, title, modal);
		parentFrame = ContexteGlobal.frame;
		
        	//	panel
		getContentPane().add(panel);		
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
			
			//	saisie identifiant
		panelKey.add(problemKey);
		panelKey.add(dirKeyButton);
		dirKeyButton.addActionListener(this);
		panelKey.add(newKeyButton);
		newKeyButton.addActionListener(this);
		panelKey.add(listFile);
		panelKey.add(deleteButton);
		listFile.addActionListener(this);
		listFile.setActionCommand("listFile");
		listFile.setEditable(true);
		listFile.setPreferredSize(dimComboBoxKey);
		if ( !listFileKey.isEmpty() )
			listFile.setSelectedIndex(0);
		deleteButton.addActionListener(this);
		panelKey.add(debugCheck);
		
		panel.add(panelKey,BorderLayout.NORTH);

		//		Type, donneur, vulnérabilité	
		panelType.add(problemType);
		chooseProblemType.setPreferredSize(dimComboBoxData);
		panelType.add(chooseProblemType);
		panelType.add(problemDonneur);
		chooseProblemDonneur.setPreferredSize(dimComboBoxData);
		panelType.add(chooseProblemDonneur);
		panelType.add(problemVulnerable);
		chooseProblemVulnerable.setPreferredSize(dimComboBoxData);
		panelType.add(chooseProblemVulnerable);
		
		panelSaisie.add(panelType);
		
		//		données facultatives et commentaires
		
		panelSaisie.add(scrollDataProblem);
		
		spanel.add(panelSaisie,BorderLayout.NORTH);
		
		//		jeux
		
		btClear.addActionListener(new ListenerCarte(9));
		panelJeu.add(btClear);

		groupJeu.add(joueurN);
		groupJeu.add(joueurE);
		groupJeu.add(joueurS);
		groupJeu.add(joueurW);
		panelJeu.add(joueurN);
		panelJeu.add(joueurE);
		panelJeu.add(joueurS);
		panelJeu.add(joueurW);
		
		btFill.addActionListener(new ListenerCarte(8));
		panelJeu.add(btFill);
		
		spanel.add(panelJeu,BorderLayout.CENTER);

		//		cartes
		
		for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++) {
			cartes[joueur][0] = "";
			cartes[joueur][1] = "";
			cartes[joueur][2] = "";
			cartes[joueur][3] = "";
			jeuCartes[joueur] = new JList<String>(cartes[joueur]);
			jeuCartes[joueur].addMouseListener(new ListMouseListener(joueur));
			jeuCartes[joueur].setFixedCellWidth(100);
			jeuCartes[joueur].setFixedCellHeight(20);
			jeuCartes[joueur].setBorder(
					BorderFactory.createCompoundBorder(
					BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(Jeu.nomJoueurs[joueur]),
					BorderFactory.createEmptyBorder(5, 5, 5, 5)),
					jeuCartes[joueur].getBorder()));

			panelSaisieJeu.add(jeuCartes[joueur]);
		}
		spanel.add(panelSaisieJeu,BorderLayout.SOUTH);
		
		//  panel central
		
		panel.add(spanel,BorderLayout.CENTER);
		
		//  jeu de carte à distribuer
		
		panelCartes.setBorder(
				BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(cartesDistrib),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)),
				panelCartes.getBorder()));
		for (int couleur = 0; couleur < Jeu.nbCouleur; couleur++) {			
			panelCartes.add(new JLabel(iconCouleur(couleur)));
			ListenerCarte listenerCarte = new ListenerCarte(couleur);
			for (int carte = 0; carte < Jeu.nbCartesCouleur; carte++) {
				JButton button = new JButton(""+Jeu.hauteurToChar(carte));
				jeuCarteDistrib[couleur][carte] = button;
				button.addActionListener(listenerCarte);
				panelCartes.add(button);
			}
		}		
		panel.add(panelCartes,BorderLayout.EAST);
		
			//	validation
		
		panelValid.add(testButton);
		testButton.addActionListener(this);	
		testButton.setEnabled(true);
		
		panelValid.add(validButton);
		validButton.addActionListener(this);	
		validButton.setEnabled(true);
		
		panelValid.add(cancelButton);	
		cancelButton.addActionListener(this);	
		cancelButton.setEnabled(true);
		
		panel.add(panelValid,BorderLayout.SOUTH);
		
		pack();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(false);
	}

	//	
	
	private ImageIcon iconCouleur(int couleur)  {
		String[] image = {"spade", "heart", "diamond", "club"};
		return new ImageIcon(ContexteGlobal.getResource(image[couleur] + "Image"));
	}
	
	/**
	 * Ouverture de la boite de dialogue
	 */
	public void open() {
			//	positionnement de la fenêtre
	//	Point p = parentFrame.getLocationOnScreen();
	//  setLocation(p.x + 100, p.y + 100);
			//	positionnement de la fenêtre auto
		setLocationRelativeTo(parentFrame);		
		listFile();
		setVisible(true);
	}
	
	/**
	 * liste des fichiers de donne
	 */
	private void listFile()	{
		File dirCourant = new File(BridgeDonneProblem.fileDir);
		File[] listFileSource = dirCourant.listFiles(new FileFilter() { 
	         public boolean accept(File dir)  {
	        	 return dir.isFile() && !dir.isHidden() &&
	           		    dir.getName().endsWith(BridgeDonneProblem.fileExt);
	         }
		} );
		listFileKey.clear();
		listFile.setSelectedIndex(-1);
		for ( File f: listFileSource )  {
			listFileKey.add(f.getName());
		}
		if ( !listFileKey.isEmpty() )
			listFile.setSelectedIndex(0);
	}
	
	/**
	 * Initialisation des données à saisir
	 */
	private void init()  {
		chooseProblemType.setSelectedIndex(0);
		chooseProblemDonneur.setSelectedIndex(0);
		chooseProblemVulnerable.setSelectedIndex(0);
		dataProblem.setText("");
		joueurN.setSelected(true);
		for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++) {
			for( int couleur = 0; couleur < Jeu.nbCouleur; couleur++ )  {
				cartes[joueur][couleur] = "";
			}
			jeuCartes[joueur].repaint();
		}
		for (int couleur = 0; couleur < Jeu.nbCouleur; couleur++) {	
			for (int carte = 0; carte < Jeu.nbCartesCouleur; carte++) {
				jeuCarteDistrib[couleur][carte].setEnabled(true);
			}	
		}		
	}
	
	/**
	 * Actions
	 */
	public void actionPerformed(ActionEvent e) {
			//		save
		if (e.getActionCommand().equals(validButton.getText())) {
			String fname = saveDonne();
			if ( fname == null )
				return;
			//  nouvelle donne
 			if ( !listFileKey.contains(fname) )  {
 				listFile.setSelectedIndex(-1);
				listFileKey.add(fname);
				listFileKey.sort(null);
			}
			listFile.setSelectedItem(fname);
			return;
		}	
			//		cancel
		if (e.getActionCommand().equals(cancelButton.getText())) {			
			setVisible(false);
			return;
		}	
			//		delete
		if (e.getActionCommand().equals(deleteButton.getText())) {	
			String fname = deleteDonne();
			if ( fname != null )  {
				listFileKey.remove(fname);
			}
			if ( !listFileKey.isEmpty() )
				listFile.setSelectedIndex(0);
			else
				listFile.setSelectedIndex(-1);
			return;
		}
			//		change directory
		if (e.getActionCommand().equals(dirKeyButton.getText())) {
			JFileChooser jFileChooserPlay = new JFileChooser(BridgeDonneProblem.fileDir);
			jFileChooserPlay.setDialogTitle("Choisir le répertoire des donnes problèmes");
			jFileChooserPlay.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jFileChooserPlay.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
				BridgeDonneProblem.fileDir = jFileChooserPlay.getSelectedFile().getPath();
				winApp.ContexteGlobal.putProperty("saveDirProblem", BridgeDonneProblem.fileDir);
				listFile();
			}
		
		}
			//		combobox fichiers paramètres
		if (e.getActionCommand().equals("listFile") )   {
			readDonne();
			return;
		}		
			//		new
		if (e.getActionCommand().equals(newKeyButton.getText()) )   {
			init();
			return;
		}
			//		teste de l'enchère
		if (e.getActionCommand().equals(testButton.getText())) {
			String fname = saveDonne();
			if ( fname == null )
				return;
			//  nouvelle donne
 			if ( !listFileKey.contains(fname) )  {
 				listFile.setSelectedIndex(-1);
				listFileKey.add(fname);
				listFileKey.sort(null);
			}
			listFile.setSelectedItem(fname);
	        Thread thread1 = new Thread(new Runnable() {
	            @Override
	            public void run() {
	                String fileName = (String) listFile.getSelectedItem();
	                if (fileName == null)
	                	return;
	        		File fileProblem = new File(BridgeDonneProblem.fileDir + '/' + fileName);
	        		if ( fileProblem == null || !fileProblem.exists() )  {
	        			return;
	        		}
	        		try {
	        			if ( dialogResult == null )  {
	        				dialogResult = new DialogueTestProbleme();
	        			}      				
        				Bridge bridge = new Bridge(true,true);
	        			dialogResult.setText(bridge.testeDonne(fileProblem));
	        			dialogResult.open();
	        			if ( debugCheck.isSelected() )
	        				ContexteGlobal.dialogSystemOut.open();
	        		} catch (Exception ex) {
	        			ex.printStackTrace();	        			
	        		}
	            }
	        });
	        thread1.start();			

		}
	}
	
	/**
	 * Lecture d'un fichier problème de bridge
	 * @return true ou false
	 */
	private boolean readDonne()  {
        String fileName = (String) listFile.getSelectedItem();
        if (fileName == null)
        	return false;
		File fileProblem = new File(BridgeDonneProblem.fileDir + '/' + fileName);
		if ( fileProblem == null || !fileProblem.exists() )  {
			return false;
		}
        BridgeDonneProblem pb = new BridgeDonneProblem();
        if ( pb.load(fileProblem) )  {
    		init();
			// type de problème
			chooseProblemType.setSelectedItem(pb.fonction);
			// donneur
			chooseProblemDonneur.setSelectedItem(pb.donneur);
			// vulnérabilité
			chooseProblemVulnerable.setSelectedItem(pb.vulnerabilite);
			// jeux
			for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++) {
				for( int couleur = 0; couleur < Jeu.nbCouleur; couleur++ )  {
					cartes[joueur][couleur] = pb.jeu[joueur][couleur];
					for ( int c = 0; c < cartes[joueur][couleur].length(); c++ )  {
						jeuCarteDistrib[couleur][Jeu.hauteurToInt(cartes[joueur][couleur].charAt(c))].setEnabled(false);						
					}					
				}
				jeuCartes[joueur].repaint();
			}
			// options
			dataProblem.append(pb.getOptions().toString());
			return true;
        } else {
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErrRead"),
					ContexteGlobal.getResourceString("messReadFileProblem"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}	
	}
	
	/**
	 * Ecriture d'un fichier problème de bridge
	 * @return nom du fichier sauvegardé, null sinon
	 */
	private String saveDonne()  {
	    String fileName = (String) listFile.getSelectedItem();
        BridgeDonneProblem pb = new BridgeDonneProblem();
        pb.fonction = (String) chooseProblemType.getSelectedItem();
        pb.donneur = (String) chooseProblemDonneur.getSelectedItem();
        pb.vulnerabilite = (String) chooseProblemVulnerable.getSelectedItem();
        pb.jeu = cartes;
		pb.readOptions(dataProblem.getText());         
		String res = pb.save(fileName);
		//  erreur d'écriture
 		if ( res == null )   {
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErrSave"),
					ContexteGlobal.getResourceString("messSaveFileProblem"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
	    // erreur contrôle du nombre de cartes par jeu
		if ( res.charAt(0) == '*' )  {
			int joueur = Integer.parseInt(res.substring(1));
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErrSaisieCartes") + " " + Jeu.nomJoueurs[joueur],
					ContexteGlobal.getResourceString("messSaveFileProblem"),
					JOptionPane.ERROR_MESSAGE);
			return null;					
		}		
		JOptionPane.showMessageDialog(this,
				ContexteGlobal.getResourceString("messSaveOK"),
				ContexteGlobal.getResourceString("messSaveFileProblem"),
				JOptionPane.INFORMATION_MESSAGE);
		winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messSaveOK"));
		return res;
	}

	
	/**
	 * Supprimer fichier problème de bridge sélectionné
	 * @return nom du fichier supprimé, null sinon
	 */
	public String deleteDonne()  {
        String fileName = (String) listFile.getSelectedItem();
		File fileProblem = new File(BridgeDonneProblem.fileDir + '/' + fileName);
		if ( fileProblem != null && fileProblem.exists() )  {
			int okno = JOptionPane.showConfirmDialog(this,
					ContexteGlobal.getResourceString("messConfirm"),
		             ContexteGlobal.getResourceString("messDeleteFileProblem"),
		             JOptionPane.YES_NO_OPTION);
			if ( okno == JOptionPane.OK_OPTION )  {
				try  {
					Files.delete(fileProblem.toPath());
					return fileName;
		        } catch (IOException e) {
					JOptionPane.showMessageDialog(this,
							ContexteGlobal.getResourceString("messErreur"),
							ContexteGlobal.getResourceString("messDeleteFileProblem"),
							JOptionPane.ERROR_MESSAGE);
					e.printStackTrace();		
		        }
			}
		}			
		return null;
	}
	
	/**
	 * Listener pour la distribution des cartes
	 *
	 */
	public class ListenerCarte implements ActionListener {
		private int couleur;

		public ListenerCarte(int couleur) {
			this.couleur = couleur;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//   recherche du joueur sélectionné
			int joueur = 0;
			if ( joueurE.isSelected() ) {
				joueur = 1;
			} else if ( joueurS.isSelected() ) {
				joueur = 2;
			} else if ( joueurW.isSelected() ) {
				joueur = 3;
			}
			
			//   ajout carte pour le joueur sélectionné
			if ( couleur < Jeu.nbCouleur )  {
				char carte = e.getActionCommand().charAt(0);
				cartes[joueur][couleur] = Jeu.ajouteCarte(cartes[joueur][couleur], carte);
				jeuCartes[joueur].repaint();
				((JButton) e.getSource()).setEnabled(false);
				return;
			}
			
			//  reset cartes pour le joueur sélectionné
			
			if ( e.getActionCommand().equals(btClear.getText() ))  {
				for( int couleur = 0; couleur < Jeu.nbCouleur; couleur++ )  {
					for ( int c = 0; c < cartes[joueur][couleur].length(); c++ )  {
						jeuCarteDistrib[couleur][Jeu.hauteurToInt(cartes[joueur][couleur].charAt(c))].setEnabled(true);						
					}					
					cartes[joueur][couleur] = "";
				}
				jeuCartes[joueur].repaint();
				return;
			}
			
			// 	remplir le jeux sélectionné avec les cartes restantes
			
			if ( e.getActionCommand().equals(btFill.getText() ))  {
				for( int couleur = 0; couleur < Jeu.nbCouleur; couleur++ )  {
					for ( int c = 0; c < Jeu.nbCartesCouleur; c++ )  {
						if ( jeuCarteDistrib[couleur][c].isEnabled() )  {
							cartes[joueur][couleur] = Jeu.ajouteCarte(cartes[joueur][couleur], Jeu.hauteurToChar(c));
							jeuCarteDistrib[couleur][c].setEnabled(false);
						}
					}					
				}
				jeuCartes[joueur].repaint();
				return;
			}
			
		}
		
	}
	
		//	mouse listener pour les jeux de carte
	
	protected class ListMouseListener implements MouseListener, ActionListener  {
	
		private JList<String> list;
		private int joueur;
		private int couleur;
			
		public ListMouseListener(int joueur) {
			this.joueur = joueur;
		}
	
		@Override
		public void mouseClicked(java.awt.event.MouseEvent e) {	
		}
	
		@Override
		public void mouseEntered(java.awt.event.MouseEvent e) {
		}
	
		@Override
		public void mouseExited(java.awt.event.MouseEvent e) {
		}
	
		@Override
		public void mousePressed(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
				displayMenuJeu(e);
	        }
		}
	
		@Override
		public void mouseReleased(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
				displayMenuJeu(e);
	        }
		}
	
	    	//	un clic droit affiche le menu
		
		private void displayMenuJeu(java.awt.event.MouseEvent e)	{
			
			//  mémorisation de la couleur
			list = (JList<String>) e.getComponent();
			couleur = list.locationToIndex(e.getPoint());
	
			//  affichage popup
			popupMenuJeu.removeAll();
	    	JMenuItem menuItem;
	    	menuItem = new JMenuItem(ContexteGlobal.getResourceString("Reset"));
			menuItem.addActionListener(this);
			popupMenuJeu.add(menuItem);
			popupMenuJeu.show(e.getComponent(), e.getX(), e.getY());						
		}
	
			//		Reset couleur
		
		public void actionPerformed(ActionEvent e) {
			for ( int c = 0; c < cartes[joueur][couleur].length(); c++ )  {
				jeuCarteDistrib[couleur][Jeu.hauteurToInt(cartes[joueur][couleur].charAt(c))].setEnabled(true);						
			}					
			cartes[joueur][couleur] = "";
			jeuCartes[joueur].repaint();
			return;
		}
		
	}

}
