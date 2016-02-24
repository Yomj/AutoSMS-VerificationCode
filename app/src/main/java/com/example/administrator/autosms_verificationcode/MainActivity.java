package com.example.administrator.autosms_verificationcode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText phoneNumEditText,verificationCodeEditText;
    //两个按键用于网络交互
    private Button sendButton,confirmButton;
    private Handler handler;
    private String verCode;
    private countDownTimer cDTimer;
    private BroadcastReceiver broadcastReceiver;

    //匹配验证码
    private String patternCode(String patternContent){
        if(TextUtils.isEmpty(patternContent)){
            return null;
        }
        //将正则表达式赋予生成的Pattern类
        Pattern pattern = Pattern.compile("(?<!\\d)\\d{6}(?!\\d)");
        //将要匹配的内容赋予生成的Matcher类
        Matcher matcher = pattern.matcher(patternContent);
        //尝试在目标字符串里查找下一个匹配字符串
        if(matcher.find()){
            //返回当前查找而获得的与组匹配的所有字符串内容
            return matcher.group();
        }
        return null;
    }

    private class countDownTimer extends CountDownTimer{
        public countDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            sendButton.setText("重新发送");
            sendButton.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            sendButton.setClickable(false);
            sendButton.setText(millisUntilFinished/1000 + "秒后可重新发送");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phoneNumEditText = (EditText) findViewById(R.id.phone_num_edit_text);
        verificationCodeEditText = (EditText) findViewById(R.id.ver_code_edit_text);
        sendButton = (Button) findViewById(R.id.send_ver_code_button);
        confirmButton =(Button) findViewById(R.id.confirm_button);
        phoneNumEditText.setText("");
        verificationCodeEditText.setText("");
        cDTimer = new countDownTimer(60000,1000);
        //获取本机号码目前没有比较方便的方法，较简单的是读取sim卡，较成功的是发短信给服务商来读取发送号码
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if(!TextUtils.isEmpty(telephonyManager.getLine1Number())){
            phoneNumEditText.setText(telephonyManager.getLine1Number());
        }
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.send_ver_code_button:
                        /*发送手机号码到服务器，服务器发送验证码到手机
                        do something;*/
                        cDTimer.start();
                        break;
                    case R.id.confirm_button:
                        //验证填写的验证码，正确就登入，错误就弹Toast
                        break;
                    default:
                        break;
                }
            }
        };
        sendButton.setOnClickListener(onClickListener);
        confirmButton.setOnClickListener(onClickListener);
        //刷新UI要用到handler传送消息
        handler = new Handler() {
            public void handleMessage(Message message){
                switch (message.what){
                    /*case 0:
                        sendButton.setText("重新发送(" + time + ")");
                        break;*/
                    case 1:
                        verificationCodeEditText.setText(verCode);
                        break;
                    default:
                        break;
                }
            }
        };
        //创建一个意图过滤器，接收收到短信时的广播
        IntentFilter intentFilter = new IntentFilter();
        //只有用于过滤广播的IntentFilter可以在代码中创建，其他的IntentFilter必须在manifest文件中声明
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        //设置优先级，由于参数必须为整数，所以简单的设置为Integer.MAX_VALUE——最大整数
        //事实上，这里可以衍生开去，进行短信的拦截等操作，和之前的电话录音配合，基本上能完成一部手机的基本监听需求
        intentFilter.setPriority(Integer.MAX_VALUE);
        //定制广播接收器
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //生成一个数组将短信内容赋值进去
                //intent.getExtras()方法就是从过滤后的意图中获取携带的数据，这里携带的是以"pdus"为key、短信内容为value的键值对
                //Android设备接收到的SMS是以pdu形式的(protocol description unit)
                Object[] pdus = (Object[]) intent.getExtras().get("pdus") ;
                /*这里for循环也可以用
                for(int i = 0; i < pdus.length; i++){
                    byte[] pdu[i] = (byte[]) pdus[i];
                    do something
                }
                */
                //遍历pdus数组，将每一次访问得到的数据放入object中
                for(Object object : pdus){
                    byte[] pdu = (byte[]) object;
                    /*
                    6.0系统开始不建议使用createFromPdu(byte[])方法，只能使用createFromPdu(byte[]，String format)方法
                    String format = intent.getExtras().getString("format");
                    SmsMessage smsMessage = SmsMessage.createFromPdu(pdu,format);
                    */
                    //获取短信
                    SmsMessage smsMessage = SmsMessage.createFromPdu(pdu);
                    //获取短信体，即内容
                    String messageBody = smsMessage.getMessageBody();
                    //获取短信发送方地址
                    String originationAddress = smsMessage.getOriginatingAddress();
                    //如果短信发送地址不为空，这里也可以使用equals限定短信发送方
                    if(!TextUtils.isEmpty(originationAddress)){
                        //解析短信体
                        String code = patternCode(messageBody);
                        if(!TextUtils.isEmpty(code)){
                            verCode = code;
                            //sendEmptyMessage(int what)方法是发送指定的消息，该方法为仅仅只是传递一个int值来表示发送的消息类型
                            handler.sendEmptyMessage(1);
                        }
                    }
                }
            }
        };
        //注册广播接收器
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //动态注册就不能忘记取消注册
        unregisterReceiver(broadcastReceiver);
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
