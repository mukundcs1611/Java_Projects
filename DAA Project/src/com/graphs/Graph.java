package com.graphs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/*
 * Graph:
 * 1.Has Vertices V
 * 2.every u from V can have an edge e to other vertex v
 * 3.For a weighted graph Each edge e From Set E will have a weight ,only  Positive for dijkstra
 * Operations:
 * i.Initialize_graph(V) //v being the number of vertices from 0 to V-1
 * addEdge(u,v,w)
 * 
 */

public class Graph {
	 int V;   // No. of vertices
	 int E=0;
    LinkedList<Integer> adj[]; //Adjacency Lists
    HashMap<String,Integer> edge_weights=new HashMap<String,Integer>(); //Weights
    HashMap<Integer,Integer> degree=new HashMap<Integer,Integer>();//Degree of each Vertex
    
    HashMap<Integer,Long> execTimes=new HashMap<Integer,Long>();
 

	/*
	 * This method initializes a linkedlist of size V and each entry in the linkedList is a link to another list 
	 * |__v1_|->|__vj_|->|_vk__|->  max size in this(->) direction is n
	 * |__v2_|->
	 *
	 *
	 */

    Graph(int v)
    {
        V = v;
        adj = new LinkedList[v];
        for (int i=0; i<v; ++i)
        	adj[i]=new LinkedList();
            
    }
 
    // Function to add an edge into the graph
    void addEdge(int v,int w)
    {
        adj[v].add(w); 
    }
    //For Weighted Graphs
    void addEdge(int u,int v,int w){
    	E=E+1;
		adj[u].add(v);
		if(edge_weights.get(""+u+"."+v+"")==null){
			edge_weights.put(""+u+"."+v+"",w);
		}
		else{
			if(w<edge_weights.get(""+u+"."+v+"")){
				edge_weights.put(""+u+"."+v+"",w);
			}
		}
		
	}
    public void setDenseGraph(int howManyEdgesPerVertex){
		  for(int i=0;i<V;i++){
			  	
			  //Dense Graph with same weights
				for(int j=0;j<(V-(V-howManyEdgesPerVertex));j++){
					int val=10;
					addEdge(i,j,val);
				}
		  }
		}
    public void setSparseGraph(){
    	for(int i=0;i<V;i++){
    		int val=new Random().nextInt(20);
    		if(i+1>=V){
    			break;
    		}
    		else{
    			addEdge(i, i+1,val);
    		}
    		
    	}
    }
	
}
