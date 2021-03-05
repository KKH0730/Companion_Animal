package studio.seno.domain.util

interface LongTaskCallback<T> {
    fun onResponse(result: studio.seno.domain.util.Result<T>)
}