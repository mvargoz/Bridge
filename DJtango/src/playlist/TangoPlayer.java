package playlist;

/*
 * 		Player
 */

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;
import javafx.util.Duration;

import winApp.ContexteGlobal;

import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.swing.*;

public class TangoPlayer extends JDialog implements WindowListener {
	private static final long serialVersionUID = 1L;

	//		constantes
	
	private static String title = ContexteGlobal.getResourceString("player");
	private static boolean modal = false;
	private static final String playImage = ContexteGlobal.getResourceString("playImage");
	private static final String pauseImage = ContexteGlobal.getResourceString("pauseImage");
	private static final int heightScreen = 850; 	// hauteur écran
	private static final int widthScreen = 1440; 	// largeur écran
	private static final int heightScreenMini = 75; 	// hauteur écran miniplayer
	private static final int widthScreenMini = 500; 	// largeur écran miniplayer
	private static final int widthPlay = 20;		// largeur indication tango à l'écoute
	private static final int widthMaestro = 100;	// largeur maestro
	private static final int widthTitre = 200;		// largeur titre tango
	private static final int widthDuration = 60;	// largeur durée tango
	private static final int heightPochette = 600;	// hauteur max pochette
	private static final int widthPochette = 600;	// largeur max pochette
	public static final String[] tbGenre = {"Cortina","Cumparsita","Milonga","Tango","Vals"};
	public static final String[] tbGenreCompl = {"Candombe","Foxtrot","Hits","Nuevo","Electro"};
	
	//		frame
	
	private Frame parentFrame;	
	private JFXPanel fxPanel;
	private BorderPane root;
	
	//		media
	
	private MediaPlayer mediaPlayer = null;
	private MediaView mediaView;
	private Media media = null;
	private MapChangeListener<String, Object> metadataChangeListener;
	
	//		media bar
		
	private Button playButton;
	private Slider timeSlider;
	private Label playTime;
	private Image PlayButtonImage;
	private Image PauseButtonImage;
	private ImageView imageViewPlay;
	private ImageView imageViewPause;
	
	//		playlist support
	
	private TableView<Playlist> playlistTable;
	private ObservableList<Playlist> playlists = FXCollections.observableArrayList();
	private int indexPlay = 0;
	private boolean bplaylist = false;

	
	//		information media

	private BorderPane infoMedia = new BorderPane();
	private HBox paneTop = new HBox(5);
	private HBox paneImage = new HBox(5);	
	private HBox paneBottom = new HBox(5);
    private Label textMusicArtist;
    private Label textMusicTitle;
    private Label textMusicYear;
    private Label textMusicGenre;

    //		informations
    
    private HBox infoMessage = new HBox(5);
    private Label milongaDuration;
    private Label milongaDurationLeft;
   
	//		data

    private boolean miniPlayer = false;
	
	private Duration duration;
	private boolean stopRequested = false;
	private boolean atEndOfMedia = false;
	
	
	//		player constructor
	
