package com.nrr.hansol.spot_200510_hs;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import DB.Like_DbOpenHelper;
import Page1.Page1;

public class Page0_2 extends AppCompatActivity implements View.OnClickListener {

    TextView a1_alone;
    TextView a2_family;
    TextView a3_friend;
    TextView page2_later_btn;

    int[] score = new int[8];

    private Like_DbOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page0_1);

        mDbOpenHelper = new Like_DbOpenHelper(Page0_2.this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();


        a1_alone = (TextView)findViewById(R.id.page0_2_a1);
        a2_family = (TextView)findViewById(R.id.page0_2_a2);
        a3_friend = (TextView)findViewById(R.id.page0_2_a3);
        page2_later_btn = (TextView)findViewById(R.id.page0_2_later);

        // 텍스트뷰 밑줄
        page2_later_btn.setPaintFlags(page2_later_btn.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);

        // 버튼 선택 되었을 때
        a1_alone.setOnClickListener(this);
        a2_family.setOnClickListener(this);
        a3_friend.setOnClickListener(this);

        // 나중에 다시하기 버튼 눌렀을 때
        page2_later_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDbOpenHelper.open();
                mDbOpenHelper.deleteAllColumns();
                mDbOpenHelper.insertLikeColumn("0 3 0 0 1 0 0 0", "열정 개미", "자연속에서 힐링하기 좋아하는");
                mDbOpenHelper.close();

                score[1] = 3; score[4] = 1; score[5] = 0;
                Intent intent = new Intent(Page0_2.this, Page1.class);
                intent.putExtra("Main", score);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        });
    }

    // 버튼 선택 시 수행하는 함수
    public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), Page0_3.class);
        if (view.getId() == R.id.page0_2_a1){
            score[0] = 0;   // 값 저장
            intent.putExtra("Page2",score);
        } else if (view.getId() == R.id.page0_2_a2) {
            score[0] = 1;
            intent.putExtra("Page2",score);
        } else if (view.getId() == R.id.page0_2_a3) {
            score[0] = 2;
            intent.putExtra("Page2",score);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_out, R.anim.hold);
    }

    // 뒤로가기 버튼 막기
    @Override
    public void onBackPressed() {
    }
}
