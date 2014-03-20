package edu.m4c.fcheck;

import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import dbhelper.db.Database;
import edu.m4c.fcheck.FragmentChecker.CheckingResult;

public class CheckFragments {
	public static void main(String[] args) throws SQLException {
		new CheckFragments().start(args);
	}

	private void start(String[] args) {
		try {
			Options options = new Options();
			options.addOption("url", true, "JDBC Url");
			
			
			DefaultParser parser = new DefaultParser();
			CommandLine cmdLine = parser.parse(options, args);

			checkOptions(cmdLine);
			
			Database db = new Database(cmdLine.getOptionValue("url"));
			fcheck(cmdLine.getArgs(), db);
			db.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkOptions(CommandLine cmdLine) {
		if (!cmdLine.hasOption("url")) {
			System.err.println("syntax: \n\t java -jar fcheck.jar -url <jdbc url> <file1> <file2> ...");
			System.exit(1);
		}
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
