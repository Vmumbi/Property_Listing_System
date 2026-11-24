# Member 7: Integration, Validation & QA Documentation

**Role:** Integration, Validation & Quality Assurance Engineer  
**Engineer:** AlvisKamau (`alvis.kamau@strathmore.edu`)  
**Branch:** `feature/member-7-integration-qa`  
**CAT 2 Stage:** 5 (Waits for Members 2, 3, 4, 5, 6)  

---

## 1. Overview & Assigned Tasks

Member 7 is responsible for uniting all independent components into a coherent, production-ready system:
- **Cross-Component Navigation & Integration:** Connect Member 4 (Authentication & Dashboard), Member 5 (RecyclerView & Interactive Forms), Member 6 (AI Valuation & WorkManager), and Member 2 (Room Database Console) into a single, unified `nav_graph.xml`.
- **Data Validation & Error States:** Enforce business integrity rules across forms (title length, price boundaries, Kenyan/international phone formats, email regex, negative counts) with field-level visual error states.
- **Persistence Verification:** Ensure properties created in UI forms persist safely through `PropertyViewModel` into Member 2's Room database and update reactive StateFlow collectors.
- **QA Test Suite:** Provide automated integration tests verifying validation rules, user auth, and repository persistence.
- **Coordination & Presentation Runbook:** Structured demo script to guarantee a seamless presentation during evaluation.

---

## 2. Integration Architecture & Unified Navigation

```
                       [ LoginFragment ] (Member 4)
                              │
               ┌──────────────┴──────────────┐
               ▼                             ▼
      [ RegisterFragment ]           [ DashboardFragment ] (Central Hub)
               │                             │
               └──────────────┬──────────────┘
                              │
         ┌────────────────────┼────────────────────┬────────────────────┐
         ▼                    ▼                    ▼                    ▼
[ PropertyListFragment ] [ AddEditProperty ] [ Member6Fragment ] [ Member2Fragment ]
  (Member 5 - Recycler)    (Member 5 Form)     (Member 6 API)       (Member 2 Room)
```

### Components Integrated
1. **`res/navigation/nav_graph.xml`:** Declares all 6 application destinations with proper backstack management (`popUpTo` inclusive on login/logout).
2. **`fragment_dashboard.xml` & `DashboardFragment.kt`:** Serves as the central command hub. Dynamically observes Room database count in real-time and provides one-click navigation to every member's subsystem.
3. **`Member6Fragment.kt` & `Member2Fragment.kt`:** Fragment bridges hosting Jetpack Compose screens seamlessly inside the XML navigation hierarchy via `ComposeView`.

---

## 3. Input Validation & Error Handling Matrix

Implemented in `PropertyValidator.kt` and `UserValidator.kt`:

| Field / Entity | Rule Enforced | Error State Displayed |
| :--- | :--- | :--- |
| **Title** | Must be $\ge$ 3 characters | `etTitle.error = "Title must be at least 3 characters long."` |
| **Price** | Must be $> 0$ and $\le 10,000,000,000$ | `etPrice.error = "Listing price must be greater than zero."` |
| **Address** | Cannot be blank | `etAddress.error = "Property address is required."` |
| **Phone** | Kenyan (`+254...`, `07...`, `01...`) or intl regex | `etContactPhone.error = "Contact phone number format is invalid."` |
| **Email** | Standard RFC-compliant email regex | `etContactEmail.error = "Contact email format is invalid."` |
| **Beds / Baths** | Cannot be negative integers | Error mapped directly to field counter |
| **Login Auth** | Valid email + password $\ge 6$ chars | Focus shifted to invalid input with descriptive hint |
| **Registration** | Passwords must match exactly | `etRegPassword.error = "Passwords do not match."` |

---

## 4. Automated QA Test Suite (`Member7IntegrationQATest.kt`)

Member 7 authored a dedicated test suite with 10 automated test cases:
- `validProperty_passesCompleteValidation()`: Confirms fully filled properties pass validation.
- `invalidProperty_titleValidation_triggersFieldError()`: Rejects short titles.
- `invalidProperty_priceValidation_zeroOrNegative_triggersFieldError()`: Rejects zero and negative prices.
- `invalidProperty_kenyanPhoneValidation()`: Validates Kenyan prefixes (`+2547...`, `07...`, `01...`).
- `invalidProperty_emailValidation()`: Rejects invalid email structures.
- `userLoginValidation_rejectsInvalidInputs()`: Validates auth form constraints.
- `userRegistrationValidation_verifiesPasswordMatch()`: Confirms password confirmation logic.
- `repository_savesAndRetrievesValidatedProperty()`: Confirms end-to-end saving and retrieval through repository.
- `repository_updateAndToggleFavorite_reflectedInState()`: Confirms in-place updates.

---

## 5. CAT 2 Final Presentation & Demo Runbook

Follow these exact steps during your group presentation:

1. **Launch App:**
   - App opens to **Login Screen** (`LoginFragment`).
   - Enter invalid email or short password $\rightarrow$ tap **Login** $\rightarrow$ show validation error state.
   - Enter valid email (`admin@strathmore.edu`) & password (`password123`) $\rightarrow$ tap **Login**.
2. **Dashboard Overview (Hub):**
   - Point out the **Live Room Database counter card** showing total properties cached.
3. **Demonstrate Member 5 & 2 (CRUD & Database):**
   - Tap **"Browse Properties (RecyclerView)"** $\rightarrow$ displays list with pre-populated properties.
   - Tap the Floating Action Button **(+)** $\rightarrow$ opens **Add New Property**.
   - Attempt to save with empty fields $\rightarrow$ show **Member 7 validation errors**.
   - Fill in details: *"Luxury Karen Bungalow"*, price *45,000,000*, address *"Karen Road"*, phone *"+254712345678"*.
   - Tap **Save Property** $\rightarrow$ confirms *"Property added to database!"* and immediately appears in the RecyclerView!
4. **Demonstrate Member 6 (Valuation & WorkManager):**
   - Tap **"Valuation & WorkManager Sync"** from Dashboard.
   - Tap **Calculate Valuation Estimate** $\rightarrow$ shows algorithmic market price and rental estimate.
   - Tap **Sync Now** $\rightarrow$ triggers WorkManager background sync notification.
   - Tap **Schedule Alert (10s)** $\rightarrow$ shows viewing appointment notification.
   - Tap **Open in Maps** $\rightarrow$ launches Google Maps pin.
5. **Conclude & Logout:**
   - Return to Dashboard $\rightarrow$ tap **Logout** $\rightarrow$ returns to Login screen.
