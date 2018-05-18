package bridgeBid;

import java.io.*;

/**
 * Décompilateur du langage d'enchères de bridge
 */

class Decompiler {
	static FileInputStream in;
	// code binaire
	static int sizeBuffer = 65000;
	static int size = 0;
	static byte[] obj = new byte[sizeBuffer];
	static int objPnt = 0;
	static int[] pileAd = new int[100];
	static int[] pileLg = new int[100];
	static int pilePnt = -1;

	static boolean debug = false;
	static boolean doc = false;
	static String texte = "";

	/**
	 * Décompilateur batch du langage d'enchères de bridge
	 * @param args nom du fichier à décompiler + [doc] pour documentation
	 */
	public static void main(String[] args) {

		if (args.length >= 2) {
			if (args[1].equalsIgnoreCase("doc"))
				doc = true;
			else
				debug = true;
		} else if (args.length < 1)
			usage();

		try {
			in = new FileInputStream(args[0]);
			size = in.read(obj);
			decompil(size);
			in.close();
		}

		catch (IOException ioe) {
			System.out.println("Erreur parametres: " + ioe);
		}
	}

	/**
	 * décompilateur
	 * @param size
	 */
	static void decompil(int size) {
		int i, cd, ad, ads;
		String param;

		while (objPnt < size) {
			cd = getObj();
			if (cd == 1) {
				// system nom = <1><lg><nom><adr fin><contenu>
				param = decodeNom();
				ad = getAdr();
				println("System " + param, "");
				decompil(ad);
				println("finSystem", "");
			} else if (cd == 2) {
				// séquence d'enchères =
				// <2><adr fin><séquence>[<3><condition enchère>]n
				ad = getAdr();
				ads = getAdr();
				SequEnchere(ads);
				i = getObj();
				while (i == 3) {
					// condition enchère = <adr fin><conditions>
					ad = getAdr();
					ads = getAdr();
					// codification des conditions
					print("   = ", "");
					CodeCond(ads);
					// codification enchère
					print(" = ", " --> enchère: ");
					PatternEnchere(getObj());
					// alerte
					param = decodeNom();
					if (param.length() > 0)
						print(" * " + param, " *" + param);
					// commentaire
					param = decodeNom();
					if (param.length() > 0)
						print("  // " + param, " " + param);

					println("", "");
					i = getObj();
					i = getObj();
				}
			} else if (cd == 0) {
				if (debug)
					System.out.println("*** fin ***");
			} else if (cd == 4) {
				// commentaire = 4<lg><texte>
				param = decodeNom();
				println(" ", " ");
				println("// " + param, "\r\n     " + param + "\r\n");
			} else if (cd == 5) {
				// alerte = 5[5<lg><nom><lg><texte>]...0
				println("Alerte", "Alertes");
				cd = getObj();
				while (cd == 5) {
					String nom = decodeNom();
					String descr = decodeNom();
					println("  " + nom + " = " + descr);
					cd = getObj();
					while (cd == 6) {
						descr = decodeNom();
						println(" - " + descr);
						cd = getObj();
					}
				}
				println("finAlerte", "");
			} else
				erreur("Instruction inconnue : " + String.valueOf(cd));

		}
	}

	/**
	 * erreurs de paramètres
	 */
	static void usage() {
		System.out.println("Usage: java BridgeInt <fichier binaire>");
		System.exit(0);
	}

	/**
	 * codification de la séquence d'enchères
	 * @param size
	 */
	static void SequEnchere(int size) {
		if (debug)
			System.out.println("Séquence enchère " + String.valueOf(size));
		int ad = 0;
		int i = getObj();
		if (i == 203) {
			println(decodeNom() + " ");
			i = getObj();
			return;
		}
		while (i > 0 || ad > 0) {
			if (i == 102)
				print("?--- ");
			else if (i == 103)
				print("?- ");
			else if (i == 104)
				print("--?- ");
			else if (i == 106)
				print("?? ");
			else if (i == 108)
				print("? ");
			else if (i == 200) {
				print("( ");
				ad = getAdr();
			} else if (i == 204) {
				print("/ ");
				ad = getAdr();
			} else if (i == 201) {
				print(")" + String.valueOf(getObj()) + " ");
				ad = getObj(); // consomme 0 final
			} else if (i == 202)
				print("* ");
			else if (PatternEnchere(i))
				print(" ");
			else if (i > 0)
				erreur("Enchere non reconnue : " + String.valueOf(i));
			i = getObj();
		}
		println(" ");
	}

	/**
	 * enchère
	 * @param i
	 * @return
	 */
	static boolean PatternEnchere(int i) {
		if (i == 105)
			print("-");
		else if (i == 100)
			print("X");
		else if (i == 101)
			print("XX");
		else if (i == 107) {
			print(decodeNom() + " ");
			PatternCouleur();
			print(" ");
			CodeCond(getAdr());
		} else if (i == 120) {
			print("0");
			PatternCouleur();
		} else if (i == 121) {
			print("+1");
			PatternCouleur();
		} else if (i == 122) {
			print("+2");
			PatternCouleur();
		} else if (i == 123) {
			print(String.valueOf(Character.forDigit(getObj(), 10)));
			print("+");
			PatternCouleur();
		} else if (i > 110 && i <= 117) {
			print(String.valueOf(Character.forDigit(i - 110, 10)));
			PatternCouleur();
		} else
			return false;
		return true;
	}

