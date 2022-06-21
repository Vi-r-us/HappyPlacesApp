package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.database.PlaceDao
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.fragments.AddFragment
import com.example.happyplaces.fragments.HomeFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class HappyPlaceAdapter (
    private val context: Context,
    private var items: ArrayList<PlaceEntity>,
    private val updateListener: (item:PlaceEntity)->Unit
) : RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemHappyPlaceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val rivAvatar = binding.rivAvatar
        val tvTitle = binding.tvTitle
        val ratingBar = binding.ratingBar
        val rating = binding.rating
        val btnLike = binding.btnLike
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
        holder.ratingBar.rating = item.rating
        holder.rating.text = item.rating.toString()

        if (item.title!!.length > 16) holder.tvTitle.text = item.title?.substring(0,16) + "..."
        else holder.tvTitle.text = item.title

        if (item.isFavorite == 0) holder.btnLike.setImageResource(R.drawable.heart_not_liked)
        else holder.btnLike.setImageResource(R.drawable.heart_liked)

        holder.itemView.setOnClickListener {
            if(onClickListener != null) {
                onClickListener!!.onClick(position, item)
            }
        }
        holder.btnLike.setOnClickListener {
            if (item.isFavorite == 0)  holder.btnLike.setImageResource(R.drawable.heart_liked)
            else  holder.btnLike.setImageResource(R.drawable.heart_not_liked)
            updateListener.invoke(item)
            notifyItemChanged(position)
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun removeAt(position: Int, placeDao: PlaceDao): ArrayList<PlaceEntity> {
        return items
    }

    fun setFilteredList(filteredList: ArrayList<PlaceEntity>) {
        items = filteredList
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val fragment: Fragment = AddFragment()
        val manager = (context as AppCompatActivity).supportFragmentManager
        val bundle = Bundle()

        bundle.putInt("REQUEST_CODE", requestCode)
        bundle.putParcelable(HomeFragment.EXTRA_PLACE_DETAILS, items[position])
        fragment.arguments = bundle

        val transaction =  manager.beginTransaction()
        transaction.replace(R.id.fragment_cntainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: PlaceEntity)
    }

}