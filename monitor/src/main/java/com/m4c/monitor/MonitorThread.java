package com.m4c.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import com.m4c.monitor.processor.LogsProcessor;

public class MonitorThread implements Runnable {
	private WatchKey key;
	private File monitorDisrectory;
	private File destDirectory;

	public MonitorThread(String monitor, String dest) throws FileNotFoundException, IOException {
		this.monitorDisrectory = new File(monitor);
		this.destDirectory = new File(dest);
		
		Path dir = monitorDisrectory.toPath(); 
		
		WatchService watcher = FileSystems.getDefault().newWatchService();
		
		key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				lookupChanges();
				
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	private void lookupChanges() {
		for (WatchEvent<?> event : key.pollEvents()) {
			Kind<?> kind = event.kind();
			WatchEvent<Path> ev = (WatchEvent<Path>) event;
			
			try {
				new LogsProcessor().processFile(
					new File(monitorDisrectory.getAbsolutePath() + File.separator + ev.context()).toPath(),
					destDirectory
				);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
