package com.loyer.storytracking.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.loyer.storytracking.GlideApp
import com.loyer.storytracking.R
import com.loyer.storytracking.model.User
import com.loyer.storytracking.util.inflate
import kotlinx.android.synthetic.main.item_user.view.*

/**
 * Created by celikrecep on 6.04.2019.
 */
class UserAdapter(
        private val list: MutableList<User>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private lateinit var userOnclick: IUserOnClick
    private var packageName = ""

    constructor(list: MutableList<User>, userOnclick: IUserOnClick,
                packageName: String) : this(list) {
        this.userOnclick = userOnclick
        this.packageName = packageName
    }

    interface IUserOnClick {
        fun onClickUser(position: Int, user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(parent.inflate(R.layout.item_user))
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(position, list[position], userOnclick)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int, user: User, userOnclick: IUserOnClick) {
            GlideApp.with(itemView.context)
                    .load(itemView.resources.getIdentifier(user.path, null, packageName))
                    .centerCrop()
                    .fitCenter()
                    .into(itemView.userPhoto)
            itemView.username.text = user.username
            itemView.userPhoto.setOnClickListener {
                userOnclick.onClickUser(position, user)
            }
        }
    }
}