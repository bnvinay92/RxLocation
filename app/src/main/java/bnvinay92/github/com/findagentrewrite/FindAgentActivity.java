package bnvinay92.github.com.findagentrewrite;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class FindAgentActivity extends AppCompatActivity {

    FindAgentPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_agent);
    }
}
