package com.sinohealth.web.generate.user;

import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.mail.EmailDefaultHandler;
import com.sinohealth.common.utils.*;
import com.sinohealth.common.utils.uuid.UUID;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.system.service.ISysUserService;
import com.sinohealth.system.service.impl.SysUserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;


import javax.mail.internet.InternetAddress;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @Author Rudolph
 * @Date 2022-10-31 16:49
 * @Desc
 */
@Slf4j
@Component
public class UserApiGenerator {

    @Autowired
    ISysUserService userService;

    private static String names = "赖志城\n" +
            "马至伟\n" +
            "夏天立\n" +
            "苏才华\n" +
            "李俊国\n" +
            "张天行\n" +
            "王梦良\n" +
            "杨梓榕\n" +
            "方海涛\n" +
            "林韵诗\n" +
            "欧杰聪\n" +
            "胡乐思\n" +
            "骆芷珊\n" +
            "赵梦潆\n" +
            "刘淑媚\n" +
            "雷玉洁\n" +
            "邓宝瑶\n" +
            "梁莉莉\n" +
            "欧沛琳\n" +
            "韩佳佳\n" +
            "黄烁林\n" +
            "王婷婷\n" +
            "李晓清\n" +
            "陈凌风\n" +
            "翟紫姹\n" +
            "李定欣\n" +
            "吴倩怡\n" +
            "彭璐\n" +
            "李普红\n" +
            "谢嘉倩\n" +
            "赖赏\n" +
            "张姣君\n" +
            "赵昕\n" +
            "蔡彦如\n" +
            "黄肖贤\n" +
            "陈晓丽\n" +
            "陈嘉森\n" +
            "陈嘉森\n" +
            "王晓燕\n" +
            "严砺寒\n" +
            "曾明智\n" +
            "郭少婷\n" +
            "江泳施\n" +
            "吴飞杏\n" +
            "黄小燕\n" +
            "邱惠莹\n" +
            "阮冠朝\n" +
            "温建\n" +
            "胡玥\n" +
            "钟卓冰\n" +
            "李曼容\n" +
            "陈泳洁\n" +
            "高晓琳\n" +
            "花思桦\n" +
            "曾穗德\n" +
            "王强\n" +
            "权凤兰\n" +
            "赵思扬\n" +
            "梁志东\n" +
            "李超琳\n" +
            "黄毅宁\n" +
            "蔡宗希\n" +
            "罗伟金\n" +
            "肖裕锋\n" +
            "蓝师峰\n" +
            "张仕淋\n" +
            "邹超\n" +
            "包景媚\n" +
            "于海鹏\n" +
            "钟思倩\n" +
            "梁结莹\n" +
            "陈建雄\n" +
            "黄凯媛\n" +
            "陈施亮\n" +
            "胡帆\n" +
            "胡职昆\n" +
            "徐东\n" +
            "李奎忠\n" +
            "卢晓敏\n" +
            "叶秀\n" +
            "李翠芳\n" +
            "戴少萍\n" +
            "梁燕埼\n" +
            "李铖\n" +
            "朝克\n" +
            "梁金龙\n" +
            "彭一文\n" +
            "吴六一\n" +
            "马小凤\n" +
            "刘巧艺\n" +
            "吴奕强\n" +
            "陈欣霞\n" +
            "叶嫚嫚\n" +
            "林紫凤\n" +
            "朱玲\n" +
            "林冰\n" +
            "林木丹\n" +
            "钟文兰\n";

    private Long baseRoleId = 38L; //内部员工基础角色

    public static void main(String[] args) {
        GenericApplicationContext context = new GenericApplicationContext();
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        beanFactory.registerBeanDefinition("sinoipaasUtilsBean", BeanDefinitionBuilder.genericBeanDefinition(SinoipaasUtils.class).getBeanDefinition());

        beanFactory.registerBeanDefinition("userBean", BeanDefinitionBuilder.genericBeanDefinition(SysUserServiceImpl.class).getBeanDefinition());

        context.refresh();
        SinoipaasUtils bean = beanFactory.getBean(SinoipaasUtils.class);
        bean.setY_T_APPID("2306991630202187778");
        bean.setToken("70723470663176656a33656972767a75");
        bean.setUrl("https://gateway.sinohealth.com");
        bean.init();

        List<String> nameList = Arrays.stream(names.split("\n")).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.toList());

        Map<String, SinoPassUserDTO> userMap = new HashMap<>();

