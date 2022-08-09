package funtion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PythonConnect {

    String basicUrl = "http://localhost:5000/";
    private static final PythonConnect instance = new PythonConnect();

    private PythonConnect(){

    }

    public static PythonConnect getInstance(){
        return instance;
    }

    public String pytest(String msg) {
        try {
            URL url = new URL(basicUrl+msg);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(200000); //서버에 연결되는 Timeout 시간 설정
            con.setReadTimeout(200000); // InputStream 읽어 오는 Timeout 시간 설정
            con.setRequestMethod("GET");//URLConnection에 대한 doOutput 필드값을 지정된 값으로 설정한다. URL 연결은 입출력에 사용될 수 있다. URL 연결을 출력용으로 사용하려는 경우 DoOutput 플래그를 true로 설정하고, 그렇지 않은 경우는 false로 설정해야 한다. 기본값은 false이다.
            con.setDoOutput(false);

            StringBuilder sb = new StringBuilder();

            if (con.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                //Stream을 처리해줘야 하는 귀찮음이 있음.
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                return sb.toString();
            } else {
                System.out.println(con.getResponseMessage());
            }
        } catch (Exception e) {
            return e.toString();
        }
        return "챗봇 반환 실패";
    }
}
