package commonlib;

public class Location {
	public String country = null;
	public String state = null;
	public String city = null;

	public Location() {
	}

	public Location(String country, String state, String city) {
		this.country = country;
		this.state = state;
		this.city = city;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();

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
