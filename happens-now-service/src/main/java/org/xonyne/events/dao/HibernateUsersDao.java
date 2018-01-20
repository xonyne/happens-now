package org.xonyne.events.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.xonyne.events.model.Event;
import org.xonyne.events.model.User;

@Repository
public class HibernateUsersDao extends AbstractDao implements UsersDao {

    private Logger logger = org.slf4j.LoggerFactory.getLogger(HibernateUsersDao.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public User find(Long userId) {
        logger.debug("find user with id:" + userId);
        try {
            return entityManager.find(User.class, userId);
        } catch (RuntimeException re) {
            logger.error("error in find User, " + re.getMessage(), re);
            throw re;
        }
    }

    @Override
    @Transactional
    public User findOrPersist(User user) {
        User storedUser = entityManager.find(User.class, user.getId());
        if (storedUser == null) {
            persistObject(user);
            storedUser = entityManager.find(User.class, user.getId());
        } else {
            storedUser.setIsStale(true);
        }

        return storedUser;
    }

    @Transactional
    @Override
    public User find(String userName, String password) {
        SessionFactory sessionFactory = getSession();
        Session session = null;
        User user = null;

        try {
            session = sessionFactory.openSession();
            Query query = session.createQuery(
                    "from User u where u.userName=:userName AND password=:password ");
            query.setParameter("userName", userName).setParameter("password", password);
            user = (User) query.uniqueResult();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return user;
    }

}
