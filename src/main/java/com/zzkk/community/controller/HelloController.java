package com.zzkk.community.controller;

import com.zzkk.community.service.AlphaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author zzkk
 * @ClassName HelloController
 * @Description Todo
 **/
@Controller
@RequestMapping("/test")
public class HelloController {
    @Resource
    private AlphaService alphaService;

    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) {
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ":" + value);
        }
        System.out.println(request.getParameter("code"));

        response.setContentType("text/html;charset=utf-8");

        try (
                PrintWriter writer = response.getWriter();
        ) {
            writer.write("<h1>我要睡觉</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping("/test")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("s")
    @ResponseBody
    public String Hello() {
        return "hello today";
    }

    @RequestMapping(path = "/student", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(int current, int limit) {
        System.out.println(current + "--" + limit);
        return "students";
    }

    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(@PathVariable(name = "id") int id) {
        System.out.println(id);
        return "student";
    }

    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int id) {
        System.out.println(name + "--" + id);
        return "success";
    }

    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name", "zzkk");
        modelAndView.addObject("age", 12);
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmp() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("name", "张三");
        emp1.put("age", 18);
        emp1.put("salary", 10000);
        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("name", "李四");
        emp2.put("age", 22);
        emp2.put("salary", 13000);
        list.add(emp1);
        list.add(emp2);
        return list;
    }
}
