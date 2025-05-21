import android.content.Context
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import com.example.easeapp.model.MeetingHistoryResponse
import com.example.easeapp.model.RetrofitProvider.RetrofitProvider
import com.example.easeapp.model.requests.DiaryApi
import retrofit2.Response


interface ChatApiService {
    @GET("/api/meetings/{meetingId}/history")
    suspend fun getChatHistory(
       // @Header("Authorization") token: String,
        @Path("meetingId") meetingId: String): Response<MeetingHistoryResponse>

}
object ChatApiClient {
    fun create(context: Context): ChatApiService {
        return RetrofitProvider.provideRetrofit(context).create(ChatApiService::class.java)
    }
}