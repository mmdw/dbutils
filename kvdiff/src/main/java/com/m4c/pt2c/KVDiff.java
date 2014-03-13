package com.m4c.pt2c;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class KVDiff {
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 2) {
			System.out.println("usage: <kvdiff> file1 file2");
		} else {
			new KVDiff().start(args);
		}
	}

	private void start(String[] args) throws FileNotFoundException {
		String file1 = args[0];
		String file2 = args[1];

		HashMap<String, String> data1 = makeMap(file1);
		HashMap<String, String> data2 = makeMap(file2);
		
		Set<String> allKeys = new HashSet<>(data1.keySet());
		allKeys.addAll(data2.keySet());
		
		boolean firstRow = true;
		
		for (String key: allKeys) {
			String v1 = maybeNull(data1.get(key));
			String v2 = maybeNull(data2.get(key));
			
			if (!v1.equals(v2)) {
				if (firstRow) {
					System.out.println("key " + file1 + " " + file2);
				}
				
				System.out.printf("%s %s %s\n", key, v1, v2);
				firstRow = false;
			}
		}
	}
	
	private String maybeNull(String value) {
		return value == null ? "<null>" : value;
	}

	private HashMap<String, String> makeMap(String fname) throws FileNotFoundException {
		HashMap<String, String> kv = new HashMap<>(); 
		Scanner sc = new Scanner(new File(fname));
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			
			String[] data = line.split("\\s+");
			String key = data[0];
			
			if (data.length > 0) {
				String value = data[1]; 
				kv.put(key, value);
			} 
		}
		
		sc.close();
		return kv;
	}
}
