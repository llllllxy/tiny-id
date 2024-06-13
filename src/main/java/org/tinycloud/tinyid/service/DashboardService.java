package org.tinycloud.tinyid.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.bean.vo.IdTableVo;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.dao.WorkNodeDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-06-12 14:49
 */
@Service
public class DashboardService {

    @Autowired
    private WorkNodeDao workNodeDao;

    @Autowired
    private IdTableDao idTableDao;

    public Map<String, Object> quantity() {
        Map<String, Object> data = new HashMap<>();
        data.put("workNodeQuantity", workNodeDao.totalCount());
        data.put("idTableQuantity", idTableDao.totalCount());
        return data;
    }

    public List<IdTableVo> topList() {
        List<TIdTable> list = idTableDao.listTop25();
        return list.stream().map(x -> {
            IdTableVo vo = new IdTableVo();
            BeanUtils.copyProperties(x, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
