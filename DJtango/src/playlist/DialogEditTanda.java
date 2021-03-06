package playlist;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import winApp.ContexteGlobal;

/*
 * 		�dition des tandas
*/

public class DialogEditTanda extends JDialog implements WindowListener, ActionListener {
	private static final long serialVersionUID = 1L;

		//	constantes
	
	private static final String playlistDir = PlaylistPanel.playlistDir;;
	private static final String playlistExt = PlaylistPanel.playlistExt;
	private static final String titreTanda = ContexteGlobal.getResourceString("titreTanda");
	private static final int widthTable = 800;
	private static final int heightTable = 300;
	private static final Dimension dimNameField = new Dimension(300, 25);

		//	frame
	
	private Frame parentFrame;
	private PlaylistPanel parentPanel;

		//	panel avec BorderLayout hgap vgap
	
	private JPanel panel = new JPanel(new BorderLayout(10,20));
	
		//  nom de tanda avec aide
	
	private JPanel panelName = new JPanel(new FlowLayout(FlowLayout.CENTER,40,20));
	private JTextField nameTanda = new JTextField();
	private JButton helpButton = new JButton(new ImageIcon(ContexteGlobal.getResource("helpImage")));

		// 	Tri des musiques d'une tanda
	
	private JRadioButton sortButton = new JRadioButton("Tri des musiques");
	
		// 	Liste des musiques d'une tanda

    private String[] columnNames = {"Titre", "Artiste", "Ann�e", "Dur�e", "Genre", ""};
    private int[] columnWidth = {250, 300, 40, 40, 70, 0};
	private TableModel tandasMusicTableModel = new DefaultTableModel(columnNames,0);
    private JTable tandasMusicTable = new JTable(tandasMusicTableModel);
	private JScrollPane tandasMusicTableScrollPane = new JScrollPane(tandasMusicTable);
	private TableRowSorter<TableModel> tandasMusicTableSorter;
	
		//	Boutons action sur tanda
	
