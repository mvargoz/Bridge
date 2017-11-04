package winApp;

import javax.swing.UIManager;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ContexteGlobal {
		//	taille �cran
	public static Dimension TailleEcran;
		//  Properties
	private static ResourceBundle resources;
		// user properties
	private static Properties appliProps = new Properties();
	private static String userProps = "user.properties";
		//	Frame
	public static WinAppFrame frame;
		//	interception System.out
	public static DialogSystemOut dialogSystemOut = null; 

	/**
	 * Initialisation application
	 * @param appli
	 */
	public static void init(String appli) {
		TailleEcran = Toolkit.getDefaultToolkit().getScreenSize();
		
		// ressources de l'application
		try {
			resources = ResourceBundle.getBundle(appli, Locale.getDefault());
		} catch (MissingResourceException mre) {
			System.err.println("properties file not found");
			System.exit(1);
		}
		
		// propri�t�s utilisateur
		try {
			FileInputStream in = new FileInputStream(userProps);
			appliProps.load(in);
			in.close();
		} catch (IOException ex) {
		}
		
		// look & feel
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (Exception e) {
			System.err.println("Look and Feel not found");
		}
		
		//	interception System.out		
		dialogSystemOut = new DialogSystemOut(); 

	}

	/**
	 * Lecture d'une propri�t�
	 * @param nom
	 * @return valeur
	 */
	public static String getResourceString(String nom) {
		String str = appliProps.getProperty(nom);;
		try {
			if ( str == null )
				str = resources.getString(nom);
		} catch (MissingResourceException mre) {
			str = null;
		}
		return str;
	}

	/**
	 * Lecture d'une propri�t� liste:
	 * textes s�par�s par , avec �ventuellement des blancs derri�re
	 * @param nom
	 * @return valeur
	 */
	public static String[] getResourceStringArray(String nom) {
		String str = appliProps.getProperty(nom);
		try {		
			if ( str == null )
				str = resources.getString(nom);
			if ( str != null )
				return str.split(",\\s*");
		} catch (MissingResourceException mre) {
		}
		return null;
	}

	/**
	 * Lecture d'une propri�t� URL
	 * @param key
	 * @return URL
	 */
	public static URL getResource(String key) {
		String name = getResourceString(key);
		if (name != null) {
			URL url = ContexteGlobal.class.getResource(name);
			if (url == null)
				url = ClassLoader.getSystemResource(name);
			return url;
		}
		return null;
	}
	
	/**
	 * Recherche d'un texte dans un tableau en ignorant les majuscules
	 * @param arrayString tableau
	 * @param key texte � rechercher
	 * @return index de key dans arrayString, -1 si absent
	 */
	public static int arrayStringSearch(String[] arrayString, String key)  {
		for ( int i = 0; i < arrayString.length; i++ )  {
			if ( arrayString[i].equalsIgnoreCase(key))
				return i;
		}
		return -1;		
	}
	
	/**
	 * Ecriture d'une propri�t� utilisateur
	 * @param key
	 * @param value
	 */
	public static void putProperty(String key, String value) {
		appliProps.put(key, value);
	}

	/**
	 * Sauvegarde des propri�t�s utilisateur
	 */
	public static void saveProperties() {
		try {
			FileOutputStream out = new FileOutputStream(userProps);
			appliProps.store(out, "user properties");
			out.close();
		} catch (IOException ex) {
			System.err.println("coudn't save properties file");
			System.exit(1);
		}
	}
}
