package bridgeBid;

import java.io.*;

/**
 * Interpr�teur du langage des ench�re de bridge
 */

public class Interpreter {
	
	// code binaire du langage d'ench�re

	private int sizeBuffer;
	private int size;
	private byte[] obj;
	private int objPnt;

	// Donne

	private EvalJeu jeu;
	private DonneBid donne;

	// ench�res

	private int iEnch; 						// indice ench�re
	private boolean sequPrecOK; 			// s�quence d'ench�re pr�c�dente accept�e
	private int sequPrecInd; 				// indice s�quence pr�c�dente
	private int valSeq;						// valeur s�quence d'ench�re facultative
	private int valSeqSave;					// valeur s�quence d'ench�re facultative pr�c�dente

	private static int nbMaxSeqNom = 100; 	// nombre max de s�quences nomm�es
	private int nbSeqNom; 					// nombre de s�quences nomm�es
	private String[] nomSequence; 			// nom des s�quences nomm�es
	private int[] adrSequence; 				// adresse des s�quences nomm�es
	private boolean bSeqNom; 				// ex�cution d'une s�quence nomm�e

	private String comment; 				// commentaire sur la s�quence en cours

	// variables

	private Variable var;

	// debug

	private boolean debug = true;
	private boolean debugNO = true;
	private String sDebug = "";

	// reconstitution du jeu du partenaire

	private EvalJeuPartenaire jt;

	// phase de l'interpr�tation
	// 1=s�quence, 2=condition, 3=annonce

	private int phase = 0;

	/**
	 * Initialisation de l'interpr�teur
	 * @param binFile : param�trage des ench�res
	 * @param pdebug : debug
	 */
	public Interpreter(String binFile, boolean pdebug) {
		InputStream in;
		sizeBuffer = 65000;
		size = 0;
		obj = new byte[sizeBuffer];
		debug = pdebug;
		// chargement du syst�me
		try {
			in = new FileInputStream(binFile);
			size = in.read(obj);
			in.close();
		} catch (IOException ioe) {
			System.out.println("Erreur fichier encheres: " + ioe);
		}
		nomSequence = new String[nbMaxSeqNom];
		adrSequence = new int[nbMaxSeqNom];
		nbSeqNom = 0;
	}

	/**
	 * �valuation de l'ench�re suivante
	 * @param pdonne : donne
	 * @return ench�re
	 */
	public String getEnchere(DonneBid pdonne) {
		int i, cd, ad, ads;
		donne = pdonne;
		jeu = donne.getJeu();
		if ( debug )  jeu.print();
		sequPrecOK = false;
		var = new Variable(donne);
		String enchere = "-";
		bSeqNom = false;
		comment = "";
		valSeq = 0;

		debugPrint("Recherche ench�re");

		objPnt = 0;
		while (objPnt < size) {
			cd = getObj();
			if (cd == 1) {
				// test du syst�me d'ench�re
				String param = decodeNom();
				ad = getAdr();
				debugPrint("System " + param);
				if (!donne.testSystem(param))
					objPnt = ad;
			} else if (cd == 2) {
				// test de la s�quence d'ench�re
				debug("Ench�res:");
				phase = 1;
				// sauvegarde des valeurs pour *
				var.saveVar();
				valSeqSave = valSeq;
				var.init();
				valSeq = 0;
				ad = getAdr();
				ads = getAdr();
				if (!SequEnchere()) {
					noDebug();
					var.norestVar();
					objPnt = ad;
				} else {
					// s�quence d'ench�re OK
					debugOK();
					phase = 2;
					i = getObj();
					while (i == 3) {
						// test des conditions
						var.saveVar();
						debug("Jeu:");
						ad = getAdr();
						ads = getAdr();
						if (!CodeCond(ads)) {
							debugNO();
							var.restVar();
							objPnt = ad;
						} else {
							// conditions OK, ench�re
							phase = 3;
							i = getObj();
							if (i == 107) {
								// appel d'une s�quence nomm�e							
								String nomSeq = decodeNom();
								// couleur d'atout
								jt = new EvalJeuPartenaire();
								jt.atout = getPatternCouleur().charAt(0);
								debug(" " + nomSeq);
								debugOK();
								bSeqNom = true;
								CodeParam(getAdr());
								if (debug) jt.print();
								objPnt = getAdSeqNom(nomSeq);
								valSeq = 0;
								var.init();
								var.setVar(5, jt.atout);
							} else {
								enchere = putEnchere(i);
								String alerteEnchere = decodeNom();
								String commentEnchere = decodeNom();
								debugOK();
								debugPrint("----------------> Ench�re faite: "
										+ enchere
										+ "[" + alerteEnchere + "]"
										+ ",(" + commentEnchere + ")");
								return enchere;
							}
						}
						i = getObj();
					}
				}
			} else if (cd == 4)	{
				// commentaires
				comment = decodeNom();
			} else if (cd == 5) {
				// alertes
				cd = getObj();
				while (cd == 5) {
					String nom = decodeNom();
					String descr = decodeNom();
					cd = getObj();
					while (cd == 6) {
						descr = decodeNom();
						cd = getObj();
					}
				}
			} else if (cd != 0) {
				debugPrint("Erreur dans fichier ench�re code: " + cd);
				break;
			}
		}
		return enchere;
	}

