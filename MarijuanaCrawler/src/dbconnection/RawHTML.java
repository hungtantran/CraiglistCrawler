package dbconnection;

import java.io.Serializable;

public class RawHTML implements Serializable {
	// Constants
	// ----------------------------------------------------------------------------------

	private static final long serialVersionUID = 1L;

	// Properties
	// ---------------------------------------------------------------------------------

	private Integer id;
	private String url;
	private String html;
	private Short positive;
	private Short predict1;
	private Short predict2;
	private String country;
	private String state;
	private String city;
	private String alt_quantities;
	private String alt_prices;

	// Getters/setters
	// ----------------------------------------------------------------------------

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Short getPositive() {
		return positive;
	}

	public void setPositive(Short positive) {
		this.positive = positive;
	}

	public Short getPredict1() {
		return predict1;
	}

	public void setPredict1(Short predict1) {
		this.predict1 = predict1;
	}

	public Short getPredict2() {
		return predict2;
	}

	public void setPredict2(Short predict2) {
		this.predict2 = predict2;
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
		return this.html;
	}
	
	// Custom methods
	public boolean isValid() {
		return this.id != null && this.url != null && this.html != null;
	}
}
