package controllers.api;

import ch.helin.messages.dto.MissionDto;
import com.google.inject.Inject;
import service.websocket.MissionWebSocketManager;
import service.websocket.WebSocketConnection;
import dao.MissionsDao;
import mappers.MissionMapper;
import models.Mission;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.LegacyWebSocket;
import play.mvc.Result;
import play.mvc.WebSocket;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MissionApiController extends Controller {

    @Inject
    private MissionsDao missionsDao;

    @Inject
    private MissionMapper missionMapper;

    @Inject
    MissionWebSocketManager webSocketManager;

    @Transactional
    public Result findByOrder(UUID orderId) {
        List<Mission> missions = missionsDao.findByOrder(orderId);
        List<MissionDto> collect = missions.stream()
            .map(missionMapper::convertToMissionDto)
            .collect(Collectors.toList());

        return ok(Json.toJson(collect));
    }

    @Transactional
    public Result show(UUID missionId) {
        Mission mission = missionsDao.findById(missionId);
        if (mission == null) {
            return forbidden("Mission not found");
        }

        MissionDto missionDto = missionMapper.convertToMissionDto(mission);
        return ok(Json.toJson(missionDto));
    }

    //We use LegacyWebSocket because new Websockets are not documented yet
    public LegacyWebSocket<String> ws(UUID missionId) {

        return WebSocket.whenReady((in, out) -> {
            WebSocketConnection webSocketConnection = new WebSocketConnection(in, out);
            webSocketManager.addWebSocketConnection(missionId, webSocketConnection);
        });
    }
}
