package synchro;

/*		Synchronise un r�pertoire source avec un r�pertoire objet
 * Seules les actions faites sur le r�pertoire source sont r�percut�es sur l'objet (cr�ation, modif, suppression)
 * on ne modifie jamais la source
 * les sous-r�pertoires et fichiers cach�s sont ignor�s (fichiers syst�me)
 *  	ainsi que les sous-r�pertoires commen�ant par un pr�fix donn� par ignoreSource et ignoreObjet
 * les nouveaux fichiers du r�pertoire source ainsi que ceux ayant une date de modification plus r�cente sont copi�s 
 * les nouveaux sous-r�pertoires sont cr��s
 * les fichiers existant dans l'objet et pas dans la source sont supprim�s
 * les sous-r�pertoires existant dans l'objet et pas dans la source sont supprim�s avec le param�tre deleteDir sinon ils sont conserv�s
 * la comparaison ne tient pas compte de la case sur windows
*/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import winApp.ContexteGlobal;

public class Backup implements Runnable  {

		//	code retour 0 = OK
		//  10 = ligne de commande erron�e dans le main
		//  30 = erreur param�tre
		//  99 = erreur IO 
	public int codeRetour = 0;
		//	Phase : 1=v�rification, 2=ex�cution	
	public int phase = 0;
		//	trace
	public boolean trace = false;
		//	r�pertoires � synchroniser
	private Stack<String> listDir;
		//	fichiers � supprimer, cr�er, mettre � jour
	private Stack<String> listDelete, listCreate, listUpdate;
		//  r�pertoire � supprimer, cr�er
	private Stack<String> listDeleteDir, listCreateDir;	
		//	ignore majuscule / minuscule pour les noms de fichier (Windows)
	private boolean ignoreCase = false;
		// 	fichier de param�tre
	private File fParam;
		//	pipe de communication 
	private PipedOutputStream logout;
		//	thread d'ex�cution
	private Thread thSynchro;
	private volatile boolean stopThread;
	

		//		Synchronisation de r�pertoires
	
	public Backup( File pfParam )  {
		
		this.fParam = pfParam;

			//	pile des r�pertoires � synchroniser
		
		listDir = new Stack<String>();
		
			//	stockage des actions � effectuer
		
		listDeleteDir = new Stack<String>();
		listCreateDir = new Stack<String>();
		listDelete = new Stack<String>();
		listCreate = new Stack<String>();
		listUpdate = new Stack<String>();

	}

		//	ex�cution de la phase de v�rification ou de synchronisation en thread

	public void go(int pPhase, PipedOutputStream pLogout)  {		
		this.phase = pPhase;
		this.logout = pLogout;
			// lance la thread (ex�cution m�thode run)
	    thSynchro = new Thread(this);
	    stopThread = false;
	    thSynchro.start();
    
	}
	
		//	stop thread
	
	public void stop()  {
	    stopThread = true;
	}

		//	ex�cution de la thread de synchro
	
	public void run()  {
		try {
			if ( phase == 1 )	{				
				writeTrace(ContexteGlobal.getResourceString("messVerify"));
					//	test du syst�me			
				String systemName = System.getProperty("os.name");
				writeTrace(ContexteGlobal.getResourceString("messSystem") + systemName);
		        if ( systemName.startsWith("Windows") )   {
		        	ignoreCase = true;
		        	writeTrace(ContexteGlobal.getResourceString("messIgnoreCase"));
		        }
		        	//  lecture des param�tres
		        codeRetour = 0;
				getParam(fParam);			
					//	recherche des �l�ments � synchroniser			
				if ( codeRetour == 0 )
					verifSynchro();
			} else if ( phase == 2 )   {			
				writeTrace(ContexteGlobal.getResourceString("messSynchro"));
					//	ex�cution de la synchronisation			
				if ( codeRetour == 0 )
					execSynchro();			
			}
        } catch (IOException ex) {
        	writeLog("Erreur : " + ex);
			System.out.println("Erreur : " + ex);
			ex.printStackTrace();	                	
        	codeRetour = 99;
        }
		//  cloture du pipe
		try {		
			logout.close();
        } catch (IOException ex) {
			System.out.println("Erreur : " + ex);
			ex.printStackTrace();	                	
        	codeRetour = 99;
        }
	}

		//		synchronisation batch
	
