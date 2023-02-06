package iconChanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;


public class IconChanger {

	public static JDA jda;
	
	private static String discordToken;
	private static String twitchToken;
	private static final String CONFIG_FILE = "src/config.properties";
	private static String THIS_FOLDER_PATH;
	
	private final static Logger LOG = LoggerFactory.getLogger(IconChanger.class);
	
	
	public static void main(String[] args) throws Exception {
		
		try (
			FileInputStream config = new FileInputStream(CONFIG_FILE);
		) {
			Properties prop = new Properties();
			prop.load(config);
			THIS_FOLDER_PATH = prop.getProperty("THIS_FOLDER_PATH");
		} catch (FileNotFoundException e) {
			//no config file
			THIS_FOLDER_PATH = "/home/orangepi/jars/AspectiBot/";
		} finally {
			//load credentials
			loadCredentials();
		}

		jda = JDABuilder.createLight(discordToken).build();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);
		jda.addEventListener(new Setup());
		
		
	}
	
	private static void loadCredentials() {
		//load discord and twitch credentials
		try {
			
			// get the files
			File discordTokenFile = new File(THIS_FOLDER_PATH + "discord.auth");
			//File twitchTokenFile = new File(THIS_FOLDER_PATH + "twitch.auth");
			
			// read the files
			// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
			try (
				BufferedReader br1 = new BufferedReader(new FileReader(discordTokenFile));
				//BufferedReader br2 = new BufferedReader(new FileReader(twitchTokenFile));
			) {
				discordToken = br1.readLine();
				//twitchToken = br2.readLine();
			}
		} catch (Exception e) {
		    LOG.error("loadCredentials: Authentication Failed!");
		}
	}
	
}
