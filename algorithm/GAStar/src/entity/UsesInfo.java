package entity;

/**
* ��conf.xml�ж�ȡ��uses�ڵ���Ϣ
* */
public class UsesInfo implements Cloneable{
	String filename;//�ļ�����
	String link;//output/input
	double dataSize;//�ļ���С
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public double getDataSize() {
		return dataSize;
	}
	public void setDataSize(double dataSize) {
		this.dataSize = dataSize;
	}
	
	public Object clone(){
		
		UsesInfo copy=null;
		
		try {
			copy=(UsesInfo) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return copy;
	}
}
