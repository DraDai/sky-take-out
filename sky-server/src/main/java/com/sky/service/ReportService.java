package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface ReportService {
    /**
     * 营业额统计
     * @param begin 开始日期
     * @param end 结束日期
     * @return 营业额统计数据
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     * @param begin 开始日期
     * @param end 结束日期
     * @return 用户统计数据
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO orderStatistics(LocalDate begin, LocalDate end);

    /**
     * 销售额前10统计
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);
}
