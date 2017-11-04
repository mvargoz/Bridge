package Card;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.*;

import winApp.ContexteGlobal;

/**
 * Règle du jeu et paramétrage
 *
 */
public class CardGameHelp extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	/**
	 * nom du jeu
	 */
	private String gameNom;
	
	// Contenu de la boite de dialogue
	
	private JPanel panelHelp = new JPanel();
	private JScrollPane jScrollPane1;
	private JTextArea jHelp = new JTextArea(30, 50);
	private Font fontHelp = new Font("Arial", Font.BOLD, 16);
	private JPanel panelParm = new JPanel();
	private JPanel panelOK = new JPanel();
	private JButton buttonOK = new JButton("OK");

	/**
	 * Constructeur
	 * @param frame
	 * @param title
	 * @param gameNom
	 * @param modal
	 */
	public CardGameHelp(Frame frame, String title, String gameNom, boolean modal) {
		super(frame, title, modal);
		this.gameNom = gameNom;
		// System.out.println(System.getProperty("user.dir"));
		Point p = frame.getLocation();
		setLocation(p.x + 100, p.y + 100);

		try {
			dialogHelp();
			pack();
			setVisible(true);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Dialogue
	 * @throws Exception
	 */
	private void dialogHelp() throws Exception {
		panelHelp.setLayout(new BorderLayout());
		getContentPane().add(panelHelp);

		jHelp.setLineWrap(true);
		jHelp.setWrapStyleWord(true);
		jHelp.setFont(fontHelp);

		jScrollPane1 = new JScrollPane(jHelp);
		panelHelp.add(jScrollPane1, BorderLayout.NORTH);
		
		CardGamePanel jPanel = (CardGamePanel) winApp.ContexteGlobal.frame.panel;
		if ( gameNom == null )  {
			CardGame jeu = jPanel.jeu;
			if (jeu == null )  {
				jHelp.append(ContexteGlobal.getResourceString("titleCardGame"));
			} else {				
				helpFile(jHelp, new File("bin/" + jeu.nom + ".txt"));
			}
		} else {
			panelParm = jPanel.getPanelParm();
			panelHelp.add(panelParm, BorderLayout.CENTER);
			helpFile(jHelp, new File("bin/" + gameNom + ".txt"));
		}
		buttonOK.setActionCommand("ok");
		buttonOK.addActionListener(this);
		panelOK.add(buttonOK);
		panelHelp.add(panelOK, BorderLayout.SOUTH);				
	}

	/**
	 * @param jHelp
	 * @param fileIn
	 */
	private void helpFile(JTextArea jHelp, File fileIn) {

		try {
			BufferedReader in = new BufferedReader(new FileReader(fileIn));
			String line = in.readLine();
			while (line != null) {
				jHelp.append(line + "\n");
				line = in.readLine();
			}
			in.close();
		} catch (Exception ex) {
			System.out.println("Erreur lecture : " + ex);
			ex.printStackTrace();
		}
	}

	@Override
	public void actionPerformed(ActionEvent actev) {
		switch ( actev.getActionCommand() )  {
		case "ok":
			dispose();
			break;
		case "cancel":
			dispose();
			break;
		}
	}

}
