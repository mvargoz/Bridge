package Card;

/**
 * Param�tre de programmation d'une animation
 * repr�sentant un mouvement d'une case � l'autre
 *
 */
public class CardMove {

	public int fromBoard; // case source
	public int toBoard; // case objet
	public int noCard; // num�ro de carte s�lectionn�e
	public boolean cardVisible; // retourner la carte

	/**
	 * Constructeur
	 * @param from case
	 * @param to case
	 * @param no carte
	 * @param visible
	 */
	public CardMove(int from, int to, int no, boolean visible) {
		this.fromBoard = from;
		this.toBoard = to;
		this.noCard = no;
		this.cardVisible = visible;
	}

	/**
	 * Constructeur mouvement vide
	 */
	public CardMove() {
		this.fromBoard = 0;
		this.toBoard = 0;
		this.noCard = 0;
		this.cardVisible = false;
	}
}
