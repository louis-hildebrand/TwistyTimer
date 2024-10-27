package com.aricneto.twistytimer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.aricneto.twistify.R;
import com.aricneto.twistytimer.fragment.dialog.AlgDialog;
import com.aricneto.twistytimer.puzzle.TrainerScrambler;
import com.aricneto.twistytimer.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class TrainerCursorAdapter extends AlgCursorAdapter {

    private List<String> selectedItems;
    private FragmentManager fragmentManager;
    private Context mContext;

    TrainerScrambler.TrainerSubset currentSubset;
    String currentPuzzleCategory;

    Drawable cardBackground;
    Drawable selectedCardBackground;

    public TrainerCursorAdapter(Context context, Cursor cursor, Fragment listFragment, TrainerScrambler.TrainerSubset subset, String category) {
        super(context, cursor, listFragment);
        Log.d("TrainerCursor", "Created trainerCursor " + subset + category);
        this.mContext = context;
        this.fragmentManager = listFragment.getFragmentManager();

        // Drawables for the cards
        cardBackground = ThemeUtils.createSquareDrawable(
                mContext,
                ThemeUtils.fetchAttrColor(mContext, R.attr.colorItemListBackground),
                0, 14, 0);
        selectedCardBackground = ThemeUtils.createSquareDrawable(
                mContext,
                ThemeUtils.fetchAttrColor(mContext, R.attr.colorItemListBackgroundSelected),
                Color.BLACK, 14, 2);

        selectedItems = new ArrayList<>();
        selectedItems.addAll(TrainerScrambler.fetchSelectedItems(subset, category));

        this.currentSubset = subset;
        this.currentPuzzleCategory = category;

    }

    private boolean isSelected(String name) {
        return selectedItems.contains(name);
    }

    public void selectAll() {
        int size = selectedItems.size();
        Log.d("TRAINER","selecteditems: " + size);
        selectedItems.clear();
        switch (currentSubset) {
            case OLL:
                if (size != 57) {
                    for (int i = 1; i < 58; i++)
                        selectedItems.add("OLL " + String.format(Locale.US, "%02d", i));
                }
                break;
            case PLL:
                if (size != 21) {
                    String[] pll_cases = {"H", "Ua", "Ub", "Z", "Aa", "Ab", "E", "F", "Ga", "Gb", "Gc", "Gd", "Ja", "Jb", "Na", "Nb", "Ra", "Rb", "T", "V", "Y"};
                    selectedItems.addAll(Arrays.asList(pll_cases));
                }
                break;
            case THREE_STYLE_CORNERS:
                // TODO: Add preference for buffer position?
                // TODO: Add preference for lettering scheme?
                ArrayList<Character> nonBufferStickers = new ArrayList<Character>(21);
                for (char c = 'A'; c <= 'X'; c++) {
                    if (c != 'C' && c != 'J' && c != 'M') {
                        nonBufferStickers.add(c);
                    }
                }
                for (char c1 : nonBufferStickers) {
                    for (char c2 : nonBufferStickers) {
                        if (c1 != c2) {
                            selectedItems.add("" + c1 + c2);
                        }
                    }
                }
                break;
        }
        TrainerScrambler.saveSelectedItems(currentSubset, currentPuzzleCategory, selectedItems);
    }

    private void toggleSelection(String name, CardView card) {
        if (!isSelected(name)) {
            selectedItems.add(name);
            card.setBackground(selectedCardBackground);
        } else {
            selectedItems.remove(name);
            card.setBackground(cardBackground);
        }
        TrainerScrambler.saveSelectedItems(currentSubset, currentPuzzleCategory, selectedItems);
    }

    @Override
    public void handleTime(AlgHolder holder, Cursor cursor) {
        super.handleTime(holder, cursor);

        long id = cursor.getLong(0);
        String pName = cursor.getString(2);

        if (isSelected(pName)) {
            holder.card.setBackground(selectedCardBackground);
        } else {
            holder.card.setBackground(cardBackground);
        }

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelection(pName, holder.card);
            }
        });

        holder.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLocked()) {
                    setIsLocked(true);
                    AlgDialog algDialog = AlgDialog.newInstance(id);
                    algDialog.show(fragmentManager, "alg_dialog");
                    algDialog.setDialogListener(TrainerCursorAdapter.this);
                }
                return true;
            }
        });
    }



    @Override
    public Cursor swapCursor(Cursor cursor) {
        super.swapCursor(cursor);
        //unselectAll();
        return cursor;
    }
}
