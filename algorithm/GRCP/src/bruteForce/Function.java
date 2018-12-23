package bruteForce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import task.ConfInfo;
import task.Node;
import task.Task;


public class Function {
	public static final int MAX_NUM=100;
	private List<Task> taskGraph;
//	private Task root=new Task();
	private List<Node> nodeGraph=new ArrayList<Node>();
	private double[][] netWidth=new double[MAX_NUM][MAX_NUM];  //网络带宽
	private double bandwidth; //固定带宽
	private double[][] netDelay=new double[MAX_NUM][MAX_NUM];  //网络延迟
	private double[][] taskData=new double[MAX_NUM][MAX_NUM];  //任务间的传输数据量
	private List<List<Double>> charge=new ArrayList<List<Double>>();   //性能阶梯
	private int nodeLevel;      //虚拟机规格
	private double ratio;
	private int taskNum;
	private int nodeNum;
	private double transTime[][]=new double[MAX_NUM][MAX_NUM];//只有一个传输任务不需要平分带宽的传输时间
	private double realTransTime[][]=new double[MAX_NUM][MAX_NUM]; //实际情况下平分带宽的传输时间
	private List<Task> rootTasks=new ArrayList<Task>(); //根任务
	private List<Task> criticalPath=new ArrayList<Task>();  //关键路径
	private double budget;
	private int maxCpu;        //单个虚拟机允许的最多CPU核数
	private double maxRam;     //单个虚拟机允许的最大内存
	private double maxStorage; //单个虚拟机允许的最大磁盘容量
	
	public Function(double budget){
		this.budget=budget;
	}
	
	public void init(){
		setTaskGraph(); //得到task的dag图
		setNodeGraph(); //得到虚拟机的信息
		getInput();   //得到一些输入信息
		getAllRootTask();
//		for(Task child:rootTasks){
//			root.addChild(child);
//		}
	}
	
	public void setTaskGraph(){
		ConfInfo info=new ConfInfo();
		taskGraph=info.getTaskGraph();
		taskNum=taskGraph.size();
		for(int i=0;i<taskNum;i++){
			Task task=taskGraph.get(i);
//			System.out.println(task.getId()+" "+task.getRunTime());
			Map<Task, Double> map=task.getTransData();
			for(Map.Entry<Task, Double> entry:map.entrySet()){
				taskData[entry.getKey().getIndex()][task.getIndex()]=entry.getValue();
			}
		}
		
		
//		for(int i=0;i<taskNum;i++){
//			for(int j=0;j<taskNum;j++){
//				System.out.print(taskData[i][j]+" ");
//			}
//			System.out.println();
//		}
	}
	
	public void setNodeGraph(){
		nodeNum=taskNum;
		for(int i=0;i<nodeNum;i++){
			Node node=new Node();
			node.setIndex(i);
			nodeGraph.add(node);
		}
		
	}
	
