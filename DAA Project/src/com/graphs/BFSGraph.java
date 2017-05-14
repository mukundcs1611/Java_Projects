package com.graphs;
// Java program to print BFS traversal from a given source vertex.

// BFS(int s) traverses vertices reachable from s.
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
 
// This class represents a directed graph using adjacency list
// representation
public class BFSGraph
{
    
    
 
    // Constructor
    
    // prints BFS traversal from a given source s
     BFSGraph(Graph g,int s)
    {
        // Mark all the vertices as not visited(By default
        // set as false)
        boolean visited[] = new boolean[g.V];
 
        // Create a queue for BFS
        LinkedList<Integer> queue = new LinkedList<Integer>();
 
        // Mark the current node as visited and enqueue it
        visited[s]=true;
        queue.add(s);
 
        while (queue.size() != 0)
        {
            // Dequeue a vertex from queue and print it
            s = queue.remove();
            System.out.println("Order of finding vertices");
            System.out.println(s+" ");
 
            // Get all adjacent vertices of the dequeued vertex s
            // If a adjacent has not been visited, then mark it
            // visited and enqueue it
            Iterator<Integer> i = g.adj[s].listIterator();
            while (i.hasNext())
            {
                int n = i.next();
                if (!visited[n])
                {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }
    
    	
    
    
   
    
    
    // Driver method to
    public static void main(String args[])
    {	
    	Graph g=new Graph(0);
        //Scanner sc=new Scanner(System.in);
    	g.setDenseGraph(100);
        long startTime=System.currentTimeMillis();
        new BFSGraph(g,0);
        long endTime=System.currentTimeMillis();
        System.out.println("Exec time "+(endTime-startTime));
       
    }
}
