package org.tinycloud.tinyid.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.tinycloud.tinyid.bean.dto.IdTableAddDto;
import org.tinycloud.tinyid.bean.dto.IdTableEditDto;
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

    @Autowired
    private SegmentIdService segmentIdService;

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

    /**
     * 删除流水号配置，并使本实例的内存号段缓冲失效（分布式下其余实例靠自然耗尽后续段生效）
     */
    public boolean delete(Long id) {
        TIdTable old = this.idTableDao.getById(id);
        boolean result = this.idTableDao.delete(id);
        if (result && old != null) {
            this.segmentIdService.invalidate(old.getIdCode());
        }
        return result;
    }

    public boolean add(IdTableAddDto dto) {
        return this.idTableDao.add(dto);
    }


    /**
     * 修改流水号配置，并使本实例的内存号段缓冲失效（分布式下其余实例靠自然耗尽后续段生效）
     */
    public boolean edit(IdTableEditDto dto) {
        TIdTable old = this.idTableDao.getById(dto.getId());
        boolean result = this.idTableDao.edit(dto);
        if (result && old != null) {
            this.segmentIdService.invalidate(old.getIdCode());
        }
        return result;
    }
} 
