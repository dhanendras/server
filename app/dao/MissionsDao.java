package dao;

import models.Mission;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;

public class MissionsDao extends AbstractDao<Mission> {

    public MissionsDao() {
        super(Mission.class, "missions");
    }

    public List<Mission> findWaitingMissions(UUID projectId) {

        TypedQuery<Mission> query = jpaApi.em().createQuery(
                "select m " +
                        " from missions m " +
                        " where m.state = 'WAITING_FOR_FREE_DRONE' " +
                        " and m.order.project.id = :projectId " +
                        " order by m.updateAt ASC ", Mission.class);

        query.setParameter("projectId", projectId);
        return query.getResultList();
    }
}