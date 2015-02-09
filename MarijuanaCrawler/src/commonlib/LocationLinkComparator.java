package commonlib;

import java.util.Comparator;

import dbconnection.LocationLink;

public class LocationLinkComparator implements Comparator<LocationLink> {
    @Override
    public int compare(LocationLink x, LocationLink y) {
        return y.getNumPositivePagesFound() - x.getNumPositivePagesFound();
    }
}
