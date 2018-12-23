package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * ��ʾ����㷨�����е�һЩȫ�ֵĹ��õĲ���ı���
 * */
public class PublicAttribute {
	public static int MAX_SERVICE = 100;//���΢��������
	public static int MAX_NODE = 100;//��������ڵ�����
	
	public static  int microserviceNum;//΢��������
	public static  int nodeNum;//�����ڵ������
	
	//�����Ĵ�������
	public static  double[][] serviceData = new double[MAX_SERVICE][MAX_SERVICE];
	
	//������ɹ������ָ��cpu���ڴ桢Ӳ�̱���ֵ
	public static double percent;
	
	//�ڵ�����
	public static long flexNetWidth;
	public static  long[][] netWidth = new long[MAX_NODE][MAX_NODE];
	
	//�ڵ����ʱ
	public static  long[][] netDelay = new long[MAX_NODE][MAX_NODE];
	
	
	//�������΢������б�
	public static  Microservice[] serviceList;
	
	//��������������
	public static int performanceNum;
	//���������������ܵ��б�
	public static  VMPerformance[] performanceList;
	
	//����Լ���������Լ�����100����
	public static double limitCost = 0;
	
	/**
	 * ����������Ϊ��
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
