package network

import domain.TokenResponse


class TokenRepository : IbbRepository() {

    @Throws(Exception::class)
    fun getToken(): TokenResponse {
        val tokenRepo = getRetrofit(BASE_URL).create(TokenService::class.java)
        val response = tokenRepo.getToken().execute()
        fileUtil.saveToken(response.body())
        return response.body()
    }



}