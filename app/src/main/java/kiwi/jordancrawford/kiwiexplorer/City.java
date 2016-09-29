package kiwi.jordancrawford.kiwiexplorer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jordan on 29/09/16.
 */
public class City implements Parcelable {
    private String name, pictureResourceName;
    private double latitude, longitude;
    private CityData cityData;

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

    public CityData getCityData() {
        return cityData;
    }

    public void setCityData(CityData cityData) {
        this.cityData = cityData;
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

    public City() {

    }

    protected City(Parcel in) {
        name = in.readString();
        pictureResourceName = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(pictureResourceName);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        @Override
        public City createFromParcel(Parcel in) {
            return new City(in);
        }

        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };

}
