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
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Setup extends ListenerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(Setup.class);
	
	/*
	public void onMessageReceived(MessageReceivedEvent event) {
		
		Message message = event.getMessage();
		Member member = event.getMember();
		MessageChannel channel = event.getChannel();
		
		if(member == null) {
			LOG.error("onMessageReceived: Member is null!");
            return;
		}
		
		//if the user is this bot, then return
		final String botId = "1072033783500517437";
		if(member.getId().equalsIgnoreCase(botId)) return;
		
		//split based on spaces
		String[] messageList = message.getContentStripped().split(" ");
		
		//command to set up icon synchronization
		if(messageList[0].equalsIgnoreCase("!iconsetup") && member.getPermissions().contains(Permission.MANAGE_SERVER)) {
			
			//if the only arg is "!iconsetup", then tell user to include channel link 
			if(messageList.length == 1) {
				channel.sendMessage("Include the channel link or name! example: `!iconsetup Jerma985` or `!iconsetup https://www.twitch.tv/Jerma895`");
			}
			
			event.getChannel().sendMessage("beginning setup...").queue();
			
			
			
		}
		
		
		
	}
	
	*/
	
	//set up slash commands when bot joins server
	public void onGuildJoin(GuildJoinEvent event) {
		Guild guild = event.getGuild();
		
		guild.updateCommands().addCommands(
				Commands.slash("setup", "set up icon changing :)")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_SERVER))
				.addOption(OptionType.STRING, "channel", "the twitch channel to link to the icon", true)
				.addOption(OptionType.ATTACHMENT, "live-icon", "the icon the discord server should switch to", true)
				.addOption(OptionType.ATTACHMENT, "offline-icon", "upload an offline icon instead of using the current server icon", false)
			).queue();
		
		//maybe could add a /reset command
	}
	
	//possibly add onGuildLeave to delete any info related to server
	public void onGuildLeave(GuildLeaveEvent event) {
		//
	}
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(event.getName().equals("setup")) {
			String twitchChannel = event.getOption("channel").getAsString().toLowerCase();
			
			//IconChanger.map.put(channelId, newServer);
			
			boolean channelExists = false;
			
			try {
				channelExists = IconChanger.twitchClient.getClientHelper().enableStreamEventListener(twitchChannel) != null;
			} catch(Exception e) {
				event.reply("invalid twitch username!").setEphemeral(true).queue();
			}
			if(channelExists) {
				
				//create the stream object
				//then set that they're setup to true
				//and use that to check if they're duplicate
				try {
					
					final int WIDTH = 128;
					final int HEIGHT = 128;
					
					Guild discordServer = event.getGuild();
					
					String serverFolderPath = IconChanger.IMAGE_FOLDER_PATH + discordServer.getId() + File.separator;
					File serverFolder = new File(serverFolderPath);
					serverFolder.mkdir();
					
					File liveIconLocation = new File(serverFolderPath + "live.png");
					liveIconLocation.createNewFile();
					File liveIcon = event.getOption("live-icon").getAsAttachment().getProxy().downloadToFile(liveIconLocation, WIDTH, HEIGHT).get();
					
					
					
					File offlineIconLocation = new File(serverFolderPath+ "offline.png");
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
					
					
					event.reply(twitchChannel + " has been linked to this server!").complete();
					
					//LOG that the server has been linked to the channel
					
				} catch (InterruptedException e) {
					LOG.error("onSlashCommandInteraction: unable to download live-icon to the specified file!");
				} catch (ExecutionException e) {
					LOG.error("onSlashCommandInteraction: exception on trying to get the proxied image as a file object!");
				} catch (IOException e) {
					LOG.error("onSlashCommandInteraction: unable to create location files!");
				}
				
			} else {
				event.reply(twitchChannel + " does not exist! double check the spelling!").setEphemeral(true).queue();
			}
		}
	}
	
}
