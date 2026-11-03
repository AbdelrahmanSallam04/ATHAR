package com.nonprofit.repository;

import com.nonprofit.model.*;
import com.nonprofit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for In-Kind (Clothes & Food) donations.
 */
public class InKindDonationRepository {

    /**
     * Persists in-kind donations (Clothes, Food, or combined batches) atomically.
     */
    public Receipt saveInKindDonations(Donor donor, List<ClothingItem> clothingItems,
                                       List<FoodItem> foodItems, FulfillmentMethod method,
                                       Address address) {
        if ((clothingItems == null || clothingItems.isEmpty()) &&
                (foodItems == null || foodItems.isEmpty())) {
            throw new IllegalStateException("At least one clothing or food item must be added.");
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Donor managedDonor = session.get(Donor.class, donor.getUserId());
            if (managedDonor == null) {
                managedDonor = donor;
                session.persist(managedDonor);
            }

            Address managedAddress = null;
            if (method == FulfillmentMethod.COURIER_PICKUP && address != null) {
                if (address.getId() != null) {
                    managedAddress = session.get(Address.class, address.getId());
                } else {
                    address.setUser(managedDonor);
                    session.persist(address);
                    managedAddress = address;
                }
            }

            ClothesDonation clothesDonation = null;
            Receipt clothesReceipt = null;
            if (clothingItems != null && !clothingItems.isEmpty()) {
                clothesDonation = new ClothesDonation();
                clothesDonation.setDonor(managedDonor);
                clothesDonation.setFulfillmentMethod(method);
                clothesDonation.setPickupAddress(managedAddress);
                for (ClothingItem item : clothingItems) {
                    clothesDonation.addItem(item);
                }
                clothesReceipt = clothesDonation.processDonation();
                session.persist(clothesDonation);
            }

            FoodDonation foodDonation = null;
            Receipt foodReceipt = null;
            if (foodItems != null && !foodItems.isEmpty()) {
                foodDonation = new FoodDonation();
                foodDonation.setDonor(managedDonor);
                foodDonation.setFulfillmentMethod(method);
                foodDonation.setPickupAddress(managedAddress);
                for (FoodItem item : foodItems) {
                    foodDonation.addItem(item);
                }
                foodReceipt = foodDonation.processDonation();
                session.persist(foodDonation);
            }

            tx.commit();

            // Build unified receipt
            if (clothesReceipt != null && foodReceipt != null) {
                String combinedDesc = clothesReceipt.getDescription() + " | " + foodReceipt.getDescription();
                String details = clothesReceipt.getDetails();
                return new Receipt(managedDonor.getName(), 0.0,
                        method == FulfillmentMethod.COURIER_PICKUP ? "Courier Pickup" : "Office Drop-off",
                        combinedDesc, details);
            } else if (clothesReceipt != null) {
                return clothesReceipt;
            } else {
                return foodReceipt;
            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Could not complete donation persistence: " + e.getMessage(), e);
        }
    }

    /**
     * Persists a ClothesDonation.
     */
    public ClothesDonation saveClothesDonation(ClothesDonation donation) {
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
     * Persists a FoodDonation.
     */
    public FoodDonation saveFoodDonation(FoodDonation donation) {
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

    public Optional<ClothesDonation> findClothesDonationById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(ClothesDonation.class, id));
        }
    }

    public Optional<FoodDonation> findFoodDonationById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(FoodDonation.class, id));
        }
    }

    public List<ClothesDonation> findAllClothesDonations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from ClothesDonation c order by c.donatedAt desc",
                    ClothesDonation.class
            ).getResultList();
        }
    }

    public List<FoodDonation> findAllFoodDonations() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "from FoodDonation f order by f.donatedAt desc",
                    FoodDonation.class
            ).getResultList();
        }
    }
}

