package com.example.kalender3

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.util.*


class MainActivity : Activity() {

    private var MIN_DAY = 1;
    private var MAX_DAY = 24;
    private var OVERRIDE_COUNT = 5;

    enum class ResourceType {
        IMAGE,
        AUDIO
    }

    data class ResourceItem(
        val resource_id: Int,
        val resource_type: ResourceType
    )

    data class DayInfo(
            var day: Int = 0,
            var door: Int = 0,
            var resources: List<ResourceItem>,
            var audio: Int? = null
    )

    private var _days : Array<DayInfo>? = null;

    private enum class FineResourceType {
        DOOR,
        IMAGE,
        AUDIO,
    }

    private data class DayResources(
            var door: Int?,
            var resources: MutableList<ResourceItem>
    )

    private fun parseName(name: String): Pair<Int, FineResourceType>? {
        var parts = name.split("_")
        if (parts.size < 2) {
            Log.w("Init", "Cannot split: ${name}")
            return null
        }
        var day = parts[1].toIntOrNull()
        if (day == null || day < 1 || day > 24) {
            Log.w("Init", "Cannot parse as day: ${parts[1]}")
            return null
        }
        when(parts[0]) {
            "door" -> {
                return Pair(day, FineResourceType.DOOR)
            }
            "image" -> {
                return Pair(day, FineResourceType.IMAGE)
            }
            "audio" -> {
                return Pair(day, FineResourceType.AUDIO)
            }
            else -> {
                Log.w("Init", "Unknown type: ${parts[0]}")
                return null
            }
        }
    }

    private fun processField(field: Field, resourceList: MutableList<DayResources>) {
        var resourceId = field.getInt(null)
        var name = resources.getResourceEntryName(resourceId)
        var parsedName = parseName(name)
        if (parsedName == null) {
            Log.w("Init", "Cannot parse name: $name")
            return
        }
        var day = resourceList[parsedName.first - 1]
        when (parsedName.second) {
            FineResourceType.DOOR -> {
                day.door = resourceId
            }
            FineResourceType.IMAGE -> {
                day.resources.add(ResourceItem(resourceId, ResourceType.IMAGE))
            }
            FineResourceType.AUDIO -> {
                day.resources.add(ResourceItem(resourceId, ResourceType.AUDIO))
            }
        }
    }

    private fun getDrawableResource() : List<DayResources>
    {
        var resourceList = mutableListOf<DayResources>();
        for (i in 1..24) {
            resourceList.add(DayResources(null, mutableListOf()))
        }
        val drawablesFields: Array<Field> = R.drawable::class.java.fields
        for (field in drawablesFields) {
            processField(field, resourceList)
        }
        val rawFields: Array<Field> = R.raw::class.java.fields
        for (field in rawFields) {
            processField(field, resourceList)
        }
        return resourceList
    }

    private fun GetDays() : Array<DayInfo> {
        var infos = mutableListOf<DayInfo>();

        val resourceList = getDrawableResource()

        for (i in resourceList.indices) {
            var drawable = resourceList.get(i)
            var day = i + 1

            if (drawable.door == null) {
                throw RuntimeException("No door for day: $day")
            }
            if (drawable.resources.isEmpty()) {
                throw RuntimeException("No image for day: $day")
            }

            var door: Int = drawable.door!!
            var dayInfo = DayInfo(day, door, drawable.resources)
            infos.add(dayInfo);
        }

        return infos.toTypedArray()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putInt("day", _day)
        savedInstanceState.putInt("overrideCounter", _overrideCounter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)
        _days = GetDays()

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

    private fun showDialog(resources: List<ResourceItem>, index: Int) {
        if (index >= resources.size) {
            return
        }
        val resource = resources.get(index)
        when (resource.resource_type) {
            ResourceType.IMAGE -> {
                showImageDialog(resources, index)
            }
            ResourceType.AUDIO -> {
                showAudioDialog(resources, index)
            }
        }
    }

    private fun showImageDialog(resources: List<ResourceItem>, index: Int) {
        val builder = AlertDialog.Builder(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.dialog_custom, null)
        val photoView = view.findViewById<PhotoView>(R.id.imageView)
        photoView.setImageResource(resources.get(index).resource_id)
        builder.setView(view)
        val mDialog = builder.create()
        mDialog.setOnDismissListener {
            showDialog(resources, index + 1)
        }
        photoView.setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.show()
    }

    private fun showAudioDialog(resources: List<ResourceItem>, index: Int) {
        val builder = AlertDialog.Builder(this@MainActivity)
        val view = layoutInflater.inflate(R.layout.audio_dialog, null)
        var playButton = view.findViewById<Button>(R.id.play);
        var seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        var mp = MediaPlayer.create(this, resources.get(index).resource_id);


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
                if (mp != null && fromUser) {
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
            showDialog(resources, index + 1)
        }

        view.setOnClickListener {
            mDialog.dismiss()
        }
        mDialog.show()
    }

    private fun openDoor() {
        var dayInfo = getDay(_day)
        showDialog(dayInfo.resources, 0)
    }

    private fun getDay(day: Int) : DayInfo {
        return _days!![(day - 1) % _days!!.size]
    }

    private fun update() {
        val dayView = findViewById<TextView>(R.id.day_view)
        dayView.text =_day.toString()
        val imageView = findViewById<ImageView>(R.id.image)
        imageView.setImageResource(getDay(_day).door)
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
