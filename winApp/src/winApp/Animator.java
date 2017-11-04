package winApp;

import javax.swing.JComponent;

public class Animator extends JComponent implements Runnable {
	private static final long serialVersionUID = 1L;
	protected static int nbStep = 10;
	protected static int lgSleep = 50;
	protected Thread animator;
	protected int Step = 0;

	public Animator() {
		super();
	}

	// d�marrer l'animation

	public void go() {

	}

	// arr�ter l'animation

	public void halt() {
		setVisible(false);
	}

	// Run the animation thread.

	public void run() {
		setVisible(true);
		Thread me = Thread.currentThread();
		while (animator == me) {
			try {
				Thread.sleep(lgSleep);
			} catch (InterruptedException e) {
				break;
			}
			synchronized (this) {
				Step++;
				if (Step > nbStep) { // mouvement en court termin�
					animator = null;
					setVisible(false);
				} else
					repaint();
			}
		}
	}

}
