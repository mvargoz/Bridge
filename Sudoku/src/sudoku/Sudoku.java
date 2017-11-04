package sudoku;

import java.util.*;

/*
 *			Sudoku 
 */

public class Sudoku
{
	// grille du jeu
	// la valeur 0 indique que la case est encore vide
	
	public int[][] grille = new int[9][9];
	
	// cases donn�es au d�but
	
	public boolean[][] grilleInit = new boolean[9][9];
	
	// possibilit�s : case libre et chiffres non utilis�s dans ligne, colonne ou bloc
	
	public boolean[][][] valeursPossibles = new boolean[9][9][9];
	
	// contr�le de la r�partition des chiffres � la cr�ation
	
	private int[] repartLig = new int[9];
	private int[] repartCol = new int[9];
	private int[] repartBloc = new int[9];
	private int ligCr = 0;
	private int colCr = 0;
	private int valCr = 0;
	
	// �tat des hypoth�ses

	private int nbHypo = 0;		// nombre d'hypoth�ses formul�es
	private int ligHypoC = 0;
	private int colHypoC = 0;	
	private int[] ligHypo = new int[81];
	private int[] colHypo = new int[81];
	private String[] valHypo = new String[81];
	private int maxPossible = 2;
	
	// sauvegardes du contexte
	
	private Stack savCtx = new Stack(); 
	
	// contr�le d'ex�cution
	
	public boolean debug = true;
	public String mess = "go!";

	// constructeur
	
	public Sudoku()
	{
		init();
	}

	// initialisation de la grille

