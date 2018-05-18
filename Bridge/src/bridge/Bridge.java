package bridge;

import java.io.*;
import java.util.*;

import bridgeBid.Interpreter;
import bridgeIHM.BridgePanel;
import bridgePlay.DonnePlay;
import bridgePlay.Jeu;
import bridgePlay.Simulator;
import winApp.ContexteGlobal;
import bridgeBid.DonneBid;
import bridgeBid.Compiler;

/**
 * 		Bridge : Test du système d'enchère et du jeu de la carte
 */

public class Bridge {
	
	/**
	 * lecture du fichier txt
	 */
	static BufferedReader in = null;
	static String line;
	static String cmd;
	static StringTokenizer STline;
	/**
	 * interpréteur d'enchères
	 */
	static Interpreter SysEnchere;
	/**
	 * système d'enchère
	 */
	static String sytemEnchere;
	/**
	 * type de tournoi
	 */
	static String tournoi;
	/**
	 * debug
	 */
	static boolean debug = false;
	/**
	 * code retour
	 */
	static int codeRetour = 0;

	/**
	 * constructeur
	 * @param debug
	 */
	public Bridge(boolean debug, boolean compil) {
		ContexteGlobal.init("appli");
		String fileObjet = ContexteGlobal.getResourceString("baseDir") + "/"
						 + ContexteGlobal.getResourceString("encheres");
		if ( compil )  {
			String fileSource = ContexteGlobal.getResourceString("baseDir") + "/"
							  + ContexteGlobal.getResourceString("encheresSource");
			Compiler compiler = new Compiler(fileSource,fileObjet);
			if ( compiler.compil() ==  null )
				System.out.println("Erreur de compilation");
		}
		SysEnchere = new Interpreter(fileObjet, debug);
		sytemEnchere = ContexteGlobal.getResourceString("sytemEnchere");
		tournoi = ContexteGlobal.getResourceString("tournoi");
	}
	
	/**
	 * Test des annonces de bridge et du jeu de la carte automatique
	 * @param args <fichier d'enchère> <fichier ou répertoire source> [debug]
	 */
	public static void main(String[] args) {
		
		//	Paramètres
		
		if (args.length == 2)
			debug = true;
		else if (args.length != 1)
			usage();
		
		//	initialisation
		
		Bridge bridge = new Bridge(debug, false);

		//	traitement des fichiers
		try {
			File file = new File(args[0]);
			if (file.isDirectory()) {
				traiteRepertoire(file);
			} else {
				if (file.getName().endsWith(".txt"))
					traiteFichier(file);
				else if (file.getName().endsWith(BridgeDonneProblem.fileExt))
					System.out.println(testeDonne(file));					
			}
		} catch (Exception ex) {
			System.out.println("Erreur : " + ex);
			ex.printStackTrace();
			codeRetour = 99;
		}

		if (codeRetour > 0) {
			System.out.println("Erreurs détectées");
		} else {
			System.out.println("Fin OK");
		}

		System.exit(codeRetour);
	}

	/**
	 * Traitement d'un répertoire
	 * @param directory
	 * @throws Exception
	 */
	private static void traiteRepertoire(File directory) throws Exception {
		File list[] = directory.listFiles();
		int lg = list.length;
		for (int i = 0; i < lg; i++) {
			if (list[i].isDirectory())
				traiteRepertoire(list[i]);
			else if (list[i].getName().endsWith(".txt"))
				traiteFichier(list[i]);
			else if (list[i].getName().endsWith(BridgeDonneProblem.fileExt))
				System.out.println(testeDonne(list[i]));					
		}		
	}
	
	/**
	 * Test donne problème
	 * @param fileName
   	 * @return résultat du test
	 * @throws Exception
	 */
	public static String testeDonne(File fileName) throws Exception {
		String print = "Problème: " + fileName.getName() + '\n';
        BridgeDonneProblem pb = new BridgeDonneProblem();
        if ( pb.load(fileName) )  {
        	if ( pb.fonction.equalsIgnoreCase(BridgeDonneProblem.listProblemType[0]))  {
        		print += testEnchere(pb);
        	} else if ( pb.fonction.equalsIgnoreCase(BridgeDonneProblem.listProblemType[1]))  {
        		print += testJeuCarte(pb);
        	}

        }
        return print;
	}
	
