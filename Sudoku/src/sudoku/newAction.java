package sudoku;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class newAction extends AbstractAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public newAction()
	{
		super("new");
		setEnabled(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		((SudokuPanel) winApp.ContexteGlobal.frame.panel).initJeu();
	}
}
