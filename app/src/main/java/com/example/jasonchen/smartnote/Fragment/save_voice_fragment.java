package com.example.jasonchen.smartnote.Fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.jasonchen.smartnote.setting.IatSettings;
import com.example.jasonchen.smartnote.util.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.sunflower.FlowerCollector;
import com.example.jasonchen.smartnote.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Jason Chen on 2017/10/28.
 */
public class save_voice_fragment extends Fragment implements View.OnClickListener{
    private String mUrl ="http://192.168.191.1:8080/SmartNoteServer/";
    private OkHttpClient okHttpClient = new OkHttpClient();
    private static String TAG = save_voice_fragment.class.getSimpleName();
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    private EditText mResultText;
    private TextView keyword;
    private Toast mToast;
    private SharedPreferences mSharedPreferences;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private boolean mTranslateEnable = false;

    private Button startButtom;
    private Button endButtom;
    private Button saveButtom;
    private Button clearButtom;
    private Button openButtom;
    private Button downloadButtom;
    private Button getkeywordButtom;
    private View view;
    private SharedPreferences userpref;
    private String result;
    private String content;
    private Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.save_voice,container,false);
        init();
        mIat = SpeechRecognizer.createRecognizer(getActivity(), mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(getActivity(), mInitListener);
        mSharedPreferences = getActivity().getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        return view;
    }

    public void init(){
        startButtom = view.findViewById(R.id.iat_recognize);
        endButtom = view.findViewById(R.id.iat_stop);
        saveButtom = view.findViewById(R.id.iat_save);
        clearButtom = view.findViewById(R.id.iat_clear);
        openButtom = view.findViewById(R.id.Openfile);
        downloadButtom = view.findViewById(R.id.Downloadfile);
        mResultText = view.findViewById(R.id.iat_text);
        keyword = view.findViewById(R.id.Keyword_text);
        userpref = getActivity().getSharedPreferences("UserInfo",Context.MODE_PRIVATE);
        getkeywordButtom = view.findViewById(R.id.get_keyword);
        startButtom.setOnClickListener(this);
        endButtom.setOnClickListener(this);
        saveButtom.setOnClickListener(this);
        clearButtom.setOnClickListener(this);
        openButtom.setOnClickListener(this);
        downloadButtom.setOnClickListener(this);
        getkeywordButtom.setOnClickListener(this);
        handler= new Handler();

    }

    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(getActivity(),"初始化失败，错误码：" + code,Toast.LENGTH_SHORT).show();
            }
        }
    };

    int ret = 0;

    @Override
    public void onClick(View view) {
        if( null == mIat ){
            // 创建单例失败，与 21001 错误为同样原因，参考 http://bbs.xfyun.cn/forum.php?mod=viewthread&tid=9688
            Toast.makeText(getActivity(),"创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化",Toast.LENGTH_SHORT).show();
            return;
        }

        switch (view.getId()){
            case R.id.iat_recognize:
                // 移动数据分析，收集开始听写事件
                FlowerCollector.onEvent(getActivity(), "iat_recognize");
                mIatResults.clear();
                // 设置参数
                setParam();
                    // 不显示听写对话框
                ret = mIat.startListening(mRecognizerListener);
                if (ret != ErrorCode.SUCCESS) {
                    Toast.makeText(getActivity(),"听写失败,错误码：" + ret,Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(),"请开始说话~~~",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iat_stop:
                mIat.stopListening();
                Toast.makeText(getActivity(),"停止听写",Toast.LENGTH_SHORT).show();
                break;

            case R.id.iat_clear:
                mResultText.setText(null);
                break;
            case R.id.iat_save:
                WriteFiles(mResultText.getText().toString());
                PostFile();
                break;

            case R.id.Openfile:
                try {
                    Log.e("xxx","qaa");
                    result = readFile();
                    Toast.makeText(getActivity(),"result"+"111",Toast.LENGTH_LONG).show();
                    mResultText.setText(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.Downloadfile:
                downloadFile();
                break;

            case R.id.get_keyword:
                getKeyword();
                break;
        }
    }



    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        this.mTranslateEnable = mSharedPreferences.getBoolean("translate", false );
        if( mTranslateEnable ){
            Log.i( TAG, "translate enable" );
            mIat.setParameter( SpeechConstant.ASR_SCH, "1" );
            mIat.setParameter( SpeechConstant.ADD_CAP, "translate" );
            mIat.setParameter( SpeechConstant.TRS_SRC, "its" );
        }
            // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");



        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
    }

    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
        }

        @Override
        public void onBeginOfSpeech() {
            Toast.makeText(getActivity(),"开始说话",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            Log.d(TAG, recognizerResult.getResultString());
            if( mTranslateEnable ){
                printTransResult(recognizerResult);
            }else{
                printResult(recognizerResult);
            }
            if (b) {
                // TODO 最后的结果
            }
        }

        @Override
        public void onError(SpeechError error) {
            if(mTranslateEnable && error.getErrorCode() == 14002) {
                Toast.makeText(getActivity(),error.getPlainDescription(true)+"/n请确认是否开通翻译功能",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),error.getPlainDescription(true),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    private void printTransResult (RecognizerResult results) {
        String trans  = JsonParser.parseTransResult(results.getResultString(),"dst");
        String oris = JsonParser.parseTransResult(results.getResultString(),"src");

        if( TextUtils.isEmpty(trans)||TextUtils.isEmpty(oris) ){
            Toast.makeText(getActivity(),"解析结果失败，请确认是否已开通翻译功能。",Toast.LENGTH_SHORT).show();
        }else{
            mResultText.setText( "原始语言:\n"+oris+"\n目标语言:\n"+trans );
        }

    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }

        mResultText.setText(resultBuffer.toString());
        mResultText.setSelection(mResultText.length());
    }

    public void WriteFiles(String content){
        FileOutputStream fos = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory(),"SmartNote/files/a.txt");
            if (!file.exists()){
                file.createNewFile();
            }
            Log.e("AAA",file.toString());
            ContentValues values = new ContentValues();
            values.put("userid",userpref.getString("isonline",""));
            values.put("filepath",file.toString());
            fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
   }

    private void PostFile() {
        File file = new File(Environment.getExternalStorageDirectory(),"SmartNote/files/a.txt");
        if (!file.exists()){
            Log.e("ttt","not exist");
            return;
        }
        Log.e("ionline",userpref.getString("isonline", ""));
        MultipartBody.Builder builder = new MultipartBody.Builder();
        MultipartBody multipartBody = builder.setType(MultipartBody.FORM)
                .addFormDataPart("userid", userpref.getString("isonline", ""))
                .addFormDataPart("mfile", "a.txt", RequestBody.create(MediaType.parse("application/octet-stream"), file)).build();

        Request request= new Request.Builder()
                .url(mUrl+"postFile")
                .post(multipartBody)
                .build();
        Call call=okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("Failure","Failure"+e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    public String readFile() throws IOException {
        String content = null;
        File file = new File(Environment.getExternalStorageDirectory(),"SmartNote/files/a.txt");
        FileInputStream fis = new  FileInputStream(file);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte [] buffer = new  byte[1024];
        int len = 0;

        while ((len = fis.read(buffer)) != -1){
            baos.write(buffer,0,len);
        }
        content = baos.toString();
        fis.close();
        baos.close();
        return content;
    }

    public void downloadFile(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Request.Builder builder = new Request.Builder();
                Request request = builder
                        .get()
                        .url(mUrl + "files/a.txt")
                        .build();

                Call call=okHttpClient.newCall(request);

                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("Failure","Failure"+e.getMessage());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream inputStream = response.body().byteStream();
                        File file = new File(Environment.getExternalStorageDirectory(),"SmartNote/files/a.txt");
                        byte[] buff = new byte[128];
                        int len = 0;
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        while ((len = inputStream.read(buff))!= -1){
                            fileOutputStream.write(buff,0,len);
                        }
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        inputStream.close();
                    }
                });
            }
        });
        thread.start();
    }


    public void getKeyword() {

        Request.Builder builder = new Request.Builder();
        Request request = builder.url(mUrl+"getkeyword").build();
        Call call=okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                content=res;
                handler.post(runnableUi);
            }
        });
    }

    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            keyword.setText("天气");
            Log.i("A",content);
        }
    };

}
