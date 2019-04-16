package com.delhivery.orion.exception

import com.delhivery.orion.exception.APIGenericExceptionMessage.BadRequestExceptionMessage
import com.delhivery.orion.exception.APIGenericExceptionMessage.GenericClientExceptionMessage
import com.delhivery.orion.exception.APIGenericExceptionMessage.GenericServerExceptionMessage
import com.delhivery.orion.exception.APIGenericExceptionMessage.InternalServerErrorExceptionMessage
import com.delhivery.orion.exception.APIGenericExceptionMessage.NotFoundExceptionMessage
import com.delhivery.orion.exception.APIGenericExceptionMessage.UnauthorizedExceptionMessage

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
    fun exceptionFromCode(code: Int) = HttpErrorCode.values()
        .filter { it.code == code }
        .firstOrNull() ?: UnknownError
  }

  fun toAPIException() = APIException(this)
}