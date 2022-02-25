package show.grip.example.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Setter;

@Setter
public class UserInfo {
    private String serviceId;
    private String userId;
    private String nickname;
    private String host;
    private String gender;
    private String ageRange;

    @Override
    public String toString() {
        ObjectMapper objMapper = new ObjectMapper();

        ObjectNode user = objMapper.createObjectNode();
        user.put("serviceId", serviceId);
        user.put("userId", userId);
        user.put("nickname", nickname);
        user.put("host", host);
        user.put("timestamp", System.currentTimeMillis()); // Require
        // userInfo.setGender(....)
        // userInfo.setAgeRange(....)

        // 사용하지 않는 값에 대해서는 빼줘야한다. 그렇지 않으면 오류.
        if (gender != null && gender.trim().length() == 0) {
            user.put("gender", gender);
        }

        if (ageRange != null && ageRange.trim().length() != 0) {
            user.put("ageRange", ageRange);
        }

        String result = null;

        try {
            result = objMapper.writeValueAsString(user);
        } catch (JsonProcessingException ex) {
            result = null;
        }

        return result;
    }
}
