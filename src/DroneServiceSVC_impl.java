import com.shigeodayo.ardrone.ARDrone;

import ssr.DroneServicePOA;

// -*-Java-*-
/*!
 * @file  DroneServiceSVC_impl.java
 * @brief Service implementation code of DroneService.idl
 *
 */
/*!
 * @class DroneServiceSVC_impl
 * Example class implementing IDL interface ssr::DroneService
 */
public class DroneServiceSVC_impl extends DroneServicePOA{
    
    private ARDrone drone;

	public DroneServiceSVC_impl() {
        // Please add extra constructor code here.
    }

    /*
     * Methods corresponding to IDL attributes and operations
     */
    public void takeOff() {
        // Please insert your code here and remove the following warning pragma
        drone.takeOff();
    }

    public void landing() {
        // Please insert your code here and remove the following warning pragma
        drone.landing();
    }

	public void setDrone(ARDrone drone) {
		this.drone = drone;
	}

//  End of example implementational code
}
