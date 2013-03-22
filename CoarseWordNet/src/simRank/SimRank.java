package simRank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.uci.ics.jung.graph.Graph;
import ezgraph.SparseMatrix;

public class SimRank {	
	
	private SparseMatrix simrank; // symmetric : only row <= col entries are used 
	
	private MyGraph graph;
	
	private double DEFAULT_C = 0.6;
	
	public SimRank(MyGraph graphPassed)
	{
		this(graphPassed, 0.0000000001, 5);
	}

	public void initialize()
	{
		int numNodes = graph.graph.getVertexCount();
		// supervised initialization
//		for ( int i = 0 ; i < numNodes ; i++ ) 
//		{ 
//			simrank.set(i,i,1.0); 
//		}	
	}
	
	public SimRank ( MyGraph graphPassed, double threshold, int maxIter ) {
		graph = graphPassed;
		int numNodes = graph.graph.getVertexCount();
		simrank = new SparseMatrix(numNodes);
		SparseMatrix simrank2 = new SparseMatrix(numNodes);
		for ( int step=0; step < maxIter && maxIter > 0; step++ ) 
		{
			System.out.println("Iteration : "+step);
			double maxDelta = Double.MIN_VALUE;			
			for ( int i = 0 ; i < numNodes ; i++ ) 
			{ 
				simrank.set(i,i,1.0); 
				simrank2.set(i,i,1.0); 
			}	
			Iterator it1 = graph.graph.getVertices().iterator();
			while(it1.hasNext())
			{
				Vertex currentVertex1 = (Vertex)it1.next();
				if(graph.graph.inDegree(currentVertex1) == 0) continue;
				int currentRow = graph.vertexToIdMap.get(currentVertex1).intValue();
				Iterator it2 = graph.graph.getVertices().iterator();
				while(it2.hasNext())
				{
					Vertex currentVertex2 = (Vertex)it2.next();
					if(graph.graph.inDegree(currentVertex2) == 0) continue;
					if ( currentVertex1 == currentVertex2 ) continue;	
					int currentCol = graph.vertexToIdMap.get(currentVertex2).intValue();
					if(currentRow > currentCol) continue;
					double quantity = 0.0;
					double sum1 = 0.0;
					Collection incomingEdges1 = graph.graph.getIncidentEdges(currentVertex1);
					for(Object edgeObject : incomingEdges1)	
						sum1 += ((Edge)edgeObject).weight;																
					for(Object edgeObject1 : incomingEdges1)
					{
						Edge edge1 = (Edge) edgeObject1;					
						double weight1 = edge1.weight / sum1;
						Collection incomingEdges2 = graph.graph.getIncidentEdges(currentVertex2);
						double sum2 = 0.0;
						for(Object edgeObject2 : incomingEdges2)													
							sum2 += ((Edge)edgeObject2).weight;						
						for(Object edgeObject2 : incomingEdges2)
						{
							Edge edge2 = (Edge) edgeObject2;
							double weight2 = edge2.weight / sum2;
							Vertex opposite1 = (Vertex) graph.graph.getOpposite(currentVertex1, edgeObject1);							
							Vertex opposite2 = (Vertex) graph.graph.getOpposite(currentVertex2, edgeObject2);
							int row = graph.vertexToIdMap.get(opposite1).intValue();
							int col = graph.vertexToIdMap.get(opposite2).intValue();
							double simValue = 0;
							if(row <= col)
								simValue = simrank.get(row, col);
							else
								simValue = simrank.get(col, row);
								
							quantity += weight1*weight2*simValue;
						}						
					}
					if(quantity != 0.0)
					{												
						double newValue = quantity*(DEFAULT_C / (1.0 * graph.graph.inDegree(currentVertex1) * graph.graph.inDegree(currentVertex2)));
						simrank2.set(currentRow, currentCol, newValue);
						double oldValue = simrank.get(currentRow, currentCol);
						maxDelta = Math.max(maxDelta, Math.abs( newValue - oldValue ) );
					}															
				}
			}
			simrank = simrank2.clone();
			simrank2 = new SparseMatrix(numNodes);
			if ( maxDelta < threshold && threshold > 0 ) break;
		}
	}
	
	public void writeSimRank(String simRankPath, String idToVertexMapPath)
	{
		int numNodes = graph.graph.getVertexCount();
		try{
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(simRankPath)));
			for(int row=0; row < numNodes; row++)
			{
				for(int col=0; col < numNodes; col++)
					bw.write(simrank.get(row, col)+" ");
				bw.write("\n");
			}
			bw.close();
			
			bw = new BufferedWriter(new FileWriter(new File(idToVertexMapPath)));
			for(int i=0; i <numNodes; i++)			
				bw.write(i+" "+graph.idToVertexMap.get(i)+"\n");			
			bw.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
}

