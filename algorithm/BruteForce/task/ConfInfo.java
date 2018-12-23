package task;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ConfInfo {
	private List<Task> taskGraph=new ArrayList<Task>();
	private List<Node> nodeGraph=new ArrayList<Node>();
	private List<TaskInfoFromConf> confList=new ArrayList<TaskInfoFromConf>();
	
	public void readXML(){
		try {
			SAXReader reader=new SAXReader();
			Document document=reader.read(new File("conf.xml"));
			Element root=document.getRootElement();
//			System.out.println(root.getName());
			List<Element> jobList=root.elements("job");
//			System.out.println(childList.size());
			//获得task的属性
			for(int i=0;i<jobList.size();i++){
				Element job=jobList.get(i);
				TaskInfoFromConf tempTask=new TaskInfoFromConf();
				tempTask.setIndex(i);
				tempTask.setId(job.attributeValue("id"));
				tempTask.setName(job.attributeValue("name"));
				tempTask.setRunTime(Double.valueOf(job.attributeValue("runtime")));
				tempTask.setCpu(Integer.valueOf(job.attributeValue("cpu")));
				tempTask.setRam(Double.valueOf(job.attributeValue("ram")));
				tempTask.setStorage(Double.valueOf(job.attributeValue("storage")));
				
				List<Element> usesList=job.elements("uses");
//				System.out.println(usesList.size());
				List<UsesInfo> infoList=new ArrayList<UsesInfo>();
				for(int j=0;j<usesList.size();j++){
					
					Element uses=usesList.get(j);
					UsesInfo tempUses=new UsesInfo();
					tempUses.setName(uses.attributeValue("file"));
					tempUses.setLink(uses.attributeValue("link"));
					tempUses.setDataSize(Double.valueOf(uses.attributeValue("size")));
					infoList.add(tempUses);
				}
				tempTask.setUsesList(infoList);
//				System.out.println(tempTask.getId()+" "+tempTask.getName());
				confList.add(tempTask);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void genTask(){
		for(int i=0;i<confList.size();i++){
			Task task=new Task();
			TaskInfoFromConf conf=confList.get(i);
			task.setId(conf.getId());
			task.setIndex(conf.getIndex());
			task.setName(conf.getName());
			task.setTime(conf.getRunTime());
			task.setRunTime(conf.getRunTime());
			task.setCPU(conf.getCpu());
			task.setRam(conf.getRam());
			task.setStorage(conf.getStorage());
//			System.out.println(task.getId()+" "+task.getIndex()+" "+task.getName()+" "+task.getComplex());
			taskGraph.add(task);
		}
	}
	
	public void getTaskRelation(){
		SAXReader reader=new SAXReader();
		try {
			Document document=reader.read(new File("conf.xml"));
			Element root=document.getRootElement();
			List<Element> childList=root.elements("child");
			
			for(int i=0;i<childList.size();i++){
				Element child=childList.get(i);
				Task childTask=new Task();
//				System.out.println(child.attributeValue("ref"));
				for(int j=0;j<taskGraph.size();j++){
					if(taskGraph.get(j).getId().equalsIgnoreCase(child.attributeValue("ref"))){
						childTask=taskGraph.get(j);
						break;
					}
				}
//				System.out.println(childTask.getId());
				Iterator<Element> iterator=child.elementIterator();
				while(iterator.hasNext()){
					Element parent=iterator.next();
					Task parentTask=new Task();
					for(int j=0;j<taskGraph.size();j++){
						if(taskGraph.get(j).getId().equalsIgnoreCase(parent.attributeValue("ref"))){
							parentTask=taskGraph.get(j);
//							System.out.println(parentTask.getId());
							childTask.addParent(parentTask);
							parentTask.addChild(childTask);
							break;
						}
					}
				}
			}
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setTaskTransData(){
		for(int i=0;i<taskGraph.size();i++){
			Task tempTask=taskGraph.get(i);
			if(tempTask.getParents().size()==0){
				continue;
			}
			
			TaskInfoFromConf tempConf=confList.get(i);
			for(int j=0;j<tempConf.getUsesList().size();j++){
				UsesInfo tempUse=tempConf.getUsesList().get(j);
				if(tempUse.getLink().equalsIgnoreCase("input")){
					for(int k=0;k<tempTask.getParents().size();k++){
						Task tempParent=tempTask.getParents().get(k);
						TaskInfoFromConf tempParentConf=confList.get(tempParent.getIndex());
						for(int m=0;m<tempParentConf.getUsesList().size();m++){
							UsesInfo tempParentUses=tempParentConf.getUsesList().get(m);
							if(tempParentUses.getName().equalsIgnoreCase(tempUse.getName())&&tempParentUses.getLink().equalsIgnoreCase("output")){
								tempTask.addTransData(tempParent, tempParentUses.getDataSize());
							}
						}
					}
				}
			}
		}
	}
	
	public List<Task> getTaskGraph(){
		readXML();
		genTask();
		getTaskRelation();
		setTaskTransData();
		return taskGraph;
	}
	
	public static void main(String[] args){
		ConfInfo info=new ConfInfo();
		info.getTaskGraph();
		for(int i=0;i<info.taskGraph.size();i++){
			Task task=info.taskGraph.get(i);
			System.out.println(task.getCPU()+" "+task.getRam()+" "+task.getStorage());
		}
	}
}
