package com.example.prodori

import java.io.Serializable

data class AdInfo(var pName: String, val eName: String, val address: String, val found_cn: String, val date: String, val punishment: String, val violation: String) :
    Serializable