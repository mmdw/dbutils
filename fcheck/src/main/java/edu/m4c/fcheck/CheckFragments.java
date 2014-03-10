package edu.m4c.fcheck;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

import dbhelper.db.Database;
import edu.m4c.fcheck.FragmentChecker.CheckingResult;

public class CheckFragments {
	public static void main(String[] args) throws SQLException {
		new CheckFragments().start(args);
	}

	private void start(String[] args) {
		try {
			Properties props = findProps();
			Database db = new Database(
				props.getProperty("jdbcUrl"), 
				props.getProperty("name"), 
				props.getProperty("password")
			);
			fcheck(args, db);
			db.close();
			
		} catch (Exception e) {
			e.printStackTrace();
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

	private void fcheck(String[] args, Database db) throws IOException {
		FragmentChecker checker = new FragmentChecker(db);
		
		for (String fileName : args) {
			CheckingResult result = checker.check(fileName);
			if (!result.getMessages().isEmpty()) {
				System.out.println("Bad file: " + fileName);
				for (String message : result.getMessages()) {
					System.out.println('\t' + message);
				}
			} else {
				System.out.println(fileName + " OK");
			}
		}
	}
}
