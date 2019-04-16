package com.delhivery.orion.exception

/**
 * API Exception,
 *
 * API Exception is thrown when generic error occurs in API
 *
 * @param errorCode Server error code
 * @param errorMessage Server error message
 */
open class APIException(
  errorCode: HttpErrorCode?,
  errorMessage: String? = null
) : Exception(errorCode?.errorMessage ?: errorMessage)