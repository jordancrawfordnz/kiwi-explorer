package kiwi.jordancrawford.kiwiexplorer;

/**
 * Created by Jordan on 29/09/16.
 */
public class City {
    private String name, pictureResourceName;
    private double latitude, longitude;

    public String getName() {
        return name;
    }

    public String getPictureResourceName() {
        return pictureResourceName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPictureResourceName(String pictureResourceName) {
        this.pictureResourceName = pictureResourceName;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", pictureResourceName='" + pictureResourceName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
