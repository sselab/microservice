package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 搜索树的一个节点的状态信息
 * */
public class MicserviceStateInSearchTree {
	
	int searchNodeIndex ;//搜索节点的编号，唯一标识
	int currDepth;//当前深度，也是服务编号
	//当前这一层所做的分配
	Assignment currDepthAssign = new Assignment();	
	//表示搜索树的节点已经做了的局部分配Ax
	List<Assignment> partialAssign = new ArrayList<Assignment>();
	//每个节点都有一个时间的f(x)值，即A*算法中的估价函数=h(x)+g(x)
	double timeFunctionVal;
	//代价f(x)的估算值
	double priceFunctionVal;
	//该节点的子节点，有多个，所以用列表存储
	//List<MicserviceStateInSearchTree> childSearchNode = new ArrayList<MicserviceStateInSearchTree>();
	//父亲节点，只有一个
	//MicserviceStateInSearchTree parentSearchNode = new MicserviceStateInSearchTree();
	MicserviceStateInSearchTree parentSearchNode;
	
	
	public MicserviceStateInSearchTree(){
		
	}
	
	public MicserviceStateInSearchTree(MicserviceStateInSearchTree searchNode){
		this.searchNodeIndex = searchNode.getSearchNodeIndex();
		this.currDepth = searchNode.getCurrDepth();
		this.currDepthAssign = searchNode.getCurrDepthAssign();
		this.partialAssign = searchNode.getPartialAssign();
		this.timeFunctionVal = searchNode.getTimeFunctionVal();
		this.priceFunctionVal = searchNode.getPriceFunctionVal();
		//this.childSearchNode = searchNode.getChildSearchNode();
		this.parentSearchNode = searchNode.getParentSearchNode();
	}
	
	public int getSearchNodeIndex() {
		return searchNodeIndex;
	}
	public void setSearchNodeIndex(int searchNodeIndex) {
		this.searchNodeIndex = searchNodeIndex;
	}
	
	public int getCurrDepth() {
		return currDepth;
	}
	public void setCurrDepth(int currDepth) {
		this.currDepth = currDepth;
	}
	/*public List<MicserviceStateInSearchTree> getChildSearchNode() {
		return childSearchNode;
	}
	public void setChildSearchNode(List<MicserviceStateInSearchTree> childSearchNode) {
		this.childSearchNode = childSearchNode;
	}*/
	public MicserviceStateInSearchTree getParentSearchNode() {
		return parentSearchNode;
	}
	public void setParentSearchNode(MicserviceStateInSearchTree parentSearchNode) {
		this.parentSearchNode = parentSearchNode;
	}
	public List<Assignment> getPartialAssign() {
		return partialAssign;
	}
	public void setPartialAssign(List<Assignment> partialAssign) {
		this.partialAssign = partialAssign;
	}
	public double getTimeFunctionVal() {
		return timeFunctionVal;
	}
	public void setTimeFunctionVal(double timeFunctionVal) {
		this.timeFunctionVal = timeFunctionVal;
	}
	public double getPriceFunctionVal() {
		return priceFunctionVal;
	}
	public void setPriceFunctionVal(double priceFunctionVal) {
		this.priceFunctionVal = priceFunctionVal;
	}
	public Assignment getCurrDepthAssign() {
		return currDepthAssign;
	}
	public void setCurrDepthAssign(Assignment currDepthAssign) {
		this.currDepthAssign = currDepthAssign;
	}
	
	
	
	
}
