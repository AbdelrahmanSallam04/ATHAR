package com.nonprofit.repository;

import com.nonprofit.model.User;
import com.nonprofit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Optional;

/**
 * Repository layer for User entity database CRUD operations using Hibernate ORM.
 */
public class UserRepository {

    public void save(User user) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(user);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to save user: " + e.getMessage(), e);
        }
    }

    public Optional<User> findByContactInfo(String contactInfo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User u WHERE LOWER(u.contactInfo) = LOWER(:contactInfo)", User.class
            );
            query.setParameter("contactInfo", contactInfo.trim());
            return query.uniqueResultOptional();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by contact info: " + e.getMessage(), e);
        }
    }

    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find user by ID: " + e.getMessage(), e);
        }
    }

    public boolean existsByContactInfo(String contactInfo) {
        return findByContactInfo(contactInfo).isPresent();
    }
}
