# Cycle Management Feature Implementation Plan

This plan introduces a dedicated management screen for all budget cycles, accessible from a new summary card on the dashboard. This allows users to easily view, edit, or delete any historical or active cycle to resolve overlaps.

## User Review Required

> [!NOTE]
> The "Edit" functionality from the dashboard header will remain as a quick action for the *active* cycle, while the new screen provides a comprehensive list for management.

## Proposed Changes

### [Core/Navigation]

#### [MODIFY] [Navigation.kt](file:///D:/Projects/Android/FundFlow/app/src/main/java/com/shawon/fundflow/core/common/Navigation.kt)
- Add `CycleManagement` to the `Screen` sealed interface.

---

### [UI/CycleManagement]

#### [NEW] [CycleManagementViewModel.kt](file:///D:/Projects/Android/FundFlow/app/src/main/java/com/shawon/fundflow/ui/cycle/CycleManagementViewModel.kt)
- Create a ViewModel to expose all budget cycles from the repository.

#### [NEW] [CycleManagementScreen.kt](file:///D:/Projects/Android/FundFlow/app/src/main/java/com/shawon/fundflow/ui/cycle/CycleManagementScreen.kt)
- Implement a screen that lists all cycles.
- Show key info: Name, Dates, Amount, Status (Active/Closed).
- Provide an "Edit" button for each item that navigates to `BudgetSetup(cycleId)`.

---

### [UI/Dashboard]

#### [MODIFY] [DashboardScreen.kt](file:///D:/Projects/Android/FundFlow/app/src/main/java/com/shawon/fundflow/ui/dashboard/DashboardScreen.kt)
- Add a new `FundFlowCard` in the dashboard content displaying "Total Cycles" and an icon.
- Make this card clickable to navigate to the `CycleManagement` screen.

---

### [Main]

#### [MODIFY] [MainActivity.kt](file:///D:/Projects/Android/FundFlow/app/src/main/java/com/shawon/fundflow/MainActivity.kt)
- Add the `CycleManagement` composable to the `NavHost`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Verify the new "Total Cycles" card appears on the dashboard.
- Click the card and ensure it navigates to the list of all cycles.
- Click "Edit" on a closed or historical cycle and verify it opens the Setup screen with correct data.
- Modify/Delete a cycle and ensure the list updates correctly upon returning.
