package ru.tbank.education.school.lesson2.travelCompany

class TourPackage(
    name: String,
    basePrice: Double,
    val destination: Destination,
    val nights: Int,
) : TourProduct(name, basePrice){
    constructor(name: String, basePrice: Double, destination: Destination) :
            this(name, basePrice, destination, 7)

    override val price: Double
        get() = super.price * nights
    override fun info(): String {
        return "Tour Package $name to ${destination.city}, price: $price, nights: $nights"
    }
}