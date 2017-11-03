package bridgeIHM;

import java.util.*;

/*
 * 			historique du jeu en cours
 */

public class CardBoardHisto {

	private Vector<CardBoard> histo = new Vector<CardBoard>();

	public CardBoardHisto() {
	}

	public void historise(CardBoard b) {
		histo.addElement((CardBoard) b.clone());
	}

	public void clear() {
		histo.removeAllElements();
	}

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

	public boolean contains(CardBoard b) {
		return histo.contains(b);
	}

}
