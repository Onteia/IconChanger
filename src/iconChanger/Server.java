package iconChanger;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;

// Manages the discord servers and keeps track of what server is
// linked to what twitch stream

public class Server implements Serializable {

	private final static Logger LOG = LoggerFactory.getLogger(Server.class);
	private final static long serialVersionUID = 1L;
	
	private transient static HashMap<Guild, Server> guildToServer = new HashMap<Guild, Server>();
	
	public enum StreamStatus {
		LIVE,
		OFFLINE;
	}

	private String twitchChannelName;
	private transient Guild discordServer;
	private String serverId;
	private File liveIcon;
	private File offlineIcon;
	
	private StreamStatus streamStatus;
	
	public Server(String twitchChannelName, Guild discordServer, File liveIcon, File offlineIcon) {
		
		this.twitchChannelName = twitchChannelName;
		this.discordServer = discordServer;
		this.serverId = discordServer.getId();
		this.liveIcon = liveIcon;
		this.offlineIcon = offlineIcon;
		
		this.setStreamStatus(StreamStatus.OFFLINE);
		
		//adds the server to the server list for future way of getting it
		guildToServer.put(discordServer, this);
				
		LOG.info("Server: Discord server [" + discordServer.getId() + "] was added!");
		
	}
	
	public static Server getServer(Guild discordServer) {
		return guildToServer.get(discordServer);	
	}
	
	public boolean initialize() {
		//get the guild from the guild id
		this.discordServer = IconChanger.jda.getGuildById(this.getServerID());
		
		//add the guild to guildToServer list
		guildToServer.put(this.discordServer, this);
		
		//return true if the server is not null
		//return false if the server is null
		return this.discordServer != null;
	}
	
	public String getChannelName() {
		return this.twitchChannelName;
	}
	
	public String getServerID() {
		//return this.discordServer.getId();
		return this.serverId;
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
