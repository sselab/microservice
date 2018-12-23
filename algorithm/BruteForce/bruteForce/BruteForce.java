package bruteForce;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class BruteForce {
	private Function function;
	private double[] object; 
	private double[] bestObject;
	private int dimension;
	private double[] x;
	private double[] temp;
	public BruteForce(double budget,int dimension){
		function=new Function(budget);
		function.init();
		object=new double[2];
		object[0]=Double.MAX_VALUE;
		object[1]=Double.MAX_VALUE;
		bestObject=new double[2];
		bestObject[0]=Double.MAX_VALUE;
		bestObject[1]=Double.MAX_VALUE;
		this.dimension=dimension;
		x=new double[dimension];
		temp=new double[dimension];
	}
	
	public void execute(){
//		System.out.println(temp.length);
		for(int i=0;i<dimension;i++){
			temp[i]=(double)i/dimension;
		}
		
		calculateRecursively(0);
	}
	
	public void calculateRecursively(int index) {
		// TODO Auto-generated method stub
		if(index==dimension){
			calculate();
			return;
		}
		
		for(int i=0;i<dimension;i++){
			x[index]=temp[i];
			//x[index]=i;
			calculateRecursively(index+1);
		}
	}
	
	public void calculate(){
//		for(int i=0;i<dimension;i++){
//			System.out.print((int)(x[i]*dimension)+" ");
//		}
//		System.out.println();
		object=function.calculate(x);
		if(object[0]<bestObject[0]){
			bestObject[0]=object[0];
			bestObject[1]=object[1];
		}
		
//		for(int i=0;i<dimension;i++){
//			System.out.print((int)(x[i]*dimension)+" ");
//		}
//		System.out.println();
	}
	
	public double[] getBest(){
		return bestObject;
	}
	
	public static List<Double> getBudget(String path){
		List<Double> budget=new ArrayList<Double>();
		File file=new File(path);
		try {
			BufferedReader reader=new BufferedReader(new FileReader(file));
			String tempString="";
			while((tempString=reader.readLine())!=null){
				budget.add(Double.valueOf(tempString));
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return budget;
		
	}
	
	public static void main(String[] args){
		int[] dimension={5,6,7,8};
		File workflow=new File("workflow");
		File[] numFiles=workflow.listFiles();
		for(int i=0;i<dimension.length;i++){
			File source1=new File("workflow//"+dimension[i]+"//input.txt");
			File dest1=new File("input.txt");
			if(dest1.exists()){
				dest1.delete();
			}
			try {
				Files.copy(source1.toPath(), dest1.toPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int j=1;j<=50;j++){
				File source=new File("workflow//"+dimension[i]+"//"+j+"//conf.xml");
				File dest=new File("conf.xml");
    			if(dest.exists()){
    				dest.delete();
    			}
    			try {
					Files.copy(source.toPath(), dest.toPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			List<Double> budget=getBudget("workflow//"+dimension[i]+"//"+j+"//budget.txt");
    			for(int k=0;k<budget.size();k++){
    				System.out.println(dimension[i]+":"+j+" "+k);
	    			long startTime=System.currentTimeMillis();
	    			
	    			BruteForce bruteForce=new BruteForce(budget.get(k), dimension[i]);
	    			bruteForce.execute();
	    			
	    			long endTime=System.currentTimeMillis();
	    			long time=startTime-endTime;
	    			
	    			String path="PF//"+dimension[i]+"//"+j;
					File numFile=new File(path);
					if(!numFile.exists()&&!numFile.isDirectory()){
						numFile.mkdirs();
					}
					
					String fileName=path+"//"+(k+1)+".txt";
					FileWriter writer=null;
					try{
						writer=new FileWriter(fileName,true);
						writer.write("time:"+bruteForce.getBest()[0]+"\r\n");
						writer.write("cost:"+bruteForce.getBest()[1]+"\r\n");
						writer.write("runTime:"+time);
					}catch (IOException e){
						e.printStackTrace();
					}finally{
						try{
							if(writer!=null){
								writer.close();
							}
						}catch (IOException e){
							e.printStackTrace();
						}
					}
    			}
			}
		}
	}

}
