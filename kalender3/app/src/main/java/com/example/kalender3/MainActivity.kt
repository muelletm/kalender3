package com.example.kalender3

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import java.util.*


class MainActivity : Activity() {

    private var MIN_DAY = 1;
    private var MAX_DAY = 24;
    private var OVERRIDE_COUNT = 5;

    data class DayInfo(
        var day: Int = 0,
        var door: Int = 0,
        var photo: Int = 0,
        var image: Int = 0,
        var audio: Int? = null
    )

    private var DAYS = arrayOf(
        DayInfo(1, R.drawable.door_01, R.drawable.image_01, R.drawable.image_01_laia),
        DayInfo(2, R.drawable.door_02, R.drawable.image_02, R.drawable.image_02_emma),
        DayInfo(3, R.drawable.door_03, R.drawable.image_03, R.drawable.image_03_laia, R.raw.twinkle_twinkle),
        DayInfo(4, R.drawable.door_04, R.drawable.image_04, R.drawable.image_04_emma),
        DayInfo(5, R.drawable.door_05, R.drawable.image_05, R.drawable.image_05_laia),
        DayInfo(6, R.drawable.door_06, R.drawable.image_06, R.drawable.image_06_laia, R.raw.tannenbaum),
        DayInfo(7, R.drawable.door_07, R.drawable.image_07, R.drawable.image_07_emma),
        DayInfo(8, R.drawable.door_08, R.drawable.image_08, R.drawable.image_08_emma),
        DayInfo(9, R.drawable.door_09, R.drawable.image_09, R.drawable.image_09_emma, R.raw.eurovision),
        DayInfo(10, R.drawable.door_10, R.drawable.image_10, R.drawable.image_10_thomas),
        DayInfo(11, R.drawable.door_11, R.drawable.image_11, R.drawable.image_11_laia),
        DayInfo(12, R.drawable.door_12, R.drawable.image_12, R.drawable.image_12_laia, R.raw.rudolf),
        DayInfo(13, R.drawable.door_13, R.drawable.image_13, R.drawable.image_13_emma),
        DayInfo(14, R.drawable.door_14, R.drawable.image_14, R.drawable.image_14_thomas),
        DayInfo(15, R.drawable.door_15, R.drawable.image_15, R.drawable.image_15_elena, R.raw.kumbaya),
        DayInfo(16, R.drawable.door_16, R.drawable.image_16, R.drawable.image_16_emma),
        DayInfo(17, R.drawable.door_17, R.drawable.image_17, R.drawable.image_17_archive),
        DayInfo(18, R.drawable.door_18, R.drawable.image_18, R.drawable.image_18_archive, R.raw.burrito_sabanero),
        DayInfo(19, R.drawable.door_19, R.drawable.image_19, R.drawable.image_19_thomas),
        DayInfo(20, R.drawable.door_20, R.drawable.image_20, R.drawable.image_20_elena),
        DayInfo(21, R.drawable.door_21, R.drawable.image_21, R.drawable.image_21_laia, R.raw.fruehling_lang),
        DayInfo(22, R.drawable.door_22, R.drawable.image_22, R.drawable.image_22_emma),
        DayInfo(23, R.drawable.door_23, R.drawable.image_23, R.drawable.image_23_emma),
        DayInfo(24, R.drawable.door_24, R.drawable.image_24, R.drawable.image_24_emma, R.raw.china_gong)
    )

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("day", _day)
        savedInstanceState.putInt("overrideCounter", _overrideCounter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)

        _day = getInitDay()
        _overrideCounter = 0
        if (savedInstanceState != null) {
            _day = savedInstanceState.getInt("day")
            _overrideCounter = savedInstanceState.getInt("overrideCounter")
        }

        findViewById<Button>(R.id.n_button).setOnClickListener {
            ++_day
            if (_day > MAX_DAY) _day = MIN_DAY
            update()
        }

        findViewById<Button>(R.id.p_button).setOnClickListener {
            _day -= 1;
            if (_day < MIN_DAY) _day = MAX_DAY
            update()
        }

        findViewById<TextView>(R.id.day_view).setOnClickListener {
            ++_overrideCounter
            update()
        }

        update()
    }

    private fun getInitDay(): Int {
        if (!isDecember()) {
            return MIN_DAY
        }
        val day = getCurrentDay()
        if (day < MIN_DAY) {
            return MIN_DAY
        }
        if (day > MAX_DAY) {
            return MAX_DAY
        }
        return day
    }

    private fun getCurrentDay(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun getCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH) + 1
    }

    private fun isDecember(): Boolean {
        return getCurrentMonth() == 12
    }

    private fun isJanuary(): Boolean {
        return getCurrentMonth() == 1
    }

    private fun canOpen(day: Int): Boolean {
        if (_overrideCounter > OVERRIDE_COUNT) {
            return true
        }
        if (isJanuary()) {
            return true
        }
        if (!isDecember()) {
            return false
        }
        return day <= getCurrentDay()
    }

    private fun showImageDialog(resource: Int, other: () -> Unit) {
        val builder = AlertDialog.Builder(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.dialog_custom, null)
        val photoView = view.findViewById<PhotoView>(R.id.imageView)
        photoView.setImageResource(resource)
        builder.setView(view)
        val mDialog = builder.create()
        mDialog.setOnDismissListener {
            other()
        }
        photoView.setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.show()
    }

    private fun showAudioDialog(resource: Int, other: () -> Unit) {
        val builder = AlertDialog.Builder(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.audio_dialog, null)
        var playButton = view.findViewById<Button>(R.id.play);
        var seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        var mp = MediaPlayer.create(this, resource);


        builder.setView(view)
        val mDialog = builder.create()
        val playText = playButton.text
        playButton.setOnClickListener {
            if (mp.isPlaying) {
                mp.pause()
                playButton.text = playText
            } else {
                mp.start();
                playButton.text = "||"
            }
        }

        var sampleSize = 1000;

        val mHandler = Handler()

        runOnUiThread(object : Runnable {
            override fun run() {
                if (mp != null) {
                    val mCurrentPosition: Int = mp.getCurrentPosition() / sampleSize
                    seekBar.setProgress(mCurrentPosition)
                }
                // Update every second.
                mHandler.postDelayed(this, 1000)
            }
        })


        seekBar.setMax(mp.duration / sampleSize);
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(mp != null && fromUser){
                    mp.seekTo(progress * sampleSize);
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        mDialog.setOnDismissListener {
            if (mp.isPlaying) {
                mp.stop()
            }
            other()
        }

        view.setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.show()
    }

    private fun openDoor() {
        val index = _day - 1
        var dayInfo = DAYS[index]
        showImageDialog(dayInfo.image, {showImageDialog(dayInfo.photo, {
            var audio = dayInfo.audio
            if (audio != null) {
                showAudioDialog(audio, {})
            }
        })})
    }

    private fun update() {
        val dayView = findViewById<TextView>(R.id.day_view)
        dayView.text =_day.toString()
        val imageView = findViewById<ImageView>(R.id.image)
        imageView.setImageResource(DAYS[(_day - 1) % DAYS.size].door)
        if (!canOpen(_day)) {
            // Set image to black & white.
            val matrix = ColorMatrix()
            matrix.setSaturation(0f)
            imageView.colorFilter = ColorMatrixColorFilter(matrix)
            imageView.setOnClickListener(null)
            dayView.setTextColor(Color.GRAY)
        } else {
            imageView.clearColorFilter()
            imageView.setOnClickListener {
                openDoor()
            }
            dayView.setTextColor(Color.BLACK)
        }
    }

    private var _day: Int = 0
    private var _overrideCounter: Int = 0;
}