	public void getInput(){
		File file=new File("input.txt");
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String tempString="";
			while((tempString=reader.readLine())!=null){
				if(tempString.equals("﻿#固定带宽")){
					tempString=reader.readLine();
					bandwidth=Double.valueOf(tempString);
				}
//				if(tempString.equals("#带宽")){
//					for(int i=0;i<nodeNum;i++){
//						tempString=reader.readLine();
//						String[] temp=tempString.split(" ");
//						for(int j=0;j<nodeNum;j++){
//							netWidth[i][j]=Double.valueOf(temp[j]);
//						}
//					}
//				}
//				if(tempString.equals("#延时")){
//					for(int i=0;i<nodeNum;i++){
//						tempString=reader.readLine();
//						String[] temp=tempString.split(" ");
//						for(int j=0;j<nodeNum;j++){
//							netDelay[i][j]=Double.valueOf(temp[j]);
//						}
//					}
//				}
				if(tempString.equals("#占用比例")){
					tempString=reader.readLine();
					ratio=Double.valueOf(tempString);
				}
				if(tempString.equals("#虚拟机规格")){
					tempString=reader.readLine();
					nodeLevel=Integer.valueOf(tempString);
				}
				if(tempString.equals("#虚拟机定价")){
					for(int i=0;i<nodeLevel;i++){
						tempString=reader.readLine();
						String[] temp=tempString.split(" ");
						List<Double> list=new ArrayList<Double>();
						for(String string:temp){
							list.add(Double.valueOf(string));
						}
						charge.add(list);
					}
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		System.out.println("带宽");
//		for(int i=0;i<nodeNum;i++){
//			for(int j=0;j<nodeNum;j++){
//				System.out.print(netWidth[i][j]+" ");
//			}
//			System.out.println();
//		}
//		
//		System.out.println("延时");
//		for(int i=0;i<nodeNum;i++){
//			for(int j=0;j<nodeNum;j++){
//				System.out.print(netDelay[i][j]+" ");
//			}
//			System.out.println();
//		}
//		System.out.println("虚拟机规格："+nodeLevel);
//		System.out.println("虚拟机定价");
//		for(int i=0;i<charge.size();i++){
//			List<Double> list=charge.get(i);
//			for(Double temp:list){
//				System.out.print(temp+" ");
//			}
//			System.out.println();
//		}
		maxCpu=0;
		maxRam=0;
		maxStorage=0;
		for(int i=0;i<nodeLevel;i++){
			int cpu=(int)(charge.get(i).get(0)*ratio);
			double ram=charge.get(i).get(1)*ratio;
			double storage=charge.get(i).get(2)*ratio;
			if(cpu>maxCpu){
				maxCpu=cpu;
			}
			if(ram>maxRam){
				maxRam=ram;
			}
			if(storage>maxStorage){
				maxStorage=storage;
			}
		}
	}
	
	public void getAllRootTask(){
		for(int i=0;i<taskGraph.size();i++){
			if(taskGraph.get(i).getParents().size()==0){
				rootTasks.add(taskGraph.get(i));
			}
		}
	}
	
	//更新task的映射关系和节点的level，第一个为映射关系，第二个为对应节点的level
	public void updateTaskAndNode(double[] mArray){
		for(int i=0;i<taskNum;i++){
			taskGraph.get(i).setNodeIndex(-1);
		}
		for(int i=0;i<nodeNum;i++){
			Node node=nodeGraph.get(i);
			node.setLevel(-1);
			node.setCpu(0);
			node.setRam(0);
			node.setStorage(0);
			node.getTaskList().clear();		
		}
		
//		for(int i=0;i<mArray.length;i++){
//			Task task=taskGraph.get(i/2);
//			if(i%2==0){
//				int nodeIndex=(int) (mArray[i]*nodeNum);
//				if(nodeIndex>=nodeNum){
//					nodeIndex=nodeNum-1;
//				}
//				task.setNodeIndex(nodeIndex);
////				System.out.println("nodeIndex:"+nodeIndex);
//			}
//			else{
//				for(int j=0;j<nodeNum;j++){
//					Node node=nodeGraph.get(j);
//					if(node.getIndex()==task.getNodeIndex()){
//						int level=(int) (mArray[i]*nodeLevel);
//						if(level>nodeLevel){
//							level=nodeLevel-1;
//						}
//						node.getTaskList().add(task);
//						node.setLevel(level);
//						node.setCpu((int)(charge.get(level).get(0)*ratio));
//						node.setRam(charge.get(level).get(1)*ratio);
//						node.setStorage(charge.get(level).get(2)*ratio);
//						node.setCost(charge.get(level).get(3));
////						System.out.println("level:"+level);
//						break;
//					}
//				}
//			}
//		}
		
		for(int i=0;i<mArray.length;i++){
			Task task=taskGraph.get(i);
			int nodeIndex=(int) (mArray[i]*nodeNum);
			if(nodeIndex>=nodeNum){
				nodeIndex=nodeNum-1;
			}
			task.setNodeIndex(nodeIndex);
			for(int j=0;j<nodeNum;j++){
				Node node=nodeGraph.get(j);
				if(node.getIndex()==nodeIndex){
					node.getTaskList().add(task);
					node.setCpu(node.getCpu()+task.getCPU());
					node.setRam(node.getRam()+task.getRam());
					node.setStorage(node.getStorage()+task.getStorage());
//					node.setCost(charge.get(level).get(3));
					break;
				}
			}
		}
		
		for(int i=0;i<nodeNum;i++){
			Node node=nodeGraph.get(i);
			for(Task task:node.getTaskList()){
				task.setRunTime(node.getTaskList().size()*task.getTime());
			}
		}
	}
	
	public void updateTransTime(){
		int[][] transLoad=new int[MAX_NUM][MAX_NUM];
		for(int i=0;i<taskNum;i++){
			Task task=taskGraph.get(i);
			for(Task child:task.getChildren()){
//				System.out.println(task.getId()+" "+child.getId());
				transLoad[task.getNodeIndex()][child.getNodeIndex()]++;
			}
		}
		for(int i=0;i<taskNum;i++){
			for(int j=0;j<taskNum;j++){
				if(taskGraph.get(i).getNodeIndex()==taskGraph.get(j).getNodeIndex()){
					realTransTime[i][j]=0;
				}
				else{
					realTransTime[i][j]=taskData[i][j]/(bandwidth/transLoad[taskGraph.get(i).getNodeIndex()][taskGraph.get(j).getNodeIndex()]);
				}
			}
		}
	}
	
	public void findCriticalPath(){
		criticalPath.clear();
		Stack<Task> stack=new Stack<Task>();
		Stack<Task> stack1=new Stack<Task>();
		int[] degree=new int[taskNum]; //结点入度
//		double[] est=new double[taskNum];  //每个任务的最早开始时间
//		double[] lst=new double[taskNum];  //每个任务的最晚开始时间
		
		//求每个任务节点的入度，并初始化每个任务的最早开始时间和最早结束时间
		for(int i=0;i<taskGraph.size();i++){
			taskGraph.get(i).setEst(0);
			taskGraph.get(i).setLst(Double.MAX_VALUE);
			degree[taskGraph.get(i).getIndex()]=taskGraph.get(i).getParents().size();
			if(degree[taskGraph.get(i).getIndex()]==0){
				stack.push(taskGraph.get(i));
			}
		}
			
//		stack.push(taskGraph.get(0));
		//求每个结点的最早开始时间
		while(!stack.isEmpty()){
			Task task=stack.pop();
			for(int i=0;i<task.getChildren().size();i++){
				Task child=task.getChildren().get(i);
				if((--degree[child.getIndex()])==0){
					stack.push(child);
				}
				if(task.getEst()+task.getRunTime()+realTransTime[task.getIndex()][child.getIndex()]>child.getEst()){
					child.setEst(task.getEst()+task.getRunTime()+realTransTime[task.getIndex()][child.getIndex()]);
				}
			}
			stack1.push(task);
		}
		
//		System.out.println("最早开始时间");
//		for(int i=0;i<taskNum;i++){
//			System.out.print(taskGraph.get(i).getEst()+" ");
//		}
//		System.out.println();
		
		//求每个结点的最晚开始时间
		double time=Double.MIN_VALUE;
		Task index=new Task();
		for(int i=0;i<taskNum;i++){
			Task task=taskGraph.get(i);
			if(task.getChildren().size()==0){
				if(task.getEst()+task.getRunTime()>time){
					time=task.getEst()+task.getRunTime();
				}
			}
		}
		
		while(!stack1.isEmpty()){
			Task task=stack1.pop();
			if(task.getChildren().size()==0){
				task.setLst(time-task.getRunTime());
				continue;
			}
			for(int i=0;i<task.getChildren().size();i++){
				Task child=task.getChildren().get(i);
				if(child.getLst()-realTransTime[task.getIndex()][child.getIndex()]-task.getRunTime()<task.getLst()){
					task.setLst(child.getLst()-realTransTime[task.getIndex()][child.getIndex()]-task.getRunTime());
				}
			}
		}
		
//		System.out.println("最晚开始时间");
//		for(int i=0;i<taskNum;i++){
//			System.out.print(taskGraph.get(i).getLst()+" ");
//		}
//		System.out.println();
		
//		for(Task task:taskGraph){
//			if(Math.abs(task.getEst()-task.getLst())<0.000001){
//				criticalPath.add(task);
//			}
//		}
		//保证关键路径中的任务是按照顺序排列的
		for(Task task:rootTasks){
			if(Math.abs(task.getEst()-task.getLst())<0.000001){
				criticalPath.add(task);
				break;
			}
		}
		Task task=criticalPath.get(0);
		while(task.getChildren().size()>0){
			for(Task temp:task.getChildren()){
				if(Math.abs(temp.getEst()-temp.getLst())<0.000001){
					criticalPath.add(temp);
					break;
				}
			}
			task=criticalPath.get(criticalPath.size()-1);
		}
		
	}
	
	public double calculateMED(){	
		double time=0;
		int index=0;
		for(int i=0;i<criticalPath.size();i++){
//			System.out.println(criticalPath.get(i).getLst());
			if(criticalPath.get(i).getLst()>time){
				time=criticalPath.get(i).getLst();
				index=i;
			}
		}
		return time+criticalPath.get(index).getRunTime();
	}
	
	public double calculateCost(double time){
		double cost=0;
		for(int i=0;i<nodeNum;i++){
			Node node=nodeGraph.get(i);
			if(node.getTaskList().size()>0&&node.getLevel()>-1){
//				System.out.println(level);
				cost+=charge.get(node.getLevel()).get(3)*time;
			}
		}
		return cost;
	}
	
	public int getNodeLevel(Node node){
		List<Integer> list=new ArrayList<Integer>();
		for(int i=0;i<nodeLevel;i++){
			if(node.getCpu()<=ratio*charge.get(i).get(0)&&node.getRam()<=ratio*charge.get(i).get(1)
				&&node.getStorage()<=ratio*charge.get(i).get(2)){
//				node.setLevel(i);
				list.add(i);
			}
		}
		
		double price=Double.MAX_VALUE;
		int index=-1;
		for(int i:list){
			if(charge.get(i).get(3)<price){
				price=charge.get(i).get(3);
				index=i;
			}
		}
		
		return index;
	}
	
	public boolean canPut(){
		for(int i=0;i<nodeNum;i++){
			Node node=nodeGraph.get(i);

			if(node.getCpu()>maxCpu||node.getRam()>maxRam||node.getStorage()>maxStorage){
//				System.out.println(i);
				return false;
			}
			int level=getNodeLevel(node);
			node.setLevel(level);
			node.setCost(charge.get(level).get(3));
		}
		return true;
	}
	
	public double[] calculate(double[] mArray){
//		for(int i=0;i<mArray.length;i++){
//			System.out.print(mArray[i]+" ");
//		}
//		System.out.println();
		double[] object=new double[2];
		object[0]=Double.MAX_VALUE;
		object[1]=Double.MAX_VALUE;
		updateTaskAndNode(mArray);
//		for(Task task:taskGraph){
//			System.out.println(task.getId()+":"+task.getNodeIndex()+" "+task.getRunTime()+" "+task.getCPU()+" "+task.getRam()+" "+task.getStorage());
//		}
		
		if(!canPut()){
//			System.out.println(canPut());
			return object;
		}
//		for(Node node:nodeGraph){
//		    System.out.print(node.getIndex()+" "+node.getLevel()+" "+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" ");
//		    for(Task task:node.getTaskList()){
//			   System.out.print(task.getId()+" ");
//		    }
//		    System.out.println();
//	    }
		updateTransTime();
//		for(int i=0;i<taskNum;i++){
//			for(int j=0;j<taskNum;j++){
//				System.out.print(realTransTime[i][j]+" ");
//			}
//			System.out.println();
//		}
		findCriticalPath();
//		for(int j=0;j<criticalPath.size();j++){
//			System.out.println(criticalPath.get(j).getId());
//		}
		double runTime=calculateMED();
		double totalCost=calculateCost(runTime);
//		System.out.println(runTime+" "+totalCost);
		if(totalCost>budget){
			return object;
		}
		object[0]=runTime;
		object[1]=totalCost;
		return object;
	}
	
	public static void main(String[] args){
		Function function=new Function(1000);
		function.init();
//		double[] mArray=new double[10];
		for(int j=0;j<1;j++){
			System.out.println("************"+j+"************");
//			for (int i = 0; i < 10; i++) {
//	            double gene = Math.random();
//	            mArray[i] = gene;
//	        }
			double[] mArray={3.0/10,2.0/10,1.0/10,0.0/10,4.0/10,5.0/10,6.0/10,0.0/10,8.0/10,7.0/10};
			double object[]=function.calculate(mArray);
			System.out.println(object[0]+" "+object[1]);
		}
		
		
	}

}
