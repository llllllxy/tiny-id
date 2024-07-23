package org.tinycloud.tinyid.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.tinycloud.tinyid.bean.dto.IdTableQueryDto;
import org.tinycloud.tinyid.bean.entity.TIdTable;
import org.tinycloud.tinyid.bean.vo.IdTableVo;
import org.tinycloud.tinyid.dao.IdTableDao;
import org.tinycloud.tinyid.model.PageModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-07-23 14:47
 */
@Service
public class IdTableService {

    @Autowired
    private IdTableDao idTableDao;

    public PageModel<IdTableVo> query(IdTableQueryDto dto) {
        PageModel<IdTableVo> responsePage = new PageModel<>(dto.getPageNo(), dto.getPageSize());
        List<TIdTable> nodeList = this.idTableDao.pageList(dto);
        Long count = this.idTableDao.pageCount(dto);
        responsePage.setTotalCount(count);
        if (!CollectionUtils.isEmpty(nodeList)) {
            responsePage.setRecords(nodeList.stream().map(x -> {
                IdTableVo vo = new IdTableVo();
                BeanUtils.copyProperties(x, vo);
                return vo;
            }).collect(Collectors.toList()));
        }
        return responsePage;
    }

    public boolean delete(Long id) {
        return this.idTableDao.delete(id);
    }
} 
