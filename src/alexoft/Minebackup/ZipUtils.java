package alexoft.Minebackup;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipUtils {

	public static void gunzip(String gzsource, String filedest) throws FileNotFoundException, IOException {
		GZIPInputStream in = new GZIPInputStream(new FileInputStream(gzsource));

		try {
			BufferedInputStream bis = new BufferedInputStream( in );

			try {
				FileOutputStream out = new FileOutputStream(filedest);

				try {
					BufferedOutputStream bos = new BufferedOutputStream(out);

					try {
						byte[] buf = new byte[8192];

						int len;

						while ((len = bis.read(buf, 0, 8192)) != -1) {
							bos.write(buf, 0, len);
						}

						buf = (byte[]) null;
					} finally {
						bos.close();
					}
				} finally {
					out.close();
				}
			} finally {
				bis.close();
			}
		} finally { in .close();
		}
	}

	public static void gzip(String filesource, String gzdest) throws FileNotFoundException, IOException {
		GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(gzdest));

		try {
			BufferedOutputStream bos = new BufferedOutputStream(out);

			try {
				FileInputStream in = new FileInputStream(filesource);

				try {
					BufferedInputStream bis = new BufferedInputStream( in );

					try {
						byte[] buf = new byte[8192];

						while (true) {
							int len;

							if ((len = bis.read(buf, 0, 8192)) <= 0) {
								buf = (byte[]) null;
								break;
							}

							bos.write(buf, 0, len);
						}
					} finally {
						bis.close();
					}
				} finally { in .close();
				}
			} finally {
				bos.close();
			}
		} finally {
			out.close();
		}

		if (!(new File(gzdest)).exists()) {
			throw new FileNotFoundException("Le fichier " + gzdest + " n'a pas Ã©tÃ© crÃ©Ã©");
		}
	}

	public static void unzipToDir(String zipsrc, String basedirdest) throws FileNotFoundException, IOException, SecurityException {
		unzipToDir((InputStream)(new FileInputStream(zipsrc)), basedirdest);
	}

	public static void unzipToDir(InputStream inzip, String basedirdest) throws IOException, SecurityException {
		File base = new File(basedirdest);

		if (!base.exists()) {
			base.mkdirs();
		}

		try {
			CheckedInputStream checksum = new CheckedInputStream(inzip, new Adler32());

			try {
				BufferedInputStream bis = new BufferedInputStream(checksum);

				try {
					ZipInputStream zis = new ZipInputStream(bis);

					try {
						byte[] buf = new byte[8192];

						ZipEntry entry;

						while ((entry = zis.getNextEntry()) != null) {
							File f = new File(basedirdest, entry.getName());

							if (entry.isDirectory()) {
								f.mkdirs();
							} else {
								int l = entry.getName().lastIndexOf(47);

								if (l != -1) {
									(new File(basedirdest, entry.getName().substring(0, l))).mkdirs();
								}

								FileOutputStream fos = new FileOutputStream(f);

								try {
									BufferedOutputStream bos = new BufferedOutputStream(fos, 8192);

									int count;
									try {
										while ((count = zis.read(buf, 0, 8192)) != -1) {
											bos.write(buf, 0, count);
										}
									} finally {
										bos.close();
									}
								} finally {
									fos.close();
								}
							}

							if (entry.getTime() != -1L) {
								f.setLastModified(entry.getTime());
							}
						}
					} finally {
						zis.close();
					}
				} finally {
					bis.close();
				}
			} finally {
				checksum.close();
			}
		} finally {
			inzip.close();
		}

	}

	public static void zipDir(String dirsource, String zipdest) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(zipdest);

		try {
			CheckedOutputStream checksum = new CheckedOutputStream(fos, new Adler32());

			try {
				BufferedOutputStream bos = new BufferedOutputStream(checksum, 8192);

				try {
					ZipOutputStream zos = new ZipOutputStream(bos);

					try {
						try {
							zos.setMethod(8);
							zos.setLevel(9);
						} catch (Exception e) {}

						zipDir(dirsource, (String) null, zos);
					} finally {
						zos.close();
					}
				} finally {
					bos.close();
				}
			} finally {
				checksum.close();
			}
		} finally {
			fos.close();
		}

	}

	private static void zipDir(String basedir, String currentdir, ZipOutputStream zos) throws FileNotFoundException, IOException {
		File zipDir = currentdir != null ? new File(basedir, currentdir) : new File(basedir);
		String[] dirList = zipDir.list();
		byte[] readBuffer = new byte[8192];
		String currentdir2 = currentdir != null ? currentdir + "/" : "";
		ZipEntry anEntry;

		if (currentdir2.length() > 0) {
			anEntry = new ZipEntry(currentdir2);
			zos.putNextEntry(anEntry);
			zos.closeEntry();
		}

		for (int i = 0; i < dirList.length; ++i) {
			File f = new File(zipDir, dirList[i]);

			if (f.exists()) {
				if (f.isDirectory()) {
					zipDir(basedir, currentdir2 + dirList[i], zos);
				} else {
					FileInputStream fis = new FileInputStream(f);

					try {
						BufferedInputStream bis = new BufferedInputStream(fis, 8192);

						try {
							anEntry = new ZipEntry(currentdir2 + dirList[i]);
							anEntry.setTime(f.lastModified());
							zos.putNextEntry(anEntry);

							int bytesIn;

							while ((bytesIn = bis.read(readBuffer, 0, 8192)) != -1) {
								zos.write(readBuffer, 0, bytesIn);
							}

							zos.closeEntry();
						} finally {
							bis.close();
						}
					} finally {
						fis.close();
					}
				}
			}
		}

	}
}