package com.example.ease.ui.rest_apis

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ease.BuildConfig
import com.example.ease.R
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"



class ArticlesViewHolder (itemView: View): RecyclerView.ViewHolder(itemView) {
    var articleTitle: TextView? = null
    var authorName: TextView? = null
    var articleImage: ImageView? = null
    var articleSquare: LinearLayout?=null

    init {
        articleTitle = itemView.findViewById(R.id.articleTitle)
        authorName = itemView.findViewById(R.id.authorName)
        articleImage = itemView.findViewById(R.id.articleImage)
        articleSquare = itemView.findViewById(R.id.articleSquare)
    }
    fun bind(article: Article) {
        articleTitle?.text = article.articleTitle
        authorName?.text = article.authorName
        if(article.articleImage != "") {
            try {
                Picasso.get()
                    .load(article.articleImage)
                    .into(articleImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        articleSquare?.setOnClickListener {
            try {
                val url = article.articleUrl
                if (url.isNotEmpty() && url.startsWith("http")) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    itemView.context.startActivity(intent)
                } else {
                    Toast.makeText(itemView.context, "Invalid URL", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(itemView.context, "Failed to open article", Toast.LENGTH_SHORT).show()
            }
        }

    }

}

data class Article(
    val articleTitle: String,
    val authorName: String,
    val articleImage: String,
    val articleUrl: String
)



class ArticleRecycleAdapter(private var articles : List<Article>?): RecyclerView.Adapter<ArticlesViewHolder>() {
    override fun getItemCount(): Int {
        return articles?.size ?: 0
    }
    fun set(_articles: List<Article>) {
        articles = _articles
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlesViewHolder {
        val inflation=LayoutInflater.from(parent.context)
        val view = inflation.inflate(R.layout.article_row, parent, false)
        return ArticlesViewHolder(view);
    }


    override fun onBindViewHolder(holder: ArticlesViewHolder, position: Int) {
        holder.bind(articles?.get(position) ?: return)
    }


}





/**
 * A simple [Fragment] subclass.
 * Use the [articlesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class articlesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ArticleRecycleAdapter
    private var articles: List<Article> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_articles, container, false)
        progressBar = view.findViewById(R.id.articlesProgressBar)
        recyclerView= view.findViewById(R.id.articlesRecyclerView)

        recyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager

        // Initialize empty list
        articles = listOf()

        // Set up adapter
        adapter = ArticleRecycleAdapter(articles)
        recyclerView.adapter = adapter

        // Fetch articles from API
        fetchArticles()

        return view
    }

    private fun fetchArticles() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                progressBar.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://newsapi.org/v2/everything?q=PTSD OR post-traumatic stress disorder OR post trauma&sortBy=publishedAt&apiKey=${BuildConfig.Articles_Api}")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                if (responseData != null) {
                    // Parse the articles list directly
                    val jsonObject = Gson().fromJson(responseData, Map::class.java)
                    val articlesJson = jsonObject["articles"] as List<Map<String, Any>>

                    val fetchedArticles = articlesJson.map { articleMap ->
                        Article(
                            articleTitle = articleMap["title"] as? String ?: "No Title",
                            authorName = articleMap["author"] as? String ?: "Unknown Author",
                            articleImage = articleMap["urlToImage"] as? String ?: "",
                            articleUrl= articleMap["url"] as? String ?: ""
                        )
                    }

                    articles = fetchedArticles

                    // Update UI on Main Thread
                    CoroutineScope(Dispatchers.Main).launch {
                        adapter.set(articles)
                        adapter.notifyDataSetChanged()
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            } catch (e: IOException) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(requireContext(), "Failed to fetch articles", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment articlesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            articlesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}