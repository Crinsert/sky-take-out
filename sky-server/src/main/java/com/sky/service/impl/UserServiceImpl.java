package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatProperties weChatProperties;
    @Override
    public User wexinlogin(UserLoginDTO userLoginDTO) {
        String openId = getOpenId(userLoginDTO.getCode());
        log.info("微信登录，获取openid：{}", openId);
        if (openId == null)
            throw new LoginFailedException("微信登录失败");
        User user=userMapper.login(openId);
        if (user==null){
            user=User.builder()
                    .openid(openId)
                    .createTime(LocalDateTime.now())
                    .build();
        }
        userMapper.insert(user);
        return user;

    }
    /**
     *  参数名	类型	必填	说明
     *                 appid	string	是	小程序 appId
     *                 secret	string	是	小程序 appSecret
     *                 js_code	string	是	登录时获取的 code，可通过wx.login获取
     *                 grant_type	string	是	授权类型，此处只需填写 authorization_code
     *                 请求体
     */
    private String getOpenId(String code) {
        log.info("===== 微信登录调试信息 =====");
        log.info("微信小程序appid: {}", weChatProperties.getAppid());
        log.info("微信小程序secret: {}", weChatProperties.getSecret());
        log.info("接收到的code: {}", code);
        log.info("code长度: {}", code != null ? code.length() : 0);

        String url = "https://api.weixin.qq.com/sns/jscode2session";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid",weChatProperties.getAppid());
        paramMap.put("secret",weChatProperties.getSecret());
        paramMap.put("js_code",code);
        paramMap.put("grant_type", "authorization_code");

        log.info("请求URL: {}", url);
        log.info("请求参数: {}", paramMap);

        String json = HttpClientUtil.doGet(url, paramMap);
        log.info("微信接口返回结果: {}", json);

        JSONObject jsonObject = JSONObject.parseObject(json);

        if (jsonObject.containsKey("errcode")) {
            Integer errcode = jsonObject.getInteger("errcode");
            String errmsg = jsonObject.getString("errmsg");
            log.error("===== 微信登录失败 =====");
            log.error("错误码: {}", errcode);
            log.error("错误信息: {}", errmsg);

            if (errcode == 40029) {
                log.error("原因: code无效（可能已使用、已过期或格式错误）");
            } else if (errcode == 40013) {
                log.error("原因: appid无效");
            } else if (errcode == 40002) {
                log.error("原因: secret无效");
            }
        }

        return jsonObject.getString("openid");
    }
}
