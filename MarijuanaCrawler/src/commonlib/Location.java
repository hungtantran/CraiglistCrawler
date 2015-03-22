package commonlib;

public class Location {
    public Integer id = null;
	public String country = null;
	public String state = null;
	public String city = null;

	public Location() {
	}

	public Location(Integer id, String country, String state, String city) throws Exception {
	    if (id == null || country == null || state == null || city == null) {
	        throw new Exception("Invalid arguments");
	    }
	       
	    this.id = id;
		this.country = country;
		this.state = state;
		this.city = city;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("Id: ");
        if (this.id != null) {
            str.append(this.id + " ");
        } else {
            str.append("(null) ");
        }
		
		str.append("Country: ");
		if (this.country != null) {
			str.append(this.country + " ");
		} else {
			str.append("(null) ");
		}

		str.append("State: ");
		if (this.state != null) {
			str.append(this.state + " ");
		} else {
			str.append("(null) ");
		}

		str.append("City: ");
		if (this.city != null) {
			str.append(this.city + " ");
		} else {
			str.append("(null) ");
		}

		return str.toString();
	}
}
