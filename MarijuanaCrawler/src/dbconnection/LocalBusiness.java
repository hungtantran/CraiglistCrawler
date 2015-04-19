package dbconnection;

public class LocalBusiness {
	// Constants
    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    // Properties
    // ---------------------------------------------------------------------------------

    String state = null;
    String city = null;
    String address = null;
    String phone_number = null;
    Integer rating = null;
    String latitude = null;
    String longitude = null;
    Integer rawhtml_fk = null;
    Integer location_link_fk = null;
    String datePosted = null;
    String timePosted = null;
    String posting_body = null;
    String title = null;
    Integer duplicatePostId = null;
    
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone_number() {
		return phone_number;
	}

	public void setPhone_number(String phone_number) {
		this.phone_number = phone_number;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public Integer getRawhtml_fk() {
		return rawhtml_fk;
	}

	public void setRawhtml_fk(Integer rawhtml_fk) {
		this.rawhtml_fk = rawhtml_fk;
	}

	public Integer getDuplicatePostId() {
		return duplicatePostId;
	}

	public void setDuplicatePostId(Integer duplicatePostId) {
		this.duplicatePostId = duplicatePostId;
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
        return this.rawhtml_fk + " State:" + this.state + ", City:" + this.city + " Lat:" + this.latitude + ", Long:" + this.longitude;
    }

    // Custom methods
    public boolean isValid() {
        return this.rawhtml_fk != null;
    }
}
