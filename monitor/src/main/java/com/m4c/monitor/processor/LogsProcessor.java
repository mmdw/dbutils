package com.m4c.monitor.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.m4c.monitor.action.GlueAction;
import com.m4c.monitor.action.UnzipAction;

public class LogsProcessor {
	private Map<String, List<String>> entryMap = new HashMap<>();
	
	public void processFile(Path iFile, File destDirectory) throws IOException {
		Path dir = new UnzipAction().unzip(iFile, destDirectory);
		new GlueAction().glueLogs(dir.toFile());
		System.out.println(dir.getFileName());
	}
}
