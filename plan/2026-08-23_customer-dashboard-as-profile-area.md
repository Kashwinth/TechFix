# Plan: Use Customer Dashboard as the Logged-In Account Area

## Desired flow

Keep the existing rich `HomeActivity` landing page visible both before and after customer login. Use the bottom **Profile** tab as the entry point to the logged-in customer dashboard, while preserving the existing personal profile page and its actions.

```text
Guest Home → Profile → Login
Customer login → HomeActivity
Logged-in Home → Profile → CustomerDashboardActivity
Customer Dashboard → Profile → ProfileActivity
ProfileActivity → Track Repairs / Logout
```

Manager/admin/staff login continues to open `AdminDashboardActivity`.

## Existing functionality to preserve

`ProfileActivity` must remain available and must keep:

- The **Track Repairs** button, opening `RepairHistoryActivity`.
- The **Logout** button, clearing the session and returning to `LoginActivity`.
- The existing customer name and email display.

The customer dashboard’s current customer-specific features must also remain:

- Current repair status
- Repair gallery
- Branch and technician access
- Nearest branch action
- Logout behavior

## Approach

1. Keep successful customer login routing to `HomeActivity`.
2. Update `CustomerNavigation` so Profile resolves by session:
   - logged out → `LoginActivity`
   - logged in → `CustomerDashboardActivity`
3. Treat `CustomerDashboardActivity` as the logged-in account/dashboard destination and highlight its Profile tab rather than Home.
4. Keep the dashboard’s Profile action opening `ProfileActivity`.
5. Leave `ProfileActivity`’s Track Repairs and Logout actions unchanged.
6. Keep Home on every customer screen routed to `HomeActivity`, so the rich landing page remains the Home page after login.
7. Leave Services and Book routing unchanged.
8. Do not alter databases, repair logic, admin behavior, themes, or the public Home UI.

## Files to change

- `app/src/main/java/com/example/techfix/utils/CustomerNavigation.java`
- `app/src/main/java/com/example/techfix/activities/CustomerDashboardActivity.java`
- Confirm only, likely no change: `app/src/main/java/com/example/techfix/activities/LoginActivity.java`
- Preserve without removing actions: `app/src/main/java/com/example/techfix/activities/ProfileActivity.java`

## Verification

- Guest opens Home and Profile sends them to Login.
- Customer login returns to the same rich Home page.
- Logged-in Home Profile opens `CustomerDashboardActivity`.
- Customer dashboard shows customer name, current repair, gallery, branches, technicians, and its existing actions.
- Customer dashboard Profile opens `ProfileActivity`.
- Profile still shows customer name and email.
- Profile **Track Repairs** still opens repair history.
- Profile **Logout** still clears the session and returns to Login.
- Dashboard Home returns to the rich Home page.
- Services and Book work from all shared bars.
- Manager/admin/staff login still opens the admin dashboard.
- Run `./gradlew :app:assembleDebug`.
