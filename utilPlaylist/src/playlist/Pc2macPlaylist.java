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
import java.util.Iterator;

/*		conversion des playlists au format mac
 * 		création d'un script pour copier les albums concernés
 * 			 sur mac depuis la sauvegarde
 */

public class Pc2macPlaylist {
	
	static File dirIn;
	static File fileCopy;
	static String dirOut;
	
		// liste des albums référencés
	
	static HashSet<String> album = new HashSet<String>();

		//	constantes

		//	répertoire de la musique sur PC
	static String pcDir = "W:";
		//	répertoire de la musique sur mac
	static String macDir = "/Users/michelvargoz/Music/";
		//	répertoire de la musique sur la sauvegarde
	static String macBackupDir = "/Volumes/Backup";
		//	extension playlist
	static String ext = ".m3u";
		//	commandes UNIX
	static String mkdirMac = "mkdir -p ";
	static String copyMac = "cp -R -f ";
		//	code retour
	static int codeRetour = 0;
	
		//	programme

	public static void main(String[] args) {

		if (args.length == 3)  {
			dirIn = new File(args[0]);
			dirOut = args[1];
			fileCopy = new File(args[2]); 			
		} else {
			usage();
		}
		
			//	traitement des playlists
		
		File[] listFile = dirIn.listFiles(new FileFilter() {
			public boolean accept(File dir) {
				return dir.isFile() && dir.getName().endsWith(ext);
			}
		});
		for (File f : listFile) {
			File fileOut = new File(dirOut + "\\Playlist\\" + f.getName());
			convertPlaylist(f, fileOut);
		}

			//	copie des albums
		
		createCopyFile();

			// 	fin
		
		if (codeRetour > 0)	{
			System.out.println("*** Erreurs détectées ***");
		} else {
			System.out.println("*** Opération effectuée avec succès ***");
		}
		
		System.exit(codeRetour);

	}

		// conversion d'une playlist
	
	private static void convertPlaylist(File fileIn, File fileOut)  {
		
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
			while (line != null)
			{
				line = line.trim();
				System.out.println(":" + line);
				if ( line.startsWith("#EXTINF:")  )  {
					out.write(line+'\n');
				} else {
						//	conversion path musique pour mac
					out.write(macDir + convAlbumMac(line) + '\n');
						//	album
					album.add(line.substring(0, line.lastIndexOf('\\')));
				}				
				line = in.readLine();			
			}
			in.close();
			out.close();
		} catch (Exception ex) {
			System.out.println("Erreur I/O : " + ex);
			ex.printStackTrace();
			codeRetour = 20;
		}

	}

		//	extrait le nom de l'album avec sa hiérarchie et le convertit pour Unix
		//  remplace \ par /

	private static String convAlbumMac(String path)  {
		String nomFile = path.substring(pcDir.length() + 1);
		nomFile = nomFile.replace('\\', '/');
		return nomFile;		
	}
	
		//	création du fichier de commande pour copier les albums
		//	sur mac depuis le Backup

	public static void createCopyFile()  {
		String [] albumList = album.toArray(new String[0]);
		Arrays.sort(albumList);
		BufferedWriter out;
		try
		{
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileCopy), "UTF8"));
			out.write("cd \"" + macBackupDir +  "\"\n");
			for( String albumPath: albumList )  {
				String albumDir = convAlbumMac(albumPath);
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
			System.out.println("Erreur I/O : " + ex);
			ex.printStackTrace();
			codeRetour = 20;
		}
	}
	
		// Erreur de paramètres
	
	private static void usage() {
		System.out.println("Usage: java Pc2macPlaylist <input dir> <output dir> <fichier de commande copy>");
		System.exit(10);
	}

}
