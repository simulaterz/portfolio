package th.demo.portfolio.repository.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import th.demo.portfolio.component.SHAComponent;
import th.demo.portfolio.configuration.property.JwtProperty;
import th.demo.portfolio.model.BaseUserModel;
import th.demo.portfolio.model.redis.AccessTokenRedis;
import th.demo.portfolio.model.redis.RefreshTokenRedis;
import th.demo.portfolio.repository.RedisClient;

import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Test -> Authentication Repository")
class AuthenticationRedisRepositoryImplTest {

    @InjectMocks
    private AuthenticationRedisRepositoryImpl repository;

    @Mock
    private RedisClient redisClient;

    @Mock
    private SHAComponent shaComponent;

    @Spy
    private JwtProperty property = new JwtProperty();

    private final String ACCESS_KEY = "ACCESSKEY:";
    private final String REFRESH_KEY = "REFRESHKEY:";
    private final long EXPIRY_SECONDS = TimeUnit.MILLISECONDS.toSeconds(1L);

    @BeforeEach
    public void setUp() {
        var key = JwtProperty.JwtKeyProperty.builder()
                .access("ACCESSKEY:")
                .refresh("REFRESHKEY:")
                .build();

        property.setSecret("jwtsecret");
        property.setKey(key);
    }

    @Test
    @DisplayName("save access token, expected success")
    void saveAccessTokenHashToRedis() throws NoSuchAlgorithmException, JsonProcessingException {
        var accessToken = "access";
        var hashAccessToken = "hashString";
        var baseUserModel = new BaseUserModel();

        var expectedKey = ACCESS_KEY + hashAccessToken;

        doReturn(hashAccessToken).when(shaComponent).toSHA256String(accessToken);
        doNothing().when(redisClient).setObject(eq(expectedKey), any(AccessTokenRedis.class), anyLong());

        repository.saveAccessTokenHashToRedis(accessToken, baseUserModel, 1L);

        var expectRedisModel = AccessTokenRedis.builder()
                        .baseUserModel(baseUserModel)
                        .build();

        verify(shaComponent, times(1)).toSHA256String(accessToken);
        verify(redisClient, times(1)).setObject(expectedKey, expectRedisModel, EXPIRY_SECONDS);
    }

    @Test
    @DisplayName("save refresh token, expected success")
    void saveRefreshTokenHashToRedis() throws NoSuchAlgorithmException, JsonProcessingException {
        var accessToken = "access";
        var refreshToken = "refresh";
        var hashRefreshToken = "hashRefreshString";
        var hashAccessToken = "hashAccessString";
        var baseUserModel = new BaseUserModel();

        var expectedKey = REFRESH_KEY + hashRefreshToken;

        doReturn(hashAccessToken).when(shaComponent).toSHA256String(accessToken);
        doReturn(hashRefreshToken).when(shaComponent).toSHA256String(refreshToken);
        doNothing().when(redisClient).setObject(eq(expectedKey), any(RefreshTokenRedis.class), anyLong());

        repository.saveRefreshTokenHashToRedis(refreshToken, accessToken, baseUserModel, 1L);

        var expectRedisModel = RefreshTokenRedis.builder()
                .baseUserModel(baseUserModel)
                .accessTokenHash(hashAccessToken)
                .build();

        verify(shaComponent, times(1)).toSHA256String(accessToken);
        verify(shaComponent, times(1)).toSHA256String(refreshToken);
        verify(redisClient, times(1)).setObject(expectedKey, expectRedisModel, EXPIRY_SECONDS);
    }

    @Test
    @DisplayName("get access token, expected success")
    void getAccessTokenDetail() throws NoSuchAlgorithmException, JsonProcessingException {
        var hashAccessToken = "hashString";
        var baseUserModel = new BaseUserModel();

        var expectedKey = ACCESS_KEY + hashAccessToken;
        var expectRedisModel = AccessTokenRedis.builder()
                .baseUserModel(baseUserModel)
                .build();

        doReturn(expectRedisModel).when(redisClient).getObjectByKey(expectedKey, AccessTokenRedis.class);

        var response = repository.getAccessTokenDetail(hashAccessToken);

        assertEquals(expectRedisModel, response);
    }

    @Test
    @DisplayName("get refresh token, expected success")
    void getRefreshTokenDetail() throws NoSuchAlgorithmException, JsonProcessingException {
        var hashRefreshToken = "hashRefreshString";
        var hashAccessToken = "hashAccessString";
        var baseUserModel = new BaseUserModel();

        var expectedKey = REFRESH_KEY + hashRefreshToken;
        var expectRedisModel = RefreshTokenRedis.builder()
                .baseUserModel(baseUserModel)
                .accessTokenHash(hashAccessToken)
                .build();

        doReturn(expectRedisModel).when(redisClient).getObjectByKey(expectedKey, RefreshTokenRedis.class);

        var response = repository.getRefreshTokenDetail(hashRefreshToken);

        assertEquals(expectRedisModel, response);
    }

    @Test
    @DisplayName("deleteOldToken, expected success")
    void deleteOldToken() {
        var hashAccessToken = "hashAccessString";
        var hashRefreshToken = "hashRefreshString";

        var expectedAccessKey = ACCESS_KEY + hashAccessToken;
        var expectedRefreshKey = REFRESH_KEY + hashRefreshToken;

        repository.deleteOldToken(hashAccessToken, hashRefreshToken);

        verify(redisClient, times(1)).del(expectedAccessKey);
        verify(redisClient, times(1)).del(expectedRefreshKey);
    }
}