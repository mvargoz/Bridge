package winApp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class couleurAction extends AbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public couleurAction() {
		super("couleur");
	}

	public void actionPerformed(ActionEvent e) {
		Color c = JColorChooser.showDialog(ContexteGlobal.frame.panel, "Choix de la couleur de fond", Color.blue);
		ContexteGlobal.frame.panel.setBackground(c);
	}
}
