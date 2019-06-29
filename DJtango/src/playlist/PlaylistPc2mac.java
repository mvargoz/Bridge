package playlist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import winApp.ContexteGlobal;

/*		conversion des playlists au format mac
 * 		création d'un script pour copier les albums concernés
 * 			 sur mac depuis la sauvegarde
 */

public class PlaylistPc2mac extends SwingWorker<String, Object> {

		//	répertoire des modèles
	private static String modelDir = PlaylistPanel.modelDir;	
		//	répertoire des playlists Windows sur PC
	private static String baseDir = ContexteGlobal.getResourceString("baseDir");
	private static String playlistDir = baseDir + "/" + ContexteGlobal.getResourceString("playlistDir");
	private static String systemSepDir = "\\";
		//	extension playlist
	private static String playlistExt = PlaylistPanel.playlistExt;
		//	répertoire des playlists mac sur PC
	private static String playlistDirMac = ContexteGlobal.getResourceString("baseDir")
			+ "/"  + ContexteGlobal.getResourceString("playlistMacDir");
		//	répertoire de la musique sur PC
	private static String pcDir = ContexteGlobal.getResourceString("pcTangoArgentinDir");
		//	répertoire de la musique sur mac
	private static String macDir = ContexteGlobal.getResourceString("macTangoArgentinDir");
		//	répertoire de la musique sur la sauvegarde
	private static String macBackupDir = ContexteGlobal.getResourceString("macBackupDir");
		//	commandes UNIX
	private static String mkdirMac = ContexteGlobal.getResourceString("mkdirMac") + " ";
	private static String copyMac = ContexteGlobal.getResourceString("copyMac") + " ";
	private static String shellCopy = modelDir + "/" + ContexteGlobal.getResourceString("shellCopy");

	// liste des fichiers musiques non accessibles

	private static HashSet<String> errMusic = new HashSet<String>();

	// liste des albums référencés

	private static HashSet<String> album = new HashSet<String>();
	
	// message
	
	private String mess = "";

	//	programme

	public PlaylistPc2mac() {
	}

	/*
	 * Main task. Executed in background thread.
	 */
	@Override
	public String doInBackground() {

		if ( !PlaylistPanel.system.startsWith("Windows") )  {
			systemSepDir = "/";
		}
		double progress = 0;
		setProgress((int) progress);

		File dirIn = new File(playlistDir);
		File dirOut = new File(playlistDirMac);
		File fileCopy = new File(shellCopy); 			

		//  vidage du répertoire objet

		File[] listFileOut = dirOut.listFiles();
		for (File f : listFileOut) {
			f.delete();
		}
		
		//  vidage liste albums

		errMusic.clear();
		album.clear();

		//	traitement des playlists

		File[] listFile = dirIn.listFiles(new FileFilter() {
			public boolean accept(File dir) {
				return dir.isFile() && dir.getName().endsWith(playlistExt);
			}
		});
		if ( listFile.length == 0 )  {
			return "Aucune playlist à convertir!";			
		}
		double incProgress = 100 / listFile.length;

		for (File f : listFile) {
			File fileOut = new File(playlistDirMac + systemSepDir + f.getName());
			convertPlaylist(f, fileOut);
			progress += incProgress;
			setProgress(Math.min(100,(int) Math.round(progress)));
			System.out.println("Conversion " + f.getName() + " OK");
		}
		mess += "playlists converties : " +  listFile.length + "\n";

		//	création du shell de copie des albums sur mac depuis la sauvegarde

		createCopyFile(fileCopy);
		mess += "création shell de copie album : " + shellCopy;

		return mess;
	}

	/*
	 * Executed in event dispatching thread
	 */
	@Override
	public void done() {
		setProgress(100);
	}

	// conversion d'une playlist

	private void convertPlaylist(File fileIn, File fileOut)  {

		BufferedReader in;		
		String line;
		BufferedWriter out;

		try
		{
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), "UTF8"));
			in = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), "UTF8"));
			line = in.readLine();
			out.write("#EXTM3U\n");
			line = in.readLine();
			while (line != null)  {
				line = line.trim();
				if ( line.startsWith("#EXTINF:")  )  {
					out.write(line+'\n');
				} else {
					//	conversion path musique pour mac
					String macFile = macDir + albumMac(line);
					//  test existence fichier musique
					if ( systemSepDir == "/" )	{
						if ( !(new File(macFile).exists()) )
							errMusic.add(macFile + " in " + fileIn.getName());
					} else {
						if ( !(new File(line).exists()) )
							errMusic.add(line + " in " + fileIn.getName());
					}
					out.write(macFile + '\n');
					//	album
					album.add(line.substring(0, line.lastIndexOf('\\')));
				}				
				line = in.readLine();			
			}
			in.close();
			out.close();
		} catch (Exception ex) {
//			JOptionPane.showMessageDialog(ContexteGlobal.frame,
//					ContexteGlobal.getResourceString("messErrIO"),
//					ContexteGlobal.getResourceString("messConvFileTanda"),
//					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}

	}

	//	extrait le nom de l'album avec sa hiérarchie et le convertit pour Unix
	//  remplace \ par /

	private String albumMac(String path)  {
		String nomFile = path.substring(pcDir.length());
		nomFile = nomFile.replace('\\', '/');
		return nomFile;		
	}

	//	création du fichier de commande pour copier les albums
	//	sur mac depuis le Backup

	public void createCopyFile(File fileCopy)  {
		String [] albumList = album.toArray(new String[0]);
		Arrays.sort(albumList);
		BufferedWriter out;
		try
		{
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileCopy), "UTF8"));
			for( String errPath: errMusic )  {
				out.write("not find: " + errPath);
				out.newLine();
			}
			out.write("cd \"" + macBackupDir +  "\"\n");
			for( String albumPath: albumList )  {
				String albumDir = albumMac(albumPath);
				//  protection caractères spéciaux: remplace $ par \$ et ! par \!
				albumDir = albumDir.replace("$", "\\$");
				albumDir = albumDir.replace("!", "\\!");
				//	création répertoire album avec arborescence
				out.write(mkdirMac + "\"" + macDir + albumDir + "\"\n");
				//	copie de l'album
				out.write(copyMac + "\"" + albumDir + "/\" \"" + macDir + albumDir + "\"\n");				
			}
			out.close();
		} catch (Exception ex) {
//			JOptionPane.showMessageDialog(ContexteGlobal.frame,
//					ContexteGlobal.getResourceString("messErrSave"),
//					ContexteGlobal.getResourceString("messShellCopy"),
//					JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

}
