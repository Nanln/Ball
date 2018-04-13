package com.xaut.manager;

import com.xaut.constant.Constant;
import com.xaut.dto.TokenModel;
import com.xaut.util.UIDUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Author : wangzhe
 * Description :
 * Version :
 */
@Component
public class TokenManager {


    @Autowired
    private JedisPool jedisPool;

    public TokenModel createToken(String userId) {
        Jedis jedis=jedisPool.getResource();
        // 使用 uuid 作为源 token
        String token= UIDUtil.getRandomUID();
        TokenModel model=new TokenModel(userId, token);
        // 存储到 redis 并设置过期时间
        jedis.set(userId, model.getToken());
        jedis.expire(userId, Constant.TOKEN_EXPIRES_HOUR);
        jedis.close();
        return model;
    }

    public TokenModel getToken(String authentication) {
        if (authentication == null || authentication.length() == 0) {
            return null;
        }
        String[] param=authentication.split("_");
        if (param.length != 2) {
            return null;
        }
        // 使用 userId 和源 token 简单拼接成的 token，可以增加加密措施
        String userId=param[0];
        String token=param[1];
        return new TokenModel(userId, token);
    }

    public boolean checkToken(TokenModel model) {
        if (model == null) {
            return false;
        }

        Jedis jedis=jedisPool.getResource();
        String token=jedis.get(model.getUserId());
        if (token == null || !token.equals(model.getToken())) {
            jedis.close();
            return false;
        }
        // 如果验证成功，说明此用户进行了一次有效操作，延长 token 的过期时间
        jedis.expire(model.getUserId(), Constant.TOKEN_EXPIRES_HOUR);
        jedis.close();
        return true;
    }

    public void deleteToken(String userId) {
        Jedis jedis=jedisPool.getResource();
        jedis.del(userId);
        jedis.close();
    }
}
