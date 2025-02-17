package alexoft.Minebackup;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;


public class MineBackup extends JavaPlugin {

	public CommandSender commandSender;

	private QuitListener quitListener;

	private Configuration cfg;

	public String backupDir;

	public String msg_BackupStarted;

	public String msg_BackupEnded;

	public String msg_BackupStartedUser;

	public int interval;

	public boolean backupIfNoPlayers;

	public boolean backupOnLastPlayerQuit;

	public int maxBackupsToKeep;

	public int taskID;

	public List<String> worlds;

	public boolean isBackupStarted;

	public void onDisable() {
		try {
			getServer().getScheduler().cancelTasks(this);
			worlds = null;
			cfg = null;
			log("version " + getDescription().getVersion() + " disabled");
		} catch (Exception e) {
			logException(e);
		}

	}

	public void onEnable() {
		try {
			isBackupStarted = false;
			loadConfig();
			launchThreads();
			quitListener = new QuitListener(this);
			getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, quitListener, Event.Priority.Normal, this);
			
			// Get the CraftBukkit CommandSender
			commandSender = ((CraftServer) Bukkit.getServer()).getServer().console;
			getCommand("minebackup").setExecutor(new BackupCommandExecutor(this));
			log("version " + getDescription().getVersion() + " ready");
		} catch (Exception var2) {
			logException(var2);
		}

	}

	public void launchThreads() {
		getServer().getScheduler().cancelTasks(this);

		if (maxBackupsToKeep > 0) {
			getServer().getScheduler().scheduleAsyncRepeatingTask(this, new CleanerThread(this), 0L, interval * 2L);
		} else {
			log("Not deleting old backups!");
		}

		taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, new BackupThread(this), interval, interval);
	}

	public void loadConfig() {
		boolean rewrite = false;
		List<String> allowedKeys = Arrays.asList(
			"worlds", "backup-dir", "interval", "if-no-players",
			"on-last-player-quit", "backups-to-keep",
			"messages.backup-started", "messages.backup-started-user", "messages.backup-ended"
		);

		cfg = new Configuration(new File(getDataFolder(), "config.yml"));
		cfg.load();

		// Remove any unknown settings
		int removedKeys = 0;
		Set<String> keys = cfg.getAll().keySet();

		for (String key: keys) {
			if (!allowedKeys.contains(key)) {
				cfg.removeProperty(key);
				removedKeys++;
			}
		}

		if (removedKeys > 0) {
			log(Level.WARNING, "Removed " + removedKeys + " unknown key(s)");
		}

		// Load worlds or set default if missing
		worlds = cfg.getStringList("worlds", null);
		if (worlds == null || worlds.isEmpty()) {
			log(Level.WARNING, "Adding 'worlds' to config...");

			for (World w: getServer().getWorlds()) {
				worlds.add(w.getName());
			}

			cfg.setProperty("worlds", worlds);
			rewrite = true;
		}

		// Set other default values if keys are missing
		backupDir = cfg.getString("backup-dir", null);
		if (backupDir == null) {
			log(Level.WARNING, "Adding 'backup-dir' to config...");
			cfg.setProperty("backup-dir", "minebackup");
			backupDir = "minebackup";
			rewrite = true;
		}

		interval = cfg.getInt("interval", -1);
		if (interval <= 0) {
			log(Level.WARNING, "Adding 'interval' to config...");
			cfg.setProperty("interval", 3600);
			interval = 3600;
			rewrite = true;
		}
		interval *= 20; // Convert to ticks
		
		backupIfNoPlayers = cfg.getBoolean("if-no-players", false);
		backupOnLastPlayerQuit = cfg.getBoolean("on-last-player-quit", true);

		maxBackupsToKeep = cfg.getInt("backups-to-keep", 0);
		if (maxBackupsToKeep == 0 || maxBackupsToKeep < -1) {
			log(Level.WARNING, "Adding 'backups-to-keep' to config...");
			cfg.setProperty("backups-to-keep", 2);
			maxBackupsToKeep = 2;
			rewrite = true;
		}

		msg_BackupStarted = cfg.getString("messages.backup-started", null);
		if (msg_BackupStarted == null) {
			log(Level.WARNING, "Adding 'messages.backup-started' to config...");
			cfg.setProperty("messages.backup-started", ChatColor.GREEN + "[MineBackup Reloaded] Backup started");
			msg_BackupStarted = ChatColor.GREEN + "[MineBackup Reloaded] Backup started";
			rewrite = true;
		}

		msg_BackupStartedUser = cfg.getString("messages.backup-started-user", null);
		if (msg_BackupStartedUser == null) {
			log(Level.WARNING, "Adding 'messages.backup-started-user' to config...");
			cfg.setProperty("messages.backup-started-user", ChatColor.GREEN + "[MineBackup Reloaded] Backup started by %player%");
			msg_BackupStartedUser = ChatColor.GREEN + "[MineBackup Reloaded] Backup started by %player%";
			rewrite = true;
		}

		msg_BackupEnded = cfg.getString("messages.backup-ended", null);
		if (msg_BackupEnded == null) {
			log(Level.WARNING, "Adding 'messages.backup-ended' to config...");
			cfg.setProperty("messages.backup-ended", ChatColor.GREEN + "[MineBackup Reloaded] Backup ended");
			msg_BackupEnded = ChatColor.GREEN + "[MineBackup Reloaded] Backup ended";
			rewrite = true;
		}

		// Generate the config header
		StringBuilder headerText = new StringBuilder("# Set 'backups-to-keep' to -1 to never delete backups.\n#\n# Available worlds:\n");

		for (World w: getServer().getWorlds()) {
			headerText.append("# - ").append(w.getName()).append("\n");
		}

		cfg.setHeader(headerText.toString());

		// Save the config if we made any changes
		if (rewrite) {
			cfg.save();
		}

		log(Level.INFO, worlds.size() + " worlds loaded.");
	}

	// Log handlers
	public void log(Level level, String logText) {
		getServer().getLogger().log(level, "[MineBackup Reloaded] " + logText);
	}

	public void log(String logText) {
		log(Level.INFO, logText);
	}
	
	//Used to send command response messages to players and console
	public void sendMessage(CommandSender sender, String message) {
		sender.sendMessage("[MineBackup Reloaded] " + message);
	}
	
	public int getBackupCount() {
		String[] backupFiles = new File(backupDir).list((dir, name) -> name.endsWith(".zip"));
		return backupFiles.length;
	}

	// Pretty error logs!
	public void logException(Throwable e) {
		log(Level.SEVERE, "---------------------------------------");
		log(Level.SEVERE, "--- an unexpected error has occured ---");
		log(Level.SEVERE, "-- please send line below to the dev --");
		log(Level.SEVERE, e.toString() + " : " + e.getLocalizedMessage());

		StackTraceElement[] element = e.getStackTrace();
		int elementLength = element.length;

		for (int i = 0; i < elementLength; ++i) {
			log(Level.SEVERE, "\t" + element[i].toString());
		}

		log(Level.SEVERE, "---------------------------------------");
	}
}