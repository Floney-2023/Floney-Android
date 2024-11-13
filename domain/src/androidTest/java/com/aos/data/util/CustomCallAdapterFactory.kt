package com.aos.data.util

import com.aos.util.NetworkState
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class CustomCallAdapterFactory : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if(Call::class.java != getRawType(returnType)){  //1
            return null
        }

        check(returnType is ParameterizedType) { //2
            "return type must be parameterized as Call<NetworkState<Foo>> or Call<NetworkState<out Foo>>"
        }

        val responseType = getParameterUpperBound(0, returnType) //3

        if (getRawType(responseType) != NetworkState::class.java) { //4
            return null
        }

        check(responseType is ParameterizedType) { //5
            "Response must be parameterized as NetworkState<Foo> or NetworkState<out Foo>"
        }

        val bodyType = getParameterUpperBound(0,responseType) //6

        return CustomCallAdapter<Any>(bodyType)

    }
}