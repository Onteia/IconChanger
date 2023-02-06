package iconChanger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		String botId = "1072033783500517437";
		if(member.getId().equalsIgnoreCase(botId)) return;
		
		event.getChannel().sendMessage("im back").queue();
		
	}
	
}
