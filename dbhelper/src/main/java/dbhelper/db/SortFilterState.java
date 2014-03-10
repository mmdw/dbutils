package dbhelper.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SortFilterState {
	private Map<String, Sort> sortState = new HashMap<>();
	private Map<String, String> filterState = new HashMap<>();
	private Set<String> columns;
	
	public enum Sort {
		ASC, DESC, NONE
	};
	
	public SortFilterState(String[] columns) {
		this.columns = new HashSet<String>();
		Collections.addAll(this.columns, columns);
	}
	
	public void setSort(String column, Sort sort) {
		sortState.clear();
		if (sort != Sort.NONE) {
			sortState.put(column, sort);
		}
	}
	
	public void setFilter(String column, String filter) {
		if (filter.isEmpty()) {
			filterState.remove(column);
		} else {
			filterState.put(column, filter);
		}
	}
	
	public Entry<String, Sort> getSort() {
		return sortState.isEmpty() ? null : sortState.entrySet().iterator().next();
	}
	
	public Set<Entry<String, String>> getFilter() {
		return filterState.entrySet();
	}

	public void toggleSort(String name) {
		Sort state = sortState.get(name);
		if (state == null) {
			state = Sort.NONE;
		}
		
		switch (state) {
		case NONE:
			sortState.put(name, Sort.ASC);
			break;
		case ASC:
			sortState.put(name, Sort.DESC);
			break;
		default:
			sortState.clear();
			break;
		}
	}

	public String getFilter(String selectedColumn) {
		String filterValue = filterState.get(selectedColumn);
		return filterValue == null ? "" : filterValue;
	}
}
