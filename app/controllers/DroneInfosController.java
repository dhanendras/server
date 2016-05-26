package controllers;

import ch.helin.messages.dto.message.DroneInfoMessage;
import com.google.inject.Inject;
import commons.WebSockets.MissionWebSocketManager;
import dao.DroneDao;
import dao.DroneInfoDao;
import mappers.DroneInfoMapper;
import models.Drone;
import models.DroneInfo;
import models.Mission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.jpa.JPAApi;

import java.util.UUID;


public class DroneInfosController {
    private static final Logger logger = LoggerFactory.getLogger(DroneInfosController.class);

    @Inject
    private DroneDao droneDao;

    @Inject
    private DroneInfoDao droneInfoDao;

    @Inject
    private DroneInfoMapper droneInfoMapper;

    @Inject
    private JPAApi jpaApi;

    @Inject
    MissionWebSocketManager webSocketManager;

    public void onDroneInfoReceived(UUID droneId, DroneInfoMessage droneInfoMessage) {
        jpaApi.withTransaction(()-> {
            DroneInfo droneInfo = droneInfoMapper.convertToDroneInfo(droneInfoMessage);

            Drone drone = droneDao.findById(droneId);
            droneInfo.setDrone(drone);

            Mission currentMission = drone.getCurrentMission();

            if(currentMission != null) {
                droneInfo.setMission(currentMission);
                webSocketManager.sendDroneInfoToConnectedClients(currentMission.getId(), droneInfoMessage);
            }

            droneInfoDao.persist(droneInfo);
        });
    }
}
