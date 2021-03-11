package studio.seno.datamodule.api

import android.content.Context
import android.util.Log
import com.kakao.kakaolink.v2.KakaoLinkResponse
import com.kakao.kakaolink.v2.KakaoLinkService
import com.kakao.message.template.TemplateParams
import com.kakao.network.ErrorResult
import com.kakao.network.callback.ResponseCallback

class LinkShareApi {
    fun sendShareLink(
        context : Context,
        params : TemplateParams,
        serverCallbackArgs : MutableMap<String, String>) {

        KakaoLinkService.getInstance().sendDefault(
            context,
            params,
            serverCallbackArgs,
            object : ResponseCallback<KakaoLinkResponse?>() {
                override fun onFailure(errorResult: ErrorResult) {
                    Log.e("error", "kakao share error : ${errorResult.errorMessage}")
                }

                override fun onSuccess(result: KakaoLinkResponse?) {}
            })
    }
}