	/**
	 * faire une ench�re
	 * @param i : instruction
	 * @return ench�re
	 */
	private String putEnchere(int i) {
		if (i == 105)
			return ("-");
		else if (i == 100)
			return ("X");
		else if (i == 101)
			return ("XX");
		else if (i == 120)
			return (Annonce(0, getPatternCouleur()));
		else if (i == 121)
			return (Annonce(1, getPatternCouleur()));
		else if (i == 122)
			return (Annonce(2, getPatternCouleur()));
		else if (i > 110 && i <= 117) {
			// v�rification que l'ench�re est possible
			char couleur = getPatternCouleur().charAt(0);
			int hauteur = i - 110;
			int hmin = donne.lastHauteurAnnonce();
			char lastCoul = donne.lastCouleurAnnonce();
			if (lastCoul == couleur || couleur == 'T' || (couleur == 'K' && lastCoul != 'T')
					|| (couleur == 'C' && lastCoul == 'P'))
				hmin++;
			if (hauteur >= hmin)
				return (String.valueOf(hauteur) + couleur);
			else {
				debugPrint("ench�re insuffisante: " + String.valueOf(hauteur) + couleur);
				return ("-");
			}
		}
		return "erreur:" + i;
	}

	/**
	 * Teste de la s�quence d'ench�res
	 * @return true ou false
	 */
	private boolean SequEnchere() {
		int k;
		iEnch = 0;
		int i = getObj();
		if (i == 203) {
			// d�finition d'une s�quence nomm�e
			String nomSeq = decodeNom();
			insertSeqNom(nomSeq, objPnt + 1);
			return false;
		}
		if (iEnch > donne.getnbEnch())
			return false;
		if (i == 202) {
			// idem s�quence pr�c�dente (*)
			debug(" *");
			if (sequPrecOK) {
				iEnch = sequPrecInd;
				//  restauration des variables
				var.restVar();
				//  restauration valeur s�quence facultative pr�c�dente
				valSeq = valSeqSave;
				i = getObj();
			} else
				return false;
		} else {
			sequPrecOK = false;
			if (i == 102) {
				debug(" 0 � 3 passe");
				for (k = 0; k < 3 && iEnch < donne.getnbEnch() && donne.getEnchere(iEnch).equals("-"); k++, iEnch++)
					;
				i = getObj();
			} else if (i == 103) {
				debug(" 0 ou 1 passe");
				if (donne.getEnchere(iEnch).equals("-"))
					iEnch++;
				i = getObj();
			} else if (i == 104) {
				debug(" 2 ou 3 passe");
				if (iEnch < donne.getnbEnch() && donne.getEnchere(iEnch).equals("-")
						&& donne.getEnchere(iEnch + 1).equals("-"))
					iEnch += 2;
				else
					return false;
				if (iEnch <= donne.getnbEnch() && donne.getEnchere(iEnch).equals("-"))
					iEnch++;
				i = getObj();

			} else if (i == 106) {			
				debug(" ??");  // n'importe quelle s�quence
				i = getObj();
				return true;
			}
		}
		return SuiteEnchere(i);
	}

