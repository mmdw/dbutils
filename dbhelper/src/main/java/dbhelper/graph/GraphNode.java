package dbhelper.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GraphNode<Key extends Comparable<Key>> implements Comparable<GraphNode<Key>> {
	private Key name;
	
	private Set<Edge<Key>> referencedTo   = new HashSet<Edge<Key>>();
	private Set<Edge<Key>> referencedFrom = new HashSet<Edge<Key>>();
	
	public GraphNode(Key name) {
		this.name = name;
	}
	
	public Key getName() {
		return name;
	}

	@Override
	public int compareTo(GraphNode<Key> o) {
		return name.compareTo(o.getName());
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof GraphNode)) {
			return false;
		}
		
		GraphNode<Key> o = (GraphNode<Key>) obj;
		
		return name.equals(o.getName());
	}
	
	@Override
	public String toString() {
		return String.valueOf(name);
	}

	public void addReferencedTo(Edge<Key> dst) {
		referencedTo.add(dst);
	}

	public void addReferencedFrom(Edge<Key> src) {
		referencedFrom.add(src);
	}

	public Collection<Edge<Key>> getReferencedTo() {
		return referencedTo;
	}
	
	public Collection<Edge<Key>> getReferencedFrom() {
		return referencedFrom;
	}
}
