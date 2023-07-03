import java.io.Serializable;

public class Bus implements Serializable {

    private static final long serialVersionUTD = 33214L;

    private String lineNumber;
    private String routeCode;
    private String vehicleId;
    private String lineName;
    private String busLineId;
    private String info;

    public Bus() {
    }

    Bus (String lineNumber, String routeCode, String vehicleId, String lineName, String busLineId, String info){
        this.lineNumber = lineNumber;
        this.routeCode = routeCode;
        this.vehicleId = vehicleId;
        this.lineName = lineName;
        this.busLineId = busLineId;
        this.info = info;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getVehicled() {
        return vehicleId;
    }

    public void setVehicled(String vehicled) {
        this.vehicleId = vehicled;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getBusLineId() {
        return busLineId;
    }

    public void setBusLineId(String busLineId) {
        this.busLineId = busLineId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String toString(){
        return "Line Number: " + lineNumber + "\nRoute Code: " + routeCode + "\nVehicle id: " + vehicleId + "\nLine name: " + lineName + "\nBusLine id: " + busLineId + "\nInfo: " + info;
    }
}
