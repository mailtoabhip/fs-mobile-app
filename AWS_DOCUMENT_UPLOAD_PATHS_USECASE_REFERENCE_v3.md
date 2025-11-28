# AWS S3 Document Upload Paths - Use Case Reference Document

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

---

## Upload Use Cases

All documents are uploaded using S3 object keys (paths). The same path structure is used as the identifier for both upload and download operations.

### 1. Trip POD Uploads

**AWS Path Pattern:** `trips/temp/vendor_pod/{transactionId}/{filename}.jpg`

**Details:**
- **Purpose:** Temporary storage for Proof of Delivery (POD) images before final processing
- **Path Construction:**
  ```kotlin
  val awsPath = "trips/temp/vendor_pod/${transactionId}/" + uploadImageName + ".jpg"
  ```
- **Path Structure:** 
  - Base: `trips/temp/vendor_pod/`
  - Transaction ID: Dynamic transaction identifier
  - Filename: Generated with timestamp
  - Extension: Always `.jpg`
- **Example Path:** `trips/temp/vendor_pod/TRX123456789/IMG_1699123456789.jpg`

---

### 2. Docket Updates

**AWS Path Pattern:** `trips/vendor_pod/docket/{filename}.jpg`

**Details:**
- **Purpose:** Docket images uploaded by vendors
- **Path Construction:**
  ```kotlin
  val awsPath = "trips/vendor_pod/docket/$uploadImageName.jpg"
  ```
- **Path Structure:**
  - Base: `trips/vendor_pod/docket/`
  - Filename: Generated with timestamp
  - Extension: Always `.jpg`
- **Example Path:** `trips/vendor_pod/docket/docket_1699123456789.jpg`

---

### 3. Share Rate Documents

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
  - Filename Format: `IMG_{timestamp}.{extension}` (e.g., `IMG_1699123456789.jpg`)
- **Example Path:** `loadboard/sharerate/IMG_1699123456789.jpg`
- **Note:** After upload, the path is stored as `Pair(path.replace(awsPath, ""), fileSize)`

---

### 4. Profile Pictures

**AWS Path Pattern:** `loadboard/profile/{filename}.jpg`

**Details:**
- **Purpose:** User profile pictures
- **Path Construction:**
  ```kotlin
  val awsPath = "loadboard/profile/$uploadImageName.jpg"
  ```
- **Path Structure:**
  - Base: `loadboard/profile/`
  - Filename: Generated with timestamp
  - Extension: Always `.jpg`
- **Example Path:** `loadboard/profile/profile_1699123456789.jpg`

---

### 5. Payment Documents

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
  - Account Proof: `"account_proof_" + System.currentTimeMillis() + "_" + phoneNumber + ".jpg"`
  - 194C: `"194C_" + System.currentTimeMillis() + "_" + phoneNumber + ".jpg"`
- **Examples:**
  - Account Proof: `loadboard/payment/account_proof_1699123456789_9876543210.jpg`
  - 194C Declaration: `loadboard/payment/194C_1699123456789_9876543210.pdf`
- **Verification:** After upload, documents are sent for verification which constructs full URLs: `s3url + awsPath + filename`
- **Note:** The backend can identify document type by checking if the filename contains `account_proof` or `194C`.

---

### 6. Bank Details (194C Documents)

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
- **Filename Generation:** `"194C_" + System.currentTimeMillis() + "_" + phoneNumber + ".jpg"`
- **Example Path:** `loadboard/payment/194C_1699123456789_9876543210.jpg`
- **Verification:** Documents sent for verification with full URLs

---

### 7. Identity Verification Documents

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
- **Filename Generation:** `docType + System.currentTimeMillis() + "." + fileExtension`
- **Example Path:** `loadboard/iv/PAN_1699123456789.jpg`
- **Verification:** Documents sent for verification with full URLs

---

### 8. GST Verification Documents

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
- **Example Path:** `loadboard/gst/GST_1699123456789.pdf`
- **Verification:** Documents sent for verification with full URLs

---

### 9. Address Verification Documents

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
- **Filename Generation:** `"Address_" + System.currentTimeMillis() + "." + fileExtension`
- **Example Path:** `loadboard/address/Address_1699123456789.jpg`

---

### 10. Aadhaar Verification Documents

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
- **Example Path:** `loadboard/aadhaar/Aadhaar_1699123456789.jpg`
- **Verification:** Documents sent for verification with full URLs

---

### 11. Business Verification Documents (LR/RC)

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
  - LR: `"Lr_doc_" + phoneNumber + "." + fileExtension`
  - RC: `"RC_doc_" + phoneNumber + "." + fileExtension`
- **Examples:**
  - LR Document: `loadboard/lr/Lr_doc_9876543210.pdf`
  - RC Document: `loadboard/lr/RC_doc_9876543210.jpg`
- **Verification:** Documents sent for verification with full URLs
- **Note:** The backend can identify document type by checking if the filename starts with `Lr_doc_` or `RC_doc_`.

---

## Download Use Cases

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

### 1. Trip POD Downloads

**Download Process:**
- **Source:** `podUrl` from backend API response
- **Path Extraction:** `podUrl` is used directly (can be full URL or S3 object key)
- **File Creation:**
  ```
  {transactionId}_pod.pdf
  {transactionId}_pod.png
  {transactionId}_pod.jpg
  ```
- **Supported Formats:** `.pdf`, `.png`, `.jpg`, `.jpeg`
- **Backend Requirement:** Return `podUrl` in API response as S3 object key (path) or full URL

---

### 2. Profile Image Downloads

**Download Process:**
- **Source:** `profileImageUrl` from backend/user preferences
- **Path Extraction:** URL used directly (assumed to be S3 object key or full URL)
- **File Creation:** `{timestamp}_profile.jpg` in app's documents directory
- **Backend Requirement:** Store profile image URL in user preferences/API response

---

### 3. KYC Documents Downloads

**Download Process:**
- **Source:** Document URLs from backend API (`docUrl`)
- **Path Extraction:**
  ```kotlin
  // Strips base URL from full S3 URL
  docUrl.replace(awsBasePath, "")
  ```
- **File Creation:**
  - For downloads: `{timestamp}{filename}` in Downloads directory
  - For viewing: `{timestamp}/{filename}` in app's documents directory
- **Backend Requirement:** Return full S3 URLs in `documentUrls` array or `docUrl` field

---

### 4. Reward Proof Downloads

**Download Process:**
- **Source:** `proofUrl` array from API response
- **Path Extraction:**
  ```kotlin
  proofUrl[0]?.replace(awsBasePath, "")
  ```
- **File Creation:** `{timestamp}{filename}` in public Downloads directory
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

## Backend Integration Notes

### For Backend Developers:

1. **Delegation Token API:**
   - **Endpoint:** The mobile app calls a delegation token API with the AWS Account ID (Target)
   - **Request Parameter:** AWS Account ID
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
   - File sizes are stored as `Pair(filename, sizeInKB)`
   - Backend may want to validate file sizes before processing

---

## Quick Reference: Upload vs Download

### Upload Operations
- **Common Pattern:** All use `awsUtils.startUpload(delegationToken, awsPath, file, listener)`
- **Path Source:** Constructed in mobile app based on document type
- **Identifier:** S3 object key (path) returned in success callback

### Download Operations
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
- Use case-specific upload and download functionality
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

