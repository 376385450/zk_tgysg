package com.sinohealth.common.utils.phone;

import com.sinohealth.common.core.mail.EmailDefaultHandler;
import com.sinohealth.common.core.redis.RedisCache;
import com.sinohealth.common.enums.ResultEnum;
import com.sinohealth.common.exception.CustomException;
import com.sinohealth.common.utils.spring.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.mail.internet.InternetAddress;
import java.util.concurrent.TimeUnit;

@Component
public class MsgCodeUtil {

    private static Logger log = LoggerFactory.getLogger(MsgCodeUtil.class);

    private static final String MSG_START_TIME = "msg_start_time"; //短信发送时间
    private static final String MOBILE_MSG_CODE = "mobile_msg_code"; //验证码
    private static final Integer CODE_VALID_TIME = 30; //验证码有效时间,单位:分钟


    /**
     * 发送验证码
     *
     * @param username 验证码
     * @param phone    电话号码
     */
    public static void sendCode(String username, String phone) throws Exception {
        //获取当前时间
        long startTime = System.currentTimeMillis();
        Long time = SpringUtils.getBean(RedisCache.class).getCacheObject(MSG_START_TIME + "_" + phone);
        long startTimefromSessioin = time == null ? 0L : time;
        //验证码获取一次最少60秒 为了防止网络延迟 设置成55
        if (((startTime - startTimefromSessioin) / 1000) <= (long) 55) {
            //发送验证码太频繁
            throw new CustomException(ResultEnum.MESSAGE_TIME_ERROR.getMsg());
        }
        //发送短信
        String msgCode = createRandomVcode();
        log.info(">>>>>>>>>> 发送手机短信验证码：" + msgCode);
        // 调用第三方接口发送短信
        Integer status = SpringUtils.getBean(SmsComponent.class).sendSms("中康科技", phone, username, msgCode);
        //Boolean sendSuccess = response.isSuccess();
        if (200 == status) {
            //短信发送成功,redis
            saveCode(phone, msgCode);
        } else {
            //短信发送失败,请稍后再试
            throw new CustomException(ResultEnum.MESSAGE_SEND_ERROR.getMsg());
        }

    }

    //校验验证码
   /* public static boolean validateCode(String mobile, String code) {
        //判断该手机号是否发送过验证码
        String mobileFromSession = (String) session.getAttribute(MOBILE_MSG_MOBILE);
        if (StringUtils.isBlank(mobileFromSession)) {
            //未发送过验证码
            throw new CustomException(ResultEnum.MOBILE_NOT_SEND_CODE.getMsg());
        }

        //判断手机号是否是发送验证码的手机号
        if (!mobile.equals(mobileFromSession))
            throw new CustomException(ResultEnum.MOBILE_CAN_NOT_CHANGE.getMsg());

        //获取短信发送时间
        long startTimeFromSession = (long) session.getAttribute(MSG_START_TIME);
        //判断当前验证码是否失效
        if (((System.currentTimeMillis() - startTimeFromSession) / 1000) <  ((long) CODE_VALID_TIME * 60)) {
            throw new CustomException(ResultEnum.MESSAGE_CODE_INVALID.getMsg());
        }

        //判断验证码是否输入正确
        if (!code.equals((String) session.getAttribute(MOBILE_MSG_CODE))) {
            throw new CustomException(ResultEnum.MESSAGE_CODE_ERROR.getMsg());
        }

        //校验通过
        return true;
    }*/


    /**
     * 短信发送成功后,保存验证码
     */
    public static void saveCode(String phone, String msgCode) {
        SpringUtils.getBean(RedisCache.class).setCacheObject(MSG_START_TIME + "_" + phone, System.currentTimeMillis(), CODE_VALID_TIME, TimeUnit.MINUTES);
        SpringUtils.getBean(RedisCache.class).setCacheObject(MOBILE_MSG_CODE + "_" + phone, msgCode, CODE_VALID_TIME, TimeUnit.MINUTES);
    }

    /**
     * 使用验证码后,销毁验证码
     */
    public static void consumeCode(String phone) {
        SpringUtils.getBean(RedisCache.class).deleteObject(MSG_START_TIME + "_" + phone);
        SpringUtils.getBean(RedisCache.class).deleteObject(MOBILE_MSG_CODE + "_" + phone);
    }

    /**
     * 生成6位数字验证码
     */
    public static String createRandomVcode() {
        String vcode = "";
        for (int i = 0; i < 6; i++) {
            vcode = vcode + (int) (Math.random() * 9);
        }
        return vcode;
    }


    /**
     * 发送验证码
     *
     * @param username 验证码
     * @param email    电话号码
     */
    public static void sendEmailCode(String username, String email) throws Exception {
        //获取当前时间
        long startTime = System.currentTimeMillis();
        Long time = SpringUtils.getBean(RedisCache.class).getCacheObject(MSG_START_TIME + "_" + email);
        long startTimefromSessioin = time == null ? 0L : time;
        //验证码获取一次最少60秒 为了防止网络延迟 设置成55
        if (((startTime - startTimefromSessioin) / 1000) <= (long) 55) {
            //发送验证码太频繁
            throw new CustomException(ResultEnum.MESSAGE_TIME_ERROR.getMsg());
        }
        //发送短信
        String msgCode = createRandomVcode();
        //发送邮箱验证码
        EmailDefaultHandler.setEmailFrom(new InternetAddress("tech@sinohealth.cn", "中康科技", "UTF-8").toString());
        EmailDefaultHandler emailDefaultHandler = new EmailDefaultHandler();
        String content = String.format("尊敬的%s，天宫易数阁系统验证码：%s，有效期30分钟，请尽快进行验证", username, msgCode);
        emailDefaultHandler.SendMsg(email, "天宫易数阁验证码", content);
        saveCode(email, msgCode);
    }
}
