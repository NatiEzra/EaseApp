import retrofit2.http.GET
import retrofit2.http.Path

interface ChatApiService {
    @GET("messages/history/{meetingId}")
    suspend fun getChatHistory(@Path("meetingId") meetingId: String): String

}
