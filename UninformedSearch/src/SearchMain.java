import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.Queue;
import java.util.Scanner;



/*
 * Consists all the search strategies in one place
 */
public class SearchMain {
	private static ArrayList<String> romanianCities =new ArrayList<String>();
	private HashMap<String,node> Nodes=new HashMap<String,node>();
	Graph g;
	node startNode;
	node goalNode;
	SearchMain(int strategy){
		readFile(new File(".\\list"));
		switch(strategy){
		case 0: new BFSGraph(g, startNode, goalNode);break;
		case 1: new DFSGraph(g,startNode,goalNode);break;
		case 2: new uniformCostSearch(g, startNode, goalNode); break;
		
		}
	}

	/**
	 * read the input file and initialize all edges and vertices
	 * @param file
	 */
	public void readFile(File file){

		Scanner sc=null;


		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while(sc.hasNextLine()){

			String[] line=sc.nextLine().split(",");
			if(line[0].equals("Source")){
				romanianCities.add(line[1]);
				Nodes.put(line[1],startNode=new node(0, null, 0));
				startNode.setState(0);

			}
			if(line[0].equals("Dest")){
				romanianCities.add(line[1]);
				Nodes.put(line[1],new node(romanianCities.indexOf(line[1]),null,0));
				goalNode=Nodes.get(line[1]);

			}
			if(line[0].equals("nodes")){
				g=new Graph(Integer.parseInt(line[1]));
			}
			else if(line.length==3){
				//if the node is not in the romanianCities list already
				if(romanianCities.indexOf(line[0])<0){
					romanianCities.add(line[0]);
					Nodes.put(line[0], new node(romanianCities.indexOf(line[0]),null,0));

				}
				if(romanianCities.indexOf(line[1])<0){
					romanianCities.add(line[1]);
					Nodes.put(line[1], new node(romanianCities.indexOf(line[1]),null,0));

				}
				int vertex1=romanianCities.indexOf(line[0]);
				int vertex2=romanianCities.indexOf(line[1]);
				int weight=Integer.parseInt(line[2]);
				g.addEdge(vertex1, vertex2, weight);

			}

		}
		sc.close();
	}
	void traverseParents(int node ){
		node curr=Nodes.get(romanianCities.get(node));
		if(curr.getParent()==null){
			System.out.println(""+romanianCities.get(curr.getState()));

			return;
		}
		else{
			traverseParents(curr.getParent().getState());
			System.out.println(""+romanianCities.get(curr.getState()));
		}
	}
	void initializeFrontier(){

	}
	class DFSGraph
	{
		node startNode;
		node goalNode;
		Graph g; 
		public DFSGraph(Graph g,node start, node goalNode){
			this.g=g;
			this.startNode = start;
			this.goalNode = goalNode;
			findPath(start.getState(),goalNode.getState());
			//System.out.println("Goal state is not reachable");


		}
		LinkedList<Integer> stack = new LinkedList<>();
		ArrayList<Integer> explored = new ArrayList<>();

		void findPath(int current ,int goal){

			if(this.startNode.equals(goalNode)){
				System.out.println("Source and destination is same ");
				return;
			}

			stack.add(current);
			explored.add(current);
			if(current==goalNode.getState()){

				System.out.println("Path to destination : ");
				traverseParents(current);
				System.out.println("Total Distance Travelled : "+goalNode.getPath_cost());
				System.out.println("Total Nodes Touched : "+explored.size());
			}	
			else{
				for(Integer li:g.adj[current]){
					if(!explored.contains(li)){
						node nodeObj=Nodes.get(romanianCities.get(li));
						node ParentObj=Nodes.get(romanianCities.get(current));
						if(nodeObj.getParent()==null&&nodeObj.getState()!=startNode.getState()){
							nodeObj.setParent(ParentObj);
							nodeObj.setPath_cost(nodeObj.getParent().getPath_cost()+g.edge_weights.get("" + current + "." + li + ""));
						}
						findPath(li, goal);
					}
				}
				stack.removeLast();
			}
		}

	}
	

