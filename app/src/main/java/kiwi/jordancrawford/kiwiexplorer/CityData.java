package kiwi.jordancrawford.kiwiexplorer;

/**
 * Represents information about a city in the database.
 *
 * Created by Jordan on 29/09/16.
 */
public class CityData {
    private long id;
    private String cityName;
    private boolean citySeen, isCurrentLocation;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCityName() {

        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public boolean isCitySeen() {
        return citySeen;
    }

    public void setCitySeen(boolean citySeen) {
        this.citySeen = citySeen;
    }

    public boolean isCurrentLocation() {
        return isCurrentLocation;
    }

    public void setCurrentLocation(boolean currentLocation) {
        isCurrentLocation = currentLocation;
    }

    @Override
    public String toString() {
        return "CityData{" +
                "id=" + id +
                ", cityName='" + cityName + '\'' +
                ", citySeen=" + citySeen +
                ", isCurrentLocation=" + isCurrentLocation +
                '}';
    }
}
