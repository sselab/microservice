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
 * 2017.1.16 ��45-60�ڵ�ģ�ֻ����һ�Σ�budget�ֿ�
 * */
public class GreedyAStar4 {
	//��������ȣ���΢�������
	int depth = PublicAttribute.microserviceNum;	
	
	//����������ά��һ��open�б���Ŵ���չ�Ľڵ�
	List<MicserviceStateInSearchTree> openList = new ArrayList<MicserviceStateInSearchTree>();
	
	//����������ά��һ��closed�б������չ��ɵĽڵ�
	//List<MicserviceStateInSearchTree> closedList = new ArrayList<MicserviceStateInSearchTree>();
	MicserviceStateInSearchTree closedSearchNode = new MicserviceStateInSearchTree();
	
	//��ǰ�Ѿ����˵ķ���״̬
	List<Assignment> allDepthAssign = new ArrayList<Assignment>();//���ÿһ��ķ��䣬f(x)��С��
	
	//���������������ܵĶ�Ӧ��ϵ,��������-���ܱ��
	Map<Integer,Integer> vm_perMap = new HashMap<Integer,Integer>();
	
	//����������������б���������-�����б�
	Map<Integer,List<Assignment>> vm_serviceMap = new HashMap<Integer,List<Assignment>>();	
	
	//��ǰ������ȣ���ʼ��Ϊ0
	int currDepth = 0;		
	//΢�������
	int serviceNum = PublicAttribute.microserviceNum;
	//���������
	int vmNumber = PublicAttribute.nodeNum;
	//�����������
	int vmPerNumber = PublicAttribute.performanceNum;
	//����Լ��
	double limitPrice = 0;
	//ͳ�������֮���ͨ����·��
	int[][] vmAmongLinkNum = new int[vmNumber][vmNumber];
	
	//һ���ַ�������ʾ��ʵ����ʱ�仨�ѡ��ɱ����ѡ�����ʱ�䣬��@���Ÿ���
	String resultStr = new String();
	
	public GreedyAStar4(double budget){
		this.limitPrice = budget;
	}
	/**
	 * �㷨���
	 * */
	public String solution(){
		//��ʼ��vm_serviceMap����ÿ���������һ��ʼ���õķ����Ϊ��
		for(int i=0;i<vmNumber;i++){
			List<Assignment> assignList = new ArrayList<Assignment>();
			vm_serviceMap.put(i, assignList);
		}
		//��ʼ��vm_perMap����ÿ�������������һ��ʼ����-1����û�����κ�ѡ��
		for(int i=0;i<vmNumber;i++){
			vm_perMap.put(i, -1);
		}
		
		while(currDepth < depth){
			//System.out.println("��ǰ��ȣ�"+currDepth);
			int minFxIndex = returnMinFx();//�ҵ�open�б���f(x)��С�Ľڵ�	
			//System.out.println("��ǰopen����f(x)ֵ��С�Ľڵ�������"+minFxIndex);
			MicserviceStateInSearchTree minFxNode = new MicserviceStateInSearchTree();
			if(minFxIndex == -1){//��open��Ϊ��
				minFxNode = null;
			}else{
				minFxNode = openList.get(minFxIndex);
				closedSearchNode = minFxNode;
				//closedList.add(minFxNode);//����closed�б�
				openList.clear();//���open���´�ֻ������չ�Ľڵ��н���ѡ�񣬼�̰��
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
				//����ڵ㵽open�б��У�����f(x).minFxNode��Ϊ����ڵ�ĸ��ڵ� 
				insertNodeToOpen(minFxNode);
			}			
		}
		//allDepthAssign�еļ�Ϊ���շ�����
		System.out.println("���շ��������£�");
		printfAllDepthAssign();		
		System.out.println("��ʱ�䣺"+closedSearchNode.getTimeFunctionVal());
		System.out.println("�ܴ��ۣ�"+closedSearchNode.getPriceFunctionVal());
		return closedSearchNode.getTimeFunctionVal()+"@"+closedSearchNode.getPriceFunctionVal();
	}
	
