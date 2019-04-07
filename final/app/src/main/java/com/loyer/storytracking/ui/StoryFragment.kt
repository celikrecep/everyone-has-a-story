package com.loyer.storytracking.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.loyer.storytracking.GlideApp
import com.loyer.storytracking.R
import com.loyer.storytracking.model.Story
import com.loyer.storytracking.model.User
import kotlinx.android.synthetic.main.fragment_story.*

class StoryFragment : Fragment() {

    companion object {
        fun newInstance(user: User?) =
                StoryFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("user", user)
                    }
                }
    }

    interface IStoryClickListener {
        fun onNext()
        fun onPrevious()
    }

    private lateinit var user: User
    private var exoPlayer: ExoPlayer? = null
    private var clickListener: IStoryClickListener? = null
    private val progressList = mutableListOf<ProgressBar>()
    private var timer: CountDownTimer? = null
    private var isStarter = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_story, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        user = arguments?.getSerializable("user") as User
        GlideApp.with(context!!)
                .load(context!!.resources.getIdentifier(user.path, null, "com.loyer.storytracking"))
                .centerCrop()
                .fitCenter()
                .into(imgWSUserPhoto)
        tvUsername.text = user.username

        setupNextClick()
        setupPreviousClick()
        ivCancelStory.setOnClickListener {
            releasePlayer()
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser) {
            releasePlayer()
            removeProgress()
        }
    }


    fun setClickListener(listener: IStoryClickListener) {
        this.clickListener = listener
    }

    fun initializePlayer() {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(
                context,
                DefaultRenderersFactory(context),
                DefaultTrackSelector(), DefaultLoadControl())
        pvStoryPlayer.player = exoPlayer
        pvStoryPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        exoPlayer?.playWhenReady = true
        val mediaSource = prepareMediaStore(user.stories)
       exoPlayer?.prepare(mediaSource, true, false)
        exoPlayer?.addListener(listener)
    }

    private val listener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_READY -> {
                    pbWatchStory.visibility = View.GONE
                    if (isStarter) {
                        updateProgressBar(exoPlayer?.duration!!, exoPlayer?.currentWindowIndex!!)
                        isStarter = false
                    }
                }
                //pressed llNext visible to the Progress
                Player.STATE_BUFFERING -> {
                    pbWatchStory.visibility = View.VISIBLE
                }
                //when playlist is done trigger to the onNext() for next the user (i mean fragment)
                Player.STATE_ENDED -> {
                    if (!exoPlayer?.hasNext()!!)
                        clickListener?.onNext()
                }
            }
        }

        //the player proceeds to by itself
        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            super.onTracksChanged(trackGroups, trackSelections)
            if (!isStarter)
                updateProgressBar(exoPlayer?.duration!!, exoPlayer?.currentWindowIndex!!)
        }
    }

    //Prepare the videoList from the activity
    private fun prepareMediaStore(list: List<Story>): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        for (videoUrl: Story in list) {
            val resource = RawResourceDataSource(context)
            val dataSpec = DataSpec(RawResourceDataSource.buildRawResourceUri(context?.resources?.getIdentifier(videoUrl.url, "raw", "com.loyer.storytracking")!!))
            resource.open(dataSpec)
            val factory = DataSource.Factory { resource }
            val mediaSource = ExtractorMediaSource.Factory(
                    factory
            ).createMediaSource(resource.uri)
            concatenatingMediaSource.addMediaSource(mediaSource)
            val weight: Float = 10f / list.size.toFloat()
            prepareProgress(weight)
        }
        return concatenatingMediaSource
    }

    fun releasePlayer() {
        if (exoPlayer != null) {
            exoPlayer?.stop()
            exoPlayer?.removeListener(listener)
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    // prepare progressbar as much as videoList size
    private fun prepareProgress(weight: Float) {
        val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
        progressBar.layoutParams =
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 5, weight)
        val progressBarAttr = progressBar.layoutParams as LinearLayout.LayoutParams
        progressBarAttr.setMargins(8, 10, 8, 10)
        progressBar.layoutParams = progressBarAttr
        progressBar.max = 100
        progressList.add(progressBar)
        progressBar.progressDrawable = ContextCompat.getDrawable(context!!, R.drawable.progress_drawable)

        llProgressContainer.addView(progressBar)
    }

    private fun removeProgress() {
        timer?.cancel()
        if (progressList.isNotEmpty()) {
            progressList.clear()
            llProgressContainer.removeAllViews()
        }
    }


    private fun updateProgressBar(duration: Long, progressIndex: Int?) {
        var progress = 1
        if (timer != null) {
            timer?.cancel()
            timer = null
        }
        timer = object : CountDownTimer(duration, 100) {
            override fun onFinish() {
                progressList[progressIndex!!].progress = 100
            }

            override fun onTick(p0: Long) {
                progressList[progressIndex!!].progress = (progress * 100 / (duration / 100).toInt())
                progress++
            }
        }.start()
    }

    //if videoList has a next video, play next
    //else trigger to the onNext() in activity
    private fun setupNextClick() {
        llNextStory.setOnClickListener {
            isStarter = true
            if (exoPlayer?.currentWindowIndex!! != -1) {
                timer?.cancel()
                progressList[exoPlayer?.currentWindowIndex!!].progress = 100
            }
            if (exoPlayer?.hasNext()!!)
                exoPlayer?.next()
            else {
                if (clickListener != null)
                    clickListener?.onNext()
            }
        }
    }

    //if videoList has a previous video, play previous
    //else trigger to the onPrevious() in activity
    private fun setupPreviousClick() {
        llPreviousStory.setOnClickListener {
            isStarter = true
            if (exoPlayer?.previousWindowIndex!! != -1) {
                timer?.cancel()
                progressList[exoPlayer?.previousWindowIndex!!].progress = 0
                progressList[exoPlayer?.currentWindowIndex!!].progress = 0
            }
            if (exoPlayer?.hasPrevious()!!)
                exoPlayer?.previous()
            else {
                if (clickListener != null)
                    clickListener?.onPrevious()
            }
        }
    }
}
