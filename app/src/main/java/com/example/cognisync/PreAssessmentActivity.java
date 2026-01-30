package com.example.cognisync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cognisync.del.ApiClient;
import com.example.cognisync.del.ApiService;
import com.example.cognisync.model.ScoreRequest;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreAssessmentActivity extends AppCompatActivity {

    private LinearLayout questionContainer;
    private TextView titleText, instructionText;
    private ImageButton backButton;
    private Button btnNext;

    private ApiService api;
    private String moduleType;
    private final List<QuestionItem> questions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment);

        // -------------------------
        // SAFE: adjust top padding for status bar / keyboard
        // -------------------------
        View container = findViewById(R.id.container); // ensure your XML has id="container"
        if (container != null) {
            final int left = container.getPaddingLeft();
            final int top = container.getPaddingTop();
            final int right = container.getPaddingRight();
            final int bottom = container.getPaddingBottom();

            ViewCompat.setOnApplyWindowInsetsListener(container, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());

                int topInset = systemBars.top;
                int bottomInset = Math.max(systemBars.bottom, ime.bottom);

                v.setPadding(
                        left,
                        top + topInset,
                        right,
                        bottom + bottomInset
                );
                return insets;
            });
            ViewCompat.requestApplyInsets(container);
        }
        // -------------------------

        titleText       = findViewById(R.id.titleText);
        instructionText = findViewById(R.id.instructionText);
        backButton      = findViewById(R.id.backButton);
        btnNext         = findViewById(R.id.btnNext);
        questionContainer = findViewById(R.id.questionContainer);

        moduleType = getIntent().getStringExtra("module_type");
        if (moduleType == null) moduleType = "focused_attention";

        titleText.setText(getReadableModuleTitle(moduleType));
        instructionText.setText(getInstructionText(moduleType));

        api = ApiClient.getClient().create(ApiService.class);

        loadQuestions(moduleType);
        displayQuestions();

        backButton.setOnClickListener(v -> finish());
        btnNext.setOnClickListener(v -> submit());
    }

    //==============================================================
    // 1️⃣ LOAD QUESTIONS (POOL + RANDOM 4-5)
    //==============================================================
    private void loadQuestions(String type) {
        questions.clear();
        switch (type) {

            case "focused_attention":
                questions.addAll(getRandomQuestions(getMAAS()));
                break;

            case "emotional_regulation":
                questions.addAll(getRandomPanasQuestions(getPANAS()));
                break;

            case "present_moment":
            case "present_moment_awareness":
                questions.addAll(getRandomQuestions(getPHLMS()));
                break;

            case "working_memory":
                questions.addAll(getRandomQuestions(getCFQ()));
                break;

            case "cognitive_flexibility":
                questions.addAll(getRandomQuestions(getDASS()));
                break;
        }
    }

    /**
     * Generic random selector: choose 4 or 5 items (randomly) from the pool
     * but never more than the pool size.
     */
    private List<QuestionItem> getRandomQuestions(List<QuestionItem> pool) {
        if (pool == null || pool.isEmpty()) return Collections.emptyList();
        List<QuestionItem> copy = new ArrayList<>(pool);
        Collections.shuffle(copy);
        int desired = chooseFourOrFive(copy.size()); // result in [1..size], usually 4 or 5
        return new ArrayList<>(copy.subList(0, desired));
    }

    /**
     * PANAS needs at least one positive and one negative item so scoring is meaningful.
     * We'll pick a total of 4 or 5 items with at least 1 positive and 1 negative.
     */
    private List<QuestionItem> getRandomPanasQuestions(List<QuestionItem> pool) {
        if (pool == null || pool.isEmpty()) return Collections.emptyList();

        List<QuestionItem> positives = new ArrayList<>();
        List<QuestionItem> negatives = new ArrayList<>();
        for (QuestionItem q : pool) {
            if (q.positive) positives.add(q);
            else negatives.add(q);
        }

        Collections.shuffle(positives);
        Collections.shuffle(negatives);

        int total = chooseFourOrFive(pool.size());
        // Ensure at least one from each side if possible
        int takePos = 0, takeNeg = 0;

        if (positives.size() == 0) {
            takeNeg = Math.min(total, negatives.size());
        } else if (negatives.size() == 0) {
            takePos = Math.min(total, positives.size());
        } else {
            // balanced split: attempt 50/50 rounding
            takePos = Math.max(1, total / 2);
            takeNeg = total - takePos;

            // adjust if one side doesn't have enough
            if (takePos > positives.size()) {
                takePos = positives.size();
                takeNeg = Math.min(total - takePos, negatives.size());
            }
            if (takeNeg > negatives.size()) {
                takeNeg = negatives.size();
                takePos = Math.min(total - takeNeg, positives.size());
            }

            // final guard: ensure at least one from each if both available
            if (takePos == 0 && positives.size() > 0) { takePos = 1; takeNeg = Math.max(0, total - 1); }
            if (takeNeg == 0 && negatives.size() > 0) { takeNeg = 1; takePos = Math.max(0, total - 1); }
        }

        List<QuestionItem> result = new ArrayList<>();
        result.addAll(positives.subList(0, Math.min(takePos, positives.size())));
        result.addAll(negatives.subList(0, Math.min(takeNeg, negatives.size())));
        // shuffle combined to randomize order presented
        Collections.shuffle(result);
        return result;
    }

    /**
     * Choose either 4 or 5 questions randomly, but never exceed poolSize.
     * If poolSize < 4, return poolSize.
     */
    private int chooseFourOrFive(int poolSize) {
        if (poolSize <= 0) return 0;
        if (poolSize <= 4) return poolSize;
        // poolSize >= 5: choose randomly 4 or 5
        Random r = new Random();
        return 4 + r.nextInt(2); // 4 or 5
    }

    //==============================================================
    // 2️⃣ ----- DYNAMIC UI (inflate card + styled spinner) -----
    //==============================================================
    private void displayQuestions() {
        questionContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < questions.size(); i++) {
            QuestionItem q = questions.get(i);

            // Inflate the card layout (rounded grey outer + inner spinner box)
            View card = inflater.inflate(
                    R.layout.item_question_spinner,
                    questionContainer,
                    false
            );

            TextView tv = card.findViewById(R.id.tvQuestion);
            Spinner sp = card.findViewById(R.id.spinnerAnswer);

            tv.setText((i + 1) + ". " + q.question);

            // Adapter using your custom spinner row layouts
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    R.layout.spinner_selected_item,
                    q.options()
            );
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            sp.setAdapter(adapter);

            // remember spinner for later scoring
            q.spinner = sp;

            // add to container
            questionContainer.addView(card);
        }
    }

    //==============================================================
    // 3️⃣ ----- SUBMIT -----
    //==============================================================
    private void submit() {
        if (hasUnanswered()) {
            Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
            return;
        }

        float score = computeScore(moduleType);
        send(score);
    }

    private boolean hasUnanswered() {
        for (QuestionItem q : questions)
            if (q.value() == -1) return true;
        return false;
    }

    //==============================================================
    // 4️⃣ ----- API -----
    //==============================================================
    private void send(float score) {
        String email = getUserEmail();
        if (email.isEmpty()) {
            Toast.makeText(this, "Email missing", Toast.LENGTH_LONG).show();
            return;
        }

        ScoreRequest req = new ScoreRequest(email, score);

        Call<Void> call;
        switch (moduleType) {
            case "focused_attention":      call = api.saveMaasPre(req);  break;
            case "emotional_regulation":   call = api.savePanasPre(req); break;
            case "working_memory":         call = api.saveCfqPre(req);   break;
            case "cognitive_flexibility":  call = api.saveDassPre(req);  break;
            case "present_moment":
            case "present_moment_awareness":
                call = api.savePhlmsPre(req); break;
            default:
                Toast.makeText(this,"Unknown module.",Toast.LENGTH_SHORT).show();
                return;
        }

        call.enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> c, Response<Void> r) {
                if (!r.isSuccessful()) {
                    Toast.makeText(PreAssessmentActivity.this,"Server "+r.code(),Toast.LENGTH_LONG).show();
                    return;
                }

                // Persist local completion flag
                SharedPreferences sp = getSharedPreferences("AssessmentStatus", MODE_PRIVATE);
                sp.edit()
                        .putBoolean(moduleType + "_pre_done", true)
                        .apply();

                // Allow caller to detect success (harmless if not used)
                setResult(RESULT_OK);
                finish();
            }
            @Override public void onFailure(Call<Void> c, Throwable t) {
                Toast.makeText(PreAssessmentActivity.this,"Network "+t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getUserEmail() {
        return getSharedPreferences("UserPrefs",MODE_PRIVATE)
                .getString("email","");
    }

    //==============================================================
    // 5️⃣ ----- NORMALIZED SCORE LOGIC (0–100 for ALL) -----
    // (UNCHANGED)
    //==============================================================
    private float computeScore(String type) {

        switch (type) {

            // MAAS 1→6 higher=better (mindfulness)
            case "focused_attention": {
                float maasSum = 0;
                for (QuestionItem q : questions) maasSum += q.value();
                float maasAvg = maasSum / questions.size();          // in [1,6]
                return normalizeLinear(maasAvg, 1f, 6f);             // → [0,100]
            }

            // PANAS: positive − negative in approx [-4, +4]
            // map diff linearly: -4 → 0, 0 → 50, 4 → 100
            case "emotional_regulation": {
                float pos = 0, neg = 0;
                int pc = 0, nc = 0;

                for (QuestionItem q : questions) {
                    if (q.positive) { pos += q.value(); pc++; }
                    else            { neg += q.value(); nc++; }
                }

                if (pc == 0 || nc == 0) {
                    // fallback neutral
                    return 50f;
                }

                float posAvg = pos / pc;
                float negAvg = neg / nc;
                float diff = posAvg - negAvg;           // ~[-4,4]
                return normalizeLinear(diff, -4f, 4f);  // → [0,100]
            }

            // DASS lower=better; convert to "wellness" %
            case "cognitive_flexibility": {
                float dSum = 0;
                for (QuestionItem q : questions) dSum += q.value();
                float maxD = questions.size() * 3f;     // each item 0–3
                if (maxD <= 0f) return 0f;
                float stressPct = (dSum / maxD) * 100f; // 0–100 stress
                float wellness = 100f - stressPct;      // 100 good, 0 bad
                return clamp100(wellness);
            }

            // CFQ more mistakes = worse, scale to 0–100 and invert
            case "working_memory": {
                float cfqSum = 0;
                for (QuestionItem q : questions) cfqSum += q.value();
                float min = questions.size() * 1f;      // all "never"
                float max = questions.size() * 5f;      // all "always"
                if (max <= min) return 0f;
                float raw = (cfqSum - min) / (max - min) * 100f; // 0–100 bad
                float wmScore = 100f - raw;             // higher = better
                return clamp100(wmScore);
            }

            // PHLMS 1→5 high=better
            case "present_moment":
            case "present_moment_awareness": {
                float total = 0;
                for (QuestionItem q: questions) total += q.value();
                float avg = total / questions.size();        // [1,5]
                return normalizeLinear(avg, 1f, 5f);         // → [0,100]
            }
        }
        return 0;
    }

    private float normalizeLinear(float v, float min, float max) {
        if (max <= min) return 0f;
        float norm = (v - min) / (max - min) * 100f;
        return clamp100(norm);
    }

    private float clamp100(float v) {
        if (v < 0f) return 0f;
        if (v > 100f) return 100f;
        return v;
    }

    //==============================================================
    // 6️⃣ QUESTION MODEL
    //==============================================================
    static class QuestionItem {
        String question;
        int min, max;
        boolean positive;
        Spinner spinner;

        QuestionItem(String q,int min,int max,boolean pos){
            this.question=q;this.min=min;this.max=max;this.positive=pos;
        }

        List<String> options(){
            List<String> arr=new ArrayList<>();
            arr.add("Select");
            for(int i=min;i<=max;i++) arr.add(String.valueOf(i));
            return arr;
        }

        int value(){
            if(spinner==null) return -1;
            int idx=spinner.getSelectedItemPosition();
            if(idx==0) return -1;
            return Integer.parseInt(spinner.getSelectedItem().toString());
        }
    }

    //==============================================================
    // 7️⃣ QUESTION POOLS — SAME AS BEFORE (UNCHANGED)
    //==============================================================
    private List<QuestionItem> getMAAS(){
        return Arrays.asList(
                new QuestionItem("I rush without paying attention.",1,6,false),
                new QuestionItem("I get easily distracted.",1,6,false),
                new QuestionItem("I fail to notice small details.",1,6,false),
                new QuestionItem("I lose track of the moment.",1,6,false),
                new QuestionItem("I do tasks automatically.",1,6,false),
                new QuestionItem("My mind wanders during tasks.",1,6,false),
                new QuestionItem("I think about future instead of now.",1,6,false)
        );
    }

    private List<QuestionItem> getPANAS(){
        return Arrays.asList(
                new QuestionItem("Interested",1,5,true),
                new QuestionItem("Excited",1,5,true),
                new QuestionItem("Strong",1,5,true),
                new QuestionItem("Enthusiastic",1,5,true),
                new QuestionItem("Alert",1,5,true),
                new QuestionItem("Upset",1,5,false),
                new QuestionItem("Guilty",1,5,false),
                new QuestionItem("Scared",1,5,false),
                new QuestionItem("Hostile",1,5,false),
                new QuestionItem("Nervous",1,5,false)
        );
    }

    private List<QuestionItem> getPHLMS(){
        return Arrays.asList(
                new QuestionItem("I noticed sensations in my body.",1,5,true),
                new QuestionItem("I was aware of my breathing.",1,5,true),
                new QuestionItem("I observed my thoughts calmly.",1,5,true),
                new QuestionItem("I was present with actions.",1,5,true),
                new QuestionItem("I noticed emotions as they appeared.",1,5,true),
                new QuestionItem("I felt subtle body sensations.",1,5,true),
                new QuestionItem("I paid attention to the present moment.",1,5,true)
        );
    }

    private List<QuestionItem> getCFQ(){
        return Arrays.asList(
                new QuestionItem("I forget why I entered a room.",1,5,true),
                new QuestionItem("I lose track mid-task.",1,5,true),
                new QuestionItem("I misplace items I just used.",1,5,true),
                new QuestionItem("I forget instructions quickly.",1,5,true),
                new QuestionItem("I forget daily routines.",1,5,true),
                new QuestionItem("I forget appointments.",1,5,true)
        );
    }

    private List<QuestionItem> getDASS(){
        return Arrays.asList(
                new QuestionItem("I found it difficult to relax.",0,3,false),
                new QuestionItem("I felt stressed over small things.",0,3,false),
                new QuestionItem("I felt tense for no reason.",0,3,false),
                new QuestionItem("I found it hard to wind down.",0,3,false),
                new QuestionItem("I felt I was close to panic.",0,3,false),
                new QuestionItem("I had difficulty concentrating.",0,3,false),
                new QuestionItem("I felt I was not coping.",0,3,false)
        );
    }

    //==============================================================
    // LABEL
    //==============================================================
    private String getReadableModuleTitle(String m){
        switch(m){
            case "focused_attention": return "Focused Attention";
            case "working_memory": return "Working Memory";
            case "emotional_regulation": return "Emotional Regulation";
            case "cognitive_flexibility": return "Cognitive Flexibility";
            case "present_moment":
            case "present_moment_awareness": return "Present-Moment Awareness";
        }
        return "Assessment";
    }

    //==============================================================
    // INSTRUCTIONS
    //==============================================================
    private String getInstructionText(String type){
        switch(type){
            case "focused_attention": return "Rate: 1 = Almost Always → 6 = Almost Never";
            case "working_memory": return "Rate: 1 = Never → 5 = Almost Always";
            case "emotional_regulation": return "Rate: 1 = Very Slightly → 5 = Extremely";
            case "present_moment_awareness": return "Rate: 1 = Never → 5 = Very Often";
            case "cognitive_flexibility": return "Rate: 0 = Never → 3 = Always";
        }
        return "Select the option that fits best.";
    }
}
