package playlist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class createBal {
	private static File dir;		// directory playlist
	private static File fileIn;		// modèle
	private static String prefixOut; // préfixe bals
	private static File fileOut;	// bals
	private static int nBal = 1; 	// nombre de bals à générer
	
	// liste des tandas
	
	private static File[] listFile;	
	private static class dataTanda  {
		public String nameTanda;
		public int used;		
	}
	
	private static ArrayList<dataTanda> tandaList = new ArrayList<dataTanda>();
	
	private static int codeRetour = 0;
	
	public static void main(String[] args)
	{
		if (args.length == 4)  {
			nBal = Integer.parseInt(args[3]);
		}
		if (args.length >= 2)  {
			dir = new File(args[0]);
			fileIn = new File(args[1]);
			prefixOut = args[2];			
		} else
			usage();

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
			dt.used = 0;
			tandaList.add(dt);
		}
		
		for ( int numBal=1; numBal <= nBal; numBal++ )
			createBalfromModel(numBal);	
		
		System.exit(codeRetour);
	}

	// assemblage de la playlist
	
	private static void createBalfromModel(int numBal)  {
		
		BufferedReader in = null;
		String line;
		fileOut = new File(prefixOut+Integer.toString(numBal)+".txt");
		FileWriter out;

		try
		{
			out = new FileWriter(fileOut);
			in = new BufferedReader(new FileReader(fileIn));
			line = in.readLine();
			while (line != null)
			{
				System.out.println(":" + line);
				int iSpace = line.indexOf(' ');
			    if ( iSpace > 0 )  {
				    String type = line.substring(0, iSpace);
				    String name = line.substring(iSpace+1);
				    ArrayList<Integer> ls = searchTanda(type, name);
				    if ( ls.size() == 0 )  {
				    	out.write(fileIn.getName() + " : tanda introuvable : '" + type + ' ' + name + "'\n");
				    } else {
						int lsi = (int) Math.floor(Math.random()*ls.size());
						int s = ls.get(lsi);
				    	dataTanda dt = tandaList.get(s);
				    	dt.used += 1;
				    	tandaList.set(s,dt);
				    	out.write(dt.nameTanda+'\n');
				    }
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
	
		if (codeRetour > 0)	{
			System.out.println("*** Erreurs détectées ***");
		} else {
			System.out.println("*** Opération effectuée avec succès ***");
		}
	}

	//  recherche des tandas qui correspondent :
	//  type : Tango, Milonga ou Vals
	//  name : suite de noms d'artiste séparés par des /
	//  les tandas sélectionnées commencent par type + nom d'artiste
	
	public static ArrayList<Integer> searchTanda(String type, String name)  {
	    StringTokenizer st = new StringTokenizer(name,"/");
	    ArrayList<String> nameMusic = new ArrayList<String>();
	    while ( st.hasMoreTokens() )  {
	    	nameMusic.add(st.nextToken());
	    }
		ArrayList<Integer> ls = new ArrayList<Integer>();
		
			// examen de la liste des tandas disponibles
		
		for ( int i=0; i < tandaList.size(); i++ )  {
			// la tanda correspond au critère (début nom identique parmi la liste des noms donnés)
			for ( int j=0; j < nameMusic.size(); j++ )  {				
				if ( tandaList.get(i).nameTanda.startsWith(type + ' ' + nameMusic.get(j)) )
					// plus une tanda a été déjà utilisée, moins elle a des chances d'être sélectionnée
					for ( int k = tandaList.get(i).used; k < nBal; k++ )
						ls.add(i);
			}
		}
		return ls;
		
	}
	
	// Erreur de paramètres

	static private void usage() {
		System.out.println("Usage: java createBal <directory> <input $BalTangoModel.txt> <output BalTango1.txt> [<nombre de bals>]");
		System.exit(10);
	}


}
