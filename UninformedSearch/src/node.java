import java.util.ArrayList;

/*
 * Data Structure to store the vertices of graph
 * Values include:
 * 1. State
 * 2.Parent 
 * 3. ACtion
 * 4. Path-COST
 */
public class node {
	private Integer State;
	private node parent;
	private String[] Action;
	private  int path_cost;

	public Integer getState() {
		return State;
	}
	public void setState(Integer state) {
		State = state;
	}
	public node getParent() {
		return parent;
	}
	public void setParent(node parent) {
		this.parent = parent;
	}
	public String[] getAction() {
		return Action;
	}
	public void setAction(String[] action) {
		Action = action;
	}
	public int getPath_cost() {
		return path_cost;
	}
	public void setPath_cost(int path_cost) {
		this.path_cost = path_cost;
	}
	public node(){
		
	}
	public node(int state,node parent,int weight){
		this.State=state;
		this.parent=parent;
		if(parent==null){
			this.path_cost=0;
			
		}
		else{
			this.path_cost=parent.path_cost+weight;
		}
	}

}
