package dbconnection;

public class LocationLink {
	// Constants
	// ----------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	// Properties
	// ---------------------------------------------------------------------------------

	Integer id;
	String link;
	String country;
	String state;
	String city;
	Integer numPositivePagesFound;
	String latitude;
	String longitude;
	String nelatitude;
	String nelongitude;
	String swlatitude;
	String swlongitude;
    Integer locationFk1 = null;
    Integer locationFk2 = null;
    Integer locationFk3 = null;
    String dateUpdated;

	// Getters/setters
	// ----------------------------------------------------------------------------

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

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

    public Integer getNumPositivePagesFound() {
        return numPositivePagesFound;
    }

    public void setNumPositivePagesFound(Integer numPositivePagesFound) {
        this.numPositivePagesFound = numPositivePagesFound;
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

    public String getNelatitude() {
        return nelatitude;
    }

    public void setNelatitude(String nelatitude) {
        this.nelatitude = nelatitude;
    }

    public String getNelongitude() {
        return nelongitude;
    }

    public void setNelongitude(String nelongitude) {
        this.nelongitude = nelongitude;
    }

    public String getSwlatitude() {
        return swlatitude;
    }

    public void setSwlatitude(String swlatitude) {
        this.swlatitude = swlatitude;
    }

    public String getSwlongitude() {
        return swlongitude;
    }

    public void setSwlongitude(String swlongitude) {
        this.swlongitude = swlongitude;
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

	public String getDateUpdated() {
		return dateUpdated;
	}

	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
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
		return this.link;
	}

	// Custom methods
	public boolean isValid() {
		return this.id != null && this.link != null && this.country != null
				&& this.state != null && this.city != null;
	}
}
