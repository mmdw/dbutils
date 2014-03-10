package dbhelper.graph;


public class Edge<Key extends Comparable<Key>> {
	private static long edgeCounter = 0L;
	private long id = edgeCounter++;
	
	private GraphNode<Key> from;
	private GraphNode<Key> to;
	private Graph<Key> graph;
	
	public Edge(GraphNode<Key> from, GraphNode<Key> to, Graph<Key> g) {
		this.from = from;
		this.to = to;
		this.graph = g;
		
		from.addReferencedTo(this);
		to.addReferencedFrom(this);
	}

	public GraphNode<Key> getFrom() {
		return from;
	}

	public GraphNode<Key> getTo() {
		return to;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || ! (obj instanceof Edge)) {
			return false;
		}
		
		return ((Edge<?>) obj).id == id;
	}
	
	@Override
	public String toString() {
		return String.format("<id: %s, %s, %s, %s>", id, from.getName(), to.getName(), graph.getEdgeData(this));
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}

	public void remove() {
		from.getReferencedTo().remove(this);
		to.getReferencedFrom().remove(this);
	
		graph.onEdgeRemoved(this);
	}

	public boolean isReflexive() {
		return getTo().equals(getFrom());
	}
	
	public long getId() {
		return id;
	}
}
