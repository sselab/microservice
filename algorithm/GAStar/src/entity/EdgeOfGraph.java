package entity;

/**
 * 存储DAG图里的每一条边
 * */
public class EdgeOfGraph {
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	Assignment startVertex;//该条边的起始节点
	Assignment endVertex;//该条边的终止节点
	double weight;//该边的权重，及节点间的传输时间
	double earliestTe;//表示该边的最早开始时间
	double latestTe;//表示该边的最晚开始时间
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