   	/**
   	 * Test enchères
   	 * @param pb
   	 * @return résultat du test
   	 * @throws Exception
   	 */
	private static String testEnchere(BridgeDonneProblem pb) throws Exception {
		// options
		String systemNS = pb.getOption("systemNS");
		if ( systemNS == null ) systemNS = sytemEnchere;
		String systemEO = pb.getOption("systemEO");
		if ( systemEO == null ) systemEO = sytemEnchere;
		String typeTournoi = pb.getOption("typeTournoi");
		if ( typeTournoi == null ) typeTournoi = tournoi;

		String print = " V:" + pb.vulnerabilite + " NS:" + systemNS + " EO:" + systemEO + " " + typeTournoi + '\n';
		
		//  enchères déjà faites
		String bid = pb.getOption("bid");
		//  enchères à faire
		String bidTodo = pb.getOption("bidTodo");
		if ( bidTodo != null )  {
			bidTodo = bidTodo.trim().replaceAll("\\s+", " ");
		}
		
  		DonneBid donne = new DonneBid(pb.donneur, pb.vulnerabilite, systemNS, systemEO, typeTournoi);
  		for( int joueur = 0; joueur < Jeu.nbJoueur; joueur++ )  {
  			if ( pb.jeuDistribue[joueur] )  {
				donne.putJeu(joueur, pb.jeu[joueur][3], pb.jeu[joueur][2], pb.jeu[joueur][1], pb.jeu[joueur][0]);
				print += "   " + Jeu.nomJoueurs[joueur];
				if ( joueur == Jeu.joueur(pb.donneur) )
					print += " donneur\n";
				else
					print += '\n';
				for (int couleur=0; couleur < Jeu.nbCouleur; couleur++)  {
					if ( pb.jeu[joueur][couleur].length() == 0 )
						print += "-\n";
					else	
						print += pb.jeu[joueur][couleur] + '\n';
				}
  			}
  		}

		//  simulation
  		
		int nbPasse = 0;

		//  simuler les enchères déjà faites
		
		if ( bid != null )  {
			bid =  bid.trim();
			bidTodo = bid + ' ' + bidTodo;
			String enchere[] = bid.split("\\s");
			for ( String s: enchere ) {
				donne.putEnchere(s);
				donne.JoueurSuivant();								
					// comptage des passes
				if (s.equals("-"))
					nbPasse++;
				else
					nbPasse = 0;
			}
		} else  {
			bid = "";
		}

		//	simulation enchères
		
		while (nbPasse < 3) {
			String s;
			// les jeux non distribués passent
			if ( pb.jeuDistribue[donne.getJoueur()] )
				s = SysEnchere.getEnchere(donne);
			else
				s = "-";
			// comptage des passes
			if (s.equals("-"))
				nbPasse++;
			else
				nbPasse = 0;
			// faire l'enchère
			donne.putEnchere(s);
			bid += " " + s;
			donne.JoueurSuivant();
		}
		bid = bid.trim();
		
		//  affichage résultat
		
		print += "   Enchères : " + bid + '\n';
		if ( !bid.equals(bidTodo))  {
			print += "Erreur enchères attendues : " + bidTodo + '\n';
			codeRetour = 10;
		}
		print += "-----\n";
		return print;
	}
	
