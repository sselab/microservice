package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * ��������һ���ڵ��״̬��Ϣ
 * */
public class MicserviceStateInSearchTree {
	
	int searchNodeIndex ;//�����ڵ�ı�ţ�Ψһ��ʶ
	int currDepth;//��ǰ��ȣ�Ҳ�Ƿ�����
	//��ǰ��һ�������ķ���
	Assignment currDepthAssign = new Assignment();	
	//��ʾ�������Ľڵ��Ѿ����˵ľֲ�����Ax
	List<Assignment> partialAssign = new ArrayList<Assignment>();
	//ÿ���ڵ㶼��һ��ʱ���f(x)ֵ����A*�㷨�еĹ��ۺ���=h(x)+g(x)
	double timeFunctionVal;
	//����f(x)�Ĺ���ֵ
	double priceFunctionVal;
	//�ýڵ���ӽڵ㣬�ж�����������б�洢
	//List<MicserviceStateInSearchTree> childSearchNode = new ArrayList<MicserviceStateInSearchTree>();
	//���׽ڵ㣬ֻ��һ��
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
