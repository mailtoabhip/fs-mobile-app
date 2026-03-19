# Bugfix Requirements Document

## Introduction

This document specifies the requirements for fixing the HomeLoadsProgressItem loading indicator visibility issue in the loads section. The bug affects the intracity, intercity, and marketplace tabs where the loading indicator is incorrectly managed, remaining visible even when data is populated, and the tabs themselves remain visible during loading when they should be hidden.

The issue impacts user experience by showing stale data alongside a loading indicator, creating confusion about whether the app is still loading or has completed. This is a UI state management bug in the HomeLoads section that requires proper coordination between the loading state and the visibility of both the progress indicator and the filter tabs.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN user clicks on any tab (intracity/intercity/marketplace) THEN the system shows the HomeLoadsProgressItem loading indicator but keeps the filter tabs visible

1.2 WHEN data is successfully loaded for a selected tab THEN the system continues to show the HomeLoadsProgressItem loading indicator alongside the populated data

1.3 WHEN user switches from one tab to another THEN the system keeps the HomeLoadsProgressItem loading indicator visible incorrectly during and after the tab transition

1.4 WHEN the loading state is active (isLoading = true or isLoadingMore = true) THEN the system displays both the filter tabs (HomeLoadsFilterItem) and the loading indicator simultaneously

1.5 WHEN pagination is triggered by scrolling THEN the system adds a HomeLoadsProgressItem with AddUpdate operation but does not remove it after data loads

### Expected Behavior (Correct)

2.1 WHEN user clicks on any tab (intracity/intercity/marketplace) THEN the system SHALL hide all views including the search bar (HomeLoadsSearchItem) and filter tabs (HomeLoadsFilterItem) and show only the HomeLoadsProgressItem loading indicator

2.2 WHEN data is successfully loaded for a selected tab THEN the system SHALL hide the HomeLoadsProgressItem loading indicator and show the search bar and filter tabs with the loaded data

2.3 WHEN user switches from one tab to another THEN the system SHALL immediately hide the filter tabs, show only the loading indicator during the fetch, and then show the filter tabs again when data is loaded

2.4 WHEN the loading state is active (isLoading = true) for initial load THEN the system SHALL ensure the filter tabs (HomeLoadsFilterItem) are not added to the adapter items list until loading completes

2.5 WHEN pagination is triggered by scrolling THEN the system SHALL add a HomeLoadsProgressItem at the bottom of the list and remove it with a Remove operation after pagination data loads successfully

### Unchanged Behavior (Regression Prevention)

3.1 WHEN data is already loaded and user is not performing any tab switch or pagination THEN the system SHALL CONTINUE TO display the filter tabs and loaded data without any loading indicator

3.2 WHEN an error occurs during data fetching THEN the system SHALL CONTINUE TO display the error UI items (HomeLoadsWarningItem_TimeOut) and hide the loading indicator

3.3 WHEN the loading state is inactive (isLoading = false and isLoadingMore = false) and data is already loaded THEN the system SHALL CONTINUE TO show the search bar (HomeLoadsSearchItem) along with the filter tabs and loaded data

3.4 WHEN pagination loading is in progress THEN the system SHALL CONTINUE TO keep the existing loaded items visible and show the progress indicator only at the bottom of the list

3.5 WHEN user has no routes configured THEN the system SHALL CONTINUE TO show/hide the routes banner based on the hasRoutes state independently of the loading indicator

3.6 WHEN KYC verification status is "failed" and it's not a pagination request THEN the system SHALL CONTINUE TO show the HomeLoadsKycPendingItem after the filter tabs

3.7 WHEN the user scrolls through the loaded data THEN the system SHALL CONTINUE TO maintain smooth scrolling performance without flickering or UI jumps
