package edu.m4c.export;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import dbhelper.db.Database;

public class Export {
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("syntax: <export> table_name pk_value");
		} else {
			new Export().start(args);
		}
	}

	private Properties findProps() throws IOException, FileNotFoundException {
		Properties props = new Properties();
		
		InputStream propsfromClasspath = this.getClass().getClassLoader()
		        .getResourceAsStream("config.properties");
		
		if (propsfromClasspath != null) {
			props.load(propsfromClasspath);
		} else {
			props.load(new FileReader("config.properties"));
		}
		
		return props;
	}
	
	private void start(String[] args) {
		try {
			Properties props = findProps();
			Database db = new Database(
				props.getProperty("jdbcUrl"), 
				props.getProperty("name"), 
				props.getProperty("password")
			);
			
			String tableName = args[0].toUpperCase();
			if (db.getTable(tableName) == null) {
				System.err.println("unknown table: " + tableName);
				return;
			}
			
			Long pkValue = Long.valueOf(args[1]);
			new ExportA(db).export(tableName, pkValue);
			
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}