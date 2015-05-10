package dbconnection;

public class PostingType {
	// Constants
    // ----------------------------------------------------------------------------------

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    // Properties
    // ---------------------------------------------------------------------------------

    Integer id = null;
    Integer postingLocationId = null;
    Integer strainId = null;
    
    // Getters/setters
    // ----------------------------------------------------------------------------
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPostingLocationId() {
		return postingLocationId;
	}

	public void setPostingLocationId(Integer postingLocationId) {
		this.postingLocationId = postingLocationId;
	}

	public Integer getStrainId() {
		return strainId;
	}

	public void setStrainId(Integer strainId) {
		this.strainId = strainId;
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
        return this.id + " " + this.postingLocationId + " " + this.strainId;
    }

    // Custom methods
    public boolean isValid() {
        return this.postingLocationId != null && this.strainId != null;
    }
}
