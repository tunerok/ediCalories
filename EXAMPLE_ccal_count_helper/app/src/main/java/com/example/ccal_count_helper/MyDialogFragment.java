package com.example.ccal_count_helper;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.Toast;

public class MyDialogFragment extends AppCompatDialogFragment {


    private EditNameDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getString(R.string.my_dialog_title);

        String btn_txt_agree = getString(R.string.my_dialog_agree);
        String btn_txt_nope =  getString(R.string.my_dialog_disagree);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);  // заголовок
        //builder.setMessage(message); // сообщение
        builder.setPositiveButton(btn_txt_agree, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Toast.makeText(getActivity(), "Вы сделали правильный выбор",
                //        Toast.LENGTH_LONG).show();
                listener.onFinishEditDialog("sasai");

            }
        });
        builder.setNegativeButton(btn_txt_nope, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Toast.makeText(getActivity(), "Возможно вы правы", Toast.LENGTH_LONG)
                 //       .show();
                listener.onFinishEditDialog("sasai");
                dismiss();
            }
        });
        //builder.setCancelable(true);

        return builder.create();
    }


    public interface EditNameDialogListener {
        void onFinishEditDialog(String inputText);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the EditNameDialogListener so we can send events to the host
            listener = (EditNameDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement EditNameDialogListener");
        }
    }


}
