package dbhelper.db;

import java.util.LinkedHashSet;
import java.util.Set;

public class Column {
	private Column referenced = null;
	private Set<Column> children = new LinkedHashSet<Column>();
	private String name;
	private Table table;
	private boolean pk;
	
	public Column(String name, Table table, String type) {
		this.name = name;
		this.table = table;
	}

	public String getName() {
		return name;
	}


	public void setPk(boolean pk) {
		this.pk = pk;
	}
	
	public Column getReferenced() {
		return referenced;
	}
	
	public void setReferenced(Column c2) {
		this.referenced = c2;
	}

	public void addChild(Column c1) {
		children.add(c1);
	}

	public Table getTable() {
		return table;
	}
	
	@Override
	public String toString() {
		return String.format("%s", name);
	}

	public boolean getPk() {
		return pk;
	}
}
