package com.zzkk.community.controller;

import com.zzkk.community.service.DataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author zzkk
 * @ClassName DataController
 * @Description Todo
 **/
@Controller
public class DataController {
    @Resource
    private DataService dataService;

    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern= "yyyy-MM-dd") Date start , @DateTimeFormat(pattern= "yyyy-MM-dd") Date end , Model model){
        Long uv = dataService.calculateUV(start, end);
        model.addAttribute("uv",uv);
        model.addAttribute("uvstart",start);
        model.addAttribute("uvend",end);
        return "forward:/data";
    }

    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern= "yyyy-MM-dd") Date start , @DateTimeFormat(pattern= "yyyy-MM-dd") Date end , Model model){
        Long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dau",dau);
        model.addAttribute("daustart",start);
        model.addAttribute("dauend",end);
        return "forward:/data";
    }
}
