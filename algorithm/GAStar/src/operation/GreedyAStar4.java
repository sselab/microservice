package operation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;

import entity.Assignment;
import entity.EdgeOfGraph;
import entity.Microservice;
import entity.MicserviceStateInSearchTree;
import entity.PublicAttribute;
import entity.VMPerformance;


/**
 * 2017.1.16 跑45-60节点的，只运行一次，budget分开
 * */
public class GreedyAStar4 {
	//搜索的深度，即微服务个数
	int depth = PublicAttribute.microserviceNum;	
	
	//搜索过程中维护一张open列表，存放带扩展的节点
	List<MicserviceStateInSearchTree> openList = new ArrayList<MicserviceStateInSearchTree>();
	
	//搜索过程中维护一张closed列表，存放扩展完成的节点
	//List<MicserviceStateInSearchTree> closedList = new ArrayList<MicserviceStateInSearchTree>();
	MicserviceStateInSearchTree closedSearchNode = new MicserviceStateInSearchTree();
	
	//当前已经做了的分配状态
	List<Assignment> allDepthAssign = new ArrayList<Assignment>();//存放每一层的分配，f(x)最小的
	
	//存放虚拟机及其性能的对应关系,虚拟机编号-性能编号
	Map<Integer,Integer> vm_perMap = new HashMap<Integer,Integer>();
	
	//存放虚拟机及其服务列表，虚拟机编号-服务列表
	Map<Integer,List<Assignment>> vm_serviceMap = new HashMap<Integer,List<Assignment>>();	
	
	//当前遍历深度，初始化为0
	int currDepth = 0;		
	//微服务个数
	int serviceNum = PublicAttribute.microserviceNum;
	//虚拟机个数
	int vmNumber = PublicAttribute.nodeNum;
	//虚拟机规格个数
	int vmPerNumber = PublicAttribute.performanceNum;
	//代价约束
	double limitPrice = 0;
	//统计虚拟机之间的通信链路数
	int[][] vmAmongLinkNum = new int[vmNumber][vmNumber];
	
	//一个字符串，表示该实例的时间花费、成本花费、运行时间，用@符号隔开
	String resultStr = new String();
	
	public GreedyAStar4(double budget){
		this.limitPrice = budget;
	}
	/**
	 * 算法入口
	 * */
	public String solution(){
		//初始化vm_serviceMap，即每个虚拟机上一开始放置的服务均为空
		for(int i=0;i<vmNumber;i++){
			List<Assignment> assignList = new ArrayList<Assignment>();
			vm_serviceMap.put(i, assignList);
		}
		//初始化vm_perMap，即每个虚拟机的性能一开始都是-1，还没有做任何选择
		for(int i=0;i<vmNumber;i++){
			vm_perMap.put(i, -1);
		}
		
		while(currDepth < depth){
			//System.out.println("当前深度："+currDepth);
			int minFxIndex = returnMinFx();//找到open列表中f(x)最小的节点	
			//System.out.println("当前open表中f(x)值最小的节点索引："+minFxIndex);
			MicserviceStateInSearchTree minFxNode = new MicserviceStateInSearchTree();
			if(minFxIndex == -1){//即open表为空
				minFxNode = null;
			}else{
				minFxNode = openList.get(minFxIndex);
				closedSearchNode = minFxNode;
				//closedList.add(minFxNode);//插入closed列表
				openList.clear();//清空open表，下次只从新扩展的节点中进行选择，即贪心
				int selectVm = minFxNode.getCurrDepthAssign().getVMNum();					
				int selectVmPer = minFxNode.getCurrDepthAssign().getVMPerIndex();
				allDepthAssign.add(minFxNode.getCurrDepthAssign());
				vm_perMap.put(selectVm, selectVmPer);
				vm_serviceMap.get(selectVm).add(minFxNode.getCurrDepthAssign());
				for(int m=0;m<vmNumber;m++){
					for(int n=0;n<vmNumber;n++){
						vmAmongLinkNum[m][n] = 0;
					}
				}
				getVmLinkNum(vmAmongLinkNum , allDepthAssign);
				currDepth++;
			}			
			
			if(currDepth < depth){
				//插入节点到open列表中，计算f(x).minFxNode即为插入节点的父节点 
				insertNodeToOpen(minFxNode);
			}			
		}
		//allDepthAssign中的即为最终分配结果
		System.out.println("最终分配结果如下：");
		printfAllDepthAssign();		
		System.out.println("总时间："+closedSearchNode.getTimeFunctionVal());
		System.out.println("总代价："+closedSearchNode.getPriceFunctionVal());
		return closedSearchNode.getTimeFunctionVal()+"@"+closedSearchNode.getPriceFunctionVal();
	}
	
