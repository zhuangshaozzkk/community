package com.zzkk.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @author zzkk
 * @ClassName AlphaDaoMybatisImpl
 * @Description Todo
 **/
@Repository
public class AlphaDaoMybatisImpl implements AlphaDao {
    @Override
    public String select() {
        return "Mybatis";
    }
}