	public static void main(String[] args)  {
		Backup bk = null;
		try  {		
			ContexteGlobal.init("Synchro");
	        FileWriter fileLogOut = new FileWriter(new File(ContexteGlobal.getResourceString("log")));	
	
				//	param�tres
			if (args.length < 1)
				usage();
			
			boolean test = false;
			if ( args.length > 1 && args[1].equalsIgnoreCase("test") )  {
				test = true;
			}
			
			bk = new Backup(new File(args[0]));
			bk.trace = true;

			for ( int phase=1; phase == 1 || phase == 2 && !test; phase++ )  {
					
					//   lancement thread de verif/synchro
			
			    PipedOutputStream logout = new PipedOutputStream();
				PipedInputStream  login  = new PipedInputStream();
				login.connect(logout);        
				bk.go(phase,logout);
				
					//  sortie log
				
		        int data = login.read();
		        while(data != -1){
		            System.out.print((char) data);
		            fileLogOut.write(data);
		            data = login.read();
		        }		
				login.close();			
			}
			fileLogOut.close();
		
		} catch (Exception ex)	{
			System.out.println("Erreur : " + ex);
			ex.printStackTrace();
		}


			//   message de fin
		
		if (bk.codeRetour > 0)
		{
			System.out.println(ContexteGlobal.getResourceString("messEndError"));
		} else
		{
			System.out.println(ContexteGlobal.getResourceString("messEndOK"));
		}

		System.exit(bk.codeRetour);

	}

		/*  lecture des param�tres
		 *  syn=<fichier de param�tre>
		 *  in=<r�pertoire source>
		 *  out=<r�pertoire objet>
		 *	parm=<liste des param�tres s�par�s par une virgule>
		 *			noDeleteDir	: ne pas supprimer les sous-r�pertoires de l'objet n'existant pas dans la source
		 *			ignoreSource/xxx/ : ne synchronise pas les fichiers sources commen�ant par xxx 
		 *			ignoreObjet/xxx/ : ne synchronise pas les fichiers objets commen�ant par xxx 
		 *	...
		*/
	
	public void getParam(File param) throws IOException  {
		writeTrace(ContexteGlobal.getResourceString("messReadFileSynchro"));					
		String line;  	// ligne courante
		BufferedReader in = new BufferedReader(new FileReader(param));
		int indWaitLine = 0;
		String[] waitLines = { "in", "out", "parm" };		
		line = in.readLine();
		while (line != null)  {
			line = line.trim();
			if ( line.length() > 0 )  {
				writeTrace(line);
				String[] p = line.split("=");
				
				if ( p[0].equals("syn") )  {
					getParam(new File(p[1]));
					indWaitLine = -1;
				} else if ( p[0].equals(waitLines[indWaitLine]) )  {
					if ( p.length > 1 )
						listDir.push(p[1]);
					else
						listDir.push("noparm");					
				} else {
					writeLog(ContexteGlobal.getResourceString("messErrFileSynchro") + waitLines[indWaitLine]);
					codeRetour = 30;
					return;
				}
				
					//  prochain param�tre
				
				indWaitLine++;
				if ( indWaitLine >= waitLines.length )
					indWaitLine = 0;
			}

			line = in.readLine();			
		}
		in.close();
	}
	
			//	recherche des �l�ments � synchroniser
	