	private JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER,20,20));
	private JButton deleteButton = new JButton(ContexteGlobal.getResourceString("deleteTanda"));
	private JButton saveButton = new JButton(ContexteGlobal.getResourceString("saveTanda"));
	
		//	Boutons action sur musiques

	private JPanel panelSideButton = new JPanel(new GridLayout(3,1));
	private JButton deleteMusicButton = new JButton(new ImageIcon(ContexteGlobal.getResource("delImage")));
	private JButton moveUpButton = new JButton(new ImageIcon(ContexteGlobal.getResource("upImage")));
	private JButton moveDownButton = new JButton(new ImageIcon(ContexteGlobal.getResource("downImage")));

		//  tanda en �dition
	
	private String tanda = null;
	
	/**
	 * action listener
	 */
	@Override
	public void actionPerformed(ActionEvent actev) {
		int[] rows = tandasMusicTable.getSelectedRows();
		for ( int i = 0; i < rows.length; i++ )  {
			rows[i] = tandasMusicTable.convertRowIndexToModel(rows[i]);
		}
		DefaultTableModel tableModel = (DefaultTableModel) tandasMusicTable.getModel();

		switch ( actev.getActionCommand() )  {
		case "upMusic":
			Arrays.sort(rows);
			for ( int row : rows )  {
				tandasMusicTable.removeRowSelectionInterval(row, row);
			}
			for ( int row : rows )  {
				if ( row > 0 )  {
					tableModel.moveRow(row, row, --row);
					tandasMusicTable.addRowSelectionInterval(row, row);
				}
			}
			break;
		case "downMusic":
			Arrays.sort(rows);
			for ( int row : rows )  {
				tandasMusicTable.removeRowSelectionInterval(row, row);
			}			
			for ( int i = rows.length - 1; i >= 0; i-- )  {
				int row = rows[i];
				if ( row < tableModel.getRowCount() - 1 )  {
					tableModel.moveRow(row, row, ++row);
					tandasMusicTable.addRowSelectionInterval(row, row);
				}
			}
			break;
		case "delMusic":
			Arrays.sort(rows);
			for ( int i = rows.length - 1; i >= 0; i-- )  {
				int row = rows[i];
				tandasMusicTable.removeRowSelectionInterval(row,row);
				tableModel.removeRow(row);
			}
			break;			
		case "delete":
			deleteTanda(nameTanda.getText());
			dispose();				
			break;			
		case "save":
			writeTanda(nameTanda.getText());
			dispose();				
			break;			
		case "help":
			String out = "";
			int i = 1;
			String outl = ContexteGlobal.getResourceString("nameTandaSyntaxL" + i++);
			while ( outl != null ) {
				out += outl + "\n";
				outl = ContexteGlobal.getResourceString("nameTandaSyntaxL" + i++);
			}
			JOptionPane.showMessageDialog(this, out,
					ContexteGlobal.getResourceString("messNameTanda"), JOptionPane.INFORMATION_MESSAGE);
			break;			
		case "sort":
			if ( sortButton.isSelected() )  {
				moveUpButton.setEnabled(false);
				moveDownButton.setEnabled(false);
					// 	tri musiques sur ann�e-titre
				List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
				sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
				sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
				tandasMusicTableSorter = new TableRowSorter<TableModel>(tandasMusicTableModel);
				tandasMusicTableSorter.setSortKeys(sortKeys);
				tandasMusicTable.setRowSorter(tandasMusicTableSorter);				
			} else {
				tandasMusicTable.setRowSorter(null);								
				moveUpButton.setEnabled(true);
				moveDownButton.setEnabled(true);
			}
			break;			
		}

	}
	  
	/**
	 * contructeur
	 * @param pp
	 */
	public DialogEditTanda(PlaylistPanel pp)	{
		super(winApp.ContexteGlobal.frame, titreTanda, false);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
			//  position
		
		parentFrame = winApp.ContexteGlobal.frame;
		parentPanel = pp;
		Point parentPoint = pp.getLocationOnScreen();
		parentPoint.x += 50;
		setLocation(parentPoint);
		
			//  contr�le du nom de la tanda
		

		nameTanda.setPreferredSize(dimNameField);
		nameTanda.setToolTipText(ContexteGlobal.getResourceString("formatNomTanda"));
		nameTanda.getDocument().addDocumentListener(new DocumentListener() {
	
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				verifTandaName();			
			}
	
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				verifTandaName();			
			}
	
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				verifTandaName();			
			}
		});

			//  custumization table

		tandasMusicTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                if ( column == 5 ) {
                	return null;
                } else if ( column == 3 && value instanceof Integer) {
        		    setText((int) value / 60 + "'" + (int)value % 60);
                } else
                	return super.getTableCellRendererComponent(table, value,
                             isSelected,  hasFocus,  row,  column);  
                return this;
            }
        });

		// Drag & drop
		
		tandasMusicTable.setDropMode(DropMode.ON_OR_INSERT);
		tandasMusicTable.setTransferHandler(new MusicTransferHandler());
		tandasMusicTable.setDragEnabled(false); // pas de drag dans la table
		
		//	construction du panel
			
		getContentPane().add(panel);		
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panelName.add(nameTanda);
		
		helpButton.setActionCommand("help");
		helpButton.addActionListener(this);
		panelName.add(helpButton);
		
		sortButton.setActionCommand("sort");
		sortButton.addActionListener(this);
		sortButton.setSelected(false);
		panelName.add(sortButton);
		
		panel.add(panelName,BorderLayout.NORTH);
		
		//	table
		
		tandasMusicTable.setPreferredScrollableViewportSize(new Dimension(widthTable,heightTable));
		tandasMusicTable.setFillsViewportHeight(true);
				//  adpatation des colonnes
		TableColumn column = null;
		for (int i = 0; i < columnWidth.length; i++) {
		    column = tandasMusicTable.getColumnModel().getColumn(i);
		    column.setPreferredWidth(columnWidth[i]);
		    if ( i == 0 )  {
				JTextField title = new JTextField();
				column.setCellEditor(new DefaultCellEditor(title));
		    }
		}
		
		panel.add(tandasMusicTableScrollPane,BorderLayout.CENTER);
		
		//  side boutons
		
		moveUpButton.setActionCommand("upMusic");
		moveUpButton.addActionListener(this);
		deleteMusicButton.setActionCommand("delMusic");
		deleteMusicButton.setMnemonic(KeyEvent.VK_DELETE); // alt + delete
		deleteMusicButton.addActionListener(this);
		moveDownButton.setActionCommand("downMusic");
		moveDownButton.addActionListener(this);
		
		panelSideButton.add(moveUpButton);
		panelSideButton.add(deleteMusicButton);
		panelSideButton.add(moveDownButton);
		panel.add(panelSideButton,BorderLayout.EAST);
				
		//  boutons
		
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);
		panelButton.add(deleteButton);
		
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		panelButton.add(saveButton);
		
		panel.add(panelButton,BorderLayout.SOUTH);
		
		pack();		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(false);

	}
			
	/**
	 * open dialog box
	 * @param tanda
	 */
	public void open(String tanda)  {		
		this.tanda = tanda;
		if ( tanda == null )  {
			nameTanda.setText("");
			nameTanda.setEnabled(true);
		} else {
			nameTanda.setText(tanda);
			nameTanda.setEnabled(true);
		}
		verifTandaName();
		sortButton.setSelected(false);
		tandasMusicTable.setRowSorter(null);								
		moveUpButton.setEnabled(true);
		moveDownButton.setEnabled(true);
		readTanda(tanda);
		setVisible(true);
	}
	
	/**
	 * v�rification du nom de la tanda
	 */
	public void verifTandaName() {
		if ( nameTanda.getText().length() == 0 )  {
			deleteButton.setEnabled(false);
			saveButton.setEnabled(false);
		} else  {
			deleteButton.setEnabled(true);
			saveButton.setEnabled(true);
		}
		if ( tanda == null  )  {
			deleteButton.setEnabled(false);
		}			
	}

	/**
	 * read and load tanda
	 * @param tanda
	 */
	private void readTanda(String tanda)  {
		String nmFile = playlistDir + "/" + tanda + playlistExt;
		DefaultTableModel tableModel = (DefaultTableModel) tandasMusicTable.getModel();
		tableModel.setRowCount(0);
		if ( tanda == null )  {
			return;
		}
			//	read playlist 
		BufferedReader in;		
		String line;
		try  {
			InfoMedia infoMedia = new InfoMedia();
			in = new BufferedReader(new InputStreamReader(new FileInputStream(nmFile),"UTF8"));
			line = in.readLine();
			line = in.readLine();
			while (line != null)  {
				String[] music = new String[6];
				line = line.trim();
				if ( line.startsWith("#EXTINF:")  )  {
					String[] s = line.substring(8).split(",");
					music[0] = s[1];
				} else {
					JOptionPane.showMessageDialog(this, "Manque #EXTINF: " + tanda,
							ContexteGlobal.getResourceString("messReadFileTanda"), JOptionPane.ERROR_MESSAGE);
					return;
				}
				String path = in.readLine();
				music[5] = path;
				File file = new File(path);
				tableModel.addRow(music);
				infoMedia.getInfoMedia(file.toURI(), tandasMusicTable, tableModel.getRowCount()-1);
				line = in.readLine();					
			}
			in.close();
			infoMedia.stopInfoMedia();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErrRead") + tanda,
					ContexteGlobal.getResourceString("messReadFileTanda"), JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return;
		}		
	}
	
	/**
	 * write tanda
	 * @param tanda
	 */
	private void writeTanda(String tanda)  {
		String nmFile = playlistDir + "/" + tanda + playlistExt;
		BufferedWriter out;
		System.setProperty( "file.encoding","UTF-8"); // ne fonctionne pas sans �a ?

		try	{
			OutputStreamWriter outstr = new OutputStreamWriter(new FileOutputStream(nmFile),"UTF8");
			out = new BufferedWriter(outstr);
			out.write("#EXTM3U\n");
			DefaultTableModel modelJTable = (DefaultTableModel)tandasMusicTable.getModel();
			for (int iView = 0; iView < modelJTable.getRowCount(); iView++ ) {
				int i = tandasMusicTable.convertRowIndexToModel(iView);
				String nameMusic = (String) modelJTable.getValueAt(i,0);
				out.write("#EXTINF:"
				 + modelJTable.getValueAt(i,3) + ","
				 + nameMusic.trim() + "\n"
				 + modelJTable.getValueAt(i,5) + "\n");
			}
			out.close();
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messSaveOK"),
					ContexteGlobal.getResourceString("messSaveFileTanda"), JOptionPane.INFORMATION_MESSAGE);
			parentPanel.listTandas();

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErrSave") + nmFile,
					ContexteGlobal.getResourceString("messSaveFileTanda"), JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}	
	}
	
	/**
	 * delete tanda
	 * @param tanda
	 */
	private void deleteTanda(String tanda)  {
		String nmFile = playlistDir + "/" + tanda + playlistExt;
		File file = new File(nmFile);
		int okno = JOptionPane.showConfirmDialog(this, ContexteGlobal.getResourceString("messConfirm"),
				ContexteGlobal.getResourceString("messDeleteFileTanda"), JOptionPane.YES_NO_OPTION);
		if (okno == JOptionPane.OK_OPTION) {
			try	{
				file.delete();
				parentPanel.listTandas();
				JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messDeleteOK"),
						ContexteGlobal.getResourceString("messDeleteFileTanda"), JOptionPane.INFORMATION_MESSAGE);			
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErrDelete") + nmFile,
						ContexteGlobal.getResourceString("messSaveFileTanda"), JOptionPane.ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}		
	}
		  
	/**
	 * Drag & drop : transfert des donn�es
	 *
	 */
	private class MusicTransferHandler extends TransferHandler {
		
		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
		 */
		public boolean canImport(TransferSupport support) {
	        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	            support.setDropAction(COPY);
	            return true;
	         }
	        return false;
	    }

	    /* (non-Javadoc)
	     * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
	     */
	    public boolean importData(TransferSupport support) {
	        if (!canImport(support)) {
	            return false;
	        }
	        	// fetch the JTable & JTableModel
	        JTable table = (JTable)support.getComponent();
	        DefaultTableModel modelJTable = (DefaultTableModel)table.getModel();
	    		// fetch drop location
	        JTable.DropLocation dl = (JTable.DropLocation)support.getDropLocation();
	        int row = dl.getRow();        
	        	// fetch the list of file
	        java.util.List<File> listFile;
	        try {
	          listFile =
	        	(java.util.List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
	        } catch (UnsupportedFlavorException e) {
	            return false;
	        } catch (IOException e) {
	            return false;
	        }
	        	//  disable sort
	        sortButton.setSelected(false);
			tandasMusicTable.setRowSorter(null);								
			moveUpButton.setEnabled(true);
			moveDownButton.setEnabled(true);
        		//  insert
	        InfoMedia infoMedia = new InfoMedia();
	        for (File file : listFile) {
	        	String path = file.getPath();
	        	int c = path.lastIndexOf(File.separatorChar);
				String[] music = new String[6];
				music[0] = path.substring(c+1,path.lastIndexOf('.'));
				music[5] = path;
// ajout car l'insertion ne marche pas avec getInfoMedia (changement valeur row)				
				modelJTable.addRow(music);
				infoMedia.getInfoMedia(file.toURI(), tandasMusicTable, modelJTable.getRowCount()-1);
//				modelJTable.insertRow(row,music);
//				infoMedia.getInfoMedia(file.toURI(), table, row);
	        }
			infoMedia.stopInfoMedia();
	        return true;
	    }
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		int okno = JOptionPane.showConfirmDialog(this, ContexteGlobal.getResourceString("messConfirm"),
				ContexteGlobal.getResourceString("messSaveFileTanda"), JOptionPane.YES_NO_OPTION);
		if (okno == JOptionPane.OK_OPTION) {
			writeTanda(nameTanda.getText());
		}
		dispose();							

	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}


}
