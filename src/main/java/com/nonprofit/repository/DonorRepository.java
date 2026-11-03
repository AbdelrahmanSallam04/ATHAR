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
     * Updates the donor's name and contact info; returns the saved donor
     * (with addresses loaded).
     */
    public Donor updateProfile(Long donorId, String name, String contactInfo) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Donor managedDonor = session.get(Donor.class, donorId);
            managedDonor.setName(name);
            managedDonor.setContactInfo(contactInfo);
            tx.commit();
            return managedDonor;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    /** True if any donation uses this address for courier pickup. */
    public boolean isAddressUsedByDonation(Long addressId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "select count(d) from Donation d where d.pickupAddress.id = :addressId", Long.class)
                    .setParameter("addressId", addressId)
                    .getSingleResult();
            return count > 0;
        }
    }

    /**
     * Removes one of the donor's saved addresses.
     */
    public void deleteAddress(Long donorId, Long addressId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Donor managedDonor = session.get(Donor.class, donorId);
            // orphanRemoval on savedAddresses deletes the row
            managedDonor.getSavedAddresses().removeIf(address -> address.getId().equals(addressId));
            tx.commit();
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

