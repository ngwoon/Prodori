package com.example.prodori

class LoginInfo {
    companion object {

        const val NO_NICKNAME = "unknown_nickname"
        const val DEFAULT_EMAIL = "unknown_email"
        const val DEFAULT_KEY = "unknown_key"
        var key = DEFAULT_KEY
        var email = DEFAULT_EMAIL
        var isLoggedIn = false
        var nickname = NO_NICKNAME
    }
}