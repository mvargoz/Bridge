package bridgeIHM;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

public class ActionBridge {
	
	//	debug
	
	public static class debugAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public debugAction()
		{
			super("debug");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((BridgePanel) winApp.ContexteGlobal.frame.panel).allVisible = !((BridgePanel) winApp.ContexteGlobal.frame.panel).allVisible;
			((BridgePanel) winApp.ContexteGlobal.frame.panel).visibiliteBoard();
			((BridgePanel) winApp.ContexteGlobal.frame.panel).sizeBoard();
		}
	}
	
	//  new
	
	public static class newAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public newAction()
		{
			super("new");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((BridgePanel) winApp.ContexteGlobal.frame.panel).reInit();
		}
	}

	//  open
	
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
			((BridgePanel) winApp.ContexteGlobal.frame.panel).open();
		}
	}

	//  save
		
	public static class saveAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public saveAction()
		{
			super("save");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((BridgePanel) winApp.ContexteGlobal.frame.panel).save();
		}
	}

	//	game
	
	public static class gameAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public gameAction()
		{
			super("game");
		}

		public void actionPerformed(ActionEvent e)
		{
			((BridgePanel) winApp.ContexteGlobal.frame.panel).initPanelJeu();
			setEnabled(false);
			winApp.ContexteGlobal.frame.getAction("new").setEnabled(true);
			winApp.ContexteGlobal.frame.getAction("open").setEnabled(true);
			winApp.ContexteGlobal.frame.getAction("save").setEnabled(true);
			winApp.ContexteGlobal.frame.getAction("debug").setEnabled(true);
			winApp.ContexteGlobal.frame.getAction("endgame").setEnabled(true);
		}
	}

	//	end game
	
	public static class endgameAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public endgameAction()
		{
			super("endgame");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((BridgePanel) winApp.ContexteGlobal.frame.panel).finJeu();
		}
	}

	//	undo
	
	public static class undoAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public undoAction()
		{
			super("undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e)
		{
			((BridgePanel) winApp.ContexteGlobal.frame.panel).undo();
		}
	}

	//	problem mode
	
	public static class problemAction extends AbstractAction
	{
		private static final long serialVersionUID = 1L;

		public problemAction()
		{
			super("problem");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e)
		{
			((BridgePanel) winApp.ContexteGlobal.frame.panel).problem();
		}
	}
	
	
}
