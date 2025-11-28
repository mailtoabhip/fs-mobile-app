# AWS S3 Document Upload Paths - Backend Reference Document

## Overview
This document provides a comprehensive reference for all AWS S3 paths used by the Android mobile application for uploading and downloading documents/images. The mobile app uses AWS S3 for storing various types of documents including KYC documents, payment proofs, trip PODs, and profile images.

## AWS Configuration

### Bucket Configuration by Environment

| Environment | Bucket Name | Region | AWS Account ID (Target) |
|------------|-------------|--------|------------------------|
| **Production** | `orion-service-prod-mum` | `ap-south-1` | `347095250728` |
| **Development** | `orion-service` | `ap-southeast-1` | `086341552770` |
| **UAT** | `orion-uat` | `us-east-1` | `086341552770` |

### Base URL Format
The base URL for accessing S3 objects follows this pattern:
```
https://{bucket-name}.s3.{region}.amazonaws.com/
```

**Examples:**
- Production: `https://orion-service-prod-mum.s3.ap-south-1.amazonaws.com/`
- Development: `https://orion-service.s3.ap-southeast-1.amazonaws.com/`
- UAT: `https://orion-uat.s3.us-east-1.amazonaws.com/`

## Document Upload and Download - Complete Class Reference

This section provides detailed information about all Activities and Fragments that use AWS S3 for uploading and downloading documents/images. Each entry includes the AWS path, identifiers, parameters, and how the upload/download process works.

---

## Upload Functionality by Class

All documents are uploaded using S3 object keys (paths). The same path structure is used as the identifier for both upload and download operations.

