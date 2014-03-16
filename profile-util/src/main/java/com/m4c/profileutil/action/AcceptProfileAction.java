package com.m4c.profileutil.action;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import com.m4c.profileutil.utils.Utils;

public class AcceptProfileAction {
	private static final String ACCEPT_PROFILE_SQL = 
			Utils.streamToString(AskAdviceAction.class.getResourceAsStream("acceptProfile.sql"));
	
	public static void acceptProfile(Connection connection, List<String> taskIds) {
		for (String taskId : taskIds) {
			acceptProfile(connection, taskId);
		}
	}

	private static void acceptProfile(Connection connection, String taskId) {
		CallableStatement cs = null;
		try {
			cs = connection.prepareCall(ACCEPT_PROFILE_SQL);
			
			cs.setString(1, taskId);
			cs.registerOutParameter(2, Types.VARCHAR);
			cs.execute();
			
			System.out.println(cs.getObject(2));
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
