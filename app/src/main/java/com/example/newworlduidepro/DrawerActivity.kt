package com.nwg.newworlduide

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.navigation.ui.AppBarConfiguration
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import android.net.Uri
import android.view.ContextThemeWrapper
import com.nwg.newworlduide.databinding.ActivityDrawerBinding
import java.lang.reflect.Array.get

class DrawerActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityDrawerBinding
    var mp: MediaPlayer? = null
    var muteBtn: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawerNav()

        loadNews()

        backgroundMusic()

        popUpMenu()

    }


    private fun backgroundMusic() {
        mp = MediaPlayer.create(this@DrawerActivity, R.raw.soundtrack)
        mp?.start()
        mp?.setOnCompletionListener { mp?.start() }
    }

    private fun popUpMenu() {
        var popUpMenu: Button = findViewById(R.id.ham_menu)
        popUpMenu.startAnimation(AnimationUtils.loadAnimation(popUpMenu.context, R.anim.bounce))
        supportActionBar
        popUpMenu.setOnClickListener {

//            val menu = PopupMenu(this, popUpMenu)
            val wrapper: Context = ContextThemeWrapper(this, R.style.MyToolbarStyle)
            val popup = PopupMenu(wrapper, popUpMenu)
            popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when (item?.itemId) {
                        R.id.DB -> {
                        var i = Intent(this@DrawerActivity, DataBaseActivity::class.java)
                            i.putExtra("mute", muteBtn)
                        startActivity(i)
                        }
                        R.id.guide -> {
                            var i = Intent(this@DrawerActivity, GuidesActivity::class.java)
                            i.putExtra("mute", muteBtn)
                            startActivity(i)
                        }
                        R.id.builds -> {
                            var i = Intent(this@DrawerActivity, BuildsActivity::class.java)
                            i.putExtra("mute", muteBtn)
                            startActivity(i)
                        }
                        R.id.map -> {
                            var i = Intent(this@DrawerActivity, MapActivity::class.java)
                            i.putExtra("mute", muteBtn)
                            startActivity(i)
                        }

                    }
                    return true
                }

            })
            popup.show()
        }
    }

    fun drawerNav() {
        binding = ActivityDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarDrawer.toolbar)

//        val drawerLayout: DrawerLayout = binding.drawerLayout
//        val navView: NavigationView = binding.navView
//        val navController = findNavController(R.id.nav_host_fragment_content_drawer)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow), drawerLayout)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)




//        navView.setNavigationItemSelectedListener {
//            when (it.itemId) {
//                R.id.nav_gallery -> {
//                    Intent(this, Builds::class.java)
//                    true
//                }
//                else -> false
//            }
//        }
    }

    fun loadNews() {
        val progress: ProgressBar = findViewById(R.id.pb)
        val retrofit = Retrofit
            .Builder()
            .baseUrl("https://newworldfans.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val pref = getSharedPreferences("settings", MODE_PRIVATE)
        val source = pref.getString("source", "website-news")
        val page = 1.toString()
        val callable = retrofit.create(CallableInterface::class.java)
        val getNews = callable.getNews(source, page)
        getNews.enqueue(object : Callback<List<Article>> {
            override fun onResponse(call: Call<List<Article>>, response: Response<List<Article>>) {
                progress.visibility = View.GONE
                val news = response.body()
                val articles = news
                showNews(articles)
                Log.d("jsoon", articles.toString()  )
            }
            override fun onFailure(call: Call<List<Article>>, t: Throwable) {
                progress.visibility = View.GONE
                Toast.makeText(this@DrawerActivity, "Failed to show up the data, please check your internet connection and try again later", Toast.LENGTH_SHORT).show();
                Log.d("json", "${t.localizedMessage} ")
            }
        })
    }


    fun showNews(articles: List<Article>?){

        val rv : RecyclerView = findViewById(R.id.rv)
        val manager = LinearLayoutManager(this)
        rv.layoutManager = manager
        val adapter = NewsAdpater(this, articles)
        rv.adapter = adapter

        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisibleItemPosition: Int = manager.findLastVisibleItemPosition()
                val totalItemCount = recyclerView!!.layoutManager?.itemCount
                super.onScrolled(recyclerView, dx, dy)
                if (lastVisibleItemPosition == totalItemCount){

                }
            }
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                val totalItemCount = recyclerView!!.layoutManager?.itemCount
//                val lastVisibleItemPosition: Int = manager.findLastVisibleItemPosition()
//                if (totalItemCount == lastVisibleItemPosition + 1) {
//
//                }
//            }
        })
//        if (manager.findFirstCompletelyVisibleItemPosition() == 0){
//
//        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.refresh, menu)
        menuInflater.inflate(R.menu.drawer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var id: Int = item.itemId
        if (id == R.id.item_refresh){
                loadNews()
        }
        if (id == R.id.mute){
            if (muteBtn == false) {
                item.setIcon(R.drawable.mute_on)
                mp?.setVolume(0F, 0F)
                mp?.pause()
            }
            if (muteBtn == true) {
                item.setIcon(R.drawable.ic_baseline_music_off_24)
                mp?.setVolume(1F, 1F)
                mp?.start()
            }
            muteBtn = if (muteBtn == false) true else false
        }
        if (id == R.id.action_settings){
            val i = Intent(this, SettingsActivity::class.java)
            startActivity(i)
        }
        if (id == R.id.about){
            val i = Intent(this, AboutActivity::class.java)
            startActivity(i)
        }
        if (id == R.id.rate){
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=com.nwg.newworlduidepro")
            startActivity(intent)
        }


        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val exitDialog = ExitDialog()
        exitDialog.isCancelable = false
        exitDialog.show(supportFragmentManager, null)
    }

    override fun onDestroy() {
        mp?.stop()
        mp?.release()
        super.onDestroy()
    }

    override fun onTrimMemory(level: Int) {
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            if(mp != null){
                mp?.pause()
            }
        }
        super.onTrimMemory(level)
    }

    override fun onResume() {
        mp?.pause()
        mp?.start()
        super.onResume()
    }

    override fun onPause() {
        finish()
        super.onPause()
    }


}


