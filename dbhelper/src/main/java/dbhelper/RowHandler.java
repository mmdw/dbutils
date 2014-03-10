package dbhelper;

import java.util.LinkedList;
import java.util.List;

public abstract class RowHandler {

	private LinkedList<String> columns;

	RowHandler(LinkedList<String> linkedList) {
		this.columns = linkedList;
	}

	abstract public void processRow(long[] row);

	public List<String> getColumns() {
		return columns;
	}
}
