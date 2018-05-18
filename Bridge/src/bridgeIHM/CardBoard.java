package bridgeIHM;

/*
 * 		Etat des cartes en cours de jeu	
 */

public class CardBoard implements Cloneable {
	/**
	 * dimension 1 du board: 4 joueurs x 4 couleurs
	 */
	private int dim = 16;
	/**
	 * dimension 2 du board: nombre de cartes max par couleur
	 */
	private int nbc = 13;
	/**
	 * nombre de cartes pour 4 joueurs x 4 couleurs
	 */
	public int[] nbCartes;
	/**
	 * contient les cartes en cours de jeu dans l'ordre : N E S W et T K C P
	 * les cartes sont codée : couleur * 13 + hauteur
	 * couleur de 0 à 3 = TKCP, hauteur de 0 à 12 = du 2 à l'As
	 */
	public int[][] board;

	/**
	 * constructeur
	 * @param dim
	 * @param nbc
	 */
	public CardBoard(int dim, int nbc) {
		this.dim = dim;
		this.nbc = nbc;
		board = new int[dim][nbc];
		nbCartes = new int[dim];
	}

	/**
	 * clonage
	 */
	public Object clone() {
		Object c = null;
		try {
			c = super.clone();
			((CardBoard) c).nbCartes = (int[]) nbCartes.clone();
			((CardBoard) c).board = new int[dim][nbc];
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < nbCartes[i]; j++)
					((CardBoard) c).board[i][j] = board[i][j];
			}
		} catch (Exception e) {
			return null;
		}

		return c;
	}

	/**
	 * @return dimension 1
	 */
	public int length() {
		return dim;
	}

	/**
	 * initialisation
	 */
	public void init() {
		for (int i = 0; i < dim; i++) {
			nbCartes[i] = 0;
		}
	}

	/**
	 * comparaison
	 */
	public boolean equals(Object b) {
		if (dim != ((CardBoard) b).dim)
			return false;
		for (int i = 0; i < dim; i++) {
			if (nbCartes[i] != ((CardBoard) b).nbCartes[i])
				return false;
			for (int j = 0; j < nbCartes[i]; j++) {
				if (board[i][j] != ((CardBoard) b).board[i][j])
					return false;
			}
		}
		return true;
	}

	/**
	 * impression
	 */
	public void print() {
		for (int i = 0; i < dim; i++) {
			System.out.print("case " + Integer.toString(i));
			System.out.print(" -c");
			for (int j = 0; j < nbCartes[i]; j++) {
				System.out.print("," + Integer.toString(board[i][j]));
			}
			System.out.println();
		}
	}
}
