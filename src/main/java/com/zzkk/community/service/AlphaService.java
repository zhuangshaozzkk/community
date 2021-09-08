package com.zzkk.community.service;

import com.zzkk.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zzkk
 * @ClassName AlphaService
 * @Description Todo
 **/
@Service
public class AlphaService {
    @Resource
    private AlphaDao alphaDao;

    public String find(){
        return alphaDao.select();
    }
}
