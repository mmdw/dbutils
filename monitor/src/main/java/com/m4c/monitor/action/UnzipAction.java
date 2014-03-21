package com.m4c.monitor.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipAction extends UnpackAction {
	public Path unzip(Path file, File destDirectory) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(file.toAbsolutePath().toFile()));

		long maxTime = -1;
		
		Path tempDirName = destDirectory.toPath().resolve("monitor" + String.valueOf(new Date().getTime()));
		Path tempDir = Files.createDirectory(tempDirName);
		
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			String entryName = entry.getName();
			Path targetFile = tempDir.resolve(entryName);
			
			if (entry.isDirectory()) {
				Files.createDirectories(targetFile);
				continue;
			}
			
			maxTime = maxTime < entry.getTime() ? entry.getTime() : maxTime;

			File parentDir = targetFile.getParent().toFile();
			parentDir.mkdirs();
			
			extractFile(zis, parentDir, targetFile.getFileName().toString());
		}
		zis.close();
		
		return renameWithTime(destDirectory, maxTime, tempDir);
	}

	private void extractFile(ZipInputStream in, File outdir, String name) throws IOException {
		byte[] buffer = new byte[1024 * 64];

		BufferedOutputStream out = new BufferedOutputStream(
			new FileOutputStream(new File(outdir, name))
		);

		int count = -1;
		while ((count = in.read(buffer)) != -1) {
			out.write(buffer, 0, count);
		}

		out.close();
	}
	
	@Override
	public boolean probe(Path file) {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(new FileInputStream(file.toAbsolutePath().toFile()));
		
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().contains("server.log")) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zis != null) {
				try {
					zis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return false;
	}
}