package bheft;

import java.util.Vector;

public class Task {
	String id;
    int vCPU;
    double RAM;
    double Storage;
    double computation_costs;    
    //TaskProcessor record[];      
    Vector<Integer> vmNUM=new Vector<Integer>();
    
    public Task(int vCPU, double rAM, double storage,double computation_cost) {
		this.vCPU = vCPU;
		RAM = rAM;
		Storage = storage;
		this.computation_costs=computation_cost;
	}
    public Task(){
    	
    }
}
