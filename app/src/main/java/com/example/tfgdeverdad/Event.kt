package com.example.tfgdeverdad

import com.google.firebase.Timestamp

data class Event(
    val titulo: String = "",
    val fechaInicio: Timestamp? = null,
    val fechaFin: Timestamp? = null,
    val horaInicio: String = "",
    val horaFin: String = "",
    val notas: String = "",
    val sticker: String = ""
)
