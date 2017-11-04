package Card;

import winApp.ContexteGlobal;

/**
 * Superviseur de thread pour l'animation
 *
 */
public class CardSupervisor implements Runnable {
	
	/**
	 * Attente pour ralentir le mouvement
	 */
	protected static int lgSleep = 50;

	/**
	 * thread d'animation
	 */
	protected Thread supervisor;

	/**
	 * jeu à animer
	 */
	protected CardGame jeu;

	/**
	 * constructeur
	 * @param jeu
	 */
	public CardSupervisor(CardGame jeu) {
		this.jeu = jeu;
		supervisor = new Thread(this);
		supervisor.start();
	}

	/**
	 * Surveillance de bStartJeuAuto pour lancement de l'animation
	 */
	public void run() {
		Thread me = Thread.currentThread();
		while (supervisor == me) {
			try {
				Thread.sleep(lgSleep);
			} catch (InterruptedException e) {
				break;
			}
			synchronized (this) {
				if (jeu.bStartJeuAuto) {
					((CardGamePanel) ContexteGlobal.frame.panel).animation();
					jeu.bStartJeuAuto = false;
					ContexteGlobal.frame.setMessage(jeu.messageFixe + ": " + ContexteGlobal.getResourceString("mess10"));
				}
			}
		}
	}

}