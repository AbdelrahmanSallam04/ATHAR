# ATHAR

A desktop app for a nonprofit to manage donations, built with JavaFX.

## Features
- Sign up  and log in
- Dashboard and user profile
- Money donations, with a payment factory for different payment methods
- In-kind donations
- Donation types pages
- Event Management 

## Architecture (MVC)

ATHAR uses the **Model–View–Controller** pattern, plus a service layer and a repository layer:

| Layer | Package / folder | Role |
|---|---|---|
| **View** | `src/main/resources/com/nonprofit/view/*.fxml` | JavaFX screens (login, dashboard, donations, profile) |
| **Controller** | `com.nonprofit.controller` | Handles user input and updates the view |
| **Model** | `com.nonprofit.model` | Domain entities (`Donor`, `Donation`, `MoneyDonation`, `Receipt`, ...) |
| **Service** | `com.nonprofit.service` | Business logic (`AuthService`, `DonationService`, ...) |
| **Repository** | `com.nonprofit.repository` | Database access with Hibernate |

**Flow:** View → Controller → Service → Repository → Database, and the result comes back to the View.

Design patterns used: **Factory Method** for payment methods (`factory/payment`) and **Strategy** for communication (`pattern/strategy`).
