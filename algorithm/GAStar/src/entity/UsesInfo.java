package entity;

/**
* 从conf.xml中读取的uses节点信息
* */
public class UsesInfo implements Cloneable{
	String filename;//文件名字
	String link;//output/input
	double dataSize;//文件大小
	
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
