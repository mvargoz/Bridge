package bridgeBid;

import java.io.*;
import java.util.*;

/**
 * Compilateur du langage d'enchères de bridge
 */

public class Compiler {
	
	//	fichiers
	
	private String source = null;
	private String dirSource = null;
	private String objet = null;
	private BufferedReader in = null;
	private BufferedReader inSave = null;
	private FileOutputStream out;
	
	// ligne courante
	
	private String line;
	private String cmd;
	private StringTokenizer STline;
	private String param;

	
	// code binaire
	
	private int sizeBuffer = 65000;
	private byte[] obj = new byte[sizeBuffer];
	private int objPnt = 0;
	private int[] pileAd = new int[100];
	private int[] pileLg = new int[100];
	private int pilePnt = -1;
	
	// valeur courante
	
	private int valeur = 0;
	
	//	paramètres
	
	static boolean debug = false;

	/**
	 * Constructeur
	 * @param source
	 * @param objet
	 */
	
	public Compiler(String source, String objet) {
		this.source = source;
		this.objet = objet;
		int posSlash = source.lastIndexOf('/');
		if ( posSlash >= 0 )
			dirSource = source.substring(0, posSlash+1);
		else
			dirSource = "";
	}
	
	/**
	 * Compilateur batch des annonces de bridge
	 * @param args : source objet
	 */
	public static void main(String[] args) {
		if (args.length == 3)
			debug = true;
		else if (args.length != 2)
			usage();
		Compiler compiler = new Compiler(args[0],args[1]);
		compiler.compil();
	}
	
	/**
	 * Compilateur
	 * @return objet
	 */
	public byte[] compil()  {
		try {
			in = new BufferedReader(new FileReader(source));
			out = new FileOutputStream(objet);
			getLine();

			while (line != null) {
				if (cmd.equalsIgnoreCase("system")) {
					// system nom = <1><lg><nom><adr fin><contenu>
					putObj(1);
					putNom(STline.nextToken());
					empileAdr();
					getLine();
				} else if (cmd.equalsIgnoreCase("finsystem")) {
					depileAdr();
					getLine();
				} else if (cmd.equalsIgnoreCase("//"))  {
					// commentaires = 4 <lg><commentaire>
					putObj(4);
					putNom(line.substring(line.indexOf("//") + 2).trim());
					getLine();
				} else if (cmd.equalsIgnoreCase("alerte")) {
					putObj(5);
					getLine();
					while (!cmd.equalsIgnoreCase("finalerte")) {
						putObj(5);
						putNom(cmd);
						putNom(line.substring(line.indexOf("=") + 1).trim());
						getLine();
						while (cmd.equalsIgnoreCase("-")) {
							putObj(6);
							putNom(line.substring(line.indexOf("-") + 1).trim());
							getLine();
						}
					}
					putObj(0);
					getLine();
				} else {
					// séquence d'enchères =
					// <2><<séquence>[<3><conditions><enchère>]n>
					putObj(2);
					empileAdr();
					// codification de la séquence d'enchères
					empileAdr();
					do {
						SequEnchere(cmd);
						if (STline.hasMoreTokens()) {
							cmd = STline.nextToken();
						} else
							break;
					} while (true);
					depileAdr();
					getLine();

					while (line != null && cmd.compareTo("=") == 0) {
						// condition enchère
						putObj(3);
						empileAdr();
						// codification des conditions
						empileAdr();
						CodeCond();
						depileAdr();
						// codification enchère
						if (STline.hasMoreTokens())
							PatternEnchere(STline.nextToken());
						else
							PatternEnchere("-");
						if (STline.hasMoreTokens()) {
							param = STline.nextToken();
							// alerte
							if (param.equalsIgnoreCase("*")) {
								if (STline.hasMoreTokens()) {
									putNom(STline.nextToken());
									if (STline.hasMoreTokens())
										param = STline.nextToken();
								} else
									erreur("manque nom alerte");
							} else
								putObj(0);
							// commentaire
							if (param.equalsIgnoreCase("//")) {
								putNom(line.substring(line.indexOf("//") + 2).trim());
							} else
								putObj(0);
						} else {
							putObj(0);
							putObj(0);
						}

						depileAdr();
						getLine();
					}
					depileAdr();
				}
			}

			in.close();
			out.write(obj, 0, objPnt);
			out.close();
			return Arrays.copyOf(obj, objPnt);
		}

		catch (IOException ioe) {
			erreur("Erreur parametres: " + ioe);
			return null;
		}
	}

	// codification de la séquence d'enchères

