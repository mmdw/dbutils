package com.m4c.profileutil.action;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import oracle.sql.CLOB;

import org.apache.log4j.Logger;

import com.m4c.profileutil.utils.SimpleQueryParser;
import com.m4c.profileutil.utils.SimpleQueryParser.BindVariable;
import com.m4c.profileutil.utils.Utils;

public class AskAdviceAction {
	private final static String ASK_ADVICE_SQLID = Utils.streamToString(AskAdviceAction.class.getResourceAsStream("askAdviceSqlid.sql"));
	private final static String ASK_ADVICE_SQLFILE_BINDS = 
			Utils.streamToString(AskAdviceAction.class.getResourceAsStream("askAdviceSqlFileBinds.sql"));
	
	private final static Logger logger = Logger.getLogger(AskAdviceAction.class);
	
	public static void askForSqlids(Connection connection, Collection<String> sqlids) {
		CallableStatement cs = null;

		for (String sqlid : sqlids) {
			logger.debug("SQL_ID: " + sqlid);
			try {
				cs = connection.prepareCall(ASK_ADVICE_SQLID);
				
				cs.setString(1, sqlid);
				cs.registerOutParameter(2, Types.CLOB);
				cs.registerOutParameter(3, Types.VARCHAR);
				cs.execute();
				
				CLOB clob = (CLOB)cs.getObject(2);
				
				try {
					logger.debug(Utils.readerToString(clob.getCharacterStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				logger.debug(cs.getObject(3));
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (cs != null) {
					try {
						cs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static String makeBindsString(List<BindVariable> bindVariables) {
		StringBuilder sb = new StringBuilder();
		
		Iterator<BindVariable> iter = bindVariables.iterator();
		
		while (iter.hasNext()) {
			BindVariable bv = iter.next();
			if (bv.getType() == Types.NUMERIC) {
				sb.append(String.format("anydata.ConvertNumber(%s)", bv.getValue()));
			} else 
			if (bv.getType() == Types.VARCHAR){
				sb.append(String.format("anydata.ConvertVarchar2('%s')", bv.getValue()));
			} else {
				throw new IllegalStateException();
			}
			
			if (iter.hasNext()) {
				sb.append(",");
			}
		}
		
		return sb.toString();
	}
	
	public static void askForSqlFiles(Connection connection, Collection<String> sqlFiles) throws IOException {
		CallableStatement cs = null;

		for (String sqlFile : sqlFiles) {
			String sql = prepareSql(sqlFile);
			SimpleQueryParser parser = new SimpleQueryParser(sql);
			List<BindVariable> bindVariables = parser.getBindVariables();
			
			logger.debug("SQL_FILE: " + sqlFile);
			try {
				cs = connection.prepareCall(
					ASK_ADVICE_SQLFILE_BINDS.replace("$BINDS", makeBindsString(bindVariables))
				);
				
				cs.setClob(1, new StringReader(parser.getQueryBody()));
				cs.registerOutParameter(2, Types.CLOB);
				cs.registerOutParameter(3, Types.VARCHAR);
				cs.execute();
				
				CLOB clob = (CLOB)cs.getObject(2);
				
				try {
					logger.debug(Utils.readerToString(clob.getCharacterStream()));
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				logger.debug(cs.getObject(3));
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (cs != null) {
					try {
						cs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static String prepareSql(String sqlFile) throws IOException,
			FileNotFoundException {
		
		String sql = Utils.readerToString(new FileReader(sqlFile));
		int i = 1;
		while (sql.contains("?")) {
			sql = sql.replaceFirst("\\?", ":" + i++);
		}
		return sql;
	}
}
