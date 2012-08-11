package media;


import java.io.File;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class Song {
	
	@Element
	private MetaData metaData;
	
	@Attribute
	private String absoluteFilePath;
	
	public Song() {
		//for xml
	}

	public Song(MetaData metaData, File absoluteFilePath) {
		this.metaData = metaData;
		this.absoluteFilePath = absoluteFilePath.getAbsolutePath();
	}

	public MetaData getMetaData() {
		return metaData;
	}

	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	public File getFile() {
		return new File(absoluteFilePath);
	}
	
	public String getAbsoluteFilePath() {
		return absoluteFilePath;
	}

	public void setFile(File file) {
		this.absoluteFilePath = file.getAbsolutePath();
	}
	
}
