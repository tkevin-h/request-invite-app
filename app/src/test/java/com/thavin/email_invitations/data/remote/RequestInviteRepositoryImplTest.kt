package com.thavin.email_invitations.data.remote

import com.google.common.truth.Truth.assertThat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.thavin.email_invitations.data.remote.model.Result
import com.thavin.email_invitations.data.remote.model.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
@ExperimentalSerializationApi
class RequestInviteRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()
    private val webServer = MockWebServer()

    private val client = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .readTimeout(1, TimeUnit.SECONDS)
        .writeTimeout(1, TimeUnit.SECONDS)
        .build()

    private val json = Json { isLenient = true }

    private val api = Retrofit.Builder()
        .baseUrl(webServer.url("/"))
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(RequestInviteApi::class.java)

    private val requestInviteRepositoryImpl = RequestInviteRepositoryImpl(api)

    @Before
    fun beforeTests() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun afterTests() {
        webServer.shutdown()
        Dispatchers.resetMain()
    }

    @Test
    fun `sending user details returns a success`() = runTest {
        val response = MockResponse()
            .setBody("registered")
            .setResponseCode(200)

        webServer.enqueue(response)

        val expectedResponse = Result.Success(message = "registered")
        val actualResponse = requestInviteRepositoryImpl.requestInvite(UserInfo("John", "john@email.com"))

        assertThat(actualResponse).isEqualTo(expectedResponse)
    }

    @Test
    fun `sending user details returns an error`() = runTest {
        val response = MockResponse()
            .setBody("{\"errorMessage\": \"This email address is already in use\"}")
            .setResponseCode(400)

        webServer.enqueue(response)

        val expectedResponse = Result.Error(message = "This email address is already in use")
        val actualResponse = requestInviteRepositoryImpl.requestInvite(UserInfo("John", "john@email.com"))

        assertThat(actualResponse).isEqualTo(expectedResponse)
    }
}