	/**
	 * couleur
	 */
	static void PatternCouleur() {
		int i = getObj();
		String s = PatternCouleurString(i);
		if (s.length() > 0)
			print(s);
		else
			erreur("Couleur non reconnue : " + String.valueOf(i));
	}

	/**
	 * couleur
	 * @return
	 */
	static String PatternCouleurString() {
		int i = getObj();
		return PatternCouleurString(i);
	}

	/**
	 * couleur
	 * @param i
	 * @return
	 */
	static String PatternCouleurString(int i) {
		if (i == 151)
			return ("T");
		else if (i == 152)
			return ("K");
		else if (i == 153)
			return ("C");
		else if (i == 154)
			return ("P");
		else if (i == 155)
			return ("S");
		else if (i == 156)
			return ("M");
		else if (i == 157)
			return ("m");
		else if (i == 158)
			return ("M'");
		else if (i == 159)
			return ("m'");
		else if (i == 160)
			return ("x");
		else if (i == 161)
			return ("y");
		else if (i == 162)
			return ("z");
		else if (i == 163)
			return ("n");
		else if (i == 164)
			return ("s");
		else if (i == 165)
			return ("?");
		else
			return ("");
	}

	/**
	 * codification des conditions et enchère
	 * @param size
	 */
	static void CodeCond(int size) {
		if (debug)
			System.out.println("Condition " + String.valueOf(size));
		print("", "  - ");
		while (objPnt < size) {
			int i = getObj();
			if (i == 0)
				return;
			if (Distribution(i)) {
			} else if (Valeur(i)) {
			} else if (Vulnerabilite(i)) {
			} else if (Tournoi(i)) {
			} else if (Condition(i)) {
			} else
				erreur("Condition non comprise : " + String.valueOf(i));
			print(" ", ", ");
		}
		erreur("manque fin dans conditions");
	}

	/**
	 * distribution
	 * @param i
	 * @return
	 */
	static boolean Distribution(int i) {
		if (i == 10) {
			print("D", "distribution");
			int j;
			for (j = 0; j < 4; j++) {
				i = getObj();
				print(i + PlusMoins());
			}
		} else if (i == 21)
			print("pl", "plat");
		else if (i == 22)
			print("reg", "régulier");
		else if (i == 23)
			print("sreg", "semi-régulier");
		else if (i == 24)
			print("sa", "sans-atout");
		else if (i == 25)
			print("tri", "tricolore");
		else if (i == 26)
			print("uni", "unicolore");
		else if (i == 27)
			print("bic", "bicolore");
		else if (i == 11) {
			print("s", "singleton ");
			PatternCouleur();
		} else if (i == 12) {
			print("ctr", "contrôle ");
			PatternCouleur();
		} else if (i == 13) {
			String c = PatternCouleurString();
			String cc = codeCarte();
			print("h" + c + cc, "honneur " + c + ":" + cc);
		} else if (i == 15) {
			i = getObj();
			print(i + "h", i + " honneurs mini ");
			PatternCouleur();
		} else if (i == 16) {
			i = getObj();
			print(i + "gh", i + " gros honneurs mini ");
			PatternCouleur();
		} else if (i == 18) {
			i = getObj();
			String pm = PlusMoins();
			print(i + "A" + pm, pm + i + " As");
		} else if (i == 19) {
			i = getObj();
			String pm = PlusMoins();
			print(i + "R" + pm, pm + i + " Roi");
		} else if (i == 20) {
			i = getObj();
			String c = PatternCouleurString();
			String pm = PlusMoins();
			print(i + "cl" + c + pm, pm + i + " clefs atout " + c);
		} else if (i == 17) {
			i = getObj();
			String c = PatternCouleurString();
			String pm = PlusMoins();
			print(i + "tsa" + c + pm, pm + i + " tenues SA " + c);
		} else {
			String c = PatternCouleurString(i);
			if (c.length() > 0) {
				i = getObj();
				String pm = PlusMoins();
				print(i + c + pm, pm + i + " cartes " + c);
			} else
				return false;
		}
		return true;
	}

	/**
	 * valeur de la main
	 * @param i
	 * @return
	 */
	static boolean Valeur(int i) {
		int min = 0;
		int max = 99;
		String type;

		if (i > 0) {
			// type
			if (i == 51)
				type = "H";
			else if (i == 52)
				type = "HL";
			else if (i == 53)
				type = "DH";
			else if (i == 54)
				type = "J";
			else if (i == 55)
				type = "LJ";
			else if (i == 56)
				type = "pe";
			else if (i == 60)
				type = "H" + PatternCouleurString();
			else if (i == 61)
				type = "S" + PatternCouleurString();
			else
				return false;

			min = getObj();
			max = getObj();
			if (min == max)
				print(String.valueOf(max) + type);
			else if (min > 0 && max < 99)
				print(String.valueOf(min) + "-" + String.valueOf(max) + type);
			else if (min == max)
				print(String.valueOf(max) + type);
			else if (min == 0)
				print(String.valueOf(max + 1) + type + "-");
			else
				print(String.valueOf(min) + type + "+");
		} else
			return false;
		return true;
	}