	/**
	 * �����������ڵ㣬����open��
	 * */
	public void insertNodeToOpen(MicserviceStateInSearchTree parentNode){
		//System.out.println("�����½ڵ�");		
		//��Ϊ����ѡ�������
		for(int i=0;i<vmNumber;i++){
			//����������������Ѿ����˷���
			if(vm_perMap.get(i) != -1){	
				Microservice currService = new Microservice(PublicAttribute.serviceList[currDepth]);
				//�洢�����Ĵ���ʱ�䣬��ʼ��Ϊ0
				double[][] transTimeArr = new double[serviceNum][serviceNum]; 
				for(int m=0;m<serviceNum;m++){
					for(int n=0;n<serviceNum;n++){
						transTimeArr[m][n] = 0;
					}
				}				
				//������ʱmap
				List<Assignment> tempDepthAssign = new ArrayList<Assignment>();//����֮ǰ�ķ���
				Map<Integer,Integer> tempVm_perMap = new HashMap<Integer,Integer>();
				Map<Integer,List<Assignment>> tempVm_serviceMap = new HashMap<Integer,List<Assignment>>();
				int[][] tempVmLinkNum = vmAmongLinkNum;
				cloneCurrDepthAssign(allDepthAssign , tempDepthAssign);//��currDepthAssign������tempDepthAssign
				copyIntegerMap(vm_perMap , tempVm_perMap);
				cloneVmServiceMap(vm_serviceMap , tempVm_serviceMap);				
				
				VMPerformance currPer = PublicAttribute.performanceList[vm_perMap.get(i)];
				//������������ϵķ��񣬼���������֮��
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
					//����֮ǰ���������£���ѡ������ϵķ�����������ʵ������ʱ��
					currService.setRealRunTime(currService.getRuntime() * (vm_serviceMap.get(i).size() + 1));
					Assignment assignNode = new Assignment();
					assignNode.setServiceInfo(currService);
					assignNode.setVMNum(i);
					assignNode.setVMPerIndex(vm_perMap.get(i));
					MicserviceStateInSearchTree searchNode = new MicserviceStateInSearchTree();
					searchNode.setCurrDepth(currDepth);
					//����ͨ��ʱ�䣬���Ѿ�����ķ���ͨ�ŵ��ʱ��
					double transTime = 0;
					for(int m=0;m<allDepthAssign.size();m++){
						 Assignment tempAssignment = allDepthAssign.get(m);
						 searchNode.getPartialAssign().add(tempAssignment);//�Ȱ���ǰ�ķ���Ž�ȥ
						 if(PublicAttribute.serviceData[m][currService.getIndex()] != 0){
							 //��ʾ��ͨ��
							 if(tempAssignment.getVMNum() == i){//ͬһ������
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
					//���������ĸ�ֵ
					tempVm_perMap.put(i, vm_perMap.get(i));
					tempVm_serviceMap.get(i).add(assignNode);					
					tempDepthAssign.add(assignNode);
					getVmLinkNum(tempVmLinkNum , tempDepthAssign);
					for(int m=0;m<serviceNum;m++){
						transTimeArr[m][currService.getIndex()] = assignNode.getAllTrans()[m];
					}
					calculateFx(searchNode , transTimeArr , tempVm_perMap , tempVm_serviceMap , tempDepthAssign , tempVmLinkNum);	
					//System.out.println("�ڵ���䣺s"+currService.getIndex()+"--v"+i+"--p"+vm_perMap.get(i)+" ����ʱ�䣺"+searchNode.getTimeFunctionVal()+" ���ƴ��ۣ�"+searchNode.getPriceFunctionVal());
					openList.add(searchNode);//���뵽open�б���
					//System.out.println("open���С��"+openList.size());
					
				}
			}else{
				//�������δ�������ܣ�����vmPerNumber�ֿ�ѡ
				for(int j=0;j<vmPerNumber;j++){
					Microservice currService = new Microservice(PublicAttribute.serviceList[currDepth]);
					//�洢�����Ĵ���ʱ�䣬��ʼ��Ϊ0
					double[][] transTimeArr = new double[serviceNum][serviceNum]; 
					for(int m=0;m<serviceNum;m++){
						for(int n=0;n<serviceNum;n++){
							transTimeArr[m][n] = 0;
						}
					}
					//������ʱmap
					List<Assignment> tempDepthAssign = new ArrayList<Assignment>();//����֮ǰ�ķ���
					Map<Integer,Integer> tempVm_perMap = new HashMap<Integer,Integer>();
					Map<Integer,List<Assignment>> tempVm_serviceMap = new HashMap<Integer,List<Assignment>>();
					int[][] tempVmLinkNum = vmAmongLinkNum;
					cloneCurrDepthAssign(allDepthAssign , tempDepthAssign);//��currDepthAssign������tempDepthAssign
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
						//����ͨ��ʱ�䣬���Ѿ�����ķ���ͨ�ŵ��ʱ��
						double transTime = 0;
						for(int m=0;m<allDepthAssign.size();m++){
							 Assignment tempAssignment = allDepthAssign.get(m);
							 searchNode.getPartialAssign().add(tempAssignment);//�Ȱ���ǰ�ķ���Ž�ȥ
							 if(PublicAttribute.serviceData[m][currService.getIndex()] != 0){
								 //��ʾ��ͨ��								
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
						//���������ĸ�ֵ
						tempVm_perMap.put(i, j);
						tempVm_serviceMap.get(i).add(assignNode);					
						tempDepthAssign.add(assignNode);
						getVmLinkNum(tempVmLinkNum , tempDepthAssign);
						for(int m=0;m<serviceNum;m++){
							transTimeArr[m][currService.getIndex()] = assignNode.getAllTrans()[m];
						}
						/*System.out.println("����䴫��ʱ�����");
						for(int m=0;m<serviceNum;m++){
							for(int n=0;n<serviceNum;n++){
								System.out.print(transTimeArr[m][n]+" ");
							}
							System.out.println();
						}*/	
						calculateFx(searchNode , transTimeArr , tempVm_perMap , tempVm_serviceMap , tempDepthAssign , tempVmLinkNum);	
						//System.out.println("�ڵ���䣺s"+currService.getIndex()+"--v"+i+"--p"+j+" ����ʱ�䣺"+searchNode.getTimeFunctionVal()+" ���ƴ��ۣ�"+searchNode.getPriceFunctionVal());
						openList.add(searchNode);//���뵽open�б���
						//System.out.println("open���С��"+openList.size());
						
					}
				}
			}
		
		}
	}
	/**
	 * ����ÿ�������ڵ��f(x)ʱ�����ֵ
	 * */
	public void calculateFx(MicserviceStateInSearchTree searchNode , double[][] transTimeArr , Map<Integer,Integer>  tempVm_perMap , Map<Integer,List<Assignment>> tempVm_serviceMap , List<Assignment> tempDepthAssign ,int[][] tempVmLinkNum){
		//����΢����list��δ����ķ���Ϊ�������ʱ���䣬����f(x)
		for(int i=currDepth+1;i<PublicAttribute.serviceList.length;i++){
			//��Ÿ÷�����ܵ����з��������ÿ�������Ӧһ��ʱ��ɱ�
			List<Assignment> possibleAssign = new ArrayList<Assignment>();
			Microservice tempService = PublicAttribute.serviceList[i];	
			//System.out.println("*************************************��ǰ�����ţ�s"+i);
			for(int j=0;j<vmNumber;j++){				
				if(tempVm_perMap.get(j) != -1){
					Microservice service = new Microservice(tempService);	
					VMPerformance currPer = PublicAttribute.performanceList[tempVm_perMap.get(j)];
					//������������ϵķ��񣬼���������֮��
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
						//����ͨ��ʱ�䣬���Ѿ�����ķ���ͨ�ŵ��ʱ��
						double transTime = 0;
						for(int m=0;m<tempDepthAssign.size();m++){
							 Assignment tempAssignment = tempDepthAssign.get(m);
							 if(PublicAttribute.serviceData[m][service.getIndex()] != 0){
								 //��ʾ��ͨ��
								 if(tempAssignment.getVMNum() == j){//ͬһ������
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
						//System.out.println("��  s"+tempService.getIndex()+"--v"+j+"--p"+tempVm_perMap.get(j)+"������ʱ�䣺"+tempService.getRuntime()+"�������ʱ�䣺"+transTime);
						possibleAssign.add(assignState1);
					}
				}else{
					//System.out.println("δ��������");
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
							//����ͨ��ʱ�䣬���Ѿ�����ķ���ͨ�ŵ��ʱ��
							double transTime = 0;
							for(int m=0;m<tempDepthAssign.size();m++){
								 Assignment tempAssignment = tempDepthAssign.get(m);
								 if(PublicAttribute.serviceData[m][service.getIndex()] != 0){
									 //��ʾ��ͨ��
								     assignState2.getAllTrans()[m] = PublicAttribute.serviceData[m][service.getIndex()] / (PublicAttribute.flexNetWidth / (tempVmLinkNum[tempAssignment.getVMNum()][j] + 1)); 
									 if( assignState2.getAllTrans()[m] > transTime){
								    		transTime =  assignState2.getAllTrans()[m];
								     }
								 }
							}								 
							assignState2.setTransTime(transTime);
							possibleAssign.add(assignState2);
							//System.out.println("��  s"+tempService.getIndex()+"--v"+j+"--p"+k+"֧�֣�����ʱ�䣺"+tempService.getRuntime()+",�����ʱ�䣺"+transTime);
							break;
						}
					}
					break;
				}
			}
			
			//����ÿ������Ŀ��ܷ�����������д�����С�ķ��䣬
			//double minServicePrice = Double.MAX_VALUE;
			//int minServicePriceIndex = 0;//�����С���۶�Ӧ����ı��
			//����ÿ������Ŀ��ܷ�������������ҳ�������ʱ��+����ʱ�䣩��С�ķ��䣬
			double minServiceTime = Double.MAX_VALUE;
			int minServiceTimeIndex = 0;//�����Сʱ���Ӧ����ı��
			
			//Ϊÿ��������㵱ǰ�Ѿ����ѵ�ʱ��ʹ��ۣ����н���ѡ��2017.1.14�¸�
			for(int s=0;s<possibleAssign.size();s++){
				MicserviceStateInSearchTree tempSearchNode = new MicserviceStateInSearchTree();
				double[][] tempTrans = transTimeArr;
				//�Ȱ�֮ǰ���������з���ӽ�ȥ
				for(int n=0;n<searchNode.getPartialAssign().size();n++){
					tempSearchNode.getPartialAssign().add(new Assignment(searchNode.getPartialAssign().get(n)));
				}
				//�ѵ�ǰ��ʱ����ӽ�ȥ
				tempSearchNode.getPartialAssign().add(new Assignment(possibleAssign.get(s)));
				//��ʣ��δ������������ʱ��ʹ���ʱ����Ϊ0
				for(int n=tempSearchNode.getPartialAssign().size();n<serviceNum;n++){
					Assignment emptyAssign = new Assignment();
					emptyAssign.setServiceInfo(new Microservice(PublicAttribute.serviceList[n]));
					emptyAssign.setVMNum(-1);
					emptyAssign.setVMPerIndex(-1);
					tempSearchNode.getPartialAssign().add(emptyAssign);
				}
				//���㵱ǰ�ֲ��������ʱ��������µ�������д�ŷ�������
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
				//���㵱ǰ�ֲ��������ʱ��������µ���������ͨ����·���
				int[][] currVmAmongLink = new int[vmNumber][vmNumber];
				getVmLinkNum(currVmAmongLink,tempSearchNode.getPartialAssign());
				//����ͨ����·������·����Ĵ���ʱ�����
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
				//���ݷ����������ÿ�������ʵ�ʼ���ʱ�䣬�����������ͬһ���������ʱ��Ӱ������ʱ��
				for(int m=0;m<tempSearchNode.getPartialAssign().size();m++){
					Assignment temp = tempSearchNode.getPartialAssign().get(m);
					int vmnum = temp.getVMNum();
					if(vmnum != -1){
						temp.getServiceInfo().setRealRunTime(temp.getServiceInfo().getRuntime() * tempVmserviceMap.get(vmnum).size());
					}
				}
				solveCriticalPath(tempSearchNode , tempTrans);
				//System.out.println("s"+tempService.getIndex()+"--v"+possibleAssign.get(s).getVMNum()+"--p"+possibleAssign.get(s).getVMPerIndex()+"֧�֣���ǰʱ�䣺"+tempSearchNode.getTimeFunctionVal()+",���ۣ�"+tempSearchNode.getPriceFunctionVal());
				//��С����
				/*double currPrice = tempSearchNode.getPriceFunctionVal();
				if(currPrice < minServicePrice){
					minServicePrice = currPrice;
					minServicePriceIndex = s;
				}*/
				//��Сʱ��
				double currTime = tempSearchNode.getTimeFunctionVal();
				if(currTime < minServiceTime){
					minServiceTime = currTime;
					minServiceTimeIndex = s;
				}
				
			}
			//System.out.println("��ѡ��ķ��䣺s"+tempService.getIndex()+"--v"+possibleAssign.get(minServiceTimeIndex).getVMNum()+"--p"+possibleAssign.get(minServiceTimeIndex).getVMPerIndex());
			//����С���۶�Ӧ�ķ�����뵽searchNode��partialAssign��
			/*searchNode.getPartialAssign().add(possibleAssign.get(minServicePriceIndex));
			tempDepthAssign.add(possibleAssign.get(minServicePriceIndex));
			tempVm_perMap.put(possibleAssign.get(minServicePriceIndex).getVMNum(), possibleAssign.get(minServicePriceIndex).getVMPerIndex());
			tempVm_serviceMap.get(possibleAssign.get(minServicePriceIndex).getVMNum()).add(possibleAssign.get(minServicePriceIndex));					
			getVmLinkNum(tempVmLinkNum , tempDepthAssign);
			for(int m=0;m<serviceNum;m++){
				transTimeArr[m][tempService.getIndex()] = possibleAssign.get(minServicePriceIndex).getAllTrans()[m];
			}*/
			//��Сʱ��
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
		//���㵱ǰ�ֲ��������ʱ��������µ�������д�ŷ�������
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
		//���㵱ǰ�ֲ��������ʱ��������µ���������ͨ����·���
		int[][] currVmAmongLink = new int[vmNumber][vmNumber];
		getVmLinkNum(currVmAmongLink,searchNode.getPartialAssign());
		//����ͨ����·������·����Ĵ���ʱ�����
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
		
		//���ݷ����������ÿ�������ʵ�ʼ���ʱ�䣬�����������ͬһ���������ʱ��Ӱ������ʱ��
		for(int i=0;i<searchNode.getPartialAssign().size();i++){
			Assignment temp = searchNode.getPartialAssign().get(i);
			int vmnum = temp.getVMNum();
			temp.getServiceInfo().setRealRunTime(temp.getServiceInfo().getRuntime() * currVmserviceMap.get(vmnum).size());
		}
		//����DAGͼ�Ĺؼ�·�����㷽�������㵱ǰ�����µ�DAGͼ���·������ʱ������Ĳ���������f(x)ʱ�����ֵ
		solveCriticalPath(searchNode , transTimeArr);
	}
	
	/**
	 * ����open�б����ڴ���Լ���ڣ�f(x)ֵ��С��
	 * */
	public int returnMinFx(){
		int index=0;
		double minFxTime = 0;
		int minFxIndex = -1;//���open���з���Ŀ�������ֵ
		boolean flag = false;
		if(openList.size() == 0){
			return -1;
		}else{
			for(int i=0;i<openList.size();i++){
				if(openList.get(i).getPriceFunctionVal() <= limitPrice){
					minFxTime = openList.get(i).getTimeFunctionVal();
					index = i;
					minFxIndex = i;
					flag = true;//�ҵ����ڴ���Լ����Χ֮�ڵ�
					break;
				}
			}
			if(flag == false){
				//���û���ڴ���Լ����Χ�ڵģ���ֱ��ȥ�Ҵ�����С��
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
				//����ȥ���ڴ���Լ����Χ�ڣ����Ǳ�֮ǰ��ʱ��С��
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
	 * �����Ȩ�ص�DAGͼ�Ĺؼ�·���������DAGͼ�����ʼ������DAGͼ�������ڹ�������е�DAGͼ��
	 * �ڼ���δ������񣨽�����ʱ���䣩��ͨ�Ŵ���ʱ��ֻ�����������ѷ������֮���ͨ�ţ�����DAGͼ�ͻ���ԭʼ�Ĳ�һ��
	 * */
	public void solveCriticalPath(MicserviceStateInSearchTree searchNode , double[][] transTime){
		DecimalFormat df = new DecimalFormat("#.00000000000000");//����14λС��
		
		//�ȼ���searchNode��ÿ��΢����ڵ�����翪ʼʱ��
		for(int i=0;i<serviceNum;i++){
			Assignment tempVertex = searchNode.getPartialAssign().get(i);
			if(tempVertex.getVMNum() != -1){
				if(tempVertex.getServiceInfo().getParentServiceList().size() == 0){
					tempVertex.setEarliestTv(0);
				}else{
					double maxTime = 0;
					for(int j=0;j<tempVertex.getServiceInfo().getParentServiceList().size();j++){
						int parentIndex = tempVertex.getServiceInfo().getParentServiceList().get(j).getIndex();
						//���翪ʼʱ��etv = max{etv[k] + runtime(k) + transTime[k][j]}
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
		//����searchNode��ÿ��΢����ڵ������ʼʱ��
		for(int i=serviceNum-1;i>-1;i--){
			Assignment tempVertex = searchNode.getPartialAssign().get(i);
			if(tempVertex.getVMNum() != -1){
				boolean flag = true;
				//�ж��Ƿ������ӽڵ��δ����
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
							//����ʼʱ��ltv = min{ltv[j] - transTime[k][j] - runtimt[k]}
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
		//����ÿ���ߵ����翪ʼʱ�������ʼʱ��
		List<EdgeOfGraph> edgeList = new ArrayList<EdgeOfGraph>();//������еıߵ�list
		List<Assignment> assignList = new ArrayList<Assignment>();//���ڱ������бߵ�һ��list
		
		for(int i=0;i<serviceNum;i++){
			Assignment tempVertex = searchNode.getPartialAssign().get(i);
			if(tempVertex.getVMNum() != -1){
				//���û�и��ڵ㣬˵������ʼ�ڵ�
				if(tempVertex.getServiceInfo().getParentServiceList().size() == 0){
					assignList.add(tempVertex);
				}
			}
		}
		
		while(assignList.size()>0){
			List<Assignment> temporaryList = new ArrayList<Assignment>();//һ���յ���ʱ��list�����ڱ�����һ��Ҫ�����Ľڵ㣬����assignList
			int assignListSize = assignList.size();
			for(int i=0;i<assignListSize;i++){
				Assignment currParentVetex = assignList.get(i);//��ǰ���ڵ�
				int currParentIndex = currParentVetex.getServiceInfo().getIndex();
				//System.out.println("��ǰ���ڵ��ţ�"+currParentIndex);
				//System.out.println("�ӽڵ㳤�ȣ�"+currParentVetex.getServiceInfo().getChildServiceList().size());
				for(int j=0;j<currParentVetex.getServiceInfo().getChildServiceList().size();j++){
					int currChildIndex = currParentVetex.getServiceInfo().getChildServiceList().get(j).getIndex();										
					if(searchNode.getPartialAssign().get(currChildIndex).getVMNum() != -1){
						//System.out.println("��ǰ�ӽڵ��ţ�"+currChildIndex);
						EdgeOfGraph currEdge = new EdgeOfGraph();
						currEdge.setStartVertex(currParentVetex);
						currEdge.setEndVertex(searchNode.getPartialAssign().get(currChildIndex));
						//���ϵĻ�����翪ʼʱ�䣺ete<k,j> = etv[k] + runtime[k]
						currEdge.setEarliestTe(Double.parseDouble(df.format(currParentVetex.getEarliestTv() + currParentVetex.getServiceInfo().getRealRunTime())));
						//���ϻ������ʼʱ�䣺lte<k,j> = ltv[j] - transtime[k][j]
						currEdge.setLatestTe(Double.parseDouble(df.format(searchNode.getPartialAssign().get(currChildIndex).getLatestTv() - transTime[currParentIndex][currChildIndex])));
						currEdge.setWeight(transTime[currParentIndex][currChildIndex]);
						edgeList.add(currEdge);
						//�����ӽڵ����assignList
						if(! temporaryList.contains(searchNode.getPartialAssign().get(currChildIndex))){
							temporaryList.add(searchNode.getPartialAssign().get(currChildIndex));
						}
					}
					
				}
			}
			assignList = temporaryList;
		}		
		
		//�������翪ʼʱ�������ʼʱ����ͬ�ıߣ���ɵļ�Ϊ�ؼ�·��
		List<EdgeOfGraph> criticalPath = new ArrayList<EdgeOfGraph>();
		for(int i=0;i<edgeList.size();i++){
			EdgeOfGraph temp = edgeList.get(i);
			//ֱ���еȻ�������
			if( ((temp.getEarliestTe()-temp.getLatestTe()) > -0.00000001) && ((temp.getEarliestTe()-temp.getLatestTe()) < 0.00000001)){
				criticalPath.add(temp);
			}
		}
		//���ݹؼ�·�������е���ʱ�䣬��f(x)
		double allEvaTime = 0;
		for(int i=0;i<criticalPath.size();i++){
			boolean flag = true;
			//�ж��Ƿ������ӽڵ��δ����
			for(int j=0;j<criticalPath.get(i).getEndVertex().getServiceInfo().getChildServiceList().size();j++){
				int tempindex = criticalPath.get(i).getEndVertex().getServiceInfo().getChildServiceList().get(j).getIndex();
				if(searchNode.getPartialAssign().get(tempindex).getVMNum() != -1){
					flag = false;
				}
			}
			//�ҵ������յ����ı�
			if(criticalPath.get(i).getEndVertex().getServiceInfo().getChildServiceList().size() == 0 || flag == true){
				int endIndex = criticalPath.get(i).getEndVertex().getServiceInfo().getIndex();
				int startIndex = criticalPath.get(i).getStartVertex().getServiceInfo().getIndex();
				//������ʱ�� = ���һ���ߵ����翪ʼʱ�� + ����ʱ�� + �յ���������ʱ��
				double tempEvaTime = Double.parseDouble(df.format(criticalPath.get(i).getEarliestTe() + transTime[startIndex][endIndex] + searchNode.getPartialAssign().get(endIndex).getServiceInfo().getRealRunTime()));
				if(tempEvaTime > allEvaTime){
					allEvaTime = tempEvaTime; 
				}
				searchNode.setTimeFunctionVal(allEvaTime);
			}
		}
		//�����ڵ�ǰ����״̬�������ѵ��ܴ��� = ��ʱ�� * �ܵ����õ���
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
	 * vm_perMap ֮��Ŀ���
	 * */
	public void copyIntegerMap(Map sourceMap , Map targetMap){
		Iterator entries = sourceMap.entrySet().iterator(); 
		while (entries.hasNext()) { 
			 Map.Entry entry = (Map.Entry) entries.next();
			 targetMap.put(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * currDepthAssign<Integer,Assignment> map֮��Ŀ���clone
	 * */
	public void cloneCurrDepthAssign(List<Assignment> sourceList , List<Assignment> targetList){
		for(int i=0;i<sourceList.size();i++){
			Assignment assi = new Assignment(sourceList.get(i));  
			targetList.add(assi);
		}
	}
	/**
	 * currDepthAssign<Integer,Assignment> map֮��Ŀ���clone
	 * */
	public void cloneVmServiceMap(Map sourceMap , Map targetMap){
		Iterator entries = sourceMap.entrySet().iterator(); 
		while (entries.hasNext()) { 
			Map.Entry entry = (Map.Entry) entries.next();
			Integer key = (Integer)entry.getKey();  //key��ʾ����
			List<Assignment> list = new ArrayList<Assignment>();
			for(int i=0;i<((List<Assignment>)entry.getValue()).size();i++){
				Assignment temp = ((List<Assignment>)entry.getValue()).get(i);
				Assignment assign = new Assignment(temp);
				list.add(assign);
			}
		   targetMap.put(key, list);
		}
	}
	//���vm_perMap������
	public void printfvm_perMap(){
		//System.out.println("���vm_perMap�е����ݣ�");
		Iterator entries2 = vm_perMap.entrySet().iterator(); 
		while (entries2.hasNext()){
			 Map.Entry entry = (Map.Entry) entries2.next();  					  
			 Integer key = (Integer)entry.getKey();  //key��ʾ����	
			 Integer value = (Integer)entry.getValue();
			// System.out.println("�������ţ�"+key+", ���ܱ�ţ�"+value);
		}
	}
	//���vm_serviceMap����
	public void printfvm_serviceMap(){
		//System.out.println("���vm_serviceMap�е����ݣ�");
		Iterator entries2 = vm_serviceMap.entrySet().iterator(); 
		while (entries2.hasNext()){
			 Map.Entry entry = (Map.Entry) entries2.next();  					  
			 Integer key = (Integer)entry.getKey();  //key��ʾ����	
			 List<Assignment> value = (List)entry.getValue();
			// System.out.println("�������ţ�"+key+", ���������"+value.size());
		}
	}
	//�������allDepthAssign�е�����
	public void printfAllDepthAssign(){
		System.out.println("���currDepthAssign�е����ݣ�");
		for(int i=0;i<allDepthAssign.size();i++){
			Assignment temp = allDepthAssign.get(i);  
			System.out.println("����"+i+"--�����"+temp.getVMNum()+"--����"+temp.getVMPerIndex()+"����ʱ�䣺"+temp.getServiceInfo().getRealRunTime());
			//System.out.println("����"+i+"--�����"+temp.getVMNum()+"--����"+temp.getVMPerIndex());
		}
	}
	/**
	 * ͳ�Ƶ�ǰ�Ѿ�����õ������֮���ͨ����·��
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
							//��������֮����ͨ��
							if(PublicAttribute.serviceData[serviceIndex1][serviceIndex2] > 0){
								if(vmnum1 != vmnum2){//�������Ų�ͬ
									vmAmongLinkNum[vmnum1][vmnum2]++;//�������֮���ͨ����·����1
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
		System.out.println("GreedyAStar4��ʼ����");
		long startTime = System.currentTimeMillis();
		//��ȡ�����ļ�����ʼ��
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
		System.out.println("GreedyAStar��ʼ����");
		long startTime = System.currentTimeMillis();
		//��ȡ�����ļ�����ʼ��
		String confPath = "D:/myeclipseWorkspace/AStarVersion4/config/15_1/1/conf.xml";
		String inputPath = "D:/myeclipseWorkspace/AStarVersion4/config/15_1/1/input.txt";
		double budget = 80.515038096;
		new Init().init(confPath,inputPath);			
		new GreedyAStar4(budget).solution();
		long endTime = System.currentTimeMillis();			
		System.out.println("����ʱ�䣺"+(endTime - startTime)+"ms");				
		
	}
}
