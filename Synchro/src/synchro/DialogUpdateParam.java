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
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import winApp.ContexteGlobal;
import winApp.GenericFileFilter;

public class DialogUpdateParam extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	//		frame
	
	private Frame parentFrame;

	//		panel

	private JPanel panel = new JPanel(new BorderLayout(0,20));
	private JPanel panelSaisie = new JPanel(new GridLayout(6,1,10,10));
	
	private JPanel panelKey = new JPanel(new FlowLayout());
	private JLabel paramButton = new JLabel(ContexteGlobal.getResourceString("paramButton"));
	private Vector<String> listFileKey = new Vector<String>();
	private JComboBox<String> listFile = new JComboBox<String>(listFileKey);
	private JButton deleteButton = new JButton(ContexteGlobal.getResourceString("deleteButton"),
								   new ImageIcon(ContexteGlobal.getResource("deleteImage")));
	
	private JPanel panelSource = new JPanel(new FlowLayout());
	private JTextField fileSource = new JTextField(40);
	private JButton fileSourceButton = new JButton(ContexteGlobal.getResourceString("fileSourceButton"),
									   new ImageIcon(ContexteGlobal.getResource("folderImage")));
	
	private JPanel panelObjet = new JPanel(new FlowLayout());
	private JTextField fileObjet = new JTextField(40);
	private JButton fileObjetButton = new JButton(ContexteGlobal.getResourceString("fileObjetButton"),
									  new ImageIcon(ContexteGlobal.getResource("folderSyncImage")));
	
	private JPanel panelOptionSource = new JPanel(new FlowLayout(FlowLayout.LEFT));
	private JLabel ignoreSourceLabel = new JLabel(ContexteGlobal.getResourceString("messOptionIgnoreSource"));
	private JTextField ignoreSource = new JTextField(10);
	private JPanel panelOptionObjet = new JPanel(new FlowLayout(FlowLayout.LEFT));
	private JLabel ignoreObjetLabel = new JLabel(ContexteGlobal.getResourceString("messOptionIgnoreObjet"));
	private JTextField ignoreObjet = new JTextField(10);
	private JCheckBox parmDeleteDir = new JCheckBox(ContexteGlobal.getResourceString("messOptionDeleteDir"));
	
	private JPanel panelValid = new JPanel(new FlowLayout());
	private JButton validButton = new JButton(ContexteGlobal.getResourceString("validButton"),
								  new ImageIcon(ContexteGlobal.getResource("validImage")));
	private JButton cancelButton = new JButton(ContexteGlobal.getResourceString("cancelButton"),
								   new ImageIcon(ContexteGlobal.getResource("cancelImage")));	
	private JLabel jlMessage = new JLabel(ContexteGlobal.getResourceString("messUpdateParam"));
	
	//		data
	
	private static String title = ContexteGlobal.getResourceString("titleUpdateParam");
	private static boolean modal = false;
	private static Dimension dimButton = new Dimension(120,34);
	private static Dimension dimComboBox = new Dimension(250,25);
	private static String paramExt = ContexteGlobal.getResourceString("saveExt");
	private static String paramDescr = ContexteGlobal.getResourceString("saveMess");
	private static String paramDir = ContexteGlobal.getResourceString("baseDir");
		
	/**
	 * 			Crï¿½ation de la boite de dialogue
	 */
		
	public DialogUpdateParam() throws Exception  {
		super(winApp.ContexteGlobal.frame, title, modal);
		parentFrame = winApp.ContexteGlobal.frame;
		
        	//	panel
		getContentPane().add(panel);		
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
			// 	messsage haut
		jlMessage.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(jlMessage,BorderLayout.NORTH);
		
			//	saisie identifiant
		panelKey.add(paramButton);
		panelKey.add(listFile);
		panelKey.add(deleteButton);
		listFile.addActionListener(this);
		listFile.setActionCommand("listFile");
		listFile.setEditable(true);
		listFile.setPreferredSize(dimComboBox);
		if ( !listFileKey.isEmpty() )
			listFile.setSelectedIndex(0);
		deleteButton.addActionListener(this);
		panelSaisie.add(panelKey);
			//	repertoire source
        panelSource.add(fileSourceButton);
        panelSource.add(fileSource);
        fileSourceButton.setActionCommand("source");
        fileSourceButton.addActionListener(this);	
        fileSourceButton.setPreferredSize(dimButton);
        panelSaisie.add(panelSource);
        	//	repertoire objet
        panelObjet.add(fileObjetButton);
        panelObjet.add(fileObjet);		
        fileObjetButton.setActionCommand("objet");
        fileObjetButton.addActionListener(this);	
        fileObjetButton.setPreferredSize(dimButton);
        panelSaisie.add(panelObjet);
        	//  options
        panelOptionSource.add(ignoreSourceLabel);
        panelOptionSource.add(ignoreSource);
        panelSaisie.add(panelOptionSource);
        
        panelOptionObjet.add(ignoreObjetLabel);
        panelOptionObjet.add(ignoreObjet);
        panelSaisie.add(panelOptionObjet);
        
        panelSaisie.add(parmDeleteDir);
        
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
			//	positionnement de la fenêtre auto
 		setLocationRelativeTo(parentFrame);
		listFile();		
		if ( !listFileKey.isEmpty() )
			listFile.setSelectedIndex(0);
		setVisible(true);
	}
	
	//		liste des fichiers de paramï¿½tre
	
	private void listFile()	{
		File dirCourant = new File(SynchroPanel.baseDir);
		if ( dirCourant == null )
			return;
		File[] listFileSource = dirCourant.listFiles(new FileFilter() { 
	         public boolean accept(File dir)  {
	        	 return dir.isFile() && !dir.isHidden() &&
	           		    dir.getName().endsWith("." + paramExt);
	         }
		} );
		listFileKey.clear();
		for ( File f: listFileSource )  {
			listFileKey.add(f.getName());
		}
	}	
	
	//		actions	
	
	public void actionPerformed(ActionEvent e) {
		String fileName;
		File paramFile;
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
						ContexteGlobal.getResourceString("mess9"),
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
			}
			break;
		case "source":
			//		choix repertoire source
			File sourceDir = chooseDir(fileSource.getText());
			if ( sourceDir != null )  {
				fileSource.setText(sourceDir.getPath());
			}
			break;
		case "objet":
			//		choix repertoire objet
			File objetDir = chooseDir(fileObjet.getText());
			if ( objetDir != null )  {
				fileObjet.setText(objetDir.getPath());
			}
			return;
		}
	}

	//	lecture d'un fichier de parametre
	
	private boolean readParam(File paramFile)  {
		String line;  	// ligne courante
			//	initialisation
		fileSource.setText("");
		fileObjet.setText("");
		ignoreSource.setText("");
		ignoreObjet.setText("");
		parmDeleteDir.setSelected(false);
			//	lecture
		try  {
			BufferedReader in = new BufferedReader(new FileReader(paramFile));
			int indWaitLine = 0;
			String[] waitLines = { "in", "out", "parm" };
			line = in.readLine();
			while (line != null)  {
				line = line.trim();
				String[] p = line.split("=");			
				if ( p[0].equals(waitLines[indWaitLine]) )  {
					if ( p[0].equals("in") )  {
						fileSource.setText(p[1]);
					} else if ( p[0].equals("out") )  {
						fileObjet.setText(p[1]);
					} else if ( p[0].equals("parm") && p.length > 1 )  {
						String[] options = p[1].split(",");
						for ( String option: options )	{
							if ( option.equals("deleteDir") )
								parmDeleteDir.setSelected(true);
							if (  option.startsWith("ignoreSource/") )
								ignoreSource.setText(option.substring(13,option.length()-1));
							if (  option.startsWith("ignoreObjet/") )
								ignoreObjet.setText(option.substring(12,option.length()-1));
						}														
					}
				} else {
					JOptionPane.showMessageDialog(this,
							ContexteGlobal.getResourceString("messErrFileSynchro") + waitLines[indWaitLine],
							ContexteGlobal.getResourceString("messReadFileSynchro"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
					//  prochain paramï¿½tre
				
				indWaitLine++;
				if ( indWaitLine >= waitLines.length )
					indWaitLine = 0;
	
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

	//	ï¿½criture d'un fichier de paramï¿½tre
	
	private String saveParam()  {
		//		controles
        String newParamNom = (String) listFile.getSelectedItem();		
 		if ( newParamNom.equals("") ||
				fileSource.getText().length() == 0 ||
				fileObjet.getText().length() == 0  )   {
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErrSaisie"),
					ContexteGlobal.getResourceString("messSaveFileSynchro"),
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		//		nom du fichier de parametres
 		String ext = '.' + paramExt;
 		if ( !newParamNom.endsWith(ext) )
 			newParamNom += ext;
		File paramFile = new File(SynchroPanel.baseDir + "/" + newParamNom);
		
		//		options
		String options = "";
		if ( parmDeleteDir.isSelected() )
			options += "deleteDir,";
		String ignoreSourceText = ignoreSource.getText().trim();
		if ( ignoreSourceText.length() > 0 )
			options += "ignoreSource/" + ignoreSourceText + "/,";
		String ignoreObjetText = ignoreObjet.getText().trim();
		if ( ignoreObjetText.length() > 0 )
			options += "ignoreObjet/" + ignoreObjetText + "/,";	
		
		//		Ecriture
		try
		{
			FileWriter out = new FileWriter(paramFile);
			out.write("in=" + fileSource.getText() + '\n');
			out.write("out=" + fileObjet.getText() + '\n');
			out.write("parm=" + options + '\n');
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

	//	choix d'un fichier de parametre
	
	public File chooseParam()	{
		JFileChooser jFileChooserPlay;
		GenericFileFilter filter = new GenericFileFilter();
		filter.addExtension(paramExt);
		filter.setDescription(paramDescr);		
		jFileChooserPlay = new JFileChooser(paramDir);
		jFileChooserPlay.setFileFilter(filter);
		int returnVal = jFileChooserPlay.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)	{
			return jFileChooserPlay.getSelectedFile();
		}
		return null;		
		
	}

	//		choix d'un repertoire
	
	public File chooseDir(String path)	{
		JFileChooser jFileChooserPlay = new JFileChooser(path);
		jFileChooserPlay.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = jFileChooserPlay.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)  {
			return jFileChooserPlay.getSelectedFile();
		}
		return null;		
	}

}
