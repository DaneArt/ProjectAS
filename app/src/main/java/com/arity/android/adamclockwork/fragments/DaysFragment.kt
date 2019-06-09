package com.arity.android.adamclockwork.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import com.arity.android.adamclockwork.R
import kotlinx.android.synthetic.main.dialog_days.view.*

class DaysFragment : DialogFragment(), CompoundButton.OnCheckedChangeListener {

    private val TAG = DaysFragment::class.java.simpleName

    private lateinit var mChkSun: CheckBox
    private lateinit var mChkMon: CheckBox
    private lateinit var mChkTue: CheckBox
    private lateinit var mChkWed: CheckBox
    private lateinit var mChkThu: CheckBox
    private lateinit var mChkFri: CheckBox
    private lateinit var mChkSat: CheckBox

    private lateinit var mBtnSelectAll: Button

    private var checkedDays = 0

    companion object {
        val EXTRA_DAYS =
                "com.arity.android.adamclockwork.days_fragment"
        private val ARG_DAYS = "days"

        fun newInstance(repeatable: String): DaysFragment {
            val args = Bundle()
            args.putString(ARG_DAYS, repeatable)

            val fragment = DaysFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val v = LayoutInflater.from(activity as Activity)
                .inflate(R.layout.dialog_days, null)

        initCheckBoxes(view = v)

        mBtnSelectAll = v.findViewById(R.id.btnSelectDays)
        mBtnSelectAll.setOnClickListener {
            if (checkedDays != 7) {
                selectAll()
                checkedDays = 7
            } else{
                deselectAll()
                checkedDays = 0
            }
        }

        return android.support.v7.app.AlertDialog.Builder(activity!!, R.style.MyCustomTheme)
                .setView(v)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    sendResult(resultCode = Activity.RESULT_OK, days = collectChecks())
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    dismiss()
                }
                .create()
    }

    private fun deselectAll() {
        mChkSun.isChecked = false
        mChkMon.isChecked = false
        mChkTue.isChecked = false
        mChkWed.isChecked = false
        mChkThu.isChecked = false
        mChkFri.isChecked = false
        mChkSat.isChecked = false
    }

    private fun selectAll() {
        mChkSun.isChecked = true
        mChkMon.isChecked = true
        mChkTue.isChecked = true
        mChkWed.isChecked = true
        mChkThu.isChecked = true
        mChkFri.isChecked = true
        mChkSat.isChecked = true
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView!!.isPressed) {
            if (isChecked) checkedDays++
            else checkedDays--
        }
    }

    private fun initCheckBoxes(view: View) {
        mChkMon = view.findViewById(R.id.chkMon)
        mChkTue = view.findViewById(R.id.chkTue)
        mChkWed = view.findViewById(R.id.chkWed)
        mChkThu = view.findViewById(R.id.chkThu)
        mChkFri = view.findViewById(R.id.chkFri)
        mChkSat = view.findViewById(R.id.chkSat)
        mChkSun = view.findViewById(R.id.chkSun)

        mChkMon.setOnCheckedChangeListener(this)
        mChkTue.setOnCheckedChangeListener(this)
        mChkWed.setOnCheckedChangeListener(this)
        mChkThu.setOnCheckedChangeListener(this)
        mChkFri.setOnCheckedChangeListener(this)
        mChkSat.setOnCheckedChangeListener(this)
        mChkSun.setOnCheckedChangeListener(this)

        val days = arguments?.getString(ARG_DAYS)?.split("")?.drop(1)?.dropLast(1)?.map { it.toInt() }

        if (days != null) {
            checkedDays = days.size
            mChkSun.isChecked = days.contains(1)
            mChkMon.isChecked = days.contains(2)
            mChkTue.isChecked = days.contains(3)
            mChkWed.isChecked = days.contains(4)
            mChkThu.isChecked = days.contains(5)
            mChkFri.isChecked = days.contains(6)
            mChkSat.isChecked = days.contains(7)
        }

    }

    private fun collectChecks(): String {
        var result = ""

        if (mChkSun.isChecked) result += "1"
        if (mChkMon.isChecked) result += "2"
        if (mChkTue.isChecked) result += "3"
        if (mChkWed.isChecked) result += "4"
        if (mChkThu.isChecked) result += "5"
        if (mChkFri.isChecked) result += "6"
        if (mChkSat.isChecked) result += "7"

        if (result == "") return "0"
        return result
    }


    private fun sendResult(resultCode: Int, days: String) {
        if (targetFragment == null) {
            return
        }

        val intent = Intent()
        intent.putExtra(EXTRA_DAYS, days)

        targetFragment!!
                .onActivityResult(targetRequestCode, resultCode, intent)
    }

}