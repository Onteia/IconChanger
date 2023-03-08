package iconChanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential;
import com.github.philippheuer.events4j.core.EventManager;
import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.TwitchClient;
import com.github.twitch4j.TwitchClientBuilder;
import com.github.twitch4j.events.ChannelGoLiveEvent;
import com.github.twitch4j.events.ChannelGoOfflineEvent;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import iconChanger.Server.StreamStatus;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Icon;

// TODO: 
// 	finish the description of this file
// 	make the botId in Setup.java take the id from config.properties
//		instead of a hard-coded value

// Entry point of IconChanger
// does stuff

public class IconChanger {

	public static JDA jda;
	public static TwitchClient twitchClient;
	
	private static String discordToken;
	private static String twitchToken;
	private static final String CONFIG_FILE = "src/config.properties";
	public static String THIS_FOLDER_PATH;
	public static String IMAGE_FOLDER_PATH;
	
	public static String test_server = "264217465305825281";
	public static String test_channel = "280546532728504320";
	
	//public static HashMap<String, Server> map = new HashMap<String, Server>();
	public static Multimap<String, Server> channelToServer = ArrayListMultimap.create();
	private final static Logger LOG = LoggerFactory.getLogger(IconChanger.class);
	
	
	public static void main(String[] args) throws Exception {
		
		try (
			FileInputStream config = new FileInputStream(CONFIG_FILE);
		) {
			Properties prop = new Properties();
			prop.load(config);
			THIS_FOLDER_PATH = prop.getProperty("THIS_FOLDER_PATH");
			IMAGE_FOLDER_PATH = prop.getProperty("IMAGE_FOLDER_PATH");
		} catch (FileNotFoundException e) {
			//no config file
			THIS_FOLDER_PATH = "/home/orangepi/jars/IconChanger/";
			IMAGE_FOLDER_PATH = "/home/orangepi/jars/IconChanger/src/images/";
		} finally {
			//load credentials
			loadCredentials();
		}

		// set up JDA
		jda = JDABuilder.createLight(discordToken).build();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		jda.getPresence().setActivity(Activity.playing("/setup"));
		jda.addEventListener(new Setup());
		
		// set up Twitch4J
		EventManager eventManager = new EventManager();
		eventManager.autoDiscovery();
		eventManager.setDefaultEventHandler(SimpleEventHandler.class);
		
		OAuth2Credential credential = new OAuth2Credential("twitch", twitchToken);
		
		twitchClient = TwitchClientBuilder.builder()
				.withDefaultAuthToken(credential)
				.withEventManager(eventManager)
				.withDefaultEventHandler(SimpleEventHandler.class)
				.withEnableHelix(true)
				.build();
		
		// if the channel goes live
		eventManager.onEvent(ChannelGoLiveEvent.class, event -> {
			String channelId = event.getChannel().getName().toLowerCase();
			channelWentLive(channelId);
		});
		
		
		// if the channel goes offline
		eventManager.onEvent(ChannelGoOfflineEvent.class, event -> {
			String channelId = event.getChannel().getName().toLowerCase();
			channelWentOffline(channelId);
		});
	}
	
	private static void channelWentLive(String channelName) {
		
		//hash map the channelName to get the Server object
		Collection<Server> serversToUpdate = channelToServer.get(channelName);
		
		serversToUpdate.forEach(server -> {
			try {
				File liveIconFile = server.getLiveIcon();
				Icon liveIcon = Icon.from(liveIconFile);
				
				String guildId = server.getServerID();
				//set the server's icon to the live icon
				jda.getGuildById(guildId).getManager().setIcon(liveIcon).complete();
				
				if(server.getStreamStatus() == StreamStatus.LIVE) {
					//if the channel is already somehow live, there is an error
					LOG.error("channelWentLive: INCONSISTENCY!!! " + channelName + " was already live for [" + guildId + "]!");
				}
				
				server.setStreamStatus(StreamStatus.LIVE);
				
				LOG.info("channelWentLive: " + channelName + "! updated [" + server.getServerID() + "]!");
			} catch (IOException e) {
				LOG.error("channelWentLive: unable to convert the liveIconFile to an Icon!");
			}
		});
		
	}
	
	private static void channelWentOffline(String channelName) {
		
		//hash map the channelName to get the Server object
		Collection<Server> serversToUpdate = channelToServer.get(channelName);
		
		serversToUpdate.forEach(server -> {
			try {
				File offlineIconFile = server.getOfflineIcon();
				Icon offlineIcon = Icon.from(offlineIconFile);
				
				String guildId = server.getServerID();
				//set the server's icon to the offline icon
				jda.getGuildById(guildId).getManager().setIcon(offlineIcon).complete();
				
				if(server.getStreamStatus() == StreamStatus.OFFLINE) {
					//if the channel is already somehow live, there is an error
					LOG.error("channelWentLive: INCONSISTENCY!!! " + channelName + " was already offline for [" + guildId + "]!");
				}
				
				server.setStreamStatus(StreamStatus.OFFLINE);
				
				LOG.info("channelWentOffline: " + channelName + "! updated [" + server.getServerID() + "]!");
			} catch (IOException e) {
				LOG.error("channelWentOffline: unable to convert the offlineIconFile to an Icon!");
			}
		});
		
	}
	
	private static void loadCredentials() {
		//load discord and twitch credentials
		try {
			
			// get the files
			File discordTokenFile = new File(THIS_FOLDER_PATH + "discord.auth");
			File twitchTokenFile = new File(THIS_FOLDER_PATH + "twitch.auth");
			
			// read the files
			// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
			try (
				BufferedReader br1 = new BufferedReader(new FileReader(discordTokenFile));
				BufferedReader br2 = new BufferedReader(new FileReader(twitchTokenFile));
			) {
				discordToken = br1.readLine();
				twitchToken = br2.readLine();
			}
		} catch (Exception e) {
		    LOG.error("loadCredentials: Authentication Failed!");
		}
	}
	
}
