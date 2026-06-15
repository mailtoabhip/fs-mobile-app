package com.dfd.delfin.api.service

import com.dfd.delfin.api.response.DocumentDownloadResponse
import com.dfd.delfin.api.response.DocumentUploadResponse
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

/**
 * Handle network calls to Document Service for secure document upload/download
 */
interface DocumentService {

    /**
     * Upload documents to secure document storage
     * @param files Array of files to upload
     * @param docType Type of document being uploaded
     * @param dynamicVariables Optional JSON string containing dynamic variables (transactionId, uploadImageName, phoneNumber, docType, proofType)
     * @return DocumentUploadResponse
     */
    @Multipart
    @POST("/document/upload")
    fun uploadDocuments(
        @Part files: List<MultipartBody.Part>,
        @Part docType: MultipartBody.Part,
        @Part dynamicVariables: MultipartBody.Part? = null
    ): Single<DocumentUploadResponse>

    /**
     * List/download documents by type (Mode 1)
     * @param docType Type of document to list
     * @return DocumentDownloadResponse
     */
//    @GET("/document/download")
//    fun listDocuments(
//        @Query("doc_type") docType: String
//    ): Single<DocumentDownloadResponse>

    /**
     * Download document by s3_path (Mode 2)
     * @param s3Path S3 path of the document (e.g., "trips/vendor_pod/docket/docket_1764233770054.jpg")
     * @return DocumentDownloadResponse
     * Note: encoded = true prevents Retrofit from URL-encoding the path, allowing slashes to remain as-is
     */
    @GET("/document/download")
    fun listDocuments(
        @Query(value = "s3_path", encoded = true) s3Path: String
    ): Single<DocumentDownloadResponse>
}
