-- bcӪ�˹�����ϵͳ�Ľ���ű�,���б����븽��ǰ׺"BS_"
-- ���д˽ű�֮ǰ��������ƽ̨�Ľ���ű�framework.db.mysql.create.sql

-- ����
create table BS_CAR (
    ID int NOT NULL auto_increment,
    UNIT_ID int COMMENT '������λID',
    NAME varchar(500) NOT NULL COMMENT '����',
    DESC_ text COMMENT '��ע',
    primary key (ID)
) COMMENT='����';
