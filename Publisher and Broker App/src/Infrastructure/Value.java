package Infrastructure;

import java.io.Serializable;

public class Value implements Serializable {

    private static final long serialVersionUTD = 21432L;

    private Bus bus;
    private double latitude;
    private double longitude;


    public Value() {
    }

    public Value (Bus bus, double latitude, double longitude){
        this.bus = bus;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Bus getBus() {
        return bus;
    }

    public void setBus(Bus bus) {
        this.bus = bus;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String toString(){
        return bus.toString() + "\nLatitude: " + latitude + "\nLongitude: " + longitude;
    }
}