	private void SequEnchere(String param) {
		if (debug)
			System.out.println("Enchère : " + param);
		if (param.compareTo("?---") == 0)
			putObj(102);
		else if (param.compareTo("?-") == 0)
			putObj(103);
		else if (param.compareTo("--?-") == 0)
			putObj(104);
		else if (param.compareTo("??") == 0)
			putObj(106);
		else if (param.compareTo("?") == 0)
			putObj(108);
		else if (param.compareTo("(") == 0) {
			putObj(200);
			empileAdr();
		} else if (param.compareTo("/") == 0) {
			depileAdr();
			putObj(204);
			empileAdr();
		} else if (param.charAt(0) == ')') {
			putObj(201);
			if (param.length() > 1) {
				try {
					putObj(Integer.parseInt(param.substring(1)));
				} catch (NumberFormatException e) {
					erreur("erreur nombre après )");
				}
			} else {
				putObj(0);
			}
			depileAdr();
		} else if (param.compareTo("*") == 0)
			putObj(202);
		else if (param.startsWith("$")) // séquence nommée
		{
			putObj(203);
			putNom(param);
		} else if (PatternEnchere(param) == false)
			erreur("Enchère non reconnue : " + param);
	}

	// enchère

	private boolean PatternEnchere(String s) {
		if (s.length() == 0)
			erreur("Enchère vide");
		if (s.compareTo("-") == 0)
			putObj(105);
		else if (s.compareTo("X") == 0)
			putObj(100);
		else if (s.compareTo("XX") == 0)
			putObj(101);
		else if (s.charAt(0) == '$') // séquence nommée
		{
			putObj(107);
			putNom(s);
			String param = null;

			// couleur (facultative si S)
			if (STline.hasMoreTokens()) {
				param = STline.nextToken();
				if (PatternCouleur(param, true) != 0)
					param = null;
				else
					putObj(155);
			}
			empileAdr();

			// codification des paramètres d'une séquence nommée

			while (param != null || STline.hasMoreTokens()) {
				if (param == null)
					param = STline.nextToken();
				if (Valeur(param)) {
				} else
					erreur("Valeur non comprise: " + param);
				param = null;
			}
			depileAdr();
		} else if (s.charAt(0) == '0') {
			putObj(120);
			if (PatternCouleur(s.substring(1), true) == 0)
				return false;
		} else if (s.charAt(0) == '+') {
			if (s.charAt(1) == '1') {
				putObj(121);
				if (PatternCouleur(s.substring(2), true) == 0)
					return false;
			}
			if (s.charAt(1) == '2') {
				putObj(122);
				if (PatternCouleur(s.substring(2), true) == 0)
					return false;
			}
		} else if (s.length() >= 3 && s.substring(1, 2).equals("+")) {
			putObj(123);
			putObj(Character.getNumericValue(s.charAt(0)));
			if (PatternCouleur(s.substring(2), true) == 0)
				return false;
		} else {
			putObj(110 + Character.getNumericValue(s.charAt(0)));
			if (PatternCouleur(s.substring(1), true) == 0)
				return false;
		}
		return true;
	}

	// couleur

	private int PatternCouleur(String s) {
		return PatternCouleur(s, false);
	}

	private int PatternCouleur(String s, boolean enchere) {
		if (debug)
			System.out.println("PatternCouleur : " + s);
		if (s.length() == 0)
			return 0;
		char sc = s.charAt(0);
		int i = 1;
		if (sc == 'T')
			putObj(151);
		else if (sc == 'K')
			putObj(152);
		else if (sc == 'C')
			putObj(153);
		else if (sc == 'P')
			putObj(154);
		else if (enchere && sc == 'S')
			putObj(155);
		else if (sc == 'M') {
			if (s.length() > 1 && s.charAt(1) == '\'') {
				i++;
				putObj(158);
			} else
				putObj(156);
		} else if (sc == 'm') {
			if (s.length() > 1 && s.charAt(1) == '\'') {
				i++;
				putObj(159);
			} else
				putObj(157);
		} else if (sc == 'x')
			putObj(160);
		else if (sc == 'y')
			putObj(161);
		else if (sc == 'z')
			putObj(162);
		else if (sc == 'n')
			putObj(163);
		else if (sc == 's')
			putObj(164);
		else if (enchere && sc == '?')
			putObj(165);
		else
			return 0;
		return i;
	}

	// codification des conditions et enchère

	private void CodeCond() {
		while (STline.hasMoreTokens()) {
			String param = STline.nextToken();
			if (param.compareTo("=") == 0)
				return;
			if (Distribution(param)) {
			} else if (Valeur(param)) {
			} else if (Vulnerabilite(param)) {
			} else if (Tournoi(param)) {
			} else if (Condition(param)) {
			} else
				erreur("Token non compris: " + param);
		}
		erreur("manque = dans conditions");
	}

	// distribution

