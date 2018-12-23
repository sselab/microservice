/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bheft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.swing.JOptionPane;


public class TestHEFT {
	private static String MESSAGE = "";
	public static boolean copyFile(String srcFileName, String destFileName,  
	         boolean overlay) {  
	         File srcFile = new File(srcFileName);  	      
	        if (!srcFile.exists()) {  
	            MESSAGE = "file" + srcFileName + "is not exist";  
	           JOptionPane.showMessageDialog(null, MESSAGE);  
	            return false;  
	        } else if (!srcFile.isFile()) {  
	            MESSAGE = "file" + srcFileName + "is not a file";  
	            JOptionPane.showMessageDialog(null, MESSAGE);  
	           return false;  
	        }  
	    
	        File destFile = new File(destFileName);  
	       if (destFile.exists()) {  	              
	            if (overlay) {  	          
	                new File(destFileName).delete();  
	            }  
	        } else {   
	            if (!destFile.getParentFile().exists()) {    
	                if (!destFile.getParentFile().mkdirs()) {    
	                    return false;  
	                }  
	            }  
            }  	  
	        int byteread = 0;  
	        InputStream in = null;  
	        OutputStream out = null;  
	
	        try {  
	            in = new FileInputStream(srcFile);  
	            out = new FileOutputStream(destFile);  
	            byte[] buffer = new byte[1024];  
	  
	            while ((byteread = in.read(buffer)) != -1) {  
	                out.write(buffer, 0, byteread);  
	            }  
	            return true;  
	        } catch (FileNotFoundException e) {  
	            return false;  
	        } catch (IOException e) {  
	            return false;  
	        } finally {  
	            try {  
	                if (out != null)  
	                    out.close();  
	                if (in != null)  
	                    in.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  

    }

    
    public static void main(String[] args) throws IOException {
    	int[] workFlow={5,6,7,8,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90,95,100};
    	for(int point=0;point<1;point++){  //workFlow.length
    		int workflow=workFlow[point];
	    	String sourceFilei;
			String destinationFilei;
			sourceFilei="E://研究生//微服务//workflow//workflow//"+workflow+"//input.txt";
			//sprintf(destinationFile,"%s//conf.xml",buf);
			destinationFilei=".//input.txt";
			copyFile(sourceFilei,destinationFilei,true); 
			String fileOne=String.format("E://研究生//微服务//result//%d",workflow);
			File dirOne=new File(fileOne);
			dirOne.mkdir();
			for(int flow=1;flow<=1;flow++){  //50
				String sourceFilec;
				String destinationFilec;
				sourceFilec=String.format("E://研究生//微服务//workflow//workflow//%d//%d//conf.xml",workflow, flow);
				destinationFilec=".//conf.xml";
				copyFile(sourceFilec,destinationFilec,true); 
				String fileTwo=String.format("%s//%d",fileOne,flow);
				File dirTwo=new File(fileTwo);
				dirTwo.mkdir();
				/*try{
					FileReader reader = new FileReader("E://研究生//微服务//workflow//workflow//"+workflow+"//"+flow+"//budget.txt");
				    BufferedReader br = new BufferedReader(reader);
				    String readtemp;
				    int num=1;
				    while((readtemp = br.readLine()) != null){
				    	 double pay=Double.parseDouble(readtemp);
	                     HEFT heft = new HEFT(pay);
	                     heft.run(num,fileTwo);
	                     num++;
				    }
				 }catch(Exception e){
			    	  e.printStackTrace();
			      }*/
				 HEFT heft = new HEFT(1000);
                 heft.run();
	    }
    }
  } 
}
