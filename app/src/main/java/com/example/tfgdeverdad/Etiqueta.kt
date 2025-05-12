package com.example.tfgdeverdad

data class Etiqueta(
    val id: String? = null,
    val titulo: String = "",
    val color: String = "#FFFFFF",
    val prioridad: Int = 1,
    val urgente: Boolean = false,
    val sticker: String = "📌",
    val userId: String = ""
)

