package playlist;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import winApp.ContexteGlobal;

/*
 * 		édition des tandas
*/

public class DialogEditTanda extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

		//	constantes
	
	private static final String playlistDir = PlaylistPanel.playlistDir;;
	private static final String playlistExt = PlaylistPanel.playlistExt;
	private static final String titreTanda = ContexteGlobal.getResourceString("titreTanda");
	private static final int widthTable = 500;
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

		// 	Liste des musiques d'une tanda

    private String[] columnNames = {"Titre", "Artiste", "Année", "Durée", ""};
    private int[] columnWidth = {250, 150, 40, 40, 0};
    private JTable tandasMusicTable = new JTable(new DefaultTableModel(columnNames,0));
	private JScrollPane tandasMusicTableScrollPane = new JScrollPane(tandasMusicTable);
	
		//	Boutons action sur tanda
	
	private JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER,20,20));
	private JButton deleteButton = new JButton(ContexteGlobal.getResourceString("deleteTanda"));
	private JButton saveButton = new JButton(ContexteGlobal.getResourceString("saveTanda"));
	
		//	Boutons action sur musiques

	private JPanel panelSideButton = new JPanel(new GridLayout(3,1));
	private JButton deleteMusicButton = new JButton(new ImageIcon(ContexteGlobal.getResource("delImage")));
	private JButton moveUpButton = new JButton(new ImageIcon(ContexteGlobal.getResource("upImage")));
	private JButton moveDownButton = new JButton(new ImageIcon(ContexteGlobal.getResource("downImage")));

		//  tanda en édition
	
	private String tanda = null;
	
		//  action listener
	
	@Override
	public void actionPerformed(ActionEvent actev) {
		int[] rows = tandasMusicTable.getSelectedRows();
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
		}

	}

		// contructeur
	
	public DialogEditTanda(PlaylistPanel pp)	{
		super(winApp.ContexteGlobal.frame, titreTanda, false);
		
			//  position
		
		parentFrame = winApp.ContexteGlobal.frame;
		parentPanel = pp;
		setLocationRelativeTo(parentFrame);
		
			//  contrôle du nom de la tanda
		

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

                if ( column == 4 ) {
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
		tandasMusicTable.setDragEnabled(true);
		
		//	construction du panel
			
		getContentPane().add(panel);		
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		panelName.add(nameTanda);
		helpButton.setActionCommand("help");
		helpButton.addActionListener(this);
		panelName.add(helpButton);
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
		

		//  open dialog box
	
	public void open(String tanda)  {		
		this.tanda = tanda;
		if ( tanda == null )  {
			nameTanda.setText("");
			nameTanda.setEnabled(true);
		} else {
			nameTanda.setText(tanda);
			nameTanda.setEnabled(false);
		}
		verifTandaName();	
		readTanda(tanda);
		setVisible(true);
	}
	
		//   vérification du nom de la tanda
	
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

		//	read and load tanda

	private void readTanda(String tanda)  {
		DefaultTableModel tableModel = (DefaultTableModel) tandasMusicTable.getModel();
		tableModel.setRowCount(0);
		if ( tanda == null )  {
			return;
		}
			//	read playlist 
		BufferedReader in;		
		String line;
		try  {
			in = new BufferedReader(new InputStreamReader(
					new FileInputStream(playlistDir + "/" + tanda + playlistExt), "UTF8"));
			line = in.readLine();
			line = in.readLine();
			while (line != null)  {
				String[] music = new String[5];
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
				music[4] = path;
				File file = new File(path);
				tableModel.addRow(music);
				new Thread(new InfoMedia(file.toURI(), tandasMusicTable, tableModel.getRowCount() - 1)).start();
				line = in.readLine();					
			}
			in.close();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, ContexteGlobal.getResourceString("messErrRead") + tanda,
					ContexteGlobal.getResourceString("messReadFileTanda"), JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
			return;
		}
		
	}
	
		//	write tanga
		
	private void writeTanda(String tanda)  {
		String nmFile = playlistDir + "/" + tanda + playlistExt;
		File file = new File(nmFile);
		FileWriter out;

		try	{
			out = new FileWriter(file);
			out.write("#EXTM3U\n");
			DefaultTableModel modelJTable = (DefaultTableModel)tandasMusicTable.getModel();
			for (int i = 0; i < modelJTable.getRowCount(); i++ ) {
				out.write("#EXTINF:"
				 + modelJTable.getValueAt(i,3) + ","
				 + modelJTable.getValueAt(i,0) + "\n"
				 + modelJTable.getValueAt(i,4) + "\n");
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
	
		//	delete tanga
	
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
		
			// Drag & drop
   
	private class MusicTransferHandler extends TransferHandler {
		
	    public boolean canImport(TransferSupport support) {
	        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	            support.setDropAction(COPY);
	            return true;
	         }
	        return false;
	    }

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
	        	//  insert
	        for (File file : listFile) {
	        	String path = file.getPath();
	        	int c = path.lastIndexOf(File.separatorChar);
				String[] music = new String[5];
				music[0] = path.substring(c+1, path.lastIndexOf('.'));
				music[4] = path;
				modelJTable.insertRow(row,music);
				new Thread(new InfoMedia(file.toURI(), table, row)).start();
	        }
	        return true;
	    }
	}


}
