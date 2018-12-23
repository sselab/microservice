package bheft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
class Edge{
	String num;
	String fromNode;
	String toNode;
}
class Result{
	Vector<Integer> rankPerformance=new Vector<Integer>();
	Vector<TaskProcessor> rSchedule=new Vector<TaskProcessor>();
	double sumPrice=0;
}

public class HEFT {
   int no_tasks,no_machines,no_service; 
   double[][] data_transfer_rate,data; 
   double[] tasks_upper_rank;
   int[] sorted_tasks;
   TaskProcessor[] schedule;   
   Task[] tasks;
   Vector<Vm> vms=new Vector<Vm>();
   Vector<Edge> edges=new Vector<Edge>();
   double[] sumCost;
   double[] avg_cost;
   double[] avg_costk;
   static int pos;  
   static int noslots;
   static double Pay;   
   
  // static  boolean isGO=true;
   //static double sumTime;
   //static double sum=9999;
   Result result=new Result();
   public HEFT(double pay) {
	   Pay=pay;
   }
   
   
   public void readXML(){  
	   try{
			File f = new File("conf.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dt = db.parse(f);
			NodeList jobList = dt.getElementsByTagName("job");    //job
			tasks=new Task[jobList.getLength()];
			no_tasks=no_machines=jobList.getLength();
			sorted_tasks=new int[no_tasks];
			tasks_upper_rank=new double[no_tasks];
			avg_cost=new double[no_tasks];
			avg_costk=new double[no_tasks];
			sumCost=new double[no_tasks];
			for(int i=0;i<no_tasks;i++){
				tasks_upper_rank[i]=-1;
			}
			schedule=new TaskProcessor[no_tasks];
			for(int i=0;i<no_tasks;i++){
				schedule[i]=new TaskProcessor();
			}
			data=new double[no_tasks][no_tasks];
			for(int i=0;i<data.length;i++)
				for(int j=0;j<data[i].length;j++)
					data[i][j]=-1;
			data_transfer_rate=new double[no_machines][no_machines];
			for (int i = 0; i < jobList.getLength(); i++){
				Task task=new Task();
				task.id= jobList.item(i).getAttributes().getNamedItem("id").getNodeValue() ;
				task.vCPU=Integer.parseInt(jobList.item(i).getAttributes().getNamedItem("cpu").getNodeValue());
				task.RAM=Double.parseDouble(jobList.item(i).getAttributes().getNamedItem("ram").getNodeValue());
				task.Storage=Double.parseDouble(jobList.item(i).getAttributes().getNamedItem("storage").getNodeValue());
				task.computation_costs=Double.parseDouble(jobList.item(i).getAttributes().getNamedItem("runtime").getNodeValue());
				tasks[i]=task;
			}
			NodeList childList = dt.getElementsByTagName("child");
			int counter=0;
			for (int i = 0; i < childList.getLength(); i++) {
				String tonode=childList.item(i).getAttributes().getNamedItem("ref").getNodeValue();
				NodeList l = childList.item(i).getChildNodes();
				for(int s=0;s<l.getLength();s++){
					if (l.item(s).getNodeName()=="parent"){
						Edge edge=new Edge();
						counter=counter+1;
						edge.num="shishir"+counter;
						edge.fromNode=l.item(s).getAttributes().getNamedItem("ref").getNodeValue();
						edge.toNode=tonode;
						edges.add(edge);
					}
				}
			}
			for(int i=0;i<jobList.getLength();i++){
				NodeList l1=jobList.item(i).getChildNodes();
				for(int s=0;s<l1.getLength();s++){
					if(l1.item(s).getNodeName()=="uses"){
						int from=0;
						int to=0;
						String file=l1.item(s).getAttributes().getNamedItem("file").getNodeValue();
						//System.out.println("file "+file);
						double mdata=Double.parseDouble(l1.item(s).getAttributes().getNamedItem("size").getNodeValue());
						//System.out.println("mdata "+mdata);
						for(int j=0;j<edges.size();j++){
							if(edges.get(j).num.equals(file)){
								for(int k1=0;k1<tasks.length;k1++){
									if(tasks[k1].id.equals(edges.get(j).fromNode))
										from=k1;
									//System.out.println("from "+from);
								}
								for(int k2=0;k2<tasks.length;k2++){
									if(tasks[k2].id.equals(edges.get(j).toNode))
										to=k2;
									//System.out.println("to "+to);
								}
								data[from][to]=mdata;
							}
						}
						
					}
				}
			}
	       }catch(Exception e){
	    	   e.printStackTrace();
	       }
	   
   }
   
   public void getVm(){
	   double rate=0;
	   int format=0;
	   for(int k=0;k<no_machines;k++){
		   Vm vm=new Vm(no_machines);
		   vms.add(vm);
	   }
	   for(int i=0;i<no_machines;i++){
		   for(int j=0;j<no_machines;j++){
			   if(j!=i){
				   data_transfer_rate[i][j]=12500000;
			   }
			   else
				   data_transfer_rate[i][j]=99999999;
		   }
	   }
	   for(int i=0;i<no_machines;i++){
		   for(int j=0;j<no_machines;j++){
			   vms.get(i).delay[j]=0;
		   }
	   }
	   try{
			FileReader reader = new FileReader("input.txt");
		    BufferedReader br = new BufferedReader(reader);
		    String readtemp;
		    int la=0;
		    while((readtemp = br.readLine()) != null){
		    	
		    	/*if(readtemp.equals("#带宽")){
		    		 for(int i=0;i<no_machines;i++){
		    			readtemp=br.readLine();
		    			double[] tempD=new double[no_machines];
		    			String[] temp=readtemp.split(" ");
		    			for(int j=0;j<temp.length;j++){
		    				double bandwidth=Double.parseDouble(temp[j]);
		    				tempD[j]=bandwidth;
		    			}
		    			data_transfer_rate[i]=tempD;
		    		}
		    	}
		    	if(readtemp.equals("#延时")){
		    		for(int i=0;i<no_machines;i++){
		    			readtemp=br.readLine();
		    			double[] tempD=new double[no_machines];
		    			String[] temp=readtemp.split(" ");
		    			for(int j=0;j<temp.length;j++){
		    				double delay=Double.parseDouble(temp[j]);
		    				tempD[j]=delay;
		    			}
		    			vms.get(i).delay=tempD;
		    		}
		    	}*/
		    	if(readtemp.equals("#占用比例")){
		    		readtemp=br.readLine();
		    		rate=Double.parseDouble(readtemp);
		    	}
		    	if(readtemp.equals("#虚拟机规格")){
		    		readtemp=br.readLine();
		    		format=Integer.parseInt(readtemp);
		    		no_service=format;
		    	}
		    	if(readtemp.equals("#虚拟机定价")){
		    		for(int i=0;i<format;i++){
		    			readtemp=br.readLine();
		    			//System.out.println(readtemp);
		    			String[] temp=readtemp.split(" ");
		    			//double[] tempD=new double[temp.length];
		    			double vCPU=Double.parseDouble(temp[0]); 
		    			double rAM=Double.parseDouble(temp[1]);  
		    			double storage=Double.parseDouble(temp[2]);
		    			double price=Double.parseDouble(temp[3]);
		    			for(int j=0;j<no_machines;j++){
		    				Service service=new Service(vCPU,rAM,storage,rate,price);
		    				vms.get(j).service.add(i, service);
		    			}
		    		}
		    	}
		    }
	      }catch(Exception e){
	    	  e.printStackTrace();
	      }
   }
   
  // Calculate average communication cost and give feed to sorted task array
   void insertinto(int task,double rank)
   {
//	   System.out.println("任务："+task);
//	   System.out.println("等级："+rank);
       int i;
       for(i=pos-1; i>=0; i--)
           if(rank>tasks_upper_rank[sorted_tasks[i]])
               sorted_tasks[i+1]=sorted_tasks[i];
           else
               break;
       sorted_tasks[i+1]=task;
       pos++;
   }
   // Calculate the average cost of communication between source and destination
   double avg_communicationcost(int source,int destination)
   {
       int i,j;
       double avg=0.0;
       double avgl=0.0;
       for(i=0; i<no_machines; i++)
           for(j=0; j<no_machines; j++)
           {
               if(data_transfer_rate[i][j]!=99999999){
                   avg+=(data[source][destination]/data_transfer_rate[i][j]);
                   avgl+=vms.get(i).delay[j];
               }
           }
       avg=(avg+avgl)/(no_machines*no_machines-no_machines);
       return avg;
   }
	// Calculate the upper rank
	double calculate_upper_rank(int task)
	{
	    
	    double avg_communication_cost,successor,avg=0.0,max=0,rank_successor;
	
	    avg=tasks[task].computation_costs;      
	
	    for(int j=0; j<no_tasks; j++)
	    {
	        // Check if node(j) is a successor of node(task)
	        if(data[task][j]!=-1)     
	        {
	            avg_communication_cost=avg_communicationcost(task,j);
	           // System.out.println(avg_communication_cost+"!!");
	            if(tasks_upper_rank[j]==-1)  
	            {
	                rank_successor= tasks_upper_rank[j]= calculate_upper_rank(j);
	                insertinto(j,rank_successor);
	            }
	            else
	                rank_successor= tasks_upper_rank[j];
	
	            successor=avg_communication_cost+rank_successor;
	
	            if(max<successor)
	                max=successor;
	        }
	    }
	    return(avg+max);
	}
	
	/*void insertslots(double[][] machineFreeTime,int current_pos, double start,double end)
	{
	    int i;
	    if(start < 0)
	        start=0;
	    for(i=current_pos-1; i>=0; i--)
	    {
	        if(start < machineFreeTime[i][0])
	        {
	            machineFreeTime[i+1][0]=machineFreeTime[i][0];
	            machineFreeTime[i+1][1]=machineFreeTime[i][1];
	        }
	        else
	            break;
	    }
	    machineFreeTime[i+1][0]=start;
	    machineFreeTime[i+1][1]=end;
	}
	
	void findfreeslots(int processor,double[][] machineFreeTime)
	{
	    int i,j;
	    noslots=0;
	    double highest_AFT=-99999.0,min=99999.0;
	    for(i=0; i<no_tasks; i++)
	    {
	        min=99999.0;
	        if(schedule[i].processor==processor)
	        {
	            if(schedule[i].AFT>highest_AFT)
	                highest_AFT=schedule[i].AFT;
	            for(j=0; j<no_tasks; j++)
	            {
	                if((i==j) || (schedule[j].processor!=processor))
	                    continue;
	                if((schedule[j].AST>=schedule[i].AFT) && (schedule[j].AST<min))
	                {
	                    min=schedule[j].AST;
	                }
	            }
	            if(min<99998.0)
	            {
	                insertslots(machineFreeTime,noslots,schedule[i].AFT,min);
	                noslots++;
	            }
	        }
	    }
	    insertslots(machineFreeTime,noslots,highest_AFT,99999.0);
	    noslots++;
	}
	// Ckeck if it is an entry task
	int isEntryTask(int task)
	{
	    int i;
	    for(i=0; i<no_tasks; i++)
	    {
	        if(data[i][task]!=-1)
	            return 0;
	    }
	    return 1;
	}
	*/
	// Find EST
	double find_EST(int stask,int processor)
	{
	    int i,j1,j2;
	    int iter=0;  
	    double ST,EST=0,comm_cost;
	     for(i=0; i<no_tasks; i++)
	    {
	    	iter=0;
	        if(data[i][stask]!=-1)
	        {
	        	for(int j=0;j<no_tasks;j++){
	        		if(schedule[j].processor==schedule[i].processor&&data[j][stask]!=-1)
	        			iter++;
		    	}
	        	if(schedule[i].processor!=processor){
		        	for(j1=0;j1<no_tasks;j1++){
		    			for(j2=0;j2<no_tasks;j2++){		    			
		    				if(schedule[j1].processor==schedule[i].processor&&schedule[j2].processor==processor&&(data[j1][j2]!=-1||data[j2][j1]!=-1)){
		    					iter++;
		    					//if(i==2){
			    					//System.out.println("iter "+iter+" j1 "+j1+" j2 "+j2);
			    				//}
		    				}
		    				
		    			}
		    		}
	              //  System.out.println(schedule[i].processor+"!!!");    !!!!!!!!!!!
	                comm_cost=data[i][stask]/(data_transfer_rate[schedule[i].processor][processor]/iter);
	               // System.out.println("comm_cost"+i+" "+comm_cost);
	        	}
	        	else{                               
	        		comm_cost=0;
	        	}
	        	//System.out.println("他的共享带宽条数为："+iter);
	        	 ST=schedule[i].AFT + comm_cost;
	        	 //System.out.println("ST"+i+" "+ST+" "+schedule[i].AFT);
	             // Try to find the max EST
	             if(EST<ST)
	                 EST=ST;  
	        }
	       
	    }
	    return EST;
	}
	// Calculate the EST and EFT
	double calculate_EST_EFT(int stask,int processor,double computation_costs)
	{
		TaskProcessor EST_EFT=new TaskProcessor();
	    double EST;
	    double EFT;
	    int number=1;
	    //if(task==2)
	    //for(i=0;i<noslots;i++)
	    //{
	    //	printf("%lf %lf\n",machineFreeTime[i][0],machineFreeTime[i][1]);
	    //}
	    EST=find_EST(stask,processor);
	    //printf("%lf\n",EST);
	    for(int i=0;i<no_tasks;i++){
	    	if(schedule[i].processor==processor){
	    		number++;
	    	}
	    }
	    EFT=EST+computation_costs*number;  
	    return EFT;
	}
	double calculate_sumCost(double avg_EFT){
		double max=0;
		double min=9999;
		double sumCost=0;
		for(int h=0;h<no_tasks;h++){
			if(schedule[h].AFT>max){
				max=schedule[h].AFT;
			}
			if(schedule[h].AST<min){
				min=schedule[h].AST;
			}
		}
		if(avg_EFT!=0&&avg_EFT>max){
			max=avg_EFT;
		}
		for(int s=0;s<no_machines;s++){
			
			if(vms.get(s).rankService==-1){
				vms.get(s).cost=0;
				//System.out.println("进入函数！！！！！！！！！！！！！！！");
			}
			else{
				vms.get(s).cost=vms.get(s).service.get(vms.get(s).rankService).Price*(max-min);
			}
			sumCost=sumCost+vms.get(s).cost;
            //max=0;
            //min=9999;
		}
		return sumCost;
	}
	double calculate_p_cost(int stask,int processor,double computationCosts){
		double computation_costs=0;
		int rank=-1;
		for(int i=noslots;i<no_tasks;i++){
			computation_costs=computation_costs+tasks[sorted_tasks[i]].computation_costs;
		}
		//System.out.println(noslots+"!!!"+computation_costs);
		double avg_computation_costs;
		if(computationCosts==0){ 
			avg_computation_costs=computation_costs/(no_tasks-1-noslots+1);
		}
		else
			avg_computation_costs=computationCosts;
		//System.out.println(noslots+"!!!"+avg_computation_costs);
		double avg_EFT=calculate_EST_EFT(stask,processor,avg_computation_costs);
		for(int j=0;j<no_service;j++){
			if(vms.get(processor).service.get(j).vCPU>=tasks[stask].vCPU&&vms.get(processor).service.get(j).RAM>=tasks[stask].RAM&&vms.get(processor).service.get(j).Storage>=tasks[stask].Storage){
				rank=j;
				break;
			}
		}
		if(vms.get(processor).rankService==-1){
			vms.get(processor).rankService=rank;
			vms.get(processor).flag=1;
		}
		double PCost;
		if(noslots>0){
			PCost=calculate_sumCost(avg_EFT)-sumCost[sorted_tasks[noslots-1]];
		}
		else
			PCost=calculate_sumCost(avg_EFT);
		if(vms.get(processor).flag==1){
			vms.get(processor).rankService=-1;
			vms.get(processor).flag=0;
		}
		return PCost;
	}
	Vector<Integer> canPutVm(int stask,int[] sumVCPU,double[] sumRAM,double[] sumStorage){   
		Vector<Integer> canPut=new Vector<Integer>();
		//int sumVCPU=0;
	    //double sumRAM=0;
	    //double sumStorage=0;
		for(int i=0;i<no_machines;i++){
			for(int j=0;j<no_tasks;j++){
				if(schedule[j].processor==i){
					sumVCPU[i]=sumVCPU[i]+tasks[j].vCPU;
					sumRAM[i]=sumRAM[i]+tasks[j].RAM;
					sumStorage[i]=sumStorage[i]+tasks[j].Storage;
				}
			}
			/*if(sumVCPU[i]+tasks[task].vCPU<vms.get(i).service.get(no_service-1).vCPU&&sumRAM[i]+tasks[task].RAM<vms.get(i).service.get(no_service-1).RAM&&sumStorage[i]+tasks[task].Storage<vms.get(i).service.get(no_service-1).Storage){
				canPut.add(i);
			}
			*/
			if(vms.get(i).rankService==-1){
				canPut.add(i);
			}
			else if(sumVCPU[i]+tasks[stask].vCPU<=vms.get(i).service.get(vms.get(i).rankService).vCPU&&sumRAM[i]+tasks[stask].RAM<vms.get(i).service.get(vms.get(i).rankService).RAM&&sumStorage[i]+tasks[stask].Storage<vms.get(i).service.get(vms.get(i).rankService).Storage){
				canPut.add(i);
			}
		}
		return canPut;
	}
	double calculate_SAB(int stask,Vector<Integer> canPut){
		double sum_cost=0;
		double sum_costk=0;
		for(int i=0;i<canPut.size();i++){
		    sum_cost=sum_cost+calculate_p_cost(stask,canPut.get(i),0);
		    sum_costk=sum_costk+calculate_p_cost(stask,canPut.get(i),tasks[stask].computation_costs);
		}
		//System.out.println(canPut.size()+"!!!!!!!!!!!");
		//System.out.println(stask+"aaaaaaa");
		//avg_cost.add(stask,sum_cost/canPut.size());
		avg_cost[stask]=sum_cost/canPut.size();
		avg_costk[stask]=sum_costk/canPut.size();
		double SAB;
		if(noslots>0){
			SAB=Pay-sumCost[sorted_tasks[noslots-1]]-(no_tasks-1-noslots+1)*avg_cost[stask];
		}
		else
			SAB=Pay-(no_tasks-1-noslots+1)*avg_cost[stask];
		return SAB;
	}
	double calculate_CTB(int stask,double SAB){
		double CTB=0;
		if(SAB>=0){
			CTB=avg_costk[stask]+SAB*1/(no_tasks-1-noslots+1);
		}
		else{
			CTB=avg_costk[stask];
		}
		return CTB;
	}
	//make schedule
	void make_schedule(int task){
		int stask=sorted_tasks[task];
		int[] sumVCPU=new int[no_machines];
		double[] sumRAM=new double[no_machines];
		double[] sumStorage=new double[no_machines];
		Vector<Integer> canPut=new Vector<Integer>();
		canPut=canPutVm(stask,sumVCPU,sumRAM,sumStorage);
		double SAB=calculate_SAB(stask,canPut);
		double CTB=calculate_CTB(stask,SAB);
		int Rank=0;
		Vector<Integer> S=new Vector<Integer>();
		for(int i=0;i<canPut.size();i++){
			double pCost=calculate_p_cost(stask,canPut.get(i),tasks[stask].computation_costs);
			if(pCost<=CTB){
				S.add(canPut.get(i));
			}
		}
		int Processor=-1;
		double min=9999;
		//System.out.println(S.size()+" S.sizelalalalal");
		if(S.size()!=0){
			//System.out.println("one lllll");
			for(int j=0;j<S.size();j++){
				//System.out.print("任务可以放入的虚拟机："+S.get(j));
				double EFT=calculate_EST_EFT(stask,S.get(j),tasks[stask].computation_costs);
				//System.out.println(" 他的时间为："+EFT);
				if(EFT<min){
					min=EFT;
					Processor=S.get(j);
				}
			}
			/*if(task==3){
				System.out.println("最小时间  "+min+" 虚拟机 "+Processor);
			}*/
		    schedule[stask].AST=find_EST(stask,Processor);
		    schedule[stask].AFT=calculate_EST_EFT(stask,Processor,tasks[stask].computation_costs);
		    schedule[stask].processor=Processor;
		}
		else if(S.size()==0&&SAB>=0){
			//System.out.println("two lllll");
			for(int j=0;j<canPut.size();j++){
				double EFT=calculate_EST_EFT(stask,canPut.get(j),tasks[stask].computation_costs);
				if(EFT<min){
					min=EFT;
					Processor=canPut.get(j);
				}
			}
			 schedule[stask].AST=find_EST(stask,Processor);
			 schedule[stask].AFT=calculate_EST_EFT(stask,Processor,tasks[stask].computation_costs);
			 schedule[stask].processor=Processor;
		}
		else if(S.size()==0&&SAB<0){
			//System.out.println("three lllll");
			for(int j=0;j<canPut.size();j++){
				double p_cost=calculate_p_cost(stask,canPut.get(j),tasks[stask].computation_costs);
				//System.out.println("任务可以放入虚拟机："+canPut.get(j)+" 花费为："+p_cost);
				if(p_cost<min){
					min=p_cost;
					Processor=canPut.get(j);
				}
			}
			schedule[stask].AST=find_EST(stask,Processor);
			schedule[stask].AFT=calculate_EST_EFT(stask,Processor,tasks[stask].computation_costs);
			schedule[stask].processor=Processor;
		}
		for(int j=0;j<no_service;j++){
			if(vms.get(schedule[stask].processor).service.get(j).vCPU>=tasks[stask].vCPU&&vms.get(schedule[stask].processor).service.get(j).RAM>=tasks[stask].RAM&&vms.get(schedule[stask].processor).service.get(j).Storage>=tasks[stask].Storage){
				Rank=j;
				break;
			}
		}
		if(vms.get(schedule[stask].processor).rankService==-1){
			vms.get(schedule[stask].processor).rankService=Rank;
		}
		/*for(int k=0;k<no_machines;k++){
			if(vms.get(k).flag==1&&k!=schedule[stask].processor){
				vms.get(k).rankService=-1;
			}
			vms.get(k).flag=0;
		}*/
		sumCost[stask]=calculate_sumCost(0);
	}
	
	
	void run(){                        //int num,String fileTwo
		System.out.println(Pay+"!!!!!!!!!!");
		long startTime=System.currentTimeMillis(); //获取开始时间
		//getVm();
		    pos=0;
		    noslots=0;
		    vms=new Vector<Vm>();
		    edges=new Vector<Edge>();
			readXML();
			getVm();
			double sumPrice=0;
			double sumTime=0;
			
			// Calculate upper rank
		    for(int i=0; i<no_tasks; i++)
		    {
		        if(tasks_upper_rank[i]==-1)
		        {
		            tasks_upper_rank[i]=calculate_upper_rank(i);
		            insertinto(i,tasks_upper_rank[i]);
		        }
		    }
		   /* for(int j=0;j<no_tasks;j++){
		        System.out.println("调度顺序："+sorted_tasks[j]);
		    }*/
		   /* for(int i=0;i<no_machines;i++){
		    	for(int j=0;j<no_service;j++){
		    		System.out.print(vms.get(i).service.get(j).vCPU+" ");
		    	}
		    	System.out.println();
		    }*/
			for(int i=0;i<no_tasks;i++){
				make_schedule(i);
				noslots++;
			}
			for(int j=0;j<sumCost.length;j++){
				if(sumCost[j]>sumPrice){
					sumPrice=sumCost[j];
				}
			}
			long endTime=System.currentTimeMillis(); //获取结束时间
			long runTime=endTime-startTime;
			if(sumPrice<=Pay)
				System.out.println("调度成功！");
			else
				System.out.println("调度没有成功");
			
			FileWriter writer=null;
			//String fileName=fileTwo+"//"+num+".txt";
			String fileName="E://研究生//微服务//2.txt";
			try{
				writer=new FileWriter(fileName,true);
				//writer.write("Mean function value:"+String.valueOf(averageFitness)+"\r\n");
				//writer.write("Standard deviation of mean function values:"+String.valueOf(stanFitness)+"\r\n");
				//writer.write("Mean other value:"+String.valueOf(averageValue)+"\r\n");
				//writer.write("Standard deviation of mean other values:"+String.valueOf(stanValue)+"\r\n");
				for(int i=0;i<no_tasks;i++){
					writer.write("任务"+i+"调度到虚拟机:"+schedule[i].processor+"\r\n");
					//System.out.println("任务"+i+"的实际完成时间:"+schedule[i].AFT);
					//System.out.println("任务"+i+"调度到虚拟机:"+schedule[i].processor);
					//System.out.println("任务"+i+"的实际完成时间:"+schedule[i].AFT);
				}
				for(int j=0;j<no_machines;j++){
					writer.write("虚拟机"+j+"性能参数："+vms.get(j).rankService+"\r\n");
					
				}
				for(int k=0;k<no_tasks;k++){
					if(schedule[k].AFT>sumTime){
						sumTime=schedule[k].AFT;
					}
				}
				 writer.write("time:"+sumTime+"\r\n");
				 writer.write("cost:"+sumPrice+"\r\n");
				 writer.write("runtime:"+runTime+"\r\n");
				
			}catch (IOException e){
				e.printStackTrace();
			}finally{
				try{
					if(writer!=null){
						writer.close();
					}
				}catch (IOException e){
					e.printStackTrace();
				}
			
				
			
			
		}
	 }
  }

