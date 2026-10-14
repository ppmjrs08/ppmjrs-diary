-- Fictional course catalog. Seed without replacing existing profiles or histories.
INSERT INTO m2_course(id,title,description,active) SELECT * FROM (VALUES
('CS101','Programming Basics','Java programming variables loops functions problem solving',TRUE),
('CS102','Database Basics','SQL database tables queries relational data design',TRUE),
('CS201','Machine Learning','machine learning data prediction classification models',TRUE),
('CS202','Web Development','web application Java backend HTTP API services',TRUE),
('CS203','Data Analytics','data analytics statistics charts visualization SQL',TRUE),
('CS204','Computer Networks','network protocols routing communication internet security',TRUE),
('CS301','Advanced Machine Learning','machine learning neural networks deep learning prediction',TRUE),
('CS302','Software Project Management','project planning teamwork requirements schedule risk management',TRUE),
('CS205','Cybersecurity Foundations','cybersecurity security threats encryption privacy protection',TRUE),
('CS206','Mathematics for Computing','mathematics linear algebra probability statistics calculus',TRUE),
('CS207','Algorithms and Data Structures','algorithms data structures sorting trees complexity Java',TRUE),
('CS208','User Experience Design','user experience interface design usability prototyping',TRUE),
('CS209','Cloud Computing','cloud deployment containers distributed services infrastructure',TRUE),
('CS210','Mobile Development','mobile Android Java application interface development',TRUE),
('CS303','Natural Language Processing','natural language processing text retrieval machine learning',TRUE),
('CS304','Computer Vision','computer vision images classification neural networks',TRUE),
('CS305','Database Administration','SQL database indexing transactions performance administration',TRUE),
('CS306','Network Security','network security protocols encryption firewall threats',TRUE),
('CS307','Software Testing','software testing quality automation unit integration Java',TRUE),
('CS308','Distributed Systems','distributed systems consistency replication cloud communication',TRUE)
) AS seed(id,title,description,active) WHERE NOT EXISTS(SELECT 1 FROM m2_course c WHERE c.id=seed.id);
INSERT INTO m2_prerequisite(course_id,prerequisite_id) SELECT * FROM (VALUES
('CS201','CS101'),('CS202','CS101'),('CS203','CS102'),('CS301','CS101'),('CS301','CS201'),('CS207','CS101'),('CS209','CS202'),('CS210','CS101'),('CS303','CS201'),('CS304','CS201'),('CS305','CS102'),('CS306','CS204'),('CS306','CS205'),('CS307','CS202'),('CS308','CS204'),('CS308','CS209')
) AS seed(course_id,prerequisite_id) WHERE NOT EXISTS(SELECT 1 FROM m2_prerequisite p WHERE p.course_id=seed.course_id AND p.prerequisite_id=seed.prerequisite_id);
INSERT INTO m2_completed(student_id,course_id) SELECT * FROM (VALUES ('student1','CS101'),('student2','CS101'),('student2','CS201'),('student2','CS102')) AS seed(student_id,course_id) WHERE NOT EXISTS(SELECT 1 FROM m2_completed c WHERE c.student_id=seed.student_id AND c.course_id=seed.course_id);
INSERT INTO m2_profile(student_id,interests,goals) SELECT * FROM (VALUES ('student1','machine learning',''),('student2','data analytics','')) AS seed(student_id,interests,goals) WHERE NOT EXISTS(SELECT 1 FROM m2_profile p WHERE p.student_id=seed.student_id);
