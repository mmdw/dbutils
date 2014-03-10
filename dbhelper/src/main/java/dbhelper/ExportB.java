package dbhelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dbhelper.db.Column;
import dbhelper.db.Database;
import dbhelper.db.Table;
import dbhelper.graph.Edge;
import dbhelper.graph.Graph;
import dbhelper.graph.GraphNode;
import dbhelper.graph.Pair;

public class ExportB {
	private Database db;
	
	public ExportB(Database db) {
		this.db = db;
	}
	public void export(String tableName, Long pkValue) {
		Graph<String> g = db.getGraph();
		
		LinkedList<Pair<String, Long>> keys = new LinkedList<Pair<String, Long>>();
		keys.add(new Pair<String, Long>(tableName, pkValue));
		
		Map<String, Set<Long>> processedPks = new HashMap<String, Set<Long>>(); 
		
		while (!keys.isEmpty()) {
			GraphNode<String> start = g.getNode(keys.getFirst().getFirst());
			pkValue = keys.getFirst().getSecond();
			
			keys.removeFirst();
			
			for (Edge<String> ed : start.getReferencedFrom()) {
				Column column = (Column)g.getEdgeData(ed);
				Table table = column.getTable();
				String tbPk = table.getPk();
				String tbName = table.getName();
				
				String request = String.format("SELECT %s FROM %s WHERE %s = %s", tbPk, tbName,	column.getName(), pkValue);

				List<Long> refKeys = db.executeLongResultQuery(request);
				for (Long refKey : refKeys) {
					System.out.println(tbName + "." + tbPk + ": " + refKey);
					if (!processed(processedPks, tbName, refKey)) {
						keys.add(new Pair<String, Long>(tbName, refKey));
					}
				}
			}
		}
	}
	
	private boolean processed(Map<String, Set<Long>> processedPks, String tbName, Long refKey) {
		if (processedPks.get(tbName) == null) {
			processedPks.put(tbName, new HashSet<Long>());
			processedPks.get(tbName).add(refKey);
			
			return false;
		}
		
		return !processedPks.get(tbName).add(refKey);		
	}
}
