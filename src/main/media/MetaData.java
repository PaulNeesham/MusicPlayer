package media;

import java.awt.Image;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.images.Artwork;
import org.simpleframework.xml.Element;

public class MetaData {

    @Element(required = false)
    private String imagePath;

    @Element(required = false)
    private String songName = "Unkown Song";

    @Element(required = false)
    private String artistName = "Unkown Artist";

    public MetaData() {
        // for xml
    }

    @Element(required = false)
    private String composerName = " ";

    @Element(required = false)
    private String albumName = "Unkown Album";

    @Element(required = false)
    private String genreName = " ";

    @Element(required = false)
    private String fileName = " ";

    @Element(required = false)
    private String comment = " ";

    @Element(required = false)
    private String encoding = " ";

    @Element(required = false)
    private String songNumber = " ";

    @Element(required = false)
    private String discNumber = " ";

    @Element(required = false)
    private String rating = " ";

    @Element(required = false)
    private String bitRate = " ";

    @Element(required = false)
    private String channels = " ";

    @Element(required = false)
    private int lengthSeconds = 0;

    @Element(required = false)
    private long fileSizeBytes = 0;

    @Element(required = false)
    private String year = " ";
    
    @Element(required = false)
    private String absoluteFilePath = " ";

    public MetaData(File file) {

        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
            final Tag tag = audioFile.getTag();
            final AudioHeader audioHeader = audioFile.getAudioHeader();
            extractFileData(file);
            extractAudioData(audioHeader);
            extractSongName(tag);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void extractFileData(File file) {
        this.fileName = file.getName();
        this.fileSizeBytes = file.length();
        this.absoluteFilePath = file.getAbsolutePath();
    }

    private void extractSongName(Tag tag) {
        this.artistName = tag.getFirst(FieldKey.ARTIST);
        this.albumName = tag.getFirst(FieldKey.ALBUM);
        this.songName = tag.getFirst(FieldKey.TITLE);
        this.comment = tag.getFirst(FieldKey.COMMENT);
        this.year = tag.getFirst(FieldKey.YEAR);
        this.songNumber = tag.getFirst(FieldKey.TRACK);
        this.discNumber = tag.getFirst(FieldKey.DISC_NO);
        this.composerName = tag.getFirst(FieldKey.COMPOSER);
        this.genreName = tag.getFirst(FieldKey.GENRE);
        this.rating = tag.getFirst(FieldKey.RATING);
        extractArtwork(tag);
    }

    private void extractArtwork(Tag tag) {

    }

    private void extractAudioData(AudioHeader audioHeader) {
        this.bitRate = audioHeader.getBitRate();
        this.channels = audioHeader.getChannels();
        this.encoding = audioHeader.getEncodingType();
        this.lengthSeconds = audioHeader.getTrackLength();
    }

    public Image getImage() {
    	try{
	    	AudioFile audioFile = AudioFileIO.read(new File(absoluteFilePath));
	        final Tag tag = audioFile.getTag();
	    
	        final List<Artwork> artworkList = tag.getArtworkList();
	        if (artworkList.size() > 0) {
	            InputStream in = new ByteArrayInputStream(tag.getFirstArtwork().getBinaryData());
	            return ImageIO.read(in);
	        }
    	}catch (Exception e) {}
    	return null;
        
    }

    public String getSongName() {
        return songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getComposerName() {
        return composerName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getGenreName() {
        return genreName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getComment() {
        return comment;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getSongNumber() {
        return songNumber;
    }

    public String getDiscNumber() {
        return discNumber;
    }

    public String getRating() {
        return rating;
    }

    public String getBitRate() {
        return bitRate;
    }

    public String getChannels() {
        return channels;
    }

    public int getLengthMs() {
        return lengthSeconds;
    }

    public long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getYear() {
        return year;
    }

	public void setNewImage(String imagePath) {
		this.imagePath = imagePath;
		try{
	    	AudioFile audioFile = AudioFileIO.read(new File(absoluteFilePath));
	        final Tag tag = audioFile.getTag();
        
	        Artwork art = null;
	        art.setFromFile(new File(imagePath));
	        tag.deleteArtworkField();
	        tag.setField(art);
	        audioFile.commit();
		}catch (Exception e){
			System.out.println("failed to set Image");
		}
	}
	
	private void setMetaData(FieldKey key, String value) {
		try{
			
			AudioFile f = AudioFileIO.read(new File(absoluteFilePath));
			Tag tag = f.getTag();
			tag.deleteField(key);
			tag.setField(key,value);				
			AudioFileIO.write(f);
			
		} catch(Exception e){
			System.out.println("failed to set " + value);
		}
	}
	

	public void setSongName(String songName) {
		this.songName = songName;
		setMetaData(FieldKey.TITLE,songName);		
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
		setMetaData(FieldKey.ARTIST, artistName);
	}

	public void setComposerName(String composerName) {
		this.composerName = composerName;
		setMetaData(FieldKey.COMPOSER, composerName);
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
		setMetaData(FieldKey.ALBUM, albumName);
	}

	public void setGenreName(String genreName) {
		this.genreName = genreName;
		setMetaData(FieldKey.GENRE, genreName);
	}


	public void setComment(String comment) {
		this.comment = comment;
		setMetaData(FieldKey.COMMENT, comment);
	}

	public void setSongNumber(String songNumber) {
		this.songNumber = songNumber;
		setMetaData(FieldKey.TRACK, songNumber);
	}

	public void setDiscNumber(String discNumber) {
		this.discNumber = discNumber;
		setMetaData(FieldKey.DISC_NO, discNumber);
	}

	public void setRating(String rating) {
		this.rating = rating;
		setMetaData(FieldKey.RATING, rating);	
	}

	public void setYear(String year) {
		this.year = year;
		setMetaData(FieldKey.YEAR, year);
	}


}
