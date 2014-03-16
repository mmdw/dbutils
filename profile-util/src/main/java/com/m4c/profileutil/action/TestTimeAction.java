package com.m4c.profileutil.action;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.m4c.profileutil.utils.QueryParser;
import com.m4c.profileutil.utils.QueryParserHandler;
import com.m4c.profileutil.utils.Utils;

public class TestTimeAction {
	private final static Logger logger = Logger.getLogger(TestTimeAction.class);
	
	public static void queryBench(Connection conn, List<String> sqlFileNames) {
		logger.debug("Check time: " + sqlFileNames);
		try {
			processFiles(conn, sqlFileNames);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void processFiles(Connection conn, List<String> sqlFileNames)
			throws FileNotFoundException {
		
		for (String fileName : sqlFileNames) {
			logger.debug(String.format("execute %s...", fileName));
			String sql = Utils.streamToString(new FileInputStream(fileName));
			
			measureTime(conn, sql);
			setSqltuneCategory(conn, "OESO_TEST");
			measureTime(conn, sql);
			setSqltuneCategory(conn, "DEFAULT");
		}
	}

	private static void measureTime(Connection conn, String sql) {
		
		long elapsedTime = executeQuery(conn, sql);
		logger.debug(String.format("elapsed: %s ms", elapsedTime));
	}

	private static void setSqltuneCategory(Connection conn, String category) {
		logger.debug("Setting sqltune category: " + category);
		Statement st = null;
		try {
			st = conn.createStatement();
			st.executeQuery(String.format("ALTER SESSION SET SQLTUNE_CATEGORY = '%s'", category));
		} catch (SQLException e) {
			logger.error(e);
		} finally {
			try {
				st.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
	}

	private static long executeQuery(final Connection conn, String sql) {
		// отвратительно
		final Set<PreparedStatement> box = new HashSet<PreparedStatement>();
		
		try {
			new QueryParser(sql, new QueryParserHandler() {
				
				@Override
				public void query(String query) throws SQLException {
					box.add(conn.prepareStatement(query));
				}
				
				@Override
				public void bindValue(int type, int number, String name, Object value) throws SQLException {
					switch (type) {
					case Types.VARCHAR:
						box.iterator().next().setString(number, (String) value);
						break;
					case Types.NUMERIC:
						box.iterator().next().setLong(number, (Long) value);
						break;
					case Types.DATE:
						box.iterator().next().setDate(number, (java.sql.Date) value);
						break;
					default:
						throw new IllegalStateException();
					}
				}
			}).parse();
			
			long startTime = new Date().getTime();
			ResultSet rs = box.iterator().next().executeQuery();
			while (rs.next()) {
				logger.debug(">> " + rs.getString(1));
			}
			
			long endTime = new Date().getTime();
			return endTime - startTime;
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			if (box.iterator().next() != null) {
				try {
					box.iterator().next().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		return -1;
	}
}
