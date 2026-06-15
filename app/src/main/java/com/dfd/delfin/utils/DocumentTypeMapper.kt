package com.dfd.delfin.utils

/**
 * Utility class to map internal document types to API document types
 * This helps with migration from AWS S3 to Document API
 */
object DocumentTypeMapper {
    
    /**
     * Map internal document type to API document type
     */
    fun mapToApiType(internalType: String): String {
        return when (internalType.lowercase()) {
            // Identity verification documents
            "cin_" -> "cin"
            "udyog_" -> "udyog_aadhaar"
            "shop_" -> "shop_establishment"
            
            // Payment documents
            "account_proof_" -> "cancelled_cheque"
            "194c_" -> "section_194C"
            "passbook_" -> "passbook_front_page"
            
            // KYC documents
            "pan_card" -> "pan"
            "aadhaar_card" -> "aadhaar"
            "gst_card" -> "gst"
            "rc_card" -> "rc"
            "lr_card" -> "lr"
            "driving_license" -> "driving_licence"
            
            // Business documents
            "visiting_card" -> "visiting_card"
            "letterhead" -> "letterhead"
            
            // Transport documents
            "loading_note" -> "loading_note"
            "lr_copy" -> "lr_copy"
            "rc_copy" -> "rc_copy"
            
            // Default case - return as is if already in correct format
            else -> internalType.lowercase()
        }
    }
    
    /**
     * Get all supported document types
     */
    fun getSupportedDocumentTypes(): List<String> {
        return listOf(
            "rc", "lr", "loading_note", "cin", "udyog_aadhaar", 
            "shop_establishment", "cancelled_cheque", "passbook_front_page", 
            "section_194C", "aadhaar", "gst", "pan", "lr_copy", 
            "visiting_card", "letterhead", "driving_licence", "rc_copy"
        )
    }
    
    /**
     * Check if document type is supported
     */
    fun isSupportedDocumentType(docType: String): Boolean {
        return getSupportedDocumentTypes().contains(docType.lowercase())
    }
}