	/**
	 * 创建搜索树节点，插入open表
	 * */
	public void insertNodeToOpen(MicserviceStateInSearchTree parentNode){
		//System.out.println("插入新节点");		
		//先为服务选择虚拟机
		for(int i=0;i<vmNumber;i++){
			//即该虚拟机的性能已经做了分配
			if(vm_perMap.get(i) != -1){	
				Microservice currService = new Microservice(PublicAttribute.serviceList[currDepth]);
				//存储服务间的传输时间，初始化为0
				double[][] transTimeArr = new double[serviceNum][serviceNum]; 
				for(int m=0;m<serviceNum;m++){
					for(int n=0;n<serviceNum;n++){
						transTimeArr[m][n] = 0;
					}
				}				
				//创建临时map
				List<Assignment> tempDepthAssign = new ArrayList<Assignment>();//拷贝之前的分配
				Map<Integer,Integer> tempVm_perMap = new HashMap<Integer,Integer>();
				Map<Integer,List<Assignment>> tempVm_serviceMap = new HashMap<Integer,List<Assignment>>();
				int[][] tempVmLinkNum = vmAmongLinkNum;
				cloneCurrDepthAssign(allDepthAssign , tempDepthAssign);//把currDepthAssign拷贝给tempDepthAssign
				copyIntegerMap(vm_perMap , tempVm_perMap);
				cloneVmServiceMap(vm_serviceMap , tempVm_serviceMap);				
				
				VMPerformance currPer = PublicAttribute.performanceList[vm_perMap.get(i)];
				//遍历该虚拟机上的服务，计算其性能之和
				double allCpu = 0;
				double allRam = 0;
				double allStorage = 0;
				for(int j=0;j<vm_serviceMap.get(i).size();j++){
					Assignment tempAssign =  vm_serviceMap.get(i).get(j);
					allCpu += tempAssign.getServiceInfo().getCpu();
					allRam += tempAssign.getServiceInfo().getRam();
					allStorage += tempAssign.getServiceInfo().getStorage();
				}
				if(currService.getCpu() <= (currPer.getCpu() - allCpu) && currService.getRam() <= (currPer.getRam() - allRam) && currService.getStorage() <= (currPer.getStorage() - allStorage)){
					//根据之前所做分配下，所选虚拟机上的服务数量更新实际运行时间
					currService.setRealRunTime(currService.getRuntime() * (vm_serviceMap.get(i).size() + 1));
					Assignment assignNode = new Assignment();
					assignNode.setServiceInfo(currService);
					assignNode.setVMNum(i);
					assignNode.setVMPerIndex(vm_perMap.get(i));
					MicserviceStateInSearchTree searchNode = new MicserviceStateInSearchTree();
					searchNode.setCurrDepth(currDepth);
					//计算通信时间，与已经分配的服务通信的最长时间
					double transTime = 0;
					for(int m=0;m<allDepthAssign.size();m++){
						 Assignment tempAssignment = allDepthAssign.get(m);
						 searchNode.getPartialAssign().add(tempAssignment);//先把先前的分配放进去
						 if(PublicAttribute.serviceData[m][currService.getIndex()] != 0){
							 //表示有通信
							 if(tempAssignment.getVMNum() == i){//同一个主机
						    	assignNode.getAllTrans()[m] = 0;
						     }else{
						    	 assignNode.getAllTrans()[m] = PublicAttribute.serviceData[m][currService.getIndex()] / (PublicAttribute.flexNetWidth / (vmAmongLinkNum[tempAssignment.getVMNum()][i] + 1)); 
						     }
							 if( assignNode.getAllTrans()[m] > transTime){
						    		transTime =  assignNode.getAllTrans()[m];
						     }
						 }
					}
					assignNode.setTransTime(transTime);
					searchNode.setCurrDepthAssign(assignNode);
					searchNode.getPartialAssign().add(assignNode);	
					searchNode.setParentSearchNode(parentNode);
					//更新下面四个值
					tempVm_perMap.put(i, vm_perMap.get(i));
					tempVm_serviceMap.get(i).add(assignNode);					
					tempDepthAssign.add(assignNode);
					getVmLinkNum(tempVmLinkNum , tempDepthAssign);
					for(int m=0;m<serviceNum;m++){
						transTimeArr[m][currService.getIndex()] = assignNode.getAllTrans()[m];
					}
					calculateFx(searchNode , transTimeArr , tempVm_perMap , tempVm_serviceMap , tempDepthAssign , tempVmLinkNum);	
					//System.out.println("节点分配：s"+currService.getIndex()+"--v"+i+"--p"+vm_perMap.get(i)+" 估计时间："+searchNode.getTimeFunctionVal()+" 估计代价："+searchNode.getPriceFunctionVal());
					openList.add(searchNode);//加入到open列表中
					//System.out.println("open表大小："+openList.size());
					
				}
			}else{
				//该虚拟机未分配性能，则有vmPerNumber种可选
				for(int j=0;j<vmPerNumber;j++){
					Microservice currService = new Microservice(PublicAttribute.serviceList[currDepth]);
					//存储服务间的传输时间，初始化为0
					double[][] transTimeArr = new double[serviceNum][serviceNum]; 
					for(int m=0;m<serviceNum;m++){
						for(int n=0;n<serviceNum;n++){
							transTimeArr[m][n] = 0;
						}
					}
					//创建临时map
					List<Assignment> tempDepthAssign = new ArrayList<Assignment>();//拷贝之前的分配
					Map<Integer,Integer> tempVm_perMap = new HashMap<Integer,Integer>();
					Map<Integer,List<Assignment>> tempVm_serviceMap = new HashMap<Integer,List<Assignment>>();
					int[][] tempVmLinkNum = vmAmongLinkNum;
					cloneCurrDepthAssign(allDepthAssign , tempDepthAssign);//把currDepthAssign拷贝给tempDepthAssign
					copyIntegerMap(vm_perMap , tempVm_perMap);
					cloneVmServiceMap(vm_serviceMap , tempVm_serviceMap);
					
					double vmCpu = PublicAttribute.performanceList[j].getCpu();
					double vmRam = PublicAttribute.performanceList[j].getRam();
					double vmStorage = PublicAttribute.performanceList[j].getStorage();
					if(currService.getCpu() <= vmCpu && currService.getRam() <= vmRam && currService.getStorage() <= vmStorage){
						currService.setRealRunTime(currService.getRuntime() * (vm_serviceMap.get(i).size() + 1));
						Assignment assignNode = new Assignment();
						assignNode.setServiceInfo(currService);
						assignNode.setVMNum(i);
						assignNode.setVMPerIndex(j);
						MicserviceStateInSearchTree searchNode = new MicserviceStateInSearchTree();
						searchNode.setCurrDepth(currDepth);
						//计算通信时间，与已经分配的服务通信的最长时间
						double transTime = 0;
						for(int m=0;m<allDepthAssign.size();m++){
							 Assignment tempAssignment = allDepthAssign.get(m);
							 searchNode.getPartialAssign().add(tempAssignment);//先把先前的分配放进去
							 if(PublicAttribute.serviceData[m][currService.getIndex()] != 0){
								 //表示有通信								
							     assignNode.getAllTrans()[m] = PublicAttribute.serviceData[m][currService.getIndex()] / (PublicAttribute.flexNetWidth / (vmAmongLinkNum[tempAssignment.getVMNum()][i] + 1)); 							     
								 if( assignNode.getAllTrans()[m] > transTime){
							    		transTime =  assignNode.getAllTrans()[m];
							     }
							 }
						}
						assignNode.setTransTime(transTime);
						searchNode.setCurrDepthAssign(assignNode);
						searchNode.getPartialAssign().add(assignNode);	
						searchNode.setParentSearchNode(parentNode);
						//更新下面四个值
						tempVm_perMap.put(i, j);
						tempVm_serviceMap.get(i).add(assignNode);					
						tempDepthAssign.add(assignNode);
						getVmLinkNum(tempVmLinkNum , tempDepthAssign);
						for(int m=0;m<serviceNum;m++){
							transTimeArr[m][currService.getIndex()] = assignNode.getAllTrans()[m];
						}
						/*System.out.println("服务间传输时间矩阵：");
						for(int m=0;m<serviceNum;m++){
							for(int n=0;n<serviceNum;n++){
								System.out.print(transTimeArr[m][n]+" ");
							}
							System.out.println();
						}*/	
						calculateFx(searchNode , transTimeArr , tempVm_perMap , tempVm_serviceMap , tempDepthAssign , tempVmLinkNum);	
						//System.out.println("节点分配：s"+currService.getIndex()+"--v"+i+"--p"+j+" 估计时间："+searchNode.getTimeFunctionVal()+" 估计代价："+searchNode.getPriceFunctionVal());
						openList.add(searchNode);//加入到open列表中
						//System.out.println("open表大小："+openList.size());
						
					}
				}
			}
		
		}
	}
	/**
	 * 计算每个搜索节点的f(x)时间估计值
	 * */
	public void calculateFx(MicserviceStateInSearchTree searchNode , double[][] transTimeArr , Map<Integer,Integer>  tempVm_perMap , Map<Integer,List<Assignment>> tempVm_serviceMap , List<Assignment> tempDepthAssign ,int[][] tempVmLinkNum){
		//遍历微服务list中未分配的服务，为其进行临时分配，计算f(x)
		for(int i=currDepth+1;i<PublicAttribute.serviceList.length;i++){
			//存放该服务可能的所有分配情况，每种情况对应一个时间成本
			List<Assignment> possibleAssign = new ArrayList<Assignment>();
			Microservice tempService = PublicAttribute.serviceList[i];	
			//System.out.println("*************************************当前服务编号：s"+i);
			for(int j=0;j<vmNumber;j++){				
				if(tempVm_perMap.get(j) != -1){
					Microservice service = new Microservice(tempService);	
					VMPerformance currPer = PublicAttribute.performanceList[tempVm_perMap.get(j)];
					//遍历该虚拟机上的服务，计算其性能之和
					double allCpu = 0;
					double allRam = 0;
					double allStorage = 0;
					for(int k=0;k<tempVm_serviceMap.get(j).size();k++){
						Assignment tempAssign =  tempVm_serviceMap.get(j).get(k);
						allCpu += tempAssign.getServiceInfo().getCpu();
						allRam += tempAssign.getServiceInfo().getRam();
						allStorage += tempAssign.getServiceInfo().getStorage();
					}
					if(service.getCpu() <= (currPer.getCpu() - allCpu) && service.getRam() <= (currPer.getRam() - allRam) && service.getStorage() <= (currPer.getStorage() - allStorage)){						
						service.setRealRunTime(service.getRuntime() * (tempVm_serviceMap.get(j).size() + 1));
						Assignment assignState1 = new Assignment();
						assignState1.setServiceInfo(service);
						assignState1.setVMNum(j);
						assignState1.setVMPerIndex(tempVm_perMap.get(j));
						//计算通信时间，与已经分配的服务通信的最长时间
						double transTime = 0;
						for(int m=0;m<tempDepthAssign.size();m++){
							 Assignment tempAssignment = tempDepthAssign.get(m);
							 if(PublicAttribute.serviceData[m][service.getIndex()] != 0){
								 //表示有通信
								 if(tempAssignment.getVMNum() == j){//同一个主机
									 assignState1.getAllTrans()[m] = 0;
							     }else{
							    	 assignState1.getAllTrans()[m] = PublicAttribute.serviceData[m][service.getIndex()] / (PublicAttribute.flexNetWidth / (tempVmLinkNum[tempAssignment.getVMNum()][j] + 1)); 
							     }
								 if( assignState1.getAllTrans()[m] > transTime){
							    		transTime =  assignState1.getAllTrans()[m];
							     }
							 }
						}						
						assignState1.setTransTime(transTime);
						//System.out.println("√  s"+tempService.getIndex()+"--v"+j+"--p"+tempVm_perMap.get(j)+"，运行时间："+tempService.getRuntime()+"，最大传输时间："+transTime);
						possibleAssign.add(assignState1);
					}
				}else{
					//System.out.println("未分配性能");
					for(int k=0;k<vmPerNumber;k++){
						Microservice service = new Microservice(tempService);
						VMPerformance perTemp = PublicAttribute.performanceList[k];
						if(service.getCpu() <= perTemp.getCpu()
								&& service.getRam() <= perTemp.getRam() 
								&& service.getStorage() <= perTemp.getStorage()){							
							service.setRealRunTime(service.getRuntime() * (tempVm_serviceMap.get(j).size() + 1));
							Assignment assignState2 = new Assignment();
							assignState2.setServiceInfo(service);
							assignState2.setVMNum(j);
							assignState2.setVMPerIndex(k);
							//计算通信时间，与已经分配的服务通信的最长时间
							double transTime = 0;
							for(int m=0;m<tempDepthAssign.size();m++){
								 Assignment tempAssignment = tempDepthAssign.get(m);
								 if(PublicAttribute.serviceData[m][service.getIndex()] != 0){
									 //表示有通信
								     assignState2.getAllTrans()[m] = PublicAttribute.serviceData[m][service.getIndex()] / (PublicAttribute.flexNetWidth / (tempVmLinkNum[tempAssignment.getVMNum()][j] + 1)); 
									 if( assignState2.getAllTrans()[m] > transTime){
								    		transTime =  assignState2.getAllTrans()[m];
								     }
								 }
							}								 
							assignState2.setTransTime(transTime);
							possibleAssign.add(assignState2);
							//System.out.println("√  s"+tempService.getIndex()+"--v"+j+"--p"+k+"支持，运行时间："+tempService.getRuntime()+",最大传输时间："+transTime);
							break;
						}
					}
					break;
				}
			}
			
			//遍历每个服务的可能分配情况，从中代价最小的分配，
			//double minServicePrice = Double.MAX_VALUE;
			//int minServicePriceIndex = 0;//标记最小代价对应分配的编号
			//遍历每个服务的可能分配情况，从中找出（运行时间+传输时间）最小的分配，
			double minServiceTime = Double.MAX_VALUE;
			int minServiceTimeIndex = 0;//标记最小时间对应分配的编号
			
			//为每个分配计算当前已经话费的时间和代价，从中进行选择2017.1.14新改
			for(int s=0;s<possibleAssign.size();s++){
				MicserviceStateInSearchTree tempSearchNode = new MicserviceStateInSearchTree();
				double[][] tempTrans = transTimeArr;
				//先把之前所做的所有分配加进去
				for(int n=0;n<searchNode.getPartialAssign().size();n++){
					tempSearchNode.getPartialAssign().add(new Assignment(searchNode.getPartialAssign().get(n)));
				}
				//把当前临时分配加进去
				tempSearchNode.getPartialAssign().add(new Assignment(possibleAssign.get(s)));
				//把剩下未分配服务的运行时间和传输时间置为0
				for(int n=tempSearchNode.getPartialAssign().size();n<serviceNum;n++){
					Assignment emptyAssign = new Assignment();
					emptyAssign.setServiceInfo(new Microservice(PublicAttribute.serviceList[n]));
					emptyAssign.setVMNum(-1);
					emptyAssign.setVMPerIndex(-1);
					tempSearchNode.getPartialAssign().add(emptyAssign);
				}
				//计算当前局部分配加临时分配情况下的虚拟机中存放服务的情况
				Map<Integer,List<Assignment>> tempVmserviceMap = new HashMap<Integer,List<Assignment>>();
				for(int n=0;n<vmNumber;n++){
					List<Assignment> assignList = new ArrayList<Assignment>();
					tempVmserviceMap.put(n, assignList);
				}
				for(int n=0;n<tempSearchNode.getPartialAssign().size();n++){
					Assignment temp = tempSearchNode.getPartialAssign().get(n);
					int vnnum = temp.getVMNum();
					if(vnnum != -1){
						tempVmserviceMap.get(vnnum).add(temp);
					}
				}
				//计算当前局部分配加临时分配情况下的虚拟机间的通信链路情况
				int[][] currVmAmongLink = new int[vmNumber][vmNumber];
				getVmLinkNum(currVmAmongLink,tempSearchNode.getPartialAssign());
				//根据通信链路情况更新服务间的传输时间矩阵
				for(int m=0;m<serviceNum;m++){
					for(int n=0;n<serviceNum;n++){
						if(PublicAttribute.serviceData[m][n] > 0){
							int vmIndexM = tempSearchNode.getPartialAssign().get(m).getVMNum();
							int vmIndexN = tempSearchNode.getPartialAssign().get(n).getVMNum();
							if(vmIndexM != -1 && vmIndexN != -1 && currVmAmongLink[vmIndexM][vmIndexN] > 0){
								tempTrans[m][n] = PublicAttribute.serviceData[m][n] / (PublicAttribute.flexNetWidth / currVmAmongLink[vmIndexM][vmIndexN]);
							}else{
								tempTrans[m][n] = 0;
							}
						}else{
							tempTrans[m][n] = 0;
						}			
					}
				}
				//根据分配情况计算每个服务的实际计算时间，多个容器放在同一个虚拟机上时会影响运行时间
				for(int m=0;m<tempSearchNode.getPartialAssign().size();m++){
					Assignment temp = tempSearchNode.getPartialAssign().get(m);
					int vmnum = temp.getVMNum();
					if(vmnum != -1){
						temp.getServiceInfo().setRealRunTime(temp.getServiceInfo().getRuntime() * tempVmserviceMap.get(vmnum).size());
					}
				}
				solveCriticalPath(tempSearchNode , tempTrans);
				//System.out.println("s"+tempService.getIndex()+"--v"+possibleAssign.get(s).getVMNum()+"--p"+possibleAssign.get(s).getVMPerIndex()+"支持，当前时间："+tempSearchNode.getTimeFunctionVal()+",代价："+tempSearchNode.getPriceFunctionVal());
				//最小代价
				/*double currPrice = tempSearchNode.getPriceFunctionVal();
				if(currPrice < minServicePrice){
					minServicePrice = currPrice;
					minServicePriceIndex = s;
				}*/
				//最小时间
				double currTime = tempSearchNode.getTimeFunctionVal();
				if(currTime < minServiceTime){
					minServiceTime = currTime;
					minServiceTimeIndex = s;
				}
				
			}
			//System.out.println("所选择的分配：s"+tempService.getIndex()+"--v"+possibleAssign.get(minServiceTimeIndex).getVMNum()+"--p"+possibleAssign.get(minServiceTimeIndex).getVMPerIndex());
			//将最小代价对应的分配加入到searchNode的partialAssign中
			/*searchNode.getPartialAssign().add(possibleAssign.get(minServicePriceIndex));
			tempDepthAssign.add(possibleAssign.get(minServicePriceIndex));
			tempVm_perMap.put(possibleAssign.get(minServicePriceIndex).getVMNum(), possibleAssign.get(minServicePriceIndex).getVMPerIndex());
			tempVm_serviceMap.get(possibleAssign.get(minServicePriceIndex).getVMNum()).add(possibleAssign.get(minServicePriceIndex));					
			getVmLinkNum(tempVmLinkNum , tempDepthAssign);
			for(int m=0;m<serviceNum;m++){
				transTimeArr[m][tempService.getIndex()] = possibleAssign.get(minServicePriceIndex).getAllTrans()[m];
			}*/
			//最小时间
			searchNode.getPartialAssign().add(possibleAssign.get(minServiceTimeIndex));	
			tempDepthAssign.add(possibleAssign.get(minServiceTimeIndex));
			tempVm_perMap.put(possibleAssign.get(minServiceTimeIndex).getVMNum(), possibleAssign.get(minServiceTimeIndex).getVMPerIndex());
			tempVm_serviceMap.get(possibleAssign.get(minServiceTimeIndex).getVMNum()).add(possibleAssign.get(minServiceTimeIndex));					
			getVmLinkNum(tempVmLinkNum , tempDepthAssign);
			for(int m=0;m<serviceNum;m++){
				transTimeArr[m][tempService.getIndex()] = possibleAssign.get(minServiceTimeIndex).getAllTrans()[m];
			}
		}
		//System.out.println(searchNode.getPartialAssign().get(serviceNum-1).getCurrTime());
		//System.out.println(searchNode.getPartialAssign().get(serviceNum-1).getCurrPrice());
		//计算当前局部分配加临时分配情况下的虚拟机中存放服务的情况
		Map<Integer,List<Assignment>> currVmserviceMap = new HashMap<Integer,List<Assignment>>();
		for(int i=0;i<vmNumber;i++){
			List<Assignment> assignList = new ArrayList<Assignment>();
			currVmserviceMap.put(i, assignList);
		}
		for(int i=0;i<searchNode.getPartialAssign().size();i++){
			Assignment temp = searchNode.getPartialAssign().get(i);
			int vnnum = temp.getVMNum();
			currVmserviceMap.get(vnnum).add(temp);
		}
		//计算当前局部分配加临时分配情况下的虚拟机间的通信链路情况
		int[][] currVmAmongLink = new int[vmNumber][vmNumber];
		getVmLinkNum(currVmAmongLink,searchNode.getPartialAssign());
		//根据通信链路情况更新服务间的传输时间矩阵
		for(int m=0;m<serviceNum;m++){
			for(int n=0;n<serviceNum;n++){
				if(PublicAttribute.serviceData[m][n] > 0){
					int vmIndexM = searchNode.getPartialAssign().get(m).getVMNum();
					int vmIndexN = searchNode.getPartialAssign().get(n).getVMNum();
					if(currVmAmongLink[vmIndexM][vmIndexN] > 0){
						transTimeArr[m][n] = PublicAttribute.serviceData[m][n] / (PublicAttribute.flexNetWidth / currVmAmongLink[vmIndexM][vmIndexN]);
					}else{
						transTimeArr[m][n] = 0;
					}
				}else{
					transTimeArr[m][n] = 0;
				}			
			}
		}
		
		//根据分配情况计算每个服务的实际计算时间，多个容器放在同一个虚拟机上时会影响运行时间
		for(int i=0;i<searchNode.getPartialAssign().size();i++){
			Assignment temp = searchNode.getPartialAssign().get(i);
			int vmnum = temp.getVMNum();
			temp.getServiceInfo().setRealRunTime(temp.getServiceInfo().getRuntime() * currVmserviceMap.get(vmnum).size());
		}
		//调用DAG图的关键路径计算方法，计算当前分配下的DAG图的最长路径，此时算出来的才是真正的f(x)时间估计值
		solveCriticalPath(searchNode , transTimeArr);
	}
	
