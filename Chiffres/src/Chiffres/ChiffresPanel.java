package Chiffres;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;

import winApp.ContexteGlobal;

/**
 * @author Michel
 *
 */
public class ChiffresPanel extends JPanel implements ActionListener, PropertyChangeListener  {

	private static final long serialVersionUID = 1L;
	private JLabel jltitle = new JLabel(ContexteGlobal.getResourceString("titleLabel"));
	private JButton calculButton = new JButton(ContexteGlobal.getResourceString("computeLabel"));
	protected JTextField saisie = new JTextField(25);
	protected JTextArea result = new JTextArea();
    private JProgressBar progressBar = new JProgressBar(0, 100);
    private PadSaisie pad = new PadSaisie(this);

	/**
	 * construct panel
	 */
	public ChiffresPanel() {

		setLayout(new BorderLayout(5, 5));

		// saisie

		JPanel jpLevel = new JPanel(new FlowLayout());
		// jltitle.setFont(new Font("Serif", Font.PLAIN, 24));
		jpLevel.add(jltitle);
		jpLevel.add(saisie);
		jpLevel.add(calculButton);
		saisie.addMouseListener(new ListMouseListener());
		calculButton.addActionListener(this);
		calculButton.setActionCommand("compute");
		winApp.ContexteGlobal.frame.getRootPane().setDefaultButton(calculButton);
		add(jpLevel, BorderLayout.NORTH);

		// resultat

		JScrollPane resultPanel = new JScrollPane(result);
		result.setLineWrap(true);
		result.setWrapStyleWord(true);
		result.setBorder(BorderFactory.createLineBorder(Color.black));
		add(resultPanel, BorderLayout.CENTER);

		// progress bar

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
		add(progressBar, BorderLayout.SOUTH);

	}

	/** 
	 * 	action listener
	 */
	public void actionPerformed(ActionEvent ev) {
		switch (ev.getActionCommand()) {
		case "compute":
			if ( !compute() )
				Toolkit.getDefaultToolkit().beep();
			break;
		}
	}

	/**
	 * data capture controls
	 * @return true or false
	 */
	public boolean controlSaisie()  {
		StringTokenizer param = new StringTokenizer(saisie.getText(), " ");
		int i = 0;
		while (i <= 5 && param.hasMoreTokens()) {
			int nb = new Integer(param.nextToken());
			if ( nb < 1 || ( nb > 10 && nb != 25 && nb != 50 && nb != 75 && nb != 100 ))
				return false;
			i++;
		}
		if ( i != 6 || !param.hasMoreTokens())
			return false;
		int nb = new Integer(param.nextToken());
		if ( nb < 100 || nb > 999 )
			return false;
		
		return true;
	}

	/**
	 * compute
	 * @return true or false
	 */
	public boolean compute()  {
		if ( controlSaisie() )  {
			try {
				result.setText("calcul en cours");
		    	progressBar.setIndeterminate(true);
				calculButton.setEnabled(false);
		        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				SolveGame solve = new SolveGame(saisie.getText());
				solve.addPropertyChangeListener(this);
				solve.execute();
				return true;
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}  else  {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErr"),
					ContexteGlobal.getResourceString("messCompute"), JOptionPane.ERROR_MESSAGE);
			return false;			
		}
	}

	/**
	 * Invoked when compute task progress property changes
	 */
	public void propertyChange(PropertyChangeEvent evt) {
	    switch (evt.getPropertyName()) {
	    case "progress":
	        int progress = (Integer) evt.getNewValue();
	        progressBar.setIndeterminate(false);
	        progressBar.setValue(progress);
	        break;
	    case "state":
	        if ( evt.getNewValue() == StateValue.DONE )  {
	            try {
	    			result.setText(((SolveGame)evt.getSource()).get());
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
				calculButton.setEnabled(true);
		        setCursor(null); //turn off the wait cursor
				repaint();
	        }	        	
	        break;     
	    } 
	}

	/**
	 * mouse listener for text field
	 */
	protected class ListMouseListener implements MouseListener  {
	
		@Override
		public void mouseClicked(java.awt.event.MouseEvent e) {
			pad.open();
		}
	
		@Override
		public void mouseEntered(java.awt.event.MouseEvent e) {
		}
	
		@Override
		public void mouseExited(java.awt.event.MouseEvent e) {
		}
			//  right clic
		@Override
		public void mousePressed(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
	        }
		}
	
		@Override
		public void mouseReleased(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
	       }
		}
	
	}
}