### 1. Trip POD Uploads
**Class:** `UploadImageActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `trips/temp/vendor_pod/{transactionId}/{filename}.jpg`

**Details:**
- **Purpose:** Temporary storage for Proof of Delivery (POD) images before final processing
- **Path Construction:**
  ```kotlin
  val awsPath = "trips/temp/vendor_pod/${viewModel.transactionId}/" + uploadImageName + ".jpg"
  ```
- **Path Structure:** 
  - Base: `trips/temp/vendor_pod/`
  - Transaction ID: Dynamic transaction identifier from `viewModel.transactionId`
  - Filename: `uploadImageName` variable (generated with timestamp)
  - Extension: Always `.jpg`
- **Upload Method:** `awsUtils.startUpload(delegationToken, awsPath, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API via `getDelegationToken()`
  - `awsPath`: Full S3 object key path
  - `file`: Local file object to upload
  - `this`: AWSProgressInterface listener
- **Example Path:** `trips/temp/vendor_pod/TRX123456789/IMG_1699123456789.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - receives the `awsPath` as parameter
- **Failure Callback:** `onAWSFailure()`

---

### 2. Docket Updates
**Class:** `DocketUpdateActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `trips/vendor_pod/docket/{filename}.jpg`

**Details:**
- **Purpose:** Docket images uploaded by vendors
- **Path Construction:**
  ```kotlin
  val awsPath = "trips/vendor_pod/docket/$uploadImageName.jpg"
  ```
- **Path Structure:**
  - Base: `trips/vendor_pod/docket/`
  - Filename: `uploadImageName` variable (generated with timestamp)
  - Extension: Always `.jpg`
- **Upload Method:** `awsUtils.startUpload(delegationToken, awsPath, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `awsPath`: Full S3 object key path
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `trips/vendor_pod/docket/docket_1699123456789.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - stores path in `viewModel.imageUrl`
- **Failure Callback:** `onAWSFailure()`

---

### 3. Share Rate Documents
**Class:** `ShareRateActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/sharerate/{filename}`

**Details:**
- **Purpose:** Documents uploaded for share rate verification
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/sharerate/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/sharerate/`
  - Filename: `uploadImageName` variable
  - Filename Format: `IMG_{timestamp}.{extension}` (e.g., `IMG_1699123456789.jpg`)
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `loadboard/sharerate/IMG_1699123456789.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `viewModel.documentProofUrl` and `uploadArray`
- **Failure Callback:** `onAWSFailure()`
- **Note:** After upload, the path is stored in `uploadArray` as `Pair(path.replace(awsPath, ""), fileSize)`

---

### 4. Profile Pictures
**Class:** `ProfileDetailsActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/profile/{filename}.jpg`

**Details:**
- **Purpose:** User profile pictures
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/profile/$uploadImageName.jpg"
  ```
- **Path Structure:**
  - Base: `loadboard/profile/`
  - Filename: `uploadImageName` variable (generated with timestamp)
  - Extension: Always `.jpg`
- **Upload Method:** `awsUtils.startUpload(delegationToken, awsPath, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `awsPath`: Full S3 object key path
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `loadboard/profile/profile_1699123456789.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - stores path in `viewModel.imageUrl`
- **Failure Callback:** `onAWSFailure()`

---

### 5. Payment Documents
**Class:** `PaymentDetailsActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/payment/{filename}`

**Details:**
- **Purpose:** Payment verification documents (Account Proof and 194C Declaration)
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/payment/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/payment/`
  - **Account Proof Filename:** `account_proof_{timestamp}_{phoneNumber}.{extension}`
  - **194C Declaration Filename:** `194C_{timestamp}_{phoneNumber}.{extension}`
- **Filename Generation:**
  - Account Proof: `"account_proof_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"`
  - 194C: `"194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"`
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Examples:**
  - Account Proof: `loadboard/payment/account_proof_1699123456789_9876543210.jpg`
  - 194C Declaration: `loadboard/payment/194C_1699123456789_9876543210.pdf`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `uploadArray` or `uploadArray1` based on document type
- **Failure Callback:** `onAWSFailure()`
- **Verification:** After upload, documents are sent for verification via `sendDocForVerification()` which constructs full URLs: `s3url + awsPath + filename`
- **Note:** The backend can identify document type by checking if the filename contains `account_proof` or `194C`.

---

### 6. Bank Details (194C Documents)
**Class:** `BankDetailsActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/payment/{filename}`

**Details:**
- **Purpose:** 194C declaration documents for bank details
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/payment/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/payment/`
  - Filename: `194C_{timestamp}_{phoneNumber}.jpg`
- **Filename Generation:** `"194C_" + System.currentTimeMillis() + "_" + userPrefs.phoneNumber + ".jpg"`
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `loadboard/payment/194C_1699123456789_9876543210.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `uploadArray`
- **Failure Callback:** `onAWSFailure()`
- **Verification:** Documents sent for verification via `sendDocForVerification()` with full URLs

---

### 7. Identity Verification Documents
**Class:** `IdentityVerificationActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/iv/{filename}`

**Details:**
- **Purpose:** Identity verification documents (PAN, Aadhaar, etc.)
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/iv/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/iv/`
  - Filename: `{docType}{timestamp}.{extension}`
  - Document types vary based on verification type (PAN, Aadhaar, etc.)
- **Filename Generation:** `docType + System.currentTimeMillis() + "." + imageScopedFile.extension`
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `loadboard/iv/PAN_1699123456789.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `uploadArray`
- **Failure Callback:** `onAWSFailure()`
- **Verification:** Documents sent for verification via `sendDocForVerification()` with full URLs

---

### 8. GST Verification Documents
**Class:** `GstVerificationActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/gst/{filename}`

**Details:**
- **Purpose:** GST certificate and related documents
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/gst/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/gst/`
  - Filename: `GST_{timestamp}.{extension}`
- **Filename Generation:** `"GST_" + System.currentTimeMillis() + ".jpg"` or with extension from file
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `loadboard/gst/GST_1699123456789.pdf`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `uploadArray`
- **Failure Callback:** `onAWSFailure()`
- **Verification:** Documents sent for verification via `sendDocForVerification()` with full URLs

---

### 9. Address Verification Documents
**Classes:** 
- `CommunicationAddressActivity.kt` (Activity)
- `AddressActivity.kt` (Activity)  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/address/{filename}`

**Details:**
- **Purpose:** Address verification documents
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/address/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/address/`
  - Filename: `Address_{timestamp}.{extension}`
- **Filename Generation:** `"Address_" + System.currentTimeMillis() + "." + imageScopedFile.extension`
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `loadboard/address/Address_1699123456789.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `uploadArray`
- **Failure Callback:** `onAWSFailure()`
- **Note:** Both activities use the same path structure and upload logic

---

### 10. Aadhaar Verification Documents
**Class:** `AadhaarVerificationActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/aadhaar/{filename}`

**Details:**
- **Purpose:** Aadhaar card verification documents
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/aadhaar/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/aadhaar/`
  - Filename: `Aadhaar_{timestamp}.{extension}`
- **Filename Generation:** `"Aadhaar_" + System.currentTimeMillis() + ".jpg"` or with extension from file
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Example Path:** `loadboard/aadhaar/Aadhaar_1699123456789.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `uploadArray`
- **Failure Callback:** `onAWSFailure()`
- **Verification:** Documents sent for verification via `sendDocForVerification()` with full URLs

---

### 11. Business Verification Documents (LR/RC)
**Class:** `BusinessVerificationActivity.kt`  
**Type:** Activity  
**Functionality:** Upload Only

**AWS Path Pattern:** `loadboard/lr/{filename}`

**Details:**
- **Purpose:** Business verification documents (Lorry Receipt - LR, Registration Certificate - RC)
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/lr/"
  val path = "$awsPath$uploadImageName"
  ```
- **Path Structure:**
  - Base: `loadboard/lr/`
  - **LR Document Filename:** `Lr_doc_{phoneNumber}.{extension}`
  - **RC Document Filename:** `RC_doc_{phoneNumber}.{extension}`
- **Filename Generation:**
  - LR: `"Lr_doc_" + userPrefs.phoneNumber + "." + imageScopedFile.extension`
  - RC: `"RC_doc_" + userPrefs.phoneNumber + "." + imageScopedFile.extension`
- **Upload Method:** `awsUtils.startUpload(delegationToken, path, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained from backend API
  - `path`: Full S3 object key (`awsPath + uploadImageName`)
  - `file`: Local file object
  - `this`: AWSProgressInterface listener
- **Examples:**
  - LR Document: `loadboard/lr/Lr_doc_9876543210.pdf`
  - RC Document: `loadboard/lr/RC_doc_9876543210.jpg`
- **Success Callback:** `onAWSSuccess(path: String)` - adds to `uploadArray`
- **Failure Callback:** `onAWSFailure()`
- **Verification:** Documents sent for verification via `sendDocForVerification()` with full URLs
- **Note:** The backend can identify document type by checking if the filename starts with `Lr_doc_` or `RC_doc_`.

---

## Download Functionality by Class

### How Downloads Work

1. **Full URL to Path Conversion:**
   - When the mobile app receives a full S3 URL (e.g., `https://orion-service-prod-mum.s3.ap-south-1.amazonaws.com/loadboard/payment/document.jpg`), it strips the base URL to get the S3 object key.
   - The S3 object key (path) is then used as the identifier for download.

2. **Path as Identifier:**
   - The same path structure used for uploads is used as the identifier for downloads.
   - Example: If a document was uploaded to `loadboard/payment/account_proof_123.jpg`, the same path is used to download it.

3. **Download Process:**
   - Mobile app requests a delegation token from backend
   - Backend provides temporary AWS credentials
   - Mobile app uses the S3 object key (path) to download the file

---

### Download Classes Reference

#### 1. Trip POD Downloads
**Classes:**
- `TripDetailsActivity.kt` (Activity)
- `SearchActivity.kt` (Activity)
- `HomePodsFragment.kt` (Fragment)

**Functionality:** Download Only

**Download Process:**
- **Source:** `podUrl` from backend API response (`HomeTripsItemData.podUrl` or `tripDetail.podUrl`)
- **Path Extraction:** `podUrl` is used directly (can be full URL or S3 object key)
- **Download Method:** `awsUtils.startDownload(delegationToken, awsPath, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained via `getDelegationToken(awsPath, file)`
  - `awsPath`: The `podUrl` from API (used as-is, no processing)
  - `file`: Local file object created based on file extension
  - `this`: AWSProgressInterface listener
- **File Creation:**
  ```kotlin
  // Based on podUrl extension
  {transactionId}_pod.pdf
  {transactionId}_pod.png
  {transactionId}_pod.jpg
  ```
- **Success Callback:** `onAWSSuccess(path: String)` - opens file with appropriate viewer
- **Failure Callback:** `onAWSFailure()`
- **Supported Formats:** `.pdf`, `.png`, `.jpg`, `.jpeg`
- **Backend Requirement:** Return `podUrl` in API response as S3 object key (path) or full URL

---

#### 2. Profile Image Downloads
**Class:** `MyProfileActivity.kt` (Activity)

**Functionality:** Download Only

**Download Process:**
- **Source:** `viewModel.userPrefs.profileImageUrl` from backend/user preferences
- **Path Extraction:** URL used directly (assumed to be S3 object key or full URL)
- **Download Method:** `awsUtils.startDownload(delegationToken, awsPath, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained via `getDownloadDelegationToken(profileImageUrl, file)`
  - `awsPath`: The `profileImageUrl` value
  - `file`: Local file created as `{timestamp}_profile.jpg`
  - `this`: AWSProgressInterface listener
- **File Creation:** `{timestamp}_profile.jpg` in app's documents directory
- **Success Callback:** `onAWSSuccess(path: String)` - displays profile image
- **Failure Callback:** `onAWSFailure()`
- **Backend Requirement:** Store profile image URL in user preferences/API response

---

#### 3. KYC Documents Downloads
**Class:** `KycDocumentsFragment.kt` (Fragment)

**Functionality:** Download Only

**Download Process:**
- **Source:** Document URLs from backend API (`DocDetailData.docUrl`)
- **Path Extraction:**
  ```kotlin
  // For viewing/downloading
  item.data.key().replace(awsUtils.awsBasePath(), "")
  // For fetching details
  data.docUrl.replace(awsUtils.awsBasePath(), "")
  ```
- **Download Method:** `awsUtils.startDownload(delegationToken, awsPath, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained via `getDownloadDelegationToken(awsPath, file)`
  - `awsPath`: S3 object key after stripping base URL
  - `file`: Local file created based on URL filename
  - `this`: AWSProgressInterface listener
- **File Creation:**
  - For downloads: `{timestamp}{filename}` in Downloads directory
  - For viewing: `{timestamp}/{filename}` in app's documents directory
- **Success Callback:** `onAWSSuccess(path: String)` - displays document or shows success message
- **Failure Callback:** `onAWSFailure()`
- **Backend Requirement:** Return full S3 URLs in `documentUrls` array or `docUrl` field

---

#### 4. Reward Proof Downloads
**Class:** `YourRewardsFragment.kt` (Fragment)

**Functionality:** Download Only

**Download Process:**
- **Source:** `data.proofUrl?.get(0)` from `YourRewardsItemData` API response
- **Path Extraction:**
  ```kotlin
  data.proofUrl?.get(0)?.replace(awsUtils.awsBasePath(), "")
  ```
- **Download Method:** `awsUtils.startDownload(delegationToken, awsPath, file, this)`
- **Parameters:**
  - `delegationToken`: Obtained via `getDownloadDelegationToken(awsPath, file)`
  - `awsPath`: S3 object key after stripping base URL
  - `file`: Local file created as `{timestamp}{filename}` in Downloads directory
  - `this`: AWSProgressInterface listener
- **File Creation:** `{timestamp}{filename}` in public Downloads directory
- **Success Callback:** `onAWSSuccess(path: String)` - shows success message
- **Failure Callback:** `onAWSFailure()`
- **Backend Requirement:** Return full S3 URLs in `proofUrl` array

---

## File Naming Conventions

### Timestamp Format
All timestamps in filenames use `System.currentTimeMillis()` which returns milliseconds since epoch (Unix timestamp in milliseconds).

### Common Patterns:
- `{prefix}_{timestamp}.{extension}` - Most common pattern
- `{prefix}_{timestamp}_{phoneNumber}.{extension}` - For payment/bank documents
- `{prefix}_{phoneNumber}.{extension}` - For business verification documents

### Supported File Extensions
- Images: `.jpg`, `.jpeg`, `.png`
- Documents: `.pdf`

---

## Technical Implementation Details

### Upload Process
1. Mobile app generates a unique filename based on document type
2. Constructs the full S3 path (object key)
3. Requests delegation token from backend API
4. Backend provides temporary AWS credentials (access key, secret key, session token)
5. Mobile app uploads file to S3 using AWS SDK
6. Upload uses `BucketOwnerFullControl` ACL

### Download Process
1. Mobile app receives document URL (full S3 URL or path)
2. Strips base URL if full URL is provided
3. Requests delegation token from backend API
4. Backend provides temporary AWS credentials
5. Mobile app downloads file from S3 using the path as object key

### S3 ACL
- **Upload ACL:** `BucketOwnerFullControl`
- This ensures the bucket owner has full control over uploaded objects

---

## Path Summary Table

| Document Type | Base Path | Filename Pattern | Example |
|--------------|-----------|------------------|---------|
| Trip POD (Temp) | `trips/temp/vendor_pod/{transactionId}/` | `{filename}.jpg` | `trips/temp/vendor_pod/TRX123/IMG_123.jpg` |
| Docket | `trips/vendor_pod/docket/` | `{filename}.jpg` | `trips/vendor_pod/docket/docket_123.jpg` |
| Share Rate | `loadboard/sharerate/` | `IMG_{timestamp}.{ext}` | `loadboard/sharerate/IMG_123.jpg` |
| Profile | `loadboard/profile/` | `{filename}.jpg` | `loadboard/profile/profile_123.jpg` |
| Payment - Account Proof | `loadboard/payment/` | `account_proof_{ts}_{phone}.{ext}` | `loadboard/payment/account_proof_123_987.jpg` |
| Payment - 194C | `loadboard/payment/` | `194C_{ts}_{phone}.{ext}` | `loadboard/payment/194C_123_987.pdf` |
| Identity Verification | `loadboard/iv/` | `{docType}{ts}.{ext}` | `loadboard/iv/PAN_123.jpg` |
| GST Verification | `loadboard/gst/` | `GST_{ts}.{ext}` | `loadboard/gst/GST_123.pdf` |
| Address Verification | `loadboard/address/` | `Address_{ts}.{ext}` | `loadboard/address/Address_123.jpg` |
| Aadhaar Verification | `loadboard/aadhaar/` | `Aadhaar_{ts}.{ext}` | `loadboard/aadhaar/Aadhaar_123.jpg` |
| Business - LR | `loadboard/lr/` | `Lr_doc_{phone}.{ext}` | `loadboard/lr/Lr_doc_987.pdf` |
| Business - RC | `loadboard/lr/` | `RC_doc_{phone}.{ext}` | `loadboard/lr/RC_doc_987.jpg` |

---

## Complete Class Summary

### Upload Classes (11 Activities)
1. `UploadImageActivity.kt` - Trip POD uploads
2. `DocketUpdateActivity.kt` - Docket images
3. `ShareRateActivity.kt` - Share rate documents
4. `ProfileDetailsActivity.kt` - Profile pictures
5. `PaymentDetailsActivity.kt` - Payment documents (account_proof & 194C)
6. `BankDetailsActivity.kt` - Bank 194C documents
7. `IdentityVerificationActivity.kt` - Identity verification
8. `GstVerificationActivity.kt` - GST verification
9. `CommunicationAddressActivity.kt` - Address verification
10. `AddressActivity.kt` - Address verification
11. `AadhaarVerificationActivity.kt` - Aadhaar verification
12. `BusinessVerificationActivity.kt` - Business verification (LR/RC)

### Download Classes (4 Activities + 2 Fragments)
1. `TripDetailsActivity.kt` - POD downloads
2. `SearchActivity.kt` - POD downloads
3. `MyProfileActivity.kt` - Profile image downloads
4. `HomePodsFragment.kt` - POD downloads (Fragment)
5. `KycDocumentsFragment.kt` - KYC document downloads (Fragment)
6. `YourRewardsFragment.kt` - Reward proof downloads (Fragment)

---

## Backend Integration Notes

### For Backend Developers:

1. **Delegation Token API:**
   - **Endpoint:** The mobile app calls a delegation token API with the AWS Account ID (Target)
   - **Request Parameter:** AWS Account ID from `AWSConfig.Target.value()`
     - Production: `347095250728`
     - Development: `086341552770`
     - UAT: `086341552770`
   - **Response Format:** Backend should return:
     ```json
     {
       "delegationToken": {
         "accessKey": "string",
         "secretKey": "string",
         "sessionToken": "string"
       }
     }
     ```
   - **Token Permissions:** Tokens must have read/write permissions for the respective S3 buckets

2. **URL Storage in Database:**
   - When storing document URLs in database, you can store either:
     - **Full S3 URL:** `https://bucket.s3.region.amazonaws.com/path/to/file.jpg`
     - **S3 Object Key (path):** `path/to/file.jpg`
   - Mobile app handles both formats:
     - For downloads: Strips base URL if full URL is provided
     - For uploads: Uses path directly

3. **Path Validation:**
   - All upload paths should start with one of these base paths:
     - `trips/temp/vendor_pod/`
     - `trips/vendor_pod/docket/`
     - `loadboard/sharerate/`
     - `loadboard/profile/`
     - `loadboard/payment/`
     - `loadboard/iv/`
     - `loadboard/gst/`
     - `loadboard/address/`
     - `loadboard/aadhaar/`
     - `loadboard/lr/`
   - Validate paths match expected patterns before processing

4. **Document Type Identification:**
   - **Payment Documents:** Check filename for:
     - `account_proof` - Account proof document
     - `194C` - 194C declaration document
   - **Business Documents:** Check filename for:
     - `Lr_doc_` - Lorry Receipt document
     - `RC_doc_` - Registration Certificate document
   - **Identity Documents:** Check path prefix:
     - `loadboard/iv/` - Identity verification documents

5. **API Response Requirements:**

   **For POD Downloads:**
   - Include `podUrl` field in trip/transaction response
   - Format: S3 object key (preferred) or full S3 URL
   - Example: `trips/temp/vendor_pod/TRX123/pod.jpg`

   **For Profile Images:**
   - Include `profileImageUrl` in user profile response
   - Format: S3 object key or full S3 URL
   - Example: `loadboard/profile/profile_123.jpg`

   **For KYC Documents:**
   - Include `documentUrls` array or `docUrl` field
   - Format: Array of full S3 URLs
   - Example: `["https://bucket.s3.region.amazonaws.com/loadboard/iv/PAN_123.jpg"]`

   **For Reward Proofs:**
   - Include `proofUrl` array in rewards response
   - Format: Array of full S3 URLs
   - Example: `["https://bucket.s3.region.amazonaws.com/loadboard/rewards/proof_123.pdf"]`

6. **File Access & Security:**
   - Ensure proper IAM policies are configured for delegation tokens
   - Tokens should have:
     - **Read permissions:** For download operations
     - **Write permissions:** For upload operations
     - **Path-based access controls:** Consider restricting access to specific path prefixes
   - Upload ACL: `BucketOwnerFullControl` (set by mobile app)

7. **Error Handling:**
   - If delegation token request fails, mobile app shows: "Please try again"
   - If download fails, mobile app shows: "Couldn't complete download, please try after sometime"
   - If upload fails, mobile app shows: "Image processing failed, please try again" or similar

8. **File Size Considerations:**
   - Mobile app tracks file sizes in KB
   - File sizes are stored in `uploadArray` as `Pair(filename, sizeInKB)`
   - Backend may want to validate file sizes before processing

---

## Questions or Updates

For any questions or updates to this document, please contact the mobile development team.

---

## Quick Reference: Upload vs Download

### Upload Operations
- **Total Classes:** 12 Activities
- **Common Pattern:** All use `awsUtils.startUpload(delegationToken, awsPath, file, listener)`
- **Path Source:** Constructed in mobile app based on document type
- **Identifier:** S3 object key (path) returned in success callback

### Download Operations
- **Total Classes:** 4 Activities + 2 Fragments
- **Common Pattern:** All use `awsUtils.startDownload(delegationToken, awsPath, file, listener)`
- **Path Source:** From backend API response (URLs or paths)
- **Identifier:** S3 object key extracted from URL or used directly

---

## Common Parameters Reference

### Upload Parameters
```kotlin
awsUtils.startUpload(
    delegationToken: DelegationToken,  // From backend API
    awsPath: String,                   // S3 object key (path)
    file: File,                        // Local file to upload
    listener: AWSProgressInterface     // Callback interface
)
```

### Download Parameters
```kotlin
awsUtils.startDownload(
    delegationToken: DelegationToken,  // From backend API
    awsPath: String,                    // S3 object key (path)
    file: File,                         // Local file destination
    listener: AWSProgressInterface      // Callback interface
)
```

### DelegationToken Structure
```kotlin
data class DelegationToken(
    val accessKey: String,
    val secretKey: String,
    val sessionToken: String
)
```

---

**Document Version:** 2.0  
**Last Updated:** 2024  
**Maintained By:** Mobile Development Team

This document includes:
- AWS configuration for all environments
- Complete class-by-class reference (12 upload classes, 6 download classes)
- Detailed upload and download functionality for each class
- AWS paths, identifiers, and parameters
- File naming conventions
- Technical implementation details
- Backend integration notes and API requirements
- Quick reference tables

The backend team can use this document to:
- Understand all S3 paths used by the mobile app
- Know which API responses need to include document URLs
- Understand the delegation token requirements
- Implement proper path validation and document type identification
