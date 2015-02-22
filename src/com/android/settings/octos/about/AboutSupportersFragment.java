package com.android.settings.octos.about;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.android.settings.R;

public class AboutSupportersFragment extends Fragment {

        public AboutSupportersFragment() {
        // empty fragment constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_about_octos_supporters, container, false);

        WebView browser = (WebView) root.findViewById(R.id.octos_supporters_display);
        browser.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.3.5; en-us; HTC Vision Build/GRI40) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        browser.loadUrl(getString(R.string.octos_supporters_url));

        return root;
    }
}
