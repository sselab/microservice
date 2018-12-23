package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 微服务的基本信息
 * */

public class Microservice implements Cloneable{
	int index;//微服务编号
	String id;//微服务id
	double runtime;//运行时间
	double realRunTime;//实际运行时间
	int cpu;//cpu核数
	double ram;//内存
	double storage;//磁盘
	List<Microservice> parentServiceList = new ArrayList<Microservice>();//父亲
	List<Microservice> childServiceList = new ArrayList<Microservice>();//孩子
	List<UsesInfo> usesList = new ArrayList<UsesInfo>();//传输数据信息
	
	public Microservice(){
		
	}
	public Microservice(Microservice service){
		this.index = service.getIndex();
		this.id = service.getId();
		this.runtime = service.getRuntime();
		this.realRunTime = service.getRealRunTime();
		this.cpu = service.getCpu();
		this.ram = service.getRam();
		this.storage = service.getStorage();
		this.parentServiceList = service.getParentServiceList();
		this.childServiceList = service.getChildServiceList();
		this.usesList = service.getUsesList();
	}
	
	
	public Object clone(){
		
		//System.out.println("拷贝前parent节点数："+this.getParentServiceList().size());
		
		Microservice copy=null;
		
		try {
			
			copy=(Microservice) super.clone();
			
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		List<Microservice> parentSL = new ArrayList<Microservice>();
		
		//System.out.println("拷贝前child节点数："+this.getChildServiceList().size());
		
		for(int i=0;i<parentServiceList.size();i++){
			
			//System.out.println("服务编号"+parentServiceList.get(i).getIndex());
			Microservice m=(Microservice) parentServiceList.get(i).clone();
			parentSL.add(m);
		}
		
		copy.setParentServiceList(parentSL);
		
		/*List<Microservice> childSL = new ArrayList<Microservice>();//孩子
		
		for(int i=0;i<childServiceList.size();i++){
			
			Microservice m=(Microservice) childServiceList.get(i).clone();
			childSL.add(m);
		}
		
		copy.setChildServiceList(childSL);*/
		
		List<UsesInfo> usesL = new ArrayList<UsesInfo>();//传输数据信息
		
		for(int i=0;i<usesList.size();i++){
			
			UsesInfo m=(UsesInfo) usesList.get(i).clone();
			usesL.add(m);
		}
		
		copy.setUsesList(usesL);
		
		copy.id=new String(id);
		
		return copy;
	}
	
	public static void main(String[] args){
		
		Microservice m=new Microservice();
		
		
	}
	
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getRuntime() {
		return runtime;
	}
	public void setRuntime(double runtime) {
		this.runtime = runtime;
	}
	public int getCpu() {
		return cpu;
	}
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}
	public double getRam() {
		return ram;
	}
	public void setRam(double ram) {
		this.ram = ram;
	}
	public double getStorage() {
		return storage;
	}
	public void setStorage(double storage) {
		this.storage = storage;
	}
	
	public List<Microservice> getParentServiceList() {
		return parentServiceList;
	}
	public void setParentServiceList(List<Microservice> parentServiceList) {
		this.parentServiceList = parentServiceList;
	}
	public List<Microservice> getChildServiceList() {
		return childServiceList;
	}
	public void setChildServiceList(List<Microservice> childServiceList) {
		this.childServiceList = childServiceList;
	}
	public List<UsesInfo> getUsesList() {
		return usesList;
	}
	public void setUsesList(List<UsesInfo> usesList) {
		this.usesList = usesList;
	}
	public double getRealRunTime() {
		return realRunTime;
	}
	public void setRealRunTime(double realRunTime) {
		this.realRunTime = realRunTime;
	}
	
	
}
