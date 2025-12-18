package com.delhivery.axle.utils

import android.util.Log
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList.BucketOwnerFullControl
import com.delhivery.axle.api.response.DelegationToken
import com.delhivery.axle.config.AWSConfig
import com.delhivery.axle.injection.scope.ActivityScope
import dagger.android.support.DaggerAppCompatActivity
import java.io.File
import javax.inject.Inject

/**
 * Util for AWS image upload
 * 
 * @deprecated This class is deprecated. Use DocumentUtils instead for secure document upload/download.
 * The new Document API does not expose AWS credentials and provides better security.
 * Migration guide: See VAPT_SECURE_DOCUMENT_API_MIGRATION.md
 */
@ActivityScope
@Deprecated(
    message = "Use DocumentUtils instead. This AWS-based approach exposes credentials and is not VAPT compliant.",
    replaceWith = ReplaceWith("DocumentUtils.uploadDocument()", imports = ["com.delhivery.axle.utils.DocumentUtils"])
)
class AWSUtils @Inject constructor(
  private val activity: DaggerAppCompatActivity
) {

  /**
   * Initiate AWS upload
   * @deprecated Use DocumentUtils.uploadDocument() instead
   */
  @Deprecated(
      message = "Use DocumentUtils.uploadDocument() instead",
      replaceWith = ReplaceWith("documentUtils.uploadDocument(file, fileType, docType, listener, dynamicVariables)")
  )
  fun startUpload(
    delegationToken: DelegationToken,
    awsPath: String,
    file: File,
    listener: AWSProgressInterface
  ) {
    val credentials = BasicSessionCredentials(
        delegationToken.accessKey, delegationToken.secretKey,
        delegationToken.sessionToken
    )
    val s3 = AmazonS3Client(credentials, Region.getRegion(AWSConfig.ServerRegion.value()))
    val transferUtility = TransferUtility.builder()
        .context(activity)
        .s3Client(s3)
        .build()

    transferUtility.upload(AWSConfig.Bucket.value(), awsPath, file, BucketOwnerFullControl)
        .setTransferListener(object : TransferListener {
          override fun onStateChanged(
            id: Int,
            state: TransferState
          ) {
            if (state == TransferState.COMPLETED) {
              listener.onAWSSuccess(awsPath)
            } else if (state == TransferState.FAILED) {
              listener.onAWSFailure()
            }
          }

          override fun onProgressChanged(
            id: Int,
            bytesCurrent: Long,
            bytesTotal: Long
          ) {
          }

          override fun onError(
            id: Int,
            ex: Exception
          ) {
            listener.onAWSFailure()
          }
        })
  }

  /**
   * Get AWS base path
   * @deprecated Use DocumentUtils.downloadByS3Path() instead
   */
  @Deprecated(
      message = "Use DocumentUtils.downloadByS3Path() instead",
      replaceWith = ReplaceWith("documentUtils.downloadByS3Path(s3Path, listener)")
  )
  fun awsBasePath(): String {
    val s3Url =  "https://"+AWSConfig.Bucket.value()+".s3."+AWSConfig.ServerRegion.value()+".amazonaws.com/"
    return s3Url
  }
  /**
   * Initiate AWS download
   * @deprecated Use DocumentUtils.downloadByS3Path() instead
   */
  @Deprecated(
      message = "Use DocumentUtils.downloadByS3Path() instead",
      replaceWith = ReplaceWith("documentUtils.downloadByS3Path(s3Path, listener)")
  )
  fun startDownload(
    delegationToken: DelegationToken,
    awsPath: String,
    file: File,
    listener: AWSProgressInterface
  ) {
    val credentials = BasicSessionCredentials(
        delegationToken.accessKey, delegationToken.secretKey,
        delegationToken.sessionToken
    )
    val s3 = AmazonS3Client(credentials, Region.getRegion(AWSConfig.ServerRegion.value()))
    val transferUtility = TransferUtility.builder()
        .context(activity)
        .s3Client(s3)
        .build()

    val observer = transferUtility.download(AWSConfig.Bucket.value(), awsPath, file)
    observer.setTransferListener(object : TransferListener {

      override fun onProgressChanged(
        id: Int,
        bytesCurrent: Long,
        bytesTotal: Long
      ) {

      }

      override fun onStateChanged(
        id: Int,
        state: TransferState?
      ) {
        if (state == TransferState.COMPLETED) {
          listener.onAWSSuccess(awsPath)
        } else if (state == TransferState.FAILED) {
          listener.onAWSFailure()
        }
      }

      override fun onError(
        id: Int,
        ex: java.lang.Exception?
      ) {
        listener.onAWSFailure()
     }

    })

  }

  /**
   * AWS upload result interface
   * @deprecated Use DocumentUtils.DocumentProgressInterface instead
   */
  @Deprecated(
      message = "Use DocumentUtils.DocumentProgressInterface instead",
      replaceWith = ReplaceWith("DocumentUtils.DocumentProgressInterface", imports = ["com.delhivery.axle.utils.DocumentUtils"])
  )
  interface AWSProgressInterface {

    /**
     * action on upload success
     */
    fun onAWSSuccess(
      path: String
    )

    /**
     * action on upload failure
     */
    fun onAWSFailure()
  }

}
