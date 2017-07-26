package com.example.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.reactivex.functions.Consumer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private UserManager userManager;
    private TextView usersTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userManager = new UserManager();

        usersTextView = (TextView) findViewById(R.id.display_users_textview);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        Button rxButton = (Button) findViewById(R.id.get_user_rx_button);
        rxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersTextView.setText(""); // clear
                progressBar.setVisibility(View.VISIBLE);
                userManager.getUser()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<User>() {
                            @Override
                            public void call(User user) {
                                progressBar.setVisibility(View.INVISIBLE);
                                usersTextView.setText(
                                        String.format("rxjava: %s %s", user.getFirstName(), user.getLastName()));
                            }
                        });
            }
        });

        Button rx2Button = (Button) findViewById(R.id.get_user_rx2_button);
        rx2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersTextView.setText(""); // clear
                progressBar.setVisibility(View.VISIBLE);
                userManager.getUsersRx2()
                        .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                        .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<User>() {
                            @Override
                            public void accept(User user) throws Exception {
                                progressBar.setVisibility(View.INVISIBLE);
                                usersTextView.setText(
                                        String.format("rxjava2: %s %s", user.getFirstName(), user.getLastName()));
                            }
                        });
            }
        });
    }
}
