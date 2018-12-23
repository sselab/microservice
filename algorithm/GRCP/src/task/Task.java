package task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Task {
	private int index;
	private String id;
	private String name;
	private double time;
	private double runTime;
	private double est;  //最早开始时间
	private double lst;  //最晚开始时间
	private List<Task> parents=new ArrayList<Task>();
	private List<Task> children=new ArrayList<Task>();
	private Map<Task, Double> transData=new HashMap<Task, Double>();
	private int nodeIndex=-1;   //映射到的虚拟机编号
	private int bestIndex=-1;
	private int cpu;         //CPU核数
	private double ram;   //内存
	private double storage; //磁盘容量
	
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double data) {
		this.time = data;
	}
	public double getRunTime() {
		return runTime;
	}
	public void setRunTime(double runTime) {
		this.runTime = runTime;
	}
	public double getEst() {
		return est;
	}
	public void setEst(double est) {
		this.est = est;
	}
	public double getLst() {
		return lst;
	}
	public void setLst(double lst) {
		this.lst = lst;
	}
	public List<Task> getParents() {
		return parents;
	}
	public void setParents(List<Task> parents) {
		this.parents = parents;
	}
	public List<Task> getChildren() {
		return children;
	}
	public void setChildren(List<Task> children) {
		this.children = children;
	}
	public Map<Task, Double> getTransData() {
		return transData;
	}
	public void setTransData(Map<Task, Double> transData) {
		this.transData = transData;
	}
	public int getNodeIndex() {
		return nodeIndex;
	}
	public void setNodeIndex(int nodeIndex) {
		this.nodeIndex = nodeIndex;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	
	public void addChild(Task child){
		children.add(child);
	}
	
	public void addParent(Task parent){
		parents.add(parent);
	}
	
	public void addTransData(Task task,double data){
		transData.put(task, data);
	}
	public int getCPU() {
		return cpu;
	}
	public void setCPU(int cPU) {
		cpu = cPU;
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
	public int getBestIndex() {
		return bestIndex;
	}
	public void setBestIndex(int bestIndex) {
		this.bestIndex = bestIndex;
	}
	
}
