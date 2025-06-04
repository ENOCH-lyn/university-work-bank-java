package com.example.banking.service;

import com.example.banking.model.Bank;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * DataManager
 *
 * 提供银行数据的保存与加载功能，使用JSON文件进行持久化。
 * 保存路径为当前工作目录下的data.json文件。
 */
public class DataManager {
    private static final String DATA_FILE = "data.json";
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 保存银行列表到本地JSON文件。
     *
     * @param banks 银行列表
     * @throws Exception 写入文件时发生异常
     */
    public static void save(List<Bank> banks) throws Exception {
        mapper.writeValue(new File(DATA_FILE), banks);
    }

    /**
     * 从本地JSON文件加载银行列表。
     *
     * @return 银行列表
     * @throws Exception 读取文件或解析JSON时发生异常
     */
    public static List<Bank> load() throws Exception {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(List.class, Bank.class));
        }
        return new ArrayList<>();
    }
}