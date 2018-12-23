package entity;

/**
 * 表示服务的一种分配状态
 * */
public class Assignment {
	Microservice serviceInfo;//服务基本信息
	int VMNum;//所在虚拟机编号
	int VMPerIndex;//所在虚拟机的性能
	double transTime;//最大传输时间
	double[] allTrans = new double[PublicAttribute.nodeNum];//表示所有服务到当前服务的通信时间
	double earliestTv;//DAG图中微服务顶点的最早开始时间
	double latestTv;//DAG图中微服务顶点的最晚开始时间
	double currTime;
	double currPrice;
	
	public Assignment(){
		
	}
	public Assignment(Assignment assign){
		this.serviceInfo = assign.getServiceInfo();
		this.VMNum = assign.getVMNum();
		this.VMPerIndex = assign.getVMPerIndex();
		this.transTime = assign.getTransTime();
		this.earliestTv = assign.getEarliestTv();
		this.latestTv = assign.getLatestTv();
	}
	public Microservice getServiceInfo() {
		return serviceInfo;
	}
	public void setServiceInfo(Microservice serviceInfo) {
		this.serviceInfo = serviceInfo;
	}
	public int getVMNum() {
		return VMNum;
	}
	public void setVMNum(int vMNum) {
		VMNum = vMNum;
	}
	
	public int getVMPerIndex() {
		return VMPerIndex;
	}
	public void setVMPerIndex(int vMPerIndex) {
		VMPerIndex = vMPerIndex;
	}
	public double getTransTime() {
		return transTime;
	}
	public void setTransTime(double transTime) {
		this.transTime = transTime;
	}
	public double getEarliestTv() {
		return earliestTv;
	}
	public void setEarliestTv(double earliestTv) {
		this.earliestTv = earliestTv;
	}
	public double getLatestTv() {
		return latestTv;
	}
	public void setLatestTv(double latestTv) {
		this.latestTv = latestTv;
	}
	public double[] getAllTrans() {
		return allTrans;
	}
	public void setAllTrans(double[] allTrans) {
		this.allTrans = allTrans;
	}
	public double getCurrTime() {
		return currTime;
	}
	public void setCurrTime(double currTime) {
		this.currTime = currTime;
	}
	public double getCurrPrice() {
		return currPrice;
	}
	public void setCurrPrice(double currPrice) {
		this.currPrice = currPrice;
	}
	
	
}
