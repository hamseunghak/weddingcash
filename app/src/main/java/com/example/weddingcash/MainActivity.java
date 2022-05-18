package com.example.weddingcash;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // private SwipeRefreshLayout swipeRefreshLayout;
    private WebView mWebView;
    public Context mContext;
    private String qrurl;

    ProgressBar pBar;
    private long backBtnTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        qrurl = intent.getStringExtra("qrurl");

        // swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        mWebView = findViewById(R.id.activity_main_webview);
        pBar =  findViewById(R.id.pBar);    // 로딩바
        pBar.setVisibility(View.GONE);      // 로딩바 가리기 (로딩때만 보여야 함)

// wide viewport를 사용하도록 설정
        mWebView.getSettings().setUseWideViewPort(true);

// 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
        mWebView.getSettings().setLoadWithOverviewMode(true);

        //zoom 허용
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDisplayZoomControls(false); //줌 컨트롤러를 안보이게 셋팅.

        mContext = this.getApplicationContext();

//        swipeRefreshLayout.setOnRefreshListener(() -> { /* Webview를 새로고침한다. */
//            mWebView.reload(); /* 업데이트가 끝났음을 알림 */
//            swipeRefreshLayout.setRefreshing(false);
//        });

        initWebView();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
    }

    // 웹뷰 초기화 함수
    public void initWebView(){
        // 1. 웹뷰클라이언트 연결 (로딩 시작/끝 받아오기)
        mWebView.setWebViewClient(new WebViewClient(){
            @Override                                   // 1) 로딩 시작
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                pBar.setVisibility(View.VISIBLE);       // 로딩이 시작되면 로딩바 보이기
            }
            @Override                                   // 2) 로딩 끝
            public void onPageFinished(WebView view, String url) {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    CookieSyncManager.getInstance().sync();
                } else {
                    CookieManager.getInstance().flush();
                }
                super.onPageFinished(view, url);
                pBar.setVisibility(View.GONE);          // 로딩이 끝나면 로딩바 없애기
            }
            @Override                                   // 3) 외부 브라우저가 아닌 웹뷰 자체에서 url 호출
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("app://")) {
                    Intent intent = new Intent(mContext.getApplicationContext(), QrScan.class);
                    startActivity(intent);
                    return true;
                }
                else {
                    view.loadUrl(url);
                    return true;
                }
            }
        });
        // 2. WebSettings: 웹뷰의 각종 설정을 정할 수 있다.
        WebSettings ws = mWebView.getSettings();
        ws.setJavaScriptEnabled(true); // 자바스크립트 사용 허가
        // 3. 웹페이지 호출
        if(qrurl != null){
            mWebView.loadUrl(qrurl);
        }else{
            mWebView.loadUrl("http://211.43.13.14:7148/");
        }



        // WebView alert() 사용
        mWebView.setWebChromeClient(new WebChromeClient() {
            // alert창 url 제거
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { result.confirm(); }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            };
        });
    }



    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        long gapTime = curTime - backBtnTime;
        if (mWebView.getUrl().equalsIgnoreCase("http://211.43.13.14:7148/index.html")
        ) {
            if(0 <= gapTime && 2000 >= gapTime) {
                super.onBackPressed();
            }
            else {
                backBtnTime = curTime;
                Toast.makeText(this, "한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
            }
            //super.onBackPressed();
        }else if(mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            super.onBackPressed();
        }
    }
}