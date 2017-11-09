package playlist;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import javafx.application.Platform;

import winApp.ContexteGlobal;
import winApp.GenericFileFilter;

public class PlaylistPanel extends JPanel implements ActionListener, PropertyChangeListener {
	private static final long serialVersionUID = 1L;

	// directories

	protected static String baseDir = ContexteGlobal.getResourceString("baseDir");
	protected static String playlistDir = baseDir + "/" + ContexteGlobal.getResourceString("playlistDir");
	protected static String tandaImageDir = baseDir + "/" + ContexteGlobal.getResourceString("imageDir");
	protected static String modelDir = ContexteGlobal.getResourceString("modelDir");
	protected static String system = System.getProperty("os.name");

	// extensions

	protected static final String playlistExt = "." + ContexteGlobal.getResourceString("playlistExt");
	private static final String modelExt = "." + ContexteGlobal.getResourceString("saveExt");

	// constants

	private static final Dimension dimNameComboBox = new Dimension(200, 25);
	private static final Dimension dimNbComboBox = new Dimension(50, 25);
	private static final Dimension dimButton = new Dimension(25, 25);
	private static final Dimension dimEditor = new Dimension(300, 680);
	private static final int widthRow = 280;
	private static final int visibleRow = 35;
	private static final String modelDescr = ContexteGlobal.getResourceString("saveMess");
	private static final String playlistMilonga = ContexteGlobal.getResourceString("playlistMilonga");
	private static final String playlistValse = ContexteGlobal.getResourceString("playlistValse");
	private static final String playlistTango = ContexteGlobal.getResourceString("playlistTango");
	private static final String playlistCortina = ContexteGlobal.getResourceString("playlistCortina");
	private static final String playlistCumparsita = ContexteGlobal.getResourceString("playlistCumparsita");
	private static final Color colorError = Color.RED;
	private static final Color bckColorError = Color.WHITE;
	private static final Color colorMaestro = Color.BLACK;
	private static final Color bckColorMaestro = Color.YELLOW;

	// toolbar

	private JProgressBar progressBar = new JProgressBar(0, 100);

	private JPanel listModelPanel = new JPanel();
	private JLabel listModelLabel = new JLabel(ContexteGlobal.getResourceString("titleModel"));
	private Vector<String> listFileKey = new Vector<String>();
	private JComboBox<String> listFile = new JComboBox<String>(listFileKey);

	private JPanel nbTangoPanel = new JPanel();
	private JLabel nbTangoLabel = new JLabel(ContexteGlobal.getResourceString("titleTangosTanda"));
	private Integer[] nbTango = { 2, 3, 4 };
	private JComboBox<Integer> nbTangoChoix = new JComboBox<Integer>(nbTango);

	private JPanel cortinaPanel = new JPanel();
	private JLabel cortinaLabel = new JLabel(ContexteGlobal.getResourceString("titleCortina"));
	private Vector<String> listCortina = new Vector<String>();
	private JComboBox<String> cortinaChoix = new JComboBox<String>(listCortina);

	private JPanel cumparsitaPanel = new JPanel();
	private JLabel cumparsitaLabel = new JLabel(ContexteGlobal.getResourceString("titleCumparsita"));
	private Vector<String> listCumparsita = new Vector<String>();
	private JComboBox<String> cumparsitaChoix = new JComboBox<String>(listCumparsita);

	// model edit

	private JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
	private JTextPane milongaModel = new JTextPane();
	private MyUndoableEditListener undoableEditListener = new MyUndoableEditListener();
	private JScrollPane milongaModelScrollPane = new JScrollPane(milongaModel);

	// undo helpers

	public UndoAction undoAction = new UndoAction();
	public RedoAction redoAction = new RedoAction();
	private UndoManager undoManager = new UndoManager();

	// insert text position

	private int posInsertMilongaModel;

	// tandas

