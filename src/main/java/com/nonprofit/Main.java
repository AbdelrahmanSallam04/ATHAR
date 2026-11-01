package com.nonprofit;

import com.nonprofit.model.Donation;
import com.nonprofit.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class Main {

    public static void main(String[] args) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            // Create one Donation item with its name attribute
            Donation donation = new Donation("summer Clothes Drive");
            session.persist(donation);

            tx.commit();
            System.out.println("Successfully created and saved donation: " + donation);
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
}
