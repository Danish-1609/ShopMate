# ShopMate — Offline Retail Inventory Manager

A fully offline Android application built for retail shop owners to manage inventory, sales, pricing, and restocking — no internet or paid APIs required.

---

## Architecture Overview

```
ShopMate/
├── app/src/main/java/com/shopmate/
│   ├── ShopMateApp.kt                  # Application class, DI wiring
│   ├── data/
│   │   ├── entities/                   # Room @Entity data classes
│   │   │   ├── Product.kt              # Products with stock status
│   │   │   ├── Sale.kt                 # Sale & SaleTransaction
│   │   │   └── Entities.kt             # Category, User, RestockAlert
│   │   ├── dao/                        # Room @Dao interfaces
│   │   │   ├── ProductDao.kt
│   │   │   ├── SaleDao.kt
│   │   │   └── UserDao.kt              # UserDao + RestockAlertDao
│   │   ├── database/
│   │   │   └── ShopDatabase.kt         # Room singleton DB
│   │   └── repository/                 # Repository pattern
│   │       ├── ProductRepository.kt
│   │       ├── SaleRepository.kt
│   │       └── UserRepository.kt       # + AlertRepository
│   ├── models/
│   │   └── Models.kt                   # SalesSummary, CartItem, DashboardData…
│   ├── ui/
│   │   ├── auth/AuthActivity.kt        # Login / Setup / Skip
│   │   ├── MainActivity.kt             # Bottom nav host
│   │   ├── dashboard/DashboardFragment.kt
│   │   ├── inventory/
│   │   │   ├── InventoryFragment.kt
│   │   │   └── AddEditProductActivity.kt
│   │   ├── sales/
│   │   │   ├── SalesFragment.kt
│   │   │   └── RecordSaleActivity.kt
│   │   └── reports/ReportsFragment.kt
│   ├── adapters/
│   │   ├── ProductAdapter.kt
│   │   └── Adapters.kt                 # Sale/Cart/LowStock/Category adapters
│   ├── viewmodels/
│   │   ├── DashboardViewModel.kt
│   │   ├── InventoryViewModel.kt
│   │   ├── SalesViewModel.kt
│   │   ├── ReportsViewModel.kt         # + AuthViewModel + BackupViewModel
│   │   └── ViewModelFactory.kt
│   └── utils/
│       ├── Extensions.kt               # toCurrency(), DateUtils, etc.
│       ├── NotificationHelper.kt       # Low stock + daily summary notifications
│       ├── StockCheckWorker.kt         # WorkManager background job
│       ├── BootReceiver.kt             # Reschedule on boot
│       └── ExportHelper.kt             # CSV + JSON backup/restore
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9 |
| Architecture | MVVM + Repository |
| Database | Room 2.6 (SQLite) |
| Async | Kotlin Coroutines + LiveData |
| UI | Material Design 3, ViewBinding |
| Navigation | Jetpack Navigation Component |
| Charts | MPAndroidChart (free, offline) |
| Background | WorkManager |
| Security | SHA-256 password hashing |
| Serialization | Gson |

---

## Features

### Authentication
- **Local login** with SHA-256 hashed passwords stored in Room
- **Account setup** on first launch (shop name + username + password)
- **Skip login** for single-user convenience — persists via SharedPreferences
- Secure, no cloud dependency

### Dashboard
- Greeting with time of day + shop name
- Live stats: Total Products, Low Stock count, Today's Revenue
- Quick action cards: Add Product, Record Sale, Inventory, Reports
- Low stock alert strip (top 5 items highlighted)
- Today's revenue + total inventory value cards

### Inventory Management
- Full CRUD for products (name, category, barcode, qty, unit, cost price, selling price, min threshold, description)
- Real-time search across name / category / barcode
- Filter chips: All / Low Stock / Out of Stock / In Stock
- Category chips (dynamic, from DB)
- Sort by: Name, Price, Stock (asc/desc)
- Color-coded stock indicator bars (green / orange / red)
- Restock dialog with quantity input
- Profit margin preview when entering prices
- Soft delete (products are deactivated, not destroyed)

### Sales
- Product picker with real-time search
- Cart with quantity stepper (+/−) and live line totals
- Discount field
- Payment method selection: Cash / UPI / Card
- Atomic transaction: inserts Sale + SaleTransaction, decrements stock, triggers restock alerts
- Today's transaction history with time, amount, profit

### Restock Alerts
- Auto-triggered when stock falls at or below threshold after a sale
- In-app visual: orange low-stock cards on Dashboard + Inventory badges
- Android local notification (high priority channel) listing affected products
- WorkManager job runs every 3 hours; re-scheduled on device boot

### Reports
- Period tabs: Today / This Week / This Month
- Summary cards: Revenue, Profit, Transactions, Inventory Value
- **Line chart** — daily sales trend (MPAndroidChart)
- **Pie chart** — revenue breakdown by category
- **Horizontal bar chart** — top 10 selling products
- Category breakdown list with color dots and progress bars

### Export & Backup
- **Products CSV** — all active products with full details
- **Sales CSV** — complete sales history
- **Full JSON backup** — products + sales in a single restorable file
- Saved to `Documents/ShopMate/` (app-specific external storage on Android 10+)
- Restore from JSON backup supported

---

## Setup & Build Instructions

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 24–34

### Steps

```bash
# 1. Clone or extract the project
cd ShopMate

# 2. Open in Android Studio
# File → Open → select the ShopMate/ folder

# 3. Sync Gradle (it will download all dependencies automatically)
# The JitPack repository is declared in settings.gradle for MPAndroidChart

# 4. Run on emulator or device (API 24+)
# Build → Run 'app'
```

### First Launch
1. App opens to **Set Up Your Shop** screen
2. Enter shop name, username, password → **Create Account**
3. OR tap **Continue without login** for quick access
4. Dashboard loads with zero state
5. Tap **Load Sample Data** (via Reports → Export section) or add products manually

---

## Key Design Decisions

- **No internet permission** — 100% offline, no analytics, no ads
- **Room over raw SQLite** — type safety, LiveData integration, migration support
- **MVVM + Repository** — clear separation; ViewModels survive rotation
- **MediatorLiveData in InventoryViewModel** — search + filter + sort compose cleanly without extra queries
- **Soft delete** — products are flagged `is_active = 0` so historical sales data remains intact
- **Sale snapshots** — `productName` and `costPrice` are stored on each Sale row so reports remain accurate even after product edits
- **WorkManager for background checks** — battery-friendly, survives process death and device reboots
- **SHA-256 password hashing** — simple but sufficient for a local single-user app

---

## Permissions

| Permission | Reason |
|---|---|
| `POST_NOTIFICATIONS` | Low stock and daily summary alerts |
| `WRITE_EXTERNAL_STORAGE` (≤ API 28) | CSV / JSON export |
| `RECEIVE_BOOT_COMPLETED` | Reschedule WorkManager after reboot |
| `VIBRATE` | Notification vibration |

---

## Extending the App

- **Barcode scanning** — integrate ZXing (`com.journeyapps:zxing-android-embedded`) wired to `etBarcode` in AddEditProductActivity
- **PIN lock** — add a PIN entry dialog in AuthActivity using the existing `pin` field in the User entity
- **Customer ledger** — add a `Customer` entity and link to `SaleTransaction`
- **Multi-user** — the `users` table already supports multiple rows; extend AuthViewModel to pick account on login
- **Thermal printer** — format receipt string from CartItems and send via Bluetooth using `BluetoothSocket`

---

*Built with Kotlin · Room · Material Design 3 · MPAndroidChart · WorkManager*
