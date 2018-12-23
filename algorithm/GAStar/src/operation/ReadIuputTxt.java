package operation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import entity.PublicAttribute;
import entity.VMPerformance;

public class ReadIuputTxt {
	
	public void readIuputTxt(String inputPath) throws IOException{
		 File file=new File(inputPath);
		 try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file),"utf-8");
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while(true){
				lineTxt = bufferedReader.readLine();
				if(lineTxt == null){
					break;
				}else{
					if(lineTxt.contains("#�̶�����")){
						PublicAttribute.flexNetWidth = Long.parseLong(bufferedReader.readLine());
					}
					
					if(lineTxt.contains("#ռ�ñ���")){
						PublicAttribute.percent = Double.parseDouble(bufferedReader.readLine());
						
					}
					
					/*if(lineTxt.contains("#����")){
						for(int i=0;i<PublicAttribute.nodeNum;i++){
							String[] str = bufferedReader.readLine().split(" ");
							for(int j=0;j<PublicAttribute.nodeNum;j++){
								PublicAttribute.netWidth[i][j] = Long.parseLong(str[j]);
							}
						}
					}*/
					
					/*if(lineTxt.equals("#��ʱ")){
						for(int i=0;i<PublicAttribute.nodeNum;i++){
							String[] str = bufferedReader.readLine().split(" ");
							for(int j=0;j<PublicAttribute.nodeNum;j++){
								PublicAttribute.netDelay[i][j] = Long.parseLong(str[j]);
							}
						}
					}*/
					if(lineTxt.equals("#��������")){
						PublicAttribute.performanceNum = Integer.parseInt(bufferedReader.readLine());
						PublicAttribute.performanceList = new VMPerformance[PublicAttribute.performanceNum];
					}
					
					if(lineTxt.equals("#���������")){
						for(int i=0;i<PublicAttribute.performanceNum;i++){
							String[] str = bufferedReader.readLine().split(" ");
							VMPerformance vmPer = new VMPerformance();						
							vmPer.setIndex(i);
							//ֱ�Ӱ����ʴ洢
							vmPer.setCpu(PublicAttribute.percent * Integer.parseInt(str[0]));
							vmPer.setRam(PublicAttribute.percent * Double.parseDouble(str[1]));
							vmPer.setStorage(PublicAttribute.percent * Integer.parseInt(str[2]));
							vmPer.setPrices(Double.parseDouble(str[3]));
							PublicAttribute.performanceList[i] = vmPer;	
						}
					}
				}
            }
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*public static void main(String[] args) throws IOException{
		new ReadIuputTxt();
		for(int i=0;i<5;i++){
			for(int j=0;j<4;j++){
				System.out.println(PublicAttribute.netDelay[i][j]);
			}
		}
	}*/
}
