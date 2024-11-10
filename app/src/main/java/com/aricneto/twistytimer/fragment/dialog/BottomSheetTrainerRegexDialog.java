package com.aricneto.twistytimer.fragment.dialog;

import static com.aricneto.twistytimer.utils.TTIntent.ACTION_GENERATE_SCRAMBLE;
import static com.aricneto.twistytimer.utils.TTIntent.CATEGORY_UI_INTERACTIONS;
import static com.aricneto.twistytimer.utils.TTIntent.broadcast;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.aricneto.twistify.R;
import com.aricneto.twistytimer.activity.MainActivity;
import com.aricneto.twistytimer.puzzle.TrainerScrambler;
import com.aricneto.twistytimer.utils.ThemeUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Bottom sheet dialog to select 3BLD cases using a simple regex-like syntax.
 */
public class BottomSheetTrainerRegexDialog extends BottomSheetDialogFragment {
    private static final String KEY_SUBSET = "subset";
    private static final String KEY_CATEGORY = "category";

    @BindView(R.id.title)
    TextView titleView;
    @BindView(R.id.regex_case_select_instructions)
    TextView instructionsView;
    @BindView(R.id.button)
    AppCompatTextView button;
    @BindView(R.id.cases_regex)
    EditText casesRegex;
    @BindView(R.id.num_cases_selected)
    TextView numCasesSelected;
    private Unbinder mUnbinder;
    private TrainerScrambler.TrainerSubset currentSubset;
    private String currentCategory;

    public static BottomSheetTrainerRegexDialog newInstance(TrainerScrambler.TrainerSubset subset, String category) {
        if (!TrainerScrambler.TrainerSubset.THREE_STYLE_CORNERS.equals(subset)) {
            throw new IllegalArgumentException("Invalid trainer subset for BottomSheetTrainerRegexDialog: " + subset.toString());
        }

        BottomSheetTrainerRegexDialog fragment = new BottomSheetTrainerRegexDialog();
        Bundle args = new Bundle();
        args.putSerializable(KEY_SUBSET, subset);
        args.putString(KEY_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            currentSubset = (TrainerScrambler.TrainerSubset) getArguments().getSerializable(KEY_SUBSET);
            currentCategory = getArguments().getString(KEY_CATEGORY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View dialogView = inflater.inflate(R.layout.dialog_bottomsheet_regex_trainer, container);
        mUnbinder = ButterKnife.bind(this, dialogView);

        @SuppressLint("ResourceType")
        Drawable icon = ThemeUtils.tintDrawable(
                getContext(),
                R.drawable.ic_outline_control_camera_24px,
                ContextCompat.getColor(getContext(), R.color.md_blue_A700)
        );
        titleView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        instructionsView.setMovementMethod(LinkMovementMethod.getInstance());

        button.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        button.setOnClickListener(v -> {
            casesRegex.setText(".*");
        });

        // TODO: initialize casesRegex with the saved regex, or empty if there is none
        Set<String> previouslySelectedItems = TrainerScrambler.fetchCaseSelection(currentSubset, currentCategory);
        String initialRegex = "";
        for (String s : previouslySelectedItems) {
            initialRegex = s;
            break;
        }
        int initialNumCases = TrainerScrambler.fetchSelectedCaseSet(currentSubset, currentCategory).size();
        numCasesSelected.setText(getString(R.string.num_cases_selected, initialNumCases));
        casesRegex.setText(initialRegex);
        casesRegex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                try {
                    Pattern.compile(str);
                    TrainerScrambler.saveCaseSelection(currentSubset, currentCategory, List.of(str));
                    int numCases = TrainerScrambler.fetchSelectedCaseSet(currentSubset, currentCategory).size();
                    numCasesSelected.setText(getString(R.string.num_cases_selected, numCases));
                    casesRegex.setError(null);
                }
                catch (PatternSyntaxException e) {
                    casesRegex.setError("Syntax error");
                }
            }
        });

        return dialogView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        getLoaderManager().destroyLoader(MainActivity.ALG_LIST_LOADER_ID);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        broadcast(CATEGORY_UI_INTERACTIONS, ACTION_GENERATE_SCRAMBLE);
    }
}
