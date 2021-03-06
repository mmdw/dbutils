package com.m4c.profileutil.utils;

import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class QueryParser {
	private String queryData;
	private QueryParserHandler handler;
	
	private static final SimpleDateFormat format 
		= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

	public QueryParser(String queryData, QueryParserHandler handler) {
		this.queryData = queryData;
		this.handler = handler;
	}
	
	public void parse() throws SQLException {
		handler.query(getQuery(queryData));
		
		List<String> params = extractParams(getParams(queryData));
		for (String param : params) {
			Pattern p = Pattern.compile("(\\d)+->(\\w+):(.+)");
			Matcher m = p.matcher(param);
			
			if (m.find()) {
				int number = Integer.valueOf(m.group(1));
				String name = m.group(2);
				String value = m.group(3);
				
				// дата-время
				if (value.matches("'\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d+'")) {
					try {
						Date parsedDate = format.parse(value.substring(1, value.length() - 1));
						
						handler.bindValue(Types.DATE, number, name, new java.sql.Date(parsedDate.getTime()));
						
					} catch (ParseException e) {
						e.printStackTrace();
					}
				} else
				// строки
				if (value.startsWith("'")) {
					value = value.substring(1, value.length() - 1);
					if (name.equals("DELETED") && value.matches("true|false")) {
						
						handler.bindValue(Types.NUMERIC, number, name, Long.valueOf(value.equals("true") ? 1 : 0));
					} else {
						handler.bindValue(Types.VARCHAR, number, name, value);
					}
				// Long
				} else {
					handler.bindValue(Types.NUMERIC, number, name, Long.valueOf(value));
				}
			}
		}
	}
	
	private static List<String> extractParams(String params) {
		if (params == null) {
			return Collections.emptyList();
		}
		
		List<String> paramValues = new LinkedList<String>();
		
		Pattern p = Pattern.compile("\\d+->\\w+:(\\d+|'[^\\']+')");
		Matcher matcher = p.matcher(params);
		while (matcher.find()) {
			paramValues.add(matcher.group(0));
		}
		
		return paramValues;
	}
	
	private static String getParams(String complexString) {
		String[] parts = complexString.split("\\[bind:");
		
		if (parts.length == 2) {
			return parts[1].substring(0, parts[1].length() - 1);
		} else {
			return null;
		}
	}
	
	private static String getQuery(String complexString) {
		String[] parts = complexString.split("\\[bind:");
		return parts[0];
	}
}
