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

    /**
     * 新增流水号配置。关闭前后缀开关时清空对应内容，避免残留文本在下次开启时悄然生效
     */
    public boolean add(IdTableAddDto dto) {
        this.normalizeAffix(dto.getHasPrefix(), dto.getHasSuffix(), dto::setIdPrefix, dto::setIdSuffix);
        return this.idTableDao.add(dto);
    }


    /**
     * 修改流水号配置，并使本实例的内存号段缓冲失效（分布式下其余实例靠自然耗尽后续段生效）。
     * 关闭前后缀开关时清空对应内容，避免残留文本在下次开启时悄然复活
     */
    public boolean edit(IdTableEditDto dto) {
        this.normalizeAffix(dto.getHasPrefix(), dto.getHasSuffix(), dto::setIdPrefix, dto::setIdSuffix);
        TIdTable old = this.idTableDao.getById(dto.getId());
        boolean result = this.idTableDao.edit(dto);
        if (result && old != null) {
            this.segmentIdService.invalidate(old.getIdCode());
        }
        return result;
    }

    /**
     * 前后缀内容规范化：开关未开启时清空对应内容。
     * 前后缀的开关与内容是两个独立字段，仅以开关作为是否生效的依据，
     * 内容必须随开关关闭一并清空，否则重新开启后会沿用旧值。
     *
     * @param hasPrefix    是否带前缀（1是 0否）
     * @param hasSuffix    是否带后缀（1是 0否）
     * @param prefixSetter 前缀内容写入方法
     * @param suffixSetter 后缀内容写入方法
     */
    private void normalizeAffix(Integer hasPrefix, Integer hasSuffix, java.util.function.Consumer<String> prefixSetter, java.util.function.Consumer<String> suffixSetter) {
        if (hasPrefix == null || hasPrefix != 1) {
            prefixSetter.accept(null);
        }
        if (hasSuffix == null || hasSuffix != 1) {
            suffixSetter.accept(null);
        }
    }
} 