	private DefaultListModel<String> tandasMilonga = new DefaultListModel<String>();
	private JList<String> tandasMilongaList = new JList<String>(tandasMilonga);
	private JScrollPane tandasMilongaListScrollPane = new JScrollPane(tandasMilongaList);

	private DefaultListModel<String> tandasValse = new DefaultListModel<String>();
	private JList<String> tandasValseList = new JList<String>(tandasValse);
	private JScrollPane tandasValseListScrollPane = new JScrollPane(tandasValseList);

	private DefaultListModel<String> tandasTango = new DefaultListModel<String>();
	private JList<String> tandasTangoList = new JList<String>(tandasTango);
	private JScrollPane tandasTangoListScrollPane = new JScrollPane(tandasTangoList);
	private JPopupMenu popupTandaMusic = new JPopupMenu();

	// data

	private PlaylistMilonga bal = null;
	private TangoPlayer tangoPlayer = null;
	private TangoPlayer tangoPlayerMini = null;
	private DialogEditTanda editTanda = null;
	private PlaylistPc2mac playlistPc2mac;

	/**
	 *   constructor
	 */
	public PlaylistPanel() {
		winApp.ContexteGlobal.frame.getAction("save").setEnabled(true);
		winApp.ContexteGlobal.frame.getAction("delete").setEnabled(true);

		// initialisation des listes

		if (baseDir == null)  {
			baseDir = ".";
			playlistDir = baseDir + "/" + ContexteGlobal.getResourceString("playlistDir");
			tandaImageDir = baseDir + "/" + ContexteGlobal.getResourceString("imageDir");
		}
		if (modelDir == null)
			modelDir = ".";

		// Panel

		setLayout(new BorderLayout(5, 5));

		// ToolBar Actions

		winApp.ContexteGlobal.frame.toolBar.add(progressBar);
		winApp.ContexteGlobal.frame.toolBar.addSeparator();

		listModelPanel.setLayout(new BoxLayout(listModelPanel, BoxLayout.Y_AXIS));
		listModelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		listModelPanel.add(listModelLabel);
		listModelPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		listFile.setAlignmentX(Component.CENTER_ALIGNMENT);
		listModelPanel.add(listFile);
		winApp.ContexteGlobal.frame.toolBar.add(listModelPanel);
		winApp.ContexteGlobal.frame.toolBar.addSeparator();

		nbTangoPanel.setLayout(new BoxLayout(nbTangoPanel, BoxLayout.Y_AXIS));
		nbTangoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		nbTangoPanel.add(nbTangoLabel);
		nbTangoPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		nbTangoChoix.setAlignmentX(Component.CENTER_ALIGNMENT);
		nbTangoPanel.add(nbTangoChoix);
		winApp.ContexteGlobal.frame.toolBar.add(nbTangoPanel);
		winApp.ContexteGlobal.frame.toolBar.addSeparator();

		cortinaPanel.setLayout(new BoxLayout(cortinaPanel, BoxLayout.Y_AXIS));
		cortinaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		cortinaPanel.add(cortinaLabel);
		cortinaPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		cortinaChoix.setAlignmentX(Component.CENTER_ALIGNMENT);
		cortinaPanel.add(cortinaChoix);
		winApp.ContexteGlobal.frame.toolBar.add(cortinaPanel);
		winApp.ContexteGlobal.frame.toolBar.addSeparator();

		cumparsitaPanel.setLayout(new BoxLayout(cumparsitaPanel, BoxLayout.Y_AXIS));
		cumparsitaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		cumparsitaPanel.add(cumparsitaLabel);
		cumparsitaPanel.add(Box.createRigidArea(new Dimension(3, 3)));
		cumparsitaChoix.setAlignmentX(Component.CENTER_ALIGNMENT);
		cumparsitaPanel.add(cumparsitaChoix);
		winApp.ContexteGlobal.frame.toolBar.add(cumparsitaPanel);
		winApp.ContexteGlobal.frame.toolBar.addSeparator();

		listFile.setEditable(true);
		listFile.setMinimumSize(dimNameComboBox);
		
		nbTangoChoix.setMaximumSize(dimNbComboBox);
		nbTangoChoix.setSelectedItem(3);
		
		cortinaChoix.setMinimumSize(dimNameComboBox);
		
		cumparsitaChoix.setMinimumSize(dimNameComboBox);
		
		// edit model

		milongaModel.setPreferredSize(dimEditor);
		milongaModelScrollPane
				.setBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(modelDescr),
										BorderFactory.createEmptyBorder(5, 5, 5, 5)),
								milongaModelScrollPane.getBorder()));
		editPanel.add(milongaModelScrollPane);

		// tanda list

		tandasMilongaList.setFixedCellWidth(widthRow);
		tandasMilongaList.setVisibleRowCount(visibleRow);
		tandasMilongaList.setDragEnabled(true);
		tandasMilongaList.addMouseListener(new ListMouseListener());
		tandasMilongaListScrollPane
				.setBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(playlistMilonga),
										BorderFactory.createEmptyBorder(5, 5, 5, 5)),
								tandasMilongaListScrollPane.getBorder()));
		editPanel.add(tandasMilongaListScrollPane);

		tandasValseList.setFixedCellWidth(widthRow);
		tandasValseList.setVisibleRowCount(visibleRow);
		tandasValseList.setDragEnabled(true);
		tandasValseList.addMouseListener(new ListMouseListener());
		tandasValseListScrollPane
				.setBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(playlistValse),
										BorderFactory.createEmptyBorder(5, 5, 5, 5)),
								tandasValseListScrollPane.getBorder()));
		editPanel.add(tandasValseListScrollPane);

		tandasTangoList.setFixedCellWidth(widthRow);
		tandasTangoList.setVisibleRowCount(visibleRow);
		tandasTangoList.setDragEnabled(true);
		tandasTangoList.addMouseListener(new ListMouseListener());
		tandasTangoListScrollPane
				.setBorder(
						BorderFactory.createCompoundBorder(
								BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(playlistTango),
										BorderFactory.createEmptyBorder(5, 5, 5, 5)),
								tandasTangoListScrollPane.getBorder()));
		editPanel.add(tandasTangoListScrollPane);

		add(editPanel, BorderLayout.CENTER);

		// start listeners

		milongaModel.setCaretPosition(0);
		milongaModel.getDocument().addDocumentListener(new MyDocumentListener());
		milongaModel.getDocument().addUndoableEditListener(undoableEditListener);
		milongaModel.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
		milongaModel.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

		winApp.ContexteGlobal.frame.addActionMenu("undo", undoAction);
		winApp.ContexteGlobal.frame.addActionMenu("redo", redoAction);
		winApp.ContexteGlobal.frame.addActionToolBar("undo", undoAction);
		winApp.ContexteGlobal.frame.addActionToolBar("redo", redoAction);

		//  fill data & install listeners
		
		listFile.addActionListener(this);
		listFile.setActionCommand("selectModelMilonga");
		
		refresh();
		
