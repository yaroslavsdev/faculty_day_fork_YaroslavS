package ru.tbank.education.school.lesson2.travelCompany

fun main() {
    // Туристическое агентство!

    val agency = Agency()

    val moscow = Destination("Russia", "Moscow")

    val firstPackage = TourPackage("Moscow Holidays", 15000.0, moscow, 3)
    val secondPackage = TourPackage("Moscow Bus Tour", 8300.0, moscow)

    val excursionToTula = Excursion("Tula Train Excursion", 4100.0, 10)

    agency.addTour(firstPackage)
    agency.addTour(secondPackage)

    agency.addTour(excursionToTula)
}