	/**
	 * 返回open列表中在代价约束内，f(x)值最小的
	 * */
	public int returnMinFx(){
		int index=0;
		double minFxTime = 0;
		int minFxIndex = -1;//标记open表中符合目标的索引值
		boolean flag = false;
		if(openList.size() == 0){
			return -1;
		}else{
			for(int i=0;i<openList.size();i++){
				if(openList.get(i).getPriceFunctionVal() <= limitPrice){
					minFxTime = openList.get(i).getTimeFunctionVal();
					index = i;
					minFxIndex = i;
					flag = true;//找到了在代价约束范围之内的
					break;
				}
			}
			if(flag == false){
				//如果没有在代价约束范围内的，则直接去找代价最小的
				minFxIndex = -1;
				double minFxPrice = Double.MAX_VALUE;
				for(int i=0;i<openList.size();i++){
					MicserviceStateInSearchTree temp = openList.get(i);
					if(temp.getPriceFunctionVal() < minFxPrice){
						minFxIndex = i;
						minFxPrice = temp.getPriceFunctionVal();
					}
				}
			}else{
				//继续去找在代价约束范围内，但是比之前的时间小的
				for(int j=index+1;j<openList.size();j++){
					if(openList.get(j).getPriceFunctionVal() <= limitPrice){
						if(openList.get(j).getTimeFunctionVal() < minFxTime){
							minFxTime = openList.get(j).getTimeFunctionVal();
							minFxIndex = j;
						}
					}
				}
			}
			return minFxIndex;
		}				
	}
	
