package dbconnection;

public class LinkQueue {
	// Constants
	// ----------------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	// Properties
	// ---------------------------------------------------------------------------------

	Integer id;
	String link;
	Integer domainTableId1;
	Integer priority;
	Integer persistent;
	String timeCrawled;
	String dateCrawled;
	String country;
	String state;
	String city;

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

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public Integer getPersistent() {
		return persistent;
	}

	public void setPersistent(Integer persistent) {
		this.persistent = persistent;
	}

	public Integer getDomainTableId1() {
		return domainTableId1;
	}

	public void setDomainTableId1(Integer domainTableId1) {
		this.domainTableId1 = domainTableId1;
	}

	public String getTimeCrawled() {
		return timeCrawled;
	}

	public void setTimeCrawled(String timeCrawled) {
		this.timeCrawled = timeCrawled;
	}

	public String getDateCrawled() {
		return dateCrawled;
	}

	public void setDateCrawled(String dateCrawled) {
		this.dateCrawled = dateCrawled;
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
		return this.id != null && this.link != null
				&& this.domainTableId1 != null && this.timeCrawled != null
				&& this.dateCrawled != null;
	}
}
