package bheft;

import java.util.Vector;

class Service{
	int vCPU;
	double RAM;
	double Storage;
	double Price;
	
	public Service(double vCPU, double rAM, double storage,double rate,double price) {
		this.vCPU = (int) Math.floor(vCPU*rate);
		RAM = rAM*rate;
		Storage = storage*rate;
		Price=price;
	}
	
}
public class Vm {
    double[] delay;
    Vector<Service> service=new Vector<Service>();
    int rankService;    
    double cost;        
    int flag;
  
    public Vm(int noMachines) {
	   delay=new double[noMachines];
	   rankService=-1;
	   flag=0;
     }
  
  
}
