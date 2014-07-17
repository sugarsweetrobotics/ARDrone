set(hdrs ARDrone.h
    PARENT_SCOPE
    DroneServiceSVC_impl.h
    )

install(FILES ${hdrs} DESTINATION ${INC_INSTALL_DIR}/${PROJECT_NAME_LOWER}
    COMPONENT library)

