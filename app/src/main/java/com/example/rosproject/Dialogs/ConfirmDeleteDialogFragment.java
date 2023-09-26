package com.example.rosproject.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.example.rosproject.R;

public class ConfirmDeleteDialogFragment extends DialogFragment {
    public static final String NAME_KEY = "DELETE_ITEM_NAME_KEY";
    public static final String POSITION_KEY = "DELETE_ITEM_POSITION_KEY";

    private DialogListener mListener;
    private String mItemName;
    private int mPosition;

    @Override
    public void setArguments(Bundle args){
        super.setArguments(args);
        mItemName = args.getString(NAME_KEY,"");
        mPosition = args.getInt(POSITION_KEY,-1);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mListener = (DialogListener) activity;
        }catch (ClassCastException e){
            //ignore
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete)
                .setMessage("删除: " + "'" + mItemName + "'" + "?")
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onConfirmDeleteDialogPositiveClick(mPosition,mItemName);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onConfrimDeleteDialogNegativeClick();
                dialog.cancel();
            }
        });
        return builder.create();
    }
    public interface DialogListener{
        void onConfirmDeleteDialogPositiveClick(int position, String name);
        void onConfrimDeleteDialogNegativeClick();
    }
}
