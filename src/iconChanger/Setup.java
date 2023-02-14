package iconChanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Setup extends ListenerAdapter {

	private final static Logger LOG = LoggerFactory.getLogger(Setup.class);
	
	public void onMessageReceived(MessageReceivedEvent event) {
		
		Message message = event.getMessage();
		Member member = event.getMember();
		
		if(member == null) {
			LOG.error("onMessageReceived: Member is null!");
            return;
		}
		
		//if the user is this bot, then return
		final String botId = "1072033783500517437";
		if(member.getId().equalsIgnoreCase(botId)) return;
		
		//command to set up icon synchronization
		if(message.getContentStripped().equalsIgnoreCase("!iconsetup") && member.getPermissions().contains(Permission.MANAGE_SERVER)) {
			event.getChannel().sendMessage("beginning setup...").queue();
			
			//go through the process of getting the offline and live icons, and the twitch user
			
		}
		
		
		
	}
	
}