//		cortinaChoix.addActionListener(this);
		cortinaChoix.setActionCommand("selectCortina");
		
		cumparsitaChoix.addActionListener(this);
		cumparsitaChoix.setActionCommand("selectCumparsita");

	}

	/**
	 *   panel refresh
	 */
	public void refresh() {
		listFile();
		if (!listFileKey.isEmpty())
			listFile.setSelectedIndex(0);
		listTandas();
		if (!listCortina.isEmpty())
			cortinaChoix.setSelectedIndex(0);
		if (!listCumparsita.isEmpty())
			cumparsitaChoix.setSelectedIndex(0);
		this.repaint();
	}

	/**
	 * Options
	 * select directories for
	 *  milonga model
	 *  tanda
	 *  image
	 *  music
	 */
	public void selectDirTanda() {
		new DialogOption(this);
	}

	/**
	 * Player
	 */
	public void playerOpen() {
		if (tangoPlayer == null)
			tangoPlayer = new TangoPlayer("");
		tangoPlayer.open();
	}

	/**
	 * New milonga model
	 */
	public void newMilongaModel() {
		listFile.setSelectedIndex(-1);
	}

	/**
	 * Choose a milonga model
	 * @return file
	 */
	public File openMilongaModel() {
		File fileModel = chooseFile();
		if (fileModel != null) {
			String fileName = fileModel.getName();
			listFile.setSelectedItem(fileName.substring(0, fileName.length() - 4));
		}
		return fileModel;
	}

	/**
	 * Save a milonga model
	 * @return file
	 */
	public File saveMilongaModel() {
		File modelFile = getModelFile();
		if (modelFile == null)
			return null;

		// controls

		Vector<Integer> errLig = syntaxControlMilongaModel();
		if (!errLig.isEmpty()) {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErreurSyntaxe"),
					ContexteGlobal.getResourceString("messSaveFileModel"), JOptionPane.ERROR_MESSAGE);
			setColTextMilongaModel(errLig, colorError, bckColorError);
			return null;
		}

		// write the file

		try {
			milongaModel.getEditorKit().write(new FileWriter(modelFile), milongaModel.getDocument(), 0,
					milongaModel.getDocument().getLength());

			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messSaveOK"),
					ContexteGlobal.getResourceString("messSaveFileModel"), JOptionPane.INFORMATION_MESSAGE);
			winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messSaveOK"));
			String fNameExt = modelFile.getName();
			String fName = fNameExt.substring(0, fNameExt.length() - 4);
			if (!listFileKey.contains(fName)) {
				listFile.setSelectedIndex(-1);
				listFileKey.add(fName);
				listFileKey.sort(null);
			}
			listFile.setSelectedItem(fName);
			return modelFile;

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErrSave"),
					ContexteGlobal.getResourceString("messSaveFileModel"), JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * 	delete milonga model
	 */
	public void deleteMilongaModel() {
		File modelFile = getModelFile();
		if (modelFile == null)
			return;
		if (modelFile != null && modelFile.exists()) {
			int okno = JOptionPane.showConfirmDialog(this, ContexteGlobal.getResourceString("messConfirm"),
					ContexteGlobal.getResourceString("messDeleteFileModel"), JOptionPane.YES_NO_OPTION);
			if (okno == JOptionPane.OK_OPTION) {
				try {
					Files.delete(modelFile.toPath());
					String fName = modelFile.getName();
					listFileKey.remove(fName.substring(0, fName.length() - 4));
					if (!listFileKey.isEmpty())
						listFile.setSelectedIndex(0);
					else
						listFile.setSelectedIndex(-1);
					winApp.ContexteGlobal.frame.setMessage("Mod�le supprim� : " + modelFile.getName());

				} catch (IOException ex) {
					JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErreur"),
							ContexteGlobal.getResourceString("messDeleteFileModel"), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * Control milonga model
	 * @return vector with list of lines in error
	 */
	private Vector<Integer> syntaxControlMilongaModel() {
		String[] text = milongaModel.getText().split("\n");
		Vector<Integer> linErr = new Vector<Integer>();
		int li = 0;
		for (String line : text) {
			line = line.trim();
			if (line.length() == 0) {
				li++;
				continue;
			}
			int iSpace = line.indexOf(' ');
			if (iSpace > 0) {
				String type = line.substring(0, iSpace);
				String name = line.substring(iSpace + 1);
				String[] nameMusic = name.split("/");
				for (String pl : nameMusic) {
					String nm = type + ' ' + pl;
					boolean okFind = false;
					if (type.equalsIgnoreCase("milonga")) {
						for (Enumeration<String> e = tandasMilonga.elements(); e.hasMoreElements();) {
							if (e.nextElement().startsWith(nm))
								okFind = true;
						}
					} else if (type.equalsIgnoreCase("vals")) {
						for (Enumeration<String> e = tandasValse.elements(); e.hasMoreElements();) {
							if (e.nextElement().startsWith(nm))
								okFind = true;
						}
					} else if (type.equalsIgnoreCase("tango")) {
						for (Enumeration<String> e = tandasTango.elements(); e.hasMoreElements();) {
							if (e.nextElement().startsWith(nm))
								okFind = true;
						}
					}
					if (!okFind) {
						linErr.add(li);
						linErr.add(line.length());
					}
				}
			} else {
				linErr.add(li);
				linErr.add(line.length());
			}
			li += line.length() + 1;
		}
		return linErr;
	}

	/**
	 * search milonga model
	 * @param genre (Tango, Vals, Milonga)
	 * @param indice generally name of maestro
	 */
	public void searchMilongaModel(String genre, String indice) {
		Vector<Integer> findText = findTandaMilongaModel(genre, indice);
		if (!findText.isEmpty()) {
			setColTextMilongaModel(findText, colorMaestro, bckColorMaestro);
		}
	}

	/**
	 * find lines containing genre & indice
	 * @param genre
	 * @param indice
	 * @return vector list of index & length
	 */
	private Vector<Integer> findTandaMilongaModel(String genre, String indice) {
		String[] text = milongaModel.getText().split("\n");
		Vector<Integer> posFind = new Vector<Integer>();
		int iFind;
		int li = 0;
		for (String line : text) {
			line = line.trim();
			String[] tkLine = line.split("\\s");
			if (tkLine[0].equalsIgnoreCase(genre)) {
				iFind = line.indexOf(indice);
				if (iFind >= 0) {
					posFind.add(li + iFind);
					posFind.add(indice.length());
				}
			}
			li += line.length() + 1;
		}
		return posFind;
	}

	/**
	 *  init edit area
	 */
	private void initMilongaModel() {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
		milongaModel.setText("");
		milongaModel.setCaretPosition(0);
		milongaModel.setCharacterAttributes(aset, true);
	}

	/**
	 * Show text in color: find or in error
	 * @param pos = vector list position, length
	 * @param color
	 * @param bckColor
	 */
	private void setColTextMilongaModel(Vector<Integer> pos, Color color, Color bckColor) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);
		aset = sc.addAttribute(aset, StyleConstants.Background, bckColor);
		Iterator<Integer> ipos = pos.iterator();
		while (ipos.hasNext()) {
			int deb = ipos.next();
			int lg = ipos.next();
			milongaModel.setCaretPosition(deb);
			milongaModel.moveCaretPosition(deb + lg);
			milongaModel.setCharacterAttributes(aset, true);
		}
		milongaModel.setCaretPosition(0);
	}

	// suppression des mises en evidence

	/**
	 *  suppress coloring in text
	 */
	private void setBlackMilongaModel() {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
		milongaModel.setCaretPosition(0);
		milongaModel.moveCaretPosition(milongaModel.getDocument().getLength());
		milongaModel.setCharacterAttributes(aset, true);
	}

	/**
	 * create the milonga playlist with the current model
	 */
	public void makeMilongaPlaylist() {
		if (bal == null)
			bal = new PlaylistMilonga();

		File modelFile = saveMilongaModel();
		if (modelFile == null)
			return;
		String nmModel = modelFile.getName();
		nmModel = nmModel.substring(0, nmModel.length() - 4);

		bal.getMilonga(nmModel, milongaModel.getText().split("\n"), (Integer) nbTangoChoix.getSelectedItem(),
				(String) cortinaChoix.getSelectedItem());

	}

	/**
	 * Milonga model list
	 */
	private void listFile() {
		listFileKey.clear();
		File dir = new File(modelDir);
		File[] listFileSource = dir.listFiles(new FileFilter() {
			public boolean accept(File dir) {
				return dir.isFile() && !dir.isHidden() && (dir.getName().endsWith(modelExt));
			}
		});
		for (File f : listFileSource) {
			String fName = f.getName();
			listFileKey.add(fName.substring(0, fName.length() - 4));
		}
	}

	/**
	 * choose a milonga model
	 * @return file
	 */
	public File chooseFile() {
		JFileChooser jFileChooserPlay;
		GenericFileFilter filter = new GenericFileFilter();
		filter.addExtension(ContexteGlobal.getResourceString("saveExt"));
		filter.setDescription(modelDescr);
		jFileChooserPlay = new JFileChooser(modelDir);
		jFileChooserPlay.setFileFilter(filter);
		int returnVal = jFileChooserPlay.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			return jFileChooserPlay.getSelectedFile();
		}
		return null;

	}

	/**
	 * current model file
	 * @return file
	 */
	private File getModelFile() {
		String fileName = (String) listFile.getSelectedItem();
		if (fileName == null) {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErreurMissModel"),
					ContexteGlobal.getResourceString("saveMess"), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if (!fileName.endsWith(modelExt))
			fileName += modelExt;
		return (new File(modelDir + "/" + fileName));
	}

	/**
	 * read a milonga model
	 * @param modelFile
	 */
	private void readModel(File modelFile) {
		initMilongaModel();
		try {
			milongaModel.getEditorKit().read(new FileReader(modelFile), milongaModel.getDocument(), 0);

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErrRead"),
					ContexteGlobal.getResourceString("messReadFileModel"), JOptionPane.ERROR_MESSAGE);
		}
		undoManager.discardAllEdits();
	}

	/**
	 * list of all prefabricated tandas
	 */
	public void listTandas() {
		File dir = new File(playlistDir);
		if (dir == null)
			return;

		// Milonga

		File[] listFile = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return (filename.startsWith(playlistMilonga) && filename.endsWith(playlistExt));
			}
		});
		if (listFile == null)
			return;
		for (File f : listFile) {
			String nm = f.getName();
			String nameTanda = nm.substring(0, nm.length() - 4);
			tandasMilonga.addElement(nameTanda);
		}

		// Vals

		listFile = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return (filename.startsWith(playlistValse) && filename.endsWith(playlistExt));
			}
		});
		if (listFile == null)
			return;
		for (File f : listFile) {
			String nm = f.getName();
			String nameTanda = nm.substring(0, nm.length() - 4);
			tandasValse.addElement(nameTanda);
		}

		// Tango

		listFile = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return (filename.startsWith(playlistTango) && filename.endsWith(playlistExt));
			}
		});
		if (listFile == null)
			return;
		for (File f : listFile) {
			String nm = f.getName();
			String nameTanda = nm.substring(0, nm.length() - 4);
			tandasTango.addElement(nameTanda);
		}

		// Cortina

		listFile = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return (filename.startsWith(playlistCortina) && filename.endsWith(playlistExt));
			}
		});
		if (listFile == null)
			return;
		for (File f : listFile) {
			String nm = f.getName();
			String nameTanda = nm.substring(0, nm.length() - 4);
			listCortina.addElement(nameTanda);
		}

		// Cumparsita

		listFile = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return (filename.startsWith(playlistCumparsita) && filename.endsWith(playlistExt));
			}
		});
		if (listFile == null)
			return;
		for (File f : listFile) {
			String nm = f.getName();
			String nameTanda = nm.substring(0, nm.length() - 4);
			listCumparsita.addElement(nameTanda);
		}
	}

	/** 
	 * implements Action Listener on combobox
	 */
	public void actionPerformed(ActionEvent e) {
		switch (e.getActionCommand())  {
		case "selectModelMilonga":
			// select model milonga file in combobox
			File modelFile = getModelFile();
			if (modelFile == null) {
				initMilongaModel();
				break;
			}
			if (modelFile.exists()) {
				readModel(modelFile);
			} else {
				initMilongaModel();
			}
			break;
			
		case "selectCortina":
			// select cortina file in combobox
			String cortina = (String) cortinaChoix.getSelectedItem();
			if (cortina == null) break;
			tandaOpen(cortina);			
			break;
			
		case "selectCumparsita":
			// select cumparsita file in combobox
			String cumparsita = (String) cumparsitaChoix.getSelectedItem();
			if (cumparsita == null) break;
			tandaOpen(cumparsita);						
			break;
		}

	}

	/**
	 * Document Listener
	 */
	class MyDocumentListener implements DocumentListener {
		
			//  drop
		public void insertUpdate(DocumentEvent e) {
			try {
				Document milongaModelDoc = (Document) e.getDocument();
				// drop ou paste
				if (e.getLength() > 1) {
					// si pas d�but ligne: ajout "/" et retrait type
					// le undo fonctionne mal
					posInsertMilongaModel = e.getOffset();
					if (!milongaModelDoc.getText(posInsertMilongaModel - 1, 1).equals("\n")) {
						SwingUtilities.invokeLater(new modifMilongaModel());
					}
				}
			} catch (Exception ex) {
			}
		}

		public void removeUpdate(DocumentEvent e) {
		}

		public void changedUpdate(DocumentEvent e) {
		}

	}

	/**
	 *	modif milonga model with drop
	 */
	protected class modifMilongaModel implements Runnable {
		public void run() {
			try {
				Document milongaModelDoc = milongaModel.getDocument();
				if (milongaModelDoc.getText(posInsertMilongaModel, 8).equalsIgnoreCase("milonga ")) {
					milongaModelDoc.remove(posInsertMilongaModel, 8);
					milongaModelDoc.insertString(posInsertMilongaModel, "/", null);
				} else if (milongaModelDoc.getText(posInsertMilongaModel, 5).equalsIgnoreCase("vals ")) {
					milongaModelDoc.remove(posInsertMilongaModel, 5);
					milongaModelDoc.insertString(posInsertMilongaModel, "/", null);
				} else if (milongaModelDoc.getText(posInsertMilongaModel, 6).equalsIgnoreCase("tango ")) {
					milongaModelDoc.remove(posInsertMilongaModel, 6);
					milongaModelDoc.insertString(posInsertMilongaModel, "/", null);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	};

	/**
	 * undo listener
	 */
	protected class MyUndoableEditListener implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
		}
	}

	/**
	 * undo
	 */
	public class UndoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public UndoAction() {
			super("Undo");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.undo();
			} catch (CannotUndoException ex) {
				JOptionPane.showMessageDialog(ContexteGlobal.frame, ContexteGlobal.getResourceString("messErrUndo"),
						ContexteGlobal.getResourceString("undoLabel"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	/**
	 * redo
	 */
	public class RedoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public RedoAction() {
			super("Redo");
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.redo();
			} catch (CannotRedoException ex) {
				JOptionPane.showMessageDialog(ContexteGlobal.frame, ContexteGlobal.getResourceString("messErrRedo"),
						ContexteGlobal.getResourceString("redoLabel"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 *  mouse listener pour les listes des tandas
	 */
	protected class ListMouseListener implements MouseListener, ActionListener {

		@Override
		public void mouseClicked(java.awt.event.MouseEvent e) {

			// show the same maestro with same genre in the model

			JList<String> list = (JList<String>) e.getComponent();
			int index = list.locationToIndex(e.getPoint());
			String tanda = list.getModel().getElementAt(index);
			String[] splitTanda = tanda.split("\\s");
			if (Arrays.binarySearch(TangoPlayer.tbGenre, splitTanda[0]) >= 0) {
				if (splitTanda.length > 1) {
					int ind = 1;
					if (Arrays.binarySearch(TangoPlayer.tbGenreCompl, splitTanda[1]) >= 0) {
						ind++;
					}
					if (splitTanda.length > ind) {
						setBlackMilongaModel();
						String[] maestroRech = splitTanda[ind].split("-");
						searchMilongaModel(splitTanda[0], maestroRech[0]);
					}
				}
			}

		}

		@Override
		public void mouseEntered(java.awt.event.MouseEvent e) {
		}

		@Override
		public void mouseExited(java.awt.event.MouseEvent e) {
		}

		// right clic detection or equivalent
		@Override
		public void mousePressed(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
				displayTanda(e);
			}
		}

		@Override
		public void mouseReleased(java.awt.event.MouseEvent e) {
			if (e.isPopupTrigger()) {
				displayTanda(e);
			}
		}

		/**
		 * a right clic display the menu with
		 * 			playlist create
		 * 			playlist update
		 * 			play musics of the playlist
		 * @param event
		 */
		private void displayTanda(java.awt.event.MouseEvent e) {
			popupTandaMusic.removeAll();
			JMenuItem menuItem;

			// selected tanda

			JList<String> list = (JList<String>) e.getComponent();
			int index = list.locationToIndex(e.getPoint());
			String tanda = list.getModel().getElementAt(index);

			// playlist menu 

			menuItem = new JMenuItem("Create");
			menuItem.addActionListener(this);
			popupTandaMusic.add(menuItem);
			menuItem.setActionCommand(tanda);

			menuItem = new JMenuItem("Edit");
			menuItem.addActionListener(this);
			popupTandaMusic.add(menuItem);
			menuItem.setActionCommand(tanda);

			popupTandaMusic.addSeparator();

			// musics

			BufferedReader in;
			String line;
			try {
				in = new BufferedReader(
						new InputStreamReader(new FileInputStream(playlistDir + "/" + tanda + playlistExt), "UTF8"));
				line = in.readLine();
				line = in.readLine();
				while (line != null) {
					line = line.trim();
					if (line.startsWith("#EXTINF:")) {
						String[] s = line.substring(8).split(",");
						menuItem = new JMenuItem("play " + s[1]);
						menuItem.addActionListener(this);
						popupTandaMusic.add(menuItem);
					} else {
						System.out.println("Manque #EXTINF:");
						return;
					}
					line = in.readLine();
					menuItem.setActionCommand(line.trim());
					line = in.readLine();
				}
				in.close();
			} catch (Exception ex) {
				System.out.println("Load playlist error : " + ex);
				ex.printStackTrace();
				return;
			}
			popupTandaMusic.show(e.getComponent(), e.getX(), e.getY());
		}

		/** 
		 *    action listener for this menu
		 */
		public void actionPerformed(ActionEvent e) {
			JMenuItem source = (JMenuItem) (e.getSource());
			switch (source.getText()) {
			case "Edit":
				tandaOpen(e.getActionCommand());
				break;
			case "Create":
				tandaOpen(null);
				break;
			default:
				if (tangoPlayerMini == null)
					tangoPlayerMini = new TangoPlayer("mini");
				else
					tangoPlayerMini.stop();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						tangoPlayerMini.open();
						tangoPlayerMini.playMusic(new File(e.getActionCommand()));
					}
				});
				break;
			}
		}
	}

	/**
	 * edit tanda
	 * @param tanda
	 */
	public void tandaOpen(String tanda) {
		if (editTanda == null)
			editTanda = new DialogEditTanda(this);
		editTanda.open(tanda);
	}

	/**
	 * convert playlist for mac
	 */
	public void pc2mac() {
		try {
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressBar.setIndeterminate(true);
			playlistPc2mac = new PlaylistPc2mac();
			playlistPc2mac.addPropertyChangeListener(this);
			playlistPc2mac.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Invoked when task pc2mac progress property changes
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		switch (evt.getPropertyName()) {
		case "progress":
			int progress = (Integer) evt.getNewValue();
			progressBar.setIndeterminate(false);
			progressBar.setValue(progress);
			break;
		case "state":
			// test de fin
			if (evt.getNewValue() == StateValue.DONE) {
				try {
					JOptionPane.showMessageDialog(ContexteGlobal.frame, playlistPc2mac.get(),
							ContexteGlobal.getResourceString("messConvFileTanda"), JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

}
