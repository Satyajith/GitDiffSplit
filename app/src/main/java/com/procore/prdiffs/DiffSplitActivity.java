package com.procore.prdiffs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reflectoring.diffparser.api.DiffParser;
import io.reflectoring.diffparser.api.UnifiedDiffParser;
import io.reflectoring.diffparser.api.model.Diff;
import io.reflectoring.diffparser.api.model.Hunk;
import io.reflectoring.diffparser.api.model.Line;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import com.procore.prdiffs.model.DiffDisplay;
import com.procore.prdiffs.utils.Utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.procore.prdiffs.model.DiffDisplay.DiffType.DIFF;
import static com.procore.prdiffs.model.DiffDisplay.DiffType.HUNK_HEADER;
import static com.procore.prdiffs.model.DiffDisplay.DiffType.LINE;
import static com.procore.prdiffs.model.DiffDisplay.DiffType.SAME_LINE;

/* Activity that has logic to display and host the diff split views */
public class DiffSplitActivity extends AppCompatActivity {

    private static final String TAG = DiffSplitActivity.class.getSimpleName();

    @BindView(R.id.from_recview)
    RecyclerView fromRecyclerView;

    @BindView(R.id.to_recview)
    RecyclerView toRecyclerView;

    private List<Diff> diffList;
    private List<DiffDisplay> fromDisplay = new ArrayList<>();
    private List<DiffDisplay> toDisplay = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff_split);
        ButterKnife.bind(this);

        try {
            diffList = new ReadDiffFileTask().execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*
        Below code is to generate a list to display from(left) side.
        1. If Current line == TO, the to_ct gets incremented
        2. If Current line == FROM, the from_ct gets incremented and current object is added to the list
        3. If current line type is NEUTRAL, checks if(from_ct < to_ct), (to_ct - from_ct) is calculated
        and adds SAME_LINE Type objects to the list in a loop to display empty lines
        4. Separate checks and implementation if end of line is met
        */

        int fromLineCount;
        for (Diff dif : diffList) {
            DiffDisplay disp = new DiffDisplay();
            disp.setDiffObj(splitFileNames(dif.getFromFileName()));
            disp.setLineNum("");
            disp.setDiffType(DIFF);
            fromDisplay.add(disp);
            for (Hunk h : dif.getHunks()) {
                DiffDisplay disp1 = new DiffDisplay();
                disp1.setDiffType(HUNK_HEADER);
                disp1.setDiffObj(h);
                disp.setLineNum("");
                fromLineCount = h.getFromFileRange().getLineStart();
                fromDisplay.add(disp1);
                int from_ct = 0, to_ct = 0;
                for (int i = 0; i < h.getLines().size(); i++) {
                    Line line = h.getLines().get(i);
                    DiffDisplay disp2 = new DiffDisplay();
                    disp2.setDiffType(LINE);
                    if (i != h.getLines().size() - 1) {
                        switch (line.getLineType()) {
                            case TO:
                                to_ct++;
                                break;
                            case FROM:
                                from_ct++;
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(fromLineCount));
                                fromLineCount++;
                                fromDisplay.add(disp2);
                                break;
                            case NEUTRAL:
                                if (from_ct < to_ct) {
                                    for (int k = 0; k < (to_ct - from_ct); k++) {
                                        DiffDisplay disp3 = new DiffDisplay();
                                        disp3.setDiffType(SAME_LINE);
                                        disp3.setDiffObj(" ");
                                        disp3.setLineNum("");
                                        fromDisplay.add(disp3);
                                    }
                                }
                                from_ct = 0;
                                to_ct = 0;
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(fromLineCount));
                                fromLineCount++;
                                fromDisplay.add(disp2);
                                break;
                        }
                    } else {
                        switch (line.getLineType()) {
                            case TO:
                                to_ct++;
                                break;
                            case FROM:
                                from_ct++;
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(fromLineCount));
                                fromDisplay.add(disp2);
                                break;
                            case NEUTRAL:
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(fromLineCount));
                                fromDisplay.add(disp2);
                                break;
                        }
                        if (from_ct < to_ct) {
                            for (int k = 0; k < (to_ct - from_ct); k++) {
                                DiffDisplay disp3 = new DiffDisplay();
                                disp3.setDiffType(SAME_LINE);
                                disp3.setDiffObj(" ");
                                disp3.setLineNum("");
                                fromDisplay.add(disp3);
                            }
                        }
                        from_ct = 0;
                        to_ct = 0;
                    }
                }
            }
        }


        /*
        Below code is to generate a list to display to(right) side.
        1. If Current line == TO, the to_ct gets incremented and current object is added to the list
        2. If Current line == FROM, the from_ct gets incremented
        3. If current line type is NEUTRAL, checks if(to_ct < from_ct), (from_ct - to_ct) is calculated
        and adds SAME_LINE Type objects to the list in a loop to display empty lines
        4. Separate checks and implementation if end of line is met
        */
        int toLineCount;
        for (Diff dif : diffList) {
            DiffDisplay disp = new DiffDisplay();
            if (!splitFileNames(dif.getFromFileName()).equals(splitFileNames(dif.getToFileName())))
                disp.setDiffObj(splitFileNames(dif.getToFileName()));
            else
                disp.setDiffObj("");
            disp.setLineNum("");
            disp.setDiffType(DIFF);
            toDisplay.add(disp);
            for (Hunk h : dif.getHunks()) {
                DiffDisplay disp1 = new DiffDisplay();
                disp1.setDiffType(HUNK_HEADER);
                disp1.setDiffObj(h);
                disp1.setLineNum("");
                toLineCount = h.getToFileRange().getLineStart();
                toDisplay.add(disp1);
                int from_ct = 0, to_ct = 0;
                for (int i = 0; i < h.getLines().size(); i++) {
                    Line line = h.getLines().get(i);
                    DiffDisplay disp2 = new DiffDisplay();
                    disp2.setDiffType(LINE);
                    if (i != h.getLines().size() - 1) {
                        switch (line.getLineType()) {
                            case TO:
                                to_ct++;
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(toLineCount));
                                toLineCount++;
                                toDisplay.add(disp2);
                                break;
                            case FROM:
                                from_ct++;
                                break;
                            case NEUTRAL:
                                if (to_ct < from_ct) {
                                    for (int k = 0; k < (from_ct - to_ct); k++) {
                                        DiffDisplay disp3 = new DiffDisplay();
                                        disp3.setDiffType(SAME_LINE);
                                        disp3.setDiffObj(" ");
                                        disp3.setLineNum("");
                                        toDisplay.add(disp3);
                                    }
                                }
                                from_ct = 0;
                                to_ct = 0;
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(toLineCount));
                                toLineCount++;
                                toDisplay.add(disp2);
                                break;
                        }
                    } else {
                        switch (line.getLineType()) {
                            case TO:
                                to_ct++;
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(toLineCount));
                                toDisplay.add(disp2);
                                break;
                            case FROM:
                                from_ct++;
                                break;
                            case NEUTRAL:
                                disp2.setDiffObj(line);
                                disp2.setLineNum(String.valueOf(toLineCount));
                                toDisplay.add(disp2);
                                break;
                        }
                        if (to_ct < from_ct) {
                            for (int k = 0; k < (from_ct - to_ct); k++) {
                                DiffDisplay disp3 = new DiffDisplay();
                                disp3.setDiffType(SAME_LINE);
                                disp3.setDiffObj(" ");
                                disp3.setLineNum("");
                                toDisplay.add(disp3);
                            }
                        }
                        from_ct = 0;
                        to_ct = 0;
                    }
                }
            }
        }

        FromSplitAdapter fromAdapter = new FromSplitAdapter(fromDisplay, this);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        layoutManager1.setOrientation(RecyclerView.VERTICAL);
        fromRecyclerView.setLayoutManager(layoutManager1);
        fromRecyclerView.setItemAnimator(new DefaultItemAnimator());
        fromRecyclerView.setAdapter(fromAdapter);
        fromRecyclerView.setNestedScrollingEnabled(true);

        ToSplitAdapter toAdapter = new ToSplitAdapter(toDisplay, this);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
        layoutManager2.setOrientation(RecyclerView.VERTICAL);
        toRecyclerView.setLayoutManager(layoutManager2);
        toRecyclerView.setItemAnimator(new DefaultItemAnimator());
        toRecyclerView.setAdapter(toAdapter);
        toRecyclerView.setNestedScrollingEnabled(true);
    }

    private String splitFileNames(String input) {
        String[] ar = input.split("/", 2);
        return ar[1];
    }

    // To read the diff.txt file and parse it using DiffParser to retrieve list if Diffs.
    private static class ReadDiffFileTask extends AsyncTask<Void, Void, List<Diff>> {
        @Override
        protected List<Diff> doInBackground(Void... voids) {
            InputStream fiStream;
            List<Diff> dList = new ArrayList<>();
            try {
                String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                String fileName = "diff.txt";
                File f = new File(baseDir + "/" + fileName);
                fiStream = new FileInputStream(f);
                DiffParser parser = new UnifiedDiffParser();
                dList = parser.parse(fiStream);
                fiStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return dList;
        }
    }
}
