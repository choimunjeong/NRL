package Page2_X;

import DB.DbOpenHelper;
import Page1.Page1_1_1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nrr.hansol.spot_200510_hs.R;
import com.google.android.material.appbar.AppBarLayout;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class Page2_X_Main extends AppCompatActivity implements Page2_X_Interface {

    int station_code = 9999;
    String[] arr_line = null;
    String[] _name = new String[station_code];           //txt에서 받은 역이름
    String[] _areaCode = new String[station_code];       //txt에서 받은 지역코드
    String[] _sigunguCode = new String[station_code];    //txt에서 받은 시군구코드
    String[] _x = new String[station_code];              //txt에서 받은 x좌표
    String[] _y = new String[station_code];              //txt에서 받은 y좌표
    String[] _benefitURL = new String[station_code];     //txt에서 받은 혜택url
    String st_name, areaCode, sigunguCode, benefitURL,station;            //전달받은 역의 지역코드, 시군구코드, 혜택URL
    Double x, y;                                         //전달받은 역의 x,y 좌표

    ArrayList<String > tourList = new ArrayList<>();

    //returnResult를 줄바꿈 단위로 쪼개서 넣은 배열
    String name_1[];

    //name_1를 "  " 단위로 쪼개서 넣은 배열
    String name_2[] = new String[5];

    String id;

    //api 관련
    int page = 1;     //api 페이지 수
    boolean isLoadData = true;
    String returnResult, url;
    String Url_front = "https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory&fname=";
    String contentTypeId = "12", cat1 = "", cat2 = "";
    ProgressDialog asyncDialog;
    ProgressBar progressBar;
    Button  gift;
    HorizontalScrollView scrollView;

    //상단
    TextView cityName;
    TextView searchCity;
    TextView backButton;

    //레이아웃 관련
    AppBarLayout appBarLayout;
    MapView mapView;
    MapPOIItem marker;
    ViewGroup mapViewContainer;
    RelativeLayout map_layout;
    Button reset_btn;
    Button category_btn;
    Button mapExpand_btn;
    Button arrow_btn;
    LinearLayout category_add;
    TextView map_error_txt;
    TextView spot_error_txt;
    private RelativeLayout info_message;
    private Button info_dismiss_btn;

    boolean isExpand = false;
    int btn_id[] = new int[]{R.id.category_add_btn1, R.id.category_add_btn2, R.id.category_add_btn3 ,
            R.id.category_add_btn4, R.id.category_add_btn5, R.id.category_add_btn6, R.id.category_add_btn7, R.id.category_add_btn8};

    //리사이클러뷰 관련
    RecyclerView recyclerView;
    Page2_X_Adapter adapter;
    private DbOpenHelper mDbOpenHelper;
    List<Recycler_item> items = new ArrayList<>();
    private  Button[] btn = new Button[8];

    //기기의 높이를 구한다.
    float d;
    int height;

    //@SuppressLint("WrongViewCast")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2_x_main);


        //기기의 높이를 구한다.
        d = Page2_X_Main.this.getResources().getDisplayMetrics().density;
        Display display = getWindowManager().getDefaultDisplay();  // in Activity
        Point size = new Point();
        display.getRealSize(size);
        height = size.y - (int)(100 * d);

        //데이터베이스 관련
        mDbOpenHelper = new DbOpenHelper(Page2_X_Main.this);
        mDbOpenHelper.open();
        mDbOpenHelper.create();

        //맵 관련
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        map_layout = (RelativeLayout) findViewById(R.id.map_view);

        //그 외 객체연결
        gift = (Button) findViewById(R.id.gift);
        cityName = (TextView)findViewById(R.id.page2_x_cityName);
        backButton = (TextView) findViewById(R.id.page2_x_back_btn);
        searchCity = (TextView)findViewById(R.id.page2_x_searchCity);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        category_btn = (Button)findViewById(R.id.category_btn);
        mapExpand_btn = (Button)findViewById(R.id.expand_btn);
        arrow_btn = (Button)findViewById(R.id.arrow_btn);
        asyncDialog= new ProgressDialog( this);
        appBarLayout = (AppBarLayout)findViewById(R.id.app_bar);
        map_error_txt = (TextView) findViewById(R.id.map_error_txt);
        spot_error_txt = (TextView) findViewById(R.id.spot_error_txt);

        info_message = findViewById(R.id.info_message2);
        info_dismiss_btn = findViewById(R.id.info_dismiss_btn2);


        //최소 실행 때 보이는 안내창-
        SharedPreferences a = getSharedPreferences("info1", MODE_PRIVATE);
        final SharedPreferences.Editor editor = a.edit();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                editor.putInt("info2", 1);
                editor.commit();
            }
        }, 300);

        //첫 실행시 나오는 안내 말풍선
        SharedPreferences preferences =getSharedPreferences("info1", MODE_PRIVATE);
        int firstviewShow = preferences.getInt("info2", 0);

        // 1이 아니라면 취향파악페이지 보여주기 = 처음 실행이라면
        if (firstviewShow != 1) {
            info_message.setVisibility(View.VISIBLE);
        }

        info_dismiss_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info_message.setVisibility(View.INVISIBLE);
            }
        });

        //위아래로 드래그 했을 때 변화를 감지하는 부분
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {

                    //Log.d("접혔어!!!!", "접혔어!!");
                    if(isExpand){
                        changeVisibility(false, height);
                        isExpand = false;

                        //카테고리랑 혜택을 보이게
                        category_btn.setVisibility(View.VISIBLE);
                        gift.setVisibility(View.VISIBLE);

                        //arrow버튼 보이게
                        arrow_btn.getLayoutParams().height = 0;
                        arrow_btn.requestLayout();
                    }

                }
            }
        });

        //텍스트뷰 밑줄
        searchCity.setPaintFlags(searchCity.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        //다른 도시 검색하기 텍스트
        searchCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Page2_X_Main.this, Page2_X.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        //뒤로가기 버튼
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });



        //세로 드래그 문제를 해결하기 위한 부분
        if (appBarLayout.getLayoutParams() != null) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)appBarLayout.getLayoutParams();
            AppBarLayout.Behavior appBarLayoutBehaviour = new AppBarLayout.Behavior();
            appBarLayoutBehaviour.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
                @Override
                public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                    return false;
                }
            });
            layoutParams.setBehavior(appBarLayoutBehaviour);
        }

        //앞 액티비티에서 값 전달받아서 서치 텍스트에 넣기
        Intent intent = getIntent();
        if (intent.hasExtra("Page1_stName")) {
            st_name = intent.getStringExtra("Page1_stName");
            //인터넷 유무 체크
            int isNetworkConnect = NetworkStatus.getConnectivityStatus(getApplicationContext());
            if(isNetworkConnect == 3) {
                map_error_txt.setVisibility(View.VISIBLE);
                spot_error_txt.setVisibility(View.VISIBLE);
            }

        } else if(intent.hasExtra("st_name")){
            // 메인에서 검색 눌렀을 때
            st_name = intent.getStringExtra("st_name");
            int isNetworkConnect = NetworkStatus.getConnectivityStatus(getApplicationContext());
            if(isNetworkConnect == 3) {
                map_error_txt.setVisibility(View.VISIBLE);
                spot_error_txt.setVisibility(View.VISIBLE);
            }

        } else {
            // 여행 코스 추천에서 '다른 관광지 더 보기' 눌렀을 때
            st_name = intent.getStringExtra("station");
            int isNetworkConnect = NetworkStatus.getConnectivityStatus(getApplicationContext());
            if (isNetworkConnect == 3) {
                map_error_txt.setVisibility(View.VISIBLE);
                spot_error_txt.setVisibility(View.VISIBLE);
            }
        }


        adapter = new Page2_X_Adapter(getApplicationContext(), items, st_name, this);

        cityName.setText("#"+st_name);

        //txt 값 읽기
        settingList();

        //전달된 역의 지역코드, 시군구코드 찾기
        compareStation();

        //맵뷰
        mapView = new MapView(this);
        mapView.setClickable(false);
        mapViewContainer.addView(mapView,0);
        //java.lang.NullPointerException:
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(y, x), 8, true);
        marker = new MapPOIItem();

        //리사이클러뷰 구현 부분
        recyclerView = (RecyclerView) findViewById(R.id.page2_X_recyclerview);
        recyclerView.setHasFixedSize(true);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        //지도가 확대되지 않도록 함
        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(!isExpand)
                    return true;
                else
                    return false;
            }
        });

        //맵 리셋버튼
        reset_btn = (Button)findViewById(R.id.reset_btn);
        reset_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(y, x), 8, true);
            }
        });


        //관광 api 연결 부분
        settingAPI_Data();
        asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        asyncDialog.setMessage("데이터 로딩중입니다.");

        //more loading
        final NestedScrollView nestedScrollView = (NestedScrollView)findViewById(R.id.nestScrollView);
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            int  visibleItemCount,  totalItemCount, pastVisiblesItems;

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if(v.getChildAt(v.getChildCount() -1) != null) {
                    if( (scrollY >= (v.getChildAt(v.getChildCount() -1).getMeasuredHeight() -  v.getMeasuredHeight() )) && scrollY > oldScrollY) {

                        visibleItemCount = gridLayoutManager.getChildCount();
                        totalItemCount = gridLayoutManager.getItemCount() ;
                        pastVisiblesItems = gridLayoutManager.findFirstVisibleItemPosition();

                        //받아온 api 개수가 20개가 안되면 다음 페이지가 없다고 판단. false로 바꿔줌
                        if(tourList.size() < 10){
                            isLoadData = false;
                        }

                        //isLoadData가 true이면
                        if(isLoadData) {
                            if( (visibleItemCount + pastVisiblesItems) >= totalItemCount ){

                                page++;

                                //관광 api 연결 부분
                                settingAPI_Data();

                                //메시지 갱신 위치
                                adapter.notifyDataSetChanged();
                            }
                        }

                        //데이터가 더 없을 때
                        else {
                            noData_Dialog();
                        }
                    }
                }
            }
        });



        //혜택 버튼을 누르면
        gift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //혜택이 없을 경우
                if(benefitURL.equals("혜택없음")){
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Page2_X_Main.this);
                    alertDialogBuilder .setMessage(st_name + "역은 혜택이 없습니다.")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                public void onClick( DialogInterface dialog, int id) {
                                    // 프로그램을 종료한다
                                    dialog.cancel(); } });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }

                //있을 경우, url로 연결해준다.
                else {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(benefitURL));
                    startActivity(intent);
                }
            }
        });

        //카테고리 버튼 누르면
        category_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Page2_X_CategoryBottom category_bottomsheet = new Page2_X_CategoryBottom();
                category_bottomsheet.show(getSupportFragmentManager(), "category");
            }
        });




        //지도 확대 버튼 누르면
        mapExpand_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //안내창 사라짐
                info_message.setVisibility(View.INVISIBLE);

                RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

                //확대되지 않았으면(false)
                if(!isExpand){
                    appBarLayout.getLayoutParams().height = height;
                    appBarLayout.requestLayout();
                    map_layout.setLayoutParams(params2);
                    isExpand = true;

                    //카테고리랑 혜택을 안보이게
                    category_btn.setVisibility(View.INVISIBLE);
                    gift.setVisibility(View.INVISIBLE);

                    //arrow버튼 보이게
                    arrow_btn.getLayoutParams().height = (int)(50*d);
                    arrow_btn.requestLayout();

                }

                //확대 됐으면(true)
                else {
                    changeVisibility(false, height);
                    isExpand = false;

                    //카테고리랑 혜택을 보이게
                    category_btn.setVisibility(View.VISIBLE);
                    gift.setVisibility(View.VISIBLE);

                    //arrow버튼 보이게
                    arrow_btn.getLayoutParams().height = 0;
                    arrow_btn.requestLayout();
                }
            }
        });
    }


    //화면을 생성할때 부드럽게 주기위한 애니메이션 함수
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void changeVisibility(final boolean isExpanded, int height) {

        // height 값을 dp로 지정해서 넣고싶으면 아래 소스를 이용
        float d = Page2_X_Main.this.getResources().getDisplayMetrics().density;
        final int applayout_height = (int) (445 * d);
        final int map_height = (int) (277 * d);

        final ValueAnimator va = isExpanded ? ValueAnimator.ofInt(applayout_height, height) : ValueAnimator.ofInt(height, applayout_height);
        va.setDuration(600);   // Animation이 실행되는 시간, n/1000초
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (int) animation.getAnimatedValue();                // value는 height 값
                appBarLayout.getLayoutParams().height = value;
                appBarLayout.requestLayout();

            }
        });

        final ValueAnimator va2 = isExpanded ? ValueAnimator.ofInt(applayout_height, height) : ValueAnimator.ofInt(height, map_height);
        va2.setDuration(600);   // Animation이 실행되는 시간, n/1000초
        va2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (int) animation.getAnimatedValue();                // value는 height 값
                map_layout.getLayoutParams().height = value;
                map_layout.requestLayout();

            }
        });

        va2.start();
        va.start();
    }

    private void settingAPI_Data(){
        SearchTask task = new SearchTask();

        String contentid= null;
        String img_Url = null;
        String name = null;
        String mapx = null;
        String mapy = null;
        String cat1 = null;
        String cat2 = null;
        String contenttypeid = null;

        try {
            tourList.clear();
            String RESULT = task.execute().get();
            Log.i("전달 받은 값", RESULT);

            JSONObject jsonObject = new JSONObject(RESULT);
            JSONObject respose = jsonObject.getJSONObject("response");
            JSONObject body = respose.getJSONObject("body");
            JSONObject allitem = body.getJSONObject("items");
            JSONArray item = allitem.getJSONArray("item");

            for(int i = 0; i < item.length(); i++) {
                JSONObject jObject = item.getJSONObject(i);
                contentid = jObject.getString("contentid");
                if (!jObject.isNull("firstimage")) {
                    img_Url = jObject.getString("firstimage");
                } else {
                    img_Url = null;
                }
                if (!jObject.isNull("mapx")) {
                    mapx = jObject.getString("mapx");
                } else {
                    mapx = null;
                }
                if (!jObject.isNull("mapy")) {
                    mapy = jObject.getString("mapy");
                } else {
                    mapy = null;
                }
                name = jObject.getString("title");
                cat1 = jObject.getString("cat1");
                if (!jObject.isNull("cat2")) {
                    cat2 = jObject.getString("cat2");
                } else {
                    cat2 = null;
                }
                contenttypeid = jObject.getString("contenttypeid");

                Log.d("contentid","contentid: "+contentid);
                Log.d("imgURL","imgURL: "+img_Url);
                Log.d("title","title: "+name);
                Log.d("mapx","mapx: "+mapx);
                Log.d("mapy","mapy: "+mapy);
                Log.d("cat1","cat1: "+cat1);
                Log.d("cat2","cat2: "+cat2);
                Log.d("contenttypeid","contenttypeid: "+contenttypeid);

//                name_1[i] = areacode + contentid + img_Url + sigungucode_arr + name;
                tourList.add(name);
                Log.d("갯수", "갯수:" + tourList.size());
                items.add(new Recycler_item(Url_front + img_Url, name, contentid, mapx, mapy, cat1, cat2, contenttypeid));
                if (mapx != null) {
                    //마커 많이 만들기
                    double X = Double.parseDouble(mapx);
                    double Y = Double.parseDouble(mapy);
                    marker.setTag(1);
                    marker.setItemName(name);
                    marker.setMapPoint(MapPoint.mapPointWithGeoCoord(Y, X));
                    marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                    marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
                    mapView.addPOIItem(marker);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    //txt 돌려 역 비교할 배열 만들기(이름 지역코드 동네코드)<-로 구성
    private void settingList(){

        String readStr = "";
        AssetManager assetManager = getResources().getAssets();
        InputStream inputStream = null;

        try{
            inputStream = assetManager.open("station_code.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String str = null;
            while (((str = reader.readLine()) != null)){ readStr += str + "\n";}
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] arr_all = readStr.split("\n"); //txt 내용을 줄바꿈 기준으로 나눈다.

        //한 줄의 값을 띄어쓰기 기준으로 나눠서, 역명/지역코드/시군구코드 배열에 넣는다.
        for(int i=0; i <arr_all.length; i++) {
            arr_line = arr_all[i].split(" ");

            _name[i] = arr_line[0];         //서울
            _areaCode[i] = arr_line[1];     //1
            _sigunguCode[i] = arr_line[2];  //0
            _y[i] = arr_line[3];            //y좌표
            _x[i] = arr_line[4];            //x좌표
            _benefitURL[i] = arr_line[5];
        }
    }


    //앞 액티비티에서 선택된 역과 같은 역을 찾는다.
    private void compareStation(){
        for(int i=0; i<_name.length; i++){
            if(st_name.equals(_name[i])){
                areaCode = _areaCode[i];
                sigunguCode = _sigunguCode[i];
                y = Double.parseDouble(_y[i]);
                x = Double.parseDouble(_x[i]);
                benefitURL = _benefitURL[i];
            }
        }
    }

    //카테고리바텀시트에서 선택한 타입 api를 돌림
    @Override
    public void onData(ArrayList<Page2_X_CategoryBottom.Category_item> list) {

        //카테고리 선택되면 기존 것들 싹 삭제해줌

        appBarLayout.getLayoutParams().height = (int)(483*d);
        appBarLayout.requestLayout();

        //카테고리에서 타입 선택시 생기는 버튼을 동적 생성
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int)(38*d) );
        params.rightMargin = (int)(12*d);

        String name= "";
        for(int i =0; i < list.size(); i++){
            name = name + list.get(i).name + "  ";
        }

        Button button = new Button(this);
        button.setText(name);
        button.setLayoutParams(params);
        button.setId(R.id.category_add_btn1);
        button.setBackgroundResource(R.drawable.box_round_category_add);

        //기존의 api 값을 지운다.
        items.clear();
        page = 1;


        for(int p=0; p < list.size(); p++){
            contentTypeId = list.get(p).getContentId();
            cat1 = list.get(p).getCat1();
            cat2 = list.get(p).getCat2();

            //관광 api 연결 부분
            settingAPI_Data();
        }
        adapter.notifyDataSetChanged();
    }



    //이 클래스는 어댑터와 서로 주고받으며 쓰는 클래스임
    public class Recycler_item {
        String image;
        String title;
        String contentviewID;
        String mapx;
        String mapy;
        String cat1;
        String cat2;
        String contenttypeid;

        String getImage() {
            return this.image;
        }

        String getTitle() {
            return this.title;
        }

        String getContentviewID() {
            return this.contentviewID;
        }

        String getMapx() {
            return this.mapx;
        }

        String getMapy() {
            return this.mapy;
        }

        public String getCat1() {
            return cat1;
        }

        public String getCat2() {
            return cat2;
        }

        public String getContenttypeid() {
            return contenttypeid;
        }

        public Recycler_item(String image, String title, String contentviewID, String mapx, String mapy, String cat1, String cat2, String contenttypeid) {
            this.image = image;
            this.title = title;
            this.contentviewID = contentviewID;
            this.mapx = mapx;
            this.mapy = mapy;
            this.cat1 = cat1;
            this.cat2 = cat2;
            this.contenttypeid = contenttypeid;
        }
    }

    //관광api 연결
    class SearchTask extends AsyncTask<String, Void, String> {
        private String str;
        @Override
        protected void onPreExecute() {
            //초기화 단계에서 사용
        }

        @Override
        protected String doInBackground(String... strings) {
            //시군구코드가 0 일 때와 0이 아닐때를 구분해서 url을 넣어준다.
            if(sigunguCode.equals("0")){
                url = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?serviceKey=" +
                        "tQVUU9RPcLsBmX4nqBFMUDqgvO3nBdfcZI%2FS8GQndON35%2BjzjShtdnH94CNN6d%2Fhb61uX1mOz7lWWD5rA6LNFg%3D%3D" +
                        "&pageNo=" + page +
                        "&numOfRows=10&MobileApp=AppTest&MobileOS=ETC&arrange=B" +
                        "&contentTypeId=" + contentTypeId +
                        "&sigunguCode=" +
                        "&areaCode=" + areaCode +
                        "&cat1=" + cat1 +
                        "&cat2=" + cat2 +
                        "&cat3=" +
                        "&listYN=Y" +
                        "&_type=json";
            } else {
                url = "http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList?serviceKey=" +
                        "tQVUU9RPcLsBmX4nqBFMUDqgvO3nBdfcZI%2FS8GQndON35%2BjzjShtdnH94CNN6d%2Fhb61uX1mOz7lWWD5rA6LNFg%3D%3D" +
                        "&pageNo=" + page +
                        "&numOfRows=10&MobileApp=AppTest&MobileOS=ETC&arrange=B" +
                        "&contentTypeId=" + contentTypeId +
                        "&sigunguCode=" + sigunguCode +
                        "&areaCode=" + areaCode +
                        "&cat1=" + cat1 +
                        "&cat2=" + cat2 +
                        "&cat3=" +
                        "&listYN=Y" +
                        "&_type=json";
            }
            returnResult = "";
            URL xmlUrl;
            Log.d("뭐냐! 뭐가 뭔제요", url);

            try {
                xmlUrl = new URL(url);
                HttpURLConnection conn = (HttpURLConnection) xmlUrl.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    returnResult = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return returnResult;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //맵 뷰 한개만 띄워주기
    @Override
    protected void onPause() {
        super.onPause();
        ((ViewGroup)mapView.getParent()).removeView(mapView);
    }

    // 현재 액티비티 새로고침
    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }



    //지도 아이콘을 누르면
    @Override
    public void onClick(double x, double y, String name) {
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(y, x), 3, true);
        marker.setTag(1);
        marker.setItemName(name);
        marker.setMapPoint(MapPoint.mapPointWithGeoCoord(y, x));
        marker.setMarkerType(MapPOIItem.MarkerType.RedPin);
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        mapView.addPOIItem(marker);
    }

    @Override
    public void make_db(String countId, String name, String cityname, String type, String image, String click) {
        mDbOpenHelper.open();
        mDbOpenHelper.insertColumn(countId, name, cityname, type, image, click);
        mDbOpenHelper.close();
    }

    @Override
    public void delete_db(String contentId) {
        mDbOpenHelper.open();
        mDbOpenHelper.deleteColumnByContentID(contentId);
        mDbOpenHelper.close();
        delete_dialog();
    }

    public void delete_dialog() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Page2_X_Main.this);
        builder.setTitle("관심관광지 삭제 성공");
        builder.setMessage("관심관광지 목록을 확인하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Page2_X_Main.this, Page1_1_1.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    @Override
    public String isClick(String countid) {
        mDbOpenHelper.open();
        Cursor iCursor = mDbOpenHelper.selectIdCulumns(countid);
        Log.d("showDatabase", "DB Size: " + iCursor.getCount());

        while (iCursor.moveToNext()) {
            String userId = iCursor.getString(iCursor.getColumnIndex("userid"));
            id = userId;
        }
        mDbOpenHelper.close();

        return id;
    }

    @Override
    public void make_dialog() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Page2_X_Main.this);
        builder.setTitle("관심관광지 추가 성공");
        builder.setMessage("관심관광지 목록을 확인하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Page2_X_Main.this, Page1_1_1.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }


    //로딩할 데이터가 더이상 없을 때
    public void noData_Dialog() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setMessage("마지막 데이터 입니다.");
        builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0,0);
    }
}