   	/**
   	 * Test du jeu de la carte
   	 * @param pb
   	 * @return résultat du test
   	 * @throws Exception
   	 */
	private static String testJeuCarte(BridgeDonneProblem pb) throws Exception {
		String print = "";
		// options
		
		int declarant = Jeu.joueur(pb.getOption("declarant"));
		String contrat = pb.getOption("contrat");
		int hauteurContrat = Integer.parseInt(contrat.substring(0,1));
		int atout = Jeu.couleurToInt(contrat.charAt(1));
		String main = pb.getOption("main");
		int joueurMain = -1;
		if ( main != null )
			joueurMain = Jeu.joueur(main);
		
		//	cartes déjà jouées
		
		String[] playCard = null;
		String carteJoue = pb.getOption("carteJoue");
		if ( carteJoue !=  null)  {
			playCard = carteJoue.split("\\s");
		}
		
		//	simulation
		// pour les donnes partielles la hauteur du contrat = nombre de plis à faire
		
		int profondeur = 2;
		DonnePlay donnePlay = new DonnePlay(pb.jeu, declarant, hauteurContrat, atout, joueurMain);
		Simulator simulator = new Simulator(donnePlay, profondeur);

		//  jeu
		
		int tour;
		int nbCartes = Jeu.nbCartes(pb.jeu[0]);
		String carte;
		String pli;
		for ( tour = 1; tour <= nbCartes; tour++ )  {
			pli = "";
			for (int i = 0; i < Jeu.nbJoueur; i++) {
				if ( tour == 1 && playCard != null && playCard.length > i )
					carte = playCard[i];
				else
					carte = simulator.getCarte();
				pli += " " + carte;
				simulator.putCarte(carte);
			}
			print += "Tour : " + tour + " : " + pli + '\n';
			print += "  - valeur jeu NS : " + donnePlay.gagnante[0] + '/' + donnePlay.perdante[0] + '\n';
			print += "  - valeur jeu EO : " + donnePlay.gagnante[1] + '/' + donnePlay.perdante[1] + '\n';
		}
		
		return print;
	}
		// options

	/**
	 * Erreur de paramètres
	 */
	private static void usage() {
		System.out.println("Usage: java Bridge <fichier ou répertoire source> [debug]");
		System.exit(20);
	}
	
