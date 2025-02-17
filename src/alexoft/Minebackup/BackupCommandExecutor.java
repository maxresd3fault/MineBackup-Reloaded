package alexoft.Minebackup;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class BackupCommandExecutor implements CommandExecutor {

	private MineBackup plugin;

	public BackupCommandExecutor(MineBackup plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		String senderName = sender.getName();

		if (!sender.isOp()) {
			plugin.sendMessage(sender, "You don't have permission to do that!");
			return true;
		} else if (senderName != null && !"".equals(senderName)) {
			if (args.length == 0) {
				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new BackupThread(plugin, senderName));
				return true;
			} else {
				switch (args[0].toLowerCase()) {
					default:
						return false;
					case "version":
						plugin.sendMessage(sender, plugin.getDescription().getVersion());
						return true;
					case "status":
						plugin.sendMessage(sender, "Currently storing " + plugin.getBackupCount() + " backups.");
						return true;
					case "reload":
						plugin.loadConfig();
						plugin.launchThreads();
						return true;
				}
			}
		}
		return false;
	}
}