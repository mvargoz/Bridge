package Chiffres;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.SwingWorker;


/**
 *	"Le Compte est bon"
 *	on donne une liste de 6 nombres parmi { 1..9, 10, 25, 75, 100 } et un nombre de 3 chiffres
 *	utiliser les chiffres de la liste au max une fois pour obtenir le nombre donné avec les opérateurs { + - * / }
 * @author Michel
 *
 */
public class SolveGame extends SwingWorker<String, Object>  {

	protected int nombre;
	protected int[] chiffres = new int[6];

	protected static boolean bdebug = false;

	// pile operations
	protected OperStack ops;

	// resultats
	protected OperStack[] opres;
	protected int maxRes = 10000;
	protected int nbResultats;
	protected int solOpt = 0;
	protected int nbElemOpt = 11;
	protected String[] resultat;
	protected int nbRes;
	
	// constructeurs

	public SolveGame(String pb) {
		StringTokenizer param = new StringTokenizer(pb, " ");
		int i = 0;
		while (i < 7 && param.hasMoreTokens()) {
			String nb = param.nextToken();
			if (i == 6) {
				nombre = Integer.parseInt(nb);
			} else {
				chiffres[i] = Integer.parseInt(nb);
			}
			i++;
		}
	}

	public SolveGame() {
	}

	// version batch
	// arguments: nombre a trouver suivi des 6 nombres donnes

	public static void main(String[] args) {
		if (args.length != 7)
			usage();
		SolveGame s = new SolveGame();
		s.nombre = Integer.parseInt(args[0]);
		for (int i = 0; i < 6; i++) {
			s.chiffres[i] = Integer.parseInt(args[i + 1]);
		}
		new Thread(s).start();
		System.out.println(s.getResult());
	}

	// Erreurs de parametres

	static void usage() {
		System.out.println("Usage: java Chiffres <nombre à trouver> <6 nombres donn�s>");
		System.exit(0);
	}

	// version thread
	// arguments: nombre a trouver suivi des 6 nombres donnes

	@Override
	protected String doInBackground() throws Exception {
		opres = new OperStack[maxRes];
		nbResultats = 0;
		ops = new OperStack(11);
		solveStack(chiffres);
		searchSolutions();
		return getResult();
	}
    /*
     * Executed in event dispatching thread
     */
    @Override
    public void done() {
		setProgress(100);
    }
    
	public String getResult() {
		String soluce = "";
		soluce += "Nombre " + nombre + " avec " + chiffres[0] + "," + chiffres[1] + "," + chiffres[2] + ","
				+ chiffres[3] + "," + chiffres[4] + "," + chiffres[5] + '\n';
		soluce += nbRes + " solutions " + '\n';
		if (nbResultats > 0) {
			OperTree t = new OperTree(opres[solOpt]);
			OperTree ts = t.simplify();
			soluce += "Solution optimale : " + ts.operString() + '\n';
			soluce += "Autres solutions : " + '\n';
			for (int i = 0; i < nbRes && i < 20; i++) {
				soluce += resultat[i] + '\n';
			}
		}
		return soluce;
	}

	// recherche des solutions

	private boolean solveStack(int[] ch) {
		int nb = ops.calcul();
		if (nombre == nb) {
			// solution trouv�e
			debug(ops.trace() + " result=" + nb);
			opres[nbResultats] = ops.clone();
			if (nbResultats < opres.length - 1) {
				if (ops.nbElem() < nbElemOpt) {
					nbElemOpt = ops.nbElem();
					solOpt = nbResultats;
				}
				nbResultats++;
			} else
				return true; // plus de place
			return false; // on continue pour trouver d'autres solutions
		}

		// pile pleine : 6 chiffres et 5 op�randes

		if (ops.nbElem() == 11)
			return false;

		int lg = ch.length;

		// ajout d'un nombre

		for (int i = 0; i < lg; i++) {
			ops.pushCh(ch[i]);
			// constitution d'une nouvelle liste sans ce nombre
			int[] chr = new int[lg - 1];
			for (int j = 0, k = 0; j < lg; j++) {
				if (j != i) {
					chr[k++] = ch[j];
				}
			}
			// soumission de la nouvelle liste
			if (solveStack(chr))
				return true;
			ops.pull();
		}

		// ajout d'un opérateur

		for (int i = 0; i < 4; i++) {
			if (ops.pushOp(i)) {
				if (solveStack(ch))
					return true;
				ops.pull();
			} else
				return false;
		}
		return false;
	}

	// recherche des vraies solutions

	private void searchSolutions() {
		resultat = new String[nbResultats];
		nbRes = 0;
		for (int i = 0; i < nbResultats; i++) {
			OperTree t = new OperTree(opres[i]);
			OperTree ts = t.simplify();
			String res = ts.operString();
			boolean deja = false;
			for (int j = 0; j < nbRes; j++) {
				if (res.equals(resultat[j])) {
					deja = true;
					break;
				}
			}
			if (!deja)
				resultat[nbRes++] = res;
		}

	}

	private void debug(String s) {
		if (bdebug)
			System.out.println("debug:" + s);
	}

}
