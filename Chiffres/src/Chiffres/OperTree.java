package Chiffres;

import java.util.LinkedList;
import java.util.Vector;

import Chiffres.OperStack.ElemSt;

//	mise en forme des op�rations sous forme d'arbre

/**
 * @author Michel
 *
 */
public class OperTree implements Cloneable {

	class Element {
		int type; // 0 = Noeud, 1 = Feuille
		int valeur; // opération (0 pour +, 1 pour -, 2 pour *, 3 pour /) ou nombre
		Element bg; // si noeud branche droite
		Element bd; // si noeud branche gauche
	}

	Element racine;

	public OperTree() {
		racine = null;
	}

	public OperTree(OperStack p) {
		racine = null;
		LinkedList<Element> pileAux = new LinkedList<Element>();
		if (!p.initGetPile())
			return;
		ElemSt ns;
		while ((ns = p.getPile()) != null) {
			Element e = new Element();
			e.type = ns.type;
			e.valeur = ns.valeur;
			if (ns.type == 0) {
				e.bd = pileAux.pollLast();
				e.bg = pileAux.pollLast();
			}
			pileAux.addLast(e);
		}
		racine = pileAux.pollLast();
	}

	public OperTree clone() {
		OperTree ot = new OperTree();
		LinkedList<Element> pileExp = new LinkedList<Element>();
		pileExp.addLast(racine);
		while (!pileExp.isEmpty()) {
			Element exp = pileExp.pollLast();
			Element ne = new Element();
			if (exp == racine)
				ot.racine = ne;
			ne.type = exp.type;
			ne.valeur = exp.valeur;
			if (exp.type == 0) {
				ne.bd = exp.bd;
				ne.bg = exp.bg;
				pileExp.addLast(exp.bd);
				pileExp.addLast(exp.bd);
			}
		}
		return ot;
	}

	// simplification d'une formule

	public OperTree simplify() {
		OperTree ot = this.clone();
		LinkedList<Element> pileExp = new LinkedList<Element>();
		boolean simple = false; // indique que la formule est totalement simplifi�e
		while (!simple) {
			pileExp.addLast(ot.racine);
			Element exp = ot.racine;
			simple = true;
			while (!pileExp.isEmpty()) {
				// exploration de l'arbre
				exp = pileExp.pollLast();
				if (exp.type == 0) {
					if (exp.bd.type == 0
							&& (exp.valeur <= 1 && exp.bd.valeur <= 1 || exp.valeur >= 2 && exp.bd.valeur >= 2)) {
						// simplification des op�rations + - et * / en cascade
						simple = false;
						// insersion du membre droit
						Element oldDroit = exp.bd;
						exp.bd = oldDroit.bg;
						oldDroit.bg = exp.bg;
						exp.bg = oldDroit;
						if (exp.valeur == 1) {
							// inverser l'op�ration si -
							if (oldDroit.valeur == 0)
								oldDroit.valeur = 1;
							else
								oldDroit.valeur = 0;
						} else if (exp.valeur == 3) {
							// inverser l'op�ration si /
							if (oldDroit.valeur == 2)
								oldDroit.valeur = 3;
							else
								oldDroit.valeur = 2;
						}
						pileExp.addLast(exp);
					} else if (exp.bg.type == 0
							&& (exp.valeur <= 1 && exp.bg.valeur <= 1 || exp.valeur >= 2 && exp.bg.valeur >= 2)
							&& exp.bd.type == 1 && exp.bg.bd.type == 1 && exp.bd.valeur > exp.bg.bd.valeur) {
						// ordonnancement des op�rations de meme type
						simple = false;
						int aux = exp.bd.valeur;
						exp.bd.valeur = exp.bg.bd.valeur;
						exp.bg.bd.valeur = aux;
						aux = exp.valeur;
						exp.valeur = exp.bg.valeur;
						exp.bg.valeur = aux;
						pileExp.addLast(exp);
					} else {
						// mise en r�serve des branches � explorer
						pileExp.addLast(exp.bd);
						pileExp.addLast(exp.bg);
					}
				}
			}
		}
		return ot;
	}

	/**
	 * mise en forme textuelle de la formule
	 * @return String
	 */
	public String string() {
		return stringBranch(0, racine);
	}

	/**
	 * mise sous forme formule
	 * @param priority
	 * @param e
	 * @return
	 */
	private String stringBranch(int priority, Element e) {
		String rs = "";
		if (e == null)
			return "<null>";
		if (e.type == 1) {
			rs = Integer.toString(e.valeur);
		} else {
			int priorityG = 2 * (e.valeur / 2);
			int priorityD = e.valeur;
			if (priorityG < priority) {
				rs += "(" + stringBranch(priorityG, e.bg) + operator(e.valeur) + stringBranch(priorityD, e.bd) + ")";
			} else {
				rs += stringBranch(priorityG, e.bg) + operator(e.valeur) + stringBranch(priorityD, e.bd);
			}
		}
		return rs;
	}
	
	/**
	 * mise sous forme suite d'opération
	 * @return String
	 */
	public String operString() {
		Vector<String> v = new Vector<String>();
		operBranch(racine, v);
		String soluce = "";
		for ( String s : v )  {
			soluce += s + ", ";			
		}
		return soluce;
	}
	
	/**
	 * Mise sous forme suite d'opérations
	 * @param e
	 * @param v
	 * @return
	 */
	private int operBranch(Element e, Vector<String> v) {
		if (e.type == 1) {
			return e.valeur;
		} else {
			int i = operBranch(e.bg, v);
			int j = operBranch(e.bd, v);
			int res = execOper(i, j, e.valeur );
			v.add(Integer.toString(i) + operator(e.valeur) + Integer.toString(j) + " = " + Integer.toString(res));
			return res;
		}
	}

	private int execOper(int i, int j, int oper)  {
		switch (oper)  {
		case 0: return i+j;
		case 1: return i-j;
		case 2: return i*j;
		case 3: return i/j;
		}
		return -1;		
	}
	
	// mise en forme textuelle de l'arbre

	public String trace() {
		return traceBranch(racine);
	}

	private String traceBranch(Element e) {
		String rs = "";
		if (e == null)
			return "<null>";
		if (e.type == 1)
			rs = Integer.toString(e.valeur);
		else
			rs += "[" + operator(e.valeur) + traceBranch(e.bg) + traceBranch(e.bd) + "]";
		return rs;
	}

	private String operator(int op) {
		switch (op) {
		case 0:
			return "+";
		case 1:
			return "-";
		case 2:
			return "*";
		case 3:
			return "/";
		}
		return "???";
	}

}
