package com.m4c.profileutil.utils;

import java.sql.SQLException;


public interface QueryParserHandler {
	void query(String query) throws SQLException;
	void bindValue(int type, int number, String name, Object value) throws SQLException;
}
