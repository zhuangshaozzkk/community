package com.zzkk.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * @author zzkk
 * @ClassName AlphaDaoHibImpl
 * @Description Todo
 **/
@Repository("hibernate")
@Primary
public class AlphaDaoHibImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}
