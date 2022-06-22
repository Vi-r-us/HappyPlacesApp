package com.example.happyplaces.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.database.PlaceEntity
import com.squareup.picasso.Picasso
import java.io.File

class FavoritePlaceAdapter(
    val context: Context?,
    private val placeList: ArrayList<PlaceEntity>,
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: FavoritePlaceAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).
                inflate(R.layout.item_favorite_place, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val place = placeList[position]
        val placeImage = holder.itemView
            .findViewById<com.flaviofaria.kenburnsview.KenBurnsView>(R.id.place_image)

        // Load Image
        Picasso.get().load(File(place.image!!)).into(placeImage)

        // kenburns animation
        placeImage.resume()

        // Other details
        holder.itemView.findViewById<TextView>(R.id.tv_title).text = place.title
        holder.itemView.findViewById<TextView>(R.id.tv_description).text = place.description
        holder.itemView.findViewById<TextView>(R.id.rating).text = place.rating.toString()
        holder.itemView.findViewById<RatingBar>(R.id.ratingBar).rating = place.rating

        holder.itemView.setOnClickListener {
            if(onClickListener != null) {
                onClickListener!!.onClick(position, place)
            }
        }

    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    fun setOnClickListener(onClickListener: FavoritePlaceAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }
    interface OnClickListener {
        fun onClick(position: Int, model: PlaceEntity)
    }

    class ViewHolder(v: View?): RecyclerView.ViewHolder(v!!), View.OnClickListener {
        override fun onClick(p0: View?) {

        }

        init {
            itemView.setOnClickListener(this)
        }
    }
}