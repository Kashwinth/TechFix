# Plan: Restore Customer Dashboard Access

## Problem

Customer login currently routes to the shared `HomeActivity` landing page, so `CustomerDashboardActivity` is not reachable through the normal login flow. The customer dashboard still contains customer-specific features such as current repair status, branch actions, technician viewing, and repair gallery access.

## Approach

1. Update only customer login routing in `LoginActivity` so a successful customer login opens `CustomerDashboardActivity`.
2. Keep manager, admin, and staff routing unchanged; they must continue opening `AdminDashboardActivity`.
3. Update the shared customer navigation Home action so it opens `CustomerDashboardActivity` for logged-in customers instead of the public `HomeActivity`.
4. Keep guest users on `HomeActivity`; guest Profile can continue opening Login.
5. Preserve the existing customer dashboard bottom navigation and customer-specific buttons.
6. Avoid changing databases, authentication validation, services, inventory, themes, or the public home layout.

## Files to change

- `app/src/main/java/com/example/techfix/activities/LoginActivity.java`
- `app/src/main/java/com/example/techfix/utils/CustomerNavigation.java`

## Verification

- Customer login opens `CustomerDashboardActivity`.
- Customer dashboard displays the logged-in customer name and current repair area.
- Dashboard Home remains stable when tapped.
- Services, Book, and Profile navigation still work.
- Manager/admin/staff login still opens the admin dashboard.
- Guest Home remains unchanged.
- Run `./gradlew :app:assembleDebug` after the change.
