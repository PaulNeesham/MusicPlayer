package media;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SongQueue {

    private SoundJLayer soundToPlay;

    private List<Song> songList;

    private List<Integer> songsPlayed;

    private Song currentSong;

    private int index = 0;
    
    private boolean isPlaying = false;

    private boolean random = false;

    private boolean loopAll = true;

    private boolean loopSong = false;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public SongQueue(List<Song> songList) {

        this.songList = songList;
        songsPlayed = new ArrayList<Integer>();
        songsPlayed.add(index);
        currentSong = songList.get(index);
        this.select();
    }

    public void select() {
        soundToPlay = new SoundJLayer(currentSong.getFile());
        soundToPlay.addPropertyChangeListener(new FocusManagerListener());

    }

    public class FocusManagerListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            final String propertyName = e.getPropertyName();
            if ("endOfSong".equals(propertyName)) {
                if (!isLoopSong()) {
                    next();                   
                }else{
                	play();
                }
            } else if ("newPosition".equals(propertyName)) {
                pcs.firePropertyChange("newPosition", 0, e.getNewValue());
            }
        }
    }

    public void next() {
        this.soundToPlay.stop();
        if (isRandom()) {
            index = new Random().nextInt(songList.size());
            currentSong = songList.get(index);
            songsPlayed.add(index);
        } else {
            if (index + 1 == songList.size()) {
                if (isLoopAll()) {
                    index = 0;
                    currentSong = songList.get(index);
                    songsPlayed.add(index);
                } else {
                    soundToPlay.stop();
                    isPlaying =false;
                    pcs.firePropertyChange("playing", true, false);
                    pcs.firePropertyChange("newPosition", 100, 0);
                }
            } else {
                currentSong = songList.get(++index);
                songsPlayed.add(index);
            }
        }
        this.select();
        if(isPlaying){
        	play();
        }
    }

    public void previous() {
        this.soundToPlay.stop();
        if (songsPlayed.size() > 1) {
            index = songsPlayed.get(songsPlayed.size() - 2);
            songsPlayed.remove(songsPlayed.size() - 1);
            currentSong = songList.get(index);
            this.select();
            if(isPlaying){
            	play();
            }
        }
    }

    public void play() {
        this.soundToPlay.play();
        isPlaying = true;
        pcs.firePropertyChange("playing", false, true);
    }

    public void pause() {
    	pcs.firePropertyChange("playing", true, false);
        this.soundToPlay.pauseToggle();
        isPlaying = false;
        
    }

    public void stop() {
        this.soundToPlay.stop();
    }

    public void fastForward() {
        this.soundToPlay.fastForward();
    }

    public void skipbackward() {
        this.soundToPlay.skipbackward();
    }

    public void skipForward() {
        this.soundToPlay.skipForward();
    }

    public float getPosition() {
        return this.soundToPlay.getPosition();
    }

    public void changeEqualizer() {
        final float[] settings = new float[32];
        Arrays.fill(settings, 1);
        this.soundToPlay.changeEqualizer(settings);
    }

    public void volumeUp() {

        if (this.soundToPlay.getVolume() < 0.9) {
            this.soundToPlay.setVolume((float) (this.soundToPlay.getVolume() + 0.1));
        } else {
            this.soundToPlay.setVolume(1);
        }
    }

    public void volumeDown() {

        if (this.soundToPlay.getVolume() > 0.1) {
            this.soundToPlay.setVolume((float) (this.soundToPlay.getVolume() - 0.1));
        } else {
            this.soundToPlay.setVolume(0);
        }
    }

    public void setVolume(float volume) {
        this.soundToPlay.setVolume(volume);
    }

    public float getVolume() {
        return this.soundToPlay.getVolume();
    }

    public void setPosition(float position) {
        this.soundToPlay.setPosition(position);
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public boolean isRandom() {
        return random;
    }

    public void setRandom(boolean value) {
        if (value) {
        	this.random = true;
            this.loopAll = false;
        }else{
        	this.random = false;
        }
       
    }

    public boolean isLoopAll() {

        return loopAll;
    }

    public void setLoopAll(boolean value) {
        if (value) {
        	this.random = false;
            this.loopAll = true;
        }else{
        	this.loopAll = false;
        }
    }

    public boolean isLoopSong() {
        return loopSong;
    }

    public void setLoopSong(boolean loopSong) {
        this.loopSong = loopSong;
    }

	public boolean isPlaying() {
		return isPlaying;
	}

	public Object[] getSongList() {
		List<String> songNames = new ArrayList<String>();
		for(Song song : songList){
			songNames.add(song.getMetaData().getSongName());
		}
		Object[] stringNames =  songNames.toArray();
		return stringNames;
	}

	public void setSongByIndex(int selectedIndex) {
		this.soundToPlay.stop();	
		index= selectedIndex;
		currentSong = songList.get(index);
        songsPlayed.add(index);
        this.select();
        if(isPlaying){
        	play();
        }
	}
    	
	public int getSongIndex() {
    		return index;
    		
	}

}
