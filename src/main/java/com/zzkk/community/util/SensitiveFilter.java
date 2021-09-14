package com.zzkk.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sun.text.normalizer.Trie;

import javax.annotation.PostConstruct;
import javax.swing.tree.TreeNode;
import javax.xml.soap.Node;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zzkk
 * @ClassName SensitiveFilter
 * @Description 敏感词过滤工具类
 **/
@Component
public class SensitiveFilter {
    // 日志
    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换符
    private static final String REPLACEMENT = "*";

    // 根节点
    private  TireNode root = new TireNode();

    // 在容器加载bean后执行
    @PostConstruct
    public void init() {
        try (
                // 读取敏感字符
                // 字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                // 字符流
                BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyWord;
            while((keyWord=bf.readLine())!=null){
                // 添加到前缀树 初始化
                this.addKeyWord(keyWord);
            }
        } catch (Exception e) {
            LOGGER.error("加载敏感词失败" + e.getMessage());
        }
    }

    // 过滤敏感词
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }
        // 指针1
        TireNode tempNode = root;
        // 指针2
        int begin =0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (begin < text.length()){
            if(position< text.length()){
                Character c = text.charAt(position);
                // 跳过符号
                if(isSymbol(c)){
                    if(tempNode == root){
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNodes(c);
                if(tempNode == null){
                    // 以begin开头的字符串不是敏感词
                    sb.append(text.charAt(begin));
                    // 进入下一个位置
                    position = ++begin;
                    // 重新指向根节点
                    tempNode = root;
                }
                // 发现敏感词
                else if(tempNode.isKeywordEnd()){
                    sb.append(REPLACEMENT);
                    begin = ++position;
                }
                // 检查下一个字符
                else{
                    position++;
                }
            }
            // position遍历越界仍未匹配到敏感词
            else{
                sb.append(text.charAt(begin));
                position = ++begin;
                tempNode = root;
            }
        }
        return sb.toString();
    }


    // 判断是否为特殊符号
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c)&&(c<0x2E80 || c>0x9FFF);
    }

    // 添加敏感词到前缀树中
    private void addKeyWord(String keyWord) {
        TireNode temp = root;
        for (int i = 0; i < keyWord.length(); i++) {
            char c = keyWord.charAt(i);
            TireNode subNode = temp.getSubNodes(c);
            if(subNode == null){
                subNode=new TireNode();
                temp.setSubNodes(c,subNode);
            }
            // 当前节点指向子节点
            temp = subNode;
            // 设置结束标志
            if(i == keyWord.length()-1){
                temp.setKeywordEnd(true);
            }
        }
    }

    // 前缀树结构
    private class TireNode {
        // 关键词结束的标志
        private boolean isKeywordEnd;

        // 子节点(key是下级字符，value是下级节点)
        private Map<Character, TireNode> subNodes = new HashMap<>();

        public void setSubNodes(Character c, TireNode trie) {
            subNodes.put(c, trie);
        }

        public TireNode getSubNodes(Character c) {
            return subNodes.get(c);
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
    }
}