	class BFSGraph 
	{
		node startNode;
		node goalNode;
		Graph g;
		int totalCost=0;
		BFSGraph(Graph g,node start, node goalNode){
			this.g=g;
			this.startNode = start;
			this.goalNode = goalNode;
			if(!findPath()){
				System.out.println("Goal state is not reachable");
			}
		}

		boolean findPath(){
			if(this.startNode.equals(goalNode)){
				System.out.println("Goal Node Found!");
				System.out.println(startNode);
			}
			Queue<Integer> queue = new LinkedList<>(); //Frontier Initialize
			ArrayList<Integer> explored = new ArrayList<>();
			queue.add(startNode.getState());
			while(!queue.isEmpty()){
				int current = queue.remove();
				if(current==goalNode.getState()) {
					explored.add(current);
					System.out.println("Distance to Destination:"+goalNode.getPath_cost());
					System.out.println("Path to reach destination:");
					traverseParents(current);
					System.out.println("Nodes Traversed in total\n source and destination included:"+(explored.size()));
					for(int i:explored){
						totalCost=totalCost+Nodes.get(romanianCities.get(i)).getPath_cost();					
					}
					System.out.println("Total Distance Travelled \n(including the nodes which doesnt lead to solution):" +totalCost);
					return true;
				}
				else{
					if(g.adj[current].isEmpty())
						return false;
					else{
						LinkedList<Integer> l=g.adj[current];
						for(Integer list:l){
							node nodeObj=Nodes.get(romanianCities.get(list));
							node ParentObj=Nodes.get(romanianCities.get(current));
							if((nodeObj.getParent()==null||nodeObj.getPath_cost()>ParentObj.getPath_cost()+g.edge_weights.get("" + ParentObj.getState() + "." + list + ""))&&nodeObj.getState()!=startNode.getState()){
								nodeObj.setParent(ParentObj);
								nodeObj.setPath_cost(nodeObj.getParent().getPath_cost()+g.edge_weights.get("" + current + "." + list + ""));
							}
							queue.add(list);
						}
					}
				}
				if(!explored.contains(current)){
					explored.add(current);
				}
			}
			return false;

		}
	}
	class uniformCostSearch{
		
		void callParent(int child,List<Integer> pred){
			if(child==0){
				System.out.println(romanianCities.get(child));
				return;
			}
			else{
				callParent(pred.get(child),pred);
				System.out.println(romanianCities.get(child));
			}
			}
		uniformCostSearch(Graph graph, node startVertex,node goalState) {
			
			// Init-graph
			boolean visited[] = new boolean[graph.V];
			
			List<Integer> pqueue = new LinkedList<Integer>();

			List<Integer> dist = new LinkedList<Integer>();
			List<Integer> pred = new LinkedList<Integer>();
			for (int i = 0; i < graph.V; i++) {
				dist.add(Integer.MAX_VALUE);
				pred.add(0);
			}

			

			dist.set(startVertex.getState(),0);
			pqueue.add(0,startVertex.getState());
			visited[0] = true;
			

			while (pqueue.size() != 0) {

				int currVertex = pqueue.remove(0); 
				visited[currVertex] = true;
				

				// Queue all the elements into the pq
				Iterator<Integer> i0 = graph.adj[currVertex].listIterator();
				int min = Integer.MAX_VALUE;
				int minVertex = -1;
				while (i0.hasNext()) 

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
			System.out.println("Path to reach Destination");
			callParent(goalNode.getState(),pred);
			System.out.println("Total Distance from source to dest : "+dist.get(goalNode.getState()));
			
			
		}
		

	}

	public static void main(String args[])
	{
		//0-BFS 1-DFS 	2-Uniform
		Scanner sc=new Scanner(System.in);
		System.out.println("Select Search Strategy: 0.BFS 1.DFS 2. Uniform Cost Search ");
		int algorithm=sc.nextInt();
		sc.close();
		SearchMain sm=new SearchMain(algorithm);

	}

}



