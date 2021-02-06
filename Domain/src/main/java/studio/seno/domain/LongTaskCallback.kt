package studio.seno.domain

import kotlin.Result

interface LongTaskCallback<T> {
    fun onResponse(result: studio.seno.domain.Result<T>)
}