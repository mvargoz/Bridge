package playlist;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import winApp.ContexteGlobal;

public class ActionPlaylist {

	// about

	public static class aboutAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public aboutAction() {
			super("about");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			JOptionPane.showMessageDialog(ContexteGlobal.frame, System.getProperty("os.name"),
					ContexteGlobal.getResourceString("aboutLabel"), JOptionPane.INFORMATION_MESSAGE);

		}
	}

	// options

	public static class optionAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public optionAction() {
			super("option");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).selectDirTanda();
		}
	}

	// refresh

	public static class refreshAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public refreshAction() {
			super("refresh");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).refresh();
		}
	}

	// new

	public static class newAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public newAction() {
			super("new");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).newMilongaModel();
		}
	}

	// open

	public static class openAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public openAction() {
			super("open");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).openMilongaModel();
		}
	}

	// save

	public static class saveAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public saveAction() {
			super("save");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).saveMilongaModel();
		}
	}

	// delete

	public static class deleteAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public deleteAction() {
			super("delete");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).deleteMilongaModel();
		}
	}

	// translation pc to mac playlist

	public static class pc2macAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public pc2macAction() {
			super("pc2mac");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).pc2mac();
		}
	}

	// call player

	public static class playerAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public playerAction() {
			super("player");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).playerOpen();
		}
	}

	// make a milonga

	public static class makeAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public makeAction() {
			super("make");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			((PlaylistPanel) winApp.ContexteGlobal.frame.panel).makeMilongaPlaylist();
		}
	}}
