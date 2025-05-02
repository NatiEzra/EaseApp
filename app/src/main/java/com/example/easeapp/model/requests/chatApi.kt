import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import com.example.easeapp.model.MeetingHistoryResponse
import retrofit2.Response


interface ChatApiService {
    @GET("/api/meetings/{meetingId}/history")
    suspend fun getChatHistory(
       // @Header("Authorization") token: String,
        @Path("meetingId") meetingId: String): Response<MeetingHistoryResponse>

}
