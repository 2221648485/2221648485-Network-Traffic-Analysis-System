package com.hdu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserActionService {

    private final RestTemplate restTemplate;

    @Value("${pa.api.base-url}")
    private String paBaseUrl;

    @Value("${pa.api.access-token}")
    private String paAccessToken;

    @Value("${aaa.api.base-url}")
    private String aaaBaseUrl;

    @Value("${aaa.api.access-token}")
    private String aaaAccessToken;

    /**
     * 调用 PA 接口，对高风险用户执行强制下线操作。
     */
    public void triggerPaAction(String userName, String riskLevel) {
        if (!"high".equalsIgnoreCase(riskLevel)) return;

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("user_name", userName);

        String url = paBaseUrl + "/api/v2/base/batch-online-drop";

        try {
            ResponseEntity<String> response = sendPostRequest(url, params, paAccessToken);
            log.info("[PA] 用户 {} 强制下线成功，响应：{}", userName, response.getBody());
        } catch (Exception e) {
            log.error("[PA] 强制下线失败，用户：{}", userName, e);
        }
    }

    /**
     * 调用 AAA 接口，对高风险用户应用隔离策略（删除 MAC 认证等）。
     */
    public void applyAaaPolicy(String userName, String riskLevel) {
        if (!"HIGH".equalsIgnoreCase(riskLevel)) return;


        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("user_name", userName);
        params.add("access_token", aaaAccessToken);

        String url = aaaBaseUrl + "/api/v2/base/delete-mac-auth";

        try {
            HttpEntity<MultiValueMap<String, String>> request = buildRequest(params);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            log.info("[AAA] 删除用户 {} 的 MAC 认证成功，响应：{}", userName, response.getBody());
        } catch (Exception e) {
            log.error("[AAA] 删除用户 {} 的 MAC 认证失败", userName, e);
        }
    }

    public void moveUserToRiskGroup(String userName, String riskLevel) {
        if (!"high".equalsIgnoreCase(riskLevel) && !"medium".equalsIgnoreCase(riskLevel)) return;
        // 暂定将中高风险转移到不同的用户组
        String targetGroupId = "high".equalsIgnoreCase(riskLevel) ? "10086" : "10010"; // 示例组ID

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("user_name", userName);
        params.add("group_id", targetGroupId);

        String url = paBaseUrl + "/api/v2/base/change-user-group";

        try {
            ResponseEntity<String> response = sendPostRequest(url, params, paAccessToken);
            log.info("[PA] 成功将用户 {} 移动到 {} 风险组，响应：{}", userName, riskLevel, response.getBody());
        } catch (Exception e) {
            log.error("[PA] 用户 {} 移动用户组失败，风险等级：{}", userName, riskLevel, e);
        }
    }
    /**
     * 统一封装 POST 请求逻辑。
     */
    private ResponseEntity<String> sendPostRequest(String url, MultiValueMap<String, String> params, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        return restTemplate.postForEntity(url, request, String.class);
    }

    /**
     * 构造带认证头的请求实体
     */
    private HttpEntity<MultiValueMap<String, String>> buildRequest(MultiValueMap<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return new HttpEntity<>(params, headers);
    }
}
