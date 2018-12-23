package task;

import java.util.List;

public class TaskInfoFromConf {
	private int index;
	private String id;
	private String name;
	private double runTime;  //运行时间
	private int cpu;         //CPU核数
	private double ram;   //内存
	private double storage; //磁盘容量
	private List<UsesInfo> usesList;
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getRunTime() {
		return runTime;
	}
	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}
	public List<UsesInfo> getUsesList() {
		return usesList;
	}
	public void setUsesList(List<UsesInfo> usesList) {
		this.usesList = usesList;
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
	
	
}
