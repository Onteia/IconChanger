package iconChanger;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;

// Manages the discord servers and keeps track of what server is
// linked to what twitch stream

public class Server {

	private final static Logger LOG = LoggerFactory.getLogger(Server.class);

	public enum StreamStatus {
		LIVE,
		OFFLINE;
	}
	
	public enum IconType {
		NONE,
		TOP,
		MIDDLE,
		BOTTOM,
		CIRCLE;
	}

	private String twitchChannelName;
	private Guild discordServer;
	//public String discordServerID;
	//public String liveIconLink;
	//public String offlineIconLink;
	
	private File offlineIcon;
	
	private IconType iconType;
	private StreamStatus streamStatus;
	
	public Server(String twitchChannelName, Guild discordServer) {
		
		this.twitchChannelName = twitchChannelName;
		this.discordServer = discordServer;
		
		this.setIconType(IconType.NONE);
		this.setStreamStatus(StreamStatus.OFFLINE);
		
		String iconLocation = IconChanger.IMAGE_FOLDER_PATH + twitchChannelName + discordServer.getId() + ".png";
		this.offlineIcon = ImageProcessing.downloadIcon(discordServer, iconLocation);
		
		System.out.println(this.offlineIcon.toString());
		LOG.info("Server: Discord server [" + discordServer.getId() + "] was added!");
		
	}
	
	
	
	
	public String getChannelName() {
		return this.twitchChannelName;
	}
	
	public String getServerID() {
		return this.discordServer.getId();
	}
	
	public Icon getLiveIcon() {
		//return this.liveIcon;
		
		//combine the icon with the overlay
		
		return null;
	}
	
	public File getOfflineIcon() {
		return this.offlineIcon;
	}




	public IconType getIconType() {
		return iconType;
	}




	public void setIconType(IconType iconType) {
		this.iconType = iconType;
	}




	public StreamStatus getStreamStatus() {
		return streamStatus;
	}




	public void setStreamStatus(StreamStatus streamStatus) {
		this.streamStatus = streamStatus;
	}
	
}
