package edu.m4c.export;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import dbhelper.db.Database;

public class Export {
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("url", true, "jdbc url");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = parser.parse(options, args);
		
		if (!cmd.hasOption("url")) {
			System.out.println("syntax:\n\t<export> -url <jdbc_url> table_name pk_value");
		} else {
			new Export().start(cmd);
		}
	}
	
	private void start(CommandLine cmd) {
		try {
			Database db = new Database(
				cmd.getOptionValue("url")
			);
			
			String tableName = cmd.getArgs()[0].toUpperCase();
			if (db.getTable(tableName) == null) {
				System.err.println("unknown table: " + tableName);
				return;
			}
			
			Long pkValue = Long.valueOf(cmd.getArgs()[1]);
			new ExportA(db).export(tableName, pkValue);
			
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}