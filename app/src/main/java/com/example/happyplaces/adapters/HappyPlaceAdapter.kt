package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.databinding.ItemHappyPlaceBinding

open class HappyPlaceAdapter (
    private val context: Context,
    private val items: ArrayList<PlaceEntity>
) : RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemHappyPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val rivAvatar = binding.rivAvatar
        val tvTitle = binding.tvTitle
        val ratingBar = binding.ratingBar
        val rating = binding.rating
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHappyPlaceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.rivAvatar.setImageURI(Uri.parse(item.image))
        if (item.title!!.length > 16)
            holder.tvTitle.text = item.title?.substring(0,16) + "..."
        else
            holder.tvTitle.text = item.title
        holder.ratingBar.rating = item.rating
        holder.rating.text = item.rating.toString()

        holder.itemView.setOnClickListener {
            if(onClickListener != null) {
                onClickListener!!.onClick(position, item)
            }
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {

    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: PlaceEntity)
    }

}