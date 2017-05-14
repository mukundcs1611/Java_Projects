package com.graphs;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/*
 * Dijkstra is used for finding the shortest paths from a single source and a graph which have no negative edges nor
 * negative weight cycles
 * Algorithm Dijkstra(G,S,w[]):
 * 1.Initialize Graph , S: the set of vertices for whom the shortest path is known Init a Queue insert starting vertex
 * into it
 * 
 * 2.While the queue isn't empty :
 * u=Extract-Min(Q)
 * S={S} U u
 * 3. for every vertex v which is adjacent to u run
 * Relax(u,v,w)
 * 
 * 
 * Relax(u,v,w){
 * if(d[v]<shortest_dist(u)+w[v])
 * shortest_dist(v)=d[v]
 * else
 * shortest_dist(v)=shortest_dist(u)+w[v];
 * 
 * }
 */

/**
 * 
 * @author chavali
 *
 */
public class DijkstraAlg {

    public DijkstraAlg(Graph graph, int startVertex) {
//
//        for (Entry<String, Integer> e : graph.edge_weights.entrySet()) {
//            if (e.getValue() < 0) {
//                throw new IllegalArgumentException("edge " + e.toString() + " has negative weight");
//            }
//        }

        // Init-graph
        boolean visited[] = new boolean[graph.V];

        List<Integer> pqueue = new LinkedList<Integer>();

        List<Integer> dist = new LinkedList<Integer>();
        List<Integer> pred = new LinkedList<Integer>();
        for (int i = 0; i < graph.V; i++) {
            dist.add(Integer.MAX_VALUE);
            pred.add(0);
            

        }

        // Start vertex will have a dist value of 0

        dist.set(startVertex,0);
        pqueue.add(0,startVertex);
        visited[0] = true;

        while (pqueue.size() != 0) {

            int currVertex = pqueue.remove(0); // O(1)

            visited[currVertex] = true;

            // Queue all the elements into the pq
            Iterator<Integer> i0 = graph.adj[currVertex].listIterator();
            int min = Integer.MAX_VALUE;
            int minVertex = -1;
            while (i0.hasNext()) // O(deg(V))

            {
                int n = i0.next();

                // Find Minimum and insert into Queue
                if (visited[n] != true) {
                    if (dist.get(n) <=min) {
                        min = dist.get(n);
                        minVertex = n;

                    }
                    pqueue.add(minVertex);
                }
            }

            for (Integer i : graph.adj[currVertex]) {// O(deg(V))
                // Relax operation
                if (dist.get(i) >= dist.get(currVertex) + graph.edge_weights.get("" + currVertex + "." + i + "")) {
                    dist.set(i, dist.get(currVertex) + graph.edge_weights.get("" + currVertex + "." + i + ""));
                    pred.set(i, currVertex);
                }

            }

        }
        System.out.println("Distance       Predeccesor");
        for (int i = 0; i < graph.V; i++) {
            System.out.println(dist.get(i) + "  		  " + pred.get(i));
        }
        // we have a graph with vertices and non negative cycles and non-negative weighted edges
        // Firstly we have maintain a queue which serves us to work on vertices which is adjacent too an already
        // processed vertex (Starting with the startVertex)
    }

    public static void main(String[] args) {
        Graph g = new Graph(5);
        g.addEdge(0, 2, 1);
        g.addEdge(0, 4, 8);
        g.addEdge(2, 3, 5);
        g.addEdge(3,2, 3);
        g.addEdge(0, 3, 2);
        g.addEdge(2, 4,-10);
        g.addEdge(3,1,2);

        
        long startTime = System.currentTimeMillis();
        DijkstraAlg d = new DijkstraAlg(g, 0);
        long endTime = System.currentTimeMillis();
        System.out.println("\nExec Time1: " + (endTime - startTime) + "ms");

    }
}