	/**
	 * Condition
	 * @param i
	 * @return
	 */
	static boolean Condition(int i) {
		if (i == 70) {
			PatternCouleur();
			i = getObj();
			if (i == 170)
				print("=");
			else if (i == 171)
				print(">");
			else if (i == 172)
				print(">=");
			else if (i == 173)
				print("<");
			else if (i == 174)
				print("<=");
			else if (i == 175)
				print("<>");
			PatternCouleur();
		} else
			return false;
		return true;
	}

	/**
	 * vulnérabilité
	 * @param i
	 * @return
	 */
	static boolean Vulnerabilite(int i) {
		if (i == 81)
			print("V", " vulnérable");
		else if (i == 82)
			print("N", " non vulnérable");
		else if (i == 83)
			print("NV", " non vulnérable contre vulnérable");
		else if (i == 84)
			print("VN", " vulnérable contre non vulnérable");
		else if (i == 85)
			print("MV", " même vulnérablilité");
		else if (i == 86)
			print("VV", " tous vulnérables");
		else if (i == 87)
			print("NN", " personne vulnérable");
		else
			return false;
		return true;
	}

	/**
	 * type de tournoi
	 * @param i
	 * @return
	 */
	static boolean Tournoi(int i) {
		if (i == 88)
			print("TPP", " tournoi par paire");
		else if (i == 89)
			print("IMP", " match par quatre");
		else
			return false;
		return true;
	}

	/**
	 * carte
	 * @return
	 */
	static String codeCarte() {
		String s = "";
		int i = getObj();
		while (i > 0) {
			if (i == 1)
				s += returnString("A", " As");
			else if (i == 2)
				s += returnString("R", " Roi");
			else if (i == 3)
				s += returnString("D", " Dame");
			else if (i == 4)
				s += returnString("V", " Valet");
			else if (i == 5)
				s += returnString("X", " Dix");
			i = getObj();
		}
		return s;
	}

	/**
	 * + -
	 * @return
	 */
	static String PlusMoins() {
		int i = getObj();
		if (i == 253)
			return "";
		else if (i == 251)
			return returnString("+");
		else if (i == 252)
			return returnString("-");
		else
			erreur(" Code +/- non reconnu : " + String.valueOf(i));
		return "";
	}

	/**
	 * erreur
	 * @param cause
	 */
	static void erreur(String cause) {
		System.out.println(" *** ");
		System.out.println(cause);
		System.exit(0);
	}

	/**
	 * lecture d'un byte
	 * @return
	 */
	static int getObj() {
		Byte cd = new Byte(obj[objPnt++]);
		int i = cd.intValue();
		if (i < 0)
			i = i + 256;
		if (debug)
			System.out.println("Byte=" + String.valueOf(i));
		return i;
	}

	/**
	 * lecture d'une adresse de fin
	 * @return
	 */
	static int getAdr() {
		Byte cd = new Byte(obj[objPnt++]);
		int i = cd.intValue();
		if (i < 0)
			i = i + 256;
		cd = new Byte(obj[objPnt++]);
		int j = cd.intValue();
		if (j < 0)
			j = j + 256;
		i = i * 256 + j;
		if (debug)
			System.out.println("Adr=" + String.valueOf(i));
		return i;
	}

	/**
	 * décodage d'un nom
	 * @return
	 */
	static String decodeNom() {
		int i = getObj();
		if (i > 0) {
			String param = new String(obj, objPnt, i);
			objPnt += i;
			return param;
		} else
			return "";
	}


	/**
	 * édition décodage ou documentation
	 * @param sdec
	 */
	static void print(String sdec) {
		print(sdec, sdec);
	}

	/**
	 * édition décodage ou documentation
	 * @param sdec
	 * @param sdoc
	 */
	static void print(String sdec, String sdoc) {
		if (doc && sdoc != null)
			System.out.print(sdoc);
		else
			System.out.print(sdec);
	}

	/**
	 * édition décodage ou documentation
	 * @param sdec
	 */
	static void println(String sdec) {
		println(sdec, sdec);
	}

	/**
	 * édition décodage ou documentation
	 * @param sdec
	 * @param sdoc
	 */
	static void println(String sdec, String sdoc) {
		if (doc && sdoc != null)
			System.out.println(sdoc);
		else
			System.out.println(sdec);
	}

	/**
	 * retour décodage ou documentation
	 * @param sdec
	 * @return
	 */
	static String returnString(String sdec) {
		return returnString(sdec, sdec);
	}

	/**
	 * retour décodage ou documentation
	 * @param sdec
	 * @param sdoc
	 * @return
	 */
	static String returnString(String sdec, String sdoc) {
		if (doc && sdoc != null)
			return sdoc;
		else
			return sdec;
	}

}
