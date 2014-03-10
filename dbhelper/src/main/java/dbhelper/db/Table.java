package dbhelper.db;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class Table {
	public String name;
	public String pk;
	
	private Map<String, Column> columns;
	
	public Table(String name) {
		this.name = name;
		columns = new LinkedHashMap<String, Column>();
	}

	public void addColumn(String name, String type) {
		columns.put(name, new Column(name, this, type));
	}

	public void setPk(String pk) {
		this.pk = pk;
		columns.get(pk).setPk(true);
	}
	
	public Column getColumn(String name) {
		return columns.get(name);
	}
	
	@Override
	public String toString() {
		return name + ':' + pk;
	}

	public Collection<Column> getColumns() {
		return columns.values();
	}

	public String getName() {
		return name;
	}

	public String getPk() {
		return pk;
	}

	public boolean hasReflexiveRelation() {
		for (Column c : columns.values()) {
			if (c.getReferenced() != null && c.getReferenced().equals(c)) {
				return true;
			}
		}
		
		return false;
	}
}
