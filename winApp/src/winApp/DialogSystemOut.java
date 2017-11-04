package winApp;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import winApp.ContexteGlobal;

/**
 *  redirige System.out vers une boite
 *
 */
public class DialogSystemOut extends JDialog {

	private static final long serialVersionUID = 1L;

	private Frame parentFrame;

	//		panel
	
	private JPanel panel = new JPanel(new BorderLayout(10,20));

	//		Résultat du test
	
	private JTextArea result = new JTextArea(30,50);
	private JScrollPane resultScroll = new JScrollPane(result);
	
	//		data
	
	private static String title = "System.out";
	private static boolean modal = false;

	/**
	 * interception System.out
	 *
	 */
	private class Interceptor extends PrintStream {
	    public Interceptor(OutputStream out) {
	        super(out, true);
	    }
	    @Override
	    public void print(String s) {
	    	super.print(s);
	    	result.append(s);
			repaint();
	    }
	    @Override
	    public void println(String s) {
	    	super.println(s);
	    	result.append(s + '\n');
			repaint();
	    }
	}
	
	/**
	 * 		Création de la boite de dialogue
	 */
	public DialogSystemOut() {
		super(winApp.ContexteGlobal.frame, title, modal);
		parentFrame = winApp.ContexteGlobal.frame;
		
	    PrintStream origOut = System.out;
	    PrintStream interceptor = new Interceptor(origOut);
	    System.setOut(interceptor);
		
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
	Point p = ContexteGlobal.frame.getLocationOnScreen();
	setLocation(p.x + 10, p.y + 20);
			//	positionnement de la fenêtre auto
//		setLocationRelativeTo(parentFrame);		
		setVisible(true);
	}
	
	/**
	 * Remplace le texte
	 * @param texte
	 */
	public void setText(String texte)  {
		result.setText(texte);
		repaint();
	}
	
	/**
	 * Ajoute un texte
	 * @param texte
	 */
	public void append(String texte)  {
		result.append(texte);
		repaint();
	}

}
