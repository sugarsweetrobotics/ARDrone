// -*- Java -*-
/*!
 * @file ARDrone.java
 * @date $Date$
 *
 * $Id$
 */

import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.RTObject_impl;
import jp.go.aist.rtm.RTC.RtcDeleteFunc;
import jp.go.aist.rtm.RTC.RtcNewFunc;
import jp.go.aist.rtm.RTC.RegisterModuleFunc;
import jp.go.aist.rtm.RTC.util.Properties;

/*!
 * @class ARDrone
 * @brief ARDrone RT Component
 */
public class ARDrone implements RtcNewFunc, RtcDeleteFunc, RegisterModuleFunc {

//  Module specification
//  <rtc-template block="module_spec">
    public static String component_conf[] = {
    	    "implementation_id", "ARDrone",
    	    "type_name",         "ARDrone",
    	    "description",       "ARDrone RT Component",
    	    "version",           "1.0.0",
    	    "vendor",            "Sugar Sweet Robotics",
    	    "category",          "Drone",
    	    "activity_type",     "STATIC",
    	    "max_instance",      "1",
    	    "language",          "Java",
    	    "lang_type",         "compile",
            // Configuration variables
            "conf.default.velocityGainTranslation", "1.0",
            "conf.default.velocityGainRotation", "1.0",
            "conf.default.use_camera", "vertical",
            // Widget
            "conf.__widget__.velocityGainTranslation", "text",
            "conf.__widget__.velocityGainRotation", "text",
            "conf.__widget__.use_camera", "radio",
            // Constraints
            "conf.__constraints__.use_camera", "(vertial, horizontal)",
    	    ""
            };
//  </rtc-template>

    public RTObject_impl createRtc(Manager mgr) {
        return new ARDroneImpl(mgr);
    }

    public void deleteRtc(RTObject_impl rtcBase) {
        rtcBase = null;
    }
    public void registerModule() {
        Properties prop = new Properties(component_conf);
        final Manager manager = Manager.instance();
        manager.registerFactory(prop, new ARDrone(), new ARDrone());
    }
}
