package gruppe11.aufgabe_2.map_items;

/**
 * Element that holds both longitude and latitude values
 * - sub: User(=Client), CommunityItem(=OtherUsers).
 */
public class Localizable {

    private double latitude;
    private double longitude;

    public Localizable(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Localizable() {
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
