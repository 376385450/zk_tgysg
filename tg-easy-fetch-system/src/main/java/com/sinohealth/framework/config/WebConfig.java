package com.sinohealth.framework.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.sinohealth.framework.config.serializer.CreateResponseDTOJsonDeserializer;
import com.sinohealth.framework.interceptor.DataInterceptor;
import com.sinohealth.saas.file.model.dto.response.CreateResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Jingjun
 * @since 2021/5/11
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(dataInterceptor).addPathPatterns("/system/table/**");
    }

    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    @Autowired
    private HttpMessageConverters httpMessageConverters;

    /**
     * MappingJackson2HttpMessageConverter 实现了HttpMessageConverter 接口，
     * httpMessageConverters.getConverters() 返回的对象里包含了MappingJackson2HttpMessageConverter
     *
     * @return
     */
    @Bean
    public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(new JacksonMapper());
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.addAll(httpMessageConverters.getConverters());
    }

    public static class JacksonMapper extends ObjectMapper {
        public JacksonMapper() {
            super();
            this.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            this.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            SimpleModule simpleModule = new SimpleModule();

            //日期格式化
            simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
            simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
            simpleModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
            simpleModule.addSerializer(Timestamp.class, new JsonSerializer<Timestamp>() {
                @Override
                public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
                    jsonGenerator.writeString(simpleDateFormat.format(timestamp));
                }
            });
            simpleModule.addSerializer(Date.class, new JsonSerializer<Date>() {
                @Override
                public void serialize(Date timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
                    jsonGenerator.writeString(simpleDateFormat.format(timestamp));
                }
            });

            // 日期反序列化
            simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT)));
            simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
            simpleModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT)));
            simpleModule.addDeserializer(Date.class, new JsonDeserializer<Date>() {
                @Override
                public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                    TreeNode treeNode = jsonParser.getCodec().readTree(jsonParser);
                    String timeStr = ((TextNode) treeNode).asText();
                    try {
                        String format;
                        if (DATE_PATTERN.matcher(timeStr).matches()) {
                            format = DEFAULT_DATE_FORMAT;
                        } else {
                            format = DEFAULT_DATE_TIME_FORMAT;
                        }
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
                        return simpleDateFormat.parse(timeStr);
                    } catch (ParseException e) {
//                        log.error("", e);
                        return null;
                    }
                }
            });

            // 自定义反序列化
            // 注意：专门为文件服务的API做处理
            simpleModule.addDeserializer(CreateResponseDTO.class, new CreateResponseDTOJsonDeserializer());
            registerModule(simpleModule);
        }
    }
}
