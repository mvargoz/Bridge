package sudoku;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class startAction extends AbstractAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public startAction()
	{
		super("start");
		setEnabled(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		((SudokuPanel) winApp.ContexteGlobal.frame.panel).resolution();
	}

}
