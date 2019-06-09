package com.arity.android.adamclockwork.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.arity.android.adamclockwork.R
import java.util.*

class SoundsFragment : DialogFragment() {

    companion object {
        val EXTRA_SOUNDS =
                "com.arity.android.adamclockwork.sounds_fragment"
        private val ARG_SOUNDS = "sounds"

        fun newInstance(soundURI: Uri): SoundsFragment {
            val args = Bundle()
            args.putString(ARG_SOUNDS, soundURI.toString())

            val fragment = SoundsFragment()
            fragment.arguments = args
            return fragment
        }

        private var mediaPlayer: MediaPlayer = MediaPlayer()
    }

    private lateinit var alarmSoundsRecyclerView: RecyclerView

    private lateinit var soundUri: Uri
    private var adapter: SoundsAdapter? = null


    private val names: ArrayList<String> = arrayListOf()
    private val uris: ArrayList<Uri> = arrayListOf()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        soundUri = Uri.parse(arguments!!.getString(ARG_SOUNDS))


        val manager = RingtoneManager(activity)
        manager.setType(RingtoneManager.TYPE_RINGTONE)
        val cursor = manager.cursor
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val ringtoneURI = manager.getRingtoneUri(cursor.position)
            names.add(title)
            uris.add(ringtoneURI)
            // Do something with the title and the URI of ringtone
        }

        val v = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_sounds, null)

        alarmSoundsRecyclerView = v.findViewById(R.id.alarm_sound_recycler_view)

        alarmSoundsRecyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = SoundsAdapter(names)
        alarmSoundsRecyclerView.adapter = adapter


        return android.support.v7.app.AlertDialog.Builder(activity!!, R.style.MyCustomTheme)
                .setView(v)
                .setTitle(getString(R.string.alarm_list_of_sounds))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    sendResult(Activity.RESULT_OK, soundUri)
                    try {
                        mediaPlayer.stop()
                        mediaPlayer.release()
                    } catch (t: Throwable) {
                    }
                }
                .create()
    }


    private inner class SoundsHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_sound, parent, false))
            , View.OnClickListener {

        override fun onClick(v: View?) {
            if (v == alarmSoundButton) {
                try {
                    try {
                        mediaPlayer.stop()
                        mediaPlayer.release()
                    } catch (t: Throwable) {
                    }
                    soundUri = uris[names.indexOf(alarmSoundButton.text)]
                    adapter?.notifyDataSetChanged()

                    mediaPlayer = MediaPlayer()
                    mediaPlayer.setDataSource(activity, soundUri)

                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                    mediaPlayer.isLooping = false
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (t: Throwable) {
                }
                Log.i("TAG", "Uri changed")
            }
        }

        private var sound: String? = null

        val alarmSoundButton: Button = itemView.findViewById(R.id.alarm_sound_item_text_view)
        val alarmSoundImageView = itemView.findViewById<ImageView>(R.id.alarm_sound_item_check_view)

        @SuppressLint("SetTextI18n")
        fun bind(sound: String) {
            this.sound = sound

            if (uris[names.indexOf(sound)] == soundUri) {
                alarmSoundImageView.visibility = View.VISIBLE
            } else {
                alarmSoundImageView.visibility = View.INVISIBLE

            }
            alarmSoundButton.text = sound
            alarmSoundButton.setOnClickListener(this)

        }

    }

    private inner class SoundsAdapter(private var sounds: ArrayList<String>?) : RecyclerView.Adapter<SoundsHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundsHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return SoundsHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: SoundsHolder, position: Int) {
            val sound = sounds!![position]
            holder.bind(sound)
        }

        override fun getItemCount(): Int {
            return sounds!!.size
        }

        fun setSounds(sounds: ArrayList<String>) {
            this.sounds = sounds
        }
    }

    private fun sendResult(resultCode: Int, soundUri: Uri) {
        if (targetFragment == null) {
            return
        }

        val intent = Intent()
        intent.putExtra(EXTRA_SOUNDS, soundUri.toString())

        targetFragment!!
                .onActivityResult(targetRequestCode, resultCode, intent)

        dialog.dismiss()
    }

}