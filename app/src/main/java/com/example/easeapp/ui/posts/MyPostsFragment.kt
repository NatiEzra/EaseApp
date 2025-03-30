package com.example.ease.ui.posts

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.ui.activities.MainActivity
import com.example.ease.R
import com.example.ease.model.PostModel
import com.example.ease.model.Post
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.ease.base.MyApplication.Globals.context
import com.example.ease.viewmodel.PostViewModel

class MyPostsViewHolder (itemView: View, private val onEditClick: (String) -> Unit, private val onDeleteClick: (String) -> Unit ) : RecyclerView.ViewHolder(itemView) {
    var profileNameTextView: TextView? = null
    var postTextView: TextView? = null
    var imageProfile: ImageView? = null
    var dateTextView: TextView? = null
    var imagePost: ImageView? = null
    var editButton: ImageView? = null
    var deleteButton: ImageView? = null





    init {
        profileNameTextView = itemView.findViewById(R.id.profileName)
        postTextView = itemView.findViewById(R.id.textPost)
        dateTextView = itemView.findViewById(R.id.postDate)
        imagePost = itemView.findViewById(R.id.imagePost)
        imageProfile = itemView.findViewById(R.id.ProfileImage)
        editButton=itemView.findViewById(R.id.editPostIcon)
        deleteButton=itemView.findViewById(R.id.deletePostIcon)

    }
    fun bind(post: Post) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        profileNameTextView?.text = post.profileName
        postTextView?.text = post.textPost
        dateTextView?.text = dateFormat.format(post.date)

        // Picasso.get().load(uri).into(profileImage)
        if(post.ProfileImage != "image") {
            try {
                Picasso.get()
                    .load(post.ProfileImage)
                    .transform(CropCircleTransformation())
                    .into(imageProfile)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the error, e.g., set a placeholder image
                //imagePost!!.setImageResource(R.drawable.image)
            }
        }

        if (imagePost != null) {
            try {
                imagePost?.visibility = View.VISIBLE
                Picasso.get().load(post.imagePost).into(imagePost)
            } catch (e: Exception) {
                imagePost?.visibility = View.GONE
                e.printStackTrace()
                // Handle the error, e.g., set a placeholder image
                //imagePost!!.setImageResource(R.drawable.image)
            }
        }
        //val resourceId = itemView.context.resources.getIdentifier(post.imagePost, "drawable", itemView.context.packageName)
        //imagePost?.setImageResource(resourceId)

        editButton?.setOnClickListener(){
            onEditClick(post.postId)
        }

        deleteButton?.setOnClickListener {

            AlertDialog.Builder(itemView.context)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Yes") { dialog, _ ->
                    onDeleteClick(post.postId)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }



    }


}

class MyPostRecycleAdapter(var posts : List<Post>?, private val onEditClick: (String) -> Unit, private val onDeleteClick: (String) -> Unit): RecyclerView.Adapter<MyPostsViewHolder>() {
    override fun getItemCount(): Int {
        return posts?.size ?: 0
    }
    fun set(_posts: List<Post>) {
        posts = _posts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostsViewHolder {
        val inflation=LayoutInflater.from(parent.context)
        val view = inflation.inflate(R.layout.my_post_row, parent, false)

        return MyPostsViewHolder(view, onEditClick, onDeleteClick);
    }


    override fun onBindViewHolder(holder: MyPostsViewHolder, position: Int) {
        holder.bind(posts?.get(position) ?: return)
    }


}




class MyPostsFragment : Fragment() {
    var adapter: MyPostRecycleAdapter? = null
    var posts: MutableList<Post> = ArrayList()
    val postViewModel: PostViewModel by viewModels()
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_feed, container, false)
        progressBar = view.findViewById(R.id.feedProgressBar)
        recyclerView = view.findViewById(R.id.fragment_feed_recycler_view)

        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        //posts = PostModel.shared.posts
        recyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        adapter = MyPostRecycleAdapter(
            posts,
            onEditClick = { postId -> editPostButtonClicked(postId) },
            onDeleteClick = { postId -> confirmDelete(postId) } // ✅
        )
        recyclerView.adapter = adapter
        getMyPosts()

        return view
    }

    private fun confirmDelete(postId: String) {
        postViewModel.deletePost(postId)
        postViewModel.postOperationState.observe(viewLifecycleOwner) { result ->
            result.onSuccess {

                Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                getMyPosts()
            }
            result.onFailure {
                Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
       // progressBar.visibility = View.VISIBLE
        //recyclerView.visibility = View.GONE
        getMyPosts()

    }
    fun editPostButtonClicked(postId: String?) {
        (activity as? MainActivity)?.editPost(postId)
    }

    private fun getMyPosts() {
        postViewModel.fetchMyPosts()
        postViewModel.myPosts.observe(viewLifecycleOwner) { fetchedPosts ->
            if (fetchedPosts.size>0){
                view?.findViewById<TextView>(R.id.noPostsTextView)?.visibility = View.GONE

            }

            posts.clear()
            posts.addAll(fetchedPosts)
            adapter?.set(posts)
            adapter?.notifyDataSetChanged()
            //progressBar.visibility = View.GONE
            //recyclerView.visibility = View.VISIBLE
        }
    }


}