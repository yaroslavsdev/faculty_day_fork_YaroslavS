package ru.tbank.education.school.lesson2.travelCompany

class Agency {
    private val allTours = mutableListOf<TourProduct>()

    fun addTour(tour: TourProduct) {
        allTours.add(tour)
    }

    fun displayTours() {
        for (tour in allTours) {
            println(tour)
        }
    }
}