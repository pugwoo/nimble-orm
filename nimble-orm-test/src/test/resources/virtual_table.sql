SELECT t1.id AS id, t1.`name` AS NAME, t2.`name` AS schoolName
FROM t_student t1
LEFT JOIN t_school t2
ON t1.`school_id`=t2.`id`
WHERE t1.`deleted`=0 AND t2.`deleted`=0