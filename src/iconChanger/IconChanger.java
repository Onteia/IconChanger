package iconChanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		
		
		//read the serialized map file
		loadMap();
		
		//call some function in Server that turns the null discordServer 
		//variable into a Guild object from the serverId (jda.getGuildById)
		initialize();
		
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
		saveMap();
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
		saveMap();
	}
	
	private static boolean subscribe(String channel) {
		boolean channelSubscribed = twitchClient.getClientHelper().enableStreamEventListener(channel) != null;
		if(!channelSubscribed) {
			LOG.error("subscribe: unable to subscribe to " + channel);
		}
		return channelSubscribed;
	}
	
	private static void initialize() {
		channelToServer.keys().forEach(channel -> {
			
			//subscribe to stream events of channels in the map
			subscribe(channel);
			
			channelToServer.get(channel).forEach(server -> {
				boolean status = server.initialize();
				
				if(status) {	
					LOG.info("initialize: [" + server.getServerID() + "] successfully initialized!");
				} else {
					LOG.error("initialize: unable to initialize [" + server.getServerID() + "]!");
				}
			});
		});
	}
	
	@SuppressWarnings("unchecked")
	private static void loadMap() {
		
		try (
				FileInputStream fileIn = new FileInputStream(THIS_FOLDER_PATH + File.separator + "savedata");
				ObjectInputStream in = new ObjectInputStream(fileIn);
		) {
			
			channelToServer = (ArrayListMultimap<String, Server>) in.readObject();
			
		} catch (IOException e) {
			LOG.error("readSaveFile: unable to read the save file!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			LOG.error("readSaveFile: class of the save file couldn't be determined!");
		}
		
	}
	
	public static void saveMap() {
		
		try (
				FileOutputStream fileOut = new FileOutputStream(IconChanger.THIS_FOLDER_PATH + File.separator + "savedata");
				ObjectOutputStream out = new ObjectOutputStream(fileOut);
		){
			out.writeObject(IconChanger.channelToServer);

		} catch (IOException e) {
			LOG.error("saveMap: unable to write the map to the save file!");
		}
		
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
