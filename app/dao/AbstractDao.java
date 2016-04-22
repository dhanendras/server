package dao;

import com.google.inject.Inject;
import models.BaseEntity;
import play.db.jpa.JPAApi;

import java.util.List;
import java.util.UUID;

public abstract class AbstractDao<T extends BaseEntity> {
    private String tableName;
    private Class<T> entityClass;

    @Inject
    protected JPAApi jpaApi;

    public AbstractDao(Class<T> entityClass, String tableName) {
        this.entityClass = entityClass;
        this.tableName = tableName;
    }

    public List<T> findAll() {
        String sql = "select e from " + tableName + " e order by e.id";
        return jpaApi.em()
                .createQuery(sql, entityClass)
                .getResultList();
    }

    public T findById(UUID id) {
        return jpaApi.em().find(entityClass, id);
    }

    public void persist(T entity) {
        jpaApi.em().persist(entity);
    }

    public void delete(T entity) {
        jpaApi.em().remove(entity);
    }

    public void remove(T entity) {
        delete(entity);
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }
}

