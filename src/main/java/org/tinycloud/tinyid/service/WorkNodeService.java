package org.tinycloud.tinyid.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.tinycloud.tinyid.bean.dto.WorkNodeQueryDto;
import org.tinycloud.tinyid.bean.entity.TWorkNode;
import org.tinycloud.tinyid.bean.vo.WorkNodeVo;
import org.tinycloud.tinyid.dao.WorkNodeDao;
import org.tinycloud.tinyid.model.PageModel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @author liuxingyu01
 * @since 2024-05-29 14:52
 */
@Service
public class WorkNodeService {

    @Autowired
    private WorkNodeDao workNodeDao;


    public PageModel<WorkNodeVo> query(WorkNodeQueryDto dto) {
        PageModel<WorkNodeVo> responsePage = new PageModel<>(dto.getPageNo(), dto.getPageSize());
        List<TWorkNode> nodeList = this.workNodeDao.pageList(dto);
        Long count = this.workNodeDao.pageCount(dto);
        responsePage.setTotalCount(count);
        if (!CollectionUtils.isEmpty(nodeList)) {
            responsePage.setRecords(nodeList.stream().map(x -> {
                WorkNodeVo vo = new WorkNodeVo();
                BeanUtils.copyProperties(x, vo);
                return vo;
            }).collect(Collectors.toList()));
        }
        return responsePage;
    }
}