	public void verifSynchro() throws IOException    {
		writeTrace(ContexteGlobal.getResourceString("messVerify"));					
			// r�pertoires
		File dirSource, dirObjet;
		String parm, dirSourceName, dirObjetName;
		int dirSourceLength, dirObjetLength;
		
		while ( !listDir.empty() )  {
			if ( stopThread )  return;
			
				//	traitement d'un r�pertoire
			
			parm = listDir.pop();
			Vector<String> ignoreSource = getIgnore("Source", parm);
			Vector<String> ignoreObjet = getIgnore("Objet", parm);
			
			dirObjetName = listDir.pop();
			dirObjetLength = dirObjetName.length();
			dirObjet = new File(dirObjetName);
			
			dirSourceName = listDir.pop();
			dirSourceLength = dirSourceName.length();
			dirSource = new File(dirSourceName);
			
			writeLog("Synchro:" + dirSourceName);					
			writeTrace("Source synchro:" + dirSourceName);					
			writeTrace("Objet synchro:" + dirObjetName);					
			
				//	liste des sous-r�pertoires sources
			
			File[] listSubDirSource = dirSource.listFiles(new FileFilter() { 
		         public boolean accept(File dir) {
		        	 if ( dir.isDirectory() && !dir.isHidden() )  {
		        		 for ( String prefix: ignoreSource )
		        		 	if ( dir.getName().startsWith(prefix) )
		        		 		return false;
		        		 return true;
		        	 }
		        	 return false;
		         }
			} );
			Arrays.sort(listSubDirSource);
			
			//	liste des sous-r�pertoires objets
			
			File[] listSubDirObjet = dirObjet.listFiles(new FileFilter() { 
		         public boolean accept(File dir) {
		        	 if ( dir.isDirectory() && !dir.isHidden() )  {
		        		 for ( String prefix: ignoreObjet )
		        		 	if ( dir.getName().startsWith(prefix) )
		        		 		return false;
		        		 return true;
		        	 }
		        	 return false;
		         }
			} );
			Arrays.sort(listSubDirObjet);

				//	traitement des sous-r�pertoires
			
			int i=0,j=0;
			while ( i < listSubDirSource.length || j < listSubDirObjet.length )  {
				String nameSource = "";
				String nameObjet = "";
				int comp = 0;
				if ( i < listSubDirSource.length && j < listSubDirObjet.length )  {
					nameSource = listSubDirSource[i].getName();
					nameObjet = listSubDirObjet[j].getName();
					if ( ignoreCase )
						comp = nameSource.compareToIgnoreCase(nameObjet);
					else
						comp = nameSource.compareTo(nameObjet);
				}  else if ( i < listSubDirSource.length )  {
					nameSource = listSubDirSource[i].getName();
					comp = -1;
				}  else if ( j < listSubDirObjet.length )  {
					nameObjet = listSubDirObjet[j].getName();
					comp = 1;					
				}

				writeTrace(ContexteGlobal.getResourceString("traceDir") +
						nameSource+"/"+nameObjet+"/"+comp);
				
				if ( comp == 0 )  {
						// source = objet => les mettre dans la pile pour copier
					listDir.push(listSubDirSource[i++].getPath());
					listDir.push(listSubDirObjet[j++].getPath());
					listDir.push(parm);

				} else if ( comp < 0 )  {
							// source < objet => nouveau r�pertoire � cr�er et copier
					listCreateDir.push(listSubDirSource[i].getPath());
					listCreateDir.push(dirObjetName + '/' + nameSource);
					writeLog("=>" +
							ContexteGlobal.getResourceString("messCreateDir") +
							dirObjetName  + '/' + nameSource);					
					i++;
					
				} else {
						// source > objet => le r�pertoire objet n'existe pas dans source
					if ( parm.indexOf("deleteDir") >= 0 )  {
						listDeleteDir.push(listSubDirObjet[j].getPath());
						writeLog("=>" +
								ContexteGlobal.getResourceString("messDeleteDir") +
								listSubDirObjet[j].getPath());					
					}  else  {						
							// archive, aucune action
						writeLog("=>" +
								ContexteGlobal.getResourceString("messNoSynchroDir") +
								listSubDirObjet[j].getPath());					
					}
					j++;
					
				}				
			}

				//	liste des fichiers sources
					
			File[] listFileSource = dirSource.listFiles(new FileFilter() { 
		         public boolean accept(File dir)
		              { return dir.isFile() && !dir.isHidden();  }
			} );
			Arrays.sort(listFileSource);

			//	liste des fichiers objets
			
			File[] listFileObjet = dirObjet.listFiles(new FileFilter() { 
		         public boolean accept(File dir)
		              { return dir.isFile() && !dir.isHidden();  }
			} );
			Arrays.sort(listFileObjet);

				//	traitement des fichiers
		
			i=j=0;
			while ( i < listFileSource.length || j < listFileObjet.length )  {
				String nameSource = "";
				String nameObjet = "";
				int comp = 0;
				if ( i < listFileSource.length && j < listFileObjet.length )  {
					nameSource = listFileSource[i].getPath().substring(dirSourceLength);
					nameObjet = listFileObjet[j].getPath().substring(dirObjetLength);							
					if ( ignoreCase )
						comp = nameSource.compareToIgnoreCase(nameObjet);
					else
						comp = nameSource.compareTo(nameObjet);
				}  else if ( i < listFileSource.length )  {
					nameSource = listFileSource[i].getPath().substring(dirSourceLength);
					comp = -1;
				}  else if ( j < listFileObjet.length )  {
					nameObjet = listFileObjet[j].getPath().substring(dirObjetLength);
					comp = 1;					
				}
				
				writeTrace(ContexteGlobal.getResourceString("traceFile")+
						nameSource+"/"+nameObjet+"/"+comp);
				
				if ( comp == 0 )  {		// source = objet => copier si date sup�rieure
					if ( listFileSource[i].lastModified() > listFileObjet[j].lastModified())  {
						listUpdate.push(dirSourceName + nameSource);
						listUpdate.push(dirObjetName + nameObjet);						
						writeLog("=>" +
								ContexteGlobal.getResourceString("messUpdateFile") +
								listFileObjet[j].getPath());
					}
					i++;
					j++;
					
				} else if ( comp < 0 )  {
						// fin objet ou pas fin source et source < objet => nouveau fichier � copier
					listCreate.push(dirSourceName + nameSource);
					listCreate.push(dirObjetName + nameSource);
					writeLog("=>" +
							ContexteGlobal.getResourceString("messCreateFile") +
							dirObjetName + nameSource);
					i++;
					
				} else  {
						// fin source ou pas fin objet et source > objet => supprimer le fichier objet
					listDelete.push(dirObjetName + nameObjet);
					writeLog("=>" +
							ContexteGlobal.getResourceString("messDeleteFile") +
							listFileObjet[j].getPath());
					j++;
					
				}
				
			}
					
		}
	}