	public void init()
	{
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				grille[i][j] = 0;
				grilleInit[i][j] = false;				
				for (int k=0; k<9; k++)
				{
					valeursPossibles[i][j][k] = true;
				}
			}
		}			
		savCtx.clear();
		nbHypo = 0;
	}
	
	// chargement de la grille � partir d'une grille donn�e
	
	public void charge(int[][] grille)
	{
		init();
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				int x = grille[i][j];
				if ( x > 0 )
					putData(i+1, j+1, x, true);				
			}
		}
		nbHypo = 0;
	}

	// sauvegarde de l'�tat courant
	
	public void save()
	{
		int[][] savGrille = new int[9][9];
		boolean[][][] savValeursPossibles = new boolean[9][9][9];
		boolean[][] savGrilleInit = new boolean[9][9];
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				savGrille[i][j] = grille[i][j];
				savGrilleInit[i][j] = grilleInit[i][j];
				for (int k=0; k<9; k++)
				{
					savValeursPossibles[i][j][k] = valeursPossibles[i][j][k];
				}
			}
		}
		savCtx.push(savValeursPossibles);
		savCtx.push(savGrille);
		savCtx.push(savGrilleInit);
	}

	// restauration de l'�tat sauvegard�
	
	public void restore()
	{
		boolean[][] savGrilleInit = (boolean[][]) savCtx.pop();
		int[][] savGrille = (int[][]) savCtx.pop();
		boolean[][][] savValeursPossibles = (boolean[][][]) savCtx.pop();
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				grille[i][j] = savGrille[i][j];
				grilleInit[i][j] = savGrilleInit[i][j];
				for (int k=0; k<9; k++)
				{
					valeursPossibles[i][j][k] = savValeursPossibles[i][j][k];
				}
			}
		}					
	}
	
	// placement d'un chiffre dans la grille avec contr�le de possibilit�
	
	public boolean putData(int lig, int col, int val, boolean init)
	{
			//	contr�le des valeurs : ligne et colonne 1-9, valeur 0-9
			//  si la valeur est diff�rente de 0, la case doit �tre vide et l'affectation possible
		if ( lig < 1 || lig > 9 || col < 1 || col > 9 || val < 0 || val > 9
			 || val != 0 && ( grille[lig-1][col-1] != 0 || valeursPossibles[lig-1][col-1][val-1] == false ))
		{
			System.out.println("Erreur affectation case " + Integer.toString(lig) + ","
						+ Integer.toString(col) + " avec valeur : "	+ Integer.toString(val));
			return false;
		}
		grille[lig-1][col-1] = val;
		grilleInit[lig-1][col-1] = false;
		if ( val > 0 ) grilleInit[lig-1][col-1] = init;
		initValeursPossibles();
		return true;		
	}
	
	// remplissage des valeurs possibles
	
	private void initValeursPossibles()
	{
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				for (int k=0; k<9; k++)
				{
					valeursPossibles[i][j][k] = true;
				}
			}
		}
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				if ( grille[i][j] != 0 )
				{
					int v = grille[i][j];
					for (int k=0; k<9; k++)
					{
						// toutes valeurs interdites sur cette case
						valeursPossibles[i][j][k] = false;
						// valeur interdite sur la m�me ligne
						valeursPossibles[i][k][v-1] = false;
						// valeur interdite sur la m�me colonne
						valeursPossibles[k][j][v-1] = false;
					}
					// coordonn�es du bloc courant
					int ibloc = (i/3)*3;
					int jbloc = (j/3)*3;
					for (int bi=ibloc; bi<ibloc+3; bi++)
					{
						for (int bj=jbloc; bj<jbloc+3; bj++)
						{
							// valeur interdite dans le m�me bloc			
							valeursPossibles[bi][bj][v-1] = false;
						}
					}		

				}
			}
		}		
	}

	/*
	 *			Cr�ation d'une grille
	 * 			niveau ?
	 */
	
	public int create(int niveau)
	{
		init();
		
			// placer les 9 premiers chiffres de fa�on al�atoire r�partis r�guli�rement
		initRepart();
		int n = 0;
		while ( n < 9 )
		{
			putCaseRepart(1);
			n++;
			putData(ligCr+1, colCr+1, n, true);
			if ( debug )
			{
				System.out.println("Cr�ation case " + Integer.toString(n) + " : "
						+ Integer.toString(ligCr+1) + ","
						+ Integer.toString(colCr+1) + "=" + Integer.toString(n) );
			}
		}
			// placer les autres de fa�on al�atoire
		while ( n < 18 )
		{
			putCase();
			putData(ligCr+1, colCr+1, valCr+1, true);
			save();
			n++;
			if ( debug )
			{
				System.out.println("Cr�ation case " + Integer.toString(n) + " : "
						+ Integer.toString(ligCr+1) + ","
						+ Integer.toString(colCr+1) + "=" + Integer.toString(valCr+1) );
			}
		}
			// contr�le solution possible
		while ( true )
		{
			save();
			int r = solveAll(false);
			if ( r == 0 )
			{		// le sudoku a une solution unique
				restore();		// enlever la solution trouv�e
				return n;					
			}
			else if ( r == -1 )
			{		// non soluble, essayer une autre case
				restore();		// enlever la solution partielle trouv�e
				restore();		// retour en arri�re
				if ( !putCase() ) return -1;
				putData(ligCr+1, colCr+1, valCr+1, true);
				if ( debug )
				{
					System.out.println("blocage: Cr�ation case " + Integer.toString(n) + " : "
							+ Integer.toString(ligCr+1) + ","
							+ Integer.toString(colCr+1) + "=" + Integer.toString(valCr+1) );
				}
			}
			else if ( r == -2 )
			{		// OK mais plusieurs solutions, continuer
					// nouvelle case en tenant compte de ce qui a d�j� �t� d�couvert
				if ( !putCase() ) return -1;
				restore();		// enlever la solution partielle trouv�e
				putData(ligCr+1, colCr+1, valCr+1, true);
				save();
				n++;
				if ( debug )
				{
					System.out.println("Cr�ation case " + Integer.toString(n) + " : "
							+ Integer.toString(ligCr+1) + ","
							+ Integer.toString(colCr+1) + "=" + Integer.toString(valCr+1) );
				}
			}
			else
				return -1;
		}
	}

	/*
	 * 		initialisation de la r�partition
	 */
	
	private void initRepart()
	{
		for (int i=0; i<9; i++)
		{
			repartLig[i] = 0;
			repartCol[i] = 0;
			repartBloc[i] = 0;
		}							
	}

	/*
	 * 		calcule une nouvelle case en assurant une bonne r�partition
	 */
	
	private boolean putCaseRepart(int tour)
	{
		ligCr = (int) Math.round(8*Math.random());
		colCr = (int) Math.round(8*Math.random());
		int bloc = (ligCr/3)*3 + (colCr/3);
		while ( repartBloc[bloc] >= tour ||
				repartLig[ligCr] >= tour ||
				repartCol[colCr] >= tour )
		{
			if ( repartLig[ligCr] >= tour )
			{
				ligCr = (ligCr < 8)?ligCr+1:0;
				bloc = (ligCr/3)*3 + (colCr/3);
			}
			if ( repartCol[colCr] >= tour )
			{
				colCr = (colCr < 8)?colCr+1:0;
				bloc = (ligCr/3)*3 + (colCr/3);
			}
			if ( repartBloc[bloc] >= tour )
			{
				ligCr = (int) Math.round(8*Math.random());			
				colCr = (int) Math.round(8*Math.random());
				bloc = (ligCr/3)*3 + (colCr/3);
			}
		}
		repartLig[ligCr]++;
		repartCol[colCr]++;
		repartBloc[bloc]++;
		return true;		
	}

	private boolean putCase()
	{
			//		recherche de la case ayant le plus de possibilit�s
		int lig = (int) Math.round(8*Math.random());
		int col = (int) Math.round(8*Math.random());
		String valPos = "";
		int lgValPos = 0;
		int i = 0;
		while ( i < 81 )
		{
			if ( grille[lig][col] == 0 )
			{
				String val = possibleCase(lig, col);
				if ( val.length() > lgValPos )
				{
					ligCr = lig;
					colCr = col;
					valPos = val;
					lgValPos = valPos.length();
				}				
			}
			i++;
			col++;
			if ( col > 8 )
			{
				col = 0;
				lig++;
				if (lig > 8 )
					lig = 0;
			}
		}
				
		if ( valPos.length() > 0 )
		{
			int j = (int) Math.round((lgValPos-1)*Math.random());
			valCr = Integer.parseInt(valPos.substring(j,j+1));
			return true;
		}
		else
		{
			System.out.println("Erreur putCase :\n" + toString());
			return false;
		}
	}
	
	// edition de la grille
	
	public String toString()
	{
		String lig = "!------!------!------!\n";
		String r = lig + "!";
		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				r += Integer.toString(grille[i][j]) + " ";
				if ((j+1)%3 == 0)
					r += "!";
			}
			if ((i+1)%3 == 0)
				r += "\n" + lig + "!";
			else
				r += "\n!";				
		}
		return r;
	}
	
	/*
	 * 			R�solution automatique
	 * 			0 = r�solution OK
	 * 			-1 = le solveur tombe sur une impossibilit�
	 * 			-2 = plusieurs solutions possibles ou solveur insuffisant
	 * 			-10 = erreur
	 */
	
	public int solveAll( boolean hypo )
	{
		int r = 1;
		while ( r == 1)
			r = solve(hypo);
		return r;		
	}
	
