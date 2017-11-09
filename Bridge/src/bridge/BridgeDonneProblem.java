package bridge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import bridgePlay.Jeu;
import winApp.ContexteGlobal;

/**
 * Gestion des problèmes de bridge
 *
 */
public class BridgeDonneProblem {
	
	//	Constantes

	public static String[] listProblemType = ContexteGlobal.getResourceStringArray("listProblemType");
	public static String[] listProblemDonneur = ContexteGlobal.getResourceStringArray("listProblemDonneur");
	public static String[] listProblemVulnerable = ContexteGlobal.getResourceStringArray("listProblemVulnerable");
	public static String[] listProblemOption = ContexteGlobal.getResourceStringArray("listProblemOption");
	public static String fileExt = "." + ContexteGlobal.getResourceString("saveExt");
	public static String fileDescr = ContexteGlobal.getResourceString("saveMess");
	public static String fileDir = ContexteGlobal.getResourceString("saveDirProblem");

	
	//	Données du problème
	
	public String fonction;
	public String donneur;
	public String vulnerabilite;
	public int nbJoueurs;
	public boolean[] jeuDistribue;
	public String[][] jeu;
	public String[] options;
	public StringBuffer commentaire;


	/**
	 * 	Constructeur
	 */
	public BridgeDonneProblem() {
		if ( fileDir == null )
			fileDir = ".";
		init();

	}
	
