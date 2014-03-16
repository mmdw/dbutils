package com.m4c.profileutil;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Configuration {
	public List<String> sqlids = new LinkedList<String>();
	public List<String> sql = new LinkedList<String>();
	public List<String> taskIds = new LinkedList<String>();
	private List<String> testFiles = new LinkedList<String>();

	private String url;
	
	public  Configuration() {
	}
	
	public List<String> getSqlids() {
		return sqlids;
	}

	public void setSqlids(List<String> sqlids) {
		this.sqlids = sqlids;
	}

	public List<String> getSql() {
		return sql;
	}

	public void addSql(String sql) {
		this.sql.add(sql);
	}

	public List<String> getTaskIds() {
		return taskIds;
	}

	public void addTaskId(String taskId) {
		this.taskIds.add(taskId);
	}

	public void addSql(Collection<String> sqlFileNames) {
		sql.addAll(sqlFileNames);
	}

	public void addSqlId(Collection<String> sqlIds) {
		sqlids.addAll(sqlIds);
	}

	public void addTaskId(Collection<String> taskids) {
		taskIds.addAll(taskids);
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}

	public List<String> getTestQueries() {
		return testFiles ;
	}

	public void addTestQueries(Collection<String> queryFiles) {
		testFiles.addAll(queryFiles);
	}
}
