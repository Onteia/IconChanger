package iconChanger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Setup extends ListenerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(Setup.class);
	
	//set up slash commands when bot joins server
	public void onGuildJoin(GuildJoinEvent event) {
		Guild guild = event.getGuild();
		
		guild.updateCommands().addCommands(
				Commands.slash("setup", "set up icon changing :)")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
				.addOption(OptionType.STRING, "channel", "the twitch channel to link to the icon", true)
				.addOption(OptionType.ATTACHMENT, "live-icon", "the icon the discord server should switch to", true)
				.addOption(OptionType.ATTACHMENT, "offline-icon", "upload an offline icon instead of using the current server icon", false)
			).addCommands(
				Commands.slash("reset", "resets your server's live icon")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
			).queue();
	}
	
	public void onGuildLeave(GuildLeaveEvent event) {	
		reset(event.getGuild());
	}
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		
		String eventName = event.getName();
		
		if(eventName.equals("setup")) {
			setup(event);
		} else if(eventName.equals("reset")) {
			reset(event.getGuild());
			event.reply("your live icon has been reset!").complete();
		}
		
	}
	
	private void setup(SlashCommandInteractionEvent event) {

		String twitchChannel = event.getOption("channel").getAsString().toLowerCase();
		
		boolean channelExists = false;
		
		//makes IconChanger listen to that stream for events
		channelExists = IconChanger.twitchClient.getClientHelper().enableStreamEventListener(twitchChannel) != null;

		if(channelExists) {
			
			try {
				
				final int WIDTH = 128;
				final int HEIGHT = 128;
				
				Guild discordServer = event.getGuild();
				
				if(Server.getServer(discordServer) != null) {
					LOG.info("setup: duplicate found, resetting previous instance!");
					//if the server already exists, reset it first
					reset(discordServer);
				}
				
				String serverFolderPath = IconChanger.IMAGE_FOLDER_PATH + discordServer.getId() + File.separator;
				File serverFolder = new File(serverFolderPath);
				serverFolder.mkdir();
				
				File liveIconLocation = new File(serverFolderPath + "live.png");
				liveIconLocation.createNewFile();
				File liveIcon = event.getOption("live-icon").getAsAttachment().getProxy().downloadToFile(liveIconLocation, WIDTH, HEIGHT).get();
				
				File offlineIconLocation = new File(serverFolderPath + "offline.png");
				offlineIconLocation.createNewFile();
				File offlineIcon;
				try {
					offlineIcon = event.getOption("offline-icon").getAsAttachment().getProxy().downloadToFile(offlineIconLocation, WIDTH, HEIGHT).get();
					//set the server's icon to this offline icon
					Icon newIcon = Icon.from(offlineIcon);
					discordServer.getManager().setIcon(newIcon).complete();
				} catch (Exception e) {
					//if optional argument is null
					offlineIcon = discordServer.getIcon().downloadToFile(offlineIconLocation).get();
				}
				
				Server newServer = new Server(twitchChannel, discordServer, liveIcon, offlineIcon);
				//IconChanger.map.put(channelId, newServer);
				IconChanger.channelToServer.put(twitchChannel, newServer);
				
				event.reply(twitchChannel + " has been linked to this server!").complete();
				
				//LOG that the server has been linked to the channel
				LOG.info("setup: " + twitchChannel + " has been linked to [" + discordServer.getId() + "]!");
				
			} catch (InterruptedException e) {
				LOG.error("setup: unable to download live-icon to the specified file!");
			} catch (ExecutionException e) {
				LOG.error("setup: exception on trying to get the proxied image as a file object!");
			} catch (IOException e) {
				LOG.error("setup: unable to create location files!");
			}
			
		} else {
			event.reply(twitchChannel + " does not exist or this server has already been set up. use `/reset` if this is the case.").setEphemeral(true).queue();
		}
	
	}
	
	
	private void reset(Guild discordServer) {
		
		Server server = Server.getServer(discordServer);
		
		File parentFolder = server.getLiveIcon().getParentFile();
		//delete the live icon
		server.getLiveIcon().delete();
		//delete the offline icon
		server.getOfflineIcon().delete();
		//delete the parent folder
		parentFolder.delete();
		
		//stop listening to the channel
		IconChanger.twitchClient.getClientHelper().disableStreamEventListener(server.getChannelName());
		
		//remove the Server from the map
		IconChanger.channelToServer.remove(server.getChannelName(), server);
		
		LOG.info("reset: [" + discordServer.getId() + "] has been reset!");
		
	}

}
