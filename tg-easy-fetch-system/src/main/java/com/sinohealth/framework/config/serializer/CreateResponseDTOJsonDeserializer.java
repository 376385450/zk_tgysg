package com.sinohealth.framework.config.serializer;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sinohealth.saas.file.model.dto.response.CreateResponseDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * @author kuangchengping@sinohealth.cn
 * 2022-11-12 11:42
 */
@Slf4j
public class CreateResponseDTOJsonDeserializer extends JsonDeserializer<CreateResponseDTO> {
    @Override
    public CreateResponseDTO deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        try {
            final String resultStr = node.toString();
            final CreateResponseDTO checkRequestDTO = JSONUtil.toBean(resultStr, CreateResponseDTO.class);
            return checkRequestDTO;
        } catch (Exception e) {
            log.error("定制反序列化方法失败:" ,e);
        }
        return null;
    }
}
