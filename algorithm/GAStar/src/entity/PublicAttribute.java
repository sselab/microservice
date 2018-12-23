package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示真个算法过程中的一些全局的共用的不变的变量
 * */
public class PublicAttribute {
	public static int MAX_SERVICE = 100;//最大微服务数量
	public static int MAX_NODE = 100;//最大主机节点数量
	
	public static  int microserviceNum;//微服务数量
	public static  int nodeNum;//主机节点的数量
	
	//服务间的传输数据
	public static  double[][] serviceData = new double[MAX_SERVICE][MAX_SERVICE];
	
	//虚拟机可供容器分割的cpu、内存、硬盘比例值
	public static double percent;
	
	//节点间带宽
	public static long flexNetWidth;
	public static  long[][] netWidth = new long[MAX_NODE][MAX_NODE];
	
	//节点间延时
	public static  long[][] netDelay = new long[MAX_NODE][MAX_NODE];
	
	
	//存放所有微服务的列表
	public static  Microservice[] serviceList;
	
	//虚拟机规格种类数
	public static int performanceNum;
	//存放所有虚拟机性能的列表
	public static  VMPerformance[] performanceList;
	
	//代价约束，我先自己定个100好了
	public static double limitCost = 0;
	
	/**
	 * 属性重新置为空
	 * */
	public static void reset(){
		
		microserviceNum=0;
		nodeNum=0;
		percent=0.0;
		flexNetWidth=0L;
		performanceNum=0;
		
		
		serviceData = new double[MAX_SERVICE][MAX_SERVICE];
		netWidth = new long[MAX_NODE][MAX_NODE];
		netDelay = new long[MAX_NODE][MAX_NODE];
		serviceList = null ;
		performanceList = null;
	}
}
