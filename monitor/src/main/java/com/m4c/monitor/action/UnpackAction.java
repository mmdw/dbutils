package com.m4c.monitor.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.mutable.MutableObject;

public abstract class UnpackAction {
	protected final static SimpleDateFormat format 
	   = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
	
	public abstract boolean probe(Path iFile);
	
	protected File rename(File destDirectory, Path tempDir, String newName)
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

	protected Path renameWithTime(File destDirectory, long time, Path tempDir)
			throws IOException {
		
		final MutableObject worklog = new MutableObject();
		
		Files.walkFileTree(destDirectory.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				
				String fileName = file.getFileName().toString();
				if (fileName.equals("ApplicationWorkLog.log")) {
					worklog.setValue(fileName);
					return FileVisitResult.TERMINATE;
				}
				
				return FileVisitResult.CONTINUE;
			}
		});
		
		String newName = "logs-" + format.format(new Date(time));
		return Util.rename(destDirectory, tempDir, newName).toPath();
	}
}
