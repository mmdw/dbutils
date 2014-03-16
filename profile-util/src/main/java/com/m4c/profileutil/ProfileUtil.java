package com.m4c.profileutil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.m4c.profileutil.action.AcceptProfileAction;
import com.m4c.profileutil.action.AskAdviceAction;
import com.m4c.profileutil.action.TestTimeAction;

public class ProfileUtil {
	private static Logger logger = Logger.getLogger(ProfileUtil.class);
	
	public static void main(String[] args) throws ParseException {
		PropertyConfigurator.configure(ProfileUtil.class.getResourceAsStream("log4j.properties"));
		logger.debug("start");
		
		Configuration conf = parseOptions(args);
		performActions(conf);
		
		logger.debug("stop");
	}

	private static void performActions(Configuration conf) {
		Connection connection = null;
		try {
			 connection = DriverManager.getConnection(conf.getUrl());
			 
			if (!conf.getSqlids().isEmpty()) {
				AskAdviceAction.askForSqlids(connection, conf.getSqlids());
			}
			
			if (!conf.getTaskIds().isEmpty()) {
				AcceptProfileAction.acceptProfile(connection, conf.getTaskIds());
			}
			
			if (!conf.getSql().isEmpty()) {
				AskAdviceAction.askForSqlFiles(connection, conf.getSql());
			}
			
			if (!conf.getTestQueries().isEmpty()) {
				TestTimeAction.queryBench(connection, conf.getTestQueries());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static Configuration parseOptions(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("sqlid", true, "id запроса");
		options.addOption("sql", true, "файл с запросом");
		options.addOption("url", true, "url для подключения к БД");
		options.addOption("taskid", true, "id задачи для применения профайла");
		options.addOption("test_sql", true, "проверка запроса");
		
		DefaultParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		Configuration config = new Configuration();
		
		if (!cmd.hasOption("url")) {
			error("Пропущен обязательный параметр: url");
		}
		
		config.setUrl(cmd.getOptionValue("url"));
		
		config.addSql(getOptions(cmd, "sql"));
		config.addSqlId(getOptions(cmd, "sqlid"));
		config.addTaskId(getOptions(cmd, "taskid"));
		config.addTestQueries(getOptions(cmd, "test_sql"));

		return config;
	}

	private static Collection<String> getOptions(CommandLine cmd, String optName) {
		if (!cmd.hasOption(optName)) {
			return Collections.emptyList();
		}
		
		return Arrays.asList(cmd.getOptionValues(optName));
	}
	
	private static void error(String message) {
		System.err.println(message);
		System.exit(-1);
	}
}
