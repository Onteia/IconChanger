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
		TOP,
		MIDDLE,
		BOTTOM,
		CIRCLE;
	}

	public String twitchChannelName;
	public String discordServerID;
	public String liveIconLink;
	public String offlineIconLink;
	
	public Server(String twitchChannelName, 
					String discordServerID,
					String liveIconLink,
					String offlineIconLink) {
		
		this.twitchChannelName = twitchChannelName;
		this.discordServerID = discordServerID;
		this.liveIconLink = liveIconLink;
		this.offlineIconLink = offlineIconLink;
		
		LOG.info("Server: New discord server was added!");
		
	}
	
	private static Icon iconFromUrl(String url) {
		
		Icon newServerIcon = null;
		
		try {
			URL iconURL = new URL(url);
			BufferedImage iconImage = ImageIO.read(iconURL);
			
			// converts the iconImage from a BufferedImage to a byte array
			byte[] buffer = ((DataBufferByte)(iconImage).getRaster().getDataBuffer()).getData();
			
			// use the JDA Icon() thing with byte[] to return an Icon object and set newServerIcon equal to that
			
			
		} catch (MalformedURLException e) {
			LOG.error("iconFromUrl: invalid URL: " + url + "!");
		} catch (IOException e) {
			LOG.error("iconFromUrl: unable to read iconURL!");
		}
		
		return newServerIcon;
		
	}
	
	
	
	public String getChannelName() {
		return this.twitchChannelName;
	}
	
	public String getServerID() {
		return this.discordServerID;
	}
	
	public File getLiveIcon() {
		//return this.liveIcon;
		return null;
	}
	
	public File getOfflineIcon() {
		//return this.offlineIcon;
		return null;
	}
	
}
