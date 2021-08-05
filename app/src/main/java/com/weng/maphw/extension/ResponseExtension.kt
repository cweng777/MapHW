package com.weng.maphw.extension

import com.weng.maphw.model.network.exception.EmptyBodyException
import retrofit2.HttpException
import retrofit2.Response

@Throws(EmptyBodyException::class)
fun <T: Response<R>, R> T.requireBody(): R {
    return this.body() ?: throw EmptyBodyException()
}

/**
 * 處理http status code，200-299的狀態，並回傳[R]，如果沒有拋出[EmptyBodyException]。
 * 其他拋出[HttpException]。
 */
@Throws(
    HttpException::class,
    EmptyBodyException::class
)
fun <T: Response<R>, R> T.checkResponseBody(): R {
   return when {
       this.isSuccessful -> {
           requireBody()
       }
       else -> {
           throw HttpException(this)
       }
   }
}