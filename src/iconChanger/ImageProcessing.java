package iconChanger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.FileUpload;

public class ImageProcessing {

	private final static Logger LOG = LoggerFactory.getLogger(ImageProcessing.class);
	

	public static File downloadIcon(Guild guild, String filepath) {
		
		File file = null;
		try {
			file = new File(filepath);
			file.createNewFile();
			guild.getIcon().downloadToFile(file).get();
		} catch (IOException e) {
			LOG.error("downloadIcon: unable to create the file!");
		} catch (IllegalArgumentException e) {
			LOG.error("downloadIcon: unable to download icon to the specified file!");
		} catch (InterruptedException e) {
			LOG.error("downloadIcon: thread was interrupted while waiting for file to download!");
		} catch (ExecutionException e) {
			LOG.error("downloadIcon: an error occurred while downloading the file!");
			e.printStackTrace();
		}	
		
		return file;
		
	}
	
	
	public static File createBanner(Server discordServer) {
		
		File banner = null;
		
		File backgroundFile = new File(IconChanger.IMAGE_FOLDER_PATH + "BACKGROUND.png");
		
		File iconImage = discordServer.getOfflineIcon();
		
		File overlayFile = new File(IconChanger.IMAGE_FOLDER_PATH + "OVERLAY.png");
		
		
		
		
		return banner;
		
	}
	
}
