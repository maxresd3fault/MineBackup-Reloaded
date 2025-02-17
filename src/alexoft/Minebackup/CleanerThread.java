package alexoft.Minebackup;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;


public class CleanerThread extends Thread {

	private MineBackup plugin;

	public CleanerThread(MineBackup plugin) {
		this.plugin = plugin;
	}

	public void run() {
		try {

			// Get the list of backup files directly in the backup directory
			File backupDir = new File(plugin.backupDir);
			String[] backupFiles = backupDir.list((dir, name) -> name.endsWith(".zip"));

			// If no backup files are found, return
			if (backupFiles == null || backupFiles.length == 0) {
				return;
			}

			if (backupFiles.length <= plugin.maxBackupsToKeep) {
				return; // No need to delete if there are fewer backups than the maximum limit
			}

			// Sort the files by date (oldest first)
			Arrays.sort(backupFiles);

			// Loop through the backup files and delete the oldest ones until we reach the desired count
			int bckDeleted = 0;

			for (int i = 0; i < backupFiles.length - plugin.maxBackupsToKeep; ++i) {
				File file = new File(backupDir, backupFiles[i]);

				if (file.exists()) {
					boolean deleted = file.delete();

					if (deleted) {
						plugin.log("Deleted " + file.getName());
						++bckDeleted;
					}
				}
			}

			if (bckDeleted > 0) {
				plugin.log(bckDeleted + " backup file(s) deleted");
			}

		} catch (Exception var9) {
			plugin.log(Level.SEVERE, "Error during backup cleanup: " + var9.getMessage());
			var9.printStackTrace();
		}
	}
}