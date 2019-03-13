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

        int fromCount;
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
                fromCount = h.getFromFileRange().getLineStart();
                fromDisplay.add(disp1);
                for (Line line : h.getLines()) {
                    DiffDisplay disp2 = new DiffDisplay();
                    disp2.setDiffType(LINE);
                    disp2.setDiffObj(line);
                    if (line.getLineType() != Line.LineType.TO) {
                        disp2.setLineNum(String.valueOf(fromCount));
                        fromCount++;
                    } else
                        disp2.setLineNum("");
                    fromDisplay.add(disp2);
                }
            }
        }

        int toCount;
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
                toCount = h.getToFileRange().getLineStart();
                toDisplay.add(disp1);
                for (Line line : h.getLines()) {
                    DiffDisplay disp2 = new DiffDisplay();
                    disp2.setDiffType(LINE);
                    disp2.setDiffObj(line);
                    if (line.getLineType() != Line.LineType.FROM) {
                        disp2.setLineNum(String.valueOf(toCount));
                        toCount++;
                    } else
                        disp2.setLineNum("");
                    toDisplay.add(disp2);
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

    private static class ReadDiffFileTask extends AsyncTask<Void, Void, List<Diff>>{
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
