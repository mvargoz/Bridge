package synchro;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import winApp.ContexteGlobal;
import winApp.GenericFileFilter;

public class DialogAssembleParam extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	//		constantes	

	private static Dimension dimComboBox = new Dimension(250,25);
	private static Border blackline = BorderFactory.createLineBorder(Color.black);
	private static Dimension dimAction = new Dimension(50,70);
	private static int widthList = 200;
	private static int heightList = 15;
	private static int rowVisibleList = 20;

	//		frame
	
	private Frame parentFrame;

	//		panel
	
	private JPanel panel = new JPanel(new BorderLayout(0,20));
	private JPanel panelSaisie = new JPanel(new BorderLayout(0,20));
	
	private JPanel panelKey = new JPanel(new FlowLayout());
	private JLabel paramButton = new JLabel(ContexteGlobal.getResourceString("assembleButton"));
	private Vector<String> listFileKey = new Vector<String>();
	private JComboBox<String> listFile = new JComboBox<String>(listFileKey);
	private JButton deleteButton = new JButton(ContexteGlobal.getResourceString("deleteButton"),
			   new ImageIcon(ContexteGlobal.getResource("deleteImage")));
	
	private JPanel panelList = new JPanel(new FlowLayout());
	private DefaultListModel<String> listModelSource = new DefaultListModel<String>();
	private JList<String> listFileParam = new JList<String>(listModelSource);
	private JScrollPane listFileParamScrollPane = new JScrollPane(listFileParam);
	
	private JPanel panelAction = new JPanel(new GridLayout(2,1,0,10));
	private JButton takeButton = new JButton(ContexteGlobal.getResourceString("takeButton"));
	private JButton removeButton = new JButton(ContexteGlobal.getResourceString("removeButton"));
	
	private DefaultListModel<String> listModelObjet = new DefaultListModel<String>();
	private JList<String> listFileAssemble = new JList<String>(listModelObjet);
	private JScrollPane listFileAssembleScrollPane = new JScrollPane(listFileAssemble);
	
	private JPanel panelValid = new JPanel(new FlowLayout());
	private JButton validButton = new JButton(ContexteGlobal.getResourceString("validButton"),
			  new ImageIcon(ContexteGlobal.getResource("validImage")));
	private JButton cancelButton = new JButton(ContexteGlobal.getResourceString("cancelButton"),
			   new ImageIcon(ContexteGlobal.getResource("cancelImage")));	
	private JLabel jlMessage = new JLabel(ContexteGlobal.getResourceString("messAssembleParam"));
	
	//		data
	
	private static String title = ContexteGlobal.getResourceString("titleAssembleParam");
	private static boolean modal = false;
	private static String paramExt = ContexteGlobal.getResourceString("saveExt");
	private static String paramAssExt = ContexteGlobal.getResourceString("saveMExt");
	private static String paramDescr = ContexteGlobal.getResourceString("saveMess");
	private static String paramDir = ContexteGlobal.getResourceString("baseDir");
	
	/**
	 * 			Cr�ation de la boite de dialogue
	 */
	
	public DialogAssembleParam() throws Exception  {
		super(winApp.ContexteGlobal.frame, title, modal);
		parentFrame = winApp.ContexteGlobal.frame;
		
        	//	panel
		getContentPane().add(panel);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));

			// 	messsage
		jlMessage.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(jlMessage,BorderLayout.NORTH);
		
			//	saisie
		panelKey.add(paramButton);
		panelKey.add(listFile);
		panelKey.add(deleteButton);
		listFile.addActionListener(this);
		listFile.setActionCommand("listFile");
		listFile.setEditable(true);
		listFile.setPreferredSize(dimComboBox);
		if ( !listFileKey.isEmpty() )
			listFile.setSelectedIndex(0);
		deleteButton.setActionCommand("delete");
		deleteButton.addActionListener(this);
		panelSaisie.add(panelKey,BorderLayout.NORTH);        
        
        listFileParam.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listFileParam.setFixedCellWidth(widthList);
        listFileParam.setFixedCellHeight(heightList);
        listFileParam.setVisibleRowCount(rowVisibleList);
        listFileParam.setBorder(blackline);
        listFileParam.setDragEnabled(true);

        panelList.add(listFileParamScrollPane);
         
        takeButton.setActionCommand("take");
        takeButton.addActionListener(this);
        panelAction.add(takeButton);
        removeButton.setActionCommand("remove");
        removeButton.addActionListener(this);
        panelAction.add(removeButton);
        panelAction.setPreferredSize(dimAction);
        panelList.add(panelAction);
         
        listFileAssemble.setFixedCellWidth(widthList);
        listFileAssemble.setFixedCellHeight(heightList);
        listFileAssemble.setVisibleRowCount(rowVisibleList);
        listFileAssemble.setBorder(blackline);
        listFileAssemble.setDragEnabled(false);
        listFileAssemble.setTransferHandler(new ListTransferHandler());
        listFileAssemble.setDropMode(DropMode.ON_OR_INSERT);
        panelList.add(listFileAssembleScrollPane);
        
        panelSaisie.add(panelList,BorderLayout.CENTER);
         
		panel.add(panelSaisie,BorderLayout.CENTER);
		
			//	validation
		
		panelValid.add(validButton);
		validButton.setActionCommand("save");
		validButton.addActionListener(this);	
		validButton.setEnabled(true);
		
		panelValid.add(cancelButton);	
		cancelButton.setActionCommand("cancel");	
		cancelButton.addActionListener(this);	
		cancelButton.setEnabled(true);
		
		panel.add(panelValid,BorderLayout.SOUTH);	
		
		pack();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(false);
	}

	//		ouverture
	
	public void open() {
 		setLocationRelativeTo(parentFrame);		
		listFile();
		if ( !listFileKey.isEmpty() )
			listFile.setSelectedIndex(0);
 		listFileSource();
		setVisible(true);
	}
	
	//		liste des fichiers d'assemblage
	
	private void listFile()	{
		File dirCourant = new File(SynchroPanel.baseDir);
		if ( dirCourant == null )
			return;
		File[] listFileSource = dirCourant.listFiles(new FileFilter() { 
	         public boolean accept(File dir)  {
	        	 return dir.isFile() && !dir.isHidden() &&
	           		    dir.getName().endsWith("." + paramAssExt);
	         }
		} );
		listFileKey.clear();
		for ( File f: listFileSource )  {
			listFileKey.add(f.getName());
		}
	}	
	
	//		action listener	
	
	public void actionPerformed(ActionEvent e) {
		String fileName;
		File paramFile;
		java.util.List<String> listParam;
		switch (e.getActionCommand())  {
		case "save":
			String fname = saveParam();
			if ( fname != null && !listFileKey.contains(fname) )  {
 				listFile.setSelectedIndex(-1);
				listFileKey.add(fname);
				listFileKey.sort(null);			
			}
			if ( fname != null )
				listFile.setSelectedItem(fname);
			break;
		case "cancel":
			setVisible(false);
			break;
		case "delete":
	        fileName = (String) listFile.getSelectedItem();
	        if ( fileName == null )
	        	break;
			paramFile = new File(SynchroPanel.baseDir + "/" + fileName);
			if ( paramFile != null && paramFile.exists() )  {
				int okno = JOptionPane.showConfirmDialog(this,
						fileName + "\n" + ContexteGlobal.getResourceString("mess9"),
			            ContexteGlobal.getResourceString("messDeleteFileSynchro"),
			            JOptionPane.YES_NO_OPTION);
				if ( okno == JOptionPane.OK_OPTION )  {
					try  {
						Files.delete(paramFile.toPath());
						listFileKey.remove(fileName);
						if ( !listFileKey.isEmpty() )
							listFile.setSelectedIndex(0);
						else
			 				listFile.setSelectedIndex(-1);						
			        } catch (IOException ex) {
						JOptionPane.showMessageDialog(this,
								ContexteGlobal.getResourceString("messErreur"),
								ContexteGlobal.getResourceString("messDeleteFileSynchro"),
								JOptionPane.ERROR_MESSAGE);
			        }
				}
			}			
			break;
		case "listFile":
			//		selection fichier de parametres dans liste
	        fileName = (String) listFile.getSelectedItem();
	        if ( fileName == null )
	        	break;
			paramFile = new File(SynchroPanel.baseDir + "/" + fileName);
			if ( paramFile != null && paramFile.exists() )  {
				readParam(paramFile);
			} else {
				listModelObjet.clear();				
			}
			break;
		case "take":
			//		choisir un ou plusieurs fichiers de parametres
			listParam = listFileParam.getSelectedValuesList();
			for ( String s : listParam )  {
				if ( !listModelObjet.contains(s) )
					listModelObjet.addElement(s);
			}
			listFileParam.clearSelection();
			break;
			//		supprimer un ou plusieurs fichiers de parametres de la liste
		case "remove":
			listParam = listFileAssemble.getSelectedValuesList();
			for ( String s : listParam )  {
				listModelObjet.removeElement(s);
			}
			listFileAssemble.clearSelection();
			break;
		}
	}
	
	//		liste des fichiers de parametre
	
	private void listFileSource()	{
		File dirCourant = new File(SynchroPanel.baseDir);
		if ( dirCourant == null )
			return;
		File[] listFileSource = dirCourant.listFiles(new FileFilter() { 
	         public boolean accept(File dir)  {
	        	 return dir.isFile() && !dir.isHidden() &&
	           		    dir.getName().endsWith("." + paramExt);
	         }
		} );
		listModelSource.clear();
		for ( File f: listFileSource )  {
			listModelSource.addElement(f.getName());
		}
	}	

	//	lecture d'un fichier d'assemblage
	
	private boolean readParam(File paramFile)  {
		listModelObjet.clear();
		String line;  	// ligne courante
		try  {
			BufferedReader in = new BufferedReader(new FileReader(paramFile));
			line = in.readLine();
			while (line != null)  {
				line = line.trim();
				String[] p = line.split("=");			
				if ( p[0].equals("syn") )  {
					listModelObjet.addElement(p[1]);

				} else {
					JOptionPane.showMessageDialog(this,
							ContexteGlobal.getResourceString("messErrFileSynchro") + "syn=",
							ContexteGlobal.getResourceString("messReadFileSynchro"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
	
				line = in.readLine();			
			}
			in.close();

        } catch (IOException ex) {
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErrRead"),
					ContexteGlobal.getResourceString("messReadFileSynchro"),
					JOptionPane.ERROR_MESSAGE);
			return false;
        }
		return true;
	}

	//	Ecriture d'un fichier d'assemblage
	
	private String saveParam()  {
		//		contr�les
        String newParamNom = (String) listFile.getSelectedItem();		
 		if ( newParamNom.trim().equals("") || listModelObjet.getSize() == 0 )   {
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErrSaisie"),
					ContexteGlobal.getResourceString("messSaveFileSynchro"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
 		
		//		nom du fichier de parametres
 		String ext = '.' + paramAssExt;
 		if ( !newParamNom.endsWith(ext) )
 			newParamNom += ext;
		File paramFile = new File(SynchroPanel.baseDir + "/" + newParamNom);

		try
		{
			FileWriter out = new FileWriter(paramFile);
			for ( Enumeration<String> e = listModelObjet.elements(); e.hasMoreElements(); )  {
				String s = e.nextElement();
				if ( s.trim().length() > 0 )
					out.write("syn=" + s + '\n');
			}
			out.close();
		
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messSaveOK"),
					ContexteGlobal.getResourceString("messSaveFileSynchro"),
					JOptionPane.INFORMATION_MESSAGE);
			winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messSaveOK"));
			
        } catch (IOException ex) {
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErrSave"),
					ContexteGlobal.getResourceString("messSaveFileSynchro"),
					JOptionPane.ERROR_MESSAGE);
			return null;
        }
		return newParamNom;
		
	}

	//	choix d'un fichier d'assemblage
	
	public File chooseParam()	{
		JFileChooser jFileChooserPlay;
		GenericFileFilter filter = new GenericFileFilter();
		filter.addExtension(paramAssExt);
		filter.setDescription(paramDescr);		
		jFileChooserPlay = new JFileChooser(paramDir);
		jFileChooserPlay.setFileFilter(filter);
		int returnVal = jFileChooserPlay.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)	{
			return jFileChooserPlay.getSelectedFile();
		}
		return null;		
		
	}

}
