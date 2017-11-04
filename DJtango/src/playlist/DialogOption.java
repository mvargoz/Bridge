package playlist;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import winApp.ContexteGlobal;

/*
 * 		saisie des options de l'application
*/

public class DialogOption extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

		//	frame
	
	private Frame parentFrame;
	private PlaylistPanel parentPanel;

	//	panel principal avec BorderLayout hgap vgap
		
	private JPanel panel = new JPanel(new BorderLayout(10,20));

		//	panel central
	
    final static String DATAPANEL1 = "Répertoires";
    final static String DATAPANEL2 = "Paramètres";
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JPanel panelData1 = new JPanel(new SpringLayout());
	private JPanel panelData2 = new JPanel(new SpringLayout());
	
		//	saisie répertoires
	
	private JLabel modelDirLabel = new JLabel(ContexteGlobal.getResourceString("modelDirLabel"));
	private JTextField modelDir = new JTextField(50);
	private JButton modelDirButton = new JButton(new ImageIcon(ContexteGlobal.getResource("folderImage")));
	
	private JLabel baseDirLabel = new JLabel(ContexteGlobal.getResourceString("baseDirLabel"));
	private JTextField baseDir = new JTextField(50);
	private JButton baseDirButton = new JButton(new ImageIcon(ContexteGlobal.getResource("folderImage")));
	
	private JLabel pcMusicDirLabel = new JLabel(ContexteGlobal.getResourceString("pcMusicDirLabel"));
	private JTextField pcMusicDir = new JTextField(50);
	private JButton pcMusicDirButton = new JButton(new ImageIcon(ContexteGlobal.getResource("folderImage")));
	
	private JLabel macMusicDirLabel = new JLabel(ContexteGlobal.getResourceString("macMusicDirLabel"));
	private JTextField macMusicDir = new JTextField(50);
	private JButton macMusicDirButton = new JButton(new ImageIcon(ContexteGlobal.getResource("folderImage")));
	
		//	Panel Boutons action
	
	private JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER,20,20));
	private JButton cancelButton = new JButton(ContexteGlobal.getResourceString("cancel"));
	private JButton saveButton = new JButton(ContexteGlobal.getResourceString("save"));

		//  Action listener
	
	@Override
	public void actionPerformed(ActionEvent actev) {
		switch ( actev.getActionCommand() )  {
		case "modelDir":
			JFileChooser jFileChooserMod = new JFileChooser(modelDir.getText());
			jFileChooserMod.setDialogTitle(modelDirLabel.getText());
			jFileChooserMod.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jFileChooserMod.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
				modelDir.setText(jFileChooserMod.getSelectedFile().getPath());
			}
			break;
		case "baseDir":
			JFileChooser jFileChooserPlay = new JFileChooser(baseDir.getText());
			jFileChooserPlay.setDialogTitle(baseDirLabel.getText());
			jFileChooserPlay.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jFileChooserPlay.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
				baseDir.setText(jFileChooserPlay.getSelectedFile().getPath());
			}
			break;
		case "pcMusicDir":
			JFileChooser jFileChooserMusPc = new JFileChooser(pcMusicDir.getText());
			jFileChooserMusPc.setDialogTitle(pcMusicDirLabel.getText());
			jFileChooserMusPc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jFileChooserMusPc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
				pcMusicDir.setText(jFileChooserMusPc.getSelectedFile().getPath());
			}
			break;
		case "macMusicDir":
			JFileChooser jFileChooserMusMac = new JFileChooser(macMusicDir.getText());
			jFileChooserMusMac.setDialogTitle(macMusicDirLabel.getText());
			jFileChooserMusMac.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jFileChooserMusMac.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
				macMusicDir.setText(jFileChooserMusMac.getSelectedFile().getPath());
			}
			break;
		case "ok":
			winApp.ContexteGlobal.putProperty("modelDir", modelDir.getText());
			PlaylistPanel.modelDir = modelDir.getText();
			winApp.ContexteGlobal.putProperty("baseDir", baseDir.getText());
			PlaylistPanel.baseDir = baseDir.getText();
			PlaylistPanel.playlistDir = PlaylistPanel.baseDir + "/" + ContexteGlobal.getResourceString("playlistDir");
			PlaylistPanel.tandaImageDir = PlaylistPanel.baseDir + "/" + ContexteGlobal.getResourceString("imageDir");
			winApp.ContexteGlobal.putProperty("pcTangoArgentinDir", pcMusicDir.getText());
			winApp.ContexteGlobal.putProperty("macTangoArgentinDir", macMusicDir.getText());
			parentPanel.refresh();
			dispose();
			break;
		case "cancel":
			dispose();
			break;

		}
	}
	
		// contructeur
	
	public DialogOption(PlaylistPanel pp)	{
		super(winApp.ContexteGlobal.frame, "Options", false);
		
			//  position
		
		parentFrame = winApp.ContexteGlobal.frame;
		parentPanel = pp;
		setLocationRelativeTo(parentFrame);
		
			//	construction du panel
			
		getContentPane().add(panel);		
		
		panelData1.add(modelDirLabel);
		panelData1.add(modelDir);
		modelDir.setText(ContexteGlobal.getResourceString("modelDir"));
		panelData1.add(modelDirButton);
		modelDirButton.setActionCommand("modelDir");
		modelDirButton.addActionListener(this);
		
		panelData1.add(baseDirLabel);
		panelData1.add(baseDir);
		baseDir.setText(ContexteGlobal.getResourceString("baseDir"));
		panelData1.add(baseDirButton);
		baseDirButton.setActionCommand("baseDir");
		baseDirButton.addActionListener(this);
		
		panelData1.add(pcMusicDirLabel);
		panelData1.add(pcMusicDir);
		pcMusicDir.setText(ContexteGlobal.getResourceString("pcTangoArgentinDir"));
		panelData1.add(pcMusicDirButton);
		pcMusicDirButton.setActionCommand("pcMusicDir");
		pcMusicDirButton.addActionListener(this);
		
		panelData1.add(macMusicDirLabel);
		panelData1.add(macMusicDir);
		macMusicDir.setText(ContexteGlobal.getResourceString("macTangoArgentinDir"));
		panelData1.add(macMusicDirButton);
		macMusicDirButton.setActionCommand("macMusicDir");
		macMusicDirButton.addActionListener(this);
		
        // Lay out the panel.
        SpringUtilities.makeCompactGrid(panelData1,
                                        4, 3, 		 //rows, cols
                                        10, 30,      //initX, initY
                                        10, 10);       //xPad, yPad

        	//  construct tab panels
        tabbedPane.addTab(DATAPANEL1, panelData1);
        tabbedPane.addTab(DATAPANEL2, panelData2);
		panel.add(tabbedPane,BorderLayout.CENTER);
		
			//  boutons
		
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		panelButton.add(cancelButton);
		
		saveButton.setActionCommand("ok");
		saveButton.addActionListener(this);
		panelButton.add(saveButton);
		
		panel.add(panelButton,BorderLayout.SOUTH);
		
			//	test du système
		
		if ( PlaylistPanel.system.startsWith("Windows") )  {
			macMusicDirButton.setEnabled(false);
		} else {
			pcMusicDirButton.setEnabled(false);
		}
		
		pack();		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);		
	}
}
