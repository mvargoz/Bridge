package playlist;

/**
 * 		Cr�ation de playlist de milonga
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import winApp.ContexteGlobal;

public class PlaylistMilonga {

	//	constantes
		
	private static String playlistExt = PlaylistPanel.playlistExt;
	private static String playlistDir = PlaylistPanel.playlistDir;
	private static String cumparsita = playlistDir + "/" + ContexteGlobal.getResourceString("playlistCumparsita") + playlistExt;

	// liste des tandas
	
	private File[] listFile;	
	private class dataTanda  {
		private String nameTanda;
		private int used;		
	}
	private ArrayList<dataTanda> tandaList = new ArrayList<dataTanda>();
	
	//	Cortinas
	
	private ArrayList<String> cortinaList = new ArrayList<String>();
	private ArrayList<String> cortinaFile = new ArrayList<String>();

	//	data
	
	private int nbBal = 0;	//nombre de milongas cr�es

	/**
	 * constructeur
	 */
	public PlaylistMilonga() {

		// liste des tandas

		File dir = new File(playlistDir);
		listFile = dir.listFiles(new FilenameFilter() { 
	         public boolean accept(File dir, String filename)
	              { return ( filename.startsWith("Milonga") || filename.startsWith("Tango") || filename.startsWith("Vals") )
	            		  && filename.endsWith(playlistExt); }
		} );
		for ( File f : listFile )  {
			dataTanda dt = new dataTanda();
			String nm = f.getName();
			dt.nameTanda = nm.substring(0,nm.length()-4);
			dt.used = 0;
			tandaList.add(dt);
		}
		
	}

	/**
	 * Cr�ation d'une playlist de milonga
	 * @param nmModel
	 * @param model
	 * @param nbTango
	 * @param pCortina
	 * @return
	 */
	public String getMilonga(String nmModel, String[] model, int nbTango, String pCortina)	{
		nbBal++;	//	nombre de milongas g�n�r�es durant la cession
		//	nom fichier playlist
		String nmPlaylist = playlistDir + "/"
				+ ContexteGlobal.getResourceString("playlistGenericName")
				+ "-" + nmModel
				+ Integer.toString(nbBal)
				+ "-" + Integer.toString(nbTango)
				+ playlistExt;
		getCortinas(pCortina);
		System.setProperty( "file.encoding","UTF-8"); // ne fonctionne pas sans �a ?

		try {
			OutputStreamWriter outstr = new OutputStreamWriter(new FileOutputStream(nmPlaylist),"UTF8");
			BufferedWriter writePlaylist = new BufferedWriter(outstr);

			writePlaylist.write("#EXTM3U\n");
			
			for ( String line: model )	{
				line = line.trim();
				if ( line.length() == 0 )
					continue;
				
				// cortina (facultative)
				
				if ( cortinaList.size() > 0 )  {
					int	noCortina = (int) Math.floor(Math.random()*cortinaList.size());
					writePlaylist.write(cortinaList.get(noCortina)+",Cortina\n");
					writePlaylist.write(cortinaFile.get(noCortina)+'\n');
					cortinaList.remove(noCortina);
					cortinaFile.remove(noCortina);
				}				
				//  tanda
				
				int iSpace = line.indexOf(' ');
			    if ( iSpace > 0 )  {
				    String type = line.substring(0, iSpace);
				    String name = line.substring(iSpace+1);
				    String nomTanda = searchTanda(type, name);
				    if ( nomTanda == null )  {
						JOptionPane.showMessageDialog(((PlaylistPanel) winApp.ContexteGlobal.frame.panel),
								ContexteGlobal.getResourceString("messErreurSyntaxe") + line,
								ContexteGlobal.getResourceString("messPlaylistMilonga"),
								JOptionPane.ERROR_MESSAGE);
						return null;				    	
				    }
				    String pathTanda = playlistDir + "/" + nomTanda + playlistExt;
					if ( type.equalsIgnoreCase("Tango") )
						copyPlaylist(pathTanda,writePlaylist,nomTanda,nbTango);
					else if ( nbTango <= 3 )						
						copyPlaylist(pathTanda,writePlaylist,nomTanda,nbTango);
					else
						copyPlaylist(pathTanda,writePlaylist,nomTanda,3);
				    
				} else {
					JOptionPane.showMessageDialog(((PlaylistPanel) winApp.ContexteGlobal.frame.panel),
							ContexteGlobal.getResourceString("messErreurSyntaxe") + line,
							ContexteGlobal.getResourceString("messPlaylistMilonga"),
							JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
			
			//  cumparsita
			
			copyPlaylist(cumparsita,writePlaylist,"Cumparsita",2);
			
			writePlaylist.close();
			JOptionPane.showMessageDialog(((PlaylistPanel) winApp.ContexteGlobal.frame.panel),
					nmPlaylist,
					ContexteGlobal.getResourceString("messPlaylistMilonga") + " OK",
					JOptionPane.INFORMATION_MESSAGE);
			return nmPlaylist;
			
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(((PlaylistPanel) winApp.ContexteGlobal.frame.panel),
					ContexteGlobal.getResourceString("messErrSave"),
					ContexteGlobal.getResourceString("messPlaylistMilonga"),
					JOptionPane.ERROR_MESSAGE);
			System.out.println("Erreur IO Playlist : " + ex);
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Recherche une tanda qui corresponde au :
	 * @param type : Tango, Milonga ou Vals
	 * @param name : suite de noms d'artiste s�par�s par des /
	 *  les tandas s�lectionn�es commencent par type + nom d'artiste,
	 *  une tanda est tir�e au sort parmi celles-ci
	 * @return nom de tanda
	 */
	private String searchTanda(String type, String name)  {
	    String [] nameMusic = name.split("/");
		ArrayList<Integer> ls = new ArrayList<Integer>();
		
			// examen de la liste des tandas disponibles
		
		for ( int i=0; i < tandaList.size(); i++ )  {
			// la tanda correspond au crit�re (d�but nom identique parmi la liste des noms donn�s)
			for ( int j=0; j < nameMusic.length; j++ )  {				
				if ( tandaList.get(i).nameTanda.startsWith(type + ' ' + nameMusic[j]) )  {
					// plus une tanda a �t� d�j� utilis�e, moins elle a des chances d'�tre s�lectionn�e
					for ( int k = tandaList.get(i).used; k <= nbBal; k++ )
						ls.add(i);
				}
			}
		}
	    if ( ls.size() == 0 )  {
			JOptionPane.showMessageDialog(((PlaylistPanel) winApp.ContexteGlobal.frame.panel),
					ContexteGlobal.getResourceString("messErreurTanda") + type + ' ' + name,
					ContexteGlobal.getResourceString("messPlaylistMilonga"),
					JOptionPane.ERROR_MESSAGE);
			return null;

	    } else {
			int lsi = (int) Math.floor(Math.random()*ls.size());
			int s = ls.get(lsi);
	    	dataTanda dt = tandaList.get(s);
	    	dt.used += 1;
	    	tandaList.set(s,dt);
			return dt.nameTanda;		
	    }
	}

	
	/**
	 * chargement des cortinas
	 * @param pCortina
	 */
	private void getCortinas(String pCortina)  {
		
		cortinaList.clear();
		cortinaFile.clear();
		if (pCortina == null || pCortina.isEmpty())
			return;
		String nmCortina = playlistDir + "/" + pCortina + playlistExt;
		String nextCortina;
		
		try {
			BufferedReader cortina = new BufferedReader(new InputStreamReader(new FileInputStream(nmCortina),"UTF8"));
			nextCortina = cortina.readLine();
			if ( nextCortina.startsWith("#EXTM3U") )		// ignore #EXTM3U
				nextCortina = cortina.readLine();
			while (nextCortina != null) {
				if ( nextCortina.startsWith("#EXTINF") )	{
					cortinaList.add(nextCortina);
					nextCortina = cortina.readLine();					
				} else {
					cortinaList.add("");
				}
				cortinaFile.add(nextCortina);
				nextCortina = cortina.readLine();
			}
			cortina.close();

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(((PlaylistPanel) winApp.ContexteGlobal.frame.panel),
					ContexteGlobal.getResourceString("messErrRead"),
					ContexteGlobal.getResourceString("messCortina"),
					JOptionPane.ERROR_MESSAGE);
			System.out.println("Erreur IO Cortina : " + ex);
			ex.printStackTrace();
		}
		
	}

	/**
	 * copie du nombre de morceaux demand�s dans la playlist
	 * @param in
	 * @param out
	 * @param nomTanda
	 * @param nbSongs
	 * @return
	 */
	private boolean copyPlaylist(String in, BufferedWriter out, String nomTanda, int nbSongs)  {
		
		ArrayList<String> songList = new ArrayList<String>();
		ArrayList<String> songFile = new ArrayList<String>();
		String line1, line2;

		try {
			BufferedReader incopy = new BufferedReader(new InputStreamReader(new FileInputStream(in),"UTF8"));
			incopy.readLine();	 // ignore #EXTM3U
			line1 = incopy.readLine();
			while (line1 != null) {
				songList.add(line1);
				line2 = incopy.readLine();
				songFile.add(line2);
				line1 = incopy.readLine();
			}
				// si la longueur de la liste est sup�rieure au nombre de morceaux demand�s
				// on les prend en s�quence � partir d'un choisi au hasard
			int sizeSongList = songList.size();
			int noSong = (int) Math.floor(Math.random()*sizeSongList);			
			for ( int i=0; i < Math.min(nbSongs, sizeSongList); i++ )  {
				int j = noSong + i;
				if ( j >= sizeSongList )
					j -= sizeSongList;
				//	suppression des , dans le titre du tango
				int iv = songList.get(j).indexOf(',');
				String extinf = songList.get(j).substring(0,iv) + ','
						+ songList.get(j).substring(iv+1).replace(',',' ');
				out.write(extinf + ',' + nomTanda + '\n');
				out.write(songFile.get(j) + '\n');
			}
			incopy.close();
			
		} catch (Exception ex)	{
			JOptionPane.showMessageDialog(((PlaylistPanel) winApp.ContexteGlobal.frame.panel),
					ContexteGlobal.getResourceString("messErrCopy"),
					ContexteGlobal.getResourceString("messPlaylistMilonga"),
					JOptionPane.ERROR_MESSAGE);
			System.out.println("copy error : " + ex);
			ex.printStackTrace();
			return false;
		}
		return true;
	}
}
