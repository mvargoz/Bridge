package synchro;

/*		Synchronise un répertoire source avec un répertoire objet
 * Seules les actions faites sur le répertoire source sont répercutées sur l'objet (création, modif, suppression)
 * on ne modifie jamais la source
 * les sous-répertoires et fichiers cachés sont ignorés (fichiers système)
 *  	ainsi que les sous-répertoires commençant par un préfix donné par ignoreSource et ignoreObjet
 * les nouveaux fichiers du répertoire source ainsi que ceux ayant une date de modification plus récente sont copiés 
 * les nouveaux sous-répertoires sont créés
 * les fichiers existant dans l'objet et pas dans la source sont supprimés
 * les sous-répertoires existant dans l'objet et pas dans la source sont supprimés avec le paramètre deleteDir sinon ils sont conservés
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
		//  10 = ligne de commande erronée dans le main
		//  30 = erreur paramètre
		//  99 = erreur IO 
	public int codeRetour = 0;
		//	Phase : 1=vérification, 2=exécution	
	public int phase = 0;
		//	trace
	public boolean trace = false;
		//	répertoires à synchroniser
	private Stack<String> listDir;
		//	fichiers à supprimer, créer, mettre à jour
	private Stack<String> listDelete, listCreate, listUpdate;
		//  répertoire à supprimer, créer
	private Stack<String> listDeleteDir, listCreateDir;	
		//	ignore majuscule / minuscule pour les noms de fichier (Windows)
	private boolean ignoreCase = false;
		// 	fichier de paramètre
	private File fParam;
		//	pipe de communication 
	private PipedOutputStream logout;
		//	thread d'exécution
	private Thread thSynchro;
	private volatile boolean stopThread;
	

		//		Synchronisation de répertoires
	
	public Backup( File pfParam )  {
		
		this.fParam = pfParam;

			//	pile des répertoires à synchroniser
		
		listDir = new Stack<String>();
		
			//	stockage des actions à effectuer
		
		listDeleteDir = new Stack<String>();
		listCreateDir = new Stack<String>();
		listDelete = new Stack<String>();
		listCreate = new Stack<String>();
		listUpdate = new Stack<String>();

	}

		//	exécution de la phase de vérification ou de synchronisation en thread

	public void go(int pPhase, PipedOutputStream pLogout)  {		
		this.phase = pPhase;
		this.logout = pLogout;
			// lance la thread (exécution méthode run)
	    thSynchro = new Thread(this);
	    stopThread = false;
	    thSynchro.start();
    
	}
	
		//	stop thread
	
	public void stop()  {
	    stopThread = true;
	}

		//	exécution de la thread de synchro
	
	public void run()  {
		try {
			if ( phase == 1 )	{				
				writeTrace(ContexteGlobal.getResourceString("messVerify"));
					//	test du système			
				String systemName = System.getProperty("os.name");
				writeTrace(ContexteGlobal.getResourceString("messSystem") + systemName);
		        if ( systemName.startsWith("Windows") )   {
		        	ignoreCase = true;
		        	writeTrace(ContexteGlobal.getResourceString("messIgnoreCase"));
		        }
		        	//  lecture des paramètres
		        codeRetour = 0;
				getParam(fParam);			
					//	recherche des éléments à synchroniser			
				if ( codeRetour == 0 )
					verifSynchro();
			} else if ( phase == 2 )   {			
				writeTrace(ContexteGlobal.getResourceString("messSynchro"));
					//	exécution de la synchronisation			
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
	
				//	paramètres
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

		/*  lecture des paramètres
		 *  syn=<fichier de paramètre>
		 *  in=<répertoire source>
		 *  out=<répertoire objet>
		 *	parm=<liste des paramètres séparés par une virgule>
		 *			noDeleteDir	: ne pas supprimer les sous-répertoires de l'objet n'existant pas dans la source
		 *			ignoreSource/xxx/ : ne synchronise pas les fichiers sources commençant par xxx 
		 *			ignoreObjet/xxx/ : ne synchronise pas les fichiers objets commençant par xxx 
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
				
					//  prochain paramètre
				
				indWaitLine++;
				if ( indWaitLine >= waitLines.length )
					indWaitLine = 0;
			}

			line = in.readLine();			
		}
		in.close();
	}
	
			//	recherche des éléments à synchroniser
	
	public void verifSynchro() throws IOException    {
		writeTrace(ContexteGlobal.getResourceString("messVerify"));					
			// répertoires
		File dirSource, dirObjet;
		String parm, dirSourceName, dirObjetName;
		int dirSourceLength, dirObjetLength;
		
		while ( !listDir.empty() )  {
			if ( stopThread )  return;
			
				//	traitement d'un répertoire
			
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
			
				//	liste des sous-répertoires sources
			
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
			
			//	liste des sous-répertoires objets
			
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

				//	traitement des sous-répertoires
			
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
							// source < objet => nouveau répertoire à créer et copier
					listCreateDir.push(listSubDirSource[i].getPath());
					listCreateDir.push(dirObjetName + '/' + nameSource);
					writeLog("=>" +
							ContexteGlobal.getResourceString("messCreateDir") +
							dirObjetName  + '/' + nameSource);					
					i++;
					
				} else {
						// source > objet => le répertoire objet n'existe pas dans source
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
				
				if ( comp == 0 )  {		// source = objet => copier si date supérieure
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
						// fin objet ou pas fin source et source < objet => nouveau fichier à copier
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

		//	traitement paramètre ignore
	
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

		//	exécution de la synchronisation
	
	public void execSynchro() throws IOException	{
		writeTrace(ContexteGlobal.getResourceString("messSynchro"));					
		
		//	actions sur les répertoires: delete, create
	
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
	
		//	creation et copie d'un répertoire

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
	
		//	suppression d'un répertoire
	
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
	
		// Erreurs de paramètres

	static void usage()
	{
		System.out.println(ContexteGlobal.getResourceString("messUsage"));
		System.exit(10);
	}

}