	public TangoPlayer(String param) {
		super(ContexteGlobal.frame, title, modal);
		addWindowListener(this);
        if ( param.contains("mini"))
        	miniPlayer = true;
		parentFrame = ContexteGlobal.frame;
        fxPanel = new JFXPanel();
        getContentPane().add(fxPanel);
        if ( miniPlayer )  {
        	setSize(widthScreenMini,heightScreenMini);
        } else {
        	setSize(widthScreen,heightScreen);
        }
		setVisible(false);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                initFX(fxPanel);
            }
       });		
	}

	//		affichage du player
	
	public void open() {
        if ( miniPlayer )  {
    		Point p = ContexteGlobal.frame.getLocationOnScreen();
    		p.y += ContexteGlobal.frame.getHeight() - heightScreenMini;
    		p.x += ContexteGlobal.frame.getWidth() - widthScreenMini;
    		setLocation(p);
        } else {
    		setLocationRelativeTo(parentFrame);
        }
		setVisible(true);
	}

	//	init fx panel
	
    private void initFX(JFXPanel fxPanel) {
		root = new BorderPane();
        Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("player.css").toExternalForm());
		mediaView = new MediaView();

		//		media bar
		
		HBox mediaBar = new HBox(5.0);
		mediaBar.setId("mediaBar");
		root.setTop(mediaBar);
		mediaBar.setPadding(new Insets(5, 10, 5, 10));
		mediaBar.setAlignment(Pos.CENTER_LEFT);
		BorderPane.setAlignment(mediaBar, Pos.CENTER);

		//		play button

		playButton = new Button();
		PlayButtonImage = new Image(ClassLoader.getSystemResourceAsStream(playImage));
		PauseButtonImage = new Image(ClassLoader.getSystemResourceAsStream(pauseImage));
		imageViewPlay = new ImageView(PlayButtonImage);
		imageViewPause = new ImageView(PauseButtonImage);
		playButton.setGraphic(imageViewPlay);
		playButton.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				updateValues();
				Status status = mediaPlayer.getStatus();
				if (status == Status.UNKNOWN || status == Status.HALTED) {
					return;
				}
				if (status == Status.PAUSED || status == Status.READY || status == Status.STOPPED) {
					if (atEndOfMedia) {
						mediaPlayer.seek(mediaPlayer.getStartTime());
						atEndOfMedia = false;
						playButton.setGraphic(imageViewPlay);
						updateValues();
					}
					mediaPlayer.play();
					playButton.setGraphic(imageViewPause);
				} else {
					mediaPlayer.pause();
				}
			}
		});
		playButton.setDisable(true);
		mediaBar.getChildren().add(playButton);
		
		// Time label
		
		Label timeLabel = new Label("Time");
		timeLabel.setMinWidth(Control.USE_PREF_SIZE);
		mediaBar.getChildren().add(timeLabel);

		// Time slider
		
		timeSlider = new Slider();
		HBox.setHgrow(timeSlider, Priority.ALWAYS);
		timeSlider.valueProperty().addListener(new InvalidationListener() {
			public void invalidated(Observable ov) {
				if (timeSlider.isValueChanging()) {
					// multiply duration by percentage calculated by slider position
					if (duration != null) {
						mediaPlayer.seek(duration.multiply(timeSlider.getValue() / 100.0));
					}
					updateValues();
				}
			}
		});
		mediaBar.getChildren().add(timeSlider);

		// Play label
		
		playTime = new Label();
		mediaBar.getChildren().add(playTime);

		// open file button

		Button buttonOpenFle = new Button("Open");

		buttonOpenFle.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (mediaPlayer != null )
					mediaPlayer.stop();
				FileChooser fileChooser = new FileChooser();
				fileChooser.setInitialDirectory(new File(PlaylistPanel.playlistDir));
				fileChooser.setTitle("Open music File");
				fileChooser.getExtensionFilters().addAll(
						new ExtensionFilter("Playlist", "*.m3u"),
						new ExtensionFilter("Audio", "*.wav", "*.mp3", "*.m4a", "*.aac", "*.aif"),
						new ExtensionFilter("All Files", "*.*"));
				File selectedFile = fileChooser.showOpenDialog(null);
				if (selectedFile != null) {
					if ( selectedFile.getName().endsWith(".m3u"))
						loadPlaylist(selectedFile);
					else
						playMusic(selectedFile);
				}
			}
		});
		mediaBar.getChildren().add(buttonOpenFle);
		
		//		information media
		
	    textMusicArtist = new Label();
		textMusicArtist.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 40));
	    textMusicTitle = new Label();
        textMusicTitle.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 50));       
	    textMusicYear = new Label();
        textMusicYear.setFont(new Font(30));
		textMusicGenre = new Label();
        textMusicGenre.setFont(new Font(30));
