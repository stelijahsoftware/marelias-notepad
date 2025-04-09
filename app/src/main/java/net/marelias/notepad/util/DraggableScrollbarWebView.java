package net.marelias.notepad.util;

//import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

@SuppressLint("ClickableViewAccessibility")
public class DraggableScrollbarWebView extends WebView {

    public DraggableScrollbarWebView(Context context) {
        super(context);
    }

    public DraggableScrollbarWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggableScrollbarWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
