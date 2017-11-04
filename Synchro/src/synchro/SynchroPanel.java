package synchro;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Vector;

import javax.swing.*;
import javax.swing.text.*;

import winApp.ContexteGlobal;


public class SynchroPanel  extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	
		//	constantes
	
	protected static String baseDir = ContexteGlobal.getResourceString("baseDir");
	private static Dimension dimComboBox = new Dimension(250,25);

		//	panel
	
	private JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,15,5));		
	private JButton paramButton = new JButton(ContexteGlobal.getResourceString("paramButton"));
	private JButton assembleButton = new JButton(ContexteGlobal.getResourceString("assembleButton"));
	private Vector<String> listFileParam = new Vector<String>();
	private JComboBox<String> listFile = new JComboBox<String>(listFileParam);
	private JButton refreshButton = new JButton(new ImageIcon(ContexteGlobal.getResource("refreshImage")));
	public JButton synchroButton = new JButton(ContexteGlobal.getResourceString("synchroButton"));
	public JButton verifyButton = new JButton(ContexteGlobal.getResourceString("verifyButton"));
	public JCheckBox traceCheck = new JCheckBox("Trace");
	public JButton stopButton = new JButton(ContexteGlobal.getResourceString("stopButton"));
	private JTextPane outputLog = new JTextPane();

		//	dialog
	
	public DialogUpdateParam updateParam;
	public DialogAssembleParam assembleParam;

		//	synchro
	
	private Backup bk = null; 

		//	data
	
	private byte[] lineLog = new byte[1000];
	private int indLineLog = 0;
	
	
	public SynchroPanel() throws Exception  {
		
		if ( baseDir == null )
			baseDir = ".";
		winApp.ContexteGlobal.frame.getAction("open").setEnabled(true);
		winApp.ContexteGlobal.frame.getAction("update").setEnabled(true);
		winApp.ContexteGlobal.frame.getAction("assemble").setEnabled(true);
			
		setLayout(new BorderLayout(5,5));
		
			//	Actions
		
		toolbarPanel.add(paramButton);
		toolbarPanel.add(assembleButton);
		toolbarPanel.add(listFile);		
		toolbarPanel.add(refreshButton);		
		toolbarPanel.add(verifyButton);		
		toolbarPanel.add(synchroButton);
		toolbarPanel.add(stopButton);
		toolbarPanel.add(traceCheck);

		paramButton.addActionListener(this);
		assembleButton.addActionListener(this);
		listFile.addActionListener(this);
		listFile.setActionCommand("listFile");
		listFile.setPreferredSize(dimComboBox);
		refreshButton.setPreferredSize(new Dimension(30,30));
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				listFile.hidePopup();
 				listFile.setSelectedIndex(-1);
				listFile();
				if ( !listFileParam.isEmpty() )
					listFile.setSelectedIndex(0);
			}
		} );
		verifyButton.addActionListener(this);	
		synchroButton.addActionListener(this);
		stopButton.addActionListener(this);
		traceCheck.addActionListener(this);
		
		add(toolbarPanel,BorderLayout.NORTH);
		
			//	init boutons
		
		verifyButton.setEnabled(false);
		winApp.ContexteGlobal.frame.getAction("verify").setEnabled(false);
		synchroButton.setEnabled(false);
		winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(false);
		stopButton.setEnabled(false);
	
			// log
		
        Font font = new Font("Serif", Font.PLAIN, 17);
        setJTextPaneFont(outputLog, font, Color.black);
		JScrollPane resultPanel = new JScrollPane(outputLog);	
		outputLog.setBorder(BorderFactory.createLineBorder(Color.black));		
		add(resultPanel,BorderLayout.CENTER);
		
			//	gestion fichiers paramètres
		
		updateParam = new DialogUpdateParam();
		assembleParam = new DialogAssembleParam();

		refresh();
	}

	
	/**
	 *		panel refresh 
	 */
	public void refresh()  {
		listFile();
		if ( !listFileParam.isEmpty() )
			listFile.setSelectedIndex(0);				
	}

	//	choix d'un fichier de paramètre
	
	public void open()	{
		File fileParam = updateParam.chooseParam();
		if ( fileParam != null ) {
			listFile.setSelectedItem(fileParam.getName());
			verifyButton.setEnabled(true);
			winApp.ContexteGlobal.frame.getAction("verify").setEnabled(true);
			synchroButton.setEnabled(false);
			winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(false);
			outputLog.setText("");
 			bk = new Backup(fileParam);
		}
		
	}
	
	//		liste des fichiers de paramètre
	
	private void listFile()	{
		File dirCourant = new File(baseDir);
		if ( dirCourant == null )
			return;
		File[] listFileSource = dirCourant.listFiles(new FileFilter() { 
	         public boolean accept(File dir)  {
	        	 return dir.isFile() && !dir.isHidden() &&
	           		   ( dir.getName().endsWith("."+ContexteGlobal.getResourceString("saveExt")) ||
	           			 dir.getName().endsWith("."+ContexteGlobal.getResourceString("saveMExt")) );
	         }
		} );
		listFileParam.clear();
		for ( File f: listFileSource )  {
			listFileParam.add(f.getName());
		}
	}	
	
	//		synchronisation
	
	public void synchroDir(int phase)  {
		if ( phase == 1 )
			winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messVerifSynchroDeb"));
		else
			winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messSynchroDeb"));
		try  {			
		    PipedOutputStream logout = new PipedOutputStream();
			PipedInputStream  login  = new PipedInputStream();
			login.connect(logout);        
			outputLog.setText("");
			if ( phase == 1 )
				appendLine2Log(ContexteGlobal.getResourceString("messVerifSynchroDeb")+ '\n', Color.BLACK, true);
			else
				appendLine2Log(ContexteGlobal.getResourceString("messSynchroDeb")+ '\n', Color.BLACK, true);
			
			//   lancement thread de synchro

 			bk.go(phase, logout);
			
			//  lancement thread sortie log	
			
	        Thread thread1 = new Thread(new Runnable() {
	            @Override
	            public void run() {
	                try {
	                    int data = login.read();
	                    while(data != -1)  {
	                        appendChar2Log(data);
	                        data = login.read();
	                    }
	                    appendChar2Log('\n');
	            		login.close();
	                } catch (IOException ex) {
	        			System.out.println("Erreur : " + ex);
	        			ex.printStackTrace();	                	
	                }
	                if ( bk.codeRetour != 0 )  {
		     			appendLine2Log(ContexteGlobal.getResourceString("messErreur")+ '\n', Color.RED, true);
	     				winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messErreur"));
	                } else if ( phase == 1 )  {
		     			appendLine2Log(ContexteGlobal.getResourceString("messVerifSynchroFin")+ '\n', Color.BLACK, true);
	     				winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messVerifSynchroFin"));
	     			} else {
		     			appendLine2Log(ContexteGlobal.getResourceString("messSynchroFin")+ '\n', Color.BLACK, true);
	     				winApp.ContexteGlobal.frame.setMessage(ContexteGlobal.getResourceString("messSynchroFin"));
	     			}
	    			stopButton.setEnabled(false);
	            }
	        });
	        thread1.start();			
        } catch (IOException ex) {
			JOptionPane.showMessageDialog(this,
					ContexteGlobal.getResourceString("messErreur"),
					ContexteGlobal.getResourceString("messSynchroDir"),
					JOptionPane.ERROR_MESSAGE);
        }
		
	}
	
	//		actions	
	
	public void actionPerformed(ActionEvent e) {
		//		gestions fichiers paramètres
		if (e.getActionCommand().equals(paramButton.getText())) {
			try {
				updateParam.open();
				return;
	        } catch (Exception ex) {
	        }				
		}
		//		assemblage fichiers paramètres
		if (e.getActionCommand().equals(assembleButton.getText())) {
			try {
				assembleParam.open();
				return;
	        } catch (Exception ex) {
	        }				
		}
		//		combobox fichiers paramètres
		if (e.getActionCommand().equals("listFile") )   {
	        String fileName = (String) listFile.getSelectedItem();
	        if ( fileName == null )  {
				verifyButton.setEnabled(false);
				winApp.ContexteGlobal.frame.getAction("verify").setEnabled(false);
				synchroButton.setEnabled(false);
				winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(false);
	        	return;
	        }
			verifyButton.setEnabled(true);
			winApp.ContexteGlobal.frame.getAction("verify").setEnabled(true);
			synchroButton.setEnabled(false);
			winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(false);
			outputLog.setText("");			
			bk = new Backup(new File(fileName));
			return;
		}
		//		vérification synchro
		if (e.getActionCommand().equals(verifyButton.getText())) {
			synchroDir(1);
			if ( bk.codeRetour == 0 )  {
				stopButton.setEnabled(true);
				synchroButton.setEnabled(true);
				winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(true);
			}
		}
		//		synchro
		if (e.getActionCommand().equals(synchroButton.getText())) {
			synchroDir(2);
			stopButton.setEnabled(true);
			synchroButton.setEnabled(false);
			winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(false);
		}
		//		stop synchro
		if (e.getActionCommand().equals(stopButton.getText())) {
			bk.stop();
			stopButton.setEnabled(false);
			synchroButton.setEnabled(false);
			winApp.ContexteGlobal.frame.getAction("synchro").setEnabled(false);
		}
		//		trace
		if (e.getActionCommand().equals(traceCheck.getText())) {
			bk.trace = traceCheck.isSelected();
		}
    }

	//		ajoute un caractère à la log
	
	private synchronized void appendChar2Log( int c )  {
		lineLog[indLineLog++] = (byte) c;
		if ( c == '\n' || lineLog.length == indLineLog )  {
			String lineLogString = new String(lineLog,0,indLineLog);
			
				//	traitement des lignes de la log
			
			if ( lineLogString.startsWith(">>") )  {
					//	trace
				appendLine2Log(lineLogString, Color.BLACK, false);
			} else if ( lineLogString.startsWith("**") )  {
					//	erreurs
				appendLine2Log(lineLogString, Color.RED, true);
			} else if ( lineLogString.startsWith("=>") )  {
					//	actions
				if ( lineLogString.startsWith("=>création") )  {
					appendLine2Log(lineLogString, Color.BLUE, false);
				} else if ( lineLogString.startsWith("=>suppression") )  {
					appendLine2Log(lineLogString, Color.MAGENTA, false);
				} else
					appendLine2Log(lineLogString, Color.BLACK, false);
					
			} else if ( lineLogString.startsWith("Synchro:") )  {
					//  message de synchro				
				String lineMsg = lineLogString.substring(8, lineLogString.length()-1);
				winApp.ContexteGlobal.frame.setMessage(lineMsg);
			} else {
					//	messages non classés
				appendLine2Log(lineLogString, Color.GRAY, false);				
			}
			indLineLog = 0;
			repaint();
		}
	}

	//		affiche une ligne sur la log
	
    private synchronized void appendLine2Log(String msg, Color c, boolean bold)
    {
    		//	Style
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "SANS_SERIF"); //  SERIF,  	MONOSPACED, 
        aset = sc.addAttribute(aset, StyleConstants.FontSize, 12);
        aset = sc.addAttribute(aset, StyleConstants.Bold, bold);	// PLAIN, ITALIC, BOLD
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        
        	//   ajout à la fin du document
        int len = outputLog.getDocument().getLength();
        outputLog.setCaretPosition(len);
        outputLog.setCharacterAttributes(aset, false);
        outputLog.replaceSelection(msg);
    }
    
    // 		Set the font and color of a JTextPane
    
    public void setJTextPaneFont(JTextPane jtp, Font font, Color c) {
    	
        	// Start with the current input attributes for the JTextPane.
        MutableAttributeSet attrs = jtp.getInputAttributes();

        	// Set the font family, size, and style
        StyleConstants.setFontFamily(attrs, font.getFamily());
        StyleConstants.setFontSize(attrs, font.getSize());
        StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
        StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);

        	// Set the font color
        StyleConstants.setForeground(attrs, c);

        	// Retrieve the pane's document object
        StyledDocument doc = jtp.getStyledDocument();

	        // Replace the style for the entire document. We exceed the length
	        // of the document by 1 so that text entered at the end of the
	        // document uses the attributes.
        doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
    } 
    
    //		choix du répertoire de base
    
    public void baseDir()  {
		JFileChooser jFileChooserBaseDir = new JFileChooser(baseDir);
		jFileChooserBaseDir.setDialogTitle(ContexteGlobal.getResourceString("baseDirTooltip"));
		jFileChooserBaseDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (jFileChooserBaseDir.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
			winApp.ContexteGlobal.putProperty("baseDir", jFileChooserBaseDir.getSelectedFile().getPath());
			baseDir = jFileChooserBaseDir.getSelectedFile().getPath();
			refresh();
		}

    }
}
