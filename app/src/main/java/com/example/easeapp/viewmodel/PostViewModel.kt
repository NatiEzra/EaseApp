package com.example.ease.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ease.model.Post
import com.example.ease.model.PostModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel(
    private val postModel: PostModel = PostModel.shared
) : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> get() = _posts

    private val _myPosts = MutableLiveData<List<Post>>()
    val myPosts: LiveData<List<Post>> get() = _myPosts

    private val _postCreateState = MutableLiveData<Result<Unit>>()
    val postCreateState: LiveData<Result<Unit>> get() = _postCreateState

    private val _postOperationState = MutableLiveData<Result<Unit>>()
    val postOperationState: LiveData<Result<Unit>> get() = _postOperationState

    private val _singlePost = MutableLiveData<Post?>()
    val singlePost: LiveData<Post?> get() = _singlePost

    fun fetchAllPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            postModel.getPosts { result ->
                _posts.postValue(result)
            }
        }
    }

    fun fetchMyPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            postModel.getMyposts { result ->
                _myPosts.postValue(result)
            }
        }
    }

    fun createPost(image: Bitmap?, text: String) {
        val email = postModel.authServer.getCurrentUserEmail()
        viewModelScope.launch(Dispatchers.IO) {
            postModel.addPost(email, image, text) { success, error ->
                if (success) _postCreateState.postValue(Result.success(Unit))
                else _postCreateState.postValue(Result.failure(Throwable(error)))
            }
        }
    }

    fun updatePost(postId: String, image: Bitmap?, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postModel.editPost(postId, image, text) { success, error ->
                if (success) _postOperationState.postValue(Result.success(Unit))
                else _postOperationState.postValue(Result.failure(Throwable(error)))
            }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postModel.deletePost(postId) { success, error ->
                if (success) _postOperationState.postValue(Result.success(Unit))
                else _postOperationState.postValue(Result.failure(Throwable(error)))
            }
        }
    }

    fun getPostById(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postModel.getPostById(postId) { post ->
                _singlePost.postValue(post)
            }
        }
    }
}
