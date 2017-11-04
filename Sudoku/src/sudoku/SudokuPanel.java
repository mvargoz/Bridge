package sudoku;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class SudokuPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static String[] chiffres = { "", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
	Sudoku sudoku;
	JComboBox caseGrille[][] = new JComboBox[9][9];
	JLabel caseGrilleSolve[][] = new JLabel[9][9];
	boolean auto = false;

	// constructeur
	
	public SudokuPanel()
	{
		sudoku = new Sudoku();
		sudoku.charge(sudokuTest.sdiab1);
		afficheGrille();
		chargeGrille(sudoku);
	}

	// construction de la grille pour saisie
	
	private void afficheGrille()
	{
		removeAll();
		Font font = new Font("Times",Font.BOLD, 24);
		setLayout(new GridLayout(3,3));
		for( int i=0; i<9; i++ )
		{
			JPanel panel = new JPanel(new GridLayout(3,3));
			panel.setBorder(BorderFactory.createEtchedBorder());
			for( int j=0; j<9; j++ )
			{
				int lig = Sudoku.iBloc(i,j);
				int col = Sudoku.jBloc(i,j);
				JComboBox cb = new JComboBox(chiffres);
				cb.setFont(font);
				cb.setMaximumRowCount(10);
				cb.setActionCommand(Integer.toString(lig)+ Integer.toString(col));
				cb.addActionListener(this);
				panel.add(cb);
				caseGrille[lig][col] = cb;
			}
			add(panel);
		}	
	}
	
	// affichage des valeurs de la grille
	
	private void chargeGrille( Sudoku s )
	{
		auto = true;
		for( int lig=0; lig<9; lig++ )
		{
			for( int col=0; col<9; col++ )
			{
				JComboBox cb = caseGrille[lig][col];
				cb.setSelectedIndex(s.grille[lig][col]);
				if ( s.grilleInit[lig][col] )
				{
					cb.setEnabled(false);
					cb.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
				}
				else
				{
					cb.setEnabled(true);
					cb.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));					
				}
			}
		}
		auto = false;	
	}

	// construction de la grille pour solution
	
	private void afficheGrilleSolve()
	{
		removeAll();
		setLayout(new GridLayout(3,3));
		for( int i=0; i<9; i++ )
		{
			JPanel panel = new JPanel(new GridLayout(3,3));
			panel.setBorder(BorderFactory.createEtchedBorder());
			for( int j=0; j<9; j++ )
			{
				JLabel cb = new JLabel();
				cb.setBorder(BorderFactory.createLineBorder(Color.black));
				cb.setHorizontalAlignment(SwingConstants.CENTER);
				panel.add(cb);
				caseGrilleSolve[Sudoku.iBloc(i,j)][Sudoku.jBloc(i,j)] = cb;
			}
			add(panel);
		}	
	}
	
	// affichage des valeurs de la grille pour solution
	
	private void chargeGrilleSolve( Sudoku s )
	{
		Font fontCase = new Font("Times",Font.BOLD, 24);
		Font fontIndice = new Font("Times",Font.BOLD, 12);
		for( int lig=0; lig<9; lig++ )
		{
			for( int col=0; col<9; col++ )
			{
				JLabel cb = caseGrilleSolve[lig][col];
				int v = s.grille[lig][col];
				if ( v == 0 )
				{
					cb.setFont(fontIndice);
					String ind = "";
					for ( int k=0; k<9; k++ )
					{
						if ( s.valeursPossibles[lig][col][k] )
							ind +=  Integer.toString(k+1) + " ";
					}
					cb.setText(ind);
				}
				else
				{
					if ( s.grilleInit[lig][col] )
						cb.setBackground(Color.blue);
					cb.setFont(fontCase);
					cb.setText(Integer.toString(v));
				}
			}
		}
	}

	// introduction d'un chiffre dans une case
	
	public void actionPerformed(ActionEvent e)
	{
		if ( auto ) return;
		JComboBox jb = (JComboBox)e.getSource();
		String coord = e.getActionCommand();
		int lig = Integer.parseInt(coord.substring(0,1)) + 1;
		int col = Integer.parseInt(coord.substring(1)) + 1;
		String valeur = (String)jb.getSelectedItem();
		int val = 0;
		if ( valeur.length() > 0 )
			val = Integer.parseInt(valeur);
		if ( !sudoku.putData(lig, col, val, false) )
		{
			String mess = winApp.ContexteGlobal.getResourceString("messInterdit");
			JOptionPane.showMessageDialog(null, valeur, mess, JOptionPane.WARNING_MESSAGE);
			jb.setSelectedIndex(-1);
		}
	} 

	// résolution automatique
	
	public void resolution()
	{
		afficheGrilleSolve();
		chargeGrilleSolve( sudoku );
		int r = sudoku.solveAll(true);
		chargeGrilleSolve( sudoku );
		JOptionPane.showMessageDialog(null, messExec(r),
				winApp.ContexteGlobal.getResourceString("messSol"),
				JOptionPane.WARNING_MESSAGE);
		winApp.ContexteGlobal.frame.setMessage(sudoku.mess);
	}

	// résolution automatique
	
	public void resolutionStep()
	{
		afficheGrilleSolve();
		chargeGrilleSolve( sudoku );
		int r = sudoku.solve(true);
		chargeGrilleSolve( sudoku );
		if ( r != 1 )
		{
			JOptionPane.showMessageDialog(null, messExec(r),
					winApp.ContexteGlobal.getResourceString("messSol"),
					JOptionPane.WARNING_MESSAGE);
		}
		winApp.ContexteGlobal.frame.setMessage(sudoku.mess);
	}

	// nouveau jeu
	
	public void initJeu()
	{
		afficheGrille();
		sudoku = new Sudoku();
		String mess = "";
		int n = 0;
		do { n = sudoku.create(18);} while ( n < 0 );
		mess = winApp.ContexteGlobal.getResourceString("messCreat");
		chargeGrille(sudoku);
		winApp.ContexteGlobal.frame.setMessage(mess);
	}

	private String messExec(int r)
	{
		String mess = "";
		if ( r == 0 )
			mess = winApp.ContexteGlobal.getResourceString("messUnique");
		else if ( r == -1 )
			mess = winApp.ContexteGlobal.getResourceString("messAucune");
		else if ( r == -2 )
			mess = winApp.ContexteGlobal.getResourceString("messMult");
		else
			mess = winApp.ContexteGlobal.getResourceString("messErr");
		return mess;		
	}
}
