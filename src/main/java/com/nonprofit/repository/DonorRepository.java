package com.nonprofit.repository;

import com.nonprofit.model.Address;
import com.nonprofit.model.Donor;
import com.nonprofit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Donor and associated Address entities.
 */
public class DonorRepository {

    /**
     * Finds the most recent donor with eagerly loaded addresses.
     */
    public Donor findLatestDonorWithAddresses() {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            List<Donor> donors = session.createQuery(
                    "select distinct d from Donor d left join fetch d.savedAddresses order by d.id desc",
                    Donor.class
            ).getResultList();
            tx.commit();
            return donors.isEmpty() ? null : donors.get(0);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds a donor by ID.
     */
    public Optional<Donor> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(Donor.class, id));
        }
    }

    /**
     * Persists or updates a donor.
     */
    public Donor save(Donor donor) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(donor);
            tx.commit();
            return donor;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /**
     * Persists a new address linked to the specified donor.
     */
    public Address saveAddress(Donor donor, Address address) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Donor managedDonor = session.get(Donor.class, donor.getUserId());
            address.setUser(managedDonor);
            managedDonor.addAddress(address);
            session.persist(address);
            tx.commit();
            return address;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }
}

