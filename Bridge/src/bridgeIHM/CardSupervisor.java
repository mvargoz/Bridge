package bridgeIHM;

/**
 * 	Automate du jeu de bridge
 */

public class CardSupervisor implements Runnable {
	protected static int lgSleep = 50;
	protected Thread supervisor;
	protected BridgePanel panel;

	/**
	 * Constructeur
	 * @param panel
	 */
	public CardSupervisor(BridgePanel panel) {
		this.panel = panel;
		supervisor = new Thread(this);
		supervisor.start();
	}

	/**
	 * Run the thread
	 *
	 */
	public void run() {
		Thread me = Thread.currentThread();
		try {
			Thread.sleep(10 * lgSleep);
		} catch (InterruptedException e) {
		}
		while (supervisor == me) {
			try {
				Thread.sleep(lgSleep);
			} catch (InterruptedException e) {
				break;
			}
			synchronized (this) {
				if (panel.bAttente == false) {
					panel.bAttente = true;
					if (panel.bEnchere) {
						panel.enchere();
					} else if (panel.bJeuCarte) {
						panel.jeuCarte();
					}
				}
			}
		}
	}

}