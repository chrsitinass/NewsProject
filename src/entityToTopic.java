
public class EntityToTopic {
	public int x,y;
	public EntityToTopic(int x, int y){
		this.x = x;
		this.y = y;
	}
    public boolean equals(Object obj){  
        if(this == obj)//判断是否是本类的一个引用  
            return true;  
        if(obj == null)//
            return false;             
        EntityToTopic pair = (EntityToTopic)obj;
        if(this.x != pair.x)  
            return false;  
        if(this.y != pair.y)  
            return false;  
        return true;  
    }
    public int hashCode(){  
        int result = 30211;  
        result = (result * 31 + this.x)%324757;  
        result = (result * 31 + this.y)%324757;
        return result;  
    }  
}