	/**
	 * contr�le de la suite de la s�quence d'ench�re 
	 * @param i	: instruction
	 * @return true ou false
	 */
	private boolean SuiteEnchere(int i) {
		boolean bSeqAlt = false; // s�quence alternative /
		boolean bSeqOK = false;  // r�ussite s�quence facultative ou alternative
		int ad = 0;				 // adresse / ou ) suivant
		while ( i > 0 )	  {
			if (i == 200)  {
				// s�quence facultative ou alternative entre ()		
				debug("(");
				bSeqAlt = false;
				bSeqOK = false;
				ad = getAdr();
				i = getObj();
				if (PatternEnchere(i, donne.getEnchere(iEnch))) {
					// si on trouve le d�but de la s�quence,
					// le reste doit �tre conforme
					iEnch++;
					i = getObj();
					if (SuiteEnchere(i))
						bSeqOK = true;
				}
				objPnt = ad;
			} else if (i == 204)  {
				// s�quence alternative	
				debug("/");
				bSeqAlt = true;
				ad = getAdr();
				if (!bSeqOK) {
					i = getObj();
					if (PatternEnchere(i, donne.getEnchere(iEnch))) { 
						// si on trouve le d�but de la s�quence,
						// le reste doit �tre conforme
						iEnch++;
						i = getObj();
						if (SuiteEnchere(i))
							bSeqOK = true;
					}
				}
				objPnt = ad;
			} else if (i == 201)  {
				// fin s�quence facultative			
				valSeq = getObj(); // valeur s�quence
				debug(")" + valSeq);
				ad = getObj(); // consomme 0 final
				return bSeqOK;
			} else {
				if (iEnch >= donne.getnbEnch())  {
					return false;
				}
				if (i == 108) {   // n'importe quelle ench�re			
					debug(" ?");
					iEnch++;
				} else if (!PatternEnchere(i, donne.getEnchere(iEnch++)))  {
					return false;
				}
			}
			i = getObj();
		}

		if (iEnch == donne.getnbEnch())  {
			if ( bSeqAlt && !bSeqOK )
				return false;	// la s�quence alternative est obligatoire
			return true;
		} else if (iEnch < donne.getnbEnch()) {
			if (!sequPrecOK) {
				sequPrecOK = true;
				sequPrecInd = iEnch;
			}
			return false;
		}
		return false;
	}

	/**
	 * test du pattern d'ench�res
	 * @param i : instruction
	 * @param Ench : ench�re faite � comparer
	 * @return true ou false
	 */
	private boolean PatternEnchere(int i, String Ench) {
		if (i == 105) {
			debug(" -");
			if (!Ench.equals("-"))
				return false;
		} else if (i == 100) {
			debug(" X");
			if (!Ench.equals("X"))
				return false;
		} else if (Ench.length() != 2)
			return false;
		else if (i == 101) {
			debug(" XX");
			if (!Ench.equals("XX"))
				return false;
		} else if (i == 120) {
			var.saveVar();
			if (!Ench.equals(Annonce(0, setPatternCouleur(Ench.charAt(1))))) {
				var.restVar();
				return false;
			} else
				var.norestVar();
		} else if (i == 121) {
			var.saveVar();
			if (!Ench.equals(Annonce(1, setPatternCouleur(Ench.charAt(1))))) {
				var.restVar();
				return false;
			} else
				var.norestVar();
		} else if (i == 122) {
			var.saveVar();
			if (!Ench.equals(Annonce(2, setPatternCouleur(Ench.charAt(1))))) {
				var.restVar();
				return false;
			} else
				var.norestVar();
		} else if (i == 123) // n+
		{
			if (Ench.length() < 2)
				return false;
			int hEnch = Character.getNumericValue(Ench.charAt(0));
			int h = getObj();
			if (hEnch < h)
				return false;
			var.saveVar();
			if (!Ench.substring(1).equals(String.valueOf(setPatternCouleur(Ench.charAt(1))))) {
				var.restVar();
				return false;
			} else
				var.norestVar();
		} else if (i > 110 && i <= 117) // 1 � 7
		{
			var.saveVar();
			String enchere = String.valueOf(i - 110) + setPatternCouleur(Ench.charAt(1));
			debug(" " + enchere);
			if (!Ench.equals(enchere)) {
				var.restVar();
				return false;
			} else
				var.norestVar();
		} else
			return false;
		return true;
	}

