package playlist;

import java.net.URI;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.embed.swing.JFXPanel;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

/**
 * @author Michel
 * Recherche asynchrone des informations musique (tag)
 *
 */
public class InfoMedia implements Runnable {
	/**
	 *  initialisation fx toolkit
	 */
	private JFXPanel fxPanel = new JFXPanel();
	/**
	 *  gestion de la synchronisation
	 */
	final Lock lockQueue = new ReentrantLock();
	final Condition notEmpty = lockQueue.newCondition();
	final Lock lockMedia = new ReentrantLock();
	final Condition ready = lockMedia.newCondition();
	private boolean active;	
	/**
	 *  Media
	 */	
	private Media media;
	private MediaPlayer mediaPlayer;
	/**
	 *  récupération des tags
	 */	
	private MapChangeListener<String, Object> metadataChangeListener;
	/**
	 *  données des tags
	 */
	private String artist = "";
	private String title = "";
	private String album = "";
	private String year = "";
	private String genre = "";
	private ImageView imageMedia = null;
	private int playTime = 0;	
	/**
	 *  élement de la queue d'exécution
	 */
	private class queueObj {
		URI uriFile;
		DefaultTableModel modelJTable;
		int row;	
	}
	/**
	 *  Queue d'exécution
	 */
	private ArrayBlockingQueue<queueObj> queue;

	/**
	 * constructeur
	 */
	public InfoMedia()  {
		queue = new ArrayBlockingQueue<queueObj>(20,true);
		active = true;
		new Thread(this).start();
	}

	/**
	 * arrêt de la thread, plus d'infos à demander
	 */
	public void stopInfoMedia()  {
		active = false;		
	}
	
	/**
	 * empile paramètres de recherche
	 * @param uriFile
	 * @param table
	 * @param row
	 */
	public void getInfoMedia(URI uriFile, JTable table, int row)  {
		lockQueue.lock();
		try {
			queueObj elem = new queueObj();
			elem.uriFile = uriFile;
			elem.modelJTable = (DefaultTableModel)table.getModel();
			elem.row = row;
			queue.put(elem);
			notEmpty.signal();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lockQueue.unlock();
        }
	}

	/**
	 * lancement des recherches
	 */
	public void run() {
		lockQueue.lock();
		try {
			queueObj elem;
			while ( active || !queue.isEmpty() )  {
				elem = queue.poll();
				if ( elem != null )
					search(elem.uriFile, elem.modelJTable, elem.row);
				else if ( active )
					notEmpty.await();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lockQueue.unlock();
	    }
	}
	
	/**
	 * recherche des infos pour une musique
	 * @param uriFile
	 * @param modelJTable
	 * @param row
	 */
	private void search(URI uriFile, DefaultTableModel modelJTable, int row)  {
		try {
			media = new Media(uriFile.toString());
			String path = uriFile.getPath();
			String titleMus = path.substring(path.lastIndexOf('/')+1,path.lastIndexOf('.'));
	        modelJTable.setValueAt(titleMus, row, 0);
			setMetaDataDisplay(media.getMetadata());			
			mediaPlayer = new MediaPlayer(media);
		
			mediaPlayer.setOnReady(new Runnable() {
				public void run() {
					lockMedia.lock();
					try {
						Duration duration = mediaPlayer.getMedia().getDuration();
						playTime =  (int) Math.floor(duration.toSeconds());
						ready.signal();
				    } finally {
				    	lockMedia.unlock();
				    }
				}
			});

			lockMedia.lock();
			try {
				ready.await();
		    } finally {
		    	lockMedia.unlock();
		    }
			
/*			String out = "Musique ready : " + uriFile + "\n";
			out += "Duration = " + playTime/60 + "'" + playTime%60 + "\n";
			out += "Artist = "+ artist + "\n";
			out += "Title = "+ title + "\n";
			out += "Album = "+ album + "\n";
			out += "Year = "+ year + "\n";
			out += "Genre = "+ genre + "\n";			
			System.out.println(out); */
			
			mediaPlayer.dispose();
	        
	        	//  tags
			
			if ( !title.isEmpty() ) {
				modelJTable.setValueAt(title, row, 0);
				modelJTable.setValueAt(artist, row, 1);
				modelJTable.setValueAt(year, row, 2);
		        modelJTable.setValueAt(genre, row, 4);
			} else {
				modelJTable.setValueAt("?", row, 1);
				modelJTable.setValueAt("?", row, 2);
		        modelJTable.setValueAt("?", row, 4);				
			}
	        modelJTable.setValueAt(playTime, row, 3);
	        
		} catch (Exception e) {
			System.out.println("Erreur sur : " + uriFile);
			e.printStackTrace();
	        modelJTable.setValueAt("erreur", row, 3);
		}

	}

	/**
	 * listener des tags du media (ne fonctionne qu'avec mp3)
	 * @param metadata
	 */
	private void setMetaDataDisplay(ObservableMap<String, Object> metadata) {
		metadataChangeListener = new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(Change<? extends String, ?> change) {
				if ( change.wasAdded() ) {
					String key=change.getKey();
					Object value=change.getValueAdded(); 
 					System.out.println("Add tag : " + key + "=" + value);
					switch(key){
					case "album":
						album = (String) value;
						break;
					case "artist":
						artist = (String) value;
						break;
					case "title":
						title = (String) value;
						break;
					case "year":
						year = (String) value.toString();
						break;
					case "genre":
						genre = (String) value;
						break;   
					case "image":
// non utilisé			imageMedia = new ImageView((Image) value);
						break;
					}
				}    
			}
		};
		metadata.addListener(metadataChangeListener);
	}
}

