package task;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Rcp {
	private static final int MAX_NUM=100;
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
	private int maxCpu;        //单个虚拟机允许的最多CPU核数
	private double maxRam;     //单个虚拟机允许的最大内存
	private double maxStorage; //单个虚拟机允许的最大磁盘容量
	private List<Task> criticalPath=new ArrayList<Task>();  //关键路径
	private List<Double> med=new ArrayList<Double>();   //存放每代的MED
	private List<Double> costList=new ArrayList<Double>(); //存放减少代价过程中每代的代价
	private List<Task> rootTasks=new ArrayList<Task>(); //根任务
	private double budget;
	private double transTime[][]=new double[MAX_NUM][MAX_NUM];//只有一个传输任务不需要平分带宽的传输时间
	private double realTransTime[][]=new double[MAX_NUM][MAX_NUM]; //实际情况下平分带宽的传输时间
	private List<List<Task>> list=new ArrayList<List<Task>>(); //存储关键路径划分的结果
	
	public Rcp(double budget){
		this.budget=budget;
	}
	
	public void setTaskGraph(){
		ConfInfo info=new ConfInfo();
		taskGraph=info.getTaskGraph();
		taskNum=taskGraph.size();
		for(int i=0;i<taskNum;i++){
			Task task=taskGraph.get(i);
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
				if(tempString.equals("#固定带宽")){
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
		maxCpu=(int) (charge.get(nodeLevel-1).get(0)*ratio);
		maxRam=charge.get(nodeLevel-1).get(1)*ratio;
		maxStorage=charge.get(nodeLevel-1).get(2)*ratio;
	}
	
	public void getAllRootTask(){
		for(int i=0;i<taskGraph.size();i++){
			if(taskGraph.get(i).getParents().size()==0){
				rootTasks.add(taskGraph.get(i));
			}
		}
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
	
	//计算任务在具有不同负载的不同节点上的运行时间
	public double calculateRunTime(Task task,Node node){
		double time=task.getTime();
		return time*node.getTaskList().size();
	}
	//记得判断list中是否包含task
	public double calculateRunTime(Task task,List<Task> list){
		double time=task.getTime();
		if(list.contains(task)){
			time=time*list.size();
		}
		else{
			time=time*(list.size()+1);
		}
		return time;
	}
	
	public double calculateMED(){
		double time=0;
		int index=0;
		for(int i=0;i<criticalPath.size();i++){
			if(criticalPath.get(i).getLst()>time){
				time=criticalPath.get(i).getLst();
				index=i;
			}
		}
		return time+criticalPath.get(index).getRunTime();
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
	
	//更新节点上的CPU、内存和磁盘等信息，以及节点上任务的运行时间
	public void updateNode(int index,Task task){
		task.setNodeIndex(index);
		for(Node node:nodeGraph){
			if(node.getIndex()==index){
				node.getTaskList().add(task);
				node.setCpu(node.getCpu()+task.getCPU());
				node.setRam(node.getRam()+task.getRam());
				node.setStorage(node.getStorage()+task.getStorage());
				for(Task tempTask:node.getTaskList()){
					tempTask.setRunTime(calculateRunTime(tempTask, node));
				}
				break;
			}
		}

	}
	
	public void removeTask(int index,Task task){
		for(Node node:nodeGraph){
			if(node.getIndex()==index){
				node.getTaskList().remove(task);
				node.setCpu(node.getCpu()-task.getCPU());
				node.setRam(node.getRam()-task.getRam());
				node.setStorage(node.getStorage()-task.getStorage());
				for(Task tempTask:node.getTaskList()){
					tempTask.setRunTime(calculateRunTime(tempTask, node));
				}
				break;
			}
		}
	}

	public void mapGraph(){
		//进行一些必要的初始化，得到任务节点、虚拟机节点和输入信息
		init();
		med.add(Double.MAX_VALUE);
		//得到初始的传输时间
//		System.out.println(bandwidth);
		for(int i=0;i<taskNum;i++){
			for(int j=0;j<taskNum;j++){
				transTime[i][j]=taskData[i][j]/bandwidth;
				realTransTime[i][j]=transTime[i][j];
			}
		}

//		for(int i=0;i<taskNum;i++){
//			System.out.println(i+":"+taskGraph.get(i).getRunTime());
//			for(int j=0;j<taskNum;j++){
//				System.out.print(transTime[i][j]+" ");
//			}
//			System.out.println();
//		}
		findCriticalPath();
//		for(int j=0;j<criticalPath.size();j++){
//			System.out.println(criticalPath.get(j).getId());
//		}
//		System.out.println(time);
		med.add(calculateMED());
//		System.out.println(med.get(med.size()-1));
//		System.out.println(med.get(med.size()-2));
		while(Math.abs(med.get(med.size()-1)-med.get(med.size()-2))>=0.000001){
			for(int i=0;i<taskNum;i++){
				taskGraph.get(i).setNodeIndex(-1);
			}
			for(int i=0;i<nodeNum;i++){
				nodeGraph.get(i).getTaskList().clear();
				nodeGraph.get(i).setCpu(0);
				nodeGraph.get(i).setRam(0);
				nodeGraph.get(i).setStorage(0);
			}
			mapCriticalPath();
//			for(int j=0;j<criticalPath.size();j++){
//				System.out.println(criticalPath.get(j).getId()+" "+criticalPath.get(j).getNodeIndex());
//			}
			mapNonCriticalTask();
			
//			for(Task task:taskGraph){
//				System.out.println(task.getId()+":"+task.getNodeIndex()+" "+task.getRunTime());
//			}		
			updateTransTime();
//			for(Task task:taskGraph){
//				System.out.println(task.getId()+":"+task.getNodeIndex()+" "+task.getRunTime());
//			}
//			for(Node node:nodeGraph){
//			    System.out.print(node.getIndex()+" "+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" ");
//			    for(Task task:node.getTaskList()){
//				   System.out.print(task.getId()+" ");
//			    }
//			    System.out.println();
//		    }
			findCriticalPath();
//			for(int j=0;j<criticalPath.size();j++){
//				System.out.println(criticalPath.get(j).getId());
//		    }
			double time=calculateMED();
//			System.out.println(time);
			med.add(time);
		}
//		for(double time:med){
//			System.out.println(time);
//		}
//		for(Node node:nodeGraph){
//		    System.out.print(node.getIndex()+" "+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" ");
//		    for(Task task:node.getTaskList()){
//			   System.out.print(task.getId()+" ");
//		    }
//		    System.out.println();
//	    }
		mapTaskUnderBudget();
		
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
		
//		for(int i=0;i<taskNum;i++){
//			System.out.println(taskGraph.get(i).getEst());
//		}
		
		//求每个结点的最晚开始时间
		Task endTask=stack1.pop();
		endTask.setLst(endTask.getEst());
		while(!stack1.isEmpty()){
			Task task=stack1.pop();
			for(int i=0;i<task.getChildren().size();i++){
				Task child=task.getChildren().get(i);
				if(child.getLst()-realTransTime[task.getIndex()][child.getIndex()]-task.getRunTime()<task.getLst()){
					task.setLst(child.getLst()-realTransTime[task.getIndex()][child.getIndex()]-task.getRunTime());
				}
			}
		}
		
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
	
	//判断关键路径上的某几个连续任务可不可以放在一个虚拟机上，可以的话返回放在一个虚拟机上的运行时间，不可以的话返回无穷
	public double putTogether(List<Task> list){
//		System.out.println("******************");
//		for(int i=0;i<list.size();i++){
//			System.out.println(list.get(i).getId());
//		}
		int cpu=0;
		double ram=0;
		double storage=0;
		double time=0;
		for(int i=0;i<list.size();i++){
			cpu+=list.get(i).getCPU();
			ram+=list.get(i).getRam();
			storage+=list.get(i).getStorage();
			if(cpu>maxCpu||ram>maxRam||storage>maxStorage){
				return Double.MAX_VALUE;
			}
		}
		for(int i=0;i<list.size();i++){
			time+=calculateRunTime(list.get(i), list);
		}
//		System.out.println(time);
		return time;
	}
	
	//通过递归对关键路径上的任务进行划分，将放在一个虚拟机上的分到一个组中
	public void divideCriticalPath(int[][] s,int i,int j){
		if(i==j){
			List<Task> temp=new ArrayList<Task>();
			temp.add(criticalPath.get(i));
			list.add(temp);
			return;
		}
		if(s[i][j]==-1){
			List<Task> temp=new ArrayList<Task>();
			for(int k=i;k<=j;k++){
				temp.add(criticalPath.get(k));
			}
			list.add(temp);
			return;
		}
		divideCriticalPath(s, i, s[i][j]);
		divideCriticalPath(s, s[i][j]+1, j);
		
	}
	
	public void mapCriticalPath(){
		 double m[][]=new double[MAX_NUM][MAX_NUM];
		 int s[][]=new int[MAX_NUM][MAX_NUM];
		 int n=criticalPath.size();
		 
		 for(int i=0;i<n;i++){
			 m[i][i]=criticalPath.get(i).getTime();
		 }
//		 for(int i=0;i<n;i++){
//			 System.out.print(m[i][i]+" ");
//		 }
//		 System.out.println();
		 for(int l=2;l<=n;l++){
			 for(int i=0;i<n-l+1;i++){
				 int j=i+l-1;
//				 System.out.print(i+":"+j+" ");
				 List<Task> list=new ArrayList<Task>();
				 for(int k=i;k<=j;k++){
					 list.add(criticalPath.get(k));
				 }
				 //判断这些任务能不能放在一起，能的话计算得到一个时间
				 m[i][j]=putTogether(list);
//				 System.out.print(m[i][j]);
				 s[i][j]=-1;
				 for(int k=i;k<j;k++){
					 double q=m[i][k]+m[k+1][j]+transTime[criticalPath.get(k).getIndex()][criticalPath.get(k+1).getIndex()];
//					 System.out.print(q);
					 if(q<m[i][j]){
						 m[i][j]=q;
						 s[i][j]=k;
					 }
				 }
//				 System.out.println();
			 }
		 }
		 
//		 for(int i=0;i<n;i++){
//			 for(int j=0;j<n;j++){
//				 System.out.print(m[i][j]+" ");
//			 }
//			 System.out.println();
//		 }
//		 for(int i=0;i<n;i++){
//			 for(int j=0;j<n;j++){
//				 System.out.print(s[i][j]+" ");
//			 }
//			 System.out.println();
//		 }
		 list.clear();
		 divideCriticalPath(s, 0, criticalPath.size()-1);
		 
		 
		 for(int i=0;i<list.size();i++){
			 List<Task> temp=list.get(i);
			 for(Task task:temp){
				 task.setNodeIndex(i);
				 task.setRunTime(calculateRunTime(task, temp));
			 }
			 
			 
		 }
		 
//		 for(int i=0;i<list.size();i++){
//			 List<Task> temp=list.get(i);
//			 for(Task task:temp){
//				 System.out.print(task.getNodeIndex());
//			 }
//			 System.out.println();
//		 }
		 
		 
		 for(Task task:criticalPath){
//			 updateNode(task.getNodeIndex(),task);
			 for(Node node:nodeGraph){
					if(node.getIndex()==task.getNodeIndex()){
						node.getTaskList().add(task);
						node.setCpu(node.getCpu()+task.getCPU());
						node.setRam(node.getRam()+task.getRam());
						node.setStorage(node.getStorage()+task.getStorage());
						break;
					}
				}
		 }
		 
//		 for(Node node:nodeGraph){
//			 System.out.print(node.getIndex()+" "+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" ");
//			 for(Task task:node.getTaskList()){
//				 System.out.print(task.getId()+" ");
//			 }
//			 System.out.println();
//		 }

	}
	
	public List<Task> sort(Task parent){
		List<Task> sortChildren=new ArrayList<Task>();
		List<Task> children=parent.getChildren();
		double[] weight=new double[children.size()];
		int[] index=new int[children.size()];
		for(int i=0;i<children.size();i++){
			Task child=children.get(i);
			weight[i]=child.getTime()+taskData[parent.getIndex()][child.getIndex()]/bandwidth;
			index[i]=i;
		}
		
		for(int i=1;i<weight.length;i++){
			double key=weight[i];
			int keyIndex=index[i];
			int j=i-1;
			while(j>=0&&weight[j]<key){
				weight[j+1]=weight[j];
				index[j+1]=index[j];
				j--;
				
			}
			weight[j+1]=key;
			index[j+1]=keyIndex;
		}
		
		for(int i=0;i<index.length;i++){
			sortChildren.add(children.get(index[i]));
		}
		return sortChildren;
	}
	
	public Map<Task, Task> currentTransLoad(int start,int end){
		Map<Task, Task> map=new HashMap<Task, Task>();
		List<Task> parents=nodeGraph.get(start).getTaskList();
		for(Task parent:parents){
			for(Task child:parent.getChildren()){
				if(child.getNodeIndex()==end){
					map.put(parent, child);
				}
			}
		}
		return map;
	}
	
	//判断任务task是否可以放在节点index上
	public boolean canPut(int index,Task task){
		Node node=nodeGraph.get(index);
		if(node.getCpu()+task.getCPU()>maxCpu||node.getRam()+task.getRam()>maxRam
				||node.getStorage()+task.getStorage()>maxStorage){
			return false;
		}
		return true;
	}
	
	public void mapNonCriticalTask(){
//		System.out.println("******************");
		LinkedList<Task> queue=new LinkedList<Task>();
		for(int i=0;i<criticalPath.size();i++){
			queue.offer(criticalPath.get(i));
		}
		//先对根节点进行分配
		for(Task root:rootTasks){
			if(root.getNodeIndex()<0){
				double time=Double.MAX_VALUE;
				int index=-1;
				for(int i=0;i<nodeNum;i++){
					if(!canPut(i,root)){
						continue;
					}
					double tempTime=0;
					Node node=nodeGraph.get(i);
					List<Task> taskList=new ArrayList<Task>(); //存放随时的某个节点上的运行任务节点
					for(Task nodeTask:node.getTaskList()){
						taskList.add(nodeTask);
					}
					taskList.add(root);
					for(Task nodeTask:node.getTaskList()){
						double temp=calculateRunTime(nodeTask, taskList);  //加上一个任务负载后原有任务变化后的时间
//						nodeTask.setRunTime(temp);
						tempTime+=temp-nodeTask.getRunTime();
					}
					tempTime+=calculateRunTime(root, taskList);  //此处为更新后的运行时间
					if(tempTime<time){
						time=tempTime;
						index=i;
					}
				}
				updateNode(index, root);
				queue.add(root);
			}
		}
		
//		for(Task task:queue){
//			System.out.println(task.getId()+" "+task.getNodeIndex());
//		}

		while(!queue.isEmpty()){
			Task task=queue.poll();
//			int nodeIndex=task.getNodeIndex();
			List<Task> list=sort(task);
//			List<Task> list=task.getChildren();
//			System.out.println(task.getId()+" "+task.getNodeIndex()+" "+task.getChildren().size());
			for(Task child:list){
//				System.out.println(child.getId()+" "+child.getNodeIndex());
				if(child.getNodeIndex()<0){
//					System.out.println(child.getId());
					double time=Double.MAX_VALUE;
					int index=-1;
					for(int i=0;i<nodeNum;i++){
						if(!canPut(i,child)){
//							System.out.println(i+" "+canPut(i, child));
							continue;
						}
						if(i==task.getNodeIndex()){
							//此处需要加一个更新运行时间的过程，如果放在同一个虚拟机上
							double tempTime=0;
							Node node=nodeGraph.get(i);
							List<Task> taskList=new ArrayList<Task>(); //存放随时的某个节点上的运行任务节点
							for(Task nodeTask:node.getTaskList()){
								taskList.add(nodeTask);
							}
							taskList.add(child);
							for(Task nodeTask:node.getTaskList()){
								double temp=calculateRunTime(nodeTask, taskList);  //加上一个任务负载后原有任务变化后的时间
//								nodeTask.setRunTime(temp);
								tempTime+=temp-nodeTask.getRunTime();
							}
							tempTime+=calculateRunTime(child, taskList);  //此处为更新后的运行时间
							if(tempTime<time){
								time=tempTime;
								index=i;
							}
						}
						else{
							double tempTime=0;
							Node node=nodeGraph.get(i);
							List<Task> taskList=new ArrayList<Task>(); //存放随时的某个节点上的运行任务节点
							for(Task nodeTask:node.getTaskList()){
								taskList.add(nodeTask);
							}
							taskList.add(child);
							for(Task nodeTask:node.getTaskList()){
								double temp=calculateRunTime(nodeTask, taskList);  //加上一个任务负载后原有任务变化后的时间
//								nodeTask.setRunTime(temp);
								tempTime+=temp-nodeTask.getRunTime();
							}
							tempTime+=calculateRunTime(child, taskList);  //此处为更新后的运行时间
//							System.out.println(task.getNodeIndex());
							Map<Task, Task> transLoad=currentTransLoad(task.getNodeIndex(), i);
							for(Map.Entry<Task, Task> entry:transLoad.entrySet()){
								
								tempTime+=transTime[entry.getKey().getIndex()][entry.getValue().getIndex()];
							}
							tempTime+=transTime[task.getIndex()][child.getIndex()]*(transLoad.size()+1);
							if(tempTime<time){
								time=tempTime;
								index=i;
							}
						}
//						System.out.println(i+":"+index+" "+time);
					}
					
//					child.setNodeIndex(index);
					updateNode(index, child);
					queue.add(child);
//					System.out.println(child.getId()+":"+child.getNodeIndex()+" "+index);
				}
//				System.out.println("node:"+child.getNodeIndex());
			}
		}
	
		
	}
	
	public int getNodeLevel(Node node){	
		for(int i=0;i<nodeLevel;i++){
			if(node.getCpu()<=ratio*charge.get(i).get(0)&&node.getRam()<=ratio*charge.get(i).get(1)
				&&node.getStorage()<=ratio*charge.get(i).get(2)){
//				node.setLevel(i);
				return i;
			}
		}
		
		return -1;
	}
	
	public double calculateCost(double time){
		double cost=0;
		for(int i=0;i<nodeNum;i++){
			int level=getNodeLevel(nodeGraph.get(i));
			if(nodeGraph.get(i).getTaskList().size()>0){
//				System.out.println(level);
				cost+=charge.get(level).get(3)*time;
			}
		}
		return cost;
	}
	public void setNodeLevel(){
		for(int i=0;i<nodeNum;i++){
			Node node=nodeGraph.get(i);
			node.setLevel(getNodeLevel(node));
		}
	}
	
	public void mapTaskUnderBudget(){
		double time=med.get(med.size()-1);
//		System.out.println(time);
		double cost=calculateCost(time);
		
//		System.out.println(cost);
		costList.add(cost);
//		for(Node node:nodeGraph){
//			System.out.println(node.getIndex()+":"+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" "+node.getLevel());
//		}
		
		while(cost>budget){
//			System.out.println("******************");
			int taskIndex=-1;
			int nodeIndex=-1;
			double minCost=Double.MAX_VALUE; //最好结果时的代价
			double minTime=Double.MAX_VALUE;//最好结果时的时间
			double weight=Double.MAX_VALUE; //时间差与代价差的比例
			for(int i=0;i<criticalPath.size();i++){
				Task task=criticalPath.get(i);
//				System.out.println("task:"+task.getIndex());
				int index=task.getNodeIndex();
				for(int j=0;j<nodeNum;j++){
//					System.out.println("node"+j);
					if(j!=index&&canPut(j, task)){
//						task.setNodeIndex(j);
						removeTask(index, task);
						updateNode(j, task);
						updateTransTime();
//						for(Task tempTask:taskGraph){
//							System.out.println(tempTask.getId()+":"+tempTask.getNodeIndex()+" "+tempTask.getRunTime());
//						}
//						for(Node node:nodeGraph){
//						    System.out.print(node.getIndex()+" "+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" ");
//						    for(Task tempTask:node.getTaskList()){
//							   System.out.print(tempTask.getId()+" ");
//						    }
//						    System.out.println();
//					    }
//						for(int k=0;k<taskNum;k++){
//							for(int l=0;l<taskNum;l++){
//								System.out.print(realTransTime[k][l]+" ");
//							}
//							System.out.println();
//						}
						findCriticalPath();
//						for(int k=0;k<criticalPath.size();k++){
//							System.out.println(criticalPath.get(k).getId());
//						}
//						for(int k=0;k<taskNum;k++){
//							System.out.println(taskGraph.get(k).getEst()+" "+taskGraph.get(k).getLst());
//						}
						
						double tempTime=calculateMED();
						double timeDiff=tempTime-time;
//						System.out.println("tempTime:"+tempTime);
						double tempCost=calculateCost(tempTime);
						double costDiff=cost-tempCost;
//						for(Node node:nodeGraph){
//							System.out.println(node.getIndex()+":"+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" "+node.getLevel());
//						}
//						System.out.println("tempCost:"+tempCost);
						double tempWeight=timeDiff/costDiff;
						if(costDiff>0&&tempWeight<weight){
							weight=tempWeight;
							taskIndex=task.getIndex();
							nodeIndex=j;
							minCost=tempCost;
							minTime=tempTime;
						}
						
//						task.setNodeIndex(index);
						updateNode(index, task);
						removeTask(j, task);
//						for(Task tempTask:taskGraph){
//							System.out.println(tempTask.getId()+":"+tempTask.getNodeIndex()+" "+tempTask.getRunTime());
//						}
//						for(Node node:nodeGraph){
//						    System.out.print(node.getIndex()+" "+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" ");
//						    for(Task tempTask:node.getTaskList()){
//							   System.out.print(tempTask.getId()+" ");
//						    }
//						    System.out.println();
//					    }
					}
				}
			}
			
//			System.out.println("taskIndex:"+taskIndex);
//			System.out.println("nodeIndex:"+nodeIndex);
			if(taskIndex<0||nodeIndex<0){
				break;
			}
			for(int i=0;i<taskNum;i++){
				Task task=taskGraph.get(i);
				if(task.getIndex()==taskIndex){
//					taskGraph.get(i).setNodeIndex(nodeIndex);
					removeTask(task.getNodeIndex(), task);
					updateNode(nodeIndex, task);
					break;
				}
			}
//			updateTransTime();
//			findCriticalPath();
			setNodeLevel();
			med.add(minTime);
			costList.add(minCost);
			time=minTime;
			cost=minCost;
		}
//		for(Task tempTask:taskGraph){
//			System.out.println(tempTask.getId()+":"+tempTask.getNodeIndex()+" "+tempTask.getRunTime());
//		}
//		for(Node node:nodeGraph){
//		    System.out.print(node.getIndex()+" "+node.getCpu()+" "+node.getRam()+" "+node.getStorage()+" "+node.getLevel()+" ");
//		    for(Task tempTask:node.getTaskList()){
//			   System.out.print(tempTask.getId()+" ");
//		    }
//		    System.out.println();
//	    }
			System.out.println(med.get(med.size()-1));
			System.out.println(costList.get(costList.size()-1));
//			
		}
	
	public static void main(String[] args){
		Rcp rcp=new Rcp(700);
		rcp.mapGraph();
		
	}
	
}