	/**
	 * couleur du pattern
	 * positionne les variables
	 * @param couleur
	 * @return couleur
	 */
	private char setPatternCouleur(char couleur) {
		int i = getObj();
		if (i == 151)
			return ('T');
		else if (i == 152)
			return ('K');
		else if (i == 153)
			return ('C');
		else if (i == 154)
			return ('P');
		else if (i == 155)
			return ('S');
		else if (i >= 156 && i <= 164) {
			if (var.setVar(i - 155, couleur)) {
				String s = var.getVar(i - 155);
				if (s.length() != 1)
					erreur("erreur couleur dans setPatternCouleur=" + s);
				return s.charAt(0);
			} else
				return ' ';
		} else if (i == 165) // ?
			return couleur;
		else
			return ' ';
	}

	/**
	 * Ench�re
	 * @param saut
	 * @param couleur
	 * @return ench�re
	 */
	private String Annonce(int saut, String couleur) {
		if (couleur.length() != 1)
			erreur("Annonce impossible, couleur=" + couleur);
		return Annonce(saut, couleur.charAt(0));
	}

	/**
	 * Ench�re
	 * @param saut
	 * @param couleur
	 * @return ench�re
	 */
	private String Annonce(int saut, char couleur) {
		int h;
		char lastCoul;
		if (phase == 1) {
			h = donne.lastHauteurAnnonce(iEnch);
			lastCoul = donne.lastCouleurAnnonce(iEnch);
		} else {
			h = donne.lastHauteurAnnonce();
			lastCoul = donne.lastCouleurAnnonce();
		}
		if (lastCoul == couleur || couleur == 'T' || (couleur == 'K' && lastCoul != 'T')
				|| (couleur == 'C' && lastCoul == 'P'))
			h++;

		debug(" " + String.valueOf(h + saut) + couleur);
		return String.valueOf(h + saut) + couleur;
	}

	/**
	 * couleur
	 * @return couleur
	 */
	private String getPatternCouleur() {
		int i = getObj();
		return getPatternCouleur(i);
	}

	/**
	 * couleur
	 * @param i : instruction
	 * @return couleur
	 */
	private String getPatternCouleur(int i) {
		String s = "";
		if (i == 151)
			s = "T";
		else if (i == 152)
			s = "K";
		else if (i == 153)
			s = "C";
		else if (i == 154)
			s = "P";
		else if (i == 155)
			s = "S";
		else if (i >= 156 && i <= 164) {
			int noVar = i - 155;
			debug("var" + noVar + ":");
			s = var.getVar(noVar);
		}
		debug(s);
		return s;
	}

	/**
	 * codification des conditions et ench�re
	 * @param size
	 * @return true ou false
	 */
	private boolean CodeCond(int size) {
		while (objPnt < size) {
			int i = getObj();
			if (i == 0)
				return true;
			if (TestCond(i) == false)
				return false;
		}
		return false;
	}

