package com.example.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.withContext
import java.util.*

class GetAddressFromLatLng(
    context: Context,
    private val lat: Double,
    private val lng: Double) {

    // to decode the lat and lng value to text address
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener:AddressListener

    suspend fun launchBackgroundProcessForRequest() {
        val address=getAddress()

        withContext(Main){
            // switch to Main thread, cuz we're going to update the UI related values from here on
            // if we get a valid address
            if (address.isEmpty()) {
                mAddressListener.onError()
            } else {
                mAddressListener.onAddressFound(address)  // updating UI
            }
        }
    }


    private fun getAddress():String {
        // there may be multiple locations/places associated with the lat and lng, we take the top/most relevant address
        try {
            val addressList: List<Address>? = geocoder.getFromLocation(lat,lng,1)

            if(!addressList.isNullOrEmpty()) {
                val address: Address = addressList[0]
                val sb = StringBuilder()
                // Returns the largest index currently in use to specify an address line.
                for(i in 0..address.maxAddressLineIndex) {
                    sb.append(address.getAddressLine(i)+" ")
                }
                // to remove the last " "
                sb.deleteCharAt(sb.length-1)

                return sb.toString()
            }
        }
        catch (e:Exception){
            e.printStackTrace()
        }
        return ""
    }

    // to attach the listener to the class property
    fun setCustomAddressListener(addressListener: AddressListener) {
        this.mAddressListener=addressListener
    }

    //can be defined anywhere
    interface AddressListener {
        fun onAddressFound(address:String)
        fun onError()
    }

}