

# 1.8.5 快照表-技术优化

https://ffepze3njn.feishu.cn/docx/F80bdrqYMop2erxfJ5rch5tonyQ?from=from_copylink

## Task

1. 建表调整
    1. 建表不使用复制local及分布式表，而是使用简单的单节点单表Merge引擎 DONE
    1. 保存路由关系 DONE
    1. 尚书台建的表还是分布式表，如何处理
       1. 修改尚书台逻辑 实现路由建表
       1. 定时处理此类资产表转换成为本地表
1. 查询调整 候选查询 DONE
1. 备份表建立 DONE
2. 流导出 DONE
2. BI数据视图同步逻辑 DONE
1. 过期后的删除逻辑（删CK）DONE
1. 历史问题： .getConnection().getSchema() 连接泄漏修复 DONE
2. 定时转换原有的资产表为快照表
3. BI 资产表不建复制表 但是 交付客户使用复制表 DONE