	private boolean Distribution(String param) {
		int i;
		char firstC = param.charAt(0);

		if (firstC == 'D') {
			putObj(10);
			int j;
			for (j = 0, i = 1; j < 4; j++) {
				if (param.length() >= i)
					putObj(Character.digit(param.charAt(i), 10));
				else
					erreur("Distribution incomplete");
				i++;
				if (param.length() >= i)
					i = i + PlusMoins(param.substring(i));
			}
		} else if (param.compareTo("pl") == 0)
			putObj(21);
		else if (param.compareTo("reg") == 0)
			putObj(22);
		else if (param.compareTo("sreg") == 0)
			putObj(23);
		else if (param.compareTo("sa") == 0)
			putObj(24);
		else if (param.compareTo("tri") == 0)
			putObj(25);
		else if (param.compareTo("uni") == 0)
			putObj(26);
		else if (param.compareTo("bic") == 0)
			putObj(27);
		else if (firstC == 's') {
			putObj(11);
			if (PatternCouleur(param.substring(1)) == 0)
				erreur("manque couleur singleton");
		} else if (param.length() >= 3 && param.substring(0, 3).compareTo("ctr") == 0) {
			putObj(12);
			if (PatternCouleur(param.substring(3)) == 0)
				erreur("manque couleur controle");
		} else if (firstC == 'h') {
			putObj(13);
			if ((i = PatternCouleur(param.substring(1))) == 0)
				erreur("manque couleur honneurs");
			codeCarte(param.substring(i + 1));
		} else if (Character.isDigit(firstC)) {
			i = decodeNb(param);
			int nb = valeur;
			String s = param.substring(i);

			if (s.length() == 0)
				return false;
			if (s.charAt(0) == 'h') {
				putObj(15);
				putObj(nb);
				if ((i = PatternCouleur(s.substring(1))) == 0)
					erreur("manque couleur honneurs");
			} else if (s.length() > 2 && s.substring(0, 2).compareTo("gh") == 0) {
				putObj(16);
				putObj(nb);
				if ((i = PatternCouleur(s.substring(2))) == 0)
					erreur("manque couleur gros honneurs");
			} else if (s.charAt(0) == 'A') {
				putObj(18);
				putObj(nb);
				PlusMoins(s.substring(1));
			} else if (s.charAt(0) == 'R') {
				putObj(19);
				putObj(nb);
				PlusMoins(s.substring(1));
			} else if (s.length() > 2 && s.substring(0, 2).compareTo("cl") == 0) {
				putObj(20);
				putObj(nb);
				if ((i = PatternCouleur(s.substring(2))) == 0)
					erreur("manque couleur atout");
				PlusMoins(s.substring(3));
			} else if (s.length() > 3 && s.substring(0, 3).compareTo("tsa") == 0) {
				putObj(17);
				putObj(nb);
				if ((i = PatternCouleur(s.substring(3))) == 0)
					erreur("manque couleur tenus SA");
				PlusMoins(s.substring(i));
			} else if ((i = PatternCouleur(s)) > 0) {
				putObj(nb);
				PlusMoins(s.substring(i));
			} else
				return false;
		} else
			return false;
		return true;
	}

	// valeur de la main

	private boolean Valeur(String param) {
		int min = 0;
		int max = 99;
		int i = decodeNb(param);
		min = valeur;
		if (i > 0) {
			String s = param.substring(i);
			// nombre mini-maxi
			if (s.charAt(0) == '-') {
				i = decodeNb(s.substring(1));
				max = valeur;
				s = s.substring(i + 1);
			} else {
				max = min;
				// + ou - à la fin
				if (s.charAt(s.length() - 1) == '+') {
					s = s.substring(0, s.length() - 1);
					max = 99;
				} else if (s.charAt(s.length() - 1) == '-') {
					s = s.substring(0, s.length() - 1);
					max = max - 1;
					min = 0;
				}
			}

			// type

			if (s.compareTo("H") == 0)
				putObj(51);
			else if (s.compareTo("HL") == 0)
				putObj(52);
			else if (s.compareTo("DH") == 0)
				putObj(53);
			else if (s.compareTo("J") == 0)
				putObj(54);
			else if (s.compareTo("LJ") == 0)
				putObj(55);
			else if (s.compareTo("pe") == 0)
				putObj(56);
			else if (s.charAt(0) == 'H') {
				putObj(60);
				PatternCouleur(s.substring(1));
			} else if (s.charAt(0) == 'S') {
				putObj(61);
				if (PatternCouleur(s.substring(1)) == 0)
					erreur("manque couleur de soutien");
			} else
				return false;

			putObj(min);
			putObj(max);
		} else
			return false;
		return true;
	}

	// Condition

