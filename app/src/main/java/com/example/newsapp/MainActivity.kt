package com.example.newsapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

import com.example.newsapp.ui.theme.NewsAppTheme
import java.net.URL

const val API_KEY = "875b8a3070be44f1b6fe4f3fa044aed5"
const val BASE_URL = "https://newsapi.org/v2/"
private const val TAG = "NewsApp"

//define the structure of the api response, response contains a list of articles

data class NewsResponse(val articles: List<Article>)
//different DIY  remember the ?
data class Article(val title:String, val description: String?, val url: String)

interface NewsApiService{
    @GET("top-headlines")
    // function that can stop and restart
    suspend fun getTopHeadLines(
        @Query("apiKey") apiKey: String = API_KEY,
        //if category not provided it will be general
        @Query("category") category: String = "",
        @Query("country") country: String = "us"
    ):NewsResponse
}

//initialize retrofit with the base url
//create the newsapiservice implementation using retrofit
//provided a suspend function to fetch and return news articles for a specified
class NewsRepository{
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(NewsApiService::class.java)

    suspend fun getTopHeadLines(category: String): List<Article>{
        return service.getTopHeadLines(category = category).articles
    }
}

class NewsViewModel: ViewModel(){
    private val repository = NewsRepository()
    //mutable state to hold articles
    var articles by mutableStateOf<List<Article>>(emptyList())
        private set

    fun fetchNews(category: String){
        viewModelScope.launch {
            try{
                articles = repository.getTopHeadLines(category)
            }catch (e:Exception){
                Log.e(TAG, "Failed to fetch News: ${e.message}")
            }
        }
    }
}

// MainActivity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this).get(NewsViewModel::class.java)

        setContent {
            NewsApp(viewModel)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsApp(viewModel: NewsViewModel) {
    var selectedCategory by remember { mutableStateOf("general") }

    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("Newsly") })
        }
    ) { innerPadding ->
        // Apply the inner padding directly to the Column
        Column(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(16.dp)) {

            // to select category
            CategorySelector(selectedCategory) { category ->
                selectedCategory = category
                //trigger fetch news
                viewModel.fetchNews(category)
            }
            Spacer(modifier = Modifier.height(16.dp))
            // to display fetched by viewModel
            NewsList(viewModel.articles, modifier = Modifier.weight(1f))
        }
    }
}


@Composable
fun CategorySelector(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    // different DIY
    val categories = listOf("general", "business", "entertainment", "health", "science", "sports", "technology")

    LazyRow {
        //has to be items data list!!!
        items(categories){ category ->
            Button(
                onClick = {onCategorySelected(category)},
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(selectedCategory == category)
                    Color.Blue else Color.LightGray
                )
            ){
                Text(
                    category.replaceFirstChar {
                        if(it.isLowerCase())
                            it.titlecase(Locale.getDefault())
                        else it.toString() },
                    color = if(selectedCategory == category)
                        Color.White else MaterialTheme.colorScheme.onSurface

                )

            }
        }
    }
}


@Composable
// to display news article use card
fun NewsList(articles: List<Article>, modifier: Modifier = Modifier) {

    LazyColumn (
        modifier = Modifier
    ){
      items(articles){ article ->
          Card(
              modifier= Modifier
                  .fillMaxSize()
                  .padding(8.dp)
          ){
              Column(
                  modifier = Modifier.padding(16.dp)
              ) {
                  //differently DIY
                  Text(text = article.title, style = MaterialTheme.typography.headlineSmall)
                  Spacer(modifier = Modifier.height(8.dp))
                  //? mark
                  Text(text = article.description ?: "No description", style = MaterialTheme.typography.bodyMedium)
              }
          }
      }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewsAppTheme {
    }
}