package winApp;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;
import java.net.URL;
import java.beans.*;

public class WinAppFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	// Constantes

	private static final String imageSuffix = "Image";
	private static final String labelSuffix = "Label";
	private static final String actionSuffix = "Action";
	private static final String tipSuffix = "Tooltip";
	
	// application graphique

	public String appliPackage = null;
	public JMenuBar menuBar = new JMenuBar();;
	public JToolBar toolBar = new JToolBar();
	public JPanel panel;
	private JComponent status = new StatusBar();
	private JLabel mesText = new JLabel("");

	//	tables
	
	private Hashtable<String, JMenuItem> menuItems;
	private Hashtable<Object, Action> commands;

	//	constructeur	

	public WinAppFrame() {
		ContexteGlobal.frame = this;
		appliPackage = ContexteGlobal.getResourceString("package");
		if ( appliPackage == null )
			appliPackage = "";
		else
			appliPackage += ".";
		try {
			addWindowListener(new AppCloser());
			setBackground(Color.lightGray);
			setTitle(ContexteGlobal.getResourceString("Title"));

			// menus

			buildTableActions();
			menuItems = new Hashtable<String, JMenuItem>();
			createMenubar();
			setJMenuBar(menuBar);

			// barre d'outils

			createToolbar();
			getContentPane().add("North", toolBar);

			// panel de l'application

			String appliPanel = ContexteGlobal.getResourceString("panel");
			try {
				Class<?> c = Class.forName(appliPackage + appliPanel);
				panel = (JPanel) c.newInstance();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
			getContentPane().add("Center", panel);

			// barre de status

			status.add(mesText);
			getContentPane().add("South", status);
			URL url = ContexteGlobal.getResource("appli" + imageSuffix);
			if ( url != null )
				setIconImage(Toolkit.getDefaultToolkit().getImage(url));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// status

	public void setMessage(String s) {
		mesText.setText(s);
	}

	//	ajout action menu
	
	public boolean addActionMenu(String searchName, Action action)  {
		JMenuItem menu = searchMenu(menuBar,searchName);
		if ( menu != null )  {
			menu.addActionListener(action);
			menu.setEnabled(true);
		}
		return true;
	}
	
	private JMenuItem searchMenu(JMenuBar menuBar, String searchName)  {
		MenuElement[] topLevelElements = menuBar.getSubElements();
		for(MenuElement menuElement: topLevelElements) {
            MenuElement[] subElements = menuElement.getSubElements();
            for(MenuElement subElement: subElements)  {
                if(subElement instanceof JPopupMenu)  {
                    for(MenuElement childMenuItem: subElement.getSubElements())  {
                        String name = ((JMenuItem) childMenuItem.getComponent()).getName();
                        if ( name != null && name.equalsIgnoreCase(searchName) )
                        	return (JMenuItem) childMenuItem.getComponent();
                    }
                }
            }
        }
		return null;
	}

	//	ajout action toolbar
	
	public boolean addActionToolBar(String searchName, Action action)  {
		JButton bt = searchToolBar(toolBar,searchName);
		if ( bt != null )  {
			bt.addActionListener(action);
			bt.setEnabled(true);
		}
		return true;
	}
	
	private JButton searchToolBar(JToolBar tooBar, String searchName)  {		
		for( int i=0; i < toolBar.getComponentCount(); i++ )   {
			Component c = toolBar.getComponent(i);
            if ( c instanceof JButton ) {
               String name = ((JButton) c).getName();
               if ( name != null && name.equalsIgnoreCase(searchName) )
                	return (JButton) c;
            }
        }
		return null;
	}
	
	// création de la barre de menu à partir du fichier de ressource

	private void createMenubar() {
		String menuBarDescr = ContexteGlobal.getResourceString("menubar");
		if (menuBarDescr != null)  {
			String[] menuKeys = menuBarDescr.split("\\s+");
			for (int i = 0; i < menuKeys.length; i++) {
				JMenu m = createMenu(menuKeys[i]);
				if (m != null) {
					menuBar.add(m);
				}
			}
		}
	}

	// création de chaque sous-menu

	private JMenu createMenu(String key) {
		String[] itemKeys = ContexteGlobal.getResourceString(key).split("\\s+");
		JMenu menu = new JMenu(ContexteGlobal.getResourceString(key + "Label"));
		for (int i = 0; i < itemKeys.length; i++) {
			if (itemKeys[i].equals("-")) {
				menu.addSeparator();
			} else {
				JMenuItem mi = createMenuItem(itemKeys[i]);
				menu.add(mi);
			}
		}
		return menu;
	}

	// création de chaque item du menu

	private JMenuItem createMenuItem(String cmd) {
		JMenuItem mi = new JMenuItem();
		mi.setName(cmd);
			// action
		String astr = ContexteGlobal.getResourceString(cmd + actionSuffix);
		if (astr == null) {
			astr = cmd;
		}
		mi.setActionCommand(astr);
		Action a = getAction(astr);
		if (a != null) {
			mi.addActionListener(a);
			a.addPropertyChangeListener(createActionChangeListener(mi));
			mi.setEnabled(a.isEnabled());
		} else {
			mi.setEnabled(false);
		}
			// icone
		URL url = ContexteGlobal.getResource(cmd + imageSuffix);
		if (url != null) {
			mi.setHorizontalTextPosition(JButton.RIGHT);
			mi.setIcon(new ImageIcon(url));
		}
			// label
		mi.setText(ContexteGlobal.getResourceString(cmd + labelSuffix));

		menuItems.put(cmd, mi);

		return mi;
	}

	//	actions possibles
	
	private void buildTableActions() {
		commands = new Hashtable<Object, Action>();
		Class<?> c;
		Action a;
			//	actions définies pour l'application
		String actionClass = ContexteGlobal.getResourceString("actionClass");
		String[] classActions = ContexteGlobal.getResourceString("classeActions").split("\\s+");
		try {
			for ( String action : classActions) {
				String prefixClass = appliPackage;
				if ( actionClass != null )  {
					prefixClass += actionClass + "$";
				}
				c = Class.forName(prefixClass + action);
				a = (Action) c.newInstance();
				commands.put(a.getValue(Action.NAME), a);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
			//	actions préprogrammées
		a = new ExitAction();
		commands.put(a.getValue(Action.NAME), a);
		a = new LogAction();
		commands.put(a.getValue(Action.NAME), a);
		commands.put("cut", new DefaultEditorKit.CutAction());
		commands.put("copy", new DefaultEditorKit.CopyAction());
		commands.put("paste", new DefaultEditorKit.PasteAction());
		JTextPane textarea = new JTextPane();
		commands.put("selectAll", textarea.getActionMap().get(DefaultEditorKit.selectAllAction));
	}

	/**
	 * Create the toolbar. By default this reads the resource file for the
	 * definition of the toolbar.
	 */

	private void createToolbar() {
		String toolBarDescr = ContexteGlobal.getResourceString("toolbar");
		if (toolBarDescr != null)	{
			String[] toolKeys = toolBarDescr.split("\\s+");
			for (int i = 0; i < toolKeys.length; i++) {
				if (toolKeys[i].equals("-")) {
					toolBar.addSeparator();
				} else {
					toolBar.add(createTool(toolKeys[i]));
				}
			}
			toolBar.add(Box.createHorizontalGlue());
		}
	}

	/**
	 * Hook through which every toolbar item is created.
	 */
	private Component createTool(String key) {
		return createToolbarButton(key);
	}

	/**
	 * Create a button to go inside of the toolbar. By default this will load an
	 * image resource. The image filename is relative to the classpath
	 * (including the '.' directory if its a part of the classpath), and may
	 * either be in a JAR file or a separate file.
	 * 
	 * @param key
	 *            The key in the resource file to serve as the basis of lookups.
	 */

	private JButton createToolbarButton(String key) {
		URL url = ContexteGlobal.getResource(key + imageSuffix);
		JButton b = new JButton(new ImageIcon(url)) {
			private static final long serialVersionUID = 1L;
			public float getAlignmentY() {
				return 0.5f;
			}
		};
		b.setRequestFocusEnabled(false);
		b.setMargin(new Insets(1, 1, 1, 1));
		b.setName(key);
			// actions
		String astr = ContexteGlobal.getResourceString(key + actionSuffix);
		if (astr == null) {
			astr = key;
		}
		Action a = getAction(astr);
		if (a != null) {
			b.setActionCommand(astr);
			b.addActionListener(a);
			a.addPropertyChangeListener(createActionChangeListener(b));
			b.setEnabled(a.isEnabled());
		} else {
			b.setEnabled(false);
		}
			// tips
		String tip = ContexteGlobal.getResourceString(key + tipSuffix);
		if (tip != null) {
			b.setToolTipText(tip);
		}
		return b;
	}

	public final Action getAction(String cmd) {
		return (Action) commands.get(cmd);
	}

	// listener pour les actions menus

	private PropertyChangeListener createActionChangeListener(JMenuItem b) {
		return new ActionChangedListener(b);
	}

	private class ActionChangedListener implements PropertyChangeListener {
		JMenuItem menuItem;

		ActionChangedListener(JMenuItem mi) {
			super();
			this.menuItem = mi;
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (e.getPropertyName().equals(Action.NAME)) {
				String text = (String) e.getNewValue();
				menuItem.setText(text);
			} else if (propertyName.equals("enabled")) {
				Boolean enabledState = (Boolean) e.getNewValue();
				menuItem.setEnabled(enabledState.booleanValue());
			}
		}
	}

	// listener pour les actions toolbar

	private PropertyChangeListener createActionChangeListener(JButton b) {
		return new ActionChangedListenerTool(b);
	}

	private class ActionChangedListenerTool implements PropertyChangeListener {
		JButton toolItem;

		ActionChangedListenerTool(JButton mi) {
			super();
			this.toolItem = mi;
		}

		public void propertyChange(PropertyChangeEvent e) {
			String propertyName = e.getPropertyName();
			if (e.getPropertyName().equals(Action.NAME)) {
				String text = (String) e.getNewValue();
				toolItem.setText(text);
			} else if (propertyName.equals("enabled")) {
				Boolean enabledState = (Boolean) e.getNewValue();
				toolItem.setEnabled(enabledState.booleanValue());
			}
		}
	}

	// status bar

	private class StatusBar extends JComponent {
		private static final long serialVersionUID = 1L;

		public StatusBar() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
	}

	// sortie

	private static final class AppCloser extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			ContexteGlobal.saveProperties();
			System.exit(0);
		}
	}

	// actions par défaut des menus

	private class LogAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public LogAction() {
			super("log");
		}

		public void actionPerformed(ActionEvent e) {
			ContexteGlobal.dialogSystemOut.open();
		}
	}

	private class ExitAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public ExitAction() {
			super("exit");
		}

		public void actionPerformed(ActionEvent e) {
			ContexteGlobal.saveProperties();
			System.exit(0);
		}
	}

}
