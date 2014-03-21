package com.m4c.monitor.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.extract.ExtractArchive;
import com.github.junrar.rarfile.FileHeader;

public class UnrarAction extends UnpackAction {
	public Path unrar(Path iFile, File destDirectory) {
		Archive rar = null;
		try {
			rar = new Archive(iFile.toFile());

			long time = -1;
			
		    FileHeader fh;
			while ((fh = rar.nextFileHeader()) != null) {
		    	long fileTime = fh.getMTime().getTime();
		    	
		    	if (fileTime > time) {
		    		time = fileTime;
		    	}
		    }
			
			
			Path tempDirName = destDirectory.toPath().resolve("monitor" + String.valueOf(new Date().getTime()));
			Path tempDir = Files.createDirectory(tempDirName);
			new ExtractArchive().extractArchive(iFile.toFile(), tempDir.toFile());
			
			return renameWithTime(destDirectory, time, tempDir);
			
		} catch (RarException | IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rar != null) {
					rar.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}

	@Override
	public boolean probe(Path iFile) {
		Archive rar = null;
		try {
			rar = new Archive(iFile.toFile());

		    FileHeader fh;
			while ((fh = rar.nextFileHeader()) != null) {
				if (fh.getFileNameString().contains("server.log")) {
					return true;
				}
			}
	    } catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rar != null) {
				try {
					rar.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return false;
	} 
}
