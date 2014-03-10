package dbhelper.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class TSort<Key extends Comparable<Key>> {
	public ArrayList<Key> tsort(Graph<Key> g) {
		Graph<Key> graph = new Graph<Key>(g);
		
		ArrayList<Key> result   = new ArrayList<Key>();
		ArrayList<GraphNode<Key>> nodes = new ArrayList<GraphNode<Key>>(graph.getNodes());
		
		Comparator<GraphNode<Key>> refComparator = new ReferencedFromComparator();
		
		while (!nodes.isEmpty()) {
			Collections.sort(nodes, refComparator);
			
			for (int i = nodes.size() - 1; i >=0; --i) {
				GraphNode<Key> node = nodes.get(i);
				
				if (node.getReferencedFrom().isEmpty()) {
					for (Edge<Key> ref : new LinkedList<Edge<Key>>(node.getReferencedTo())) {
						ref.remove();
					}
					
					result.add(node.getName());
					nodes.remove(i);
				}
			}
		}
		
		return result;
	}
	
	private class ReferencedFromComparator implements Comparator<GraphNode<Key>> {

		@Override
		public int compare(GraphNode<Key> o1, GraphNode<Key> o2) {
			int o1Ref = o1.getReferencedFrom().size();
			int o2Ref = o2.getReferencedFrom().size();
			
			if (o1Ref > o2Ref) {
				return -1;
			} else 
			if (o1Ref < o2Ref) {
				return 1;
			}
			return 0;
		}
	}
}
