# Testing Guide for Details Submitted Success Dialog

## 🧪 How to Test the Dialog Implementation

This guide shows you multiple ways to test the dialog implementation using the `DialogUsageExample` and test components.

## 📱 Method 1: Using TestDialogActivity (Recommended)

### Step 1: Add Activity to AndroidManifest.xml
```xml
<activity
    android:name=".ui.test.TestDialogActivity"
    android:exported="true"
    android:label="Test Dialog"
    android:theme="@style/Theme.AppCompat.Light.NoActionBar">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### Step 2: Run the App
1. Build and run the app
2. The test activity will open automatically
3. Use the three test buttons to test different scenarios

### Step 3: Test Scenarios
- **Test with Example Data**: Shows dialog with predefined example data
- **Test with Custom Data**: Shows dialog with custom test data
- **Test Different Scenarios**: Shows dialog with different city scenarios

## 🔧 Method 2: Using TestDialogFragment in Existing Activity

### Step 1: Add Fragment to Any Activity
```kotlin
// In your existing activity
class YourExistingActivity : BaseActivity<YourBinding, YourViewModel>() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Add test fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, TestDialogFragment())
                .commit()
        }
    }
}
```

### Step 2: Add Container to Layout
```xml
<FrameLayout
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## 🚀 Method 3: Direct Integration in Your Code

### Step 1: Inject DialogUsageExample
```kotlin
@Inject
lateinit var dialogUsageExample: DialogUsageExample
```

### Step 2: Call the Dialog Function
```kotlin
// In your button click or any event
binding.btnShowDialog.setOnClickListener {
    dialogUsageExample.showSuccessDialogExample()
}
```

## 🧪 Testing Checklist

### ✅ UI Testing
- [ ] Dialog appears with correct layout
- [ ] Success icon and checkmark are visible
- [ ] "Details submitted successfully!" title is displayed
- [ ] Information box shows correct message
- [ ] App link is displayed correctly
- [ ] Copy button is functional
- [ ] WhatsApp share button is functional
- [ ] Close button (X) works properly

### ✅ Functionality Testing
- [ ] **Copy Button Test**:
  - Tap the "Copy" button
  - Check if app link is copied to clipboard
  - Verify snackbar message appears: "App link copied to clipboard"
  - Paste in any text field to verify content

- [ ] **WhatsApp Share Test**:
  - Tap "Share App link on WhatsApp"
  - Verify WhatsApp opens (if installed)
  - Check if message is pre-filled correctly
  - Verify message format matches the required format
  - Test fallback if WhatsApp is not installed

- [ ] **Dialog Dismissal Test**:
  - Tap the X button to close dialog
  - Verify dialog closes properly
  - Check if callback is triggered

### ✅ Data Testing
- [ ] Test with different ticket IDs
- [ ] Test with different reporting centres
- [ ] Test with different reporting times
- [ ] Test with different app links
- [ ] Test with different video links
- [ ] Test with empty/null values (edge cases)

### ✅ WhatsApp Message Format Verification
The generated message should match this exact format:
```
📲 Delhivery Driver App Required !!
You've been assigned an an Intracity Adhoc Ticket.

🧾 Ticket ID: [Your Ticket ID]
📍 Reporting Centre: [Your Google Maps Link]
⏰ Reporting Time: [Your Time]

Use the Delhivery Driver App to start your trip and mark attendance.
📥 App Link: [Your Play Store Link]

🎥 Mark-in / Mark-out Video: 

Hindi - [Your Hindi Video Link]

English - [Your English Video Link]

✅ Mark-in & Mark-out on Driver App must!
Thank you!
```

## 🐛 Debugging Tips

### 1. Check Logs
Look for these log messages:
```
Dialog dismissed
```

### 2. Common Issues
- **Dialog not showing**: Check if activity is finishing
- **Copy not working**: Verify clipboard permissions
- **WhatsApp not opening**: Check if WhatsApp is installed
- **Binding errors**: Verify layout file names match

### 3. Testing on Different Devices
- Test on different screen sizes
- Test on devices with/without WhatsApp
- Test on different Android versions

## 📋 Sample Test Data

### Example 1: Mumbai Scenario
```kotlin
val mumbaiData = mapOf(
    "ticketId" to "MUM_TKT_001",
    "reportingCentre" to "https://maps.google.com/?q=Mumbai+Airport",
    "reportingTime" to "08:00 AM",
    "playStoreLink" to "https://play.google.com/store/apps/details?id=com.delhivery.driver.mumbai",
    "hindiVideoLink" to "https://youtube.com/watch?v=mumbai_hindi",
    "englishVideoLink" to "https://youtube.com/watch?v=mumbai_english"
)
```

### Example 2: Delhi Scenario
```kotlin
val delhiData = mapOf(
    "ticketId" to "DEL_TKT_002",
    "reportingCentre" to "https://maps.google.com/?q=Delhi+Warehouse",
    "reportingTime" to "10:30 AM",
    "playStoreLink" to "https://play.google.com/store/apps/details?id=com.delhivery.driver.delhi",
    "hindiVideoLink" to "https://youtube.com/watch?v=delhi_hindi",
    "englishVideoLink" to "https://youtube.com/watch?v=delhi_english"
)
```

## 🎯 Quick Test Commands

### For Quick Testing
```kotlin
// Add this to any activity for instant testing
binding.btnTestDialog.setOnClickListener {
    dialogUsageExample.showSuccessDialogExample()
}
```

### For Custom Data Testing
```kotlin
// Test with your own data
dialogUsageExample.showSuccessDialogWithDynamicData(
    ticketId = "YOUR_TICKET_ID",
    reportingCentre = "YOUR_GOOGLE_MAPS_LINK",
    reportingTime = "YOUR_TIME",
    playStoreLink = "YOUR_PLAY_STORE_LINK",
    hindiVideoLink = "YOUR_HINDI_VIDEO_LINK",
    englishVideoLink = "YOUR_ENGLISH_VIDEO_LINK"
)
```

## 📞 Support

If you encounter any issues during testing:
1. Check the logs for error messages
2. Verify all dependencies are properly injected
3. Ensure layout files are correctly named
4. Test on different devices/emulators 