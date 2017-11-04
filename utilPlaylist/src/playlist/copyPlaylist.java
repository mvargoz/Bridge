package playlist;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardCopyOption.*;

/*	Copie des fichiers d'une playlist avec numérotation en tête pour appareil ne supportant pas les playlists  */

public class copyPlaylist {
	
	static File fileIn;		// playlist
	
	static BufferedReader in = null;
	
	static String dirout;	// répertoire de sortie	
	
	static String line;  	// ligne courante

	static int numligne = 1;	// numérotation des morceaux

	static int codeRetour = 0;

	public static void main(String[] args)
	{

		if (args.length != 2 && args.length != 3 )
			usage();
		if (args.length == 3)
			numligne = Integer.parseInt(args[2]);
		fileIn = new File(args[0]);
		dirout = args[1];
		
		buildPlaylist();
		
		System.exit(codeRetour);
	}
		
	public static void buildPlaylist()  {

		try
		{
			in = new BufferedReader(new FileReader(fileIn));
			line = in.readLine();
			while (line != null)
			{
				if ( !line.startsWith("#")  )  {
					String numerotation = "";
					if ( numligne != 0 )
						numerotation = String.format("%03d", numligne++) + "_";
					String dest = dirout + "/" + numerotation + line.substring(line.lastIndexOf('\\')+1);
					Files.copy(Paths.get(line), Paths.get(dest), REPLACE_EXISTING);
					System.out.println("copy " + line + " to " + dest + " OK");
				}
				line = in.readLine();			
			}
			in.close();
		} catch (Exception ex) {
			System.out.println("Erreur lecture : " + ex);
			ex.printStackTrace();
			codeRetour = 20;
		}

		if (codeRetour > 0) {
			System.out.println("*** Erreurs détectées ***");
		} else	{
			System.out.println("*** Opération effectuée avec succès ***");
		}

		System.exit(codeRetour);
	}

		// Erreurs de paramètres

	static void usage()
	{
		System.out.println("Usage: java copyPlaylist <playlist> <output directory>");
		System.exit(10);
	}

}
