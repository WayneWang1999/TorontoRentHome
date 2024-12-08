package com.example.torontorenthome.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.torontorenthome.data.HouseRepository
import com.example.torontorenthome.models.House
import kotlinx.coroutines.launch

class ListViewModel(private val repository: HouseRepository) : ViewModel() {

    private val _houses = MutableLiveData<List<House>>()
    val houses: LiveData<List<House>> get() = _houses

    fun fetchHouses() {
        viewModelScope.launch {
            try {
                val houseList = repository.fetchHouses()
                _houses.postValue(houseList)
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching houses: ${e.message}")
            }
        }
    }


}