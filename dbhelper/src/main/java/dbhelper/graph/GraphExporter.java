package dbhelper.graph;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import dbhelper.db.Column;

public class GraphExporter {
	private String header = "<?xml version='1.0' encoding='UTF-8'?>" +
	"<graphml xmlns='http://graphml.graphdrawing.org/xmlns' " +  
	    "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
	    "xmlns:y='http://www.yworks.com/xml/graphml' " +
	    "xsi:schemaLocation='http://graphml.graphdrawing.org/xmlns " +
	    "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd'>" +
	    "<key for='node' id='d1' yfiles.type='nodegraphics'/>" +
	    "<key  for='edge' id='d2' attr.name='text' attr.type='string'/>" +
	    "<graph id='G' edgedefault='directed'>";
	
	private final String footer = 
	  "</graph></graphml>";
	
	public void exportToGraphML(Graph<String> g, String fileName) {
		try {
			PrintStream ps = new PrintStream(fileName);
			ps.println(header);
			
			for (GraphNode<String> node : g.getNodes()) {
				printNode(ps, node);
			}
			
			for (GraphNode<String> node : g.getNodes()) {
				for (Edge<String> ref : node.getReferencedTo()) {
					ps.printf("<edge id='%s%s' source='%s' target='%s'>\n", 
						node.getName(), ref.getTo().getName(), node.getName(), ref.getTo().getName());
					
					ps.printf("<data key='d2'>%s</data>\n", ((Column) g.getEdgeData(ref)).getName());
					ps.println("</edge>");
				}
			}
			
			ps.println(footer);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void printNode(PrintStream ps, GraphNode<String> node) {
		String line = "<data key='d1'><y:ShapeNode><y:Shape type='rectangle'/>"
				+ "<y:Geometry height='30.0' width='180.0' x='0.0' y='0.0'/>"
				+ "<y:Fill color='#FFCC00' transparent='false'/>"
				+ "<y:BorderStyle color='#000000' type='line' width='1.0'/>"
				+ "<y:NodeLabel>" + node.getName() + "</y:NodeLabel>"
			+ "</y:ShapeNode></data>";
		
		ps.printf("<node id='%s'>\n", node.getName());
		ps.println(line);
		ps.println("</node>");
	}
}
