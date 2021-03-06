package com.angcyo.oaschool;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.angcyo.oaschool.components.RConstant;
import com.angcyo.oaschool.util.Util;
import com.angcyo.oaschool.view.BaseFragment;
import com.angcyo.oaschool.view.fragment.SaveFileFragment;
import com.orhanobut.logger.Logger;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by angcyo on 15-09-13-013.
 */
public class ContentActivity extends BaseActivity {
    public static String KEY_ID = "key_id";
    WebView webView;
    @Bind(R.id.webview)
    WebView webview;
    @Bind(R.id.container)
    FrameLayout container;
    private int id = 1;//文章的id

    @Override
    protected void initView() {
        id = Integer.valueOf(getIntent().getStringExtra(KEY_ID));
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        webView = (WebView) findViewById(R.id.webview);
        initWebView(webView);
        initWindow(R.color.action_bar_bg);

        SlidrConfig config = new SlidrConfig.Builder().edge(true).build();
        Slidr.attach(this, config);
    }

    @Override
    protected void initBefore() {
        super.initBefore();
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
    }

    @Override
    protected void initAfter() {
        webView.loadUrl(getContentUrl());
        webView.zoomOut();
    }

    private WebView initWebView(WebView webView) {
//        WebView webView = new WebView(this);
        webView.getSettings().setDefaultTextEncodingName("gbk");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);

        webView.getSettings().setBuiltInZoomControls(true);//支持缩放手势
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);//不显示缩放控件
//        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        //缩放网页,以便显示整个网页
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
//        webView.setInitialScale(1);

        webView.getSettings().setUserAgentString("Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_4; zh-tw) AppleWebKit/533.16 (KHTML, like Gecko) Version/5.0 Safari/533.16");

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                setTitle("Loading...");
                setProgress(progress * 100); //Make the bar disappear after URL is loaded
                if (progress >= 100)
                    setTitle("用户:" + OaApplication.getUserInfo().tname);
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.getSettings().setDefaultTextEncodingName("utf8");
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setTitle("用户:" + OaApplication.getUserInfo().tname);
            }

        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Logger.e(url + "  " + mimetype + "\n" + contentDisposition);
                String fileName = "未知文件名";
                if (!Util.isEmpty(contentDisposition) && contentDisposition.contains("filename")) {
                    fileName = contentDisposition.substring(contentDisposition.indexOf("=") + 1);
                }
                BaseFragment fragment = new SaveFileFragment();
                Bundle args = new Bundle();
                args.putString(SaveFileFragment.KEY_URL, url);
                args.putString(SaveFileFragment.KEY_FILE_NAME, fileName);
                args.putString(SaveFileFragment.KEY_FILE_MIME_TYPE, mimetype);
                args.putString(SaveFileFragment.KEY_FILE_SIZE, Formatter.formatFileSize(ContentActivity.this, contentLength));
                fragment.setArguments(args);
                ContentActivity.this.add(fragment);
            }
        });
        return webView;
    }

    private String getContentUrl() {
        String url = String.format("http://%s/APP/TongZhiview.asp?APPID=%s&id=%d", RConstant.SER_IP, OaApplication.getUserInfo().appid, id);
        return url;
    }

    public void replace(BaseFragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
    }

    public void add(BaseFragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, fragment);
        fragmentTransaction.addToBackStack(fragment.getClass().getSimpleName()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.quit) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
