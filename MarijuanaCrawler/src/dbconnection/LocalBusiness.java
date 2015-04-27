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
    String datePosted = null;
    String timePosted = null;
    String posting_body = null;
    String title = null;
    Integer duplicatePostId = null;
    Integer locationFk1 = null;
    Integer locationFk2 = null;
    Integer locationFk3 = null;
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

	public Integer getLocationFk1() {
		return locationFk1;
	}

	public void setLocationFk1(Integer locationFk1) {
		this.locationFk1 = locationFk1;
	}

	public Integer getLocationFk2() {
		return locationFk2;
	}

	public void setLocationFk2(Integer locationFk2) {
		this.locationFk2 = locationFk2;
	}

	public Integer getLocationFk3() {
		return locationFk3;
	}

	public void setLocationFk3(Integer locationFk3) {
		this.locationFk3 = locationFk3;
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
    	LocalBusiness otherBusiness = (LocalBusiness)other;
        if (this.address != null && otherBusiness.address != null && this.address.equals(otherBusiness.address)) {
        	return true;
        }

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
