package com.nonprofit.repository;

import com.nonprofit.model.ClothesDonation;
import com.nonprofit.model.Donation;
import com.nonprofit.model.FoodDonation;
import com.nonprofit.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

/**
 * Read access to donations of every type (money, food, clothes).
 */
public class DonationRepository {

    /**
     * All donations of one donor, newest first. Each element is the real
     * subclass (MoneyDonation, FoodDonation or ClothesDonation).
     * Item lists are loaded before the session closes so they can be shown later.
     */
    public List<Donation> findByDonor(Long donorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Donation> donations = session.createQuery(
                            "from Donation d where d.donor.id = :donorId order by d.donatedAt desc",
                            Donation.class)
                    .setParameter("donorId", donorId)
                    .getResultList();

            // Load all item lists with one query per type instead of one per donation.
            // The same session returns the same objects, so their lists get filled in.
            if (donations.stream().anyMatch(FoodDonation.class::isInstance)) {
                session.createQuery("select distinct f from FoodDonation f left join fetch f.items "
                                + "where f.donor.id = :donorId", FoodDonation.class)
                        .setParameter("donorId", donorId)
                        .getResultList();
            }
            if (donations.stream().anyMatch(ClothesDonation.class::isInstance)) {
                session.createQuery("select distinct c from ClothesDonation c left join fetch c.items "
                                + "where c.donor.id = :donorId", ClothesDonation.class)
                        .setParameter("donorId", donorId)
                        .getResultList();
            }

            return donations;
        }
    }
}
