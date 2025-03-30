package com.example.ease.ui.posts

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.R
import com.example.ease.model.PostModel
import com.example.ease.model.Post
import com.example.ease.viewmodel.PostViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.text.SimpleDateFormat
import java.util.Locale

class PostsViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    var profileNameTextView: TextView? = null
    var postTextView: TextView? = null
    var imageProfile: ImageView? = null
    var dateTextView: TextView? = null
    var imagePost: ImageView? = null





    init {
        profileNameTextView = itemView.findViewById(R.id.profileName)
        postTextView = itemView.findViewById(R.id.textPost)
        dateTextView = itemView.findViewById(R.id.postDate)
        imagePost = itemView.findViewById(R.id.imagePost)
        imageProfile = itemView.findViewById(R.id.ProfileImage)

    }
    /*itemView.setOnClickListener{
            adapterPosition
        }
        itemView.findViewById<View>(R.id.student_row).setOnClickListener {
            val intent = Intent(itemView.context, StudentDetailsActivity::class.java).apply {
                putExtra("STUDENT_ID", student?.id)
                putExtra("STUDENT_NAME", student?.name)
                putExtra("STUDENT_ADDRESS", student?.address)
                putExtra("STUDENT_PHONE", student?.phone)
                putExtra("STUDENT_IS_CHECKED", student?.isChecked)
                putExtra("STUDENT_INDEX", adapterPosition)
            }
            itemView.context.startActivity(intent)
        }
*/


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

            if (imagePost!=null&& imagePost?.context!=null) {
                try {
                    imagePost?.visibility = View.VISIBLE
                    Picasso.get().load(post.imagePost).into(imagePost)
                    imagePost?.setOnClickListener {
                        showFullScreenImage(post.imagePost, imagePost!!.context)
                    }
                } catch (e: Exception) {
                    imagePost?.visibility = View.GONE
                    e.printStackTrace()
                }
            }

        //val resourceId = itemView.context.resources.getIdentifier(post.imagePost, "drawable", itemView.context.packageName)
        //imagePost?.setImageResource(resourceId)



    }

}
fun showFullScreenImage(imageUrl: String, context: Context) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(R.layout.dialog_fullscreen_image)

    val imageView = dialog.findViewById<ImageView>(R.id.fullScreenImageView)
    Picasso.get().load(imageUrl).into(imageView)

    dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.BLACK))
    dialog.show()
}
class PostRecycleAdapter(private var posts : List<Post>?): RecyclerView.Adapter<PostsViewHolder>() {
    override fun getItemCount(): Int {
        return posts?.size ?: 0
    }
    fun set(_posts: List<Post>) {
        posts = _posts
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val inflation=LayoutInflater.from(parent.context)
        val view = inflation.inflate(R.layout.post_row, parent, false)
        return PostsViewHolder(view);
    }


    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.bind(posts?.get(position) ?: return)
    }


}


class FeedFragment : Fragment() {
    //var adapter: PostRecycleAdapter? = null
    var adapter = PostRecycleAdapter(PostModel.shared.posts)
    var posts: MutableList<Post> = ArrayList()
    private lateinit var progressBar: ProgressBar
    private lateinit var  recyclerView: RecyclerView
    val postViewModel: PostViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_feed, container, false)
        view.findViewById<TextView>(R.id.noPostsTextView).visibility = View.GONE
        postViewModel.posts.observe(viewLifecycleOwner) { fetchedPosts ->
            if (fetchedPosts.size>0){
                view.findViewById<TextView>(R.id.noPostsTextView).visibility = View.GONE

            }
            posts.clear()
            posts.addAll(fetchedPosts)
            adapter?.set(posts)
            adapter?.notifyDataSetChanged()
            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }



        progressBar = view.findViewById(R.id.feedProgressBar)

        recyclerView = view.findViewById(R.id.fragment_feed_recycler_view)
        recyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        adapter = PostRecycleAdapter(posts)
        recyclerView.adapter = adapter
        getAllPosts()
        return view
    }

    override fun onResume() {
        super.onResume()
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        getAllPosts()

    }

    private fun getAllPosts() {

        postViewModel.fetchAllPosts()

    }
}