	/**
	 * test des conditions
	 * @param i : instruction
	 * @return true ou false
	 */
	private boolean TestCond(int i) {
		String D = jeu.distribution;
		String v = donne.getVulnerabilite();
		String tt = donne.getTypeTournoi();
		
		// distribution

		if (i == 10) {
			debug(" D");
			int j, nbCartes, s;
			for (j = 0; j < 4; j++) {
				nbCartes = Character.digit(D.charAt(j), 10);
				i = getObj();
				debug(Integer.toString(i));
				s = PlusMoins();
				if (s == 0 && nbCartes != i || s == 1 && nbCartes < i || s == -1 && nbCartes >= i) {
					return false;
				}
			}
			return true;
		} else if (i == 21) // pl
		{
			debug(" pl");
			if (jeu.typeDistrib("pl"))
				return true;
			else
				return false;
		} else if (i == 22) // reg
		{
			debug(" reg");
			if (jeu.typeDistrib("reg"))
				return true;
			else
				return false;
		} else if (i == 23) // sreg
		{
			debug(" sreg");
			if (jeu.typeDistrib("sreg"))
				return true;
			else
				return false;
		} else if (i == 24) // sa
		{
			debug(" sa");
			if (jeu.typeDistrib("reg") || jeu.typeDistrib("sreg"))
				return true;
			else
				return false;
		} else if (i == 25) // tri
		{
			debug(" tri");
			if (jeu.typeDistrib("tri"))
				return true;
			else
				return false;
		} else if (i == 26) // uni
		{
			debug(" uni");
			if (jeu.typeDistrib("uni"))
				return true;
			else
				return false;
		} else if (i == 27) // bic
		{
			debug(" bic");
			if (jeu.typeDistrib("bic"))
				return true;
			else
				return false;
		} else if (i == 11) // s
		{
			debug(" s");
			var.saveVar();
			String s = getPatternCouleur();
			for (int j = 0; j < s.length(); j++) {
				if (jeu.nbCartes(s.charAt(j)) < 2) {
					if (s.length() > 1)
						var.setVar(s.charAt(j));
					var.norestVar();
					return true;
				}
			}
			var.restVar();
			return false;
		} else if (i == 12) // ctr
		{
			debug(" ctr");
			var.saveVar();
			String s = getPatternCouleur();
			for (int j = 0; j < s.length(); j++) {
				if (jeu.Controles(s.charAt(j)) >= 2) {
					if (s.length() > 1)
						var.setVar(s.charAt(j));
					var.norestVar();
					return true;
				}
			}
			var.restVar();
			return false;
		} else if (i == 13) // h
		{
			debug(" h");
			var.saveVar();
			String s = getPatternCouleur();
			String cc = codeCarte();
			for (int j = 0; j < s.length(); j++) {
				if (jeu.Cartes(s.charAt(j), cc)) {
					if (s.length() > 1)
						var.setVar(s.charAt(j));
					var.norestVar();
					return true;
				}
			}
			var.restVar();
			return false;
		} else if (i == 15) // h
		{
			i = getObj();
			debug(" " + i + "h");
			var.saveVar();
			String s = getPatternCouleur();
			for (int j = 0; j < s.length(); j++) {
				if (jeu.nbHonneurs(s.charAt(j)) >= i) {
					if (s.length() > 1)
						var.setVar(s.charAt(j));
					var.norestVar();
					return true;
				}
			}
			var.restVar();
			return false;
		} else if (i == 16) // gh
		{
			i = getObj();
			debug(" " + i + "gh");
			var.saveVar();
			String s = getPatternCouleur();
			for (int j = 0; j < s.length(); j++) {
				if (jeu.nbGrosHonneurs(s.charAt(j)) >= i) {
					if (s.length() > 1)
						var.setVar(s.charAt(j));
					var.norestVar();
					return true;
				}
			}
			var.restVar();
			return false;
		} else if (i == 18) // A
		{
			i = getObj();
			debug(" " + i + "A");
			int pm = PlusMoins();
			int nbc = jeu.nbCartesType('A');
			if (pm > 0 && nbc >= i || pm == 0 && nbc == i || pm < 0 && nbc < i)
				return true;
			else
				return false;
		} else if (i == 19) // R
		{
			i = getObj();
			debug(" " + i + "R");
			int pm = PlusMoins();
			int nbc = jeu.nbCartesType('R');
			if (pm > 0 && nbc >= i || pm == 0 && nbc == i || pm < 0 && nbc < i)
				return true;
			else
				return false;
		} else if (i == 20) // cl
		{
			i = getObj();
			debug(" " + i + "cl");
			String s = getPatternCouleur();
			if (s.length() != 1) // variables ind�termin�es interdites
				return false;
			int pm = PlusMoins();
			int nbc = jeu.nbCartesType('A');
			if (jeu.Cartes(s.charAt(0), "R"))
				nbc++;
			if (pm > 0 && nbc >= i || pm == 0 && nbc == i || pm < 0 && nbc < i)
				return true;
			else
				return false;
		} else if (i == 17) // tsa
		{
			i = getObj();
			debug(" " + i + "tsa");
			var.saveVar();
			String s = getPatternCouleur();
			int pm = PlusMoins();
			for (int j = 0; j < s.length(); j++) {
				if (pm >= 0 && jeu.ControlesSA(s.charAt(j)) >= i || pm < 0 && jeu.ControlesSA(s.charAt(j)) < i) {
					if (s.length() > 1)
						var.setVar(s.charAt(j));
					var.norestVar();
					return true;
				}
			}
			var.restVar();
			return false;
		} else if (i >= 151 && i <= 167) // nb cartes par couleur
		{
			var.saveVar();
			debug(" nb cartes:");
			String s = getPatternCouleur(i);
			i = getObj();
			debug("=" + i);
			int pm = PlusMoins();
			for (int j = 0; j < s.length(); j++) {
				int imin = i;
				int imax = i;
				if (bSeqNom) {
					imin -= jt.nbCartesMin[EvalJeu.noCouleur(s.charAt(j))];
					imax -= jt.nbCartesMax[EvalJeu.noCouleur(s.charAt(j))];
					debug("nb cartes=" + imin + "-" + imax);
				}
				if (pm > 0 && jeu.nbCartes(s.charAt(j)) >= imin || pm == 0 && jeu.nbCartes(s.charAt(j)) == imin
						|| pm < 0 && jeu.nbCartes(s.charAt(j)) < imax) {
					if (s.length() > 1) {
						debug("(" + s.charAt(j) + ")");
						var.setVar(s.charAt(j));
					}
					var.norestVar();
					return true;
				}
			}
			var.restVar();
			return false;
		}

		// valeur

		else if (i >= 51 && i <= 61) {
			int min = 0;
			int max = 99;
			int valJeu = 0;
			int valJeuMax = 0;
			String s = "";
			if (i == 51) {
				debug(" PH");
				valJeu = valJeuMax = jeu.PH;
				if (bSeqNom) {
					valJeu += jt.PH;
					valJeuMax += jt.PHM;
				}
			} else if (i == 52) {
				debug(" PHL");
				valJeu = valJeuMax = jeu.PHL;
				if (bSeqNom) {
					valJeu += jt.PHL;
					valJeuMax += jt.PHLM;
				}
			} else if (i == 53) {
				debug(" PDH");
				valJeu = valJeuMax = jeu.PDH;
				if (bSeqNom) {
					valJeu += jt.PDH;
					valJeuMax += jt.PDHM;
				}
			} else if (i == 54) {
				debug(" PJ");
				valJeu = valJeuMax = jeu.PJ;
				if (bSeqNom) {
					valJeu += jt.PDH;
					valJeuMax += jt.PDHM;
				}
			} else if (i == 55) {
				debug(" LJ");
				valJeu = valJeuMax = jeu.LJ;
				if (bSeqNom) {
					valJeu += jt.LJ;
					valJeuMax += jt.LJM;
				}
			} else if (i == 56) {
				debug(" pe");
				valJeu = jeu.P;
			} else if (i == 60) {
				debug(" H");
				var.saveVar();
				s = getPatternCouleur();
			} else if (i == 61) {
				debug(" S");
				var.saveVar();
				s = getPatternCouleur();
			}

			min = getObj();
			max = getObj();
			if ( donne.getTypeJoueur() == 3 )  {
				min -= valSeq;
				max -= valSeq;
			}
			debug(min + "-" + max);

			if (i == 60) // H
			{
				for (int j = 0; j < s.length(); j++) {
					if (jeu.Honneur(s.charAt(j)) >= min && jeu.Honneur(s.charAt(j)) <= max) {
						if (s.length() > 1)
							var.setVar(s.charAt(j));
						var.norestVar();
						return true;
					}
				}
				var.restVar();
				return false;
			} else if (i == 61) // S
			{
				for (int j = 0; j < s.length(); j++) {
					if (jeu.Soutien(s.charAt(j)) >= min && jeu.Soutien(s.charAt(j)) <= max && (!bSeqNom
							|| jeu.Soutien(s.charAt(j)) + jt.PDH >= min && jeu.Soutien(s.charAt(j)) + jt.PDHM <= max)) {
						if (s.length() > 1)
							var.setVar(s.charAt(j));
						var.norestVar();
						return true;
					}
				}
				var.restVar();
				return false;
			} else if (valJeu >= min && valJeuMax <= max )
				return true;
			return false;
		}

		// Condition

		else if (i == 70) {
			var.saveVar();
			debug(" condition: ");
			String s1 = getPatternCouleur(getObj());
			int op = getObj();
			String ops = "";
			if (op == 170)
				ops = "=";
			else if (op == 171)
				ops = ">";
			else if (op == 172)
				ops = ">=";
			else if (op == 173)
				ops = "<";
			else if (op == 174)
				ops = "<";
			else if (op == 175)
				ops = "<>";
			String s2 = getPatternCouleur(getObj());
			debug(" condition: " + s1 + ops + s2);

			if (op == 170 && s1.equals(s2) || op == 171 && s1.compareTo(s2) > 0 || op == 172 && s1.compareTo(s2) >= 0
					|| op == 173 && s1.compareTo(s2) < 0 || op == 174 && s1.compareTo(s2) <= 0
					|| op == 175 && s1.compareTo(s2) != 0) {
				var.norestVar();
				return true;
			} else {
				var.restVar();
				return false;
			}
		}

		// vuln�rabilit�

		else if (i == 81) // V
		{
			debug(" V");
			if (v.charAt(0) == 'V')
				return true;
			else
				return false;
		} else if (i == 82) // N
		{
			debug(" N");
			if (v.charAt(0) == 'N')
				return true;
			else
				return false;
		} else if (i == 83) // NV
		{
			debug(" NV");
			if (v.equals("NV"))
				return true;
			else
				return false;
		} else if (i == 84) // VN
		{
			debug(" VN");
			if (v.equals("VN"))
				return true;
			else
				return false;
		} else if (i == 85) // MV
		{
			debug(" MV");
			if (v.equals("VV") || v.equals("NN"))
				return true;
			else
				return false;
		} else if (i == 86) // VV
		{
			debug(" VV");
			if (v.equals("VV"))
				return true;
			else
				return false;
		} else if (i == 87) // NN
		{
			debug(" NN");
			if (v.equals("NN"))
				return true;
			else
				return false;
		}
		// type de tournoi

		else if (i == 88) // TPP
		{
			debug(" TPP");
			if (tt.equals("TPP"))
				return true;
			else
				return false;
		} else if (i == 89) // IMP
		{
			debug(" IMP");
			if (tt.equals("IMP"))
				return true;
			else
				return false;
		}
		return false;
	}

