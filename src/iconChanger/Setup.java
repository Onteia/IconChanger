package iconChanger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.entities.Message.Attachment;
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
			event.reply("your live icon has been reset!").queue();
		}
		
	}
	
	private void setup(SlashCommandInteractionEvent event) {

		String twitchChannel = event.getOption("channel").getAsString().toLowerCase();
		
		boolean channelExists = false;
		
		//makes IconChanger listen to that stream for events
		try {
			channelExists = IconChanger.twitchClient.getClientHelper().enableStreamEventListener(twitchChannel) != null;
		} catch (Exception e) {
			event.reply("invalid channel name!").setEphemeral(true).queue();
			return;
		}
		
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
				
				Attachment liveAttachment = event.getOption("live-icon").getAsAttachment();
				//is liveAttachment an image
				if(!liveAttachment.isImage()) {
					//if the attachment isn't a picture
					event.reply("your live-icon must be an image!").setEphemeral(true).queue();
					return;
				}
				
				//used for getting the smaller value between 128x128 and the downloaded image's dimensions
				int liveWidth = liveAttachment.getWidth();
				int liveHeight = liveAttachment.getHeight();
				
				Attachment offlineAttachment = null;
				//used for getting the smaller value between 128x128 and the downloaded image's dimensions
				int offlineWidth = WIDTH;
				int offlineHeight = HEIGHT;
				try {
					offlineAttachment = event.getOption("offline-icon").getAsAttachment();
					//is offlineAttachment an image
					if(!offlineAttachment.isImage()) {
						//if the attachment isn't a picture
						event.reply("your offline-icon must be an image!").setEphemeral(true).queue();
						return;
					}
					
					offlineWidth = offlineAttachment.getWidth();
					offlineHeight = offlineAttachment.getHeight();
					
				} catch (Exception e) {
					//no optional second file
				}
				
				String serverFolderPath = IconChanger.IMAGE_FOLDER_PATH + discordServer.getId() + File.separator;
				File serverFolder = new File(serverFolderPath);
				serverFolder.mkdir();
				
				File liveIconLocation = new File(serverFolderPath + "live.png");
				liveIconLocation.createNewFile();
				File liveIcon = liveAttachment.getProxy()
						.downloadToFile(liveIconLocation, Math.min(WIDTH, liveWidth), Math.min(HEIGHT, liveHeight))
						.get();
				
				File offlineIconLocation = new File(serverFolderPath + "offline.png");
				offlineIconLocation.createNewFile();
				File offlineIcon;
				try {
					offlineIcon = offlineAttachment.getProxy()
							.downloadToFile(
									offlineIconLocation, 
									Math.min(WIDTH, 
											offlineWidth), 
									Math.min(HEIGHT, 
											offlineHeight))
							.get();
					//set the server's icon to this offline icon
					Icon newIcon = Icon.from(offlineIcon);
					discordServer.getManager().setIcon(newIcon).queue();
				} catch (Exception e) {
					//if optional argument is null
					offlineIcon = discordServer.getIcon().downloadToFile(offlineIconLocation).get();
				}
				
				Server newServer = new Server(twitchChannel, discordServer, liveIcon, offlineIcon);
				
				//add the channel and server to the map
				IconChanger.channelToServer.put(twitchChannel, newServer);
				
				event.reply(twitchChannel + " has been linked to this server!").queue();
				
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
			return;
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
