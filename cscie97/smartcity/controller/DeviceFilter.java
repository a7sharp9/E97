package cscie97.smartcity.controller;

import java.util.Collection;

import cscie97.smartcity.model.Device;

/**
 * The functional interface allowing to filter devices according to the supplied rules
 */
public interface DeviceFilter {
	public Collection<Device <?>> filter (Controller controller);
}
