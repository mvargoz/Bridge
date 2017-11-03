package bridgeIHM;

/*
 * 		Etat des cartes en cours de jeu	
 */

public class CardBoard implements Cloneable {
	private int dim = 16;
	private int nbc = 13;
	public int[] nbCartes;
	// contient les cartes en cours de jeu dans l'ordre : N E S W et T K C P
	// les cartes sont codée : couleur * 13 + hauteur
	// couleur de 0 à 3 = TKCP, hauteur de 0 à 12 = du 2 à l'As
	public int[][] board;

	public CardBoard(int dim, int nbc) {
		this.dim = dim;
		this.nbc = nbc;
		board = new int[dim][nbc];
		nbCartes = new int[dim];
	}

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

	public int length() {
		return dim;
	}

	public void init() {
		for (int i = 0; i < dim; i++) {
			nbCartes[i] = 0;
		}
	}

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