	/**
	 * 计算带权重的DAG图的关键路径。这里的DAG图不是最开始给定的DAG图，而是在估算过程中的DAG图，
	 * 在计算未分配服务（进行临时分配）的通信代价时，只计算它们与已分配服务之间的通信，这样DAG图就会与原始的不一样
	 * */
	public void solveCriticalPath(MicserviceStateInSearchTree searchNode , double[][] transTime){
		DecimalFormat df = new DecimalFormat("#.00000000000000");//保留14位小数
		
		//先计算searchNode中每个微服务节点的最早开始时间
		for(int i=0;i<serviceNum;i++){
			Assignment tempVertex = searchNode.getPartialAssign().get(i);
			if(tempVertex.getVMNum() != -1){
				if(tempVertex.getServiceInfo().getParentServiceList().size() == 0){
					tempVertex.setEarliestTv(0);
				}else{
					double maxTime = 0;
					for(int j=0;j<tempVertex.getServiceInfo().getParentServiceList().size();j++){
						int parentIndex = tempVertex.getServiceInfo().getParentServiceList().get(j).getIndex();
						//最早开始时间etv = max{etv[k] + runtime(k) + transTime[k][j]}
						if(searchNode.getPartialAssign().get(parentIndex).getVMNum() != -1){
							double currTime = searchNode.getPartialAssign().get(parentIndex).getEarliestTv() + 
									searchNode.getPartialAssign().get(parentIndex).getServiceInfo().getRealRunTime() + transTime[parentIndex][i];
							currTime = Double.parseDouble(df.format(currTime));
							if(currTime > maxTime){
								maxTime = currTime;
							}
						}	
					}
					tempVertex.setEarliestTv(maxTime);
				}
			}
		}
		//计算searchNode中每个微服务节点的最晚开始时间
		for(int i=serviceNum-1;i>-1;i--){
			Assignment tempVertex = searchNode.getPartialAssign().get(i);
			if(tempVertex.getVMNum() != -1){
				boolean flag = true;
				//判断是否所有子节点均未分配
				for(int j=0;j<tempVertex.getServiceInfo().getChildServiceList().size();j++){
					int tempindex = tempVertex.getServiceInfo().getChildServiceList().get(j).getIndex();
					if(searchNode.getPartialAssign().get(tempindex).getVMNum() != -1){
						flag = false;
					}
				}			
				if(tempVertex.getServiceInfo().getChildServiceList().size() == 0 || flag == true){
					tempVertex.setLatestTv(tempVertex.getEarliestTv());
				}
				else{
					double minTime = Double.MAX_VALUE;
					for(int j=0;j<tempVertex.getServiceInfo().getChildServiceList().size();j++){
						int childIndex = tempVertex.getServiceInfo().getChildServiceList().get(j).getIndex();
						if(searchNode.getPartialAssign().get(childIndex).getVMNum() != -1){
							//最晚开始时间ltv = min{ltv[j] - transTime[k][j] - runtimt[k]}
							double currMinTime = searchNode.getPartialAssign().get(childIndex).getLatestTv() - transTime[i][childIndex] 
									- searchNode.getPartialAssign().get(i).getServiceInfo().getRealRunTime();
							currMinTime = Double.parseDouble(df.format(currMinTime));
							if(currMinTime < minTime){
								minTime = currMinTime;
							}
						}						
					}
					tempVertex.setLatestTv(minTime);
				}
			}	
		}
		//计算每条边的最早开始时间和最晚开始时间
		List<EdgeOfGraph> edgeList = new ArrayList<EdgeOfGraph>();//存放所有的边的list
		List<Assignment> assignList = new ArrayList<Assignment>();//用于遍历所有边的一个list
		
		for(int i=0;i<serviceNum;i++){
			Assignment tempVertex = searchNode.getPartialAssign().get(i);
			if(tempVertex.getVMNum() != -1){
				//如果没有父节点，说明是起始节点
				if(tempVertex.getServiceInfo().getParentServiceList().size() == 0){
					assignList.add(tempVertex);
				}
			}
		}
		
		while(assignList.size()>0){
			List<Assignment> temporaryList = new ArrayList<Assignment>();//一个空的临时的list，用于保存下一次要遍历的节点，赋给assignList
			int assignListSize = assignList.size();
			for(int i=0;i<assignListSize;i++){
				Assignment currParentVetex = assignList.get(i);//当前父节点
				int currParentIndex = currParentVetex.getServiceInfo().getIndex();
				//System.out.println("当前父节点编号："+currParentIndex);
				//System.out.println("子节点长度："+currParentVetex.getServiceInfo().getChildServiceList().size());
				for(int j=0;j<currParentVetex.getServiceInfo().getChildServiceList().size();j++){
					int currChildIndex = currParentVetex.getServiceInfo().getChildServiceList().get(j).getIndex();										
					if(searchNode.getPartialAssign().get(currChildIndex).getVMNum() != -1){
						//System.out.println("当前子节点编号："+currChildIndex);
						EdgeOfGraph currEdge = new EdgeOfGraph();
						currEdge.setStartVertex(currParentVetex);
						currEdge.setEndVertex(searchNode.getPartialAssign().get(currChildIndex));
						//边上的活动的最早开始时间：ete<k,j> = etv[k] + runtime[k]
						currEdge.setEarliestTe(Double.parseDouble(df.format(currParentVetex.getEarliestTv() + currParentVetex.getServiceInfo().getRealRunTime())));
						//边上活动的最晚开始时间：lte<k,j> = ltv[j] - transtime[k][j]
						currEdge.setLatestTe(Double.parseDouble(df.format(searchNode.getPartialAssign().get(currChildIndex).getLatestTv() - transTime[currParentIndex][currChildIndex])));
						currEdge.setWeight(transTime[currParentIndex][currChildIndex]);
						edgeList.add(currEdge);
						//将孩子节点放入assignList
						if(! temporaryList.contains(searchNode.getPartialAssign().get(currChildIndex))){
							temporaryList.add(searchNode.getPartialAssign().get(currChildIndex));
						}
					}
					
				}
			}
			assignList = temporaryList;
		}		
		
		//查找最早开始时间和最晚开始时间相同的边，组成的即为关键路径
		List<EdgeOfGraph> criticalPath = new ArrayList<EdgeOfGraph>();
		for(int i=0;i<edgeList.size();i++){
			EdgeOfGraph temp = edgeList.get(i);
			//直接判等会出现误差
			if( ((temp.getEarliestTe()-temp.getLatestTe()) > -0.00000001) && ((temp.getEarliestTe()-temp.getLatestTe()) < 0.00000001)){
				criticalPath.add(temp);
			}
		}
		//根据关键路径求运行的总时间，即f(x)
		double allEvaTime = 0;
		for(int i=0;i<criticalPath.size();i++){
			boolean flag = true;
			//判断是否所有子节点均未分配
			for(int j=0;j<criticalPath.get(i).getEndVertex().getServiceInfo().getChildServiceList().size();j++){
				int tempindex = criticalPath.get(i).getEndVertex().getServiceInfo().getChildServiceList().get(j).getIndex();
				if(searchNode.getPartialAssign().get(tempindex).getVMNum() != -1){
					flag = false;
				}
			}
			//找到连接终点服务的边
			if(criticalPath.get(i).getEndVertex().getServiceInfo().getChildServiceList().size() == 0 || flag == true){
				int endIndex = criticalPath.get(i).getEndVertex().getServiceInfo().getIndex();
				int startIndex = criticalPath.get(i).getStartVertex().getServiceInfo().getIndex();
				//运行总时间 = 最后一条边的最早开始时间 + 传输时间 + 终点服务的运行时间
				double tempEvaTime = Double.parseDouble(df.format(criticalPath.get(i).getEarliestTe() + transTime[startIndex][endIndex] + searchNode.getPartialAssign().get(endIndex).getServiceInfo().getRealRunTime()));
				if(tempEvaTime > allEvaTime){
					allEvaTime = tempEvaTime; 
				}
				searchNode.setTimeFunctionVal(allEvaTime);
			}
		}
		//计算在当前分配状态下所花费的总代价 = 总时间 * 总的租用单价
		List<Integer> vmArr = new ArrayList<Integer>();
		double allPer = 0;
		for(int i=0;i<serviceNum;i++){
			Assignment tempVertex = searchNode.getPartialAssign().get(i);
			if(tempVertex.getVMNum() != -1 && !vmArr.contains(tempVertex.getVMNum())){
				vmArr.add(tempVertex.getVMNum());
				allPer += PublicAttribute.performanceList[tempVertex.getVMPerIndex()].getPrices();
			}
		}
		double allEvaPrices = Double.parseDouble(df.format(allPer * searchNode.getTimeFunctionVal()));
		searchNode.setPriceFunctionVal(allEvaPrices);
		
	}
	/**
	 * vm_perMap 之间的拷贝
	 * */
	public void copyIntegerMap(Map sourceMap , Map targetMap){
		Iterator entries = sourceMap.entrySet().iterator(); 
		while (entries.hasNext()) { 
			 Map.Entry entry = (Map.Entry) entries.next();
			 targetMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * currDepthAssign<Integer,Assignment> map之间的拷贝clone
	 * */
	public void cloneCurrDepthAssign(List<Assignment> sourceList , List<Assignment> targetList){
		for(int i=0;i<sourceList.size();i++){
			Assignment assi = new Assignment(sourceList.get(i));  
			targetList.add(assi);
		}
	}
	/**
	 * currDepthAssign<Integer,Assignment> map之间的拷贝clone
	 * */
	public void cloneVmServiceMap(Map sourceMap , Map targetMap){
		Iterator entries = sourceMap.entrySet().iterator(); 
		while (entries.hasNext()) { 
			Map.Entry entry = (Map.Entry) entries.next();
			Integer key = (Integer)entry.getKey();  //key表示服务
			List<Assignment> list = new ArrayList<Assignment>();
			for(int i=0;i<((List<Assignment>)entry.getValue()).size();i++){
				Assignment temp = ((List<Assignment>)entry.getValue()).get(i);
				Assignment assign = new Assignment(temp);
				list.add(assign);
			}
		   targetMap.put(key, list);
		}
	}
	//输出vm_perMap的内容
	public void printfvm_perMap(){
		//System.out.println("输出vm_perMap中的内容：");
		Iterator entries2 = vm_perMap.entrySet().iterator(); 
		while (entries2.hasNext()){
			 Map.Entry entry = (Map.Entry) entries2.next();  					  
			 Integer key = (Integer)entry.getKey();  //key表示服务	
			 Integer value = (Integer)entry.getValue();
			// System.out.println("虚拟机编号："+key+", 性能编号："+value);
		}
	}
	//输出vm_serviceMap内容
	public void printfvm_serviceMap(){
		//System.out.println("输出vm_serviceMap中的内容：");
		Iterator entries2 = vm_serviceMap.entrySet().iterator(); 
		while (entries2.hasNext()){
			 Map.Entry entry = (Map.Entry) entries2.next();  					  
			 Integer key = (Integer)entry.getKey();  //key表示服务	
			 List<Assignment> value = (List)entry.getValue();
			// System.out.println("虚拟机编号："+key+", 服务个数："+value.size());
		}
	}
	//测试输出allDepthAssign中的内容
	public void printfAllDepthAssign(){
		System.out.println("输出currDepthAssign中的内容：");
		for(int i=0;i<allDepthAssign.size();i++){
			Assignment temp = allDepthAssign.get(i);  
			System.out.println("服务"+i+"--虚拟机"+temp.getVMNum()+"--性能"+temp.getVMPerIndex()+"运行时间："+temp.getServiceInfo().getRealRunTime());
			//System.out.println("服务"+i+"--虚拟机"+temp.getVMNum()+"--性能"+temp.getVMPerIndex());
		}
	}
	/**
	 * 统计当前已经分配好的虚拟机之间的通信链路数
	 * */
	public void getVmLinkNum(int[][] vmAmongLinkNum , List<Assignment> particalAssign){
		for(int m=0;m<particalAssign.size();m++){
			Assignment assign1 = particalAssign.get(m);
			Microservice service1 = assign1.getServiceInfo();
			int serviceIndex1 = service1.getIndex();
			int vmnum1 = assign1.getVMNum();
			if(vmnum1 != -1){
				for(int n=0;n<particalAssign.size();n++){
					Assignment assign2 = particalAssign.get(n);
					Microservice service2 = assign2.getServiceInfo();
					int serviceIndex2 = service2.getIndex();
					int vmnum2 = assign2.getVMNum();
					if(vmnum2 != -1){
						if(m != n){
							//表明服务之间有通信
							if(PublicAttribute.serviceData[serviceIndex1][serviceIndex2] > 0){
								if(vmnum1 != vmnum2){//虚拟机编号不同
									vmAmongLinkNum[vmnum1][vmnum2]++;//两虚拟机之间的通信链路数加1
									vmAmongLinkNum[vmnum2][vmnum1]++;
								}
							}
						}
					}
				}
			}
			
		}
	}
	
	public String greedyAStarRun(String confPath,String inputPath,int budgetIndex,double currBudget){
		System.out.println("GreedyAStar4开始运行");
		long startTime = System.currentTimeMillis();
		//读取配置文件，初始化
		try {
			new Init().init(confPath,inputPath);
		} catch (DocumentException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		String resultStr = new GreedyAStar4(currBudget).solution();
		long endTime = System.currentTimeMillis();	
		long runTime = endTime - startTime;
		resultStr += "@"+runTime+"@"+budgetIndex;
		
		return resultStr;
	}
	
	public static void main(String[] args) throws DocumentException, IOException{
		System.out.println("GreedyAStar开始运行");
		long startTime = System.currentTimeMillis();
		//读取配置文件，初始化
		String confPath = "D:/myeclipseWorkspace/AStarVersion4/config/15_1/1/conf.xml";
		String inputPath = "D:/myeclipseWorkspace/AStarVersion4/config/15_1/1/input.txt";
		double budget = 80.515038096;
		new Init().init(confPath,inputPath);			
		new GreedyAStar4(budget).solution();
		long endTime = System.currentTimeMillis();			
		System.out.println("计算时间："+(endTime - startTime)+"ms");				
		
	}
}