	/**
	 * Traitement de l'ancien format des donnes problèmes
	 * @param file
	 */
	private static void traiteFichier(File file) {
		boolean errEnch;
		String fonction;
		int nbJoueurs = 0;
		String systemNS;
		String systemEO;
		String donneur;
		String vulnerabilite;
		String TypeTournoi;
		String jeu[][] = new String[4][4];
		DonneBid donne;
		String EncheresResultat[] = new String[50];
		int nbEnchR;
		int i, j;

		try {
			in = new BufferedReader(new FileReader(file));
			getLine();
			while (line != null) {
				// chargement des paramètres

				nbEnchR = 0;
				if (!cmd.equalsIgnoreCase("***"))
					erreur("Début donne non reconnue");
				fonction = getToken("fonction");
				if (fonction.equalsIgnoreCase("1j")) // enchère 1 joueur
					nbJoueurs = 1;
				else if (fonction.equalsIgnoreCase("2j")) // enchère 2 joueurs
					nbJoueurs = 2;
				else if (fonction.equalsIgnoreCase("4j")) // enchère 4 joueurs
					nbJoueurs = 4;
				else if (fonction.equalsIgnoreCase("rev")) // ???
					nbJoueurs = 0;
				else if (fonction.equalsIgnoreCase("pb4j")) // problème à 4 jeux
					nbJoueurs = 4;

				donneur = getToken("donneur");
				vulnerabilite = getToken("vulnérabilité");
				systemNS = getToken("système NS");
				systemEO = getToken("système EO");
				TypeTournoi = getToken("type de tournoi");

				donne = new DonneBid(donneur, vulnerabilite, systemNS, systemEO, TypeTournoi);
				getLine();

				// chargement des donnes NESW couleur PCKT

				for (i = 0; i < nbJoueurs && line != null; i++) {
					for (j = 3; j >= 0 && line != null; j--) {
						if (cmd == null)
							erreur("manque jeux");
						if (cmd.equalsIgnoreCase("-"))
							jeu[i][j] = "";
						else
							jeu[i][j] = substitueCartex(cmd);
						getLine();
					}
				}

					// traitement des enchères

				if (nbJoueurs <= 1) {
					if (cmd.equalsIgnoreCase("E=")) {
						while (STline.hasMoreTokens()) {
							String s = STline.nextToken();
							donne.putEnchere(s);
							donne.JoueurSuivant();
							EncheresResultat[nbEnchR++] = s;
						}
						getLine();
					}
					if (nbJoueurs == 1) {
						j = donne.getJoueur();
						donne.putJeu(j, jeu[0][0], jeu[0][1], jeu[0][2], jeu[0][3]);
						if (debug)
							donne.getJeu(j).print();
					}
				} else if (nbJoueurs == 2) {
					donne.putJeu(0, jeu[0][0], jeu[0][1], jeu[0][2], jeu[0][3]);
					donne.putJeu(2, jeu[1][0], jeu[1][1], jeu[1][2], jeu[1][3]);
					if (debug)
						donne.getJeu(0).print();
					if (debug)
						donne.getJeu(2).print();
				} else if (nbJoueurs == 4) {
					donne.putJeu(0, jeu[0][0], jeu[0][1], jeu[0][2], jeu[0][3]);
					donne.putJeu(1, jeu[1][0], jeu[1][1], jeu[1][2], jeu[1][3]);
					donne.putJeu(2, jeu[2][0], jeu[2][1], jeu[2][2], jeu[2][3]);
					donne.putJeu(3, jeu[3][0], jeu[3][1], jeu[3][2], jeu[3][3]);
				}

				if (cmd.equalsIgnoreCase("R=")) {
					while (STline.hasMoreTokens())
						EncheresResultat[nbEnchR++] = STline.nextToken();
					getLine();
				}

				if (fonction.equalsIgnoreCase("1j"))
					donne.putEnchere(SysEnchere.getEnchere(donne));
				else if (fonction.equalsIgnoreCase("2j")) {
					String s = SysEnchere.getEnchere(donne);
					donne.putEnchere(s);
					donne.JoueurSuivant();
					donne.putEnchere("-");
					donne.JoueurSuivant();
					s = SysEnchere.getEnchere(donne);
					donne.putEnchere(s);
					while (!s.equals("-")) {
						donne.JoueurSuivant();
						donne.putEnchere("-");
						donne.JoueurSuivant();
						s = SysEnchere.getEnchere(donne);
						donne.putEnchere(s);
					}
				} else if (fonction.equalsIgnoreCase("4j")) {
					int nbPasse = 0;
					while (nbPasse < 3) {
						String s = SysEnchere.getEnchere(donne);
						if (s.equals("-"))
							nbPasse++;
						else
							nbPasse = 0;
						donne.putEnchere(s);
						donne.JoueurSuivant();
					}
				}

				if (!fonction.equalsIgnoreCase("pb4j")) {
					System.out.print("Enchère : ");
					errEnch = false;
					if (nbEnchR != donne.getnbEnch())
						errEnch = true;
					for (i = 0; i < donne.getnbEnch(); i++) {
						System.out.print(donne.getEnchere(i) + " ");
						if (nbEnchR >= i && !donne.getEnchere(i).equals(EncheresResultat[i])) {
							System.out.print("(" + EncheresResultat[i] + ")");
							errEnch = true;
						}
					}
					System.out.println(" ");
					if (errEnch) {
						System.out.println("Erreur enchères");
						codeRetour = 1;
					} else
						System.out.println("Enchères OK");
					System.out.println("-----------------------------------------");
				}
				getLine();
			}
			in.close();
		} catch (IOException ioe) {
			System.out.println("Erreur paramètres: " + ioe);
		} catch (Exception ex) {
			System.out.println("Erreur : " + ex);
			ex.printStackTrace();
		}
	}

	/**
	 * lecture ligne
	 * @throws IOException
	 */
	private static void getLine() throws IOException {
		line = in.readLine();
		if (line != null) {
			System.out.println(">>" + line);
			STline = new StringTokenizer(line, " ");
			if (STline.hasMoreTokens()) {
				cmd = STline.nextToken();
				if (cmd.equals("//"))
					getLine();
			} else
				getLine();
		}
	}

	/**
	 * lecture token
	 * @param nomToken
	 * @return token
	 */
	private static String getToken(String nomToken) {
		if (!STline.hasMoreTokens())
			erreur("Il manque " + nomToken);
		return STline.nextToken();
	}

	/**
	 * Sortie erreur
	 * @param cause
	 */
	private static void erreur(String cause) {
		System.out.println(" *** ");
		System.out.println(cause);
		System.exit(0);
	}

	/**
	 * Substitue les cartes notées x par de petites cartes de 2 à 9
	 * @param jeu
	 * @return jeu substitué
	 */
	private static String substitueCartex(String jeu) {
		int c = jeu.indexOf('x');
		if (c < 0)
			return jeu;
		return jeu.substring(0, c) + "98765432".substring(0, jeu.length() - c);
	}

}