	/**
	 * param�tre d'une s�quence nomm�e
	 * @param size
	 */
	private void CodeParam(int size) {
		debug("Param�tres: ");
		while (objPnt < size) {
			int i = getObj();
			if (i >= 51 && i <= 61) // valeur
			{
				int min = getObj();
				int max = getObj();
				// sur�valuation du jeu de l'ouvreur en fonction de la s�quence
				if ( donne.getTypeJoueur() == 3 )  {
					min += valSeq;
					max += valSeq;
				}
				debug(min + "-" + max);
				if (i == 51) {
					debug(" PH");
					jt.PH = min;
					jt.PHM = max;
					jt.PHL = min;
					jt.PHLM = max;
					jt.PDH = min;
					jt.PDHM = max;
				} else if (i == 52) {
					debug(" PHL");
					jt.PHL = min;
					jt.PHLM = max;
					jt.PDH = min;
					jt.PDHM = max;
				} else if (i == 53) {
					debug(" PDH");
					jt.PDH = min;
					jt.PDHM = max;
				} else if (i == 55) {
					debug(" LJ");
					jt.LJ = min;
					jt.LJM = max;
				}
			}
		}
		debug("\n");
	}

	/**
	 * carte
	 * @return hauteur carte
	 */
	private String codeCarte() {
		int i = getObj();
		String s = "";
		while (i > 0) {
			if (i == 1)
				s += 'A';
			else if (i == 2)
				s += 'R';
			else if (i == 3)
				s += 'D';
			else if (i == 4)
				s += 'V';
			else if (i == 5)
				s += 'X';
			i = getObj();
		}
		debug(s);
		return s;
	}

