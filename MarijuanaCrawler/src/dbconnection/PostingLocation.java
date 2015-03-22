package dbconnection;

public class PostingLocation {
    // Constants
    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    // Properties
    // ---------------------------------------------------------------------------------

    String state = null;
    String city = null;
    String latitude = null;
    String longitude = null;
    Integer location_fk = null;
    Integer location_link_fk = null;
    String datePosted = null;
    String timePosted = null;
    
    // Getters/setters
    // ----------------------------------------------------------------------------
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getLatitude() {
        return latitude;
    }
    
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
    
    public String getLongitude() {
        return longitude;
    }
    
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
    
    public Integer getLocation_fk() {
        return location_fk;
    }
    
    public void setLocation_fk(Integer location_fk) {
        this.location_fk = location_fk;
    }

    public Integer getLocation_link_fk() {
        return location_link_fk;
    }

    public void setLocation_link_fk(Integer location_link_fk) {
        this.location_link_fk = location_link_fk;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public String getTimePosted() {
        return timePosted;
    }

    public void setTimePosted(String timePosted) {
        this.timePosted = timePosted;
    }
    
    // Object overrides
    // ---------------------------------------------------------------------------

    /**
         */
    @Override
    public boolean equals(Object other) {
        // TODO implement this
        return false;
    }

    /**
         */
    @Override
    public int hashCode() {
        // TODO implement this
        return 0;
    }

    /**
         */
    @Override
    public String toString() {
        // TODO implement this
        return this.location_fk + " State:" + this.state + ", City:" + this.city + " Lat:" + this.latitude + ", Long:" + this.longitude;
    }

    // Custom methods
    public boolean isValid() {
        return this.location_fk != null;
    }
}
