package operation;

import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;

import entity.Microservice;
import entity.PublicAttribute;
import entity.UsesInfo;

public class Init {
	public void init(String confPath,String inputPath) throws DocumentException, IOException{
		new ReadXMLConfig().readXMLConfig(confPath);
		new ReadIuputTxt().readIuputTxt(inputPath);
		getServiceData();
		//printAllService();
	}
	
	public void getServiceData(){
		//一开始均初始化为0
		for(int i=0;i<PublicAttribute.microserviceNum;i++){
			for(int j=0;j<PublicAttribute.microserviceNum;j++){
				PublicAttribute.serviceData[i][j]=0;
			}
		}
		
		//遍历serviceList中的所有服务
		for(int i=0;i<PublicAttribute.serviceList.length;i++){
			Microservice serviceTemp = PublicAttribute.serviceList[i];
			List<UsesInfo> serviceUsesList = serviceTemp.getUsesList();
			for(int j=0;j<serviceUsesList.size();j++){//遍历其uses信息
				UsesInfo usesTemp = serviceUsesList.get(j);
				if(usesTemp.getLink().equals("output")){
					//遍历其孩子
					for(int k=0;k<serviceTemp.getChildServiceList().size();k++){
						Microservice childServiceTemp = serviceTemp.getChildServiceList().get(k);
						//遍历child的uses信息
						for(int m=0;m<childServiceTemp.getUsesList().size();m++){
							UsesInfo childUsesTemp = childServiceTemp.getUsesList().get(m);
							if(childUsesTemp.getFilename().equals(usesTemp.getFilename()) && childUsesTemp.getLink().equals("input")){
								PublicAttribute.serviceData[serviceTemp.getIndex()][childServiceTemp.getIndex()] = usesTemp.getDataSize();
							}
						}
					}
				}
			}
		}
		
		/*for(int i=0;i<PublicAttribute.microserviceNum;i++){
			for(int j=0;j<PublicAttribute.microserviceNum;j++){
				System.out.print(" "+PublicAttribute.serviceData[i][j]+" ");
			}
			System.out.println();
		}*/
	}
	
	public void printAllService(){
		System.out.println("***************************获取所有服务信息************************");
		for(int i=0;i<PublicAttribute.serviceList.length;i++){
			Microservice temp = PublicAttribute.serviceList[i];
			System.out.println("服务编号："+temp.getId());
			System.out.println("child个数："+temp.getChildServiceList().size());
			System.out.print("child的服务编号：");
			for(int j=0;j<temp.getChildServiceList().size();j++){
				System.out.print(" "+temp.getChildServiceList().get(j).getId());
			}
			System.out.println();
			System.out.println("parent个数："+temp.getParentServiceList().size());
			System.out.print("parent的服务编号：");
			for(int j=0;j<temp.getParentServiceList().size();j++){				
				System.out.print(" "+temp.getParentServiceList().get(j).getId());
			}
			System.out.println();
			System.out.println("uses个数："+temp.getUsesList().size());
			for(int j=0;j<temp.getUsesList().size();j++){
				UsesInfo tempUse = temp.getUsesList().get(j);
				System.out.print("filename："+tempUse.getFilename());
				System.out.print(" link："+tempUse.getLink());
				System.out.println(" size："+tempUse.getDataSize());
			}
			System.out.println("**************************************************");
		}
		
	}
}
