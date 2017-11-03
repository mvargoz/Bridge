package bridgeIHM;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import winApp.ContexteGlobal;

public class DialogueTestProbleme extends JDialog {

	private static final long serialVersionUID = 1L;

	private Frame parentFrame;

	//		panel
	
	private JPanel panel = new JPanel(new BorderLayout(10,20));

	//		Résultat du test
	
	private JTextArea result = new JTextArea(30,50);
	private JScrollPane resultScroll = new JScrollPane(result);
	
	//		data
	
	private static String title = ContexteGlobal.getResourceString("titleDonneProblem");
	private static boolean modal = false;

	/**
	 * 		Création de la boite de dialogue
	 */
	public DialogueTestProbleme() {
		super(winApp.ContexteGlobal.frame, title, modal);
		parentFrame = winApp.ContexteGlobal.frame;
		
    	//	panel
		
		getContentPane().add(panel);		
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel.add(resultScroll,BorderLayout.CENTER);
		
		pack();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(false);

	}
	
	/**
	 * 		Ouverture de la boite de dialogue
	 */
	public void open() {
			//	positionnement de la fenêtre
		Point p = parentFrame.getLocationOnScreen();
	    setLocation(p.x + 600, p.y + 20);
			//	positionnement de la fenêtre auto
//		setLocationRelativeTo(parentFrame);		
		setVisible(true);
	}
	
	public void setText(String texte)  {
		result.setText(texte);
		repaint();
	}

}
