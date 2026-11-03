package com.nonprofit.service;

import com.nonprofit.model.*;
import com.nonprofit.util.HibernateUtil;
import com.nonprofit.util.UserSession;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 * Service orchestrating In-Kind (Food & Clothes) donation processing,
 * address management, and database persistence.
 */
public class InKindDonationService {

    /**
     * Retrieves the current donor from the session, or from the database.
     */
    public Donor getActiveDonor() {
        if (UserSession.getCurrentDonor() != null) {
            return UserSession.getCurrentDonor();
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            List<Donor> donors = session.createQuery(
                    "select distinct d from Donor d left join fetch d.savedAddresses order by d.id desc",
                    Donor.class
            ).getResultList();

            Donor active;
            if (!donors.isEmpty()) {
                active = donors.get(0);
            } else {
                active = new Donor("Registered Donor", "01000000000");
                session.persist(active);
            }
            tx.commit();
            UserSession.setCurrentDonor(active);
            return active;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            Donor fallback = new Donor("Registered Donor", "01000000000");
            UserSession.setCurrentDonor(fallback);
            return fallback;
        }
    }

    /**
     * Persists a new address linked to the active donor.
     */
    public Address saveNewAddress(Donor donor, Address address) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Donor managedDonor = session.get(Donor.class, donor.getUserId());
            address.setUser(managedDonor);
            managedDonor.addAddress(address);
            session.persist(address);
            tx.commit();
            return address;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Could not save address: " + e.getMessage(), e);
        }
    }

    /**
     * Submits in-kind donations. Handles clothes-only, food-only, or combined batches atomically.
     */
    public Receipt submitInKindDonations(Donor donor, List<ClothingItem> clothingItems,
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
            throw new RuntimeException("Could not complete donation: " + e.getMessage(), e);
        }
    }
}
