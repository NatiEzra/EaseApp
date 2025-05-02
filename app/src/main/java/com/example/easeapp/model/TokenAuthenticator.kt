import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import com.example.ease.repositories.AuthRepository

class TokenAuthenticator(
    private val context: Context,
    private val authRepository: AuthRepository
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) {
            return null // כדי לא להיכנס ללופ אינסופי
        }

        val newAccessToken = runBlocking {
            authRepository.refreshAccessToken(context)
        }

        return if (newAccessToken.isNotEmpty()) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } else null
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
