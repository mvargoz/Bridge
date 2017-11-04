package Chiffres;

//	pile de calcul

public class OperStack {
	public class ElemSt {
		int type; // 0 = opérateur, 1 = nombre
		int valeur; // opération (0 pour +, 1 pour -, 2 pour *, 3 pour /) ou nombre
	}

	private ElemSt[] pile;
	private int indPile = -1;
	private int dimension = 0;
	private int nbCh = 0;
	private int nbOp = 0;
	private int indEnum = 0;

	// Création d'une pile

	public OperStack(int dim) {
		pile = new ElemSt[dim];
		for (int i = 0; i < dim; i++) {
			pile[i] = new ElemSt();
		}
		indPile = 0;
		dimension = dim;
	}

	// Clonage d'une pile

	public OperStack clone() {
		OperStack newos = new OperStack(this.dimension);
		for (int i = 0; i < indPile; i++) {
			newos.pile[i] = new ElemSt();
			newos.pile[i].type = pile[i].type;
			newos.pile[i].valeur = pile[i].valeur;
		}
		newos.indPile = indPile;
		newos.nbCh = nbCh;
		newos.nbOp = nbOp;
		return newos;
	}

	public int dimension() {
		return dimension;
	}

	public int nbElem() {
		return indPile;
	}

	// empilage

	public boolean push(int t, int v) {
		if (t == 0)
			return pushOp(v);
		else
			return pushCh(v);
	}

	// empilage d'un opérateur

	public boolean pushOp(int v) {
		if (nbOp >= nbCh - 1)
			return false;
		pile[indPile].type = 0;
		pile[indPile].valeur = v;
		indPile++;
		nbOp++;
		return true;
	}

	// empilage d'un nombre

	public boolean pushCh(int v) {
		pile[indPile].type = 1;
		pile[indPile].valeur = v;
		indPile++;
		nbCh++;
		return true;
	}

	// dépilage

	public boolean pull() {
		if (indPile > 0) {
			indPile--;
			if (pile[indPile].type == 0)
				nbOp--;
			else
				nbCh--;
		} else
			return false;
		return true;
	}

	// dépilage d'un opérateur

	public int pullOp() {
		if (indPile > 0 && pile[indPile - 1].type == 0) {
			nbOp--;
			indPile--;
			return pile[indPile].valeur;
		}
		return -1;
	}

	// dépilage d'un nombre

	public int pullCh() {
		if (indPile > 0 && pile[indPile - 1].type == 1) {
			nbCh--;
			indPile--;
			return pile[indPile].valeur;
		}
		return -1;
	}

	// vidage de la pile

	public boolean initGetPile() {
		indEnum = 0;
		if (indPile < 1) {
			return false;
		}
		return true;
	}

	public ElemSt getPile() {
		if (indEnum < indPile) {
			return pile[indEnum++];
		} else {
			indEnum = 0;
		}
		return null;
	}

	// calcul de la pile (notation polonaire inversée)

	public int calcul() {
		if (indPile < 1 || nbOp != nbCh - 1)
			return -1; // pile vide ou nombre d'opérateurs insuffisants

		ElemSt[] ns = copy(pile);
		int i = indPile;
		int res = 0;
		while (i > 1) {
			int k = 0;
			// recherche (valeur valeur opérande) exécution de l'opération et réduction pile
			while (k < i - 2) {
				if (ns[k].type == 1 && ns[k + 1].type == 1 && ns[k + 2].type == 0) {
					int op = ns[k + 2].valeur;
					int op2 = ns[k + 1].valeur;
					res = ns[k].valeur;
					if (res < op2)
						return -1; // opération non normalisée
					if (op == 0) {
						res += op2;
					} else if (op == 1) {
						res -= op2;
					} else if (op == 2) {
						if (res <= 1 || op2 <= 1)
							return -1; // opération inutile
						res *= op2;
					} else if (op == 3) {
						if (op2 <= 1 || (res % op2) != 0)
							return -1; // division inutile ou non entière
						res /= op2;
					}
					if (res <= 0)
						return -1; // opération interdite
					ns[k].valeur = res;
					for (int j = k + 1; j < i - 2; j++) {
						ns[j].type = ns[j + 2].type;
						ns[j].valeur = ns[j + 2].valeur;
					}
					i -= 2;
					break;
				} else {
					k++;
				}
			}
		}
		return ns[0].valeur;
	}

	// mise en forme de la formule pour présentation

	public String string() {
		if (indPile < 1 || nbOp != nbCh - 1)
			return null;

		ElemSt[] ns = copy(pile);

		int i = indPile;
		String[] pileRes = new String[indPile];
		for (int k = 0; k < indPile; k++)
			pileRes[k] = Integer.toString(ns[k].valeur);
		String res = "";

		while (i > 1) {
			int k = 0;
			// recherche (valeur valeur opérande) exécution de l'opération et réduction pile
			while (k < i - 2) {
				if (ns[k].type == 1 && ns[k + 1].type == 1 && ns[k + 2].type == 0) {
					int op = ns[k + 2].valeur;
					res = "(" + pileRes[k];
					if (op == 0) {
						res += "+";
					} else if (op == 1) {
						res += "-";
					} else if (op == 2) {
						res += "*";
					} else if (op == 3) {
						res += "/";
					}
					res += pileRes[k + 1] + ")";
					pileRes[k] = res;
					for (int j = k + 1; j < i - 2; j++) {
						ns[j].type = ns[j + 2].type;
						ns[j].valeur = ns[j + 2].valeur;
						pileRes[j] = pileRes[j + 2];
					}
					i -= 2;
					break;
				} else
					k++;
			}
		}
		return pileRes[0];
	}

	// trace

	public String trace() {
		String res = "Stack=(" + indPile + "," + nbCh + "," + nbOp + ")";
		for (int i = 0; i < indPile; i++) {
			if (pile[i].type == 0) {
				int op = pile[i].valeur;
				if (op == 0)
					res += "+ ";
				else if (op == 1)
					res += "- ";
				else if (op == 2)
					res += "* ";
				else if (op == 3)
					res += "/ ";
			} else
				res += Integer.toString(pile[i].valeur) + " ";
		}
		return res;
	}

	// copie d'un élément

	private ElemSt[] copy(ElemSt[] os) {
		ElemSt[] ns = new ElemSt[os.length];
		for (int i = 0; i < ns.length; i++) {
			ns[i] = new ElemSt();
		}
		for (int i = 0; i < os.length; i++) {
			ns[i].type = os[i].type;
			ns[i].valeur = os[i].valeur;
		}
		return ns;
	}

}
