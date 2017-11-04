package Card;

import java.util.*;

/**
 * Historisation du jeu
 *
 */
public class CardBoardHisto {

	private Vector<Object> histo = new Vector<Object>();

	public CardBoardHisto() {
	}

	/**
	 * Historisation d'un board
	 * @param b
	 */
	public void historise(CardBoard b) {
		histo.addElement(b.clone());
	}

	/**
	 * Vidage de l'historique
	 */
	public void clear() {
		histo.removeAllElements();
	}

	/**
	 * Restauration dernier board
	 * @return CardBoard
	 */
	public CardBoard restore() {
		CardBoard b = null;
		if (histo.size() > 1) {
			try {
				histo.removeElementAt(histo.size() - 1);
				b = (CardBoard) ((CardBoard) histo.lastElement()).clone();
			} catch (Exception e) {
			}
		}
		return b;
	}

	/**
	 * Teste si l'on est déjà passé par cette position
	 * détection d'une boucle 
	 * @param b = CardBoard
	 * @return true or false
	 */
	public boolean contains(CardBoard b) {
		return histo.contains(b);
	}

}