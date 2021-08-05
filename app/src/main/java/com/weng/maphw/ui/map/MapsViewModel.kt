package com.weng.maphw.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.weng.maphw.model.repository.GeoRepository
import kotlinx.coroutines.launch

class MapsViewModel(
    private val geoRepository: GeoRepository
): ViewModel() {

    private val _coordinates : MutableLiveData<List<LatLng>> = MutableLiveData()
    val coordinates : LiveData<List<LatLng>> get() = _coordinates

    fun getCoordinates() {
        viewModelScope.launch {
            geoRepository.getCoordinates().fold(
                {
                    _coordinates.value = it
                },{

                }
            )
        }
    }
}