		//	traitement param�tre ignore
	
	private Vector<String> getIgnore( String rech, String parm)	{
		Vector<String> result = new Vector<String>();
		String[] allParm = parm.split(",");
		String prefix = "ignore" + rech + '/';
		
		for ( String unParm: allParm )  {
			if ( unParm.startsWith(prefix))  {
				result.addElement(unParm.substring(prefix.length(), unParm.length()-1));
			}
		}
		
		return result;
	}

		//	ex�cution de la synchronisation
	
	public void execSynchro() throws IOException	{
		writeTrace(ContexteGlobal.getResourceString("messSynchro"));					
		
		//	actions sur les r�pertoires: delete, create
	
		while ( !listCreateDir.empty() )  {
			String nameObjet = listCreateDir.pop();
			String nameSource = listCreateDir.pop();
			copyDir(new File(nameSource),new File(nameObjet));
			writeLog("=>" +
					ContexteGlobal.getResourceString("messCreateDir") +
					nameObjet);					
		}

		while ( !listDeleteDir.empty() )  {
			String nameObjet = listDeleteDir.pop();
			deleteDir(new File(nameObjet));
			writeLog("=>" +
					ContexteGlobal.getResourceString("messDeleteDir") +
					nameObjet);					
		}
		
		//	actions sur les fichier: delete, create, update
	
		while ( !listDelete.empty() )  {
			String nameObjet = listDelete.pop();
			File f = new File(nameObjet);
			if ( f.delete() )				
				writeLog("=>" +
					ContexteGlobal.getResourceString("messDeleteFile") +
					nameObjet);
			else
				writeLog("=>" +
						ContexteGlobal.getResourceString("messDeleteFileErr") +
						nameObjet);				
		}
		
		while ( !listCreate.empty() )  {
			String nameObjet = listCreate.pop();
			String nameSource = listCreate.pop();
			Files.copy(Paths.get(nameSource), Paths.get(nameObjet));
			writeLog("=>" +
					ContexteGlobal.getResourceString("messCreateFile") +
					nameObjet);
		}
		
		while ( !listUpdate.empty() )  {
			String nameObjet = listUpdate.pop();
			String nameSource = listUpdate.pop();
			Files.copy(Paths.get(nameSource), Paths.get(nameObjet),StandardCopyOption.REPLACE_EXISTING);
			writeLog("=>" +
					ContexteGlobal.getResourceString("messUpdateFile") +
					nameObjet);
		}		
		
	}
	
		//	creation et copie d'un r�pertoire

public void copyDir( File dirSource, File dirObjet ) throws IOException  {
	Files.createDirectory(dirObjet.toPath());
	File[] listFileDir = dirSource.listFiles(new FileFilter() { 
         public boolean accept(File dir)
              { return !dir.isHidden();  }
	} );
	for ( File f: listFileDir )  {
		if ( f.isDirectory() )	{
			copyDir(f, new File(dirObjet.getPath() + '/' + f.getName()));
		}  else  {
			Path pSource = f.toPath();
			Files.copy(pSource, dirObjet.toPath().resolve(pSource.getFileName()));
		}
	}
}
	
		//	suppression d'un r�pertoire
	
	public void deleteDir( File dir ) throws IOException  {
		File[] listFileDir = dir.listFiles(new FileFilter() { 
	         public boolean accept(File dir)
	              { return !dir.isHidden();  }
		} );
		for ( File f: listFileDir )  {
			if ( f.isDirectory() )
				deleteDir(f);
			else  {
				if ( !f.delete() )
					writeLog("=>" +
						ContexteGlobal.getResourceString("messDeleteFileErr") +
						f.getName());				

			}
		}
		if ( !dir.delete() )
			writeLog("=>" +
				ContexteGlobal.getResourceString("messDeleteFileErr") +
				dir.getName());				
	}
	
		//	Log	
	
	public void writeLog( String s )  {
		String slog = s + "\n";
		try  {
			logout.write(slog.getBytes());
		} catch (Exception ex)	{
			System.out.println("Erreur : " + ex);
			ex.printStackTrace();
			codeRetour = 99;
		}

	}
	
	public void writeTrace( String s )  {
		if ( trace )
			writeLog(">>" + s);
	}
	
		// Erreurs de param�tres

	static void usage()
	{
		System.out.println(ContexteGlobal.getResourceString("messUsage"));
		System.exit(10);
	}

}