	/**
	 * + -
	 * @return
	 */
	private int PlusMoins() {
		int i = getObj();
		if (i == 251) // +
		{
			debug("+");
			return 1;
		} else if (i == 252) // -
		{
			debug("-");
			return -1;
		}
		return 0;
	}

	/**
	 * lecture d'un byte
	 * @return instruction
	 */
	private int getObj() {
		Byte cd = new Byte(obj[objPnt++]);
		int i = cd.intValue();
		if (i < 0)
			i = i + 256;
		return i;
	}

	/**
	 * lecture d'une adresse de fin
	 * @return adresse
	 */
	private int getAdr() {
		Byte cd = new Byte(obj[objPnt++]);
		int i = cd.intValue();
		if (i < 0)
			i = i + 256;
		cd = new Byte(obj[objPnt++]);
		int j = cd.intValue();
		if (j < 0)
			j = j + 256;
		i = i * 256 + j;
		return i;
	}

	/**
	 * d�codage d'un nom
	 * @return nom
	 */
	private String decodeNom() {
		int i = getObj();
		String param = new String(obj, objPnt, i);
		objPnt += i;
		return param;
	}

	/**
	 * m�morisation d'un nom de s�quence et de son adresse
	 * @param nom
	 * @param adr
	 */
	private void insertSeqNom(String nom, int adr) {
		for (int i = 0; i < nbSeqNom; i++) {
			if (nomSequence[i].equals(nom))
				return;
		}
		nomSequence[nbSeqNom] = nom;
		adrSequence[nbSeqNom] = adr;
		nbSeqNom++;
	}

