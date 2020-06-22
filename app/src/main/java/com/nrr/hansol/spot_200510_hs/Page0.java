package com.nrr.hansol.spot_200510_hs;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import DB.Like_DbOpenHelper;
import Page1.Page1;

public class Page0 extends AppCompatActivity {

    TextView btn_later;
    TextView btn_start;
    ImageView menu_img;

    int[] score = new int[8];
    private Like_DbOpenHelper mDbOpenHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page0);

        int firstFin = 1;
        SharedPreferences a = getSharedPreferences("a", MODE_PRIVATE);
        SharedPreferences.Editor editor = a.edit();
        editor.putInt("First", firstFin);
        editor.commit();

        mDbOpenHelper = new Like_DbOpenHelper(Page0.this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();

        btn_later = (TextView) findViewById(R.id.page0_later_btn);
        btn_start = (TextView) findViewById(R.id.page0_start_btn);
        menu_img = (ImageView)findViewById(R.id.menu_userImage);


        // 시작하기 버튼 눌렀을 때
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (Page0.this, Page0_2.class);
                startActivity(intent);
                // 액티비티 전환 효과
                overridePendingTransition(R.anim.anim_slide_out, R.anim.hold);
            }
        });


        // 나중에 하기 버튼 눌렀을 때
        btn_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDbOpenHelper.open();
                mDbOpenHelper.deleteAllColumns();
                mDbOpenHelper.insertLikeColumn("0 3 0 0 1 0 0 0", "열정 개미", "자연속에서 힐링하기 좋아하는");
                mDbOpenHelper.close();

                // 임의의 값 넘겨서 기본 카테고리 보여주기
                score[1] = 3; score[4] = 1; score[5] = 0;
                Intent intent = new Intent(Page0.this, Page1.class);
                intent.putExtra("Main", score);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide, R.anim.hold);
    }



}
