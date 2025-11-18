package ru.tbank.education.school.lesson2.travelCompany

abstract class TourProduct(
    val name: String,
    protected val basePrice: Double,
) {
    open val price: Double
        get() = basePrice

    abstract fun info(): String
}