package com.project.accommodations.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.accommodations.databinding.RecyclerRowBinding
import com.project.accommodations.model.Accommodation
import com.squareup.picasso.Picasso

class FeedRecyclerAdapter(private val postList: ArrayList<Accommodation>) :
    RecyclerView.Adapter<FeedRecyclerAdapter.PostHolder>() {
    class PostHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostHolder(binding)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        //holder.binding.tvEmail.text = postList.get(position).email
        holder.binding.tvComment.text = postList.get(position).comment
        holder.binding.tvName.text = postList.get(position).name
        holder.binding.tvPhone.text = postList.get(position).phone
        //holder.binding.tvCategory.text = postList.get(position).category
        //holder.binding.tvArea.text = postList.get(position).area
        //holder.binding.tvState.text = postList.get(position).state
        Picasso.get().load(postList.get(position).downloadUrl)
            .into(holder.binding.ivPostImage)

        holder.binding.btnContact.setOnClickListener {
            val post = postList.get(position)
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${post.phone}")
            }
            it.context.startActivity(intent)
        }

    }


    override fun getItemCount(): Int {
        return postList.size
    }
}