-- 为举报表添加用户填写的举报内容字段
ALTER TABLE afd.reports ADD COLUMN report_content TEXT COMMENT '用户填写的详细举报内容' AFTER reason;

-- 修改reason字段注释，明确其为举报原因选项
ALTER TABLE afd.reports MODIFY COLUMN reason VARCHAR(500) NOT NULL COMMENT '举报原因(从预设选项中选择)'; 