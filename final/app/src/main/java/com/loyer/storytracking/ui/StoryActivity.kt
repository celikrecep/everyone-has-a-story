package com.loyer.storytracking.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.loyer.storytracking.R
import com.loyer.storytracking.model.User
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity() {
    companion object {
        fun newInstance(context: Context, stories: ArrayList<User>) {
            val intent = Intent(context, StoryActivity::class.java)
            intent.putExtra("stories", stories)
            context.startActivity(intent)
        }
    }
    private var stories: ArrayList<User>? = null
    private lateinit var storyAdapter: StoryAdapter
    private var isStarter = true
    private var fragmentCount = 0
    private var pos = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)
        stories = intent.getSerializableExtra("stories") as ArrayList<User>
        storyAdapter = StoryAdapter(supportFragmentManager)
        //the first screen
        storyAdapter.addFragment(setupFragment(0, onStoryClickListener))

        vpStory.adapter = storyAdapter
        vpStory.offscreenPageLimit = 2

        vpStory.addOnPageChangeListener(onPageChangeListener)
    }


    //start the exoPlayer cuz timing
    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
        }
        // first triggered here, then triggering onPageSelected
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (isStarter) {
                (storyAdapter.getItem(position) as StoryFragment).initializePlayer()
                isStarter = false
            }
        }

        override fun onPageSelected(position: Int) {
            pos = position
            (storyAdapter.getItem(position) as StoryFragment).initializePlayer()
        }
    }

    override fun onBackPressed() {
        (storyAdapter.getItem(pos) as StoryFragment).releasePlayer()
        super.onBackPressed()
    }
    /**
     * this listener if the next item is null in VideoList, it adds a new fragment.
     * VideoList in WatchItFragment. this interface trigger to the WatchItFragment
     */
    private val onStoryClickListener = object : StoryFragment.IStoryClickListener {
        /**
         * This control was performed so that each forward pressed does not add a new fragment.
         */
        override fun onNext() {
            fragmentCount++
            if (fragmentCount < stories?.size!!  &&
                    fragmentCount == storyAdapter.count) {
                storyAdapter.addFragment(setupFragment(fragmentCount, this))
                vpStory.offscreenPageLimit = fragmentCount + 2
            } else
                onBackPressed()
            vpStory.currentItem = vpStory.currentItem + 1
        }

        override fun onPrevious() {
            fragmentCount--
            vpStory.currentItem = vpStory.currentItem - 1
        }
    }

    private fun setupFragment(position: Int, listener: StoryFragment.IStoryClickListener): Fragment {
        val fragment = StoryFragment.newInstance(stories?.get(position))
        fragment.setClickListener(listener)
        return fragment
    }
}