	/**
	 * 	Initialisation
	 */
	private void init()  {		
		fonction = "";
		donneur = "";
		vulnerabilite = "";
		nbJoueurs = 0;
		jeu = new String[Jeu.nbJoueur][Jeu.nbCouleur];
		jeuDistribue = new boolean[Jeu.nbJoueur];
		for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++) {
			jeuDistribue[joueur] = false;
			for( int couleur = 0; couleur < Jeu.nbCouleur; couleur++ )  {
				jeu[joueur][couleur] = "";
			}
		}
		options = new String[listProblemOption.length];
		commentaire = new StringBuffer();
	}

	/**
	 * recherche d'une option
	 * @param key nom de l'option
	 * @return valeur de l'option, null si non trouvée
	 */
	public String getOption(String key)  {
		int noOption = ContexteGlobal.arrayStringSearch(listProblemOption, key);
		if ( noOption >= 0 )
			return options[noOption];
		return null;
	}
	
	/**
	 * mémorisation d'une option
	 * @param key nom de l'option
	 * @param valeur
	 * @return true ou false
	 */
	public boolean setOption(String key, String valeur)  {
		int noOption = ContexteGlobal.arrayStringSearch(listProblemOption, key);
		if ( noOption >= 0 )  {
			options[noOption] = valeur;
			return true;
		}		
		return false;		
	}
	
	/**
	 * @return Options et commentaire sous forme textuelle
	 */
	public StringBuffer getOptions()  {
		StringBuffer res = new StringBuffer();
		for ( int i = 0; i < options.length; i++ )  {
			if ( options[i] != null && options[i].length() > 0 )
				res.append(listProblemOption[i] + '=' + options[i] + '\n');
		}
		res.append(commentaire);
		return res;
	}
	
	/**
	 * <pre>
	 * Chargement d'un problème de bridge
	 * Contenu du fichier:
	 * Type de problème: Enchère, Déclarant, Flanc, Puzzle, Entame, Donne
	 * Donneur: Nord, Est, Sud, Ouest
	 * Nombre de jeux
	 * Pour chaque jeu: [Joueur / cartes P / C / K / T]
	 * Options facultatives :
	 * 	contrat=
	 *  main=<joueur ayant la main>
	 *  systemNS=<système enchère NS>
	 *  systemEO=<système enchère NS>
	 *  systemFlanc=<signalisation en flanc>
	 *  bid=<enchère déjà faites>
	 *  bidTodo=<enchères à faire>
	 *  tournoi=IMP / PAIRE
	 *  commentaire
	 * </pre>
	 * @param fileName
	 * @return true ou false
	 */
	public boolean load( String fileName )  {
        if (fileName == null)
        	return false;
		return load( new File(BridgeDonneProblem.fileDir + '/' + fileName) );
	}
	
	/**
	 * Chargement d'un problème de bridge
	 * @param fileProblem
	 * @return true ou false
	 */
	public boolean load( File fileProblem )  {
		if ( fileProblem == null || !fileProblem.exists() )  {
			return false;
		}
		init();
		String line;
		BufferedReader in;
		//		chargement
		try {
			in = new BufferedReader(new FileReader(fileProblem));
			// type de problème
			fonction = in.readLine();
			// donneur
			donneur = in.readLine();
			// vulnérabilité
			vulnerabilite = in.readLine();
			// jeux
			nbJoueurs = Integer.parseInt(in.readLine()); // nombre de jeux
			for (int j = 0; j < nbJoueurs; j++) {
				int joueur = Jeu.joueur(in.readLine()); // joueur
				jeuDistribue[joueur] = true;
				for (int couleur = Jeu.nbCouleur-1; couleur >=0; couleur--) {  // couleurs P C K T
					jeu[joueur][couleur] = in.readLine();
				}
			}
			//  options facultatives
			line = in.readLine();
			while ( line != null )  {
				int noOption = -1;
				String token[] = line.split("=");
				if ( token.length == 2 )
					noOption = ContexteGlobal.arrayStringSearch(listProblemOption, token[0]);
				if ( noOption >= 0 )  {
					options[noOption] = token[1];
				} else {
					commentaire.append(line + "\n");					
				}
				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();		
			return false;
		}
		
		return true;
	}
	
	/**
	 * chargement des options
	 * @param optionText
	 */
	public void readOptions(String optionText)  {
		String[] lines = optionText.split("\\n");
		for ( String line : lines )  {
			int noOption = -1;
			String token[] = line.split("=");
			if ( token.length == 2 )
				noOption = ContexteGlobal.arrayStringSearch(listProblemOption, token[0]);
			if ( noOption >= 0 )  {
				options[noOption] = token[1];
			} else {
				commentaire.append(line + "\n");					
			}
		}
		
	}
		
	/**
	 * <pre>
	 * contrôle et sauvegarde d'un problème de bridge
	 * @param fileName
	 * @return nom du fichier sauvegardé,
	 *  null ou *<code erreur> sinon
	 * </pre>
	 */
	public String save(String fileName)  {
		//		contrôles
 		if ( fileName == null || fileName.equals("") )   {
			return null;
		}
	    // contrôle du nombre de cartes par jeu
 		int nbJoueurs = 0;
		for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++) {  // joueurs N E S O
			if ( Jeu.nbCartes(jeu[joueur]) != 0 )  {
				if ( Jeu.nbCartes(jeu[joueur]) != 13 )  {
					return "*" + joueur;					
				}
				nbJoueurs++;				
			}
		}		
        //		fichier de sortie
	    String ext = '.' + ContexteGlobal.getResourceString("saveExt");
	    if ( !fileName.endsWith(ext) )
	    	fileName += ext;
	    String dirFileName = BridgeDonneProblem.fileDir + '/' + fileName;
        String line;
		BufferedWriter out;
		// 		écriture
		try {
			out = new BufferedWriter(new FileWriter(new File(dirFileName)));
			// type de problème
			out.write(fonction, 0, fonction.length());
			out.newLine();
			// donneur 
			out.write(donneur, 0, donneur.length());
			out.newLine();
			// vulnérabilité
			out.write(vulnerabilite, 0, vulnerabilite.length());
			out.newLine();
			// jeux
			line = Integer.toString(nbJoueurs);
			out.write(line, 0, line.length());
			out.newLine();
			for (int joueur = 0; joueur < Jeu.nbJoueur; joueur++) {  // joueurs N E S O
				if ( Jeu.nbCartes(jeu[joueur]) != 0 )  {
					line = Jeu.nomJoueurs[joueur];
					out.write(line, 0, line.length());
					out.newLine();					
					for (int couleur = Jeu.nbCouleur-1; couleur >=0; couleur--)  { // couleurs P C K T
						line = jeu[joueur][couleur];
						out.write(line, 0, line.length());
						out.newLine();
					}
				}
			}
			//  paramètres facultatifs
			line = getOptions().toString();
			out.write(line, 0, line.length());
			//  save OK
			out.close();
		} catch (Exception e) {
			e.printStackTrace();		
			return null;
		}       
		return fileName;
	}


}
