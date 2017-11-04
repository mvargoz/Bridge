package sudoku;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class debugAction extends AbstractAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public debugAction()
	{
		super("debug");
		setEnabled(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		((SudokuPanel) winApp.ContexteGlobal.frame.panel).resolutionStep();
	}

}