package iconChanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
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
			).queue();
	}
	
	//possibly add onGuildLeave to delete any info related to server
	public void onGuildLeave(GuildLeaveEvent event) {
		//
	}
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(event.getName().equals("setup")) {
			String channel = event.getOption("channel").getAsString();
			
			
			
			
//			//check that there's only one channel linked
//			
//			
//			//go through the process of getting the offline and live icons, and the twitch user
//			
//			//this will cerate the specific server object
//			
//			//then map the twitch channel name to the server object with this line
//			String channelName = "";
//			Server newServer = null;
//			IconChanger.map.put(channelName, newServer);
			
			
			event.reply(channel + " has been linked!").queue();
		}
	}
	
}