	/**
	 * recherche de l'adresse d'une s�quence
	 * @param nom
	 * @return adresse
	 */
	private int getAdSeqNom(String nom) {
		for (int i = 0; i < nbSeqNom; i++) {
			if (nomSequence[i].equals(nom))
				return adrSequence[i];
		}
		return -1;
	}

	/**
	 * erreur
	 * @param cause
	 */
	private void erreur(String cause) {
		System.out.println(" *** ");
		System.out.println(cause);
		System.exit(1);
	}

	/**
	 * stocke texte debug
	 * @param s
	 */
	private void debug(String s) {
		if (debug)
			sDebug += s;
	}

	/**
	 * print texte debug si condition vraie
	 */
	private void debugOK() {
		if (debug) {
			System.out.println(sDebug + " ***OK*** ");
		}
		sDebug = "";
	}

	/**
	 * print texte debug si condition fausse
	 */
	private void debugNO() {
		if (debugNO) {
			System.out.println(sDebug + " ***NO*** ");
		}
		sDebug = "";
	}

	/**
	 * efface texte debug
	 */
	private void noDebug() {
		sDebug = "";
	}

	/**
	 * print debug direct
	 * @param s
	 */
	private void debugPrint(String s) {
		if (debug) {
			System.out.println(s);
		}
	}

}
