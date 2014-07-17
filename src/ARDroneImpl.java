// -*- Java -*-
/*!
 * @file  ARDroneImpl.java
 * @brief ARDrone RT Component
 * @date  $Date$
 *
 * $Id$
 */

import java.awt.image.BufferedImage;

import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.CorbaPort;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.DoubleHolder;
import jp.go.aist.rtm.RTC.util.StringHolder;

import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import RTC.CameraImage;
import RTC.ReturnCode_t;
import RTC.TimedDouble;
import RTC.TimedOrientation3D;
import RTC.TimedVelocity3D;

import com.shigeodayo.ardrone.navdata.AttitudeListener;
import com.shigeodayo.ardrone.navdata.VelocityListener;
import com.shigeodayo.ardrone.video.ImageListener;

/*!
 * @class ARDroneImpl
 * @brief ARDrone RT Component
 *
 */
public class ARDroneImpl extends DataFlowComponentBase {

	/*
	 * !
	 * 
	 * @brief constructor
	 * 
	 * @param manager Maneger Object
	 */
	public ARDroneImpl(Manager manager) {
		super(manager);
		// <rtc-template block="initializer">
        m_targetVelocity_val = new TimedVelocity3D();
        m_targetVelocity = new DataRef<TimedVelocity3D>(m_targetVelocity_val);
        m_targetVelocityIn = new InPort<TimedVelocity3D>("targetVelocity", m_targetVelocity);
        m_camera_val = new CameraImage();
        m_camera = new DataRef<CameraImage>(m_camera_val);
        m_cameraOut = new OutPort<CameraImage>("camera", m_camera);
        m_orientation_val = new TimedOrientation3D();
        m_orientation = new DataRef<TimedOrientation3D>(m_orientation_val);
        m_orientationOut = new OutPort<TimedOrientation3D>("orientation", m_orientation);
        m_attitude_val = new TimedDouble();
        m_attitude = new DataRef<TimedDouble>(m_attitude_val);
        m_attitudeOut = new OutPort<TimedDouble>("attitude", m_attitude);
        m_currentVelocity_val = new TimedVelocity3D();
        m_currentVelocity = new DataRef<TimedVelocity3D>(m_currentVelocity_val);
        m_currentVelocityOut = new OutPort<TimedVelocity3D>("currentVelocity", m_currentVelocity);
        m_DroneServicePort = new CorbaPort("DroneService");
		// </rtc-template>

	}

	/*
	 * !
	 * 
	 * The initialize action (on CREATED->ALIVE transition) formaer
	 * rtc_init_entry()
	 * 
	 * @return RTC::ReturnCode_t
	 */
	@Override
	protected ReturnCode_t onInitialize() {
		// Registration: InPort/OutPort/Service
		// <rtc-template block="registration">
        // Set InPort buffers
        addInPort("targetVelocity", m_targetVelocityIn);
        
        // Set OutPort buffer
        addOutPort("camera", m_cameraOut);
        addOutPort("orientation", m_orientationOut);
        addOutPort("attitude", m_attitudeOut);
        addOutPort("currentVelocity", m_currentVelocityOut);
        
        // Set service provider to Ports
        try {
        	m_DroneServicePort.registerProvider("DroneService", "ssr.DroneService", m_droneService);
        	m_droneService.setDrone(drone);
        } catch (ServantAlreadyActive e) {
            e.printStackTrace();
        } catch (WrongPolicy e) {
            e.printStackTrace();
        } catch (ObjectNotActive e) {
            e.printStackTrace();
        }
        
        // Set service consumers to Ports
        
        // Set CORBA Service Ports
        addPort(m_DroneServicePort);
		// </rtc-template>
		bindParameter("velocityGainTranslation", m_velocityGainTranslation,
				"1.0");
		bindParameter("velocityGainRotation", m_velocityGainRotation, "1.0");
		bindParameter("use_camera", m_use_camera, "vertical");
		return super.onInitialize();
	}

