package playlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class contentBal {
	
	static File dir;		// directory playlist
	static File fileOut;	// liste des tandas
	static String prefix;	// sélection des bals par le début de leur nom
	
	static BufferedReader in = null;
	
	static FileWriter out;
	
	static String line;  	// ligne courante
	
	static File[] listFile;
	
	static class dataTanda  {
		public String nameTanda;
		public ArrayList<String> balList;		
	}
	
	static ArrayList<dataTanda> tandaList = new ArrayList<dataTanda>();

	static int codeRetour = 0;

	public static void main(String[] args)
	{

		if (args.length != 3)
			usage();

		dir = new File(args[0]);
		prefix = args[1];
		fileOut = new File(args[2]);

		// liste des tandas
		
		listFile = dir.listFiles(new FilenameFilter() { 
	         public boolean accept(File dir, String filename)
	              { return ( filename.startsWith("Milonga") || filename.startsWith("Tango") || filename.startsWith("Vals") )
	            		  && filename.endsWith(".m3u"); }
		} );
		for ( File f : listFile )  {
			dataTanda dt = new dataTanda();
			String nm = f.getName();
			dt.nameTanda = nm.substring(0,nm.length()-4);
			dt.balList = new ArrayList<String>();
			tandaList.add(dt);
		}

		// liste des bals
		
		listFile = dir.listFiles(new FilenameFilter() { 
	         public boolean accept(File dir, String filename)
	              { return filename.startsWith(prefix) && filename.endsWith(".txt"); }
		} );

		try  {
			out = new FileWriter(fileOut);
			for ( File f : listFile )  {
				System.out.println("fichier : " + f);
				tanda(f);
			}
			
			for ( int i=0; i < tandaList.size(); i++ )  {
				dataTanda dt = tandaList.get(i);
				out.write(dt.nameTanda + " (");
				for ( int j=0; j < dt.balList.size(); j++ )  {
					out.write( ", " + dt.balList.get(j) );
				}
				out.write(")\n");
			}

			out.close();
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
	
	public static void tanda(File fileIn)  {

		try  {
			in = new BufferedReader(new FileReader(fileIn));
			line = in.readLine();
			while (line != null)
			{
				System.out.println(line);
				if ( line.startsWith("#EXTINF:")  )  {
					line = in.readLine();
				} else if ( !line.trim().equals("") && !line.startsWith("#EXT"))  {					
//					out.write(line + " (" + fileIn.getName() + ")\n");
				    StringTokenizer st = new StringTokenizer(line,",");
				    String name = st.nextToken();
				    int s = searchTanda(name);
				    if ( s == -1 )  {
				    	out.write(fileIn.getName() + " : tanda inconnue : '" + name + "'\n");
				    } else {
				    	dataTanda dt = tandaList.get(s);
				    	dt.balList.add(fileIn.getName());
				    	tandaList.set(s,dt);
				    }
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

	//  recherche tanda
	
	public static int searchTanda(String name)  {
		for ( int i=0; i < tandaList.size(); i++ )  {
			if ( tandaList.get(i).nameTanda.compareTo(name) == 0 )
				return i;
		}
		return -1;
		
	}

		// Erreurs de paramètres
	
	static void usage()
	{
		System.out
				.println("Usage: java contentPlaylist <directory> <filtre> <sortie liste>");
		System.exit(10);
	}

		

}
