package com.m4c.monitor.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Util {
	public static File rename(File destDirectory, Path tempDir, String newName)
			throws IOException {
		
		String newPath = destDirectory.getAbsolutePath() + File.separator + newName;
		
		File newFile = new File(newPath);
		int i = 1;
		while (newFile.exists()) {
			newFile = new File(newPath + "-" + i++);
		}
		
		Files.move(tempDir, newFile.toPath());
		tempDir.toFile().renameTo(newFile);
		
		return newFile;
	}
	
	public static String testContent(File dir) {
		return "null";
	}
}
