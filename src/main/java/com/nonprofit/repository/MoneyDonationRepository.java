package com.nonprofit.repository;

import com.nonprofit.model.Donation;
import com.nonprofit.model.MoneyDonation;
import com.nonprofit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for MoneyDonation entities.
 */
public class MoneyDonationRepository {

    /**
     * Persists a MoneyDonation (and cascades its donor if new) in one transaction.
     */
    public MoneyDonation save(MoneyDonation donation) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(donation);
            tx.commit();
            return donation;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Persists any Donation subclass.
     */
    public Donation saveDonation(Donation donation) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(donation);
            tx.commit();
            return donation;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Finds a MoneyDonation by ID.
     */
    public Optional<MoneyDonation> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(MoneyDonation.class, id));
        }
    }

    /**
     * Retrieves all money donations ordered by donation date descending.
     */
    public List<MoneyDonation> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from MoneyDonation m order by m.donatedAt desc",
                    MoneyDonation.class
            ).getResultList();
        }
    }
}

