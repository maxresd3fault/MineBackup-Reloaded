package alexoft.Minebackup;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.World;


public class BackupThread extends Thread {

	private MineBackup plugin;

	public boolean userStarted;

	public String userName;

	// Called at plugin init
	public BackupThread(MineBackup plugin) {
		this.plugin = plugin;
		this.userStarted = false;
	}

	// For when a manual backup is called
	public BackupThread(MineBackup plugin, String userName) {
		this.plugin = plugin;
		this.userStarted = true;
		this.userName = userName;
	}

	public void MakeBackup() {
		try {
			plugin.log(Level.INFO, "Starting backup...");
			File tempDir = new File(String.valueOf(Math.random()));
			tempDir.mkdirs();
			Iterator<String> var3 = plugin.worlds.iterator();

			while (var3.hasNext()) {
				String w = (String) var3.next();
				plugin.log(Level.INFO, " * " + w);
				copyWorld(plugin.getServer().getWorld(w), tempDir);
			}

			ZipDir(tempDir);
		} catch (Exception var4) {
			plugin.log(Level.WARNING, "error; " + var4);
		}

	}

	public void ZipDir(File tempDir) {
		plugin.log(Level.INFO, "Compressing...");
		Calendar today = Calendar.getInstance();
		String currentFileName = format(today.get(5)) + "_" + format(today.get(2) + 1) + "_" + today.get(1) + "_" + today.get(11) + "_" + format(today.get(12));
		(new File(plugin.backupDir)).mkdirs();
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ZipDir(plugin, this, tempDir.getPath(), plugin.backupDir + "/" + currentFileName + ".zip"));
	}

	private void copyWorld(World world, File tempDir) throws IOException {
		world.save();
		plugin.getServer().savePlayers();

		try {
			DirUtils.copyDirectory(new File(world.getName()), new File(tempDir.getPath() + "/" + world.getName()));
		} catch (Exception var4) {
			DirUtils.deleteDirectory(tempDir);
			throw new IOException();
		}
	}

	private String format(int i) {
		String r = String.valueOf(i);

		if (r.length() == 1) {
			r = "0" + r;
		}

		return r;
	}

	public void afterZip() {
		plugin.log(Level.INFO, "Done !");
		plugin.getServer().dispatchCommand(plugin.commandSender, "save-on");
		plugin.getServer().broadcastMessage(plugin.msg_BackupEnded);
		plugin.isBackupStarted = false;
	}

	public void run() {

		// Extra check just in case an auto backup, player quit backup, or manual backup try to run at the same time
		if (plugin.isBackupStarted) {
			return;
		}

		if (userStarted || (plugin.backupOnLastPlayerQuit && QuitListener.shouldRunBackup()) || Bukkit.getOnlinePlayers().length > 0 || plugin.backupIfNoPlayers) {
			try {

				plugin.isBackupStarted = true;

				if (userStarted) {
					plugin.getServer().broadcastMessage(plugin.msg_BackupStartedUser.replaceAll("%player%", userName));
				} else {
					plugin.getServer().broadcastMessage(plugin.msg_BackupStarted);
				}

				plugin.getServer().dispatchCommand(plugin.commandSender, "save-off");
				plugin.getServer().dispatchCommand(plugin.commandSender, "save-all");
				MakeBackup();
			} catch (Exception e) {
				plugin.isBackupStarted = false;
				plugin.logException(e);
			}

		}

	}
}