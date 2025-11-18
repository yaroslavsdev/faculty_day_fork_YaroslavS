package ru.tbank.education.school.lesson2.travelCompany

class Excursion(
    name: String,
    basePrice: Double,
    val duraton: Int,
) : TourProduct(name, basePrice) {
    override fun info(): String {
        return "Excursion $name, duration: $duraton h., price: $price"
    }
}