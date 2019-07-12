package com.ducktapedapps.updoot.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ducktapedapps.updoot.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class accountsBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accounts_modal_bottom_sheet, container, false);

        List<String> accounts = new ArrayList<>();
        accounts.add("Add new account");
        ListView accountLV = view.findViewById(R.id.accountsLV);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(inflater.getContext(), R.layout.modal_sheet_account_item, R.id.bottom_sheet_userName, accounts);
        accountLV.setAdapter(adapter);

        accountLV.setOnItemClickListener((parent, view1, position, id) -> {
            mListener.onButtonClicked(adapter.getItem(position));
            dismiss();
        });
        return view;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "has not implemented BottomSheetListener");
        }

    }

    public interface BottomSheetListener {
        void onButtonClicked(String text);
    }
}
