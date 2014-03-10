package dbhelper.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Graph<Key extends Comparable<Key>> {
	private HashMap<Edge<Key>, Object> edgeData = new HashMap<Edge<Key>, Object>();
	private Map<Key, GraphNode<Key>> nodes = new HashMap<Key, GraphNode<Key>>();
	
	public Graph() {
	}
	
	public Graph(Graph<Key> g) {
		for (GraphNode<Key> gn : g.getNodes()) {
			addNode(gn.getName());
		}
		
		for (Edge<Key> ed : g.edgeData.keySet()) {
			addEdge(ed.getFrom().getName(), ed.getTo().getName(), g.edgeData.get(ed));
		}
	}

	public Edge<Key> addEdge(Key nameFrom, Key nameTo, Object data) {
		GraphNode<Key> from = maybeNewNode(nameFrom);
		GraphNode<Key> to   = maybeNewNode(nameTo);
		
		Edge<Key> edge = new Edge<Key>(from, to, this);
		edgeData.put(edge, data);
		
		return edge;
	}
	
	public void walk(GraphNode<Key> start, NodeVisitor<Edge<Key>> visitor) {
		LinkedList<Pair<Edge<Key>, Iterator<Edge<Key>>>> path = new LinkedList<Pair<Edge<Key>, Iterator<Edge<Key>>>>();
		
		ArrayList<Edge<Key>> reflexiveEdges    = new ArrayList<Edge<Key>>();
		ArrayList<Edge<Key>> notReflexiveEdges = new ArrayList<Edge<Key>>();

		for (Edge<Key> e : start.getReferencedTo()) {
			if (e.isReflexive()) {
				reflexiveEdges.add(e);
			} else {
				notReflexiveEdges.add(e);
			}
		}
		
		reflexiveEdges.addAll(notReflexiveEdges);
		path.add(new Pair<Edge<Key>, Iterator<Edge<Key>>>(null, notReflexiveEdges.iterator()));
		
		while (!path.isEmpty()) {
			Iterator<Edge<Key>> nextIter = path.getLast().getSecond();

			GraphNode<Key> nextNode;
			if (nextIter.hasNext()) {
				Edge<Key> nextEdge = nextIter.next();
				if (nextEdge.isReflexive()) {
					continue;
				}
				
				nextNode = nextEdge.getTo();
				path.add(new Pair<Edge<Key>, Iterator<Edge<Key>>>(nextEdge, nextNode.getReferencedTo().iterator()));
			} else {
				if (path.getLast().getFirst() != null && path.getLast().getFirst().getTo().getReferencedTo().isEmpty()) {
					visitor.visit(path);
				}
				path.removeLast();
			}
		}
	}
	
	public List<LinkedList<Edge<Key>>> getPaths(GraphNode<Key> start) {
		List<LinkedList<Edge<Key>>> result = new LinkedList<LinkedList<Edge<Key>>>();
		
		LinkedList<LinkedList<Edge<Key>>> paths = new LinkedList<LinkedList<Edge<Key>>>();
		for (Edge<Key> edge : start.getReferencedTo()) {
			LinkedList<Edge<Key>> newPath = new LinkedList<Edge<Key>>();
			newPath.add(edge);
			
			paths.add(newPath);
		}
		
		while (!paths.isEmpty()) {
			LinkedList<Edge<Key>> p = paths.getFirst();
			Iterator<Edge<Key>> refs = p.getLast().getTo().getReferencedTo().iterator();
			
			if (!refs.hasNext()) {
				result.add(p);
				paths.removeFirst();
				continue;
			}

			Edge<Key> firstRef = refs.next();
		
			if (!firstRef.isReflexive() || needToAddReflexive(p, firstRef)) {
				while (refs.hasNext()) {
					LinkedList<Edge<Key>> newPath = new LinkedList<Edge<Key>>(p);
					Edge<Key> edge = refs.next();
					
					if (!edge.isReflexive() || needToAddReflexive(newPath, edge)) {
						newPath.add(edge);
						paths.add(newPath);
					}
				}
				p.add(firstRef);
			} else {
				result.add(p);
				paths.removeFirst();
			}
		}
		
		return result;
	}

	private boolean needToAddReflexive(LinkedList<Edge<Key>> p, Edge<Key> firstRef) {
		return p.size() < 2 || !firstRef.isReflexive() || !p.get(p.size() - 2).isReflexive();
	}
	
	private GraphNode<Key> maybeNewNode(Key name) {
		if (!nodes.containsKey(name)) {
			nodes.put(name, new GraphNode<Key>(name));
		}
		
		return nodes.get(name);
	}

	public Collection<GraphNode<Key>> getNodes() {
		return nodes.values();
	}

	public void onEdgeRemoved(Edge<Key> edge) {
		edgeData.remove(edge);
	}

	public void removeReflexiveRelations() {
		List<Edge<Key>> forDelete = new LinkedList<Edge<Key>>();
		for (GraphNode<Key> node : nodes.values()) {
			for (Edge<Key> ed : node.getReferencedTo()) {
				if (ed.getTo().equals(node)) {
					forDelete.add(ed);
				}
			}
		}
		
		for (Edge<Key> ed : forDelete) {
			ed.remove();
		}
	}

	public GraphNode<Key> getNode(String string) {
		return nodes.get(string);
	}

	public Object getEdgeData(Edge<Key> edge) {
		return edgeData.get(edge);
	}

	public void addNode(Key name) {
		maybeNewNode(name);
	}
}
