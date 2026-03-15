abstract class BaseRepository(
    private val logger: ErrorLogger
) {

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Resource<T> {

        return try {

            Resource.Success(apiCall())

        } catch (e: CancellationException) {
            throw e
        } catch (e: SocketTimeoutException) {

            logger.log(e)

            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Timeout
            )

        } catch (e: IOException) {

            Resource.Failure(
                isNetworkError = true,
                errorCode = null,
                apiError = ApiError.Network
            )

        } catch (e: HttpException) {

            val code = e.code()

            val apiError = when (code) {

                401 -> ApiError.Unauthorized
                403 -> ApiError.AccessDenied
                404 -> ApiError.NotFound
                503 -> ApiError.ServiceUnavailable

                else -> ApiError.Unknown
            }

            Resource.Failure(
                isNetworkError = false,
                errorCode = code,
                apiError = apiError
            )

        } catch (e: Exception) {

            logger.log(e)

            Resource.Failure(
                isNetworkError = false,
                errorCode = null,
                apiError = ApiError.Unknown
            )
        }
    }
}

suspend fun <A, B, R> parallelApiCall2(
    call1: suspend () -> A,
    call2: suspend () -> B,
    transform: (A, B) -> R
): Resource<R> {

    return try {

        coroutineScope {

            val deferred1 = async { call1() }
            val deferred2 = async { call2() }

            val result = transform(
                deferred1.await(),
                deferred2.await()
            )

            Resource.Success(result)
        }

    } catch (e: Exception) {
        Resource.Error(e)
    }
}

suspend fun <A, B, C, R> parallelApiCall3(
    call1: suspend () -> A,
    call2: suspend () -> B,
    call3: suspend () -> C,
    transform: (A, B, C) -> R
): Resource<R> {

    return try {

        coroutineScope {

            val d1 = async { call1() }
            val d2 = async { call2() }
            val d3 = async { call3() }

            val result = transform(
                d1.await(),
                d2.await(),
                d3.await()
            )

            Resource.Success(result)
        }

    } catch (e: Exception) {
        Resource.Error(e)
    }
}

class HomeRepository(
    private val api: HomeApi
) {

    suspend fun getHomeData(): Resource<HomeData> {

        return parallelApiCall(
            call1 = { api.getUser() },
            call2 = { api.getWallet() },
            call3 = { api.getOrders() }
        ) { user, wallet, orders ->

            HomeData(
                user = user,
                wallet = wallet,
                orders = orders
            )
        }
    }
}

data class HomeData(
    val user: User,
    val wallet: Wallet,
    val orders: List<Order>
)

viewModelScope.launch {

    when(val result = repository.getHomeData()) {

        is Resource.Success -> {
            _state.value = result.data
        }

        is Resource.Error -> {
            showError(result.throwable)
        }
    }
}