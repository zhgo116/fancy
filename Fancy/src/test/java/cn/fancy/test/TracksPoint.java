package cn.fancy.test;

import java.text.DecimalFormat;

public class TracksPoint implements Comparable<TracksPoint> {
	public int id;//点ID
    public double pe;//经度
    public double pn;//维度
    public TracksPoint(){}//空构造函数
    
    public String toString(){
        //DecimalFormat df = new DecimalFormat("0.000000");
        return this.id+"#"+this.pn+","+this.pe;
    }
    public String getTestString(){
        DecimalFormat df = new DecimalFormat("0.000000");
        return df.format(this.pn)+","+df.format(this.pe);
    }
    public String getResultString(){
        DecimalFormat df = new DecimalFormat("0.000000");
        return this.id+"#"+df.format(this.pn)+","+df.format(this.pe);
    }
    
    @Override
    public int compareTo(TracksPoint other) {
        if(this.id<other.id)  return -1;
        else if(this.id>other.id)  return 1;
        else
            return 0;
    }
}
