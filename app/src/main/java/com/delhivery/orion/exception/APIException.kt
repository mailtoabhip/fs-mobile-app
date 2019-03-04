package com.delhivery.orion.exception

/**
 * API Exception,
 *
 * API Exception is thrown when generic error occurs in API
 *
 * @param errorCode Server error code
 * @param errorMessage Server error message
 */
class APIException(val errorCode: Int, val errorMessage: String) : Exception(errorMessage)