import java.io.Serializable;

public class Topic implements Comparable<Topic>, Serializable {

    private static final long serialVersionUTD = 43122L;

    private String busLine;

    public Topic(){}
    public Topic (String busLine){
        this.busLine = busLine;
    }

    public void setBusLine (String busLine){
        this.busLine = busLine;
    }

    public String getBusLine () {
        return busLine;
    }

    public int compareTo(Topic t){
        if(busLine.compareTo(t.getBusLine())==0) return 0;
        else return 1;
    }
}
