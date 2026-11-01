# Database Setup & Environment Management

All database configurations and environment files live directly inside this **`db/`** directory.

---

## 1. Active Configuration File

Your Java application and Docker compose both read from:
* **[`db/.env`](file:///d:/Desktop/ATHAR/db/.env)**

---

## 2. How to Switch Environments

Simply copy the content of the example template you want into **`db/.env`** (or copy the file over):

### Option A: Online TiDB Cloud (Shared with teammates)
* **Copy command:**
  ```powershell
  Copy-Item db\.env.online.example db\.env
  ```
* **Password:** Make sure your TiDB password is in `db/.env`.
* **Web GUI:** [tidbcloud.com](https://tidbcloud.com) (Chat2Query / SQL Editor)

### Option B: Local Docker (Offline development)
* **Copy command:**
  ```powershell
  Copy-Item db\.env.local.example db\.env
  ```
* **Start containers:**
  ```powershell
  cd db
  docker compose up -d
  ```
* **Web GUI:** [http://localhost:8080](http://localhost:8080) (phpMyAdmin, auto-login)
