package Chiffres;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import winApp.ContexteGlobal;

/**
 * @author Michel
 *
 */
public class PadSaisie extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

		//	frame
	
	private ChiffresPanel parentPanel;
	
		//   dimensions
	
	private static final Dimension dimButton = new Dimension(20, 20);
	private static final Dimension dimButtonText = new Dimension(100, 20);
	private static final Dimension dimPanel = new Dimension(300, 150);

		//	panel principal
		
	private JPanel panel = new JPanel(new BorderLayout(5,5));
	private JPanel padPanel = new JPanel(new GridLayout(3,5));
	private int[] padTouch = {0,1,2,3,4,5,6,7,8,9,10,25,50,75,100};
	private JButton[] padButton = new JButton[padTouch.length];
	private JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,5,5));
	private JButton computeButton = new JButton(ContexteGlobal.getResourceString("computeLabel"));
	private JButton deleteButton = new JButton(ContexteGlobal.getResourceString("deleteLabel"));
	
	
	
	
		// contruct pad
	
	public PadSaisie(ChiffresPanel cp)  {
		super(winApp.ContexteGlobal.frame, ContexteGlobal.getResourceString("titlePadLabel"), false);
		parentPanel = cp;
		
			//	construction du panel
			
		getContentPane().add(panel);		
		panel.setPreferredSize(dimPanel);
		
		int i = 0;
		for ( int touch : padTouch )  {
			padButton[i] = new JButton(new Integer(touch).toString());
			padButton[i].setActionCommand("chiffre");
			padButton[i].addActionListener(this);
			padButton[i].setPreferredSize(dimButton);
			padPanel.add(padButton[i]);
			i++;
		}	
		panel.add(padPanel, BorderLayout.CENTER);
		
		computeButton.setActionCommand("compute");
		computeButton.addActionListener(this);
		computeButton.setPreferredSize(dimButtonText);
		buttonPanel.add(computeButton);
		
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);
		deleteButton.setPreferredSize(dimButtonText);
		buttonPanel.add(deleteButton);
		
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		pack();		
		setVisible(false);								
	}

	// open & position pad
		
	public void open() {
		
		//  position
		
		Point p = parentPanel.saisie.getLocationOnScreen();
		p.y += 25;
		setLocation(p);
		enabledButton();
		setVisible(true);								
	}

	// close pad
	
	public void close() {
		setVisible(false);										
	}
	
	private void enabledButton() {
		String s = parentPanel.saisie.getText();
		String[] nb = s.split("\\s");
		int i = nb.length;
		if ( i < 6 )  {
			padButton[0].setEnabled(false);
			padButton[10].setEnabled(true);
			padButton[11].setEnabled(true);
			padButton[12].setEnabled(true);
			padButton[13].setEnabled(true);
			padButton[14].setEnabled(true);			
		} else {
			if ( i == 7 )
				padButton[0].setEnabled(true);
			else
				padButton[0].setEnabled(false);
			padButton[10].setEnabled(false);
			padButton[11].setEnabled(false);
			padButton[12].setEnabled(false);
			padButton[13].setEnabled(false);
			padButton[14].setEnabled(false);			
		}		
	}

	// action listener
	
	@Override
	public void actionPerformed(ActionEvent actev) {
		String s = parentPanel.saisie.getText().trim();
		String[] nb = s.split("\\s+");
		
		switch (actev.getActionCommand())  {
		case "compute":
			if ( !parentPanel.compute() )
				Toolkit.getDefaultToolkit().beep();
			else
			    close();
			break;
		case "delete":
			String newS = "";
			for ( int i = 0; i < nb.length - 1; i++ )
				newS = newS.concat(nb[i]).concat(" ");
			parentPanel.saisie.setText(newS);
			break;
		case "chiffre":
			String b = ((JButton) actev.getSource()).getText();
			int i = nb.length;
			if ( i > 7 )  {
				Toolkit.getDefaultToolkit().beep();
			} else if ( i == 7 ){
				if ( nb[6].length() < 3)
					parentPanel.saisie.setText(s + b);
				else
					Toolkit.getDefaultToolkit().beep();					
			} else {
				parentPanel.saisie.setText(s + " " + b);
			}
			break;
		}
		enabledButton();
	}

}
