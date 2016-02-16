package com.example.administrator.autosms_verificationcode;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {



    //匹配验证码
    private String patternCode(String patternContent){
        if(TextUtils.isEmpty(patternContent)){
            return null;
        }
        //将正则表达式赋予生成的Pattern类
        Pattern pattern = Pattern.compile("(?<!\\\\d)\\\\d{6}(?!\\\\d)");
        //将要匹配的内容赋予生成的Matcher类
        Matcher matcher = pattern.matcher(patternContent);
        //尝试在目标字符串里查找下一个匹配字符串
        if(matcher.find()){
            //返回当前查找而获得的与组匹配的所有字符串内容
            return matcher.group();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
