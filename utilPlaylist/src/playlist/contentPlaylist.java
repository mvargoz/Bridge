package playlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class contentPlaylist {
	
	static File dir;		// directory playlist
	static File fileOut;	// liste des musiques
	static File fileOutAlbum;  // liste des albums utilisés
	static String prefix;	// sélection des playlist par le début de leur nom
	
	static BufferedReader in = null;
	
	static FileWriter out, outAlbum;
	
	static String line;  	// ligne courante
	
	static File[] listFile;

	static int codeRetour = 0;

	public static void main(String[] args)
	{

		if (args.length != 3)
			usage();

		dir = new File(args[0]);
		prefix = args[1];
		fileOut = new File(args[2]);
		fileOutAlbum = new File("album" + args[2]);		
		
		listFile = dir.listFiles(new FilenameFilter() { 
	         public boolean accept(File dir, String filename)
	              { return filename.startsWith(prefix); }
		} );

		try  {
			out = new FileWriter(fileOut);
			outAlbum = new FileWriter(fileOutAlbum);
			for ( File f : listFile )  {
				System.out.println("fichier : " + f);
				musicPlaylist(f);
				out.write('\n');
			}
			out.close();
			outAlbum.close();
		} catch (Exception ex) {
			System.out.println("Erreur écriture : " + ex);
			ex.printStackTrace();
		}

		if (codeRetour > 0) {
			System.out.println("*** Erreurs détectées ***");
		} else {
			System.out.println("*** Opération effectuée avec succès ***");
		}

		System.exit(codeRetour);		
	}
		
	public static void musicPlaylist(File fileIn)  {

		try  {
			in = new BufferedReader(new FileReader(fileIn));
			line = in.readLine();
			while (line != null)
			{
				if ( line.startsWith("#EXTM3U")  )  {					
				}
				else if ( line.startsWith("#EXTINF")  )  {
					System.out.println(line);
					out.write(line.split(",",2)[1] + " (" + fileIn.getName() + ")\n");
				}
				else {
					File fmusic = new File(line);
					String album = fmusic.getParent();
					outAlbum.write(album + "\n");
				}
				line = in.readLine();			
			}
			in.close();
		} catch (Exception ex) {
			System.out.println("Erreur lecture : " + ex);
			ex.printStackTrace();
			codeRetour = 20;
		}
	}
	
		// Erreurs de paramètres

	static void usage()
	{
		System.out
				.println("Usage: java contentPlaylist <directory> <filtre> <sortie liste>");
		System.exit(10);
	}



}
