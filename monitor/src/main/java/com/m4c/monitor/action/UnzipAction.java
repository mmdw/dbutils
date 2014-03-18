package com.m4c.monitor.action;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipAction {
	private final static SimpleDateFormat format 
		= new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
	
	public Path unzip(Path file, File destDirectory) throws IOException {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(file.toAbsolutePath().toFile()));
		ZipEntry entry;

		long maxTime = -1;
		
		Path tempDirName = destDirectory.toPath().resolve("monitor" + String.valueOf(new Date().getTime()));
		
		Path tempDir = Files.createDirectory(tempDirName);
		while ((entry = zis.getNextEntry()) != null) {
			String entryName = entry.getName();
			
			maxTime = maxTime < entry.getTime() ? entry.getTime() : maxTime;

			Path targetFile = tempDir.resolve(entryName);
			targetFile.getParent().toFile().mkdirs();
			extractFile(zis, targetFile.getParent().toFile(), targetFile.getFileName().toString());
		}
		zis.close();
		
		File newFile = new File(destDirectory.getAbsolutePath() + File.separator + "logs-" + format.format(new Date(maxTime)));
		Files.move(tempDir, newFile.toPath());
		tempDir.toFile().renameTo(newFile);
		
		return newFile.toPath();
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
}