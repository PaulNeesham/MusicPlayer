package media;


import java.util.ArrayList;
import java.util.List;


import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class SongLibrary {
		
	@ElementList
	private List<Song> songList;
	
	public SongLibrary() {
		//for xml
	}

	public SongLibrary(List<Song> songList) {
		this.songList = songList;
	}
	
	public List<Song> getSongList(){
		return songList;
	}
	
	public Song getSong(int songNumber){
		return songList.get(songNumber);		
	}
	
	public List<Song> getSearchSongsByName(String songName){
		List<Song> resultList = new ArrayList<Song>();
		for(Song song: songList) {
			if(song.getMetaData().getSongName().toLowerCase().contains(songName.toLowerCase())){
				resultList.add(song);
			}
		}
		return resultList;		
	}
	
	public List<Song> getSearchSongsByArtist(String artistName){
		List<Song> resultList = new ArrayList<Song>();
		for(Song song: songList) {
			if(song.getMetaData().getArtistName().toLowerCase().contains(artistName.toLowerCase())){
				resultList.add(song);
			}
		}
		return resultList;		
	}
	
	public List<Song> getSearchSongsByAlbum(String albumName){
		List<Song> resultList = new ArrayList<Song>();
		for(Song song: songList) {
			if(song.getMetaData().getAlbumName().toLowerCase().contains(albumName.toLowerCase())){
				resultList.add(song);
			}
		}
		return resultList;		
	}
	
	public Song getSongByAbsolutePath(String absolutePath){
		for(Song song: songList) {
			if(song.getAbsoluteFilePath().contains(absolutePath)){
				return song;
			}
		}
		return null;		
	}
	
	public List<Song> getSearchSongsByAll(String name){
		List<Song> resultList = new ArrayList<Song>();			
		for(Song song: songList) {
			if(song.getMetaData().getSongName().toLowerCase().contains(name.toLowerCase())){
				resultList.add(song);
			} else if(song.getMetaData().getArtistName().toLowerCase().contains(name.toLowerCase())){
				resultList.add(song);
			} else if(song.getMetaData().getAlbumName().toLowerCase().contains(name.toLowerCase())){
				resultList.add(song);
			}
		}
		return resultList;		
	}
}
