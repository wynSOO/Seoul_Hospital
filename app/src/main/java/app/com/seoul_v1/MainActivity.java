package app.com.seoul_v1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import app.com.seoul_v1.data.Seoul_Pet_VO;
import app.com.seoul_v1.helper.HostViewAdapter;

public class MainActivity extends AppCompatActivity {

    List<Seoul_Pet_VO> pets;
    RecyclerView host_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pets = new ArrayList<Seoul_Pet_VO>();
        host_list = findViewById(R.id.host_list);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  host_list.scrollToPosition(0);
                host_list.smoothScrollToPosition(0);
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                host_list.scrollToPosition(0);
                return false;
            }
        });

        Seoul_Delegate seoul_delegate = new Seoul_Delegate();
        seoul_delegate.execute(100);
    }

    // seoul_pet() method를 실행할 대리자를 생성한다.
    class Seoul_Delegate extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            Log.i("서울시","대리자 시작");
            seoul_pet();
            return null;
        }

        // doInBackground에서 호출한 method가 모두 일을 마치면
        // 최종 마지막 보고나, 화면정리등르 하는 method가 있다.
        // AsyncTask에 의해서 어떤 method가 호출되고
        // view나 Layout등에 내용을 표시하는 일들을 하는 곳
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.i("서울시","AsyncTask 종료");

            int cnt = 0;
            for(Seoul_Pet_VO vo : pets){
                Log.i("서울시",++cnt+vo.toString());
            }

            // RecyclerView 그리기
            // 1. adapter 생성
            HostViewAdapter adapter = new HostViewAdapter(pets);

            host_list.setAdapter(adapter);

            LinearLayoutManager lm = new LinearLayoutManager(MainActivity.this);

            host_list.setLayoutManager(lm);
        }
    }


    // 실제로 서울 공공 data에 접속해서 데이터를 가져오는 method
    void seoul_pet(){
        String pet_url = "http://openAPI.seoul.go.kr:8088/";
        String service_url = "vtrHospitalInfo";

        String api_url = pet_url ; // 시작 URL
        api_url += Seoul_key.SEOUL_API_KEY; // OpenAPI Key
        api_url += "/json/"; // 바들 데이터 형식
        api_url += service_url; // 서비스 문자열
        api_url += "/1/100/";   // 데이터를 100개 불러와라

        try {
            URL url = new URL(api_url);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 서울시 공공 데이터는 Method만 설정하면 되고,
            // 나머지는 모두 url 문자열에 포함되어있다.
            conn.setRequestMethod("GET");

            // 연결 실행
            int resCode = conn.getResponseCode();
            // => Http Status Code 를 받는다.
            if (resCode==200){
                Log.i("서울시","정상 응답");

                // 실제 데이터를 받는 절차
                InputStreamReader is;
                is = new InputStreamReader(conn.getInputStream());

                BufferedReader buffer;
                buffer = new BufferedReader(is);

                String reader = new String();

                // reader가 데이터를 모두 읽고
                // 또다시 readeLine()을 실행하면
                // reader는 null 이 되어 버린다.
                // 그러기 전에 reader의 값을
                // sb에 보관 해 둘 것입니다.
                StringBuffer sb = new StringBuffer();

                while(true){
                    reader = buffer.readLine();
                    if(reader==null) break;

                    Log.i("서울시",reader);

                    // 문자열 reader를 다른 문자열 변수에
                    // 임시 보관하는 방식인데,
                    // 안드로이드에서는 문자열 = 문자열 + 다른문자열 방식은
                    // 성능상 약간의 문제가 있다고 하여
                    // StringBuffer를 만들고
                    // sb.appened(다른문자열) 형식을 사용하라고 권장함
                    sb.append(reader);
                }

                // String을 JSON 객체로 변환
                JSONObject jsonData = new JSONObject(sb.toString());
                Log.i("서울시","Data:"+jsonData.toString());
                // jsonData 로 부터 필요한 부분만 잘라내기

                /*
                    서울시 공공 data로 부터 return 받은 데이터의 구조
                    jsonData 로부터
                    1. service_url 부분을 자르고
                    2. row 부분을 잘라내어야 실제 원하는 데이터를 얻을 수 있다.
                 */

                // 1. service_url을 기준으로 이하 부분을 잘라내기
                JSONObject jsonVT = jsonData.getJSONObject(service_url);
                Log.i("서울시",service_url+":"+jsonVT.toString());

                // row 이하의 값을 잘라내는데
                // row 이하의 값은 배열 형태로 되어 있기 때문에 JSONArray로 추출
                JSONArray hArray = jsonVT.getJSONArray("row");

                Log.i("서울시","배열개수"+hArray.length());
                Log.i("서울시","배열내용"+hArray.toString());

                // 추출된 배열을 pets List에 옮기기
                for(int i =0;i<hArray.length();i++){

                    // 1개의 데이터를 추출
                    JSONObject jo = (JSONObject) hArray.get(i);

                    // 추출한 데이터를 vo 담고
                    Seoul_Pet_VO vo = new Seoul_Pet_VO();
                    vo.name = jo.getString("NM");
                    vo.addr_old = jo.getString("ADDR_OLD");
                    vo.addr = jo.getString("ADDR");
                    vo.state = jo.getString("STATE");
                    vo.tel = jo.getString("TEL");

                    // vo를 List에 추가
                    pets.add(vo);
                }

                // RecyclerView에 데이터 뿌리기를 한다.라고 하고 싶지만
                // AsyncTask 대리자가 실행하는 method에서는
                // 절대! view,layout 등에 직접 데이터를 표현하는 코드를 쓸 수 없다.

            }else{
                Log.i("서울시","응답X");
                Log.i("서울시","오류코드:"+resCode);
            }


        } catch (MalformedURLException e) {
            // URL 객체 생성
            //e.printStackTrace();
            Log.d("서울시", "URL 객체 생성 오류");
        } catch (IOException e) {
            //e.printStackTrace();
            Log.d("서울시","HttpConnection 생성 오류");
        } catch (JSONException e) {
            //e.printStackTrace();
            Log.d("서울시","문자열을 JSON으로 변화시키는데 오류 발생");
        }


    }
}
