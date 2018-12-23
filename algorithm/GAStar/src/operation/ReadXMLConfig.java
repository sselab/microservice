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
        // ��ȡ��Ԫ��
        Element root = document.getRootElement();       
        // ��ȡ�ض����Ƶ���Ԫ�أ�job
        List<Element> jobList = root.elements("job");
        PublicAttribute.microserviceNum = jobList.size();//΢���������ֵ
        PublicAttribute.nodeNum = jobList.size();//�����ڵ������ֵ
        PublicAttribute.serviceList = new Microservice[jobList.size()];
        int jobIndex=0;
        // �������
        for (Iterator iter = jobList.iterator(); iter.hasNext();)
        {       	
        	//����job�ڵ㣬����Microservice���󣬼ӵ��б���
        	Microservice micService = new Microservice();
            Element jobEle = (Element) iter.next();
            micService.setId(jobEle.attributeValue("id"));
            micService.setIndex(jobIndex);
            micService.setRuntime(Double.parseDouble(jobEle.attributeValue("runtime")));
            micService.setRealRunTime(Double.parseDouble(jobEle.attributeValue("runtime")));//ʵ������ʱ���ʼ��Ϊ����ʱ��
            micService.setCpu(Integer.parseInt(jobEle.attributeValue("cpu")));
            micService.setRam(Double.parseDouble(jobEle.attributeValue("ram")));
            micService.setStorage(Double.parseDouble(jobEle.attributeValue("storage")));
            PublicAttribute.serviceList[jobIndex] = micService;
            jobIndex++;
            
            //��ȡuses�ڵ����Ϣ
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
        //��ȡ�ض����Ƶ���Ԫ�أ�child
        List<Element> childList = root.elements("child");
        for(Iterator childIter = childList.iterator(); childIter.hasNext();){
        	Element childElem = (Element) childIter.next();
        	String ref = childElem.attributeValue("ref");//��ȡclid�ڵ��ref����
        	Microservice currChildService = new Microservice();
        	//�����ҵ���ǰchild�ڵ���list�ж�Ӧ�ķ���
        	for(int i=0;i<PublicAttribute.serviceList.length;i++){
        		Microservice childtemp = PublicAttribute.serviceList[i];
        		if(childtemp.getId().equals(ref)){
        			currChildService = childtemp;
        			break;
        		}
        	}
        	//System.out.println("��ǰchild�����ţ�"+currChildService.getId());
        	List<Element> parentList = childElem.elements("parent");//��ȡparent�ڵ�
			for(Iterator paIter = parentList.iterator(); paIter.hasNext();){
				Element parentE = (Element) paIter.next();
				String parentRef = parentE.attributeValue("ref");//��ȡparent�ڵ��ref����
				//System.out.println("parent�����ţ�"+parentRef);
				
				for(int i=0;i<PublicAttribute.serviceList.length;i++){
	        		Microservice parenttemp = PublicAttribute.serviceList[i];
	        		if(parenttemp.getId().equals(parentRef)){
	        			//System.out.println("�ҵ���Ӧ��parent����");
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
