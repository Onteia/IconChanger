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

	private String twitchChannelName;
	private Guild discordServer;
	//public String discordServerID;
	//public String liveIconLink;
	//public String offlineIconLink;
	
	private File liveIcon;
	private File offlineIcon;
	
	private StreamStatus streamStatus;
	
	public Server(String twitchChannelName, Guild discordServer, File liveIcon, File offlineIcon) {
		
		this.twitchChannelName = twitchChannelName;
		this.discordServer = discordServer;
		this.liveIcon = liveIcon;
		this.offlineIcon = offlineIcon;
		
		this.setStreamStatus(StreamStatus.OFFLINE);
		
		LOG.info("Server: Discord server [" + discordServer.getId() + "] was added!");
		
	}
	
	
	
	
	public String getChannelName() {
		return this.twitchChannelName;
	}
	
	public String getServerID() {
		return this.discordServer.getId();
	}
	
	public File getLiveIcon() {	
		return this.liveIcon;
	}
	
	public File getOfflineIcon() {
		return this.offlineIcon;
	}

	public StreamStatus getStreamStatus() {
		return streamStatus;
	}

	public void setStreamStatus(StreamStatus streamStatus) {
		this.streamStatus = streamStatus;
	}
	
}
