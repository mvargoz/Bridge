package synchro;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ActionSynchro {
	
	// base directory application
	
	public static class baseDirAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public baseDirAction()
		{
			super("baseDir");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e)
		{
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).baseDir();
		}
	}
	
	// open parameter file
	
	public static class openAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public openAction()
		{
			super("open");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).open();
		}
	}

	// update parameter file
	
	public static class updateAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public updateAction()
		{
			super("update");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).updateParam.open();
		}
	}

	//	assemble parameter files
	
	public static class assembleAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public assembleAction()
		{
			super("assemble");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).assembleParam.open();
		}
	}

	//  verify synchro
	
	public static class verifyAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public verifyAction()
		{
			super("verify");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).synchroDir(1);
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).synchroButton.setEnabled(true);
			winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(true);

		}
	}

	//  synchronization
	
	public static class synchroAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public synchroAction()
		{
			super("synchro");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).synchroDir(2);
			((SynchroPanel) winApp.ContexteGlobal.frame.panel).synchroButton.setEnabled(false);
			winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(false);

		}
	}
}
