package com.zzkk.community.entity;

/**
 * @author zzkk
 * @ClassName Page
 * @Description 封装分页相关的信息
 **/
public class Page {
    // 当前页码
    private int currentPage = 1;
    // 每页显示上限条目
    private int limit = 10;
    // 查询数据总数 用于计算总的页数（total/limit）
    private int total;
    // 查询的路径 (复用分页的链接)
    private String path;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        if (currentPage >= 1) {
            this.currentPage = currentPage;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        if (total >= 0) {
            this.total = total;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @Description //获取当前页偏移行（跳过）
     **/
    public int getOffset() {
        return currentPage * limit - limit;
    }

    /**
     * @Description //获取总的页数
     **/
    public int getTotalNumOfPage() {
        if (total % limit == 0) {
            return total / limit;
        } else {
            return total / limit + 1;
        }
    }

    /**
     * @Description //获取起始页码
     **/
    public int getFrom() {
        int startPage = currentPage - 2;
        return startPage > 0 ? startPage : 1;
    }

    /**
     * @Description //获取截止页码
     **/
    public int getTo() {
        int endPage = currentPage + 2;
        int totalNumOfPage = getTotalNumOfPage();
        return endPage < totalNumOfPage ? endPage : totalNumOfPage;
    }
}
