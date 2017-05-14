package com.graphs;

import java.util.LinkedList;


/**
 * 
 * @author chavali
 *
 */
public class Bellmann_ford {

    Bellmann_ford(Graph g, int startVertex) throws Exception {
        // TODO Auto-generated constructor stub
        // Init -graph
        boolean visited[] = new boolean[g.V];
       

        LinkedList<Integer> dist = new LinkedList<Integer>();
        LinkedList<Integer> pred = new LinkedList<Integer>();
        for (int i = 0; i < g.V; i++) {
            dist.add(Integer.MAX_VALUE);
            pred.add(Integer.MAX_VALUE);
            // pqueue.add(0);
        }

        // Start vertex will have a dist value of 0

        dist.set(startVertex, 0);

        visited[0] = true;

        for (int i = 1; i < g.V; i++) {
            
            for (String k : g.edge_weights.keySet()) {
            
                String[] vertices = k.split("\\.");
                int u = Integer.parseInt(vertices[0]);
                int v = Integer.parseInt(vertices[1]);
                // Relax operation
                if (dist.get(v) > dist.get(u) + g.edge_weights.get(k) && dist.get(u) != Integer.MAX_VALUE) {
                    dist.set(v, dist.get(u) + g.edge_weights.get(k));
                    pred.set(v, u);
                }

            }

        }
        // Find Negative Cycles
        for (int j = 0; j < g.V; j++) {
            for (Integer k : g.adj[j]) {
                // Relax operation
                if (dist.get(j) != (Integer.MAX_VALUE)
                    && dist.get(k) > dist.get(j) + g.edge_weights.get("" + j + "." + k + "")) {
                    throw new Exception("Negative Cycles");
                }
            }
        }
        System.out.println("Distance       Predeccesor");
        for (int i = 0; i < g.V; i++) {
            System.out.println(dist.get(i) + "  		  " + pred.get(i));
        }

    }

    public static void main(String[] args) {
//        Graph g = new Graph(600);
//        g.setDenseGraph(280);
//


    	Graph g = new Graph(5);
        g.addEdge(0, 2, 1);
        g.addEdge(0, 4, 8);
        g.addEdge(2, 3, 5);
        g.addEdge(3,2, 3);
        g.addEdge(0, 3, 2);
        g.addEdge(2, 4,-10);
        g.addEdge(3,1,2);
        long startTime = System.currentTimeMillis();
        try {
            Bellmann_ford bf = new Bellmann_ford(g, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    	long endTime = System.currentTimeMillis();
        System.out.println("\nExec Time1: " + (endTime - startTime) + "ms");

    }

}
