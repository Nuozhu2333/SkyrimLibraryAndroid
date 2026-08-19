package com.nuozhu.skyrimlibrary.utils

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nuozhu.skyrimlibrary.R

class BottomSheetDialogFragmentUpdateLog : BottomSheetDialogFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet_update_log, container, false)

        val buttonDismiss: ImageButton = view.findViewById(R.id.buttonDismiss)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buttonDismiss.tooltipText = getText(R.string.close)
        }
        buttonDismiss.setOnClickListener {
            dismiss()
        //关闭BottomSheet
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        return dialog
    }
}