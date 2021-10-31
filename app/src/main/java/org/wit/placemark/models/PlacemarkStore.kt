package org.wit.placemark.models

interface PlacemarkStore {
    fun findAll(): List<PlacemarkModel>
    fun create(placemark: PlacemarkModel)
    //Update function
    fun update(placemark: PlacemarkModel)
    //Delete function
    fun delete(placemark: PlacemarkModel)
}