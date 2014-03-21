package com.m4c.monitor.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.m4c.monitor.action.GlueAction;
import com.m4c.monitor.action.UnrarAction;
import com.m4c.monitor.action.UnzipAction;

public class LogsProcessor {
	private Map<String, List<String>> entryMap = new HashMap<>();
	
	public void processFile(Path iFile, File destDirectory) throws IOException {
		Path dir = null;
		
		String fileName = iFile.getFileName().toString();
		if (fileName.endsWith("rar")) {
			UnrarAction unrarAction = new UnrarAction();
			if (unrarAction.probe(iFile)) {
				dir = unrarAction.unrar(iFile, destDirectory);
				glueLogs(dir);
			}
		} else 
		if (fileName.endsWith("zip")){
			UnzipAction unzipAction = new UnzipAction();
			if (unzipAction.probe(iFile)) {
				dir = unzipAction.unzip(iFile, destDirectory);
				glueLogs(dir);
			}
		}
	}

	private void glueLogs(Path dir) {
		new GlueAction().glueLogs(dir.toFile());
	}
}
