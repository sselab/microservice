package entity;

/**
 * ��ʾ�����һ�ַ���״̬
 * */
public class Assignment {
	Microservice serviceInfo;//���������Ϣ
	int VMNum;//������������
	int VMPerIndex;//���������������
	double transTime;//�����ʱ��
	double[] allTrans = new double[PublicAttribute.nodeNum];//��ʾ���з��񵽵�ǰ�����ͨ��ʱ��
	double earliestTv;//DAGͼ��΢���񶥵�����翪ʼʱ��
	double latestTv;//DAGͼ��΢���񶥵������ʼʱ��
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