        List<SinoPassUserDTO> users = SinoipaasUtils.employeeSelectbypage("", 0, 99999)
                .stream().filter(u -> u.getEmail().endsWith("@sinohealth.cn") && !u.getEmployeeStatusText().equalsIgnoreCase("离职"))
                .collect(Collectors.toList());

        users.stream().forEach(u -> userMap.put(u.getUserName(),u));

        for (String n : nameList) {
            if (userMap.containsKey(n)) {

            }
        }
        System.out.println(users);

    }

    public Map<String, List<String>> initUserInfo() throws UnsupportedEncodingException {

        List<String> successHandleList = new ArrayList<>();
        List<String> failureHanldeList = new ArrayList<>();

        List<String> nameList = Arrays.stream(names.split("\n")).filter(s -> StringUtils.isNotBlank(s)).collect(Collectors.toList());

        Map<String, SinoPassUserDTO> userMap = new HashMap<>();

        List<SinoPassUserDTO> users = SinoipaasUtils.employeeSelectbypage("", 0, 99999)
                .stream().filter(u -> u.getEmail().endsWith("@sinohealth.cn") && !u.getEmployeeStatusText().equalsIgnoreCase("离职"))
                .collect(Collectors.toList());

        users.stream().forEach(u -> userMap.put(u.getUserName(),u));

        for (String n : nameList) {
            if (userMap.containsKey(n)) {
                try {
                    SinoPassUserDTO sinoPassUserDTO = userMap.get(n);
                    String userName = sinoPassUserDTO.getEmail().replaceAll("@sinohealth.cn", "");
                    Optional<SysUser> sysUser = Optional.ofNullable(userService.selectUserByUserName(userName));
                    AtomicReference<String> password = new AtomicReference<>();

                    if (!sysUser.isPresent()) {
                        // 1.如果不存在账号则新建账号信息
                        password.set(userName + (new Random().nextInt(9999 - 1000 + 1) + 1000));
                        SysUser newUser = new SysUser();
                        newUser.setUserName(userName);
                        newUser.setRealName(sinoPassUserDTO.getUserName());
                        newUser.setPhonenumber(sinoPassUserDTO.getMobilePhone());
                        newUser.setEmail(sinoPassUserDTO.getEmail());
                        newUser.setUserInfoType(CommonConstants.INNER_UESR);
                        newUser.setCreateBy("system_init");
                        newUser.setCreateTime(DateUtils.getNowDate());
                        newUser.setUpdateTime(DateUtils.getNowDate());
                        newUser.setOrgUserId(sinoPassUserDTO.getId());
                        newUser.setPassword(SecurityUtils.encryptPassword(password.get()));
                        newUser.setToken(UUID.fastUUID().toString());
                        userService.insertUser(newUser, new ArrayList<Long>() {{add(baseRoleId);}}, null, null, Collections.emptyList());
                        successHandleList.add(n);
                    }
                    sysUser.ifPresent(u->{
                        // 2.如果存在用户则修改密码
                        password.set(u.getUserName() + (new Random().nextInt(9999 - 1000 + 1) + 1000));
                        u.setPassword(SecurityUtils.encryptPassword(password.get()));
                        userService.updateUserProfile(u);
                        successHandleList.add(n);
                    });
                    // 3. 发送密码信息给对应用户
                    EmailDefaultHandler.setEmailFrom(new InternetAddress("tech@sinohealth.cn", "中康科技", "UTF-8").toString());
                    EmailDefaultHandler emailDefaultHandler = new EmailDefaultHandler();
                    String content = String.format("您好,%s，您的易数阁初始化账号：%s， 初始化密码： %s， 登录地址： %s",sinoPassUserDTO.getUserName(), userName, password, "http://tgysg.sinohealth.cn");
                    emailDefaultHandler.SendMsg(sinoPassUserDTO.getEmail(), "天宫易数阁账号测试环境初始化" , content);
                } catch (Exception e) {
                    failureHanldeList.add(n);
                    log.info("异常信息：{}", e);
                    log.info("-----------------------------------");
                    log.info("当前人员：{}", n);
                    log.info("已成功处理列表：{}", successHandleList);
                }

            }

        }

        failureHanldeList.addAll(nameList);
        failureHanldeList.removeAll(successHandleList);
        List<String> distinctFailureHanldeList = failureHanldeList.stream().distinct().collect(Collectors.toList());
        return new HashMap<String, List<String>>() {{
            put("成功处理列表", successHandleList);
            put("失败处理列表", distinctFailureHanldeList);
        }};

    }

    public static UserApiGenerator newInstance() {
        return new UserApiGenerator();
    }

}
