package entity;

/**
 * �洢DAGͼ���ÿһ����
 * */
public class EdgeOfGraph {
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	Assignment startVertex;//�����ߵ���ʼ�ڵ�
	Assignment endVertex;//�����ߵ���ֹ�ڵ�
	double weight;//�ñߵ�Ȩ�أ����ڵ��Ĵ���ʱ��
	double earliestTe;//��ʾ�ñߵ����翪ʼʱ��
	double latestTe;//��ʾ�ñߵ�����ʼʱ��
	public Assignment getStartVertex() {
		return startVertex;
	}
	public void setStartVertex(Assignment startVertex) {
		this.startVertex = startVertex;
	}
	public Assignment getEndVertex() {
		return endVertex;
	}
	public void setEndVertex(Assignment endVertex) {
		this.endVertex = endVertex;
	}
	public double getEarliestTe() {
		return earliestTe;
	}
	public void setEarliestTe(double earliestTe) {
		this.earliestTe = earliestTe;
	}
	public double getLatestTe() {
		return latestTe;
	}
	public void setLatestTe(double latestTe) {
		this.latestTe = latestTe;
	}
	
	
}