//      textMusicGenre.getStyleClass().add("blue");

        root.setCenter(infoMedia);

        paneTop.getChildren().addAll(textMusicArtist);       
        paneTop.setAlignment(Pos.CENTER);
        infoMedia.setTop(paneTop);
        paneImage.setAlignment(Pos.CENTER);
        infoMedia.setCenter(paneImage);
        paneBottom.getChildren().addAll(textMusicGenre,
        		new Label("-"),
        		textMusicTitle,
         		textMusicYear);       
        paneBottom.setAlignment(Pos.CENTER);
        infoMedia.setBottom(paneBottom);
		
		//		playlist

		playlistTable = new TableView<Playlist>();
		playlistTable.setItems(playlists);
		
		TableColumn<Playlist,String> playCol = new TableColumn<Playlist,String>(" ");
		playCol.setPrefWidth(widthPlay);
		playCol.setCellValueFactory(new PropertyValueFactory<Playlist,String>("play"));
		
		TableColumn<Playlist,String> chefCol = new TableColumn<Playlist,String>("Maestro");
		chefCol.setPrefWidth(widthMaestro);
		chefCol.setCellValueFactory(new PropertyValueFactory<Playlist,String>("chef"));
		
		TableColumn<Playlist,String> nameCol = new TableColumn<Playlist,String>("Titre");
		nameCol.setPrefWidth(widthTitre);
		nameCol.setCellValueFactory(new PropertyValueFactory<Playlist,String>("name"));
		
		TableColumn<Playlist,String> timeCol = new TableColumn<Playlist,String>("Durée");
		timeCol.setPrefWidth(widthDuration);
		timeCol.setCellValueFactory(new PropertyValueFactory<Playlist,String>("duration"));
		
		playlistTable.getColumns().setAll(playCol, chefCol, nameCol, timeCol);
		
			//   double clic sur une musique de la playlist
		
		playlistTable.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
	            if(mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2){
					if (mediaPlayer != null )
						mediaPlayer.stop();
					Playlist pl1 = playlists.get(indexPlay);
					pl1.setPlay(" ");
					indexPlay = playlistTable.getSelectionModel().getSelectedIndex();	            
		    		playPlaylist();
	            }
			}
			
		});
		
			//	 mise en évidence des musiques en fonction du genre: tango, milonga ou valse
		
		playlistTable.setRowFactory(new Callback<TableView<Playlist>, TableRow<Playlist>>() {
		    @Override
		    public TableRow<Playlist> call(final TableView<Playlist> p) {
		        return new TableRow<Playlist>() {
		            @Override
		            public void updateItem(Playlist item, boolean empty) {
		                super.updateItem(item, empty);
                		setStyle("-fx-background-color: silver");
		                if ( item != null && item.getTandaName() != null )  {
		                	if ( item.getTandaName().startsWith("Tango"))
		                		setStyle("-fx-background-color: paleturquoise");
		                	else if ( item.getTandaName().startsWith("Milonga"))
		                		setStyle("-fx-background-color: palevioletred");
		                	else if ( item.getTandaName().startsWith("Vals"))
		                		setStyle("-fx-background-color: palegreen");
		                }
		            }
		        };
		    }
		   });
		
		root.setLeft(playlistTable);
		
		milongaDuration = new Label(". h .. mn .. s");		
		milongaDurationLeft = new Label(". h .. mn .. s");
		infoMessage.getChildren().addAll(
				new Label("Durée de la milonga : "),milongaDuration,
				new Label("   Durée restante : "),milongaDurationLeft
				);
		infoMessage.setAlignment(Pos.CENTER);
		infoMessage.setId("infoMessage");
		root.setBottom(infoMessage);

        fxPanel.setScene(scene);
    }
  
    //		update playtime, timeSlider
    
	private void updateValues() {
		if (playTime != null && timeSlider != null && duration != null) {
			Platform.runLater(new Runnable() {
				public void run() {
					Duration currentTime = mediaPlayer.getCurrentTime();
					playTime.setText(formatTime(currentTime, duration));
					timeSlider.setDisable(duration.isUnknown());
					if (!timeSlider.isDisabled() && duration.greaterThan(Duration.ZERO)
							&& !timeSlider.isValueChanging()) {
						timeSlider.setValue(currentTime.divide(duration).toMillis() * 100.0);
					}
				}
			});
		}
	}

	//		format time
	
	private String formatTime(Duration elapsed, Duration duration) {
		int intElapsed = (int) Math.floor(elapsed.toSeconds());
		int elapsedHours = intElapsed / (60 * 60);
		if (elapsedHours > 0) {
			intElapsed -= elapsedHours * 60 * 60;
		}
		int elapsedMinutes = intElapsed / 60;
		int elapsedSeconds = intElapsed - elapsedHours * 60 * 60 - elapsedMinutes * 60;

		if (duration.greaterThan(Duration.ZERO)) {
			int intDuration = (int) Math.floor(duration.toSeconds());
			int durationHours = intDuration / (60 * 60);
			if (durationHours > 0) {
				intDuration -= durationHours * 60 * 60;
			}
			int durationMinutes = intDuration / 60;
			int durationSeconds = intDuration - durationHours * 60 * 60 - durationMinutes * 60;

			if (durationHours > 0) {
				return String.format("%d:%02d:%02d/%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds,
						durationHours, durationMinutes, durationSeconds);
			} else {
				return String.format("%02d:%02d/%02d:%02d", elapsedMinutes, elapsedSeconds, durationMinutes,
						durationSeconds);
			}
		} else {
			if (elapsedHours > 0) {
				return String.format("%d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
			} else {
				return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
			}
		}
	}

    //	load playlist
    
    public void loadPlaylist(File playlist)  {
    	playlists.clear();
    	int milongaTime = 0;
		BufferedReader in;		
		String line;
		try
		{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(playlist), "UTF8"));
			line = in.readLine();
			line = in.readLine();
			while (line != null)
			{
				Playlist pl = new Playlist();
				pl.setPlay(" ");
					//	paramètres #EXTINF: durée,titre,tanda
				line = line.trim();
				if ( line.startsWith("#EXTINF:")  )  {
					String[] s = line.substring(8).split(",");
					pl.setName(s[1]);
			    	int d = Integer.parseInt(s[0]);
			    	milongaTime += d;
			    	pl.setDurationTime(d);
			    	int mn = d / 60;
			    	int sec = d % 60;
					pl.setDuration(String.format("%02d:%02d", mn, sec));
						//	si EXTINF contient l'information tanda
					if ( s.length > 2 )  {
						pl.setTandaName(s[2]);
						//	éclatement du nom de la tanda
						//  nom = genre [complément] [chef[-chanteur]] [année]
						String[] splitTanda = s[2].split("\\s");
						if ( Arrays.binarySearch(tbGenre,splitTanda[0]) >= 0 )  {
							pl.setGenre(splitTanda[0]);
							if ( splitTanda.length > 1 )	{
								int ind = 1;
								if ( Arrays.binarySearch(tbGenreCompl,splitTanda[1]) >= 0 )  {
									pl.setGenreCompl(splitTanda[ind++]);
								}
								pl.setChef("");
								pl.setChanteur("");
								pl.setYear("");
								if ( splitTanda.length > ind )	{
									String artist = splitTanda[ind++];
									String[] splitArtist = artist.split("-");
									pl.setChef(splitArtist[0]);
									if ( splitArtist.length > 1)
										pl.setChanteur(splitArtist[1]);
									if ( splitTanda.length > ind )
										pl.setYear(splitTanda[ind]);
									}
							}
						}
					}
				} else {
					System.out.println("ligne #EXTINF absente");
					return;
				}
					//	nom de fichier
				line = in.readLine();
				line = line.trim();
				pl.setFileName(line);
				milongaDuration.setText(formatHmns(milongaTime));
				playlists.add(pl);
				line = in.readLine();			
			}
			in.close();
		} catch (Exception ex) {
			System.out.println("Load playlist error : " + ex);
			ex.printStackTrace();
			return;
		}
		
		indexPlay = 0;
		playPlaylist();
    }

    //	play playlist
    
    public void playPlaylist()  {
    	if ( indexPlay > playlists.size() ) {
        	bplaylist = false;
    		indexPlay = 0;
        	return;   		
    	}
    	bplaylist = true;
		Playlist pl1 = playlists.get(indexPlay);
		pl1.setPlay("@");
		int durationLeft = 0;
		for ( int i=indexPlay; i < playlists.size(); i++ )
			durationLeft += playlists.get(i).getDurationTime();
		milongaDurationLeft.setText(formatHmns(durationLeft));
		
		playlistTable.scrollTo(indexPlay);
		
		playMusic(new File(pl1.getFileName()));
    }

    //	play music
    
    public void playMusic(File music)  {
		setTitle(music.getName());
		textMusicArtist.setText("");
		textMusicTitle.setText("");
		textMusicYear.setText("");
		textMusicGenre.setText("");
		paneImage.getChildren().clear();
		media = new Media(music.toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		if ( bplaylist )  {
			Playlist pl1 = playlists.get(indexPlay);
			if (  pl1.getGenre() != null &&
				 !pl1.getGenre().equals("Cortina") &&
				 !pl1.getGenre().equals("Cumparsita") )  {
				textMusicTitle.setText(pl1.getName());
				textMusicArtist.setText(pl1.getChef() + " " + pl1.getChanteur());
				textMusicGenre.setText(pl1.getGenre());
				textMusicYear.setText(pl1.getYear());
				try {
					File chefImage = new File(PlaylistPanel.tandaImageDir + "/" + pl1.getChef() + ".jpg");
					if ( chefImage.exists() )  {
						Image imageTanda = new Image(new FileInputStream(chefImage));
						ImageView viewTanda = new ImageView(imageTanda);
						viewTanda.setPreserveRatio(true);
						viewTanda.setFitWidth(widthPochette);
						viewTanda.setFitHeight(heightPochette);
						paneImage.getChildren().add(viewTanda);
					} else {
						System.out.println("Pas de photo pour chef d'orchestre : " + pl1.getChef());
							//	image de substitution
						File tangoImage = new File(PlaylistPanel.tandaImageDir + "/" + "Tango1.jpg");
						Image imageTanda = new Image(new FileInputStream(tangoImage));
						ImageView viewTanda = new ImageView(imageTanda);
						viewTanda.setPreserveRatio(true);
						viewTanda.setFitWidth(widthPochette);
						viewTanda.setFitHeight(heightPochette);						
						paneImage.getChildren().add(viewTanda);
					}						
				} catch (Exception e) {  }
			}
		}
		setMetaDataDisplay(media.getMetadata());
		mediaPlayer.setAutoPlay(true);
		mediaView.setMediaPlayer(mediaPlayer);
		mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
					Duration newValue) {
				updateValues();
			}
		});
		mediaPlayer.setOnPlaying(new Runnable() {
			public void run() {
				if (stopRequested) {
					mediaPlayer.pause();
					stopRequested = false;
				} else {
					playButton.setGraphic(imageViewPause);
				}
			}
		});
		mediaPlayer.setOnPaused(new Runnable() {
			public void run() {
				playButton.setGraphic(imageViewPlay);
			}
		});
		mediaPlayer.setOnReady(new Runnable() {
			public void run() {
				duration = mediaPlayer.getMedia().getDuration();
				updateValues();
			}
		});
		mediaPlayer.setOnEndOfMedia(new Runnable() {
			public void run() {
				if ( bplaylist )  {
					Playlist pl1 = playlists.get(indexPlay);
					pl1.setPlay(" ");
					indexPlay++;
					playPlaylist();					
				}
				if ( !bplaylist )  {
					playButton.setGraphic(imageViewPlay);
					stopRequested = true;
					atEndOfMedia = true;					
				}
			}
		});
		playButton.setDisable(false);
	
		mediaPlayer.play();
    }   

    //		stop music
    
    public void stop()  {
		if (mediaPlayer != null )
			mediaPlayer.stop();
    }
    
	// affichage des tags du media (ne fonctionne qu'avec mp3)

	private void setMetaDataDisplay(ObservableMap<String, Object> metadata) {
		metadataChangeListener = new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(Change<? extends String, ?> change) {
//				System.out.println("tag:" + change.getKey());
				if ( change.getKey().equals("image") && paneImage.getChildren().isEmpty() )  {
					ImageView imageMedia = new ImageView((Image) change.getValueAdded());
					imageMedia.setPreserveRatio(true);
					imageMedia.setFitWidth(widthPochette);
					imageMedia.setFitHeight(heightPochette);
					paneImage.getChildren().add(imageMedia);
				} else if ( change.getKey().equals("artist") )  {
					String musicArtist = ((String) change.getValueAdded());
			        textMusicArtist.setText(musicArtist);
				} else if ( change.getKey().equals("title") )  {
					String musicTitle = ((String) change.getValueAdded());
			        textMusicTitle.setText(musicTitle);
				} else if ( change.getKey().equals("year") )  {
					String musicYear = "(" + Integer.toString((int) change.getValueAdded()) + ")";
			        textMusicYear.setText(musicYear);
				} else if ( change.getKey().equals("genre") )  {
					String musicGenre = "(" + ((String) change.getValueAdded()) + ")";
			        textMusicGenre.setText(musicGenre);
				}
			}
		};
		metadata.addListener(metadataChangeListener);
	}
	
	//	conversion et formatage seconde en heure, minutes, secondes
	
	private String formatHmns(int secondes)  {
		int h = secondes / 3600;
		int hrest = secondes % 3600;
		int mn = hrest / 60;
    	int sec = hrest % 60;
		return String.format("%d h %02d mn %02d s", h, mn, sec);		
	}
	
	// WindowListener

	@Override
	public void windowActivated(WindowEvent arg0) {
		
	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		stop();				
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowOpened(WindowEvent e) {
		
	}

}
