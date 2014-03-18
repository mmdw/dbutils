package com.m4c.monitor.action;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;

public class GlueAction {

	private FileFilter directoryFilter = new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory();
		}
	};
	
	private Comparator<String> nameComparator = new Comparator<String>() {

		@Override
		public int compare(String o1, String o2) {
			return getIndex(o1) - getIndex(o2);
		}

		private int getIndex(String o1) {
			int dot = o1.lastIndexOf(".");
			return Integer.valueOf(o1.substring(dot + 1));
		}
	};

	
	private FileFilter logFilter = new FileFilter() {
		
		@Override
		public boolean accept(File pathname) {
			boolean accept = true;
			accept &= !pathname.isDirectory();
			accept &= pathname.getName().matches(".*\\.log$");
			
			return accept;
		}
	};
	
	public void glueLogs(File dir) {
		LinkedList<File> files = new LinkedList<File>();
		files.addAll(Arrays.asList(dir.listFiles()));
		
		while (!files.isEmpty()) {
			dir = files.pop();
			files.addAll(Arrays.asList(dir.listFiles(directoryFilter)));
			
			processFiles(dir.listFiles(logFilter));
		}
	}

	private void processFiles(File[] listFiles) {
		for (File logFile : listFiles) {
			String[] parts = logFile.getParentFile().list(makeLogFileFilter(logFile));
			Collections.sort(Arrays.asList(parts), nameComparator);
			
			for (String part: parts) {
				append(logFile, part);
				System.out.println(logFile.getName() + ": " + part);
			}
		}
	}

	private void append(File logFile, String part) {
		FileOutputStream fos = null;
		FileInputStream fin = null;
		File iFile = new File(logFile.getParentFile().getAbsolutePath() + File.separator + part);
		
		try {
			fos = new FileOutputStream(logFile, true);
			fin = new FileInputStream(iFile);
			
			IOUtils.copy(fin, fos);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		iFile.delete();
	}

	private FilenameFilter makeLogFileFilter(final File logFile) {
		return new FilenameFilter() {
			
			@Override
			public boolean accept(File file, String name) {
				boolean accept = true;
				accept &= name.startsWith(logFile.getName());
				accept &= !name.equals(logFile.getName());
				return accept;
			}
		};
	}
}
