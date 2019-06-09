package com.arity.android.adamclockwork.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.arity.android.adamclockwork.R
import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.vk.sdk.api.*
import com.vk.sdk.api.model.VKApiUserFull
import com.vk.sdk.api.model.VKList
import kotlinx.android.synthetic.main.list_item_friend.view.*

class GoodMorningFragment : DialogFragment() {

    private lateinit var alarmMessageEditText: EditText
    private lateinit var alarmFriendsRecyclerView: RecyclerView
    private lateinit var friendsChooseAllButton: Button
    private lateinit var friendsCpvLoad: CircularProgressView
    private lateinit var friendsTxtError: TextView


    private var friendsList: VKList<*> = VKList<VKApiUserFull>()
    private var checkList: Array<Int> = arrayOf()
    private var userIds = ""
    private var choosed = false

    private lateinit var adapter: MorningAdapter

    companion object {
        val EXTRA_MESSAGE =
                "com.arity.android.adamclockwork.message"
        val EXTRA_FRIENDS =
                "com.arity.android.adamclockwork.friends"
        private val ARG_MESSAGE = "message"
        private val ARG_FRIENDS = "friends"


        fun newInstance(message: String, friends: String): GoodMorningFragment {
            val args = Bundle()
            args.putString(ARG_MESSAGE, message)
            args.putString(ARG_FRIENDS, friends)
            val fragment = GoodMorningFragment()
            fragment.arguments = args
            return fragment
        }
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val v = LayoutInflater.from(activity).inflate(R.layout.dialog_morning, null)

        alarmMessageEditText = v.findViewById(R.id.alarm_message_edit_text)
        alarmMessageEditText.setText(arguments?.getString(ARG_MESSAGE))

        alarmFriendsRecyclerView = v.findViewById(R.id.alarm_friends_recycler_view)
        adapter = MorningAdapter(friendsList)

        if (arguments?.getString(ARG_FRIENDS) != null)
            userIds = arguments?.getString(ARG_FRIENDS)!!

        alarmFriendsRecyclerView.layoutManager = LinearLayoutManager(activity)
        alarmFriendsRecyclerView.adapter = adapter

        friendsChooseAllButton = v.findViewById(R.id.alarm_choose_all_friends_button)
        friendsChooseAllButton.setOnClickListener {
            if (choosed) {
                for (i in 0 until checkList.size) checkList[i] = 0
            } else {
                for (i in 0 until checkList.size) checkList[i] = 1
            }
            choosed = !choosed
            adapter.notifyDataSetChanged()
        }

        friendsCpvLoad = v.findViewById(R.id.alarm_friends_cpv)
        friendsTxtError = v.findViewById(R.id.alarm_friends_error)

        loadFriends()

        return android.support.v7.app.AlertDialog.Builder(activity!!, R.style.MyCustomTheme)
                .setView(v)
                .setPositiveButton(android.R.string.ok) { dialog, which ->

                    for (i in 0 until friendsList.size)
                        if (checkList[i] == 1) userIds += "${friendsList[i].fields.get("id")}R"

                    sendResult(Activity.RESULT_OK, userIds, alarmMessageEditText.text.toString())
                    dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    dismiss()
                }
                .create()
    }

    private inner class MorningHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_friend, parent, false)),
            CompoundButton.OnCheckedChangeListener {

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (buttonView!!.isPressed) {
                checkList[adapterPosition] = if (isChecked) 1 else 0
                if (!isChecked) choosed = false
                else {
                    choosed = true
                    for (i in 0 until alarmFriendsRecyclerView.childCount) {
                       if(alarmFriendsRecyclerView.findViewHolderForAdapterPosition(i)!=null) {
                           val holder = alarmFriendsRecyclerView.findViewHolderForAdapterPosition(i) as MorningHolder
                           if (holder.itemView.alarm_friend_check_box!!.isChecked) {
                               choosed = false
                           }
                       }
                    }
                }
            }

        }

        private val friendCheckBox: CheckBox = itemView.findViewById(R.id.alarm_friend_check_box)

        @SuppressLint("SetTextI18n")
        fun bind(user: VKApiUserFull) {
            friendCheckBox.text = user.first_name + " " + user.last_name
            friendCheckBox.setOnCheckedChangeListener(this)
            friendCheckBox.isChecked = checkList[adapterPosition] != 0
        }

    }

    fun loadFriends(){

        alarmFriendsRecyclerView.visibility = View.GONE
        friendsTxtError.visibility = View.GONE
        friendsCpvLoad.visibility = View.VISIBLE

        val request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id,first_name,last_name,photo_100,photo_max_orig,online,status"))
        request.executeWithListener(object : VKRequest.VKRequestListener() {

            override fun onComplete(response: VKResponse?) {
                super.onComplete(response)

                friendsList = response!!.parsedModel as VKList<*>

                checkList = emptyArray()

                for (i in 0..friendsList.size) checkList += 0

                for (i in 0 until friendsList.size) {
                    checkList[i] = if (userIds.contains(friendsList[i].fields.get("id").toString())) 1 else 0
                }

                adapter.setDays(friendsList)
                alarmFriendsRecyclerView.visibility = View.VISIBLE
                friendsCpvLoad.visibility = View.GONE
                friendsTxtError.visibility = View.GONE

                adapter.notifyDataSetChanged()

            }

            override fun onError(error: VKError?) {
                super.onError(error)

                friendsTxtError.text = getString(R.string.friends_search_error)

                alarmFriendsRecyclerView.visibility = View.GONE
                friendsCpvLoad.visibility = View.GONE
                friendsTxtError.visibility = View.VISIBLE
            }

        })
    }

    private inner class MorningAdapter(private var friends: VKList<*>) : RecyclerView.Adapter<MorningHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MorningHolder {
            val layoutInflater = LayoutInflater.from(activity)
            return MorningHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: MorningHolder, position: Int) {
            val friendID = friends[position]
            holder.bind((friendID as VKApiUserFull?)!!)

        }

        override fun getItemCount(): Int {
            return friends.size
        }

        fun setDays(friends: VKList<*>) {
            this.friends = friends
        }
    }

    private fun sendResult(resultCode: Int, userId: String, message: String) {
        if (targetFragment == null) {
            return
        }

        val intent = Intent()
        intent.putExtra(EXTRA_MESSAGE, message)
        intent.putExtra(EXTRA_FRIENDS, userId)

        targetFragment!!
                .onActivityResult(targetRequestCode, resultCode, intent)
    }
}