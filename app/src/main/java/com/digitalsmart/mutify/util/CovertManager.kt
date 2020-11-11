package com.digitalsmart.mutify.util

import androidx.recyclerview.widget.RecyclerView
import com.digitalsmart.mutify.R
import com.digitalsmart.mutify.UserDataManager
import nz.co.trademe.covert.Covert

class CovertManager (recyclerView: RecyclerView, userDataManager: UserDataManager)
{
    private val covertConfig : Covert.Config = Covert.Config(
            iconRes = R.drawable.location_icon, // The icon to show
            iconDefaultColorRes = R.color.colorBlue,            // The color of the icon
            actionColorRes = R.color.cardview_dark_background          // The color of the background
    )

    val covert = Covert.with(covertConfig)
            .setIsActiveCallback {
                // This is a callback to check if the item is active, i.e checked
                userDataManager.contains(it.adapterPosition)
            }
            .doOnSwipe { viewHolder, _ ->
                // This callback is fired when a ViewHolder is swiped
                userDataManager.remove(viewHolder.adapterPosition)
            }
            .attachTo(recyclerView)
}