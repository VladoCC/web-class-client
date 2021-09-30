import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TodoClient(val host: String, username: String, password: String) {
  val tokenClient = HttpClient(CIO) {
    install(JsonFeature) {
      serializer = GsonSerializer()
    }
  }
  val userClient = HttpClient(CIO) {
    install(JsonFeature) {
      serializer = GsonSerializer()
    }
    install(Auth) {
      bearer {
        suspend fun getToken(): BearerTokens = tokenClient
          .post<AuthResponse>("$host/user") {
            contentType(ContentType.Application.Json)
            body = AuthRequest(username, password)
          }
          .let { BearerTokens(it.token, "") }


        loadTokens { getToken() }
        refreshTokens { getToken() }
      }
    }
  }

  data class AuthRequest(val username: String, val password: String)
  data class AuthResponse(val token: String, val action: String)

  suspend fun getTodos(): List<Todo> {
    try {
      val response = userClient.get<GetTodosResponse>("$host/todo") {
        contentType(ContentType.Application.Json)
      }
      return response.todo.map { Todo(it.key, it.value) }
    } catch (e: ClientRequestException) {
      e.printStackTrace()
    }
    return emptyList()
  }

  private data class GetTodosResponse(val todo: Map<String, String>)
  data class Todo(val id: String, val text: String)
  data class TodoContent(val text: String)

  suspend fun postTodo(text: String): Boolean {
    try {
      val response = userClient.post<HttpResponse>("$host/todo") {
        contentType(ContentType.Application.Json)
        body = TodoContent(text)
      }
      return response.status == HttpStatusCode.OK
    } catch (e: ClientRequestException) {
      e.printStackTrace()
    }
    return false
  }

  suspend fun updateTodo(text: String, id: String): Boolean {
    try {
      val response = userClient.put<HttpResponse>("$host/todo/$id") {
        contentType(ContentType.Application.Json)
        body = TodoContent(text)
      }
      return response.status == HttpStatusCode.OK
    } catch (e: ClientRequestException) {
      e.printStackTrace()
    }
    return false
  }

  suspend fun deleteTodo(id: String): Boolean {
    try {
      val response = userClient.delete<HttpResponse>("$host/todo/$id")
      return response.status == HttpStatusCode.OK
    } catch (e: ClientRequestException) {
      e.printStackTrace()
    }
    return false
  }
}