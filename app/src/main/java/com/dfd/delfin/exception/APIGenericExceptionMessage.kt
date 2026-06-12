package com.dfd.delfin.exception

/**
 * Generic api exception messages
 */
object APIGenericExceptionMessage {
  /* generic client exception message */
  const val GenericClientExceptionMessage = "Client error"
  /* generic server exception message */
  const val GenericServerExceptionMessage = "Server error"

  const val BadRequestExceptionMessage = "Bad Request"
  const val UnauthorizedExceptionMessage = "Token Expired/Unauthorised"
  const val NotFoundExceptionMessage = "Not Found"
  const val InternalServerErrorExceptionMessage = "Internal server error"
  const val BadGatewayExceptionMessage = "Bad gateway: unable to create request, try again"
}