package playlist;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/*
 * 		Playlist
 */

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Playlist {
    private StringProperty fileName;
    public void setFileName(String value) { fileNameProperty().set(value); }
    public String getFileName() { return fileNameProperty().get(); }
    public StringProperty fileNameProperty() { 
        if (fileName == null) fileName = new SimpleStringProperty(this, "fileName");
        return fileName; 
    }

    private StringProperty name;
    public void setName(String value) { nameProperty().set(value); }
    public String getName() { return nameProperty().get(); }
    public StringProperty nameProperty() { 
        if (name == null) name = new SimpleStringProperty(this, "name");
        return name; 
    } 
	 
    private IntegerProperty durationTime;
    public void setDurationTime(int value) { durationTimeProperty().set(value); }
    public int getDurationTime() { return durationTimeProperty().get(); }
    public IntegerProperty durationTimeProperty() { 
        if (durationTime == null) durationTime = new SimpleIntegerProperty(this, "durationTime");
        return durationTime; 
    } 
    
    private StringProperty duration;
    public void setDuration(String value) { durationProperty().set(value); }
    public String getDuration() { return durationProperty().get(); }
    public StringProperty durationProperty() { 
        if (duration == null) duration = new SimpleStringProperty(this, "duration");
        return duration; 
    } 

    private StringProperty tandaName;
    public void setTandaName(String value) { tandaNameProperty().set(value); }
    public String getTandaName() { return tandaNameProperty().get(); }
    public StringProperty tandaNameProperty() { 
        if (tandaName == null) tandaName = new SimpleStringProperty(this, "tandaName");
        return tandaName; 
    } 

    private StringProperty genre;
    public void setGenre(String value) { genreProperty().set(value); }
    public String getGenre() { return genreProperty().get(); }
    public StringProperty genreProperty() { 
        if (genre == null) genre = new SimpleStringProperty(this, "genre");
        return genre; 
    } 

    private StringProperty genreCompl;
    public void setGenreCompl(String value) { genreComplProperty().set(value); }
    public String getGenreCompl() { return genreComplProperty().get(); }
    public StringProperty genreComplProperty() { 
        if (genreCompl == null) genreCompl = new SimpleStringProperty(this, "genreCompl");
        return genreCompl; 
    } 

    private StringProperty chef;
    public void setChef(String value) { chefProperty().set(value); }
    public String getChef() { return chefProperty().get(); }
    public StringProperty chefProperty() { 
        if (chef == null) chef = new SimpleStringProperty(this, "chef");
        return chef; 
    } 

    private StringProperty chanteur;
    public void setChanteur(String value) { chanteurProperty().set(value); }
    public String getChanteur() { return chanteurProperty().get(); }
    public StringProperty chanteurProperty() { 
        if (chanteur == null) chanteur = new SimpleStringProperty(this, "chanteur");
        return chanteur; 
    } 

    private StringProperty year;
    public void setYear(String value) { yearProperty().set(value); }
    public String getYear() { return yearProperty().get(); }
    public StringProperty yearProperty() { 
        if (year == null) year = new SimpleStringProperty(this, "year");
        return year; 
    } 

    private StringProperty play;
    public void setPlay(String value) { playProperty().set(value); }
    public String getPlay() { return playProperty().get(); }
    public StringProperty playProperty() { 
        if (play == null) play = new SimpleStringProperty(this, "play");
        return play; 
    } 
	 
}
