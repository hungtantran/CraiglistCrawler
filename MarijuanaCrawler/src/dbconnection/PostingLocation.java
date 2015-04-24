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
    String posting_body = null;
    String title = null;
    String alt_prices = null;
    String alt_quantities = null;
    String url = null;
    
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

    public String getPosting_body() {
        return posting_body;
    }

    public void setPosting_body(String posting_body) {
        this.posting_body = posting_body;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	public String getAlt_quantities() {
		return alt_quantities;
	}

	public void setAlt_quantities(String alt_quantities) {
		this.alt_quantities = alt_quantities;
	}

	public String getAlt_prices() {
		return alt_prices;
	}

	public void setAlt_prices(String alt_prices) {
		this.alt_prices = alt_prices;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
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
