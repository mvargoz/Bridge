package bridgeIHM;
/*
 * 		saisie des options de l'application
*/

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

public class DialogueOption  extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	//	frame

private Frame parentFrame;
private BridgePanel parentPanel;

//	panel principal avec BorderLayout hgap vgap
	
private JPanel panel = new JPanel(new BorderLayout(10,20));

	//	panel central

final static String DATAPANEL1 = "Répertoires";
final static String DATAPANEL2 = "Paramètres";
private JTabbedPane tabbedPane = new JTabbedPane();
private JPanel panelData1 = new JPanel(new SpringLayout());
private JPanel panelData2 = new JPanel(new SpringLayout());

	//	saisie répertoires

private JLabel baseDirLabel = new JLabel(ContexteGlobal.getResourceString("baseDirLabel"));
private JTextField baseDir = new JTextField(50);
private JButton baseDirButton = new JButton(new ImageIcon(ContexteGlobal.getResource("folderImage")));

	//	Panel Boutons action

private JPanel panelButton = new JPanel(new FlowLayout(FlowLayout.CENTER,20,20));
private JButton cancelButton = new JButton(ContexteGlobal.getResourceString("cancelButton"));
private JButton saveButton = new JButton(ContexteGlobal.getResourceString("validButton"));

	//  Action listener


	@Override
	public void actionPerformed(ActionEvent actev) {
		switch ( actev.getActionCommand() )  {
		case "baseDir":
			JFileChooser jFileChooserPlay = new JFileChooser(baseDir.getText());
			jFileChooserPlay.setDialogTitle(baseDirLabel.getText());
			jFileChooserPlay.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (jFileChooserPlay.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)  {
				baseDir.setText(jFileChooserPlay.getSelectedFile().getPath());
			}
			break;
		case "ok":
			winApp.ContexteGlobal.putProperty("baseDir", baseDir.getText());
			BridgePanel.baseDir = baseDir.getText();
			dispose();
			parentPanel.initPanelJeu();
			break;
		case "cancel":
			dispose();
			break;
		}
		
	}
	
	
	// contructeur

	public DialogueOption(BridgePanel pp)	{
		super(winApp.ContexteGlobal.frame, "Options", false);
		
			//  position
		
		parentFrame = winApp.ContexteGlobal.frame;
		parentPanel = pp;
		setLocationRelativeTo(parentFrame);
		
			//	construction du panel
			
		getContentPane().add(panel);		
		
		panelData1.add(baseDirLabel);
		panelData1.add(baseDir);
		baseDir.setText(ContexteGlobal.getResourceString("baseDir"));
		panelData1.add(baseDirButton);
		baseDirButton.setActionCommand("baseDir");
		baseDirButton.addActionListener(this);
		
	    // Lay out the panel.
	    SpringUtilities.makeCompactGrid(panelData1,
                    1, 3, 		 // number of rows, number of cols
                    10, 30,      // position initX, initY
                    10, 10);     // padding xPad, yPad
	
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
		
		pack();		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);		
	}

}
