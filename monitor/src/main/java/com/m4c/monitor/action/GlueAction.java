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
	
	private Comparator<File> nameComparator = new Comparator<File>() {

		@Override
		public int compare(File o1, File o2) {
			return getIndex(o1) - getIndex(o2);
		}

		private int getIndex(File o1) {
			int dot = o1.getName().lastIndexOf(".");
			return Integer.valueOf(o1.getName().substring(dot + 1));
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
		
		files.addAll(Arrays.asList(dir.listFiles(directoryFilter)));
		processFiles(dir.listFiles(logFilter));
		
		while (!files.isEmpty()) {
			dir = files.pop();
			if (dir.isDirectory()) {
				files.addAll(Arrays.asList(dir.listFiles(directoryFilter)));
				processFiles(dir.listFiles(logFilter));
			}
		}	
	}

	private void processFiles(File[] listFiles) {
		for (File logFile : listFiles) {
			File[] parts = logFile.getParentFile().listFiles(makeLogFileFilter(logFile));
			
			if (parts.length == 0) {
				continue;
			}
			
			Collections.sort(Arrays.asList(parts), Collections.reverseOrder(nameComparator));

			for (int i = 1; i < parts.length; ++i) {
				append(parts[0], parts[i]);
			}
			
			append(parts[0], logFile);
			
			parts[0].renameTo(logFile);
			
			for (File part: parts) {
				System.out.println(logFile.getName() + ": " + part.getName());
			}
		}
	}

	private void append(File logFile, File part) {
		FileOutputStream fos = null;
		FileInputStream fin = null;
		
		try {
			fos = new FileOutputStream(logFile, true);
			fin = new FileInputStream(part);
			
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
		
		part.delete();
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
