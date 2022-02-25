package show.grip.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import show.grip.example.config.UserInfo;
import show.grip.example.utils.AESCryptor;
import show.grip.example.utils.HmacSha256Signature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Controller
@RequestMapping("/")
public class ViewController {
    private static final String REST = "https://eapi.grip.show";

    @GetMapping()
    public String player(Model model) {
        // Todo: 초기값 세팅 필수
        /*
        * 파트너센터 > 상단 바 > 유저 프로필 클릭 > 드롭다운 메뉴에서 API 클릭
        * Service ID, Secret Key, User Code에 대해서는 파트너 센터의 위 메뉴에서 확인 가능합니다.
        * */
        String serviceId = null;
        String secretKey = null;
        String userCode = null;

        if (serviceId == null || serviceId == "") {
            throw new RuntimeException("Service ID를 기재해주세요.");
        }
        else if (secretKey == null || secretKey == "") {
            throw new RuntimeException("secretKey를 기재해주세요.");
        }
        else if (userCode == null || userCode == "") {
            throw new RuntimeException("userCode를 기재해주세요.");
        }

        String originUserId = "userId";
        String originNickname = "홍길동";

        // 1. 유저 정보 세팅 (UserInfo.java 참고)
        // Todo: 민감 정보 암호화 필수
        UserInfo userInfo = new UserInfo();
        userInfo.setServiceId(serviceId);
        userInfo.setUserId(strToSHA256(originUserId)); // 그립 클라우드 내 개인정보보호 이슈 해소를 위한 암호화 처리
        userInfo.setNickname(originNickname);
        userInfo.setHost("www.example.com"); // Todo: 실제 고객사에서 서비스되는 도메인

        // 채팅등의 기능을 이용하기 위해서는 Session Key 필요함.
        // 잘못된 SessionKey 혹은 SessionKey가 없을시에는 시청만 가능
        String sessionKey = AESCryptor.encrypt(userInfo.toString(), secretKey); // 세션키 생성

        log.info("Used user info: " + userInfo.toString());
        log.info("Secret Key: " + secretKey);
        log.info("Session Key: " + sessionKey);

        // 2. api 요청을 위한 fingerprint 발급
        // 그립 클라우드 내 API 호출 시 요청 헤더에 들어가는 필수 값
        long currentTime = System.currentTimeMillis();
        String fingerprint = HmacSha256Signature.generateFingerprint(serviceId, currentTime, secretKey);

        log.info("X-Fingerprint-Timestamp: " + Long.toString(currentTime));
        log.info("X-Fingerprint: " + fingerprint);

        // 3. API를 통한 예약된 방송이 있는지 조회
        // Todo: 모든 API 요청 시 ServiceId 파라미터는 필수입니다.
        Date date = new Date();
        SimpleDateFormat start = new SimpleDateFormat("yyyy-MM-dd%2000:00:00");
        SimpleDateFormat end = new SimpleDateFormat("yyyy-MM-dd%2023:59:59");

        String reservationUri = "serviceId=" + serviceId + "&" +
                                "userCode=" + userCode + "&" +
                                "start=" + start.format(date) + "&" +
                                "end=" + end.format(date);

        String reservationId = null;

        HttpGet reservation = new HttpGet(REST + "/svc/v2/reservations?" + reservationUri);
        reservation.setHeader("Content-Type", "application/json;charset=UTF-8");
        reservation.setHeader("X-Fingerprint-timestamp", Long.toString(currentTime));
        reservation.setHeader("X-Fingerprint", fingerprint);

        try {
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(reservation);
            HttpEntity entity = response.getEntity();

            ObjectMapper objectMapper = new ObjectMapper();

            if (response.getStatusLine().getStatusCode() != 200) {
                GripErrorResponse error = objectMapper.readValue(entity.getContent(), GripErrorResponse.class);

                throw new RuntimeException(error.getMessage());
            }

            Map<String, Object> body = objectMapper.readValue(entity.getContent(), Map.class);
            ArrayList<LinkedHashMap<String, Object>> datas = (ArrayList<LinkedHashMap<String, Object>>) body.get("result");
            reservationId = datas.size() >= 1 ? (String)datas.get(0).get("reservationId") : null;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        model.addAttribute("serviceId", serviceId);
        model.addAttribute("sessionKey", sessionKey);
        model.addAttribute("reservationId", reservationId);

        return "index";
    }

    public GripErrorResponse convertExToGripError(String body) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            GripErrorResponse error = objectMapper.readValue(body, GripErrorResponse.class);

            return error;
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    public String strToSHA256(String text) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
