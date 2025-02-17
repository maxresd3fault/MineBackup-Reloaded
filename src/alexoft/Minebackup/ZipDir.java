package alexoft.Minebackup;

import java.io.File;
import java.util.logging.Level;


public class ZipDir extends Thread {

	public BackupThread parent;

	public MineBackup plugin;

	public String srcDir;

	public String destDir;

	public ZipDir(MineBackup plugin, BackupThread parent, String source, String dest) {
		this.plugin = plugin;
		this.parent = parent;
		this.destDir = dest;
		this.srcDir = source;
	}

	public void run() {
		try {
			(new File(destDir)).createNewFile();
			ZipUtils.zipDir(srcDir, destDir);
			DirUtils.deleteDirectory(new File(srcDir));
			parent.afterZip();
		} catch (Exception var2) {
			plugin.log(Level.WARNING, "error; " + var2);
			(new File(destDir)).delete();
		}

	}
}