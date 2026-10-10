# 📚 Smart Library

**Manage • Discover • Grow**

A complete Android library management system with dedicated dashboards for students and admins — built with Java, SQLite, and Firebase Authentication.

Smart Library digitizes the full lending lifecycle: searching, issuing, returning, fines, memberships, reservations, and online catalog discovery — replacing manual registers and rigid legacy tools with a single real-time app.

---

## ✨ Features

### 🎓 Student Experience
- **Search & Discover** — live-filtered search by title, author, or ID across the full catalog
- **Issue & Return** — membership-aware borrowing limits with automatic overdue fine calculation
- **Membership Tiers** — Basic & Premium plans with different loan durations and borrowing limits, upgradeable in-app
- **Digital Wallet** — pay fines in-app from a wallet, topped up via a demo payment form (EasyPaisa/Card)
- **Smart Reservations** — reserve a checked-out title; it's automatically issued to you the moment a copy is returned
- **Online Catalog Search** — search the Open Library API and request titles not yet in the local collection
- **Firebase Authentication** — verified sign-up (email verification required before login) and a Forgot Password flow

### 🔑 Admin Panel
- **Manage Resources** — add, edit, and delete books with full quantity tracking (total vs. available copies)
- **Manage Users** — search, view, and manage every registered student account
- **Issued Resources** — live view of every book currently checked out, with due dates and fines
- **Acquisition Requests** — accept or reject book requests submitted from the student's online search
- **Cancel Memberships** — reset any user's membership tier directly from the panel
- **Secured Access** — separate password-gated admin login, isolated from the student app

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Platform | Android (Java, Android Studio) |
| Local Data | SQLite (`DatabaseHelper` — users, books, issued books, reservations, requests) |
| Authentication | Firebase Authentication (email/password, email verification, password reset) |
| External API | [Open Library](https://openlibrary.org/developers/api) — online catalog search |
| UI | Fragment-based multi-tab dashboards, `RecyclerView` adapters, Material `TabLayout` |

---


Built solo, one feature at a time. 📖
