package winApp;

import java.awt.*;

/*
 *  Lancement d'une application fenêtrée
 *  l'argument donne le nom du fichier properties qui fixe les paramètres de l'application
 *  appli par défaut
 */

public class WinAppIHM {

	public WinAppIHM() {
		WinAppFrame frame = new WinAppFrame();
		int vSize = Integer.parseInt(ContexteGlobal.getResourceString("screenVerticalSize"));
		int hSize = Integer.parseInt(ContexteGlobal.getResourceString("screenHorizontalSize"));
		frame.setSize(new Dimension(hSize, vSize));
		frame.setLocation((ContexteGlobal.TailleEcran.width - hSize) / 2,
				(ContexteGlobal.TailleEcran.height - vSize) / 2);
		frame.validate();
		frame.setVisible(true);
		ContexteGlobal.frame = frame;
	}

	public static void main(String[] args) {
		String nameAppli = "appli";
		if (args.length > 0)
			nameAppli = args[0];
		ContexteGlobal.init(nameAppli);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new WinAppIHM();
			}
		});
	}

}
