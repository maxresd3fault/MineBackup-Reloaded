package alexoft.Minebackup;

import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerListener;

public class QuitListener extends PlayerListener {

	private MineBackup plugin;
	private static boolean lastPlayerQuitTriggered = false;

    public QuitListener(MineBackup plugin) {
        this.plugin = plugin;
    }
    
    public void onPlayerQuit(PlayerQuitEvent event) {
        
    	try {
			runBackup();
		} catch (Exception e) {
			plugin.logException(e);
		}

    }
    
    public void runBackup() throws Exception {
    	
        if(plugin.backupOnLastPlayerQuit && !plugin.isBackupStarted && Bukkit.getOnlinePlayers().length == 1) {
        	lastPlayerQuitTriggered = true;
        	plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new BackupThread(plugin));
        	
        }
    	
    }
    
    public static boolean shouldRunBackup() {
        if (lastPlayerQuitTriggered) {
        	lastPlayerQuitTriggered = false;
            return true;
        }
        return false;
    }
    
}