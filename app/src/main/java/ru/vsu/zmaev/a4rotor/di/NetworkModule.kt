package ru.vsu.zmaev.a4rotor.di

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.koin.dsl.module
import retrofit2.Retrofit
import ru.vsu.zmaev.a4rotor.BuildConfig

val networkModule = module {
    fun provideOkhttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) Level.BODY else Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addNetworkInterceptor {
                val request = it.request().newBuilder()
                    .build()
                it.proceed(request)
            }
            .build()
    }

    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
//            .baseUrl(BuildConfig.API_URL)
            .client(okHttpClient)
            .build()
    }

//    fun provideNatGeoApi(retrofit: Retrofit): NatGeoApi {
//        return retrofit.create(NatGeoApi::class.java)
//    }

    single { provideOkhttpClient() }
    single { provideRetrofit(get()) }
}