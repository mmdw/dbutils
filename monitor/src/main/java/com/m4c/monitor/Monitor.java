package com.m4c.monitor;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


public class Monitor {
	public static void main(String[] args) throws IOException, AWTException, InterruptedException, ParseException {
		Options options = new Options();
		options.addOption("monitor", true, "Папка для отслеживания изменений");
		options.addOption("dest", true, "Папка для распаковки");
		
		DefaultParser parser = new DefaultParser();
		CommandLine cmdLine = parser.parse(options, args);
		
		new Monitor().install(
			cmdLine.getOptionValue("monitor"), 
			cmdLine.getOptionValue("dest")
		);
	}

	private void install(String monitor, String dest) throws IOException, AWTException, InterruptedException {
		TrayIcon icon = getIcon();
		SystemTray tray = SystemTray.getSystemTray();
		tray.add(icon);
		
		Thread monitorThread = new Thread(new MonitorThread(monitor, dest));
		monitorThread.setDaemon(true);
		monitorThread.start();
		monitorThread.join();
		
		tray.remove(icon);
	}

	private TrayIcon getIcon() throws IOException {
		return new TrayIcon(ImageIO.read(Monitor.class.getResourceAsStream("application_xp_terminal.png")));
	}
}
