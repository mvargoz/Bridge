package playlist;

import java.net.URI;

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

public class InfoMedia implements Runnable {
	private JFXPanel fxPanel = new JFXPanel();  // initialisation fx toolkit
	private Object thisObj = this;
	private DefaultTableModel modelJTable;
	private int row;
	private Media media;
	private MediaPlayer mediaPlayer;
	private boolean playerReady = false;
	private MapChangeListener<String, Object> metadataChangeListener;
	private URI uriFile;
	private String artist = "";
	private String title = "";
	private String album = "";
	private String year = "";
	private String genre = "";
	private ImageView imageMedia = null;
	private int playTime = 0;

		//	paramètre de la recherche

	public InfoMedia(URI uriFile, JTable table, int row)  {
		this.uriFile = uriFile;
        modelJTable = (DefaultTableModel)table.getModel();
        this.row = row;
	}

		//   lancement de la recherche
	
	public void run() {
		try {
			media = new Media(uriFile.toString());
			setMetaDataDisplay(media.getMetadata());
			
			mediaPlayer = new MediaPlayer(media);
			
			mediaPlayer.setOnReady(new Runnable() {
				public void run() {
					Duration duration = mediaPlayer.getMedia().getDuration();
					playTime =  (int) Math.floor(duration.toSeconds());
//					System.out.println("Musique ready : " + uriFile);
					synchronized(thisObj){
						playerReady = true;
						thisObj.notifyAll();
					}

				}
			});

			synchronized(thisObj){
				while ( !playerReady ) {
					thisObj.wait();
				}
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

	        modelJTable.setValueAt(playTime, row, 3);
	        
	        	//  tags
	        
			if ( title.length() > 0 )  {
		        modelJTable.setValueAt(title, row, 0);
		        modelJTable.setValueAt(artist, row, 1);
		        modelJTable.setValueAt(year, row, 2);
			}

		} catch (Exception e) {
			System.out.println("Erreur sur : " + uriFile);
			e.printStackTrace();
		}

	}

	// tags du media (ne fonctionne qu'avec mp3)

	private void setMetaDataDisplay(ObservableMap<String, Object> metadata) {
		metadataChangeListener = new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(Change<? extends String, ?> change) {
				if ( change.wasAdded() ) {
					String key=change.getKey();
					Object value=change.getValueAdded(); 
//					System.out.println("Add tag " + uriFile + " : " + key + "=" + value);
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
						imageMedia = new ImageView((Image) value);
						break;
					}
				}    
			}
		};
		metadata.addListener(metadataChangeListener);
	}
}

