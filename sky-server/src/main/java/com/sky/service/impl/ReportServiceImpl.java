package com.sky.service.impl;

import com.sky.apidto.GeoCodeDTO;
import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final OrderDetailMapper orderDetailMapper;
    @Autowired
    public ReportServiceImpl(OrderMapper orderMapper, UserMapper userMapper, OrderDetailMapper orderDetailMapper) {

        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.orderDetailMapper = orderDetailMapper;
    }

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //获取开始日期和结束日期之间的所有日期
        List<LocalDate> dates = begin.datesUntil(end.plusDays(1)).collect(Collectors.toList());
        List<Integer> counts = new ArrayList<>();

        // 遍历日期列表，查询每个日期的营业额
        dates.forEach(date -> {
            // 构建查询时间范围
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime theEnd = date.plusDays(1).atStartOfDay();

            // 查询营业额
            Integer count = orderMapper.selectTurnoverByDate(start, theEnd);

            if(count == null){
                counts.add(0);
            }else {
                // 将查询结果添加到列表中
                counts.add(count);
            }

        });

        // 将日期和营业额转换为字符串
        String datesString = dates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.joining(","));
        String countsString = counts.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        // 构建返回对象
        return TurnoverReportVO.builder()
                .dateList(datesString)
                .turnoverList(countsString)
                .build();
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = begin.datesUntil(end.plusDays(1))
                .collect(Collectors.toList());

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalList = new ArrayList<>();

        dates.forEach(date -> {
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();

            Integer newUserCount = userMapper.selectDayNewUserCount(startTime, endTime);
            Integer allUserCount = userMapper.selectAllUserCount(endTime);

            newUserList.add(newUserCount);
            totalList.add(allUserCount);
        });

        String dateString = dates.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String newUserString = newUserList.stream().map(String::valueOf).collect(Collectors.joining(","));
        String totalUserString = totalList.stream().map(String::valueOf).collect(Collectors.joining(","));

        return UserReportVO.builder()
                .dateList(dateString)
                .newUserList(newUserString)
                .totalUserList(totalUserString)
                .build();
    }

    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dates = begin.datesUntil(end.plusDays(1)).collect(Collectors.toList());
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = orderMapper.allCount();
        Integer validOrderCount = orderMapper.validCount();
        Double orderCompletionRate = (double)validOrderCount / (double)totalOrderCount;

        dates.forEach(date -> {
            LocalDateTime startTime = date.atStartOfDay();
            LocalDateTime endTime = date.plusDays(1).atStartOfDay();

            Integer orderCount = orderMapper.selectOrderCountByDate(startTime, endTime);
            Integer theValidOrderCount = orderMapper.selectValidOrderCountByDate(startTime, endTime);

            orderCountList.add(orderCount);
            validOrderCountList.add(theValidOrderCount);
        });

        String datesString = dates.stream().map(LocalDate::toString).collect(Collectors.joining(","));
        String orderCountString = orderCountList.stream().map(String::valueOf).collect(Collectors.joining(","));
        String validOrderCountString = validOrderCountList.stream().map(String::valueOf).collect(Collectors.joining(","));

        return OrderReportVO.builder()
                .dateList(datesString)
                .orderCountList(orderCountString)
                .validOrderCountList(validOrderCountString)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        LocalDateTime startTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();

        List<Long> ids = orderMapper.selectOrderIdsByDate(startTime, endTime);
        List<GoodsSalesDTO> goodsSalesDTOS = orderDetailMapper.selectGoodsSalesByOrderIds(ids);

        String names = goodsSalesDTOS.stream()
                .map(GoodsSalesDTO::getName)
                .collect(Collectors.joining(","));

        String numbers = goodsSalesDTOS.stream()
                .map(goodsSalesDTO -> {
                    Integer number = goodsSalesDTO.getNumber();
                    return String.valueOf(number);
                }).collect(Collectors.joining(","));

        return SalesTop10ReportVO.builder()
                .nameList(names)
                .numberList(numbers)
                .build();
    }
}
