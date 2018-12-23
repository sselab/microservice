package operation;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import entity.Microservice;
import entity.PublicAttribute;
import entity.UsesInfo;

public class ReadXMLConfig {
	

	public void readXMLConfig(String confPath) throws DocumentException{
		SAXReader saxReader = new SAXReader(); 

        Document document = saxReader.read(new File(confPath));
        // 获取根元素
        Element root = document.getRootElement();       
        // 获取特定名称的子元素：job
        List<Element> jobList = root.elements("job");
        PublicAttribute.microserviceNum = jobList.size();//微服务个数赋值
        PublicAttribute.nodeNum = jobList.size();//主机节点个数赋值
        PublicAttribute.serviceList = new Microservice[jobList.size()];
        int jobIndex=0;
        // 迭代输出
        for (Iterator iter = jobList.iterator(); iter.hasNext();)
        {       	
        	//遍历job节点，生成Microservice对象，加到列表中
        	Microservice micService = new Microservice();
            Element jobEle = (Element) iter.next();
            micService.setId(jobEle.attributeValue("id"));
            micService.setIndex(jobIndex);
            micService.setRuntime(Double.parseDouble(jobEle.attributeValue("runtime")));
            micService.setRealRunTime(Double.parseDouble(jobEle.attributeValue("runtime")));//实际运行时间初始化为运行时间
            micService.setCpu(Integer.parseInt(jobEle.attributeValue("cpu")));
            micService.setRam(Double.parseDouble(jobEle.attributeValue("ram")));
            micService.setStorage(Double.parseDouble(jobEle.attributeValue("storage")));
            PublicAttribute.serviceList[jobIndex] = micService;
            jobIndex++;
            
            //获取uses节点的信息
            List<Element> useList = jobEle.elements("uses");
            for(Iterator useIter = useList.iterator(); useIter.hasNext();){
            	Element useElem = (Element) useIter.next();
            	UsesInfo useTemp = new UsesInfo();
            	useTemp.setFilename(useElem.attributeValue("file"));
            	useTemp.setLink(useElem.attributeValue("link"));
            	useTemp.setDataSize(Double.parseDouble(useElem.attributeValue("size")));
            	micService.getUsesList().add(useTemp);
            }
        }
        //获取特定名称的子元素：child
        List<Element> childList = root.elements("child");
        for(Iterator childIter = childList.iterator(); childIter.hasNext();){
        	Element childElem = (Element) childIter.next();
        	String ref = childElem.attributeValue("ref");//获取clid节点的ref属性
        	Microservice currChildService = new Microservice();
        	//遍历找到当前child节点在list中对应的服务
        	for(int i=0;i<PublicAttribute.serviceList.length;i++){
        		Microservice childtemp = PublicAttribute.serviceList[i];
        		if(childtemp.getId().equals(ref)){
        			currChildService = childtemp;
        			break;
        		}
        	}
        	//System.out.println("当前child服务编号："+currChildService.getId());
        	List<Element> parentList = childElem.elements("parent");//获取parent节点
			for(Iterator paIter = parentList.iterator(); paIter.hasNext();){
				Element parentE = (Element) paIter.next();
				String parentRef = parentE.attributeValue("ref");//获取parent节点的ref属性
				//System.out.println("parent服务编号："+parentRef);
				
				for(int i=0;i<PublicAttribute.serviceList.length;i++){
	        		Microservice parenttemp = PublicAttribute.serviceList[i];
	        		if(parenttemp.getId().equals(parentRef)){
	        			//System.out.println("找到对应的parent服务：");
	        			parenttemp.getChildServiceList().add(currChildService);
	        			currChildService.getParentServiceList().add(parenttemp);
	        			//break;
	        		}
	        	}
			}
        }
        
	}
	/*public static void main(String[] args) throws DocumentException{
		new ReadXMLConfig().readXMLConfig();
	}*/
}
