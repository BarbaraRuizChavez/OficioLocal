package com.example.oficiolocal.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @GET("providers")
    suspend fun getProviders(): List<ProviderDto>

    @GET("providers/{id}")
    suspend fun getProvider(@Path("id") id: String): ProviderDto

    @GET("requests")
    suspend fun getRequests(): List<RequestDto>

    @POST("requests")
    suspend fun createRequest(@Body body: RequestDto): RequestDto

    @PUT("requests/{id}")
    suspend fun updateRequest(@Path("id") id: String, @Body body: RequestDto): RequestDto

    @DELETE("requests/{id}")
    suspend fun deleteRequest(@Path("id") id: String)

    @GET("reviews")
    suspend fun getReviews(): List<ReviewDto>

    @POST("reviews")
    suspend fun createReview(@Body body: ReviewDto): ReviewDto

    @PUT("reviews/{id}")
    suspend fun updateReview(@Path("id") id: String, @Body body: ReviewDto): ReviewDto

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String)
}
