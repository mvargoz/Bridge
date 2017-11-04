package playlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.lang.Math;
import java.util.*;

	/*	Crée une playlist à partir d'un fichier contenant une liste de tandas
	 *  Paramètres :
	 *  	nom du fichier contenant la liste des playlist des tandas (sans suffixe)
	 *  	nom de la playlist en sortie
	 *  	nombre de tangos par tanda (4 par défaut)
	 *  	nom du fichier des cortinas (Tango Cortina.m3u par défaut)
	 *  	nom du fichier des cumparsitas (Tango Cumparsita.m3u par défaut)
	 *  Le programme choisit 3 ou 4 morceaux dans la playlist de la tanda
	 *  Il ajoute automatiquement les cortinas
	 *  	Si l'on donne une playlist de cortina existante elles seront diffusées dans l'ordre
	 *  	sinon le programme prend la playlist par défaut en mode aléatoire
	 *  Il ajoute une tanda de cumparsita à la fin
	 */

public class createPlaylist {
	
	static File fileIn, fileOut, fileCortina;
	
	static int nBTangos = 4; // nombre de tangos par tanda
	static String cumparsita = "Tango Cumparsita.m3u";
	static String cortina = "Tango Cortina.m3u";
	static boolean randomCortina = true;
	static int noCortina = 0;
	
	static int codeRetour = 0;
	
	static ArrayList<String> cortinaList = new ArrayList<String>();
	static ArrayList<String> cortinaFile = new ArrayList<String>();

		// programme batch

	public static void main(String[] args)
	{

		if (args.length >= 3)  {
			nBTangos = Integer.parseInt(args[2]);
		}
		if (args.length >= 4)  {
			String cortinaTest = args[3];
			fileCortina = new File(cortinaTest);
			if ( fileCortina.exists() ) {
				cortina = cortinaTest;
				randomCortina = false;				
			}
		}
		if (args.length >= 5)  {
			cumparsita = args[4];
		}
		if (args.length >= 2)  {
			fileIn = new File(args[0]);
			fileOut = new File(args[1]);
		} else
			usage();
		
		getCortinas();
		
		assemblePlaylist();
		
		System.exit(codeRetour);
	}

	// chargement des cortinas
	
	private static void getCortinas()  {
		
		fileCortina = new File(cortina);
		String nextCortina1, nextCortina2;
		BufferedReader cortina = null;
		
		try
		{
			cortina = new BufferedReader(new FileReader(fileCortina));
			cortina.readLine();  // ignore #EXTM3U
			nextCortina1 = cortina.readLine();
			while (nextCortina1 != null)
			{
				cortinaList.add(nextCortina1);
				nextCortina2 = cortina.readLine();
				cortinaFile.add(nextCortina2);
				nextCortina1 = cortina.readLine();
			}
			cortina.close();

		} catch (Exception ex) {
			System.out.println("Erreur I/O : " + ex);
			ex.printStackTrace();
			codeRetour = 20;
		}
		
	}

	// assemblage de la playlist
	
	private static void assemblePlaylist()  {
		
		BufferedReader in = null;
		String line;
		FileWriter out;

		try
		{
			out = new FileWriter(fileOut);
			out.write("#EXTM3U\n");
			in = new BufferedReader(new FileReader(fileIn));
			line = in.readLine();
			while (line != null)
			{
				System.out.println(":" + line);
				if ( line.startsWith("#EXTINF:")  )  {
					out.write(line+'\n');
					line = in.readLine().trim();
					out.write(line+'\n');
				} else if ( !line.equals("") && !line.startsWith("#EXT"))  {
				     StringTokenizer st = new StringTokenizer(line,",");
				     String playlist = st.nextToken();
				     int noTango = 0;
				     if (st.hasMoreTokens()) {
				         noTango = Integer.parseInt(st.nextToken()) - 1;
				     }
					if ( !playlist.endsWith(".m3u") )
						playlist += ".m3u";
					
					// tanda
					
					if ( playlist.startsWith("Tango") )
						copyFile(new FileReader(playlist),out,nBTangos,noTango);
					else						
						copyFile(new FileReader(playlist),out,3,noTango);
					
					// cortina
					
					if ( randomCortina )  {
						noCortina = (int) Math.floor(Math.random()*cortinaList.size());
						out.write(cortinaList.get(noCortina)+'\n');
						out.write(cortinaFile.get(noCortina)+'\n');
						cortinaList.remove(noCortina);
						cortinaFile.remove(noCortina);
					}  else  {
						out.write(cortinaList.get(noCortina)+'\n');
						out.write(cortinaFile.get(noCortina)+'\n');
						noCortina++;
						if ( noCortina >= cortinaList.size() )  {
							System.out.println("*** Il manque des cortinas ***");
							noCortina = 0;
						}
					}
						
				}
					
				line = in.readLine();			
			}
			in.close();
			copyFile(new FileReader(cumparsita),out,3,0);
			out.close();
		} catch (Exception ex) {
			System.out.println("Erreur I/O : " + ex);
			ex.printStackTrace();
			codeRetour = 20;
		}

		if (codeRetour > 0)	{
			System.out.println("*** Erreurs détectées ***");
		} else {
			System.out.println("*** Opération effectuée avec succès ***");
		}
	}

	// copie du nombre de morceaux demandés dans la playlist
	
	private static boolean copyFile(FileReader in, FileWriter out, int nbSongs, int noSong){
		
		ArrayList<String> songList = new ArrayList<String>();
		ArrayList<String> songFile = new ArrayList<String>();
		String line1, line2;
		BufferedReader incopy = new BufferedReader(in);

		try {
			incopy.readLine();	 // ignore #EXTM3U
			line1 = incopy.readLine();
			while (line1 != null) {
				songList.add(line1);
				line2 = incopy.readLine();
				songFile.add(line2);
				line1 = incopy.readLine();
			}
				// si la longueur de la liste est supérieure au nombre de morceaux demandés
				// on les prend en séquence à partir d'un choisi au hasard
			int sizeSongList = songList.size();
			if ( noSong == 0 && sizeSongList > nbSongs )
				noSong = (int) Math.floor(Math.random()*sizeSongList);			
			for ( int i=0; i < nbSongs; i++)  {
				int j = noSong + i;
				if ( j >= sizeSongList )
					j -= sizeSongList;
				out.write(songList.get(j)+'\n');
				out.write(songFile.get(j)+'\n');
			}
			
		} catch (Exception ex)	{
			System.out.println("copy error : " + ex);
			ex.printStackTrace();
			return false;
		}
		return true;
	}

		// Erreur de paramètres

	static private void usage() {
		System.out.println("Usage: java createPlaylist <input playlist.txt> <output playlist.m3u> [<nombre max tangos/tanda> <cortina playlist.m3u> <cumparsita playlist.m3u> ]");
		System.exit(10);
	}

}
