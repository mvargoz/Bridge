package Card;

/**
 * Tableau de jeu représentant les cases
 *
 */
public class CardBoard implements Cloneable {
	/**
	 * nombre de cases
	 */
	private int dim;
	/**
	 * nombre de cartes
	 */
	private int nbc;
	/**
	 * nombre de cartes cachées par case
	 */
	public int[] nbCartesCaches;
	/**
	 * nombre de cartes visibles par case
	 */
	public int[] nbCartesVisibles;
	/**
	 * cartes cachées dans chaque case 
	 */
	public int[][] boardCache;
	/**
	 * cartes visibles dans chaque case 
	 */
	public int[][] boardVisible;

	/**
	 * Constructeur
	 * @param dimBoard nombre de cases
	 * @param nbCartes nombre de cartes
	 */
	public CardBoard(int dimBoard, int nbCartes) {
		dim = dimBoard;
		nbc = nbCartes;
		boardCache = new int[dimBoard][nbc];
		nbCartesCaches = new int[dimBoard];
		boardVisible = new int[dimBoard][nbc];
		nbCartesVisibles = new int[dimBoard];
	}

	/**
	 * clonage
	 */
	public Object clone() {
		Object c = null;
		try {
			c = super.clone();
			((CardBoard) c).nbCartesCaches = (int[]) nbCartesCaches.clone();
			((CardBoard) c).nbCartesVisibles = (int[]) nbCartesVisibles.clone();
			/*
			 * devrait faire la même chose que la suite mais ne marche pas sans un doute un
			 * pb de clonage en profondeur ((CardBoard)c).boardCache = ( int[][] )
			 * boardCache.clone(); ((CardBoard)c).boardVisible = ( int[][] )
			 * boardVisible.clone();
			 */
			((CardBoard) c).boardCache = new int[dim][nbc];
			((CardBoard) c).boardVisible = new int[dim][nbc];
			for (int i = 0; i < dim; i++) {
				for (int j = 0; j < nbCartesCaches[i]; j++)
					((CardBoard) c).boardCache[i][j] = boardCache[i][j];
				for (int j = 0; j < nbCartesVisibles[i]; j++)
					((CardBoard) c).boardVisible[i][j] = boardVisible[i][j];
			}
		} catch (Exception e) {
			return null;
		}

		return c;
	}

	/**
	 * @return nombre de cases
	 */
	public int length() {
		return dim;
	}

	/**
	 * Initialisation
	 */
	public void init() {
		for (int i = 0; i < dim; i++) {
			nbCartesCaches[i] = 0;
			nbCartesVisibles[i] = 0;
		}
	}

	/**
	 * test d'égalité
	 */
	public boolean equals(Object b) {
		if (dim != ((CardBoard) b).dim)
			return false;
		for (int i = 0; i < dim; i++) {
			if (nbCartesCaches[i] != ((CardBoard) b).nbCartesCaches[i])
				return false;
			if (nbCartesVisibles[i] != ((CardBoard) b).nbCartesVisibles[i])
				return false;
			for (int j = 0; j < nbCartesCaches[i]; j++) {
				if (boardCache[i][j] != ((CardBoard) b).boardCache[i][j])
					return false;
			}
			for (int j = 0; j < nbCartesVisibles[i]; j++) {
				if (boardVisible[i][j] != ((CardBoard) b).boardVisible[i][j])
					return false;
			}
		}
		return true;
	}

	/**
	 * impression du contenu
	 */
	public void print() {
		for (int i = 0; i < dim; i++) {
			System.out.print("case " + Integer.toString(i));
			System.out.print(" -c");
			for (int j = 0; j < nbCartesCaches[i]; j++) {
				System.out.print("," + Integer.toString(boardCache[i][j]));
			}
			System.out.print(" -v");
			for (int j = 0; j < nbCartesVisibles[i]; j++) {
				System.out.print("," + Integer.toString(boardVisible[i][j]));
			}
			System.out.println();
		}
	}
}
