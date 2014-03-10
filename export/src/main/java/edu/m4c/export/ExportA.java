package edu.m4c.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dbhelper.db.Column;
import dbhelper.db.Database;
import dbhelper.graph.Edge;
import dbhelper.graph.Graph;
import dbhelper.graph.GraphNode;
import dbhelper.graph.NodeVisitor;
import dbhelper.graph.Pair;
import dbhelper.graph.TSort;

public class ExportA {
	private Database db;
	private Map<String, LinkedHashMap<Long, Long>> fkCache 
		= new HashMap<String, LinkedHashMap<Long, Long>>();

	private Map<String, LinkedList<Long>> data = new HashMap<String, LinkedList<Long>>();
	private Map<String, Graph<Long>>  hierData = new HashMap<String, Graph<Long>>();
	
	public ExportA(Database db) {
		this.db = db;
	}
	
	private void storeData(String tableName, Long pkValue) {
		if (!data.containsKey(tableName)) {
			data.put(tableName, new LinkedList<Long>());
		}
		
		if (!data.get(tableName).contains(pkValue)) {
			data.get(tableName).add(pkValue);
		}
	}
	
	private void storeDataHier(String name, Long key, Long parent) {
		if (!hierData.containsKey(name)) {
			hierData.put(name, new Graph<Long>());
		}
		
		Graph<Long> g = hierData.get(name);
		g.addEdge(key, parent, null);
	}
	
	private Long fetchKey(Long key, Column c) {
		String tb = c.getTable().getName() + '.' + c.getName();
		if (!fkCache.containsKey(tb)) {
			fkCache.put(tb, new LinkedHashMap<Long, Long>());
		}
		
		if (!fkCache.get(tb).containsKey(key)) {
			Long fk = db.selectFk(c, key);
			
			fkCache.get(tb).put(key, fk);
			if (fk != null) {
				String refName = c.getReferenced().getTable().getName();
				if (!c.getReferenced().getTable().equals(c.getTable())) {
					storeData(refName, fk);
				} else {
					storeDataHier(refName, key, fk);
				}
			}
		}
		
		return fkCache.get(tb).get(key);
	}
	
	public void export(String tableName, final Long pkValue) {
		storeData(tableName, pkValue);
		
		final Graph<String> g = db.getGraph();
		g.walk(g.getNode(tableName), new Walker(pkValue, g));
		
		g.removeReflexiveRelations();
		TSort<String> tSortString = new TSort<String>();
		TSort<Long> tsortLong = new TSort<Long>();
		
		ArrayList<String> names = tSortString.tsort(g);
		Collections.reverse(names);
		
		for (String name : names) {
			LinkedList<Long> keys = new LinkedList<Long>();
			if (hierData.containsKey(name)) {
				ArrayList<Long> sortedKeys = tsortLong.tsort(hierData.get(name));
				
				Collections.reverse(sortedKeys);
				keys.addAll(sortedKeys);
				
				for (Long k : keys) {
					db.exportRow(name, String.valueOf(k));
				}
			} else
			if (data.containsKey(name)) {
				for (Long k : data.get(name)) {
					db.exportRow(name, String.valueOf(k));
				}
			}
		}
	}
	
	private final class Walker implements NodeVisitor<Edge<String>> {
		private final Long pkValue;
		private final Graph<String> g;

		private Walker(Long pkValue, Graph<String> g) {
			this.pkValue = pkValue;
			this.g = g;
		}

		@Override
		public void visit(LinkedList<Pair<Edge<String>, Iterator<Edge<String>>>> path) {
			Set<Long> keySet = new LinkedHashSet<Long>();
			List<Long> tempSet = new LinkedList<Long>();
			
			keySet.add(pkValue);
			for (Pair<Edge<String>, Iterator<Edge<String>>> p : path) {
				Edge<String> edge = p.getFirst();
				if (edge == null) {
					continue;
				}

				if (keySet.isEmpty()) {
					break;
				}
				
				tempSet.clear();
				tempSet.addAll(keySet);

				for (Edge<String> reflexive : findReflexiveRelations(edge.getFrom())) {
					Column reflexColumn = (Column) g.getEdgeData(reflexive);
					for (Long key : keySet) {
						while ((key = fetchKey(key, reflexColumn)) != null) {
							tempSet.add(key);
						}
					}
				}
				
				for (Long key : tempSet) {
					Column c = (Column) g.getEdgeData(edge);
					Long newKey = fetchKey(key, c);
					keySet.remove(key);
					if (newKey != null) {
						keySet.add(newKey);
					}
				}
			}
		}

		private Collection<Edge<String>> findReflexiveRelations(GraphNode<String> to) {
			Collection<Edge<String>> reflexiveEdges = null;
			for (Edge<String> e : to.getReferencedTo()) {
				if (e.isReflexive()) {
					if (reflexiveEdges == null) {
						reflexiveEdges = new ArrayList<Edge<String>>();
					}
					
					reflexiveEdges.add(e);
				}
			}
			
			if (reflexiveEdges == null) {
				return Collections.emptyList();
			}
			
			return reflexiveEdges;
		}
	}
}
