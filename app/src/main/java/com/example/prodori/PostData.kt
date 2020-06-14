package com.example.prodori

import java.io.Serializable

data class PostData(val title: String, val writer: String, val content: String, val category: String, var like: Long, var unlike: Long, var report: Long) : Serializable