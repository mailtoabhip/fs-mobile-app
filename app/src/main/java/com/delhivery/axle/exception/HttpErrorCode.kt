package com.delhivery.axle.exception

import com.delhivery.axle.exception.APIGenericExceptionMessage.BadRequestExceptionMessage
import com.delhivery.axle.exception.APIGenericExceptionMessage.GenericClientExceptionMessage
import com.delhivery.axle.exception.APIGenericExceptionMessage.GenericServerExceptionMessage
import com.delhivery.axle.exception.APIGenericExceptionMessage.InternalServerErrorExceptionMessage
import com.delhivery.axle.exception.APIGenericExceptionMessage.NotFoundExceptionMessage
import com.delhivery.axle.exception.APIGenericExceptionMessage.UnauthorizedExceptionMessage

/**
 * Http Error Codes
 */
enum class HttpErrorCode(
  val code: Int,
  val errorMessage: String
) {
  BadRequest(400, BadRequestExceptionMessage),
  Unauthorized(401, UnauthorizedExceptionMessage),
  Forbidden(403, UnauthorizedExceptionMessage),
  NotFound(404, NotFoundExceptionMessage),
  InternalServerError(500, InternalServerErrorExceptionMessage),
  NotImplemented(501, GenericServerExceptionMessage),
  ClientError(4, GenericClientExceptionMessage),
  ServerError(5, GenericServerExceptionMessage),
  UnknownError(-1, "Unknown error");

  companion object {
    /**
     * Get exception from http response code
     */
    fun exceptionFromCode(code: Int) = values().firstOrNull { it.code == code } ?: UnknownError
  }

  fun toAPIException() = APIException(this)
}