	/***
	 * 
	 * The finalize action (on ALIVE->END transition) formaer
	 * rtc_exiting_entry()
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	// @Override
	// protected ReturnCode_t onFinalize() {
	// return super.onFinalize();
	// }

	/***
	 * 
	 * The startup action when ExecutionContext startup former
	 * rtc_starting_entry()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	// @Override
	// protected ReturnCode_t onStartup(int ec_id) {
	// return super.onStartup(ec_id);
	// }

	/***
	 * 
	 * The shutdown action when ExecutionContext stop former
	 * rtc_stopping_entry()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	// @Override
	// protected ReturnCode_t onShutdown(int ec_id) {
	// return super.onShutdown(ec_id);
	// }

	/***
	 * 
	 * The activated action (Active state entry action) former
	 * rtc_active_entry()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	@Override
	protected ReturnCode_t onActivated(int ec_id) {
		drone = new com.shigeodayo.ardrone.ARDrone(this.m_ipAddress.value);
		drone.connect();

		drone.addImageUpdateListener(new ImageListener() {

			@Override
			public void imageUpdated(BufferedImage arg0) {
				setImage(arg0);
			}

		});

		drone.addAttitudeUpdateListener(new AttitudeListener() {

			@Override
			public void attitudeUpdated(float pitch, float roll, float yaw,
					int attitude) {
				m_orientation.v.data.p = pitch;
				m_orientation.v.data.y = yaw;
				m_orientation.v.data.r = roll;
				m_orientationOut.write();
				m_orientationOut.setTimestamp(m_orientation_val);
				m_attitude.v.data = attitude;
				m_attitudeOut.setTimestamp(m_attitude_val);
				m_attitudeOut.write();
			}

		});

		drone.addVelocityUpdateListener(new VelocityListener() {

			@Override
			public void velocityChanged(float vx, float vy, float vz) {
				m_currentVelocity.v.data.vx = vx;
				m_currentVelocity.v.data.vy = vy;
				m_currentVelocity.v.data.vz = vz;
				m_currentVelocityOut.setTimestamp(m_currentVelocity_val);
				m_currentVelocityOut.write();
			}
		});
		
		drone.connectNav();
		drone.connectVideo();
		drone.start();

		drone.reset();
		return super.onActivated(ec_id);
	}

	/***
	 * 
	 * The deactivated action (Active state exit action) former
	 * rtc_active_exit()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	@Override
	protected ReturnCode_t onDeactivated(int ec_id) {
		drone.stop();
		drone.disconnect();
		return super.onDeactivated(ec_id);
	}

	/***
	 * 
	 * The execution action that is invoked periodically former rtc_active_do()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	@Override
	protected ReturnCode_t onExecute(int ec_id) {
		if (m_targetVelocityIn.isNew()) {
			m_targetVelocityIn.read();
			
		}
		return super.onExecute(ec_id);
	}

	/***
	 * 
	 * The aborting action when main logic error occurred. former
	 * rtc_aborting_entry()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	// @Override
	// public ReturnCode_t onAborting(int ec_id) {
	// return super.onAborting(ec_id);
	// }

	/***
	 * 
	 * The error action in ERROR state former rtc_error_do()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	@Override
	public ReturnCode_t onError(int ec_id) {
		return super.onError(ec_id);
	}

	/***
	 * 
	 * The reset action that is invoked resetting This is same but different the
	 * former rtc_init_entry()
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	@Override
	protected ReturnCode_t onReset(int ec_id) {
		return super.onReset(ec_id);
	}

	/***
	 * 
	 * The state update action that is invoked after onExecute() action no
	 * corresponding operation exists in OpenRTm-aist-0.2.0
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	// @Override
	// protected ReturnCode_t onStateUpdate(int ec_id) {
	// return super.onStateUpdate(ec_id);
	// }

	/***
	 * 
	 * The action that is invoked when execution context's rate is changed no
	 * corresponding operation exists in OpenRTm-aist-0.2.0
	 * 
	 * @param ec_id
	 *            target ExecutionContext Id
	 * 
	 * @return RTC::ReturnCode_t
	 * 
	 * 
	 */
	// @Override
	// protected ReturnCode_t onRateChanged(int ec_id) {
	// return super.onRateChanged(ec_id);
	// }
	//
	// Configuration variable declaration
	// <rtc-template block="config_declare">
    /*!
     * 
     * - Name:  velocityGainTranslation
     * - DefaultValue: 1.0
     */
    protected DoubleHolder m_velocityGainTranslation = new DoubleHolder();
    /*!
     * 
     * - Name:  velocityGainRotation
     * - DefaultValue: 1.0
     */
    protected DoubleHolder m_velocityGainRotation = new DoubleHolder();
    /*!
     * 
     * - Name:  use_camera
     * - DefaultValue: vertical
     */
    protected StringHolder m_use_camera = new StringHolder();
    /*!
     * 
     * - Name:  ipAddress
     * - DefaultValue: 192.168.1.1
     */
    protected StringHolder m_ipAddress = new StringHolder();
	// </rtc-template>

	// DataInPort declaration
	// <rtc-template block="inport_declare">
    protected TimedVelocity3D m_targetVelocity_val;
    protected DataRef<TimedVelocity3D> m_targetVelocity;
    /*!
     */
    protected InPort<TimedVelocity3D> m_targetVelocityIn;

    
	// </rtc-template>

	// DataOutPort declaration
	// <rtc-template block="outport_declare">
    protected CameraImage m_camera_val;
    protected DataRef<CameraImage> m_camera;
    /*!
     */
    protected OutPort<CameraImage> m_cameraOut;

    protected TimedOrientation3D m_orientation_val;
    protected DataRef<TimedOrientation3D> m_orientation;
    /*!
     */
    protected OutPort<TimedOrientation3D> m_orientationOut;

    protected TimedDouble m_attitude_val;
    protected DataRef<TimedDouble> m_attitude;
    /*!
     */
    protected OutPort<TimedDouble> m_attitudeOut;

    protected TimedVelocity3D m_currentVelocity_val;
    protected DataRef<TimedVelocity3D> m_currentVelocity;
    /*!
     */
    protected OutPort<TimedVelocity3D> m_currentVelocityOut;

    
	// </rtc-template>

	// CORBA Port declaration
	// <rtc-template block="corbaport_declare">
    /*!
     */
    protected CorbaPort m_DroneServicePort;
    
	// </rtc-template>

	// Service declaration
	// <rtc-template block="service_declare">
    /*!
     */
    protected DroneServiceSVC_impl m_droneService = new DroneServiceSVC_impl();
    
	// </rtc-template>

	// Consumer declaration
	// <rtc-template block="consumer_declare">
    
	// </rtc-template>

	com.shigeodayo.ardrone.ARDrone drone;
	
	BufferedImage image;
	public void setImage(BufferedImage im) {image = im;}

}
