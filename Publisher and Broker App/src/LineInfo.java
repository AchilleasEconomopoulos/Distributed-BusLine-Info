public class LineInfo {
    String lineName;

    public LineInfo(){
    }

    public LineInfo(String name, int type){
        lineName = name;

        if(type==1) lineName+= " Afethria -> Terma";
        else lineName += " Terma -> Afethria";
    }

    public String getLineName(){return lineName;}
    public void setLineName(String name, int type){
        lineName = name;
        if(type==1) lineName+= " Afethria -> Terma";
        else lineName += " Terma -> Afethria";
    }
}