/*
 * 			R�solution automatique par �tape avec ou sans hypoth�se
 * 			1 = �tape OK mais la r�solution n'est pas compl�te
 * 			0 = r�solution OK
 * 			-1 = le solveur tombe sur une impossibilit�
 * 			-2 = plusieurs solutions possibles ou solveur insuffisant
 */
	
	public int solve( boolean hypo )
	{
		boolean fini = true;

			// recherche solution simple

		for (int i=0; i<9; i++)
		{
			for (int j=0; j<9; j++)
			{
				int lig = i+1;
				int col = j+1;
					// exploration des cases vides
				if ( grille[i][j] == 0 )
				{
						// la case n'a qu'une solution possible ?
					fini = false;
					int s = solutionCase(i,j);
					int val = s+1;
					if ( s >= 0 )
					{
						putData(lig,col,val,false);
						mess = "Case " + Integer.toString(lig) + "," + Integer.toString(col) + " = "
									+ Integer.toString(val)	+ " : seule valeur possible";
						if ( debug ) System.out.println(mess);
						return 1;
					}
					else if ( s == -1 )
						return solveHypo(false);		// aucune valeur possible pour cette case
					else
					{
						val = solutionImpose(i,j);
						if ( val >= 0 )
						{			// la case est la seule � pouvoir contenir cette valeur
							val++;
							putData(lig,col,val,false);
							mess = "Case " + Integer.toString(lig) + "," + Integer.toString(col) + " = "
										+ Integer.toString(val)	+ " : seule case pouvant contenir cette valeur";
							if ( debug ) System.out.println(mess);
							return 1;
						}
					}
				}					
			}
		}
		if ( fini )
			return 0;
		if ( hypo )
			return solveHypo(true);
		return -2;
	}
	
	/*
	 * 			R�solution par hypoth�se
	 * 			nouvelle = true : nouvelle hypoth�se
	 * 			sinon prendre une autre hypoth�se
	 */
	
	public int solveHypo( boolean nouvelle )
	{			
				// d�pile pour faire une autre hypoth�se
		if ( !nouvelle )
		{
			if ( nbHypo == 0 )
				return -1;	// pas de solution
			restore();
			nbHypo--;
			if ( valHypo[nbHypo].length() > 0 )
			{		// essai autre valeur du couple
				save();
				int val = Integer.parseInt(valHypo[nbHypo].substring(0,1))+1;
				putData(ligHypo[nbHypo],colHypo[nbHypo],val,false);
				valHypo[nbHypo] = valHypo[nbHypo].substring(1);
				mess = "Impossibilit� : Case " + Integer.toString(ligHypo[nbHypo]) + "," + Integer.toString(colHypo[nbHypo]) + " = "
							+ Integer.toString(val)	+ " : autre hypoth�se niveau " + Integer.toString(nbHypo+1);
				if ( debug ) System.out.println(mess);
				nbHypo++;
				return 1;					
			}			
		}
			// recherche d'un couple
		while ( true  )
		{
			String pos = possibleCase(ligHypoC,colHypoC);		
			if ( pos.length() >= 2 && pos.length() <= maxPossible )
			{
				save();
				ligHypo[nbHypo] = ligHypoC+1;
				colHypo[nbHypo] = colHypoC+1;
				valHypo[nbHypo] = pos.substring(1);
				int val = Integer.parseInt(pos.substring(0,1))+1;
				putData(ligHypoC+1,colHypoC+1,val,false);
				nbHypo++;
				mess = "Case " + Integer.toString(ligHypoC+1) + "," + Integer.toString(colHypoC+1) + " = "
							+ Integer.toString(val)	+ " : nouvelle hypoth�se niveau " + nbHypo;
				if ( debug ) System.out.println(mess);
				return 1;					
			}
			if ( colHypoC >= 8 )
			{
				if ( ligHypoC >= 8 )
					break;
				ligHypoC++;
				colHypoC = 0;
			}
			else
				colHypoC++;
		}
		return -1;		
	}
	
	/*
	 * 		Retourne la solution unique pour une case vide i,j : (0 � 8)
	 * 		-1 pas de solution
	 * 		-2 plusieurs solutions
	 */

	private int solutionCase(int i, int j)
	{
		int sol = -1;
		for (int k=0; k < 9; k++)
		{
			if ( valeursPossibles[i][j][k] )
			{
				if ( sol != -1 )
					return -2;  // plusieurs solutions
				sol = k;
			}
		}
		return sol;		
	}

	/*
	 * 		Retourne les solutions possibles pour une case vide i,j
	 * 		sous forme de String: suite ordonn�e de chiffres 0 � 8
	 */

	private String possibleCase(int i, int j)
	{
		String sol = "";
		if ( grille[i][j] != 0)
			return sol;
		for (int k=0; k < 9; k++)
		{
			if ( valeursPossibles[i][j][k] )
			{
				sol += Integer.toString(k);
			}
		}
		return sol;		
	}

	/*
	 * 		Retourne les couples (c1,c2) possibles pour une case vide i,j
	 * 		dans un vecteur sous forme 10*c1 + c2 avec c1 < c2
	 */

	private Vector<Integer> getCouples(int i, int j)
	{
		Vector<Integer> sol = new Vector<Integer>();
		int[] p = new int[9];
		int indP = 0;
		for (int k=0; k < 9; k++)
		{
			if ( valeursPossibles[i][j][k] )
			{
				p[indP++] = k;
			}
		}
		for (int k=0; k < indP; k++)
		{
			for (int l=k+1; l < indP; l++)
			{
				sol.add(10*p[k] + p[l]);				
			}			
		}
		return sol;		
	}

	/*
	 * 		Recherche d'une valeur impos�e sur une case vide i,j
	 *  	par l'impossibilit� de se trouver dans une autre case
	 *  	de la ligne, colonne ou bloc
	 *  	retourne la valeur (0 � 8)
	 *  	-1 si pas de solution
	 */
  
	private int solutionImpose(int i, int j)
	{
		for (int k=0; k < 9; k++)
		{
			if ( valeursPossibles[i][j][k] )
			{			
					// recherche ligne
				boolean find = true;
				for (int l=0; l < 9; l++)
				{
					if ( l != i && valeursPossibles[l][j][k] )
					{
						find = false;
					}
				}
				if ( find )
				{
					return k; // la valeur k+1 ne peut pas �tre ailleurs sur la ligne
				}
				
					// recherche colonne
				find = true;
				for (int l=0; l < 9; l++)
				{
					if ( l != j && valeursPossibles[i][l][k] )
					{
						find = false;
					}
				}
				if ( find )
				{
					return k; // la valeur k+1 ne peut pas �tre ailleurs dans la colonne
				}		
					// recherche bloc
				find = true;
				int ibloc = i/3;
				ibloc *= 3;
				int jbloc = j/3;
				jbloc *= 3;
				for (int l=ibloc; l<ibloc+3; l++)
				{
					for (int m=jbloc; m<jbloc+3; m++)
					{
						if ( ( l != i || m != j ) && valeursPossibles[l][m][k] )
						{
							find = false;
							break;
						}
					}
				}		
				if ( find )
				{
					return k; // la valeur k+1 ne peut pas �tre ailleurs dans le bloc
				}
			}
		}
		return -1;
	}
	
	private String trace(int[] a)
	{
		String r = "";
		for (int i=0; i < a.length; i++)
			r += Integer.toString(a[i]);
		return r;
	}

	// calcule les coordonn�es de la case bi dans le bloc b
	
	static public int iBloc( int b, int bi )
	{
		return (b/3)*3 + bi/3;
	}

	static public int jBloc( int b, int bi )
	{
		return (b%3)*3 + bi%3;
	}

}