	private boolean Condition(String param) {
		putObj(70);
		int i = PatternCouleur(param);
		if (i > 0) {
			if (param.charAt(i) == '=') {
				putObj(170);
			} else if (param.charAt(i) == '>') {
				if (param.charAt(i + 1) == '=') {
					i++;
					putObj(172);
				} else
					putObj(171);
			} else if (param.charAt(i) == '<') {
				if (param.charAt(i + 1) == '=') {
					i++;
					putObj(174);
				} else if (param.charAt(i + 1) == '>') {
					i++;
					putObj(175);
				} else
					putObj(173);
			}
			i++;
			PatternCouleur(param.substring(i));
		} else
			return false;
		return true;
	}

	// vulnérabilité

	private boolean Vulnerabilite(String param) {
		if (param.compareTo("V") == 0)
			putObj(81);
		else if (param.compareTo("N") == 0)
			putObj(82);
		else if (param.compareTo("NV") == 0)
			putObj(83);
		else if (param.compareTo("VN") == 0)
			putObj(84);
		else if (param.compareTo("MV") == 0)
			putObj(85);
		else if (param.compareTo("VV") == 0)
			putObj(86);
		else if (param.compareTo("NN") == 0)
			putObj(87);
		else
			return false;
		return true;
	}

	// type de tournoi

	private boolean Tournoi(String param) {
		if (param.compareTo("TPP") == 0)
			putObj(88);
		else if (param.compareTo("IMP") == 0)
			putObj(89);
		else
			return false;
		return true;
	}

	// carte

	private void codeCarte(String s) {
		int i;
		for (i = 0; i < s.length(); i++) {
			if (s.charAt(i) == 'A')
				putObj(1);
			else if (s.charAt(i) == 'R')
				putObj(2);
			else if (s.charAt(i) == 'D')
				putObj(3);
			else if (s.charAt(i) == 'V')
				putObj(4);
			else if (s.charAt(i) == 'X')
				putObj(5);
		}
		putObj(0);
	}

	// + -

	private int PlusMoins(String s) {
		if (s.length() == 0) {
			putObj(253);
			return 0;
		} else if (s.charAt(0) == '+') {
			putObj(251);
			return 1;
		} else if (s.charAt(0) == '-') {
			putObj(252);
			return 1;
		} else {
			putObj(253);
			return 0;
		}
	}

	// nombre

	private int decodeNb(String param) {
		valeur = 0;
		int i = 0;
		char c = param.charAt(i);
		while (Character.isDigit(c)) {
			valeur = 10 * valeur + Character.digit(c, 10);
			i++;
			c = param.charAt(i);
		}
		return i;
	}

	// erreur

	private void erreur(String cause) {
		System.out.println(" *** ");
		System.out.println(cause);
		System.exit(1);
	}

	// lecture ligne

	private void getLine() throws IOException {
		String param;
		line = in.readLine();
		if (line == null && inSave != null) {
			in.close();
			in = inSave;
			inSave = null;
			line = in.readLine().trim();
		}
		if (line != null) {
			System.out.println(">>" + line);
			STline = new StringTokenizer(line, " \t");
			if (STline.hasMoreTokens()) {
				cmd = STline.nextToken();
				if (cmd.equalsIgnoreCase("include")) {
					inSave = in;
					param = STline.nextToken();
					in = new BufferedReader(new FileReader(dirSource + param + ".txt"));
					getLine();
				}
			} else
				getLine();
		}
	}

	// erreurs de paramètres

	static void usage() {
		System.out.println("Usage: java BridgeComp <fichier source> <fichier binaire>");
		System.exit(-10);
	}

	// conversion int en byte

	private byte byteValue(int pi) {
		Integer i = new Integer(pi);
		return i.byteValue();
	}

	// empile l'adresse de l'emplacement d'une adresse de fin

	private void empileAdr() {
		pilePnt++;
		pileAd[pilePnt] = objPnt;
		pileLg[pilePnt] = 0;
		objPnt += 2;
	}

	// fin de champ, mémorise l'adresse de fin

	private void depileAdr() {
		putObj(0);
		obj[pileAd[pilePnt]] = byteValue(objPnt >> 8);
		obj[pileAd[pilePnt] + 1] = byteValue(objPnt);
		pilePnt--;
	}

	// mémorise un byte

	private void putObj(int valeur) {
		if (valeur > 255)
			erreur("valeur octet > 255");

		if (objPnt < sizeBuffer) {
			Integer i = new Integer(valeur);
			obj[objPnt++] = i.byteValue();
		} else {
			erreur("Débordement buffer Obj");
		}
	}

	// mémorise un nom sous forme <longueur><nom 1 caractère/byte>

	private void putNom(String param) {
		if (param.length() > 255)
			erreur("longueur string > 255");

		byte[] tmp = param.getBytes();
		putObj(byteValue(param.length()));
		for (int i = 0; i < param.length(); i++)
			putObj(tmp[i]);
	}

}
