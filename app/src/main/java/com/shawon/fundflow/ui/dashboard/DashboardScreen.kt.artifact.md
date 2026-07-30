# Confirmation for End Cycle

Add a confirmation dialog when the user clicks the "End Cycle" button on the Dashboard screen.

## Proposed Changes

### DashboardScreen

#### [MODIFY] [DashboardScreen.kt](file:///E:/Learn_and_Build/FundFlow/app/src/main/java/com/shawon/fundflow/ui/dashboard/DashboardScreen.kt)

- Add a state variable `showEndCycleConfirmation` to track whether the confirmation dialog should be shown.
- Wrap the `viewModel.closeCycle(state.cycle.id)` call in the `TextButton`'s `onClick` to instead set `showEndCycleConfirmation = true`.
- Implement an `AlertDialog` that shows when `showEndCycleConfirmation` is true.
- The dialog will have:
    - Title: "End Cycle?"
    - Text: "Are you sure you want to end the current cycle? You won't be able to add more expenses to it."
    - Confirm button: "End Cycle", which calls `viewModel.closeCycle(state.cycle.id)` and sets `showEndCycleConfirmation = false`.
    - Dismiss button: "Cancel", which sets `showEndCycleConfirmation = false`.

## Verification Plan

### Manual Verification
- Deploy the app.
- Go to the Dashboard screen.
- Click "End Cycle".
- Verify that a confirmation dialog appears.
- Click "Cancel" and verify the cycle does not end.
- Click "End Cycle" again and then "End Cycle" in the dialog, and verify the cycle ends.
