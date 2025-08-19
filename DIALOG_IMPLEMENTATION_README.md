# Details Submitted Success Dialog Implementation

## Overview
This implementation creates a popup dialog that matches the UI shown in the screenshot, with functionality to copy app links and share information via WhatsApp.

## Files Created/Modified

### 1. Layout File
- **File**: `app/src/main/res/layout/dialog_details_submitted_success.xml`
- **Purpose**: Defines the UI layout for the success dialog
- **Features**:
  - Success icon with checkmark
  - Information box with driver app requirement message
  - App download link with copy functionality
  - WhatsApp share button

### 2. Drawable Resource
- **File**: `app/src/main/res/drawable/ic_whatsapp.xml`
- **Purpose**: WhatsApp icon for the share button

### 3. DialogUtils.kt Modifications
- **File**: `app/src/main/java/com/delhivery/axle/utils/DialogUtils.kt`
- **Added Functions**:
  - `showDetailsSubmittedSuccessDialog()` - Main dialog function
  - `copyToClipboard()` - Copies text to clipboard
  - `generateWhatsAppShareText()` - Generates formatted WhatsApp message
  - `shareOnWhatsApp()` - Shares text via WhatsApp with fallback

### 4. Interface
- **Interface**: `DetailsSubmittedSuccessInterface`
- **Purpose**: Callback interface for dialog events

## Usage

### Basic Usage
```kotlin
@Inject
lateinit var dialogUtils: DialogUtils

// Show the dialog
val dialog = dialogUtils.showDetailsSubmittedSuccessDialog(
    ticketId = "TKT123456",
    reportingCentre = "https://maps.google.com/?q=Mumbai+MIDC",
    reportingTime = "09:00 AM",
    playStoreLink = "https://play.google.com/store/apps/details?id=com.delhivery.driver",
    hindiVideoLink = "https://youtube.com/watch?v=hindi_video_id",
    englishVideoLink = "https://youtube.com/watch?v=english_video_id",
    dialogInterface = object : DetailsSubmittedSuccessInterface {
        override fun onDialogDismissed() {
            // Handle dialog dismissal
        }
    }
)
```

### Parameters
- `ticketId`: The ticket ID to display in the WhatsApp message
- `reportingCentre`: Google Maps link for the reporting centre
- `reportingTime`: The reporting time
- `playStoreLink`: Link to the driver app on Play Store
- `hindiVideoLink`: Link to Hindi tutorial video
- `englishVideoLink`: Link to English tutorial video
- `dialogInterface`: Callback interface for dialog events

## Features

### 1. Copy Functionality
- Clicking the "Copy" button copies the app link to clipboard
- Shows a snackbar confirmation message

### 2. WhatsApp Sharing
- Clicking "Share App link on WhatsApp" generates a formatted message
- Opens WhatsApp with the pre-filled message
- Falls back to general share if WhatsApp is not installed

### 3. WhatsApp Message Format
The generated message follows this format:
```
📲 Delhivery Driver App Required !!
You've been assigned an an Intracity Adhoc Ticket.

🧾 Ticket ID: [Ticket ID]
📍 Reporting Centre: [Google Maps link]
⏰ Reporting Time: [Time]

Use the Delhivery Driver App to start your trip and mark attendance.
📥 App Link: [Play Store link]

🎥 Mark-in / Mark-out Video: 

Hindi - [Hindi YouTube link]

English - [English YouTube link]

✅ Mark-in & Mark-out on Driver App must!
Thank you!
```

## Dependencies
- `UiUtils` - For showing snackbar messages
- `Context.CLIPBOARD_SERVICE` - For clipboard functionality
- `Intent.ACTION_SEND` - For sharing functionality

## Styling
The dialog uses existing drawable resources from the project:
- `bg_top_round_corner_pure_white` - Dialog background
- `bg_all_round_corner_light_green` - Success icon background
- `bg_all_round_corner_light_orange_v2` - Information box background
- `bg_all_round_corner_light_grey_v2` - App link text background
- `bg_all_round_corner_solid_orange` - Copy button background
- `bg_all_round_corner_solid_black` - WhatsApp button background

## Example Implementation
See `DialogUsageExample.kt` for complete usage examples. 